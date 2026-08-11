<!-- 个人中心页面 -->
<template>
  <div v-loading="loading" class="user-center-page w-full min-h-full">
    <ElAlert
      v-if="loadError"
      :title="$t('userCenter.messages.loadFailed')"
      type="error"
      show-icon
      class="mb-4"
    >
      <template #default>
        <ElButton link type="primary" @click="loadProfile">{{ $t('common.retry') }}</ElButton>
      </template>
    </ElAlert>

    <div class="relative flex items-start gap-5 max-lg:block">
      <aside class="w-90 shrink-0 max-lg:w-full max-lg:mb-4">
        <div class="art-card-sm relative p-8 pb-7 overflow-hidden text-center">
          <img class="absolute top-0 left-0 w-full h-42 object-cover" src="@imgs/user/bg.webp" />
          <ElUpload
            class="profile-avatar-uploader"
            accept="image/jpeg,image/png,image/webp"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="uploadAvatar"
            :disabled="uploadingAvatar"
          >
            <button
              type="button"
              class="profile-avatar-trigger"
              :class="{ 'is-uploading': uploadingAvatar }"
              :disabled="uploadingAvatar"
              :aria-label="$t('userCenter.actions.uploadAvatar')"
            >
              <span class="profile-avatar-image">
                <img :src="avatarPreview" :alt="displayName" @error="avatarBroken = true" />
                <span class="profile-avatar-mask">
                  <ArtSvgIcon
                    :icon="uploadingAvatar ? 'ri:loader-4-line' : 'ri:camera-line'"
                    :class="{ 'is-spinning': uploadingAvatar }"
                  />
                  <span>{{ $t('userCenter.actions.uploadAvatar') }}</span>
                </span>
              </span>
              <span class="profile-avatar-badge" aria-hidden="true">
                <ArtSvgIcon
                  :icon="uploadingAvatar ? 'ri:loader-4-line' : 'ri:camera-line'"
                  :class="{ 'is-spinning': uploadingAvatar }"
                />
              </span>
            </button>
          </ElUpload>
          <h2 class="mt-4 text-xl font-medium">{{ displayName }}</h2>
          <p class="mt-2 min-h-10 text-sm leading-5 text-g-600">
            {{ profile?.bio || $t('userCenter.profile.signature') }}
          </p>

          <div class="mt-7 pt-5 text-left border-t border-g-300/80">
            <h3 class="mb-3 text-sm font-medium">{{ $t('userCenter.profile.accountInfo') }}</h3>
            <div class="profile-detail">
              <ArtSvgIcon icon="ri:user-line" />
              <span>{{ profile?.username || '-' }}</span>
            </div>
            <div class="profile-detail">
              <ArtSvgIcon icon="ri:mail-line" />
              <span>{{ profile?.email || $t('userCenter.profile.notSet') }}</span>
            </div>
            <div class="profile-detail">
              <ArtSvgIcon icon="ri:phone-line" />
              <span>{{ profile?.phone || $t('userCenter.profile.notSet') }}</span>
            </div>
            <div class="profile-detail">
              <ArtSvgIcon icon="ri:calendar-line" />
              <span>{{ $t('userCenter.profile.joinedAt') }}：{{ joinedAt }}</span>
            </div>
          </div>

          <div class="mt-6 pt-5 text-left border-t border-g-300/80">
            <h3 class="mb-3 text-sm font-medium">{{ $t('userCenter.profile.rolesTitle') }}</h3>
            <ElSpace wrap>
              <ElTag v-for="role in profile?.roles || []" :key="role" type="primary">
                {{ formatRoleName(role, role) }}
              </ElTag>
              <span v-if="!profile?.roles?.length" class="text-sm text-g-500">
                {{ $t('userCenter.profile.notSet') }}
              </span>
            </ElSpace>
          </div>
        </div>
      </aside>

      <main class="min-w-0 flex-1 max-lg:w-full">
        <section class="art-card-sm">
          <header class="flex-cb p-4 border-b border-g-300">
            <h1 class="text-xl font-normal">{{ $t('userCenter.sections.basicSettings') }}</h1>
            <ElTag v-if="!isEditing" type="success">{{ $t('common.saved') }}</ElTag>
          </header>

          <ElForm
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            class="box-border p-5"
            label-position="top"
          >
            <ElRow :gutter="20">
              <ElCol :xs="24" :sm="12">
                <ElFormItem :label="$t('userCenter.fields.username')">
                  <ElInput v-model="profileForm.username" disabled />
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :sm="12">
                <ElFormItem :label="$t('userCenter.fields.realName')" prop="realName">
                  <ElInput v-model="profileForm.realName" :disabled="!isEditing" maxlength="50" />
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :sm="12">
                <ElFormItem :label="$t('userCenter.fields.nickname')" prop="nickname">
                  <ElInput v-model="profileForm.nickname" :disabled="!isEditing" maxlength="50" />
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :sm="12">
                <ElFormItem :label="$t('userCenter.fields.gender')" prop="gender">
                  <ElSelect
                    v-model="profileForm.gender"
                    class="w-full"
                    :placeholder="$t('userCenter.placeholders.selectGender')"
                    :disabled="!isEditing"
                  >
                    <ElOption
                      v-for="option in genderOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :sm="12">
                <ElFormItem :label="$t('userCenter.fields.email')" prop="email">
                  <ElInput
                    v-model="profileForm.email"
                    :placeholder="$t('userCenter.placeholders.email')"
                    :disabled="!isEditing"
                    maxlength="100"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :sm="12">
                <ElFormItem :label="$t('userCenter.fields.phone')" prop="phone">
                  <ElInput
                    v-model="profileForm.phone"
                    :placeholder="$t('userCenter.placeholders.phone')"
                    :disabled="!isEditing"
                    maxlength="11"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="24">
                <ElFormItem :label="$t('userCenter.fields.bio')" prop="bio">
                  <ElInput
                    v-model="profileForm.bio"
                    type="textarea"
                    :rows="4"
                    :placeholder="$t('userCenter.placeholders.bio')"
                    :disabled="!isEditing"
                    maxlength="500"
                    show-word-limit
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>

            <div class="flex justify-end gap-3">
              <template v-if="isEditing">
                <ElButton @click="cancelEditing">{{ $t('common.cancel') }}</ElButton>
                <ElButton type="primary" :loading="saving" @click="saveProfile">
                  {{ $t('common.save') }}
                </ElButton>
              </template>
              <ElButton v-else type="primary" :disabled="!profile" @click="isEditing = true">
                {{ $t('common.edit') }}
              </ElButton>
            </div>
          </ElForm>
        </section>

        <section class="art-card-sm my-5">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">
            {{ $t('userCenter.sections.changePassword') }}
          </h1>

          <ElForm
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            class="box-border p-5"
            label-position="top"
          >
            <ElFormItem :label="$t('userCenter.fields.currentPassword')" prop="currentPassword">
              <ElInput
                v-model="passwordForm.currentPassword"
                type="password"
                show-password
                autocomplete="current-password"
                :placeholder="$t('userCenter.placeholders.currentPassword')"
              />
            </ElFormItem>
            <ElFormItem :label="$t('userCenter.fields.newPassword')" prop="newPassword">
              <ElInput
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                autocomplete="new-password"
                :placeholder="$t('userCenter.placeholders.newPassword')"
              />
            </ElFormItem>
            <ElFormItem :label="$t('userCenter.fields.confirmPassword')" prop="confirmPassword">
              <ElInput
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                autocomplete="new-password"
                :placeholder="$t('userCenter.placeholders.confirmPassword')"
              />
            </ElFormItem>

            <div class="flex justify-end gap-3">
              <ElButton @click="resetPasswordForm">{{ $t('common.reset') }}</ElButton>
              <ElButton type="primary" :loading="changingPassword" @click="changePassword">
                {{ $t('userCenter.actions.changePassword') }}
              </ElButton>
            </div>
          </ElForm>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules, UploadProps, UploadRequestOptions } from 'element-plus'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import defaultAvatar from '@/assets/images/user/avatar.webp'
  import { useUserStore } from '@/store/modules/user'
  import {
    fetchChangePassword,
    fetchGetProfile,
    fetchUpdateProfile,
    fetchUploadAvatar
  } from '@/api/profile'
  import { formatRoleName } from '@/utils/system-manage'

  defineOptions({ name: 'UserCenter' })

  const { t, locale } = useI18n()
  const userStore = useUserStore()
  const profileFormRef = ref<FormInstance>()
  const passwordFormRef = ref<FormInstance>()
  const profile = ref<Api.SystemManage.UserProfile | null>(null)
  const loading = ref(false)
  const loadError = ref(false)
  const saving = ref(false)
  const uploadingAvatar = ref(false)
  const changingPassword = ref(false)
  const isEditing = ref(false)
  const avatarBroken = ref(false)

  const createProfileForm = () => ({
    username: '',
    realName: '',
    nickname: '',
    gender: 1,
    phone: '',
    email: '',
    bio: ''
  })

  const profileForm = reactive(createProfileForm())
  const passwordForm = reactive<Api.SystemManage.PasswordChangeParams>({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })

  const displayName = computed(
    () => profile.value?.realName || profile.value?.nickname || profile.value?.username || '-'
  )
  const avatarPreview = computed(() =>
    profile.value?.avatar && !avatarBroken.value ? profile.value.avatar : defaultAvatar
  )
  const joinedAt = computed(() => {
    if (!profile.value?.createdAt) return '-'
    const date = new Date(profile.value.createdAt.replace(' ', 'T'))
    if (Number.isNaN(date.getTime())) return profile.value.createdAt
    return date.toLocaleDateString(locale.value === 'en' ? 'en-US' : 'zh-CN')
  })

  const genderOptions = computed(() => [
    { value: 1, label: t('common.male') },
    { value: 2, label: t('common.female') }
  ])

  const profileRules = computed<FormRules>(() => ({
    realName: [
      { required: true, message: t('userCenter.validation.realNameRequired'), trigger: 'blur' },
      { min: 2, max: 50, message: t('userCenter.validation.realNameLength'), trigger: 'blur' }
    ],
    nickname: [
      { required: true, message: t('userCenter.validation.nicknameRequired'), trigger: 'blur' },
      { min: 2, max: 50, message: t('userCenter.validation.nicknameLength'), trigger: 'blur' }
    ],
    gender: [
      { required: true, message: t('userCenter.validation.genderRequired'), trigger: 'change' }
    ],
    email: [
      { required: true, message: t('userCenter.validation.emailRequired'), trigger: 'blur' },
      { type: 'email', message: t('userCenter.validation.emailInvalid'), trigger: 'blur' }
    ],
    phone: [
      {
        pattern: /^$|^1[3-9]\d{9}$/,
        message: t('userCenter.validation.phoneInvalid'),
        trigger: 'blur'
      }
    ],
    bio: [{ max: 500, message: t('userCenter.validation.bioLength'), trigger: 'blur' }]
  }))

  const passwordRules = computed<FormRules>(() => ({
    currentPassword: [
      {
        required: true,
        message: t('userCenter.validation.currentPasswordRequired'),
        trigger: 'blur'
      }
    ],
    newPassword: [
      {
        required: true,
        message: t('userCenter.validation.newPasswordRequired'),
        trigger: 'blur'
      },
      {
        min: 8,
        max: 64,
        message: t('userCenter.validation.newPasswordLength'),
        trigger: 'blur'
      },
      {
        pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/,
        message: t('userCenter.validation.newPasswordComplexity'),
        trigger: 'blur'
      }
    ],
    confirmPassword: [
      {
        required: true,
        message: t('userCenter.validation.confirmPasswordRequired'),
        trigger: 'blur'
      },
      {
        validator: (_rule, value: string, callback: (error?: Error) => void) => {
          if (value !== passwordForm.newPassword) {
            callback(new Error(t('userCenter.validation.passwordMismatch')))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  }))

  const applyProfile = (data: Api.SystemManage.UserProfile): void => {
    profile.value = data
    Object.assign(profileForm, {
      username: data.username,
      realName: data.realName || '',
      nickname: data.nickname || '',
      gender: data.gender || 1,
      phone: data.phone || '',
      email: data.email || '',
      bio: data.bio || ''
    })
    avatarBroken.value = false
    userStore.updateUserInfo({ email: data.email, avatar: data.avatar || undefined })
  }

  const loadProfile = async (): Promise<void> => {
    loading.value = true
    loadError.value = false
    try {
      applyProfile(await fetchGetProfile())
    } catch {
      loadError.value = true
    } finally {
      loading.value = false
    }
  }

  const saveProfile = async (): Promise<void> => {
    if (!profileFormRef.value) return
    const valid = await profileFormRef.value.validate().catch(() => false)
    if (!valid) return

    saving.value = true
    try {
      const payload: Api.SystemManage.ProfileUpdateParams = {
        realName: profileForm.realName,
        nickname: profileForm.nickname,
        gender: profileForm.gender,
        phone: profileForm.phone,
        email: profileForm.email,
        bio: profileForm.bio
      }
      applyProfile(await fetchUpdateProfile(payload))
      isEditing.value = false
      ElMessage.success(t('userCenter.messages.saveSuccess'))
    } catch {
      return
    } finally {
      saving.value = false
    }
  }

  const cancelEditing = (): void => {
    if (profile.value) applyProfile(profile.value)
    isEditing.value = false
    nextTick(() => profileFormRef.value?.clearValidate())
  }

  const resetPasswordForm = (): void => {
    Object.assign(passwordForm, {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    nextTick(() => passwordFormRef.value?.clearValidate())
  }

  const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
    if (!allowedTypes.includes(file.type)) {
      ElMessage.error(t('userCenter.validation.avatarType'))
      return false
    }
    if (file.size > 2 * 1024 * 1024) {
      ElMessage.error(t('userCenter.validation.avatarSize'))
      return false
    }
    return true
  }

  const uploadAvatar = async (
    options: UploadRequestOptions
  ): Promise<Api.SystemManage.UserProfile> => {
    uploadingAvatar.value = true
    try {
      const data = await fetchUploadAvatar(options.file)
      applyProfile(data)
      ElMessage.success(t('userCenter.messages.avatarSuccess'))
      return data
    } finally {
      uploadingAvatar.value = false
    }
  }

  const changePassword = async (): Promise<void> => {
    if (!passwordFormRef.value) return
    const valid = await passwordFormRef.value.validate().catch(() => false)
    if (!valid) return

    changingPassword.value = true
    try {
      await fetchChangePassword({ ...passwordForm })
      resetPasswordForm()
      await ElMessageBox.alert(
        t('userCenter.messages.reLogin'),
        t('userCenter.messages.passwordSuccess'),
        {
          confirmButtonText: t('userCenter.actions.loginAgain'),
          type: 'success',
          showClose: false,
          closeOnClickModal: false,
          closeOnPressEscape: false
        }
      )
      userStore.logOut()
    } catch {
      return
    } finally {
      changingPassword.value = false
    }
  }

  watch(
    () => profile.value?.avatar,
    () => {
      avatarBroken.value = false
    }
  )

  onMounted(loadProfile)
</script>

<style scoped>
  .profile-detail {
    display: flex;
    gap: 10px;
    align-items: flex-start;
    margin-top: 12px;
    font-size: 13px;
    line-height: 20px;
    color: var(--art-gray-700);
    word-break: break-word;

    .art-svg-icon {
      flex: none;
      margin-top: 2px;
      font-size: 16px;
    }
  }

  .profile-avatar-uploader {
    position: relative;
    z-index: 10;
    display: flex;
    width: 88px;
    height: 88px;
    margin: 88px auto 0;
  }

  :deep(.profile-avatar-uploader .el-upload) {
    width: 88px;
    height: 88px;
    border-radius: 50%;
  }

  .profile-avatar-trigger {
    position: relative;
    width: 88px;
    height: 88px;
    padding: 0;
    background: transparent;
    border: 0;
    border-radius: 50%;
    cursor: pointer;

    &:focus-visible {
      outline: 3px solid var(--el-color-primary-light-5);
      outline-offset: 4px;
    }

    &:hover .profile-avatar-mask,
    &:focus-visible .profile-avatar-mask,
    &.is-uploading .profile-avatar-mask {
      opacity: 1;
    }

    &:disabled {
      cursor: wait;
    }
  }

  .profile-avatar-image {
    position: relative;
    display: block;
    width: 88px;
    height: 88px;
    overflow: hidden;
    background: white;
    border: 3px solid white;
    border-radius: 50%;
    box-shadow: 0 6px 18px rgb(31 35 41 / 16%);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .profile-avatar-mask {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: white;
    background: rgb(0 0 0 / 55%);
    opacity: 0;
    transition: opacity 0.2s;

    .art-svg-icon {
      font-size: 20px;
    }
  }

  .profile-avatar-badge {
    position: absolute;
    right: -3px;
    bottom: 1px;
    display: flex;
    width: 28px;
    height: 28px;
    align-items: center;
    justify-content: center;
    color: white;
    background: var(--el-color-primary);
    border: 3px solid white;
    border-radius: 50%;
    box-shadow: 0 3px 8px rgb(31 35 41 / 20%);

    .art-svg-icon {
      font-size: 14px;
    }
  }

  .is-spinning {
    animation: avatar-spin 0.9s linear infinite;
  }

  @keyframes avatar-spin {
    to {
      transform: rotate(360deg);
    }
  }
</style>
