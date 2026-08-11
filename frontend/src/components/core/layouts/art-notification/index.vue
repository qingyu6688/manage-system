<template>
  <div
    v-show="visible"
    class="art-notification-panel art-card-sm !shadow-xl"
    :style="{
      transform: show ? 'scaleY(1)' : 'scaleY(0.92)',
      opacity: show ? 1 : 0
    }"
    @click.stop
  >
    <div class="flex-cb px-4 py-3.5 border-b-d">
      <div>
        <p class="text-base font-medium text-g-800">{{ $t('notice.title') }}</p>
        <p class="mt-1 text-xs text-g-500">
          {{ $t('systemManage.message.unreadCount', { count: unreadCount }) }}
        </p>
      </div>
      <ElButton
        link
        type="primary"
        :disabled="unreadCount === 0"
        :loading="markingAll"
        @click="markAllRead"
      >
        {{ $t('systemManage.message.readAll') }}
      </ElButton>
    </div>

    <div v-loading="loading" class="notification-list scrollbar-thin">
      <button
        v-for="item in messages"
        :key="item.receiverId"
        type="button"
        class="message-item"
        @click="openMessage(item)"
      >
        <span class="message-icon" :class="typeConfig(item.type).className">
          <ArtSvgIcon :icon="typeConfig(item.type).icon" />
        </span>
        <span class="min-w-0 flex-1 text-left">
          <span class="flex-c gap-2">
            <span v-if="!item.read" class="unread-dot"></span>
            <span
              class="truncate text-sm"
              :class="item.read ? 'text-g-700' : 'font-medium text-g-900'"
            >
              {{ item.title }}
            </span>
          </span>
          <span class="mt-1.5 block truncate text-xs text-g-500">
            {{ item.sender }} · {{ formatDateTime(item.createdAt) }}
          </span>
        </span>
      </button>

      <ElEmpty
        v-if="!loading && messages.length === 0"
        :description="$t('systemManage.message.empty')"
        :image-size="90"
      />
    </div>

    <div class="px-4 py-3 border-t-d">
      <ElButton class="w-full" @click="viewAll">
        {{ $t('notice.viewAll') }}
      </ElButton>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router'
  import {
    fetchGetMessageList,
    fetchGetUnreadMessageCount,
    fetchReadAllMessages,
    fetchReadMessage
  } from '@/api/operations'
  import { mittBus } from '@/utils/sys'

  const props = withDefaults(defineProps<{ value?: boolean }>(), { value: false })
  const emit = defineEmits<{
    (event: 'update:value', value: boolean): void
    (event: 'unread-change', value: number): void
  }>()
  const router = useRouter()
  const visible = ref(false)
  const show = ref(false)
  const loading = ref(false)
  const markingAll = ref(false)
  const messages = ref<Api.SystemManage.MessageListItem[]>([])
  const unreadCount = ref(0)

  const typeConfig = (type: Api.SystemManage.MessageType) => {
    const configs = {
      SYSTEM: { icon: 'ri:notification-3-line', className: 'system' },
      REMINDER: { icon: 'ri:alarm-warning-line', className: 'reminder' },
      TASK: { icon: 'ri:task-line', className: 'task' }
    }
    return configs[type]
  }
  const formatDateTime = (value: string) => value?.replace('T', ' ') || '-'

  const updateUnreadCount = (value: number) => {
    unreadCount.value = value
    emit('unread-change', value)
    mittBus.emit('messageUnreadChange', value)
  }

  const loadMessages = async () => {
    loading.value = true
    try {
      const [page, count] = await Promise.all([
        fetchGetMessageList({ current: 1, size: 10 }),
        fetchGetUnreadMessageCount()
      ])
      messages.value = page.records
      updateUnreadCount(count)
    } finally {
      loading.value = false
    }
  }

  const showPanel = (open: boolean) => {
    if (open) {
      visible.value = true
      requestAnimationFrame(() => {
        show.value = true
      })
      loadMessages()
      return
    }
    show.value = false
    window.setTimeout(() => {
      visible.value = false
    }, 250)
  }

  const markAllRead = async () => {
    markingAll.value = true
    try {
      await fetchReadAllMessages()
      messages.value.forEach((item) => {
        item.read = true
      })
      updateUnreadCount(0)
    } finally {
      markingAll.value = false
    }
  }

  const openMessage = async (item: Api.SystemManage.MessageListItem) => {
    if (!item.read) {
      await fetchReadMessage(item.receiverId)
      item.read = true
      updateUnreadCount(Math.max(0, unreadCount.value - 1))
    }
    await viewAll()
  }

  const viewAll = async () => {
    emit('update:value', false)
    await router.push('/system/message')
  }

  watch(() => props.value, showPanel)
  onMounted(async () => {
    try {
      updateUnreadCount(await fetchGetUnreadMessageCount())
    } catch {
      updateUnreadCount(0)
    }
  })
</script>

<style scoped lang="scss">
  .art-notification-panel {
    position: absolute;
    top: 58px;
    right: 20px;
    z-index: 1000;
    width: 360px;
    overflow: hidden;
    transform-origin: top;
    transition:
      transform 0.25s ease,
      opacity 0.2s ease;
  }

  .notification-list {
    min-height: 260px;
    max-height: 390px;
    overflow-y: auto;
  }

  .message-item {
    display: flex;
    width: 100%;
    gap: 12px;
    align-items: center;
    padding: 13px 16px;
    border: 0;
    border-bottom: 1px solid var(--art-card-border);
    background: transparent;
    cursor: pointer;

    &:hover {
      background: var(--el-fill-color-light);
    }
  }

  .message-icon {
    display: flex;
    width: 36px;
    height: 36px;
    flex: none;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    font-size: 18px;

    &.system {
      color: var(--el-color-danger);
      background: var(--el-color-danger-light-9);
    }

    &.reminder {
      color: var(--el-color-warning);
      background: var(--el-color-warning-light-9);
    }

    &.task {
      color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
    }
  }

  .unread-dot {
    width: 7px;
    height: 7px;
    flex: none;
    border-radius: 999px;
    background: var(--el-color-danger);
  }

  .scrollbar-thin::-webkit-scrollbar {
    width: 5px;
  }

  @media screen and (width <= 640px) {
    .art-notification-panel {
      top: 65px;
      right: 0;
      width: 100%;
    }
  }
</style>
