<script setup>
import { ref } from 'vue'
import HeaderBar from './HeaderBar.vue'
import Sidebar from './Sidebar.vue'

const collapsed = ref(false)
const mobileOpen = ref(false)
</script>

<template>
  <div class="app-shell">
    <div class="mobile-mask" :class="{ show: mobileOpen }" @click="mobileOpen = false" />
    <Sidebar :collapsed="collapsed" :mobile-open="mobileOpen" @navigate="mobileOpen = false" />
    <div class="workspace" :class="{ collapsed }">
      <HeaderBar @toggle-sidebar="collapsed = !collapsed" @toggle-mobile="mobileOpen = !mobileOpen" />
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>
