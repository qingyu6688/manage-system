/**
 * HTTP 请求封装模块
 * 基于 Axios 封装的 HTTP 请求工具，提供统一的请求/响应处理
 *
 * ## 主要功能
 *
 * - 请求/响应拦截器（自动添加 Token、统一错误处理）
 * - 401 未授权自动登出（带防抖机制）
 * - 请求失败自动重试（可配置）
 * - 统一的成功/错误消息提示
 * - 支持 GET/POST/PUT/DELETE 等常用方法
 *
 * @module utils/http
 * @author Art Design Pro Team
 */

import axios, { AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { useUserStore } from '@/store/modules/user'
import { ApiStatus } from './status'
import { HttpError, handleError, showError, showSuccess } from './error'
import { $t } from '@/locales'
import { BaseResponse, ErrorDetail } from '@/types'
import { belongsToSameSession, createSessionRefresher } from './session-refresh'
import { parseBlobError } from './blob-error'

/** 请求配置常量 */
const REQUEST_TIMEOUT = 15000
const LOGOUT_DELAY = 500
const MAX_RETRIES = 0
const RETRY_DELAY = 1000
const UNAUTHORIZED_DEBOUNCE_TIME = 3000

/** 401防抖状态 */
let isUnauthorizedErrorShown = false
let unauthorizedTimer: NodeJS.Timeout | null = null

/** 扩展 AxiosRequestConfig */
interface ExtendedAxiosRequestConfig extends AxiosRequestConfig {
  showErrorMessage?: boolean
  showSuccessMessage?: boolean
  skipAuthRefresh?: boolean
  authRetried?: boolean
}

const { VITE_API_URL, VITE_WITH_CREDENTIALS } = import.meta.env

/** Axios实例 */
const axiosInstance = axios.create({
  timeout: REQUEST_TIMEOUT,
  baseURL: VITE_API_URL,
  withCredentials: VITE_WITH_CREDENTIALS === 'true',
  validateStatus: (status) => status >= 200 && status < 300,
  transformResponse: [
    (data, headers) => {
      const contentType = headers['content-type']
      if (contentType?.includes('application/json')) {
        try {
          return JSON.parse(data)
        } catch {
          return data
        }
      }
      return data
    }
  ]
})

/** 请求拦截器 */
axiosInstance.interceptors.request.use(
  (request: InternalAxiosRequestConfig) => {
    const { accessToken } = useUserStore()
    if (accessToken) request.headers.set('Authorization', `Bearer ${accessToken}`)

    if (request.data && !(request.data instanceof FormData) && !request.headers['Content-Type']) {
      request.headers.set('Content-Type', 'application/json')
      request.data = JSON.stringify(request.data)
    }

    return request
  },
  (error) => {
    showError(createHttpError($t('httpMsg.requestConfigError'), ApiStatus.error))
    return Promise.reject(error)
  }
)

/** 响应拦截器 */
axiosInstance.interceptors.response.use(
  (response: AxiosResponse<BaseResponse | Blob>) => {
    if (response.config.responseType === 'blob') return response
    if (response.data instanceof Blob) return response
    const { code, msg, error } = response.data
    if (code === ApiStatus.success) return response
    if (code === ApiStatus.unauthorized) return recoverSession(response.config, msg, error)
    throw new HttpError(msg || $t('httpMsg.requestFailed'), code, {
      data: response.data,
      url: response.config.url,
      method: response.config.method?.toUpperCase(),
      error
    })
  },
  (error) => {
    if (error.response?.status === ApiStatus.unauthorized) {
      return recoverSession(error.config, error.response?.data?.msg, error.response?.data?.error)
    }
    return Promise.reject(handleError(error))
  }
)

const refreshSession = createSessionRefresher({
  currentRefreshToken: () => useUserStore().refreshToken,
  renew: async (refreshToken) => {
    const config: ExtendedAxiosRequestConfig = {
      method: 'POST',
      url: '/api/auth/refresh',
      data: { refreshToken },
      skipAuthRefresh: true
    }
    const response = await axiosInstance.request<BaseResponse<Api.Auth.LoginResponse>>(config)
    return response.data.data
  },
  save: (tokens) => useUserStore().setToken(tokens.token, tokens.refreshToken)
})

async function recoverSession(
  config: ExtendedAxiosRequestConfig,
  message?: string,
  detail?: ErrorDetail
): Promise<AxiosResponse> {
  if (config.skipAuthRefresh || config.url === '/api/auth/login') {
    throw new HttpError(message || $t('httpMsg.unauthorized'), ApiStatus.unauthorized)
  }
  const store = useUserStore()
  const sentToken = String(config.headers?.Authorization || '').replace(/^Bearer /, '')
  if (!belongsToSameSession(sentToken, store.accessToken)) {
    if (sentToken === store.accessToken) handleUnauthorizedError(message, detail)
    throw new HttpError('登录状态已变化，请重新操作', ApiStatus.unauthorized)
  }
  if (!config.authRetried && store.refreshToken) {
    const originalRefresh = store.refreshToken
    try {
      // 较晚返回的旧令牌 401 可以直接使用其他请求已经刷新的令牌。
      if (sentToken === store.accessToken) await refreshSession()
    } catch (error) {
      if (store.refreshToken !== originalRefresh) throw error
      handleUnauthorizedError(message, detail)
    }
    if (!belongsToSameSession(sentToken, store.accessToken)) {
      throw new HttpError('登录状态已变化，请重新操作', ApiStatus.unauthorized)
    }
    return axiosInstance.request({ ...config, authRetried: true } as ExtendedAxiosRequestConfig)
  }
  return handleUnauthorizedError(message, detail)
}

/** 统一创建HttpError */
function createHttpError(message: string, code: number) {
  return new HttpError(message, code)
}

/** 处理401错误（带防抖） */
function handleUnauthorizedError(message?: string, errorDetail?: ErrorDetail): never {
  const error = new HttpError(message || $t('httpMsg.unauthorized'), ApiStatus.unauthorized, {
    error: errorDetail
  })

  if (!isUnauthorizedErrorShown) {
    isUnauthorizedErrorShown = true
    logOut()

    unauthorizedTimer = setTimeout(resetUnauthorizedError, UNAUTHORIZED_DEBOUNCE_TIME)

    showError(error, true)
    throw error
  }

  throw error
}

/** 重置401防抖状态 */
function resetUnauthorizedError() {
  isUnauthorizedErrorShown = false
  if (unauthorizedTimer) clearTimeout(unauthorizedTimer)
  unauthorizedTimer = null
}

/** 退出登录函数 */
function logOut() {
  setTimeout(() => {
    useUserStore().logOut()
  }, LOGOUT_DELAY)
}

/** 是否需要重试 */
function shouldRetry(statusCode: number) {
  return [
    ApiStatus.requestTimeout,
    ApiStatus.internalServerError,
    ApiStatus.badGateway,
    ApiStatus.serviceUnavailable,
    ApiStatus.gatewayTimeout
  ].includes(statusCode)
}

/** 请求重试逻辑 */
async function retryRequest<T>(
  config: ExtendedAxiosRequestConfig,
  retries: number = MAX_RETRIES
): Promise<T> {
  try {
    return await request<T>(config)
  } catch (error) {
    if (retries > 0 && error instanceof HttpError && shouldRetry(error.code)) {
      await delay(RETRY_DELAY)
      return retryRequest<T>(config, retries - 1)
    }
    throw error
  }
}

/** 延迟函数 */
function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/** 请求函数 */
async function request<T = unknown>(config: ExtendedAxiosRequestConfig): Promise<T> {
  // POST | PUT 参数自动填充
  if (
    ['POST', 'PUT'].includes(config.method?.toUpperCase() || '') &&
    config.params &&
    !config.data
  ) {
    config.data = config.params
    config.params = undefined
  }

  try {
    const res = await axiosInstance.request<BaseResponse<T>>(config)

    // 显示成功消息
    if (config.showSuccessMessage && res.data.msg) {
      showSuccess(res.data.msg)
    }

    return res.data.data as T
  } catch (error) {
    if (error instanceof HttpError && error.code !== ApiStatus.unauthorized) {
      const showMsg = config.showErrorMessage !== false
      showError(error, showMsg)
    }
    return Promise.reject(error)
  }
}

/**
 * 下载二进制文件，跳过业务响应结构解析。
 * 后端业务错误（如文件已被移除）以 HTTP 200 + JSON 返回，需要识别出来，不能当成文件保存。
 */
async function download(config: ExtendedAxiosRequestConfig): Promise<Blob> {
  try {
    const response = await axiosInstance.request<Blob>({
      ...config,
      method: config.method || 'GET',
      responseType: 'blob'
    })
    const blobError = await parseBlobError(response.data)
    if (blobError) {
      throw new HttpError(blobError.msg || $t('httpMsg.requestFailed'), blobError.code, {
        data: blobError,
        url: config.url,
        method: config.method?.toUpperCase() || 'GET',
        error: blobError.error
      })
    }
    return response.data
  } catch (error) {
    const httpError = await normalizeBlobHttpError(error, config)
    if (httpError instanceof HttpError && httpError.code !== ApiStatus.unauthorized) {
      showError(httpError, config.showErrorMessage !== false)
    }
    return Promise.reject(httpError)
  }
}

/** HTTP 层错误（403、500 等）的响应体在 blob 模式下也是 Blob，需要转换后才能拿到后端提示 */
async function normalizeBlobHttpError(
  error: unknown,
  config: ExtendedAxiosRequestConfig
): Promise<unknown> {
  if (!(error instanceof HttpError) || !(error.data instanceof Blob)) return error
  const blobError = await parseBlobError(error.data)
  if (!blobError) return error
  return new HttpError(blobError.msg || error.message, blobError.code, {
    data: blobError,
    url: config.url,
    method: config.method?.toUpperCase() || 'GET',
    error: blobError.error
  })
}

/** API方法集合 */
const api = {
  get<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'GET' })
  },
  post<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'POST' })
  },
  put<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'PUT' })
  },
  del<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'DELETE' })
  },
  request<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>(config)
  },
  download(config: ExtendedAxiosRequestConfig) {
    return download(config)
  }
}

export default api
