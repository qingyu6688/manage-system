<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleCancel"
    width="760px"
    align-center
    class="menu-dialog"
    @closed="handleClosed"
  >
    <ArtForm
      ref="formRef"
      v-model="form"
      :items="formItems"
      :rules="rules"
      :span="width > 640 ? 12 : 24"
      :gutter="20"
      label-width="100px"
      :show-reset="false"
      :show-submit="false"
    >
      <template #menuType>
        <ElRadioGroup v-model="form.menuType" :disabled="disableMenuType">
          <ElRadioButton value="MENU">{{ $t('systemManage.menu.types.menu') }}</ElRadioButton>
          <ElRadioButton value="BUTTON">{{ $t('systemManage.menu.types.button') }}</ElRadioButton>
        </ElRadioGroup>
      </template>
    </ArtForm>

    <template #footer>
      <ElButton @click="handleCancel">{{ $t('common.cancel') }}</ElButton>
      <ElButton type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormRules } from 'element-plus'
  import type { AppRouteRecord } from '@/types/router'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import { useWindowSize } from '@vueuse/core'
  import { useI18n } from 'vue-i18n'

  interface ParentOption {
    label: string
    value: number
  }

  interface MenuDialogForm extends Api.SystemManage.MenuSaveParams {
    id?: number
  }

  interface Props {
    visible: boolean
    editData?: AppRouteRecord & {
      parentId?: number
      meta: AppRouteRecord['meta'] & { authMark?: string }
    }
    type?: 'MENU' | 'BUTTON'
    lockType?: boolean
    parentOptions?: ParentOption[]
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', data: MenuDialogForm): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    type: 'MENU',
    lockType: false,
    parentOptions: () => []
  })
  const emit = defineEmits<Emits>()
  const { t } = useI18n()
  const { width } = useWindowSize()
  const formRef = ref()
  const isEdit = ref(false)

  const createInitialForm = (): MenuDialogForm => ({
    parentId: 0,
    menuType: 'MENU',
    title: '',
    routeName: '',
    path: '',
    component: '',
    permission: '',
    icon: '',
    sort: 10,
    enabled: true,
    hidden: false,
    keepAlive: true,
    hiddenTab: false,
    iframe: false,
    externalLink: '',
    badge: false,
    textBadge: '',
    fixedTab: false,
    activePath: '',
    fullPage: false
  })

  const form = reactive<MenuDialogForm>(createInitialForm())

  const rules = computed<FormRules>(() => ({
    title: [
      { required: true, message: t('systemManage.menu.dialog.validation.name'), trigger: 'blur' }
    ],
    parentId:
      form.menuType === 'BUTTON'
        ? [
            {
              required: true,
              message: t('systemManage.menu.dialog.validation.parent'),
              trigger: 'change'
            }
          ]
        : [],
    routeName:
      form.menuType === 'MENU'
        ? [
            {
              required: true,
              message: t('systemManage.menu.dialog.validation.routeName'),
              trigger: 'blur'
            }
          ]
        : [],
    path:
      form.menuType === 'MENU'
        ? [
            {
              required: true,
              message: t('systemManage.menu.dialog.validation.path'),
              trigger: 'blur'
            }
          ]
        : [],
    permission:
      form.menuType === 'BUTTON'
        ? [
            {
              required: true,
              message: t('systemManage.menu.dialog.validation.permission'),
              trigger: 'blur'
            }
          ]
        : []
  }))

  const parentSelectOptions = computed(() => [
    { label: t('systemManage.menu.dialog.root'), value: 0 },
    ...props.parentOptions
  ])

  const formItems = computed<FormItem[]>(() => {
    const commonItems: FormItem[] = [
      { label: t('systemManage.menu.dialog.fields.nodeType'), key: 'menuType', span: 24 },
      {
        label: t('systemManage.menu.dialog.fields.parent'),
        key: 'parentId',
        type: 'select',
        props: {
          options: parentSelectOptions.value,
          placeholder: t('systemManage.menu.dialog.placeholders.parent'),
          filterable: true
        }
      },
      {
        label: t('systemManage.menu.dialog.fields.displayName'),
        key: 'title',
        type: 'input',
        props: { placeholder: t('systemManage.menu.dialog.placeholders.displayName') }
      },
      {
        label: t('systemManage.menu.dialog.fields.sort'),
        key: 'sort',
        type: 'number',
        props: { min: 0, controlsPosition: 'right', style: { width: '100%' } }
      }
    ]

    if (form.menuType === 'BUTTON') {
      return [
        ...commonItems,
        {
          label: t('systemManage.menu.dialog.fields.permission'),
          key: 'permission',
          type: 'input',
          props: { placeholder: t('systemManage.menu.dialog.placeholders.permission') }
        },
        {
          label: t('systemManage.menu.dialog.fields.enabled'),
          key: 'enabled',
          type: 'switch',
          span: 12
        }
      ]
    }

    const switchSpan = width.value < 640 ? 12 : 6
    return [
      ...commonItems,
      {
        label: t('systemManage.menu.dialog.fields.routeName'),
        key: 'routeName',
        type: 'input',
        // 后端权限标识由路由名称拼成，创建后不允许修改
        props: {
          placeholder: t('systemManage.menu.dialog.placeholders.routeName'),
          disabled: isEdit.value && Boolean(props.editData?.name)
        }
      },
      {
        label: t('systemManage.menu.dialog.fields.path'),
        key: 'path',
        type: 'input',
        props: { placeholder: t('systemManage.menu.dialog.placeholders.path') }
      },
      {
        label: t('systemManage.menu.dialog.fields.component'),
        key: 'component',
        type: 'input',
        props: { placeholder: t('systemManage.menu.dialog.placeholders.component') }
      },
      {
        label: t('systemManage.menu.dialog.fields.icon'),
        key: 'icon',
        type: 'input',
        props: { placeholder: t('systemManage.menu.dialog.placeholders.icon') }
      },
      {
        label: t('systemManage.menu.dialog.fields.externalLink'),
        key: 'externalLink',
        type: 'input',
        props: { placeholder: t('systemManage.menu.dialog.placeholders.externalLink') }
      },
      {
        label: t('systemManage.menu.dialog.fields.textBadge'),
        key: 'textBadge',
        type: 'input',
        props: { placeholder: t('systemManage.menu.dialog.placeholders.textBadge') }
      },
      {
        label: t('systemManage.menu.dialog.fields.activePath'),
        key: 'activePath',
        type: 'input',
        props: { placeholder: t('systemManage.menu.dialog.placeholders.activePath') }
      },
      {
        label: t('systemManage.menu.dialog.fields.enabled'),
        key: 'enabled',
        type: 'switch',
        span: switchSpan
      },
      {
        label: t('systemManage.menu.dialog.fields.keepAlive'),
        key: 'keepAlive',
        type: 'switch',
        span: switchSpan
      },
      {
        label: t('systemManage.menu.dialog.fields.hidden'),
        key: 'hidden',
        type: 'switch',
        span: switchSpan
      },
      {
        label: t('systemManage.menu.dialog.fields.hiddenTab'),
        key: 'hiddenTab',
        type: 'switch',
        span: switchSpan
      },
      {
        label: t('systemManage.menu.dialog.fields.iframe'),
        key: 'iframe',
        type: 'switch',
        span: switchSpan
      },
      {
        label: t('systemManage.menu.dialog.fields.badge'),
        key: 'badge',
        type: 'switch',
        span: switchSpan
      },
      {
        label: t('systemManage.menu.dialog.fields.fixedTab'),
        key: 'fixedTab',
        type: 'switch',
        span: switchSpan
      },
      {
        label: t('systemManage.menu.dialog.fields.fullPage'),
        key: 'fullPage',
        type: 'switch',
        span: switchSpan
      }
    ]
  })

  const dialogTitle = computed(() => {
    if (form.menuType === 'MENU') {
      return t(
        isEdit.value ? 'systemManage.menu.dialog.editMenu' : 'systemManage.menu.dialog.addMenu'
      )
    }
    return t(
      isEdit.value ? 'systemManage.menu.dialog.editButton' : 'systemManage.menu.dialog.addButton'
    )
  })

  const disableMenuType = computed(() => isEdit.value || props.lockType)

  const loadFormData = () => {
    const row = props.editData
    if (!row) return
    isEdit.value = Boolean(row.id)

    if (props.type === 'BUTTON') {
      Object.assign(form, createInitialForm(), {
        id: row.id,
        parentId: row.parentId || 0,
        menuType: 'BUTTON',
        title: row.meta?.title || '',
        permission: row.meta?.authMark || '',
        sort: Number(row.meta?.sort ?? 10),
        enabled: row.meta?.isEnable ?? true
      })
      return
    }

    Object.assign(form, createInitialForm(), {
      id: row.id,
      parentId: row.parentId || 0,
      menuType: 'MENU',
      title: row.meta?.title || '',
      routeName: String(row.name || ''),
      path: row.path || '',
      component: typeof row.component === 'string' ? row.component : '',
      icon: row.meta?.icon || '',
      sort: Number(row.meta?.sort ?? 10),
      enabled: row.meta?.isEnable ?? true,
      hidden: row.meta?.isHide ?? false,
      keepAlive: row.meta?.keepAlive ?? false,
      hiddenTab: row.meta?.isHideTab ?? false,
      iframe: row.meta?.isIframe ?? false,
      externalLink: row.meta?.link || '',
      badge: row.meta?.showBadge ?? false,
      textBadge: row.meta?.showTextBadge || '',
      fixedTab: row.meta?.fixedTab ?? false,
      activePath: row.meta?.activePath || '',
      fullPage: row.meta?.isFullPage ?? false
    })
  }

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate()
    emit('submit', { ...form })
  }

  const handleCancel = () => emit('update:visible', false)

  const handleClosed = () => {
    Object.assign(form, createInitialForm())
    delete form.id
    isEdit.value = false
    formRef.value?.reset()
  }

  watch(
    () => props.visible,
    (visible) => {
      if (!visible) return
      Object.assign(form, createInitialForm(), { menuType: props.type })
      nextTick(loadFormData)
    }
  )
</script>
