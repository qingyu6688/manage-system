import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parseBlobError } from '../src/utils/http/blob-error.ts'

const jsonBlob = (body: unknown) => new Blob([JSON.stringify(body)], { type: 'application/json' })

test('文件被移除时返回的业务错误不能当成文件保存', async () => {
  const error = await parseBlobError(
    jsonBlob({ code: 404, msg: '文件不存在或已被移除', data: null })
  )
  assert.equal(error?.code, 404)
  assert.equal(error?.msg, '文件不存在或已被移除')
})

test('真实文件内容原样放行', async () => {
  assert.equal(await parseBlobError(new Blob(['%PDF-1.7'], { type: 'application/pdf' })), undefined)
  assert.equal(await parseBlobError(new Blob(['a,b\n1,2'], { type: 'text/csv' })), undefined)
})

test('上传的 JSON 文件或成功响应不误判为错误', async () => {
  assert.equal(await parseBlobError(jsonBlob({ code: 200, msg: '操作成功' })), undefined)
  assert.equal(await parseBlobError(jsonBlob([{ name: '导出数据' }])), undefined)
  assert.equal(
    await parseBlobError(new Blob(['{损坏的JSON'], { type: 'application/json' })),
    undefined
  )
})
