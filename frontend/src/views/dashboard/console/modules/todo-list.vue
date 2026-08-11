<template>
  <div class="art-card h-128 p-5 mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>{{ $t('dashboard.todo.title') }}</h4>
        <p>{{ $t('dashboard.todo.pending') }}<span class="text-danger">&nbsp;3</span></p>
      </div>
    </div>

    <div class="h-[calc(100%-40px)] overflow-auto">
      <ElScrollbar>
        <div
          class="flex-cb h-17.5 border-b border-g-300 text-sm last:border-b-0"
          v-for="(item, index) in list"
          :key="index"
        >
          <div>
            <p class="text-sm">{{ $t(item.titleKey) }}</p>
            <p class="text-g-500 mt-1">{{ $t(item.timeKey) }}</p>
          </div>
          <ElCheckbox v-model="item.complate" />
        </div>
      </ElScrollbar>
    </div>
  </div>
</template>

<script setup lang="ts">
  interface TodoItem {
    titleKey: string
    timeKey: string
    complate: boolean
  }

  /**
   * 待办事项列表
   * 记录每日工作任务及完成状态
   */
  const list = reactive<TodoItem[]>([
    {
      titleKey: 'dashboard.todo.items.reviewWork',
      timeKey: 'dashboard.todo.times.morning0930',
      complate: true
    },
    {
      titleKey: 'dashboard.todo.items.replyEmail',
      timeKey: 'dashboard.todo.times.morning1030',
      complate: true
    },
    {
      titleKey: 'dashboard.todo.items.prepareReport',
      timeKey: 'dashboard.todo.times.morning1100',
      complate: true
    },
    {
      titleKey: 'dashboard.todo.items.productMeeting',
      timeKey: 'dashboard.todo.times.afternoon0200',
      complate: false
    },
    {
      titleKey: 'dashboard.todo.items.organizeMeeting',
      timeKey: 'dashboard.todo.times.afternoon0330',
      complate: false
    },
    {
      titleKey: 'dashboard.todo.items.planTomorrow',
      timeKey: 'dashboard.todo.times.afternoon0630',
      complate: false
    }
  ])
</script>
