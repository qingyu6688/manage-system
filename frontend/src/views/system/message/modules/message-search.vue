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

  const props = defineProps<{ modelValue: Api.SystemManage.MessageSearchParams }>()
  const emit = defineEmits<{
    (event: 'update:modelValue', value: Api.SystemManage.MessageSearchParams): void
    (event: 'search', value: Api.SystemManage.MessageSearchParams): void
    (event: 'reset'): void
  }>()
  const { t } = useI18n()
  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })
  const typeOptions = computed(() => [
    { label: t('systemManage.message.types.system'), value: 'SYSTEM' },
    { label: t('systemManage.message.types.reminder'), value: 'REMINDER' },
    { label: t('systemManage.message.types.task'), value: 'TASK' }
  ])
  const readOptions = computed(() => [
    { label: t('systemManage.message.read'), value: 'READ' },
    { label: t('systemManage.message.unread'), value: 'UNREAD' }
  ])
  const formItems = computed(() => [
    {
      label: t('systemManage.message.fields.keyword'),
      key: 'keyword',
      type: 'input',
      props: { placeholder: t('systemManage.message.placeholders.keyword'), clearable: true }
    },
    {
      label: t('systemManage.message.fields.type'),
      key: 'type',
      type: 'select',
      props: {
        placeholder: t('systemManage.message.placeholders.type'),
        clearable: true,
        options: typeOptions.value
      }
    },
    {
      label: t('systemManage.message.fields.readStatus'),
      key: 'readStatus',
      type: 'select',
      props: {
        placeholder: t('systemManage.message.placeholders.readStatus'),
        clearable: true,
        options: readOptions.value
      }
    }
  ])
  const handleSearch = async (params: Api.SystemManage.MessageSearchParams) => {
    await searchBarRef.value?.validate()
    emit('search', params)
  }
</script>
