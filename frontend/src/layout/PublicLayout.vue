<script setup>
import { useRoute } from 'vue-router'
import { session, homeFor } from '../utils/session'
const route = useRoute()
</script>
<template>
  <div class="public-shell">
    <header class="public-header">
      <router-link class="public-brand" to="/"><span class="brand-seal">IP</span><span><b>知产云策</b><small>知识产权代理事务所</small></span></router-link>
      <nav>
        <router-link to="/services">专业服务</router-link><router-link to="/success-cases">成功案例</router-link><router-link to="/announcements">事务公告</router-link>
      </nav>
      <router-link class="header-action" :to="session.token ? homeFor() : '/login'">{{ session.token ? '进入工作台' : '客户登录' }}</router-link>
    </header>
    <main :class="{ 'public-home': route.path === '/' }"><router-view /></main>
    <footer class="public-footer"><span>© 2026 知产云策</span><span>专业 · 严谨 · 可追溯</span></footer>
  </div>
</template>
