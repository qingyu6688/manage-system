<template>
  <div class="art-card p-5 h-128 overflow-hidden mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>{{ $t('dashboard.newUsers.title') }}</h4>
        <p>
          {{ $t('dashboard.newUsers.growthThisMonth') }}<span class="text-success">&nbsp;+20%</span>
        </p>
      </div>
      <ElRadioGroup v-model="radio2">
        <ElRadioButton value="currentMonth">{{
          $t('dashboard.newUsers.period.currentMonth')
        }}</ElRadioButton>
        <ElRadioButton value="lastMonth">{{
          $t('dashboard.newUsers.period.lastMonth')
        }}</ElRadioButton>
        <ElRadioButton value="currentYear">{{
          $t('dashboard.newUsers.period.currentYear')
        }}</ElRadioButton>
      </ElRadioGroup>
    </div>
    <ArtTable
      class="w-full"
      :data="tableData"
      style="width: 100%"
      size="large"
      :border="false"
      :stripe="false"
      :header-cell-style="{ background: 'transparent' }"
    >
      <template #default>
        <ElTableColumn :label="$t('dashboard.newUsers.columns.avatar')" prop="avatar" width="150px">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <img class="size-9 rounded-lg" :src="scope.row.avatar" alt="avatar" />
              <span class="ml-2">{{ $t(scope.row.usernameKey) }}</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('dashboard.newUsers.columns.region')" prop="provinceKey">
          <template #default="scope">{{ $t(scope.row.provinceKey) }}</template>
        </ElTableColumn>
        <ElTableColumn :label="$t('dashboard.newUsers.columns.gender')" prop="avatar">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <span style="margin-left: 10px">{{
                scope.row.sex === 1 ? $t('common.male') : $t('common.female')
              }}</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('dashboard.newUsers.columns.progress')" width="240">
          <template #default="scope">
            <ElProgress
              :percentage="scope.row.pro"
              :color="scope.row.color"
              :stroke-width="4"
              :aria-label="
                t('dashboard.newUsers.progressAria', {
                  name: t(scope.row.usernameKey),
                  progress: scope.row.pro
                })
              "
            />
          </template>
        </ElTableColumn>
      </template>
    </ArtTable>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import avatar1 from '@/assets/images/avatar/avatar1.webp'
  import avatar2 from '@/assets/images/avatar/avatar2.webp'
  import avatar3 from '@/assets/images/avatar/avatar3.webp'
  import avatar4 from '@/assets/images/avatar/avatar4.webp'
  import avatar5 from '@/assets/images/avatar/avatar5.webp'
  import avatar6 from '@/assets/images/avatar/avatar6.webp'

  interface UserTableItem {
    usernameKey: string
    provinceKey: string
    sex: 0 | 1
    age: number
    percentage: number
    pro: number
    color: string
    avatar: string
  }

  const ANIMATION_DELAY = 100

  const { t } = useI18n()
  const radio2 = ref('currentMonth')

  /**
   * 新用户表格数据
   * 包含用户基本信息和完成进度
   */
  const tableData = reactive<UserTableItem[]>([
    {
      usernameKey: 'dashboard.people.xiaoyu',
      provinceKey: 'dashboard.regions.beijing',
      sex: 0,
      age: 22,
      percentage: 60,
      pro: 0,
      color: 'var(--art-primary)',
      avatar: avatar1
    },
    {
      usernameKey: 'dashboard.people.xiaohe',
      provinceKey: 'dashboard.regions.shenzhen',
      sex: 1,
      age: 21,
      percentage: 20,
      pro: 0,
      color: 'var(--art-secondary)',
      avatar: avatar2
    },
    {
      usernameKey: 'dashboard.people.suisuinian',
      provinceKey: 'dashboard.regions.shanghai',
      sex: 1,
      age: 23,
      percentage: 60,
      pro: 0,
      color: 'var(--art-warning)',
      avatar: avatar3
    },
    {
      usernameKey: 'dashboard.people.fadaicao',
      provinceKey: 'dashboard.regions.changsha',
      sex: 0,
      age: 28,
      percentage: 50,
      pro: 0,
      color: 'var(--art-info)',
      avatar: avatar4
    },
    {
      usernameKey: 'dashboard.people.tiantong',
      provinceKey: 'dashboard.regions.zhejiang',
      sex: 1,
      age: 26,
      percentage: 70,
      pro: 0,
      color: 'var(--art-error)',
      avatar: avatar5
    },
    {
      usernameKey: 'dashboard.people.lengyuedaidai',
      provinceKey: 'dashboard.regions.hubei',
      sex: 1,
      age: 25,
      percentage: 90,
      pro: 0,
      color: 'var(--art-success)',
      avatar: avatar6
    }
  ])

  /**
   * 添加进度条动画效果
   * 延迟后将进度值从 0 更新到目标百分比，触发动画
   */
  const addAnimation = (): void => {
    setTimeout(() => {
      tableData.forEach((item) => {
        item.pro = item.percentage
      })
    }, ANIMATION_DELAY)
  }

  onMounted(() => {
    addAnimation()
  })
</script>

<style lang="scss" scoped>
  .art-card {
    :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
      color: var(--el-color-primary) !important;
      background: transparent !important;
    }
  }
</style>
