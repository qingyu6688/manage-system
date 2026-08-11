/**
 * API 接口类型定义模块
 *
 * 提供所有后端接口的类型定义
 *
 * ## 主要功能
 *
 * - 通用类型（分页参数、响应结构等）
 * - 认证类型（登录、用户信息等）
 * - 系统管理类型（用户、角色等）
 * - 全局命名空间声明
 *
 * ## 使用场景
 *
 * - API 请求参数类型约束
 * - API 响应数据类型定义
 * - 接口文档类型同步
 *
 * ## 注意事项
 *
 * - 在 .vue 文件使用需要在 eslint.config.mjs 中配置 globals: { Api: 'readonly' }
 * - 使用全局命名空间，无需导入即可使用
 *
 * ## 使用方式
 *
 * ```typescript
 * const params: Api.Auth.LoginParams = { userName: 'admin', password: '123456' }
 * const response: Api.Auth.UserInfo = await fetchUserInfo()
 * ```
 *
 * @module types/api/api
 * @author Art Design Pro Team
 */

declare namespace Api {
  /** 通用类型 */
  namespace Common {
    /** 分页参数 */
    interface PaginationParams {
      /** 当前页码 */
      current: number
      /** 每页条数 */
      size: number
      /** 总条数 */
      total: number
    }

    /** 通用搜索参数 */
    type CommonSearchParams = Pick<PaginationParams, 'current' | 'size'>

    /** 分页响应基础结构 */
    interface PaginatedResponse<T = any> {
      records: T[]
      current: number
      size: number
      total: number
    }

    /** 启用状态 */
    type EnableStatus = '1' | '2'
  }

  /** 认证类型 */
  namespace Auth {
    /** 登录参数 */
    interface LoginParams {
      userName: string
      password: string
    }

    /** 登录响应 */
    interface LoginResponse {
      token: string
      refreshToken: string
    }

    /** 用户信息 */
    interface UserInfo {
      buttons: string[]
      roles: string[]
      userId: number
      userName: string
      email: string
      avatar?: string
    }
  }

  /** 系统管理类型 */
  namespace SystemManage {
    /** 用户列表 */
    type UserList = Api.Common.PaginatedResponse<UserListItem>

    /** 用户列表项 */
    interface UserListItem {
      id: number
      avatar: string
      status: string
      userName: string
      userGender: string
      nickName: string
      userPhone: string
      userEmail: string
      roleIds: number[]
      userRoles: string[]
      createBy: string
      createTime: string
      updateBy: string
      updateTime: string
    }

    interface UserSaveParams {
      userName: string
      nickName: string
      gender: number
      userPhone: string
      userEmail: string
      avatar?: string
      password?: string
      status: number
      roleIds: number[]
    }

    /** 当前用户个人资料 */
    interface UserProfile {
      userId: number
      username: string
      realName: string
      nickname: string
      gender: number
      phone: string | null
      email: string
      avatar: string | null
      address: string | null
      bio: string | null
      roles: string[]
      createdAt: string
    }

    interface ProfileUpdateParams {
      realName: string
      nickname: string
      gender: number
      phone: string
      email: string
      bio: string
    }

    interface PasswordChangeParams {
      currentPassword: string
      newPassword: string
      confirmPassword: string
    }

    /** 用户搜索参数 */
    type UserSearchParams = Partial<
      Pick<UserListItem, 'id' | 'userName' | 'userGender' | 'userPhone' | 'userEmail' | 'status'> &
        Api.Common.CommonSearchParams
    >

    /** 角色列表 */
    type RoleList = Api.Common.PaginatedResponse<RoleListItem>

    /** 角色列表项 */
    interface RoleListItem {
      roleId: number
      roleName: string
      roleCode: string
      description: string
      enabled: boolean
      createTime: string
    }

    interface RoleOption {
      roleId: number
      roleName: string
      roleCode: string
    }

    interface RoleSaveParams {
      roleName: string
      roleCode: string
      description: string
      enabled: boolean
    }

    interface MenuSaveParams {
      parentId: number
      menuType: 'MENU' | 'BUTTON'
      title: string
      routeName?: string
      path?: string
      component?: string
      permission?: string
      icon?: string
      sort: number
      enabled: boolean
      hidden: boolean
      keepAlive: boolean
      hiddenTab: boolean
      iframe: boolean
      externalLink?: string
      badge: boolean
      textBadge?: string
      fixedTab: boolean
      activePath?: string
      fullPage: boolean
    }

    type AnnouncementType = 'SYSTEM' | 'ACTIVITY' | 'REMINDER'
    type AnnouncementStatus = 'DRAFT' | 'PUBLISHED'

    type AnnouncementList = Api.Common.PaginatedResponse<AnnouncementListItem>

    interface AnnouncementListItem {
      id: number
      title: string
      content: string
      type: AnnouncementType
      status: AnnouncementStatus
      top: boolean
      publishedBy: string | null
      publishedAt: string | null
      createdBy: string
      createdAt: string
      updatedBy: string
      updatedAt: string
    }

    interface AnnouncementSaveParams {
      title: string
      content: string
      type: AnnouncementType
      status: AnnouncementStatus
      top: boolean
    }

    type AnnouncementSearchParams = Partial<
      Pick<AnnouncementListItem, 'title' | 'type' | 'status'> & Api.Common.CommonSearchParams
    >

    type OperationLogType = 'OPERATION' | 'LOGIN'
    type OperationLogList = Api.Common.PaginatedResponse<OperationLogListItem>

    interface OperationLogListItem {
      id: number
      logType: OperationLogType
      username: string
      method: string
      path: string
      queryParams: string | null
      statusCode: number
      success: boolean
      ipAddress: string | null
      userAgent: string | null
      durationMs: number
      createdAt: string
    }

    type OperationLogSearchParams = Partial<
      Pick<OperationLogListItem, 'logType' | 'username' | 'path' | 'method' | 'success'> &
        Api.Common.CommonSearchParams
    >

    type ManagedFileList = Api.Common.PaginatedResponse<ManagedFileListItem>

    interface ManagedFileListItem {
      id: number
      originalName: string
      contentType: string
      fileSize: number
      uploader: string
      createdAt: string
    }

    type ManagedFileSearchParams = Partial<
      Pick<ManagedFileListItem, 'originalName' | 'contentType'> & Api.Common.CommonSearchParams
    >

    type MessageType = 'SYSTEM' | 'REMINDER' | 'TASK'
    type MessageReadStatus = 'READ' | 'UNREAD'
    type MessageList = Api.Common.PaginatedResponse<MessageListItem>

    interface MessageListItem {
      receiverId: number
      messageId: number
      title: string
      content: string
      type: MessageType
      sender: string
      createdAt: string
      read: boolean
      readAt: string | null
    }

    interface MessageSendParams {
      title: string
      content: string
      type: MessageType
      allUsers: boolean
      receiverIds: number[]
    }

    type MessageSearchParams = Partial<
      Pick<MessageListItem, 'type'> &
        Api.Common.CommonSearchParams & {
          keyword: string
          readStatus: MessageReadStatus
        }
    >

    /** 角色搜索参数 */
    type RoleSearchParams = Partial<
      Pick<RoleListItem, 'roleId' | 'roleName' | 'roleCode' | 'description' | 'enabled'> &
        Api.Common.CommonSearchParams & {
          startTime: string | null
          endTime: string | null
        }
    >
  }
}
