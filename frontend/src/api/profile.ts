import request from '@/utils/http'

export function fetchGetProfile() {
  return request.get<Api.SystemManage.UserProfile>({
    url: '/api/profile'
  })
}

export function fetchUpdateProfile(data: Api.SystemManage.ProfileUpdateParams) {
  return request.put<Api.SystemManage.UserProfile>({
    url: '/api/profile',
    data
  })
}

export function fetchUploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<Api.SystemManage.UserProfile>({
    url: '/api/profile/avatar',
    data: formData
  })
}

export function fetchChangePassword(data: Api.SystemManage.PasswordChangeParams) {
  return request.put<void>({
    url: '/api/profile/password',
    data
  })
}
