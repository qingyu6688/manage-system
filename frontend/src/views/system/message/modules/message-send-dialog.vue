<template>
  <ElDialog
    v-model="visible"
    :title="$t('systemManage.message.sendTitle')"
    width="620px"
    align-center
    destroy-on-close
    @open="handleOpen"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="90px">
      <ElFormItem :label="$t('systemManage.message.fields.title')" prop="title">
        <ElInput
          v-model="form.title"
          maxlength="150"
          show-word-limit
          :placeholder="$t('systemManage.message.placeholders.title')"
        />
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.message.fields.type')" prop="type">
        <ElSelect v-model="form.type" class="w-full">
          <ElOption :label="$t('systemManage.message.types.system')" value="SYSTEM" />
          <ElOption :label="$t('systemManage.message.types.reminder')" value="REMINDER" />
          <ElOption :label="$t('systemManage.message.types.task')" value="TASK" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.message.fields.scope')" prop="allUsers">
        <ElSwitch
          v-model="form.allUsers"
          :active-text="$t('systemManage.message.allUsers')"
          :inactive-text="$t('systemManage.message.selectedUsers')"
        />
      </ElFormItem>
      <ElFormItem
        v-if="!form.allUsers"
        :label="$t('systemManage.message.fields.receivers')"
        prop="receiverIds"
      >
        <ElSelect
          v-model="form.receiverIds"
          multiple
          filterable
          collapse-tags
          collapse-tags-tooltip
          class="w-full"
          :loading="userLoading"
          :placeholder="$t('systemManage.message.placeholders.receivers')"
        >
          <ElOption
            v-for="user in userOptions"
            :key="user.id"
            :label="`${user.nickName} (${user.userName})`"
            :value="user.id"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.message.fields.content')" prop="content">
        <ElInput
          v-model="form.content"
          type="textarea"
          :rows="7"
          maxlength="5000"
          show-word-limit
          resize="none"
          :placeholder="$t('systemManage.message.placeholders.content')"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="visible = false">{{ $t('common.cancel') }}</ElButton>
      <ElButton type="primary" :loading="submitting" @click="submit">
        {{ $t('systemManage.message.send') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchSendMessage, fetchMessageReceiverOptions } from '@/api/operations'

  const props = defineProps<{ modelValue: boolean }>()
  const emit = defineEmits<{
    (event: 'update:modelValue', value: boolean): void
    (event: 'success'): void
  }>()
  const { t } = useI18n()
  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })
  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const userLoading = ref(false)
  const userOptions = ref<Pick<Api.SystemManage.UserListItem, 'id' | 'userName' | 'nickName'>[]>([])
  const createInitialForm = (): Api.SystemManage.MessageSendParams => ({
    title: '',
    content: '',
    type: 'SYSTEM',
    allUsers: true,
    receiverIds: []
  })
  const form = reactive<Api.SystemManage.MessageSendParams>(createInitialForm())

  const rules = computed<FormRules<Api.SystemManage.MessageSendParams>>(() => ({
    title: [
      { required: true, message: t('systemManage.message.validation.title'), trigger: 'blur' }
    ],
    type: [
      { required: true, message: t('systemManage.message.validation.type'), trigger: 'change' }
    ],
    receiverIds: [
      {
        validator: (_rule, value: number[], callback) => {
          if (!form.allUsers && (!value || value.length === 0)) {
            callback(new Error(t('systemManage.message.validation.receivers')))
            return
          }
          callback()
        },
        trigger: 'change'
      }
    ],
    content: [
      { required: true, message: t('systemManage.message.validation.content'), trigger: 'blur' }
    ]
  }))

  const loadUsers = async () => {
    if (userOptions.value.length > 0) return
    userLoading.value = true
    try {
      userOptions.value = await fetchMessageReceiverOptions()
    } finally {
      userLoading.value = false
    }
  }

  const handleOpen = () => {
    Object.assign(form, createInitialForm())
    nextTick(() => formRef.value?.clearValidate())
    loadUsers()
  }

  const submit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      await fetchSendMessage({ ...form, receiverIds: [...form.receiverIds] })
      visible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
