import { test } from 'node:test'
import assert from 'node:assert/strict'
import {
  belongsToSameSession,
  createSessionRefresher,
  type SessionTokens
} from '../src/utils/http/session-refresh.ts'

test('同一会话的轮换可重试，切换账号或重新登录后禁止重放旧请求', () => {
  const token = (sid: string, revision: number) =>
    `header.${Buffer.from(JSON.stringify({ sid, revision })).toString('base64url')}.signature`
  assert.equal(belongsToSameSession(token('会话一', 1), token('会话一', 2)), true)
  assert.equal(belongsToSameSession(token('会话一', 1), token('会话二', 1)), false)
  assert.equal(belongsToSameSession(token('会话一', 1), ''), false)
  assert.equal(belongsToSameSession('格式不正确的令牌', '格式不正确的令牌'), false)
})

test('并发失效请求只消费一次刷新令牌', async () => {
  let token = '旧刷新令牌'
  let calls = 0
  const refresh = createSessionRefresher({
    currentRefreshToken: () => token,
    renew: async () => {
      calls++
      await new Promise((resolve) => setTimeout(resolve, 5))
      return { token: '新访问令牌', refreshToken: '新刷新令牌' }
    },
    save: (result) => {
      token = result.refreshToken
    }
  })
  await Promise.all(Array.from({ length: 8 }, () => refresh()))
  assert.equal(calls, 1)
  assert.equal(token, '新刷新令牌')
})

test('退出登录后到达的刷新结果不能恢复旧会话', async () => {
  let token = '旧刷新令牌'
  let finish: (value: SessionTokens) => void = () => {}
  let saved = false
  const refresh = createSessionRefresher({
    currentRefreshToken: () => token,
    renew: () =>
      new Promise((resolve) => {
        finish = resolve
      }),
    save: () => {
      saved = true
    }
  })
  const pending = refresh()
  await Promise.resolve()
  token = ''
  finish({ token: '迟到的访问令牌', refreshToken: '迟到的刷新令牌' })
  await assert.rejects(pending, /登录状态已变化/)
  assert.equal(saved, false)
})

test('刷新失败后释放等待状态，后续操作不会永久挂起', async () => {
  let calls = 0
  const refresh = createSessionRefresher({
    currentRefreshToken: () => '当前令牌',
    renew: async () => {
      calls++
      throw new Error('网络不可用')
    },
    save: () => {
      assert.fail('失败结果不能保存')
    }
  })
  await assert.rejects(refresh(), /网络不可用/)
  await assert.rejects(refresh(), /网络不可用/)
  assert.equal(calls, 2)
})
