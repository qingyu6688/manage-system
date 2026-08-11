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

  const props = defineProps<{ modelValue: Api.SystemManage.ManagedFileSearchParams }>()
  const emit = defineEmits<{
    (event: 'update:modelValue', value: Api.SystemManage.ManagedFileSearchParams): void
    (event: 'search', value: Api.SystemManage.ManagedFileSearchParams): void
    (event: 'reset'): void
  }>()
  const { t } = useI18n()
  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })
  const formItems = computed(() => [
    {
      label: t('systemManage.file.fields.name'),
      key: 'originalName',
      type: 'input',
      props: { placeholder: t('systemManage.file.placeholders.name'), clearable: true }
    },
    {
      label: t('systemManage.file.fields.type'),
      key: 'contentType',
      type: 'input',
      props: { placeholder: t('systemManage.file.placeholders.type'), clearable: true }
    }
  ])
  const handleSearch = async (params: Api.SystemManage.ManagedFileSearchParams) => {
    await searchBarRef.value?.validate()
    emit('search', params)
  }
</script>
