<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as Icons from '@element-plus/icons-vue'
import { session } from '../utils/session'
const props=defineProps({ collapsed: Boolean, mobileOpen: Boolean }); defineEmits(['navigate'])
const route = useRoute(); const base = computed(() => `/${session.user?.role?.toLowerCase()}`)
const menus = {
  CLIENT: [['dashboard','工作台','DataAnalysis'],['profile','客户资料','OfficeBuilding'],['contacts','企业联系人','User'],['cases','我的案件','Briefcase'],['bills','费用账单','Wallet'],['invoices','发票记录','Tickets'],['notifications','消息中心','Bell']],
  AGENT: [['dashboard','工作台','DataAnalysis'],['profile','个人资料','User'],['cases','我的案件','Briefcase'],['deadlines','我的时限','Calendar'],['documents','业务文件','FolderOpened'],['performance','个人业绩','TrendCharts'],['notifications','消息中心','Bell']],
}
const adminGroups = [
  { key:'overview', label:'工作概览', icon:'DataAnalysis', items:[['dashboard','工作台','Monitor'],['notifications','消息中心','Bell'],['statistics','业务统计','PieChart']] },
  { key:'cases', label:'案件业务', icon:'Briefcase', items:[['case-review','案件审核','DocumentChecked'],['case-assignment','案件分配','Connection'],['cases','全部案件','Files'],['deadlines','时限管理','Calendar']] },
  { key:'documents', label:'文件管理', icon:'FolderOpened', items:[['document-review','文件审核','DocumentChecked'],['official-documents','官文录入','UploadFilled']] },
  { key:'finance', label:'财务管理', icon:'Wallet', items:[['bills','账单管理','Wallet'],['invoices','发票记录','Tickets']] },
  { key:'content', label:'门户内容', icon:'Collection', items:[['service-products','服务产品','Goods'],['success-cases','成功案例','Trophy'],['announcements','公告管理','Notification']] },
  { key:'system', label:'系统管理', icon:'Setting', items:[['users','用户管理','UserFilled'],['external-sync','外部同步','Refresh']] },
]
const currentMenus=computed(()=>session.user?.role==='ADMIN'?adminGroups.flatMap(group=>group.items):(menus[session.user?.role]||[]))
const active = computed(() => { const match = currentMenus.value.find(([path]) => route.path.startsWith(`${base.value}/${path}`)); return match ? `${base.value}/${match[0]}` : route.path })
const activeGroup=computed(()=>adminGroups.find(group=>group.items.some(([path])=>route.path.startsWith(`${base.value}/${path}`)))?.key)
const menuRef=ref()
watch([activeGroup,()=>props.collapsed,()=>props.mobileOpen],([group,collapsed,mobileOpen])=>{if(group&&(!collapsed||mobileOpen))nextTick(()=>menuRef.value?.open(group))},{immediate:true})
</script>
<template>
  <aside class="sidebar" :class="{ collapsed, 'mobile-open': mobileOpen }">
    <router-link class="brand" :to="`${base}/dashboard`"><span class="brand-mark">IP</span><span v-if="!collapsed||mobileOpen" class="brand-copy"><strong>知产云策</strong><small>事务管理系统</small></span></router-link>
    <el-menu ref="menuRef" :default-active="active" :default-openeds="activeGroup?[activeGroup]:[]" router unique-opened :collapse="collapsed&&!mobileOpen" @select="$emit('navigate')">
      <template v-if="session.user?.role==='ADMIN'">
        <el-sub-menu v-for="group in adminGroups" :key="group.key" :index="group.key">
          <template #title><el-icon><component :is="Icons[group.icon]" /></el-icon><span>{{group.label}}</span></template>
          <el-menu-item v-for="([path,label,icon]) in group.items" :key="path" :index="`${base}/${path}`"><el-icon><component :is="Icons[icon]" /></el-icon><template #title>{{label}}</template></el-menu-item>
        </el-sub-menu>
      </template>
      <template v-else>
        <el-menu-item v-for="([path,label,icon]) in menus[session.user?.role]" :key="path" :index="`${base}/${path}`"><el-icon><component :is="Icons[icon]" /></el-icon><template #title>{{ label }}</template></el-menu-item>
      </template>
    </el-menu>
  </aside>
</template>
