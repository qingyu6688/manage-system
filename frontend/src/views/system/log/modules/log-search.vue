<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    @reset="emit('reset')"
    @search="handleSearch"
  />
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'

  const props = defineProps<{
    modelValue: Api.SystemManage.OperationLogSearchParams
    mode: 'operation' | 'login'
  }>()
  const emit = defineEmits<{
    (event: 'update:modelValue', value: Api.SystemManage.OperationLogSearchParams): void
    (event: 'search', value: Api.SystemManage.OperationLogSearchParams): void
    (event: 'reset'): void
  }>()
  const { t } = useI18n()
  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const formItems = computed(() => {
    const usernameItem = {
      label: t('systemManage.log.fields.username'),
      key: 'username',
      type: 'input',
      props: { placeholder: t('systemManage.log.placeholders.username'), clearable: true }
    }
    const resultItem = {
      label: t('systemManage.log.fields.result'),
      key: 'success',
      type: 'select',
      props: {
        placeholder: t('systemManage.log.placeholders.result'),
        clearable: true,
        options: [
          { label: t('systemManage.log.success'), value: true },
          { label: t('systemManage.log.failed'), value: false }
        ]
      }
    }

    if (props.mode === 'login') {
      return [usernameItem, resultItem]
    }

    return [
      usernameItem,
      {
        label: t('systemManage.log.fields.path'),
        key: 'path',
        type: 'input',
        props: { placeholder: t('systemManage.log.placeholders.path'), clearable: true }
      },
      {
        label: t('systemManage.log.fields.method'),
        key: 'method',
        type: 'select',
        props: {
          placeholder: t('systemManage.log.placeholders.method'),
          clearable: true,
          options: ['POST', 'PUT', 'PATCH', 'DELETE'].map((value) => ({ label: value, value }))
        }
      },
      resultItem
    ]
  })

  const handleSearch = async (params: Api.SystemManage.OperationLogSearchParams) => {
    await searchBarRef.value?.validate()
    emit('search', params)
  }
</script>
