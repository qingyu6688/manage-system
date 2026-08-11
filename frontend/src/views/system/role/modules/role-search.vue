<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    @reset="handleReset"
    @search="handleSearch"
  >
  </ArtSearchBar>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'

  type RoleSearchFormParams = Api.SystemManage.RoleSearchParams & {
    daterange?: string[]
  }

  interface Props {
    modelValue: RoleSearchFormParams
  }

  interface Emits {
    (e: 'update:modelValue', value: RoleSearchFormParams): void
    (e: 'search', params: RoleSearchFormParams): void
    (e: 'reset'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const { t } = useI18n()

  const searchBarRef = ref()

  /**
   * 表单数据双向绑定
   */
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  /**
   * 表单校验规则
   */
  const rules = {}

  /**
   * 角色状态选项
   */
  const statusOptions = computed(() => [
    { label: t('common.enabled'), value: true },
    { label: t('common.disabled'), value: false }
  ])

  /**
   * 搜索表单配置项
   */
  const formItems = computed(() => [
    {
      label: t('systemManage.role.fields.name'),
      key: 'roleName',
      type: 'input',
      placeholder: t('systemManage.role.placeholders.name'),
      clearable: true
    },
    {
      label: t('systemManage.role.fields.code'),
      key: 'roleCode',
      type: 'input',
      placeholder: t('systemManage.role.placeholders.code'),
      clearable: true
    },
    {
      label: t('systemManage.role.fields.description'),
      key: 'description',
      type: 'input',
      placeholder: t('systemManage.role.placeholders.description'),
      clearable: true
    },
    {
      label: t('systemManage.role.fields.status'),
      key: 'enabled',
      type: 'select',
      props: {
        placeholder: t('systemManage.role.placeholders.status'),
        options: statusOptions.value,
        clearable: true
      }
    },
    {
      label: t('systemManage.role.fields.createdAt'),
      key: 'daterange',
      type: 'datetime',
      props: {
        style: { width: '100%' },
        placeholder: t('systemManage.role.placeholders.dateRange'),
        type: 'daterange',
        rangeSeparator: t('systemManage.role.date.separator'),
        startPlaceholder: t('systemManage.role.placeholders.startDate'),
        endPlaceholder: t('systemManage.role.placeholders.endDate'),
        valueFormat: 'YYYY-MM-DD',
        shortcuts: [
          { text: t('systemManage.role.date.today'), value: [new Date(), new Date()] },
          {
            text: t('systemManage.role.date.lastWeek'),
            value: [new Date(Date.now() - 604800000), new Date()]
          },
          {
            text: t('systemManage.role.date.lastMonth'),
            value: [new Date(Date.now() - 2592000000), new Date()]
          }
        ]
      }
    }
  ])

  /**
   * 处理重置事件
   */
  const handleReset = () => {
    emit('reset')
  }

  /**
   * 处理搜索事件
   * 验证表单后触发搜索
   */
  const handleSearch = async (params: RoleSearchFormParams) => {
    await searchBarRef.value.validate()
    emit('search', params)
  }
</script>
