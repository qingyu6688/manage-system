<template>
  <div class="art-card h-128 p-5 mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>{{ $t('dashboard.activity.title') }}</h4>
        <p>{{ $t('dashboard.activity.newCount') }}<span class="text-success">+6</span></p>
      </div>
    </div>

    <div class="h-9/10 mt-2 overflow-hidden">
      <ElScrollbar>
        <div
          class="h-17.5 leading-17.5 border-b border-g-300 text-sm overflow-hidden last:border-b-0"
          v-for="(item, index) in list"
          :key="index"
        >
          <span class="text-g-800 font-medium">{{ $t(item.usernameKey) }}</span>
          <span class="mx-2 text-g-600">{{ $t(item.actionKey) }}</span>
          <span class="text-theme">{{ $t(item.targetKey) }}</span>
        </div>
      </ElScrollbar>
    </div>
  </div>
</template>

<script setup lang="ts">
  interface DynamicItem {
    usernameKey: string
    actionKey: string
    targetKey: string
  }

  /**
   * 用户动态列表
   * 记录用户的关注、发文、提问、兑换等各类活动
   */
  const list = reactive<DynamicItem[]>([
    {
      usernameKey: 'dashboard.people.xiaoyu',
      actionKey: 'dashboard.activity.actions.followed',
      targetKey: 'dashboard.people.suisuinian'
    },
    {
      usernameKey: 'dashboard.people.xiaohe',
      actionKey: 'dashboard.activity.actions.published',
      targetKey: 'dashboard.activity.targets.article'
    },
    {
      usernameKey: 'dashboard.people.xiaoyu',
      actionKey: 'dashboard.activity.actions.followed',
      targetKey: 'dashboard.people.suisuinian'
    },
    {
      usernameKey: 'dashboard.people.xiaohe',
      actionKey: 'dashboard.activity.actions.published',
      targetKey: 'dashboard.activity.targets.article'
    },
    {
      usernameKey: 'dashboard.people.suisuinian',
      actionKey: 'dashboard.activity.actions.asked',
      targetKey: 'dashboard.activity.targets.themeQuestion'
    },
    {
      usernameKey: 'dashboard.people.fadaicao',
      actionKey: 'dashboard.activity.actions.redeemed',
      targetKey: 'dashboard.activity.targets.uniqueLife'
    },
    {
      usernameKey: 'dashboard.people.tiantong',
      actionKey: 'dashboard.activity.actions.closedQuestion',
      targetKey: 'dashboard.people.fadaicao'
    },
    {
      usernameKey: 'dashboard.people.lengyuedaidai',
      actionKey: 'dashboard.activity.actions.redeemed',
      targetKey: 'dashboard.activity.targets.sevenHabits'
    }
  ])
</script>
