<script setup>
import { useRoute, useRouter } from 'vue-router'
import { Menu, Fold, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import { labels } from '../utils/enums'

defineEmits(['toggle-sidebar', 'toggle-mobile'])
const route = useRoute()
const router = useRouter()
const user = JSON.parse(sessionStorage.getItem('user') || '{}')

const logout = () => {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('user')
  router.replace('/login')
}
</script>

<template>
  <header class="topbar">
    <div class="topbar-left">
      <el-button class="desktop-toggle" text :icon="Fold" aria-label="收起侧栏" @click="$emit('toggle-sidebar')" />
      <el-button class="mobile-toggle" text :icon="Menu" aria-label="打开导航" @click="$emit('toggle-mobile')" />
      <span class="breadcrumb-title">{{ route.meta.title }}</span>
    </div>
    <el-dropdown trigger="click">
      <button class="user-menu">
        <span class="avatar">{{ (user.realName || '用').slice(0, 1) }}</span>
        <span class="user-copy"><strong>{{ user.realName || '当前用户' }}</strong><small>{{ labels.role[user.role] || user.role }}</small></span>
        <el-icon><ArrowDown /></el-icon>
      </button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item @click="router.push('/profile')">个人信息</el-dropdown-item>
          <el-dropdown-item divided :icon="SwitchButton" @click="logout">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </header>
</template>
