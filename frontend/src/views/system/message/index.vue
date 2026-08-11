<template>
  <div class="art-full-height">
    <MessageSearch v-model="searchForm" @search="handleSearch" @reset="handleReset" />

    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshAll">
        <template #left>
          <ElButton v-auth="'send'" @click="sendDialogVisible = true">
            {{ $t('systemManage.message.send') }}
          </ElButton>
          <ElButton :disabled="unreadCount === 0" @click="readAll">
            {{ $t('systemManage.message.readAll') }}
          </ElButton>
          <span class="ml-3 text-sm text-g-600">
            {{ $t('systemManage.message.unreadCount', { count: unreadCount }) }}
          </span>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />

      <MessageSendDialog v-model="sendDialogVisible" @success="refreshAll" />

      <ElDialog v-model="detailVisible" :title="currentMessage?.title" width="620px" align-center>
        <div v-if="currentMessage" class="message-detail">
          <div class="flex-c gap-2 mb-4 text-sm text-g-500">
            <ElTag :type="typeConfig(currentMessage.type).tag">
              {{ typeConfig(currentMessage.type).label }}
            </ElTag>
            <span>{{ currentMessage.sender }}</span>
            <span>{{ formatDateTime(currentMessage.createdAt) }}</span>
          </div>
          <p class="whitespace-pre-wrap leading-7 text-g-800">{{ currentMessage.content }}</p>
        </div>
      </ElDialog>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ElButton, ElMessageBox, ElSpace, ElTag } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import { mittBus } from '@/utils/sys'
  import {
    fetchGetMessageList,
    fetchGetUnreadMessageCount,
    fetchReadAllMessages,
    fetchReadMessage
  } from '@/api/operations'
  import MessageSearch from './modules/message-search.vue'
  import MessageSendDialog from './modules/message-send-dialog.vue'

  defineOptions({ name: 'MessageCenter' })

  type MessageItem = Api.SystemManage.MessageListItem
  const { t } = useI18n()
  const unreadCount = ref(0)
  const sendDialogVisible = ref(false)
  const detailVisible = ref(false)
  const currentMessage = ref<MessageItem>()
  const searchForm = ref<Api.SystemManage.MessageSearchParams>({})
  const formatDateTime = (value: string) => value?.replace('T', ' ') || '-'
  const typeConfig = (type: Api.SystemManage.MessageType) => {
    const configs = {
      SYSTEM: { label: t('systemManage.message.types.system'), tag: 'danger' as const },
      REMINDER: { label: t('systemManage.message.types.reminder'), tag: 'warning' as const },
      TASK: { label: t('systemManage.message.types.task'), tag: 'info' as const }
    }
    return configs[type]
  }

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    replaceSearchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchGetMessageList,
      apiParams: { current: 1, size: 20 },
      columnsFactory: () => [
        { type: 'globalIndex', width: 70, label: t('common.index') },
        {
          prop: 'title',
          label: t('systemManage.message.fields.title'),
          minWidth: 300,
          formatter: (row) =>
            h('div', { class: ['message-title', { unread: !row.read }] }, [
              !row.read ? h('span', { class: 'unread-dot' }) : null,
              h('span', row.title)
            ])
        },
        {
          prop: 'type',
          label: t('systemManage.message.fields.type'),
          width: 110,
          formatter: (row) => {
            const config = typeConfig(row.type)
            return h(ElTag, { type: config.tag }, () => config.label)
          }
        },
        { prop: 'sender', label: t('systemManage.message.fields.sender'), width: 120 },
        {
          prop: 'read',
          label: t('systemManage.message.fields.readStatus'),
          width: 100,
          formatter: (row) =>
            h(ElTag, { type: row.read ? 'info' : 'success', effect: 'plain' }, () =>
              row.read ? t('systemManage.message.read') : t('systemManage.message.unread')
            )
        },
        {
          prop: 'createdAt',
          label: t('systemManage.message.fields.createdAt'),
          width: 180,
          formatter: (row) => formatDateTime(row.createdAt)
        },
        {
          prop: 'operation',
          label: t('common.operations'),
          width: 150,
          fixed: 'right',
          formatter: (row) =>
            h(ElSpace, { size: 4 }, () => [
              h(ElButton, { link: true, type: 'primary', onClick: () => openMessage(row) }, () =>
                t('systemManage.message.view')
              ),
              !row.read
                ? h(ElButton, { link: true, type: 'success', onClick: () => markRead(row) }, () =>
                    t('systemManage.message.markRead')
                  )
                : null
            ])
        }
      ]
    }
  })

  const loadUnreadCount = async () => {
    unreadCount.value = await fetchGetUnreadMessageCount()
    mittBus.emit('messageUnreadChange', unreadCount.value)
  }

  const refreshAll = async () => {
    await Promise.all([refreshData(), loadUnreadCount()])
  }

  const handleSearch = (params: Api.SystemManage.MessageSearchParams) => {
    replaceSearchParams(params)
    getData()
  }

  const handleReset = () => {
    resetSearchParams()
    loadUnreadCount()
  }

  const markRead = async (row: MessageItem) => {
    if (row.read) return
    await fetchReadMessage(row.receiverId)
    row.read = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    mittBus.emit('messageUnreadChange', unreadCount.value)
  }

  const openMessage = async (row: MessageItem) => {
    await markRead(row)
    currentMessage.value = row
    detailVisible.value = true
  }

  const readAll = () => {
    ElMessageBox.confirm(
      t('systemManage.message.readAllConfirm'),
      t('systemManage.message.readAll'),
      {
        type: 'info',
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel')
      }
    ).then(async () => {
      await fetchReadAllMessages()
      await refreshAll()
    })
  }

  onMounted(loadUnreadCount)
</script>

<style scoped lang="scss">
  :deep(.message-title) {
    display: flex;
    align-items: center;
    gap: 8px;

    &.unread {
      font-weight: 600;
    }

    .unread-dot {
      width: 7px;
      height: 7px;
      flex: none;
      border-radius: 999px;
      background: var(--el-color-danger);
    }
  }
</style>
