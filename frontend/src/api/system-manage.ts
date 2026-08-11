import request from '@/utils/http'
import { AppRouteRecord } from '@/types/router'

// 获取用户列表
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return request.get<Api.SystemManage.UserList>({
    url: '/api/user/list',
    params
  })
}

export function fetchCreateUser(data: Api.SystemManage.UserSaveParams) {
  return request.post<number>({
    url: '/api/user',
    data,
    showSuccessMessage: true
  })
}

export function fetchUpdateUser(id: number, data: Api.SystemManage.UserSaveParams) {
  return request.put<void>({
    url: `/api/user/${id}`,
    data,
    showSuccessMessage: true
  })
}

export function fetchDeleteUser(id: number) {
  return request.del<void>({
    url: `/api/user/${id}`,
    showSuccessMessage: true
  })
}

// 获取角色列表
export function fetchGetRoleList(params: Api.SystemManage.RoleSearchParams) {
  return request.get<Api.SystemManage.RoleList>({
    url: '/api/role/list',
    params
  })
}

export function fetchGetRoleOptions() {
  return request.get<Api.SystemManage.RoleOption[]>({
    url: '/api/role/options'
  })
}

export function fetchCreateRole(data: Api.SystemManage.RoleSaveParams) {
  return request.post<number>({
    url: '/api/role',
    data,
    showSuccessMessage: true
  })
}

export function fetchUpdateRole(id: number, data: Api.SystemManage.RoleSaveParams) {
  return request.put<void>({
    url: `/api/role/${id}`,
    data,
    showSuccessMessage: true
  })
}

export function fetchDeleteRole(id: number) {
  return request.del<void>({
    url: `/api/role/${id}`,
    showSuccessMessage: true
  })
}

export function fetchGetRoleMenuIds(id: number) {
  return request.get<number[]>({
    url: `/api/role/${id}/menu-ids`
  })
}

export function fetchUpdateRoleMenus(id: number, menuIds: number[]) {
  return request.put<void>({
    url: `/api/role/${id}/menus`,
    data: { menuIds },
    showSuccessMessage: true
  })
}

// 获取菜单列表
export function fetchGetMenuList() {
  return request.get<AppRouteRecord[]>({
    url: '/api/v3/system/menus'
  })
}

export function fetchGetMenuTree() {
  return request.get<AppRouteRecord[]>({
    url: '/api/menu/tree'
  })
}

export function fetchCreateMenu(data: Api.SystemManage.MenuSaveParams) {
  return request.post<number>({
    url: '/api/menu',
    data,
    showSuccessMessage: true
  })
}

export function fetchUpdateMenu(id: number, data: Api.SystemManage.MenuSaveParams) {
  return request.put<void>({
    url: `/api/menu/${id}`,
    data,
    showSuccessMessage: true
  })
}

export function fetchDeleteMenu(id: number) {
  return request.del<void>({
    url: `/api/menu/${id}`,
    showSuccessMessage: true
  })
}

// 获取公告列表
export function fetchGetAnnouncementList(params: Api.SystemManage.AnnouncementSearchParams) {
  return request.get<Api.SystemManage.AnnouncementList>({
    url: '/api/announcement/list',
    params
  })
}

export function fetchCreateAnnouncement(data: Api.SystemManage.AnnouncementSaveParams) {
  return request.post<number>({
    url: '/api/announcement',
    data,
    showSuccessMessage: true
  })
}

export function fetchUpdateAnnouncement(id: number, data: Api.SystemManage.AnnouncementSaveParams) {
  return request.put<void>({
    url: `/api/announcement/${id}`,
    data,
    showSuccessMessage: true
  })
}

export function fetchUpdateAnnouncementStatus(
  id: number,
  status: Api.SystemManage.AnnouncementStatus
) {
  return request.put<void>({
    url: `/api/announcement/${id}/status`,
    data: { status },
    showSuccessMessage: true
  })
}

export function fetchDeleteAnnouncement(id: number) {
  return request.del<void>({
    url: `/api/announcement/${id}`,
    showSuccessMessage: true
  })
}
