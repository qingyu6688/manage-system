<template>
  <div class="art-full-height">
    <FileSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElUpload
            v-auth="'upload'"
            :show-file-list="false"
            :http-request="uploadFile"
            :before-upload="beforeUpload"
            accept=".jpg,.jpeg,.png,.gif,.webp,.pdf,.txt,.csv,.md,.zip,.docx,.xlsx,.pptx"
          >
            <ElButton :loading="uploading">
              {{ $t('systemManage.file.upload') }}
            </ElButton>
          </ElUpload>
          <span class="ml-3 text-xs text-g-500">{{ $t('systemManage.file.uploadHint') }}</span>
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
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import FileSaver from 'file-saver'
  import { ElButton, ElMessage, ElMessageBox, ElSpace, ElTag } from 'element-plus'
  import type { UploadProps, UploadRequestOptions } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchDeleteFile,
    fetchDownloadFile,
    fetchGetFileList,
    fetchUploadFile
  } from '@/api/operations'
  import FileSearch from './modules/file-search.vue'

  defineOptions({ name: 'FileManage' })

  const MAX_FILE_SIZE = 20 * 1024 * 1024
  const { t } = useI18n()
  const uploading = ref(false)
  const searchForm = ref<Api.SystemManage.ManagedFileSearchParams>({})
  const formatDateTime = (value: string) => value?.replace('T', ' ') || '-'
  const formatFileSize = (size: number) => {
    if (size < 1024) return `${size} B`
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
    return `${(size / 1024 / 1024).toFixed(1)} MB`
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
    refreshRemove
  } = useTable({
    core: {
      apiFn: fetchGetFileList,
      apiParams: { current: 1, size: 20 },
      columnsFactory: () => [
        { type: 'globalIndex', width: 70, label: t('common.index') },
        { prop: 'originalName', label: t('systemManage.file.fields.name'), minWidth: 260 },
        {
          prop: 'contentType',
          label: t('systemManage.file.fields.type'),
          minWidth: 210,
          formatter: (row) => h(ElTag, { type: 'info', effect: 'plain' }, () => row.contentType)
        },
        {
          prop: 'fileSize',
          label: t('systemManage.file.fields.size'),
          width: 110,
          formatter: (row) => formatFileSize(row.fileSize)
        },
        { prop: 'uploader', label: t('systemManage.file.fields.uploader'), width: 120 },
        {
          prop: 'createdAt',
          label: t('systemManage.file.fields.createdAt'),
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
              h(ElButton, { link: true, type: 'primary', onClick: () => downloadFile(row) }, () =>
                t('systemManage.file.download')
              ),
              h(ElButton, { link: true, type: 'danger', onClick: () => deleteFile(row) }, () =>
                t('common.delete')
              )
            ])
        }
      ]
    }
  })

  const handleSearch = (params: Api.SystemManage.ManagedFileSearchParams) => {
    replaceSearchParams(params)
    getData()
  }

  const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    if (file.size <= MAX_FILE_SIZE) return true
    ElMessage.error(t('systemManage.file.sizeExceeded'))
    return false
  }

  const uploadFile = async (options: UploadRequestOptions) => {
    uploading.value = true
    try {
      const result = await fetchUploadFile(options.file)
      options.onSuccess(result)
      await refreshCreate()
    } finally {
      uploading.value = false
    }
  }

  const downloadFile = async (row: Api.SystemManage.ManagedFileListItem) => {
    const blob = await fetchDownloadFile(row.id)
    FileSaver.saveAs(blob, row.originalName)
  }

  const deleteFile = (row: Api.SystemManage.ManagedFileListItem) => {
    ElMessageBox.confirm(
      t('systemManage.file.deleteConfirm', { name: row.originalName }),
      t('systemManage.file.deleteTitle'),
      {
        type: 'warning',
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel')
      }
    ).then(async () => {
      await fetchDeleteFile(row.id)
      await refreshRemove()
    })
  }
</script>
