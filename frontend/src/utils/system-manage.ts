import i18n, { $t } from '@/locales'

const BUILT_IN_ROLE_KEY_MAP: Record<string, string> = {
  R_SUPER: 'systemManage.role.builtIn.super',
  R_ADMIN: 'systemManage.role.builtIn.admin'
}

/**
 * 翻译系统内置角色名称；用户自行创建的角色保留数据库原文。
 */
export const formatRoleName = (roleCode: string, fallbackName: string): string => {
  const baseKey = BUILT_IN_ROLE_KEY_MAP[roleCode]
  const nameKey = baseKey ? `${baseKey}.name` : ''
  return nameKey && i18n.global.te(nameKey) ? $t(nameKey) : fallbackName
}

/**
 * 翻译系统内置角色描述；用户自行创建的角色保留数据库原文。
 */
export const formatRoleDescription = (roleCode: string, fallbackDescription: string): string => {
  const baseKey = BUILT_IN_ROLE_KEY_MAP[roleCode]
  const descriptionKey = baseKey ? `${baseKey}.description` : ''
  return descriptionKey && i18n.global.te(descriptionKey) ? $t(descriptionKey) : fallbackDescription
}
