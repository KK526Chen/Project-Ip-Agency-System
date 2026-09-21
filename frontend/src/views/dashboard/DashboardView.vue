<script setup>
import { computed, onMounted, ref } from 'vue'
import { Briefcase, Bell, Calendar, Check, Wallet } from '@element-plus/icons-vue'
import { dashboard } from '../../api/admin'
import { listCases } from '../../api/case'
import { listDeadlines } from '../../api/deadline'
import { session } from '../../utils/session'
import { formatDate, text, tagType } from '../../utils/enums'
import PageHeader from '../../components/PageHeader.vue'
const data=ref({});const cases=ref([]);const deadlines=ref([]);const loading=ref(true)
const role=session.user.role;const base=`/${role.toLowerCase()}`
const metrics=computed(()=>[
  {label:'案件总数',value:data.value.cases||0,icon:Briefcase},
  {label:'办理中案件',value:data.value.processingCases||0,icon:Check},
  {label:'未完成时限',value:data.value.pendingDeadlines||0,icon:Calendar},
  {label:'临近时限',value:data.value.upcomingDeadlines||0,icon:Calendar},
  {label:role==='CLIENT'?'待处理账单':'未读消息',value:role==='CLIENT'?(data.value.pendingBills||0):(data.value.unreadNotifications||0),icon:role==='CLIENT'?Wallet:Bell},
])
onMounted(async()=>{try{const [d,c,t]=await Promise.all([dashboard(role),listCases(`${base}/cases`,{pageSize:5}),listDeadlines({pageSize:5,upcoming:true})]);data.value=d.data;cases.value=c.data.list;deadlines.value=t.data.list}finally{loading.value=false}})
</script>
<template><div v-loading="loading" class="page"><PageHeader title="工作台" :description="`${session.user.realName}，欢迎回来。这里是你的业务概览。`"/><section class="metric-grid"><article v-for="m in metrics" :key="m.label" class="metric-card"><div class="metric-top"><span>{{m.label}}</span><span class="metric-icon"><el-icon><component :is="m.icon"/></el-icon></span></div><strong class="metric-value">{{m.value}}</strong></article></section><div class="dashboard-grid"><section class="section-panel"><div class="section-title"><h2>近期案件</h2><router-link :to="`${base}/cases`">查看全部</router-link></div><el-table :data="cases"><el-table-column prop="caseName" label="案件名称" min-width="200"/><el-table-column prop="currentStage" label="当前阶段" width="130"/><el-table-column label="状态" width="110"><template #default="{row}"><el-tag :type="tagType(row.status)">{{text('caseStatus',row.status)}}</el-tag></template></el-table-column><el-table-column width="70"><template #default="{row}"><router-link :to="`${base}/cases/${row.id}`">详情</router-link></template></el-table-column></el-table></section><section class="section-panel"><div class="section-title"><h2>临近时限</h2><router-link v-if="role!=='CLIENT'" :to="`${base}/deadlines`">查看全部</router-link></div><ul class="deadline-list"><li v-for="d in deadlines" :key="d.id" class="deadline-item"><div class="date-box"><strong>{{String(d.officialDeadline).slice(8,10)}}</strong><span>{{String(d.officialDeadline).slice(5,7)}} 月</span></div><div class="deadline-copy"><strong>{{d.taskName}}</strong><span>{{formatDate(d.officialDeadline)}} · {{text('priority',d.priority)}}</span></div></li><li v-if="!deadlines.length"><el-empty description="暂无临近时限" :image-size="70"/></li></ul></section></div></div></template>
