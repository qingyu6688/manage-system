<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    @reset="handleReset"
    @search="handleSearch"
  />
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'

  interface Props {
    modelValue: Api.SystemManage.AnnouncementSearchParams
  }

  interface Emits {
    (e: 'update:modelValue', value: Api.SystemManage.AnnouncementSearchParams): void
    (e: 'search', params: Api.SystemManage.AnnouncementSearchParams): void
    (e: 'reset'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const { t } = useI18n()
  const searchBarRef = ref()

  const formData = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const typeOptions = computed(() => [
    { label: t('systemManage.announcement.types.system'), value: 'SYSTEM' },
    { label: t('systemManage.announcement.types.activity'), value: 'ACTIVITY' },
    { label: t('systemManage.announcement.types.reminder'), value: 'REMINDER' }
  ])

  const statusOptions = computed(() => [
    { label: t('systemManage.announcement.statuses.draft'), value: 'DRAFT' },
    { label: t('systemManage.announcement.statuses.published'), value: 'PUBLISHED' }
  ])

  const formItems = computed(() => [
    {
      label: t('systemManage.announcement.fields.title'),
      key: 'title',
      type: 'input',
      props: {
        placeholder: t('systemManage.announcement.placeholders.title'),
        clearable: true
      }
    },
    {
      label: t('systemManage.announcement.fields.type'),
      key: 'type',
      type: 'select',
      props: {
        placeholder: t('systemManage.announcement.placeholders.type'),
        clearable: true,
        options: typeOptions.value
      }
    },
    {
      label: t('systemManage.announcement.fields.status'),
      key: 'status',
      type: 'select',
      props: {
        placeholder: t('systemManage.announcement.placeholders.status'),
        clearable: true,
        options: statusOptions.value
      }
    }
  ])

  const handleReset = (): void => {
    emit('reset')
  }

  const handleSearch = async (params: Api.SystemManage.AnnouncementSearchParams) => {
    await searchBarRef.value?.validate()
    emit('search', params)
  }
</script>
