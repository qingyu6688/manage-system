<template>
  <ElDialog
    v-model="dialogVisible"
    :title="
      dialogType === 'add'
        ? $t('systemManage.announcement.addTitle')
        : $t('systemManage.announcement.editTitle')
    "
    width="680px"
    align-center
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem :label="$t('systemManage.announcement.fields.title')" prop="title">
        <ElInput
          v-model="formData.title"
          maxlength="150"
          show-word-limit
          :placeholder="$t('systemManage.announcement.placeholders.title')"
        />
      </ElFormItem>

      <ElFormItem :label="$t('systemManage.announcement.fields.type')" prop="type">
        <ElSelect
          v-model="formData.type"
          class="w-full"
          :placeholder="$t('systemManage.announcement.placeholders.type')"
        >
          <ElOption
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </ElSelect>
      </ElFormItem>

      <ElFormItem :label="$t('systemManage.announcement.fields.status')" prop="status">
        <ElRadioGroup v-model="formData.status">
          <ElRadio value="DRAFT">{{ $t('systemManage.announcement.statuses.draft') }}</ElRadio>
          <ElRadio value="PUBLISHED">{{
            $t('systemManage.announcement.statuses.published')
          }}</ElRadio>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem :label="$t('systemManage.announcement.fields.top')" prop="top">
        <ElSwitch v-model="formData.top" />
        <span class="ml-3 text-sm text-g-600">
          {{ $t('systemManage.announcement.topHint') }}
        </span>
      </ElFormItem>

      <ElFormItem :label="$t('systemManage.announcement.fields.content')" prop="content">
        <ElInput
          v-model="formData.content"
          type="textarea"
          :rows="9"
          maxlength="10000"
          show-word-limit
          resize="vertical"
          :placeholder="$t('systemManage.announcement.placeholders.content')"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
      <ElButton type="primary" :loading="saving" @click="handleSubmit">
        {{ $t('common.submit') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchCreateAnnouncement, fetchUpdateAnnouncement } from '@/api/system-manage'
  import type { DialogType } from '@/types'
  import { useI18n } from 'vue-i18n'

  interface Props {
    visible: boolean
    type: DialogType
    announcementData?: Partial<Api.SystemManage.AnnouncementListItem>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const { t } = useI18n()
  const formRef = ref<FormInstance>()
  const saving = ref(false)

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })
  const dialogType = computed(() => props.type)

  const createInitialForm = (): Api.SystemManage.AnnouncementSaveParams => ({
    title: '',
    content: '',
    type: 'SYSTEM',
    status: 'DRAFT',
    top: false
  })

  const formData = reactive<Api.SystemManage.AnnouncementSaveParams>(createInitialForm())

  const typeOptions = computed(() => [
    { label: t('systemManage.announcement.types.system'), value: 'SYSTEM' },
    { label: t('systemManage.announcement.types.activity'), value: 'ACTIVITY' },
    { label: t('systemManage.announcement.types.reminder'), value: 'REMINDER' }
  ])

  const rules = computed<FormRules>(() => ({
    title: [
      {
        required: true,
        message: t('systemManage.announcement.validation.title'),
        trigger: 'blur'
      }
    ],
    content: [
      {
        required: true,
        message: t('systemManage.announcement.validation.content'),
        trigger: 'blur'
      }
    ],
    type: [
      {
        required: true,
        message: t('systemManage.announcement.placeholders.type'),
        trigger: 'change'
      }
    ],
    status: [
      {
        required: true,
        message: t('systemManage.announcement.placeholders.status'),
        trigger: 'change'
      }
    ]
  }))

  const initFormData = (): void => {
    Object.assign(formData, createInitialForm(), {
      title: props.announcementData?.title || '',
      content: props.announcementData?.content || '',
      type: props.announcementData?.type || 'SYSTEM',
      status: props.announcementData?.status || 'DRAFT',
      top: props.announcementData?.top || false
    })
  }

  watch(
    () => props.visible,
    (visible) => {
      if (!visible) return
      initFormData()
      nextTick(() => formRef.value?.clearValidate())
    }
  )

  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return
    await formRef.value.validate()
    saving.value = true
    try {
      if (dialogType.value === 'add') {
        await fetchCreateAnnouncement(formData)
      } else if (props.announcementData?.id) {
        await fetchUpdateAnnouncement(props.announcementData.id, formData)
      }
      dialogVisible.value = false
      emit('success')
    } finally {
      saving.value = false
    }
  }
</script>
