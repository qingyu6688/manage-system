/**
 * API 响应类型定义模块
 *
 * 提供统一的 API 响应结构类型定义
 *
 * ## 主要功能
 *
 * - 基础响应结构定义
 * - 泛型支持（适配不同数据类型）
 * - 统一的响应格式约束
 *
 * ## 使用场景
 *
 * - API 请求响应类型约束
 * - 接口数据类型定义
 * - 响应数据解析
 *
 * @module types/common/response
 * @author Art Design Pro Team
 */

/** 后端错误追踪信息 */
export interface ErrorDetail {
  /** 可在服务端日志中检索的错误编号 */
  errorId: string
  /** 稳定错误码 */
  errorCode: string
  /** 错误分类 */
  errorType: 'VALIDATION' | 'AUTHENTICATION' | 'AUTHORIZATION' | 'BUSINESS' | 'RESOURCE' | 'SYSTEM'
  /** 请求方法 */
  method: string
  /** 请求路径 */
  path: string
  /** 服务端记录时间 */
  timestamp: string
}

/** 基础 API 响应结构 */
export interface BaseResponse<T = unknown> {
  /** 状态码 */
  code: number
  /** 消息 */
  msg: string
  /** 数据 */
  data: T
  /** 失败响应的错误追踪信息 */
  error?: ErrorDetail
}
