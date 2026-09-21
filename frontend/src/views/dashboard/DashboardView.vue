<script setup>
import { Briefcase, User, List, Calendar, TrendCharts, ArrowRight } from '@element-plus/icons-vue'
import { tasks, deadlines } from '../../utils/mockData'
import { labels, tagType } from '../../utils/enums'

const metrics = [
  { label: '客户总数', value: 36, icon: User },
  { label: '案件总数', value: 58, icon: Briefcase },
  { label: '办理中案件', value: 21, icon: TrendCharts },
  { label: '待办任务', value: 12, icon: List },
  { label: '近期到期', value: 4, icon: Calendar },
]
</script>

<template>
  <div class="page">
    <div class="page-heading"><div><h1>首页总览</h1><p>今日业务概况与近期工作安排</p></div><span class="muted">2026 年 9 月 20 日</span></div>
    <section class="metric-grid">
      <article v-for="item in metrics" :key="item.label" class="metric-card">
        <div class="metric-top"><span>{{ item.label }}</span><span class="metric-icon"><el-icon><component :is="item.icon" /></el-icon></span></div>
        <div class="metric-value">{{ item.value }}</div>
      </article>
    </section>
    <div class="dashboard-grid">
      <section class="section-panel">
        <div class="section-title"><h2>我的任务</h2><el-button text type="primary" :icon="ArrowRight" @click="$router.push('/tasks')">查看全部</el-button></div>
        <el-table :data="tasks" stripe>
          <el-table-column prop="title" label="任务" min-width="180" />
          <el-table-column prop="caseName" label="关联案件" min-width="210" show-overflow-tooltip />
          <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="tagType(row.status)" effect="plain">{{ labels.taskStatus[row.status] }}</el-tag></template></el-table-column>
          <el-table-column prop="dueDate" label="截止日期" width="110" />
        </el-table>
      </section>
      <section class="section-panel">
        <div class="section-title"><h2>即将到期</h2><el-button text type="primary" :icon="ArrowRight" @click="$router.push('/deadlines')">期限管理</el-button></div>
        <ul class="deadline-list">
          <li v-for="item in deadlines" :key="item.id" class="deadline-item">
            <div class="date-box"><strong>{{ item.deadlineDate.slice(8) }}</strong><span>{{ item.deadlineDate.slice(5, 7) }} 月</span></div>
            <div class="deadline-copy"><strong>{{ item.deadlineName }}</strong><span>{{ item.caseName }}</span></div>
          </li>
        </ul>
      </section>
    </div>
  </div>
</template>
