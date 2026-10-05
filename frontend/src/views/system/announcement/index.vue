<template>
  <div class="announcement-page art-full-height">
    <AnnouncementSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton v-auth="'add'" @click="showDialog('add')" v-ripple>
            {{ $t('systemManage.announcement.addButton') }}
          </ElButton>
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

      <AnnouncementDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :announcement-data="currentAnnouncement"
        @success="handleDialogSuccess"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useAuth } from '@/hooks/core/useAuth'
  import { ElButton, ElMessageBox, ElSpace, ElTag } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import type { DialogType } from '@/types'
  import {
    fetchDeleteAnnouncement,
    fetchGetAnnouncementList,
    fetchUpdateAnnouncementStatus
  } from '@/api/system-manage'
  import AnnouncementSearch from './modules/announcement-search.vue'
  import AnnouncementDialog from './modules/announcement-dialog.vue'

  defineOptions({ name: 'Announcement' })

  type AnnouncementListItem = Api.SystemManage.AnnouncementListItem

  const { t } = useI18n()
  const { hasAuth } = useAuth()
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentAnnouncement = ref<Partial<AnnouncementListItem>>({})
  const searchForm = ref<Api.SystemManage.AnnouncementSearchParams>({
    title: undefined,
    type: undefined,
    status: undefined
  })

  const getTypeConfig = (type: Api.SystemManage.AnnouncementType) => {
    const configs = {
      SYSTEM: { type: 'danger' as const, text: t('systemManage.announcement.types.system') },
      ACTIVITY: { type: 'warning' as const, text: t('systemManage.announcement.types.activity') },
      REMINDER: { type: 'info' as const, text: t('systemManage.announcement.types.reminder') }
    }
    return configs[type]
  }

  const formatDateTime = (value: string | null): string => {
    return value ? value.replace('T', ' ') : '-'
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
    refreshData,
    refreshCreate,
    refreshUpdate,
    refreshRemove
  } = useTable({
    core: {
      apiFn: fetchGetAnnouncementList,
      apiParams: {
        current: 1,
        size: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'index', width: 60, label: t('common.index') },
        {
          prop: 'title',
          label: t('systemManage.announcement.fields.title'),
          minWidth: 320,
          formatter: (row) =>
            h('div', { class: 'announcement-title-cell' }, [
              h('div', { class: 'title-line' }, [
                row.top
                  ? h(ElTag, { type: 'danger', size: 'small', class: 'mr-2' }, () =>
                      t('systemManage.announcement.top')
                    )
                  : null,
                h('span', { class: 'font-medium' }, row.title)
              ]),
              h('p', { class: 'content-preview' }, row.content)
            ])
        },
        {
          prop: 'type',
          label: t('systemManage.announcement.fields.type'),
          width: 110,
          formatter: (row) => {
            const config = getTypeConfig(row.type)
            return h(ElTag, { type: config.type }, () => config.text)
          }
        },
        {
          prop: 'status',
          label: t('systemManage.announcement.fields.status'),
          width: 110,
          formatter: (row) =>
            h(ElTag, { type: row.status === 'PUBLISHED' ? 'success' : 'info' }, () =>
              row.status === 'PUBLISHED'
                ? t('systemManage.announcement.statuses.published')
                : t('systemManage.announcement.statuses.draft')
            )
        },
        {
          prop: 'publishedBy',
          label: t('systemManage.announcement.fields.publisher'),
          width: 120,
          formatter: (row) => row.publishedBy || '-'
        },
        {
          prop: 'publishedAt',
          label: t('systemManage.announcement.fields.publishedAt'),
          width: 180,
          formatter: (row) => formatDateTime(row.publishedAt)
        },
        {
          prop: 'operation',
          label: t('common.operations'),
          width: 245,
          fixed: 'right',
          formatter: (row) =>
            h(ElSpace, { size: 4 }, () => [
              hasAuth('publish') &&
                h(
                  ElButton,
                  {
                    link: true,
                    type: row.status === 'PUBLISHED' ? 'warning' : 'success',
                    onClick: () => toggleStatus(row)
                  },
                  () =>
                    row.status === 'PUBLISHED'
                      ? t('systemManage.announcement.unpublish')
                      : t('systemManage.announcement.publish')
                ),
              hasAuth('edit') &&
                (row.status !== 'PUBLISHED' || hasAuth('publish')) &&
                h(
                  ElButton,
                  { link: true, type: 'primary', onClick: () => showDialog('edit', row) },
                  () => t('common.edit')
                ),
              hasAuth('delete') &&
                h(
                  ElButton,
                  { link: true, type: 'danger', onClick: () => deleteAnnouncement(row) },
                  () => t('common.delete')
                )
            ])
        }
      ]
    }
  })

  const handleSearch = (params: Api.SystemManage.AnnouncementSearchParams): void => {
    replaceSearchParams(params)
    getData()
  }

  const showDialog = (type: DialogType, row?: AnnouncementListItem): void => {
    dialogType.value = type
    currentAnnouncement.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const handleDialogSuccess = async (): Promise<void> => {
    dialogVisible.value = false
    currentAnnouncement.value = {}
    if (dialogType.value === 'add') {
      await refreshCreate()
      return
    }
    await refreshUpdate()
  }

  const toggleStatus = (row: AnnouncementListItem): void => {
    const nextStatus: Api.SystemManage.AnnouncementStatus =
      row.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED'
    const publishing = nextStatus === 'PUBLISHED'
    ElMessageBox.confirm(
      publishing
        ? t('systemManage.announcement.publishConfirm', { title: row.title })
        : t('systemManage.announcement.unpublishConfirm', { title: row.title }),
      publishing
        ? t('systemManage.announcement.publishTitle')
        : t('systemManage.announcement.unpublishTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: publishing ? 'success' : 'warning'
      }
    ).then(async () => {
      await fetchUpdateAnnouncementStatus(row.id, nextStatus)
      await refreshUpdate()
    })
  }

  const deleteAnnouncement = (row: AnnouncementListItem): void => {
    ElMessageBox.confirm(
      t('systemManage.announcement.deleteConfirm', { title: row.title }),
      t('systemManage.announcement.deleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'error'
      }
    ).then(async () => {
      await fetchDeleteAnnouncement(row.id)
      await refreshRemove()
    })
  }
</script>

<style scoped lang="scss">
  :deep(.announcement-title-cell) {
    min-width: 0;

    .title-line {
      display: flex;
      align-items: center;
      min-width: 0;
    }

    .content-preview {
      max-width: 520px;
      margin-top: 5px;
      overflow: hidden;
      color: var(--art-gray-600);
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
</style>
