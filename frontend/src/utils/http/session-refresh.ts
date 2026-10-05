export interface SessionTokens {
  token: string
  refreshToken: string
}

/** 仅用于阻止跨会话重放；令牌真实性仍由服务端验证。 */
export function belongsToSameSession(sentToken: string, currentToken: string): boolean {
  const sessionId = (token: string): string | undefined => {
    try {
      const payload: unknown = JSON.parse(
        atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
      )
      if (
        typeof payload === 'object' &&
        payload !== null &&
        'sid' in payload &&
        typeof payload.sid === 'string'
      ) {
        return payload.sid
      }
    } catch {
      return undefined
    }
    return undefined
  }
  const sentSession = sessionId(sentToken)
  return Boolean(sentSession && sentSession === sessionId(currentToken))
}

interface SessionAdapter {
  currentRefreshToken: () => string
  renew: (token: string) => Promise<SessionTokens>
  save: (tokens: SessionTokens) => void
}

/** 同一页面的并发请求共用一次刷新，避免轮换令牌被重复消费。 */
export function createSessionRefresher(adapter: SessionAdapter): () => Promise<void> {
  let pending: Promise<void> | undefined
  return () => {
    if (!pending) {
      const token = adapter.currentRefreshToken()
      pending = Promise.resolve()
        .then(async () => {
          if (!token) throw new Error('会话已失效，请重新登录')
          const tokens = await adapter.renew(token)
          if (adapter.currentRefreshToken() !== token) {
            throw new Error('登录状态已变化，请重新操作')
          }
          adapter.save(tokens)
        })
        .finally(() => {
          pending = undefined
        })
    }
    return pending
  }
}
