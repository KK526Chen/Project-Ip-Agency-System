<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Bell, Fold, Menu, SwitchButton } from '@element-plus/icons-vue'
import { unreadCount } from '../api/notification'
import { clearSession, session } from '../utils/session'
import { text } from '../utils/enums'
defineEmits(['toggle-sidebar', 'toggle-mobile'])
const route = useRoute(); const router = useRouter(); const unread = ref(0); let timer
const notificationPath = computed(() => `/${session.user?.role?.toLowerCase()}/notifications`)
const profilePath = computed(() => session.user?.role === 'ADMIN' ? '/admin/users' : `/${session.user?.role?.toLowerCase()}/profile`)
const refresh = async () => { try { unread.value = (await unreadCount()).data || 0 } catch { unread.value = 0 } }
const logout = () => { clearSession(); router.replace('/login') }
onMounted(() => { refresh(); timer = setInterval(refresh, 60000) }); onBeforeUnmount(() => clearInterval(timer))
</script>
<template>
  <header class="topbar">
    <div class="topbar-left">
      <el-button class="desktop-toggle" text :icon="Fold" aria-label="收起侧栏" @click="$emit('toggle-sidebar')" />
      <el-button class="mobile-toggle" text :icon="Menu" aria-label="打开导航" @click="$emit('toggle-mobile')" />
      <span class="breadcrumb-title">{{ route.meta.title }}</span>
    </div>
    <div class="topbar-actions">
      <el-badge :value="unread" :hidden="!unread" :max="99"><el-button circle plain :icon="Bell" aria-label="消息中心" @click="router.push(notificationPath)" /></el-badge>
      <el-dropdown trigger="click">
        <button class="user-menu"><span class="avatar">{{ (session.user?.realName || '用').slice(0, 1) }}</span><span class="user-copy"><strong>{{ session.user?.realName }}</strong><small>{{ text('role', session.user?.role) }}</small></span><el-icon><ArrowDown /></el-icon></button>
        <template #dropdown><el-dropdown-menu><el-dropdown-item @click="router.push(profilePath)">{{ session.user?.role === 'ADMIN' ? '用户管理' : '个人资料' }}</el-dropdown-item><el-dropdown-item divided :icon="SwitchButton" @click="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
      </el-dropdown>
    </div>
  </header>
</template>
