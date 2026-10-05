<template>
  <div class="art-full-height log-page">
    <div class="log-toolbar">
      <div class="log-toolbar__switcher">
        <ElSegmented
          v-model="activeLogType"
          class="log-type-switch"
          :options="logTypeOptions"
          :aria-label="$t('systemManage.log.splitHint')"
        />
        <span class="log-toolbar__hint">{{ $t('systemManage.log.splitHint') }}</span>
      </div>
      <ElButton v-auth="'clear'" type="danger" plain @click="clearLogs">
        {{ $t('systemManage.log.clear') }}
      </ElButton>
    </div>

    <ElCard
      class="log-panel"
      :class="activeLogType === 'OPERATION' ? 'operation-log-panel' : 'login-log-panel'"
      shadow="never"
    >
      <template #header>
        <div class="log-panel__header">
          <div>
            <div class="log-panel__title">
              <span
                class="log-panel__marker"
                :class="
                  activeLogType === 'OPERATION'
                    ? 'log-panel__marker--operation'
                    : 'log-panel__marker--login'
                "
              ></span>
              {{
                activeLogType === 'OPERATION'
                  ? $t('systemManage.log.logTypes.operation')
                  : $t('systemManage.log.logTypes.login')
              }}
            </div>
            <p>
              {{
                activeLogType === 'OPERATION'
                  ? $t('systemManage.log.operationDescription')
                  : $t('systemManage.log.loginDescription')
              }}
            </p>
          </div>
          <ElTag :type="activeLogType === 'OPERATION' ? 'primary' : 'success'" effect="plain">
            {{ activeLogType === 'OPERATION' ? operationPagination.total : loginPagination.total }}
          </ElTag>
        </div>
      </template>

      <template v-if="activeLogType === 'OPERATION'">
        <LogSearch
          v-model="operationSearchForm"
          mode="operation"
          @search="handleOperationSearch"
          @reset="operationResetSearchParams"
        />

        <ArtTableHeader
          v-model:columns="operationColumnChecks"
          :loading="operationLoading"
          full-class="operation-log-panel"
          @refresh="operationRefreshData"
        />

        <ArtTable
          :loading="operationLoading"
          :data="operationData"
          :columns="operationColumns"
          :pagination="operationPagination"
          @pagination:size-change="operationHandleSizeChange"
          @pagination:current-change="operationHandleCurrentChange"
        />
      </template>

      <template v-else>
        <LogSearch
          v-model="loginSearchForm"
          mode="login"
          @search="handleLoginSearch"
          @reset="loginResetSearchParams"
        />

        <ArtTableHeader
          v-model:columns="loginColumnChecks"
          :loading="loginLoading"
          full-class="login-log-panel"
          @refresh="loginRefreshData"
        />

        <ArtTable
          :loading="loginLoading"
          :data="loginData"
          :columns="loginColumns"
          :pagination="loginPagination"
          @pagination:size-change="loginHandleSizeChange"
          @pagination:current-change="loginHandleCurrentChange"
        />
      </template>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ElButton, ElMessageBox, ElSegmented, ElTag } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchClearOperationLogs,
    fetchDeleteOperationLog,
    fetchGetOperationLogList
  } from '@/api/operations'
  import LogSearch from './modules/log-search.vue'
  import { isHandledRejection } from '@/utils/http/error'

  defineOptions({ name: 'OperationLog' })

  type LogType = Api.SystemManage.OperationLogType
  type LogSearchParams = Api.SystemManage.OperationLogSearchParams
  type LogListItem = Api.SystemManage.OperationLogListItem

  const { t } = useI18n()
  const activeLogType = ref<LogType>('OPERATION')
  const logTypeOptions = computed(() => [
    { label: t('systemManage.log.logTypes.operation'), value: 'OPERATION' },
    { label: t('systemManage.log.logTypes.login'), value: 'LOGIN' }
  ])
  const operationSearchForm = ref<LogSearchParams>({})
  const loginSearchForm = ref<LogSearchParams>({})
  const formatDateTime = (value: string): string => value?.replace('T', ' ') || '-'

  const fetchOperationLogList = (params: LogSearchParams) =>
    fetchGetOperationLogList({ ...params, logType: 'OPERATION' })

  const fetchLoginLogList = (params: LogSearchParams) =>
    fetchGetOperationLogList({ ...params, logType: 'LOGIN' })

  const {
    columns: operationColumns,
    columnChecks: operationColumnChecks,
    data: operationData,
    loading: operationLoading,
    pagination: operationPagination,
    getData: operationGetData,
    replaceSearchParams: replaceOperationSearchParams,
    resetSearchParams: operationResetSearchParams,
    handleSizeChange: operationHandleSizeChange,
    handleCurrentChange: operationHandleCurrentChange,
    refreshData: operationRefreshData,
    refreshRemove: operationRefreshRemove
  } = useTable({
    core: {
      apiFn: fetchOperationLogList,
      apiParams: { current: 1, size: 10, logType: 'OPERATION' },
      columnsFactory: () => [
        { type: 'globalIndex', width: 64, label: t('common.index') },
        { prop: 'username', label: t('systemManage.log.fields.username'), width: 105 },
        {
          prop: 'method',
          label: t('systemManage.log.fields.method'),
          width: 90,
          formatter: (row) => h(ElTag, { type: 'info', effect: 'plain' }, () => row.method)
        },
        { prop: 'path', label: t('systemManage.log.fields.path'), minWidth: 220 },
        {
          prop: 'success',
          label: t('systemManage.log.fields.result'),
          width: 90,
          formatter: (row) => formatResult(row)
        },
        { prop: 'statusCode', label: t('systemManage.log.fields.statusCode'), width: 90 },
        {
          prop: 'durationMs',
          label: t('systemManage.log.fields.duration'),
          width: 105,
          formatter: (row) => `${row.durationMs} ms`
        },
        {
          prop: 'createdAt',
          label: t('systemManage.log.fields.createdAt'),
          width: 170,
          formatter: (row) => formatDateTime(row.createdAt)
        },
        {
          prop: 'operation',
          label: t('common.operations'),
          width: 80,
          fixed: 'right',
          formatter: (row) => formatDeleteAction(row, 'OPERATION')
        }
      ]
    }
  })

  const {
    columns: loginColumns,
    columnChecks: loginColumnChecks,
    data: loginData,
    loading: loginLoading,
    pagination: loginPagination,
    getData: loginGetData,
    replaceSearchParams: replaceLoginSearchParams,
    resetSearchParams: loginResetSearchParams,
    handleSizeChange: loginHandleSizeChange,
    handleCurrentChange: loginHandleCurrentChange,
    refreshData: loginRefreshData,
    refreshRemove: loginRefreshRemove
  } = useTable({
    core: {
      apiFn: fetchLoginLogList,
      apiParams: { current: 1, size: 10, logType: 'LOGIN' },
      columnsFactory: () => [
        { type: 'globalIndex', width: 64, label: t('common.index') },
        { prop: 'username', label: t('systemManage.log.fields.username'), width: 115 },
        {
          prop: 'success',
          label: t('systemManage.log.fields.result'),
          width: 90,
          formatter: (row) => formatResult(row)
        },
        { prop: 'statusCode', label: t('systemManage.log.fields.statusCode'), width: 90 },
        { prop: 'ipAddress', label: t('systemManage.log.fields.ip'), minWidth: 130 },
        {
          prop: 'durationMs',
          label: t('systemManage.log.fields.duration'),
          width: 105,
          formatter: (row) => `${row.durationMs} ms`
        },
        {
          prop: 'createdAt',
          label: t('systemManage.log.fields.createdAt'),
          width: 170,
          formatter: (row) => formatDateTime(row.createdAt)
        },
        {
          prop: 'operation',
          label: t('common.operations'),
          width: 80,
          fixed: 'right',
          formatter: (row) => formatDeleteAction(row, 'LOGIN')
        }
      ]
    }
  })

  const formatResult = (row: LogListItem) =>
    h(ElTag, { type: row.success ? 'success' : 'danger' }, () =>
      row.success ? t('systemManage.log.success') : t('systemManage.log.failed')
    )

  const formatDeleteAction = (row: LogListItem, logType: LogType) =>
    h(ElButton, { link: true, type: 'danger', onClick: () => deleteLog(row, logType) }, () =>
      t('common.delete')
    )

  const handleOperationSearch = (params: LogSearchParams): void => {
    replaceOperationSearchParams({ ...params, logType: 'OPERATION' })
    void operationGetData()
  }

  const handleLoginSearch = (params: LogSearchParams): void => {
    replaceLoginSearchParams({ ...params, logType: 'LOGIN' })
    void loginGetData()
  }

  const deleteLog = (row: LogListItem, logType: LogType): void => {
    ElMessageBox.confirm(t('systemManage.log.deleteConfirm'), t('common.tips'), {
      type: 'warning',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel')
    })
      .then(async () => {
        await fetchDeleteOperationLog(row.id)
        if (logType === 'OPERATION') {
          await operationRefreshRemove()
          return
        }
        await loginRefreshRemove()
      })
      .catch((error: unknown) => {
        if (!isHandledRejection(error)) throw error
      })
  }

  const clearLogs = (): void => {
    ElMessageBox.confirm(t('systemManage.log.clearConfirm'), t('systemManage.log.clear'), {
      type: 'error',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel')
    })
      .then(async () => {
        await fetchClearOperationLogs()
        await Promise.all([operationGetData(), loginGetData()])
      })
      .catch((error: unknown) => {
        if (!isHandledRejection(error)) throw error
      })
  }
</script>

<style scoped>
  .log-page {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .log-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 14px 16px;
    color: var(--el-text-color-secondary);
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: var(--el-border-radius-base);
  }

  .log-toolbar__switcher {
    display: flex;
    min-width: 0;
    align-items: center;
    gap: 16px;
  }

  .log-type-switch {
    flex: none;
  }

  .log-toolbar__hint {
    color: var(--el-text-color-secondary);
    font-size: 13px;
    line-height: 1.5;
  }

  .log-panel {
    min-width: 0;
  }

  .log-panel__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .log-panel__header p {
    margin: 6px 0 0;
    color: var(--el-text-color-secondary);
    font-size: 13px;
    line-height: 1.5;
  }

  .log-panel__title {
    display: flex;
    align-items: center;
    color: var(--el-text-color-primary);
    font-size: 16px;
    font-weight: 600;
  }

  .log-panel__marker {
    width: 4px;
    height: 16px;
    margin-right: 9px;
    border-radius: 4px;
  }

  .log-panel__marker--operation {
    background: var(--el-color-primary);
  }

  .log-panel__marker--login {
    background: var(--el-color-success);
  }

  :deep(.log-panel .art-search-bar) {
    margin-bottom: 16px;
  }

  @media (max-width: 640px) {
    .log-toolbar {
      align-items: flex-start;
      flex-direction: column;
    }

    .log-toolbar__switcher {
      width: 100%;
      align-items: flex-start;
      flex-direction: column;
      gap: 10px;
    }

    .log-type-switch {
      width: 100%;
    }
  }
</style>
