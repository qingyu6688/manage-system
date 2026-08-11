/**
 * 路由工具函数
 *
 * 提供路由相关的工具函数
 *
 * @module utils/router
 */
import { RouteLocationNormalized, RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import i18n, { $t } from '@/locales'

/**
 * 兼容后端初始数据中的中文菜单标题。
 * 新增菜单时可以直接保存翻译键，旧数据则通过此映射平滑支持中英文切换。
 */
const LEGACY_MENU_TITLE_MAP: Record<string, string> = {
  工作台: 'menus.dashboard.title',
  控制台: 'menus.dashboard.console',
  系统管理: 'menus.system.title',
  用户管理: 'menus.system.user',
  角色管理: 'menus.system.role',
  菜单管理: 'menus.system.menu',
  公告管理: 'menus.system.announcement',
  操作日志: 'menus.system.operationLog',
  日志管理: 'menus.system.operationLog',
  文件管理: 'menus.system.fileManage',
  消息通知: 'menus.system.messageCenter',
  个人中心: 'menus.system.userCenter',
  新增用户: 'systemManage.permissions.addUser',
  编辑用户: 'systemManage.permissions.editUser',
  注销用户: 'systemManage.permissions.deactivateUser',
  新增角色: 'systemManage.permissions.addRole',
  编辑角色: 'systemManage.permissions.editRole',
  删除角色: 'systemManage.permissions.deleteRole',
  菜单授权: 'systemManage.permissions.assignMenu',
  新增菜单: 'systemManage.permissions.addMenu',
  编辑菜单: 'systemManage.permissions.editMenu',
  删除菜单: 'systemManage.permissions.deleteMenu',
  新增公告: 'systemManage.permissions.addAnnouncement',
  编辑公告: 'systemManage.permissions.editAnnouncement',
  删除公告: 'systemManage.permissions.deleteAnnouncement',
  发布公告: 'systemManage.permissions.publishAnnouncement',
  删除日志: 'systemManage.permissions.deleteLog',
  清空日志: 'systemManage.permissions.clearLog',
  上传文件: 'systemManage.permissions.uploadFile',
  下载文件: 'systemManage.permissions.downloadFile',
  删除文件: 'systemManage.permissions.deleteFile',
  发送消息: 'systemManage.permissions.sendMessage',
  标记已读: 'systemManage.permissions.readMessage'
}

/** 扩展的路由配置类型 */
export type AppRouteRecordRaw = RouteRecordRaw & {
  hidden?: boolean
}

/** 顶部进度条配置 */
export const configureNProgress = () => {
  NProgress.configure({
    easing: 'ease',
    speed: 600,
    showSpinner: false,
    parent: 'body'
  })
}

/**
 * 设置页面标题，根据路由元信息和系统信息拼接标题
 * @param to 当前路由对象
 */
export const setPageTitle = (to: RouteLocationNormalized): void => {
  const { title } = to.meta
  if (title) {
    setTimeout(() => {
      document.title = `${formatMenuTitle(String(title))} - ${$t('app.name')}`
    }, 150)
  }
}

/**
 * 格式化菜单标题
 * @param title 菜单标题，可以是 i18n 的 key，也可以是字符串
 * @returns 格式化后的菜单标题
 */
export const formatMenuTitle = (title: string): string => {
  if (title) {
    const translationKey = LEGACY_MENU_TITLE_MAP[title] || title

    // 使用 te() 方法检查翻译键值是否存在，避免控制台警告
    if (i18n.global.te(translationKey)) {
      return $t(translationKey)
    }

    if (title.startsWith('menus.')) {
      // 如果翻译不存在，返回键值的最后部分作为 fallback
      return title.split('.').pop() || title
    }

    return title
  }
  return ''
}
