import { fetchLogout } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'

/**
 * 主动退出：先尽力撤销服务端会话，再清理本地登录状态。
 *
 * 撤销失败（后端不可用、网络中断）时也必须完成本地退出，否则用户在公共电脑上无法登出；
 * 没能撤销的服务端会话会在刷新令牌到期后自然失效。
 */
export async function logoutWithRevoke(): Promise<void> {
  const userStore = useUserStore()
  try {
    if (userStore.accessToken) await fetchLogout()
  } catch (error) {
    console.warn('[Logout] 服务端会话撤销失败，已继续清理本地登录状态', error)
  } finally {
    userStore.logOut()
  }
}
