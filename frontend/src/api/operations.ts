import request from '@/utils/http'

export function fetchMessageReceiverOptions(keyword?: string) {
  return request.get<Pick<Api.SystemManage.UserListItem, 'id' | 'userName' | 'nickName'>[]>({
    url: '/api/message/receivers',
    params: { keyword }
  })
}

export function fetchGetOperationLogList(params: Api.SystemManage.OperationLogSearchParams) {
  return request.get<Api.SystemManage.OperationLogList>({
    url: '/api/log/list',
    params
  })
}

export function fetchDeleteOperationLog(id: number) {
  return request.del<void>({
    url: `/api/log/${id}`,
    showSuccessMessage: true
  })
}

export function fetchClearOperationLogs() {
  return request.del<void>({
    url: '/api/log/clear',
    showSuccessMessage: true
  })
}

export function fetchGetFileList(params: Api.SystemManage.ManagedFileSearchParams) {
  return request.get<Api.SystemManage.ManagedFileList>({
    url: '/api/file/list',
    params
  })
}

export function fetchUploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<Api.SystemManage.ManagedFileListItem>({
    url: '/api/file',
    data: formData,
    showSuccessMessage: true
  })
}

export function fetchDownloadFile(id: number) {
  return request.download({
    url: `/api/file/${id}/download`
  })
}

export function fetchDeleteFile(id: number) {
  return request.del<void>({
    url: `/api/file/${id}`,
    showSuccessMessage: true
  })
}

export function fetchGetMessageList(params: Api.SystemManage.MessageSearchParams) {
  return request.get<Api.SystemManage.MessageList>({
    url: '/api/message/list',
    params
  })
}

export function fetchGetUnreadMessageCount() {
  return request.get<number>({
    url: '/api/message/unread-count',
    showErrorMessage: false
  })
}

export function fetchSendMessage(data: Api.SystemManage.MessageSendParams) {
  return request.post<number>({
    url: '/api/message',
    data,
    showSuccessMessage: true
  })
}

export function fetchReadMessage(receiverId: number) {
  return request.put<void>({
    url: `/api/message/${receiverId}/read`
  })
}

export function fetchReadAllMessages() {
  return request.put<void>({
    url: '/api/message/read-all',
    showSuccessMessage: true
  })
}
