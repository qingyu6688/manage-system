import { ApiStatus } from './status'

/** 下载接口失败时后端返回的统一响应结构 */
export interface BlobErrorBody {
  code: number
  msg?: string
  error?: {
    errorId: string
    errorCode: string
    errorType:
      | 'VALIDATION'
      | 'AUTHENTICATION'
      | 'AUTHORIZATION'
      | 'BUSINESS'
      | 'RESOURCE'
      | 'SYSTEM'
    method: string
    path: string
    timestamp: string
  }
}

/**
 * 从 blob 响应中识别后端错误。
 *
 * 业务错误（如文件已被移除）以 HTTP 200 + JSON 返回，blob 模式下会被当成正常文件；
 * 这里只认 application/json 且 code 不是成功码的内容，真实文件返回 undefined。
 */
export async function parseBlobError(data: unknown): Promise<BlobErrorBody | undefined> {
  if (!(data instanceof Blob) || !data.type.includes('application/json')) return undefined
  let parsed: unknown
  try {
    parsed = JSON.parse(await data.text())
  } catch (parseError) {
    console.warn('[HTTP] 下载接口返回的 JSON 无法解析', parseError)
    return undefined
  }
  if (
    typeof parsed === 'object' &&
    parsed !== null &&
    'code' in parsed &&
    typeof parsed.code === 'number' &&
    parsed.code !== ApiStatus.success
  ) {
    return parsed as BlobErrorBody
  }
  return undefined
}
