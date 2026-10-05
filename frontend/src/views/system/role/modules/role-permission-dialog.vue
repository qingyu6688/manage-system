<template>
  <ElDialog
    v-model="visible"
    :title="$t('systemManage.role.menuPermission')"
    width="520px"
    align-center
    class="el-dialog-border"
    @close="handleClose"
  >
    <ElScrollbar height="70vh" v-loading="loading">
      <!-- 角色可以只授权菜单而不授权其下按钮，树组件自带的父子联动表达不了，改为严格模式手动联动 -->
      <ElTree
        ref="treeRef"
        :data="processedMenuList"
        show-checkbox
        check-strictly
        node-key="id"
        :default-expand-all="isExpandAll"
        :props="defaultProps"
        @check="handleNodeCheck"
      >
        <template #default="{ data }">
          <div style="display: flex; align-items: center">
            <span v-if="data.isAuth">
              {{ formatMenuTitle(data.label || '') }}
            </span>
            <span v-else>{{ defaultProps.label(data) }}</span>
          </div>
        </template>
      </ElTree>
    </ElScrollbar>
    <template #footer>
      <ElButton @click="toggleExpandAll">{{
        isExpandAll ? $t('systemManage.role.collapseAll') : $t('systemManage.role.expandAll')
      }}</ElButton>
      <ElButton @click="toggleSelectAll" style="margin-left: 8px">{{
        isSelectAll ? $t('systemManage.role.unselectAll') : $t('systemManage.role.selectAll')
      }}</ElButton>
      <ElButton type="primary" :loading="saving" :disabled="loading" @click="savePermission">{{
        $t('common.save')
      }}</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { AppRouteRecord } from '@/types/router'
  import { formatMenuTitle } from '@/utils/router'
  import { fetchGetMenuTree, fetchGetRoleMenuIds, fetchUpdateRoleMenus } from '@/api/system-manage'
  import { toggleMenuPermission, type TreeKey } from '@/utils/menu-permission'

  type RoleListItem = Api.SystemManage.RoleListItem

  interface Props {
    modelValue: boolean
    roleData?: RoleListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    roleData: undefined
  })

  const emit = defineEmits<Emits>()

  const menuTree = ref<AppRouteRecord[]>([])
  const treeRef = ref()
  const isExpandAll = ref(true)
  const isSelectAll = ref(false)
  const saving = ref(false)
  const loading = ref(false)

  /**
   * 弹窗显示状态双向绑定
   */
  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  /**
   * 菜单节点类型
   */
  interface MenuNode {
    id?: string | number
    name?: string
    label?: string
    meta?: {
      title?: string
      authList?: Array<{
        id?: number
        authMark: string
        title: string
        checked?: boolean
      }>
    }
    children?: MenuNode[]
    [key: string]: any
  }

  /**
   * 处理菜单数据，将 authList 转换为树形子节点
   * 递归处理菜单树，将权限列表展开为可选择的子节点
   */
  const processedMenuList = computed(() => {
    const processNode = (node: MenuNode): MenuNode => {
      const processed = { ...node }

      // 如果有 authList，将其转换为子节点
      if (node.meta?.authList?.length) {
        const authNodes = node.meta.authList.map((auth) => ({
          id: auth.id,
          name: `${node.name}_${auth.authMark}`,
          label: auth.title,
          authMark: auth.authMark,
          isAuth: true,
          checked: auth.checked || false
        }))

        processed.children = processed.children ? [...processed.children, ...authNodes] : authNodes
      }

      // 递归处理子节点
      if (processed.children) {
        processed.children = processed.children.map(processNode)
      }

      return processed
    }

    // 使用完整菜单树而不是当前登录人的菜单，否则未授权给自己的菜单无法分配给其他角色
    return (menuTree.value as MenuNode[]).map(processNode)
  })

  /**
   * 树形组件配置
   */
  const defaultProps = {
    children: 'children',
    label: (data: any) => formatMenuTitle(data.meta?.title) || data.label || ''
  }

  /**
   * 监听弹窗打开，加载完整菜单树和角色已授权节点
   */
  watch(
    () => props.modelValue,
    async (open) => {
      if (!open || !props.roleData) return
      loading.value = true
      try {
        const [tree, menuIds] = await Promise.all([
          fetchGetMenuTree(),
          fetchGetRoleMenuIds(props.roleData.roleId)
        ])
        menuTree.value = tree
        await nextTick()
        // 严格模式下按原样回填，父节点不会再把所有子节点一起勾上
        treeRef.value?.setCheckedKeys(menuIds)
        handleTreeCheck()
      } finally {
        loading.value = false
      }
    }
  )

  /**
   * 关闭弹窗并清空选中状态
   */
  const handleClose = () => {
    visible.value = false
    treeRef.value?.setCheckedKeys([])
  }

  /**
   * 勾选节点时同时勾选下级和全部上级（上级未授权时子节点不可达），取消时一并取消下级
   */
  const handleNodeCheck = (data: MenuNode, state: { checkedKeys: TreeKey[] }) => {
    const tree = treeRef.value
    if (!tree || data.id == null) return
    const checked = state.checkedKeys.includes(data.id)
    tree.setCheckedKeys(
      toggleMenuPermission(processedMenuList.value, state.checkedKeys, data.id, checked)
    )
    handleTreeCheck()
  }

  /**
   * 保存权限配置
   */
  const savePermission = async () => {
    if (!treeRef.value || !props.roleData) return
    const checkedKeys = treeRef.value.getCheckedKeys() as Array<number | string>
    const menuIds = [...new Set(checkedKeys)].map(Number).filter(Number.isFinite)

    saving.value = true
    try {
      await fetchUpdateRoleMenus(props.roleData.roleId, menuIds)
      emit('success')
      handleClose()
    } finally {
      saving.value = false
    }
  }

  /**
   * 切换全部展开/收起状态
   */
  const toggleExpandAll = () => {
    const tree = treeRef.value
    if (!tree) return

    const nodes = tree.store.nodesMap
    // 这里保留 any，因为 Element Plus 的内部节点类型较复杂
    Object.values(nodes).forEach((node: any) => {
      node.expanded = !isExpandAll.value
    })

    isExpandAll.value = !isExpandAll.value
  }

  /**
   * 切换全选/取消全选状态
   */
  const toggleSelectAll = () => {
    const tree = treeRef.value
    if (!tree) return

    if (!isSelectAll.value) {
      const allKeys = getAllNodeKeys(processedMenuList.value)
      tree.setCheckedKeys(allKeys)
    } else {
      tree.setCheckedKeys([])
    }

    isSelectAll.value = !isSelectAll.value
  }

  /**
   * 递归获取所有节点的 key
   * @param nodes 节点列表
   * @returns 所有节点的 key 数组
   */
  const getAllNodeKeys = (nodes: MenuNode[]): Array<string | number> => {
    const keys: Array<string | number> = []
    const traverse = (nodeList: MenuNode[]): void => {
      nodeList.forEach((node) => {
        if (node.id != null) keys.push(node.id)
        if (node.children?.length) traverse(node.children)
      })
    }
    traverse(nodes)
    return keys
  }

  /**
   * 同步更新全选按钮状态
   */
  const handleTreeCheck = () => {
    const tree = treeRef.value
    if (!tree) return

    const checkedKeys = tree.getCheckedKeys()
    const allKeys = getAllNodeKeys(processedMenuList.value)

    isSelectAll.value = checkedKeys.length === allKeys.length && allKeys.length > 0
  }
</script>
