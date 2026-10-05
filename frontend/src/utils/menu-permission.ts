export type TreeKey = string | number

export interface KeyedTreeNode {
  id?: TreeKey
  children?: KeyedTreeNode[]
}

/**
 * 计算勾选或取消某个菜单/按钮节点后的授权集合。
 *
 * 角色可以只授权菜单而不授权其下按钮（只读），树组件自带的父子联动表达不了这种状态，
 * 所以授权树使用严格模式，联动规则在这里实现：
 * - 勾选：连同全部下级，并补齐全部上级（上级未授权时子节点在后端不可达）
 * - 取消：连同全部下级，上级保持不变
 */
export function toggleMenuPermission(
  tree: KeyedTreeNode[],
  checkedKeys: Iterable<TreeKey>,
  key: TreeKey,
  checked: boolean
): TreeKey[] {
  const result = new Set<TreeKey>(checkedKeys)
  const path = findPath(tree, key)
  if (!path) return [...result]

  const applyBranch = (node: KeyedTreeNode): void => {
    if (node.id != null) {
      if (checked) result.add(node.id)
      else result.delete(node.id)
    }
    node.children?.forEach(applyBranch)
  }
  applyBranch(path[path.length - 1])

  if (checked) {
    path.slice(0, -1).forEach((ancestor) => {
      if (ancestor.id != null) result.add(ancestor.id)
    })
  }
  return [...result]
}

/** 返回从根节点到目标节点的路径，找不到时返回 undefined */
function findPath(nodes: KeyedTreeNode[], key: TreeKey): KeyedTreeNode[] | undefined {
  for (const node of nodes) {
    if (node.id === key) return [node]
    if (node.children?.length) {
      const childPath = findPath(node.children, key)
      if (childPath) return [node, ...childPath]
    }
  }
  return undefined
}
