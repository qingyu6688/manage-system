import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toggleMenuPermission, type KeyedTreeNode } from '../src/utils/menu-permission.ts'

// 与种子数据结构一致：系统管理(10) > 用户管理(11) > 新增/编辑/注销按钮(12~14)，角色管理(20) > 新增角色(21)
const tree: KeyedTreeNode[] = [
  {
    id: 10,
    children: [
      { id: 11, children: [{ id: 12 }, { id: 13 }, { id: 14 }] },
      { id: 20, children: [{ id: 21 }] }
    ]
  },
  { id: 1, children: [{ id: 2 }] }
]

const sorted = (keys: Array<string | number>) => [...keys].map(Number).sort((a, b) => a - b)

test('勾选按钮时补齐上级菜单，但不会顺带勾上兄弟菜单', () => {
  const keys = toggleMenuPermission(tree, [13], 13, true)
  assert.deepEqual(sorted(keys), [10, 11, 13])
})

test('勾选菜单时带上全部下级按钮，取消单个按钮后菜单保持只读授权', () => {
  const withButtons = toggleMenuPermission(tree, [11], 11, true)
  assert.deepEqual(sorted(withButtons), [10, 11, 12, 13, 14])

  const readOnly = ['12', '13', '14'].reduce<Array<string | number>>(
    (keys, id) =>
      toggleMenuPermission(
        tree,
        keys.filter((key) => key !== Number(id)),
        Number(id),
        false
      ),
    withButtons
  )
  assert.deepEqual(sorted(readOnly), [10, 11])
})

test('取消菜单时一并取消其下按钮，不影响上级和其他菜单', () => {
  const keys = toggleMenuPermission(tree, [10, 11, 12, 13, 20, 21, 1, 2], 11, false)
  assert.deepEqual(sorted(keys), [1, 2, 10, 20, 21])
})
