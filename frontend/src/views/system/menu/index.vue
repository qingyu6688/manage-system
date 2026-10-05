<!-- 菜单管理页面 -->
<template>
  <div class="menu-page art-full-height">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="formFilters"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card">
      <!-- 表格头部 -->
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElButton v-auth="'add'" @click="handleAddMenu" v-ripple>
            {{ $t('systemManage.menu.addButton') }}
          </ElButton>
          <ElButton @click="toggleExpand" v-ripple>
            {{ isExpanded ? $t('common.collapse') : $t('common.expand') }}
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        rowKey="path"
        :loading="loading"
        :columns="columns"
        :data="filteredTableData"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      />

      <!-- 菜单弹窗 -->
      <MenuDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :editData="editData"
        :lockType="lockMenuType"
        :parent-options="parentOptions"
        @submit="handleSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { formatMenuTitle } from '@/utils/router'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import type { AppRouteRecord } from '@/types/router'
  import MenuDialog from './modules/menu-dialog.vue'
  import {
    fetchCreateMenu,
    fetchDeleteMenu,
    fetchGetMenuTree,
    fetchUpdateMenu
  } from '@/api/system-manage'
  import { ElTag, ElMessageBox } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { isHandledRejection } from '@/utils/http/error'

  defineOptions({ name: 'Menus' })

  const { t } = useI18n()

  // 状态管理
  const loading = ref(false)
  const isExpanded = ref(false)
  const tableRef = ref()

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'MENU' | 'BUTTON'>('MENU')
  const editData = ref<AppRouteRecord | any>(null)
  const lockMenuType = ref(false)

  // 搜索相关
  const initialSearchState = {
    name: '',
    route: ''
  }

  const formFilters = reactive({ ...initialSearchState })
  const appliedFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: t('systemManage.menu.name'),
      key: 'name',
      type: 'input',
      props: { clearable: true }
    },
    {
      label: t('systemManage.menu.route'),
      key: 'route',
      type: 'input',
      props: { clearable: true }
    }
  ])

  onMounted(() => {
    getMenuList()
  })

  /**
   * 获取菜单列表数据
   */
  const getMenuList = async (): Promise<void> => {
    loading.value = true

    try {
      const list = await fetchGetMenuTree()
      tableData.value = list
    } catch (error) {
      throw error instanceof Error ? error : new Error(t('systemManage.menu.loadFailed'))
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取菜单类型标签颜色
   * @param row 菜单行数据
   * @returns 标签颜色类型
   */
  const getMenuTypeTag = (
    row: AppRouteRecord
  ): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
    if (row.meta?.isAuthButton) return 'danger'
    if (row.children?.length) return 'info'
    if (row.meta?.link && row.meta?.isIframe) return 'success'
    if (row.path) return 'primary'
    if (row.meta?.link) return 'warning'
    return 'info'
  }

  /**
   * 获取菜单类型文本
   * @param row 菜单行数据
   * @returns 菜单类型文本
   */
  const getMenuTypeText = (row: AppRouteRecord): string => {
    if (row.meta?.isAuthButton) return t('systemManage.menu.types.button')
    if (row.children?.length) return t('systemManage.menu.types.directory')
    if (row.meta?.link && row.meta?.isIframe) return t('systemManage.menu.types.iframe')
    if (row.path) return t('systemManage.menu.types.menu')
    if (row.meta?.link) return t('systemManage.menu.types.external')
    return t('systemManage.menu.types.unknown')
  }

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'meta.title',
      label: t('systemManage.menu.name'),
      minWidth: 120,
      formatter: (row: AppRouteRecord) => formatMenuTitle(row.meta?.title)
    },
    {
      prop: 'type',
      label: t('systemManage.menu.type'),
      formatter: (row: AppRouteRecord) => {
        return h(ElTag, { type: getMenuTypeTag(row) }, () => getMenuTypeText(row))
      }
    },
    {
      prop: 'path',
      label: t('systemManage.menu.route'),
      formatter: (row: AppRouteRecord) => {
        if (row.meta?.isAuthButton) return ''
        return row.meta?.link || row.path || ''
      }
    },
    {
      prop: 'meta.authList',
      label: t('systemManage.menu.permission'),
      formatter: (row: AppRouteRecord) => {
        if (row.meta?.isAuthButton) {
          return row.meta?.authMark || ''
        }
        if (!row.meta?.authList?.length) return ''
        return t('systemManage.menu.permissionCount', { count: row.meta.authList.length })
      }
    },
    {
      prop: 'status',
      label: t('common.status'),
      formatter: (row: AppRouteRecord) =>
        h(ElTag, { type: row.meta?.isEnable === false ? 'info' : 'success' }, () =>
          row.meta?.isEnable === false ? t('common.stopped') : t('common.enabled')
        )
    },
    {
      prop: 'operation',
      label: t('common.operations'),
      width: 180,
      align: 'right',
      formatter: (row: AppRouteRecord) => {
        const buttonStyle = { style: 'text-align: right' }

        if (row.meta?.isAuthButton) {
          return h('div', buttonStyle, [
            h(ArtButtonTable, {
              type: 'edit',
              onClick: () => handleEditAuth(row)
            }),
            h(ArtButtonTable, {
              type: 'delete',
              onClick: () => handleDeleteAuth(row)
            })
          ])
        }

        return h('div', buttonStyle, [
          h(ArtButtonTable, {
            type: 'add',
            onClick: () => handleAddAuth(row),
            title: t('systemManage.menu.addPermission')
          }),
          h(ArtButtonTable, {
            type: 'edit',
            onClick: () => handleEditMenu(row)
          }),
          h(ArtButtonTable, {
            type: 'delete',
            onClick: () => handleDeleteMenu(row)
          })
        ])
      }
    }
  ])

  // 数据相关
  const tableData = ref<AppRouteRecord[]>([])

  /**
   * 重置搜索条件
   */
  const handleReset = (): void => {
    Object.assign(formFilters, { ...initialSearchState })
    Object.assign(appliedFilters, { ...initialSearchState })
    getMenuList()
  }

  /**
   * 执行搜索
   */
  const handleSearch = (): void => {
    Object.assign(appliedFilters, { ...formFilters })
    getMenuList()
  }

  /**
   * 刷新菜单列表
   */
  const handleRefresh = (): void => {
    getMenuList()
  }

  /**
   * 深度克隆对象
   * @param obj 要克隆的对象
   * @returns 克隆后的对象
   */
  const deepClone = <T,>(obj: T): T => {
    if (obj === null || typeof obj !== 'object') return obj
    if (obj instanceof Date) return new Date(obj) as T
    if (Array.isArray(obj)) return obj.map((item) => deepClone(item)) as T

    const cloned = {} as T
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        cloned[key] = deepClone(obj[key])
      }
    }
    return cloned
  }

  /**
   * 将权限列表转换为子节点
   * @param items 菜单项数组
   * @returns 转换后的菜单项数组
   */
  const convertAuthListToChildren = (items: AppRouteRecord[]): AppRouteRecord[] => {
    return items.map((item) => {
      const clonedItem = deepClone(item)

      if (clonedItem.children?.length) {
        clonedItem.children = convertAuthListToChildren(clonedItem.children)
      }

      if (item.meta?.authList?.length) {
        const authChildren: AppRouteRecord[] = item.meta.authList.map(
          (auth: {
            id?: number
            title: string
            authMark: string
            enabled?: boolean
            sort?: number
          }) => ({
            id: auth.id,
            parentId: item.id,
            path: '',
            name: '',
            meta: {
              title: auth.title,
              authMark: auth.authMark,
              isAuthButton: true,
              parentPath: item.path,
              isEnable: auth.enabled ?? true,
              sort: auth.sort ?? 10
            }
          })
        )

        clonedItem.children = clonedItem.children?.length
          ? [...clonedItem.children, ...authChildren]
          : authChildren
      }

      return clonedItem
    })
  }

  /**
   * 搜索菜单
   * @param items 菜单项数组
   * @returns 搜索结果数组
   */
  const searchMenu = (items: AppRouteRecord[]): AppRouteRecord[] => {
    const results: AppRouteRecord[] = []

    for (const item of items) {
      const searchName = appliedFilters.name?.toLowerCase().trim() || ''
      const searchRoute = appliedFilters.route?.toLowerCase().trim() || ''
      const menuTitle = formatMenuTitle(item.meta?.title || '').toLowerCase()
      const menuPath = (item.path || '').toLowerCase()
      const nameMatch = !searchName || menuTitle.includes(searchName)
      const routeMatch = !searchRoute || menuPath.includes(searchRoute)

      if (item.children?.length) {
        const matchedChildren = searchMenu(item.children)
        if (matchedChildren.length > 0) {
          const clonedItem = deepClone(item)
          clonedItem.children = matchedChildren
          results.push(clonedItem)
          continue
        }
      }

      if (nameMatch && routeMatch) {
        results.push(deepClone(item))
      }
    }

    return results
  }

  // 过滤后的表格数据
  const filteredTableData = computed(() => {
    const searchedData = searchMenu(tableData.value)
    return convertAuthListToChildren(searchedData)
  })

  const parentOptions = computed(() => {
    const options: Array<{ label: string; value: number }> = []
    const traverse = (nodes: AppRouteRecord[], prefix = '') => {
      nodes.forEach((node) => {
        if (node.id != null) {
          const title = formatMenuTitle(node.meta?.title || '')
          options.push({ label: `${prefix}${title}`, value: node.id })
        }
        if (node.children?.length) traverse(node.children, `${prefix}— `)
      })
    }
    traverse(tableData.value)
    return options
  })

  /**
   * 添加菜单
   */
  const handleAddMenu = (): void => {
    dialogType.value = 'MENU'
    editData.value = null
    lockMenuType.value = true
    dialogVisible.value = true
  }

  /**
   * 添加权限按钮
   */
  const handleAddAuth = (row: AppRouteRecord): void => {
    dialogType.value = 'BUTTON'
    editData.value = { parentId: row.id, meta: { title: '' } }
    lockMenuType.value = true
    dialogVisible.value = true
  }

  /**
   * 编辑菜单
   * @param row 菜单行数据
   */
  const handleEditMenu = (row: AppRouteRecord): void => {
    dialogType.value = 'MENU'
    editData.value = row
    lockMenuType.value = true
    dialogVisible.value = true
  }

  /**
   * 编辑权限按钮
   * @param row 权限行数据
   */
  const handleEditAuth = (row: AppRouteRecord): void => {
    dialogType.value = 'BUTTON'
    editData.value = row
    lockMenuType.value = true
    dialogVisible.value = true
  }

  /**
   * 菜单表单数据类型
   */
  interface MenuFormData extends Api.SystemManage.MenuSaveParams {
    id?: number
  }

  /**
   * 提交表单数据
   * @param formData 表单数据
   */
  const handleSubmit = async (formData: MenuFormData): Promise<void> => {
    const { id, ...payload } = formData
    if (id) {
      await fetchUpdateMenu(id, payload)
    } else {
      await fetchCreateMenu(payload)
    }
    dialogVisible.value = false
    await getMenuList()
  }

  /**
   * 删除菜单
   */
  const handleDeleteMenu = async (row: AppRouteRecord): Promise<void> => {
    try {
      await ElMessageBox.confirm(t('systemManage.menu.deleteMenuConfirm'), t('common.tips'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
      if (row.id == null) return
      await fetchDeleteMenu(row.id)
      await getMenuList()
    } catch (error) {
      // 接口错误已由请求层提示，这里只处理意料之外的异常，避免重复弹两条错误
      if (!isHandledRejection(error)) {
        ElMessage.error(t('systemManage.menu.deleteFailed'))
      }
    }
  }

  /**
   * 删除权限按钮
   */
  const handleDeleteAuth = async (row: AppRouteRecord): Promise<void> => {
    try {
      await ElMessageBox.confirm(t('systemManage.menu.deletePermissionConfirm'), t('common.tips'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
      if (row.id == null) return
      await fetchDeleteMenu(row.id)
      await getMenuList()
    } catch (error) {
      // 接口错误已由请求层提示，这里只处理意料之外的异常，避免重复弹两条错误
      if (!isHandledRejection(error)) {
        ElMessage.error(t('systemManage.menu.deleteFailed'))
      }
    }
  }

  /**
   * 切换展开/收起所有菜单
   */
  const toggleExpand = (): void => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      if (tableRef.value?.elTableRef && filteredTableData.value) {
        const processRows = (rows: AppRouteRecord[]) => {
          rows.forEach((row) => {
            if (row.children?.length) {
              tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
              processRows(row.children)
            }
          })
        }
        processRows(filteredTableData.value)
      }
    })
  }
</script>
