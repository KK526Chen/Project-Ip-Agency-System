<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { DataAnalysis, User, Briefcase, List, Calendar, FolderOpened, Wallet, UserFilled, Setting } from '@element-plus/icons-vue'

defineProps({ collapsed: Boolean, mobileOpen: Boolean })
defineEmits(['navigate'])
const route = useRoute()
const currentUser = JSON.parse(sessionStorage.getItem('user') || '{}')
const active = computed(() => route.path.startsWith('/cases/') ? '/cases' : route.path)
</script>

<template>
  <aside class="sidebar" :class="{ collapsed, 'mobile-open': mobileOpen }">
    <div class="brand">
      <div class="brand-mark">IP</div>
      <div v-if="!collapsed" class="brand-copy"><strong>知产事务</strong><span>管理系统</span></div>
    </div>
    <el-menu :default-active="active" router :collapse="collapsed" @select="$emit('navigate')">
      <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><template #title>首页总览</template></el-menu-item>
      <div class="menu-label">客户与案件</div>
      <el-menu-item index="/clients"><el-icon><User /></el-icon><template #title>客户管理</template></el-menu-item>
      <el-menu-item index="/cases"><el-icon><Briefcase /></el-icon><template #title>案件管理</template></el-menu-item>
      <div class="menu-label">案件执行</div>
      <el-menu-item index="/tasks"><el-icon><List /></el-icon><template #title>任务管理</template></el-menu-item>
      <el-menu-item index="/deadlines"><el-icon><Calendar /></el-icon><template #title>期限管理</template></el-menu-item>
      <el-menu-item index="/documents"><el-icon><FolderOpened /></el-icon><template #title>文档管理</template></el-menu-item>
      <div class="menu-label">财务管理</div>
      <el-menu-item index="/fees"><el-icon><Wallet /></el-icon><template #title>费用管理</template></el-menu-item>
      <div class="menu-label">系统管理</div>
      <el-menu-item v-if="currentUser.role === 'ADMIN'" index="/users"><el-icon><UserFilled /></el-icon><template #title>用户管理</template></el-menu-item>
      <el-menu-item index="/profile"><el-icon><Setting /></el-icon><template #title>个人信息</template></el-menu-item>
    </el-menu>
  </aside>
</template>
