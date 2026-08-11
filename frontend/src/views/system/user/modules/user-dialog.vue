<template>
  <ElDialog
    v-model="dialogVisible"
    :title="
      dialogType === 'add' ? $t('systemManage.user.addTitle') : $t('systemManage.user.editTitle')
    "
    width="520px"
    align-center
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="90px">
      <ElFormItem :label="$t('systemManage.user.fields.username')" prop="userName">
        <ElInput
          v-model="formData.userName"
          :placeholder="$t('systemManage.user.placeholders.loginUsername')"
        />
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.user.fields.nickname')" prop="nickName">
        <ElInput
          v-model="formData.nickName"
          :placeholder="$t('systemManage.user.placeholders.nickname')"
        />
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.user.fields.password')" prop="password">
        <ElInput
          v-model="formData.password"
          type="password"
          show-password
          autocomplete="new-password"
          :placeholder="
            dialogType === 'add'
              ? $t('systemManage.user.placeholders.passwordAdd')
              : $t('systemManage.user.placeholders.passwordEdit')
          "
        />
      </ElFormItem>
      <ElFormItem :label="$t('common.phone')" prop="userPhone">
        <ElInput
          v-model="formData.userPhone"
          maxlength="11"
          :placeholder="$t('systemManage.user.placeholders.phone')"
        />
      </ElFormItem>
      <ElFormItem :label="$t('common.email')" prop="userEmail">
        <ElInput
          v-model="formData.userEmail"
          :placeholder="$t('systemManage.user.placeholders.email')"
        />
      </ElFormItem>
      <ElFormItem :label="$t('common.gender')" prop="gender">
        <ElRadioGroup v-model="formData.gender">
          <ElRadio :value="1">{{ $t('common.male') }}</ElRadio>
          <ElRadio :value="2">{{ $t('common.female') }}</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem :label="$t('common.status')" prop="status">
        <ElRadioGroup v-model="formData.status">
          <ElRadio :value="1">{{ $t('common.enabled') }}</ElRadio>
          <ElRadio :value="2">{{ $t('common.stopped') }}</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.user.fields.roles')" prop="roleIds">
        <ElSelect
          v-model="formData.roleIds"
          multiple
          class="w-full"
          :placeholder="$t('systemManage.user.placeholders.roles')"
        >
          <ElOption
            v-for="role in roleList"
            :key="role.roleId"
            :value="role.roleId"
            :label="formatRoleName(role.roleCode, role.roleName)"
          />
        </ElSelect>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
      <ElButton type="primary" :loading="saving" @click="handleSubmit">{{
        $t('common.submit')
      }}</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchCreateUser, fetchGetRoleOptions, fetchUpdateUser } from '@/api/system-manage'
  import type { DialogType } from '@/types'
  import { useI18n } from 'vue-i18n'
  import { formatRoleName } from '@/utils/system-manage'

  interface Props {
    visible: boolean
    type: DialogType
    userData?: Partial<Api.SystemManage.UserListItem>
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
  const roleList = ref<Api.SystemManage.RoleOption[]>([])

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })
  const dialogType = computed(() => props.type)

  const createInitialForm = (): Api.SystemManage.UserSaveParams => ({
    userName: '',
    nickName: '',
    gender: 1,
    userPhone: '',
    userEmail: '',
    avatar: '',
    password: '',
    status: 1,
    roleIds: []
  })

  const formData = reactive<Api.SystemManage.UserSaveParams>(createInitialForm())

  const rules = computed<FormRules>(() => ({
    userName: [
      { required: true, message: t('systemManage.user.placeholders.username'), trigger: 'blur' },
      {
        min: 2,
        max: 30,
        message: t('systemManage.user.validation.usernameLength'),
        trigger: 'blur'
      }
    ],
    nickName: [
      { required: true, message: t('systemManage.user.placeholders.nickname'), trigger: 'blur' }
    ],
    password: [
      {
        validator: (_rule, value: string, callback) => {
          if (dialogType.value === 'add' && !value) {
            callback(new Error(t('systemManage.user.validation.initialPassword')))
            return
          }
          if (value && value.length < 8) {
            callback(new Error(t('systemManage.user.validation.passwordLength')))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ],
    userPhone: [
      {
        pattern: /^1[3-9]\d{9}$/,
        message: t('systemManage.user.validation.phone'),
        trigger: 'blur'
      }
    ],
    userEmail: [
      { type: 'email', message: t('systemManage.user.validation.email'), trigger: 'blur' }
    ],
    roleIds: [
      {
        required: true,
        type: 'array',
        min: 1,
        message: t('systemManage.user.placeholders.roles'),
        trigger: 'change'
      }
    ]
  }))

  const initFormData = () => {
    const row = props.userData
    Object.assign(formData, createInitialForm(), {
      userName: row?.userName || '',
      nickName: row?.nickName || '',
      gender: row?.userGender === '女' ? 2 : 1,
      userPhone: row?.userPhone || '',
      userEmail: row?.userEmail || '',
      avatar: row?.avatar || '',
      status: Number(row?.status || 1),
      roleIds: row?.roleIds || []
    })
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      initFormData()
      roleList.value = await fetchGetRoleOptions()
      nextTick(() => formRef.value?.clearValidate())
    }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate()
    saving.value = true
    try {
      if (dialogType.value === 'add') {
        await fetchCreateUser(formData)
      } else if (props.userData?.id) {
        await fetchUpdateUser(props.userData.id, formData)
      }
      dialogVisible.value = false
      emit('success')
    } finally {
      saving.value = false
    }
  }
</script>
