<script setup>
import { onMounted, ref } from 'vue'
import { ArrowRight, Medal, Files, Timer } from '@element-plus/icons-vue'
import { publicList } from '../../api/public'
import { text } from '../../utils/enums'
const services = ref([]); const announcements = ref([])
onMounted(async () => { const [s,a] = await Promise.all([publicList('services',{pageSize:3}),publicList('announcements',{pageSize:3})]); services.value=s.data.list; announcements.value=a.data.list })
</script>
<template>
  <section class="hero"><div class="hero-copy"><span class="eyebrow">PROTECT IDEAS · CREATE VALUE</span><h1>专业守护创新价值<br><em>连接创意与未来</em></h1><p>为企业、高校与创新者提供专利、商标、著作权的一站式知识产权服务。</p><div class="hero-actions"><router-link class="el-button el-button--primary" to="/services">探索专业服务</router-link><router-link class="text-link" to="/success-cases">查看成功案例 <el-icon><ArrowRight /></el-icon></router-link></div></div><div class="hero-art"><div class="orbit orbit-one"></div><div class="orbit orbit-two"></div><div class="hero-seal">IP<small>INTELLECTUAL<br>PROPERTY</small></div></div></section>
  <section class="public-section"><div class="section-intro"><span class="eyebrow">OUR SERVICES</span><h2>覆盖创新全周期的专业服务</h2><router-link to="/services">查看全部 <el-icon><ArrowRight /></el-icon></router-link></div><div class="service-grid"><router-link v-for="(item,index) in services" :key="item.id" :to="`/services/${item.id}`" class="service-card"><span class="service-index">0{{ index+1 }}</span><h3>{{ item.serviceName }}</h3><p>{{ item.description }}</p><small>{{ text('serviceType',item.serviceType) }} · {{ item.estimatedCycle || '周期面议' }}</small></router-link></div></section>
  <section class="trust-strip"><div><el-icon><Medal /></el-icon><b>专业团队</b><span>细分领域代理经验</span></div><div><el-icon><Files /></el-icon><b>全程留痕</b><span>业务节点清晰可查</span></div><div><el-icon><Timer /></el-icon><b>时限守护</b><span>关键期限主动提醒</span></div></section>
  <section class="public-section announcements-preview"><div class="section-intro"><div><span class="eyebrow">LATEST NEWS</span><h2>事务所动态</h2></div><router-link to="/announcements">全部公告 <el-icon><ArrowRight /></el-icon></router-link></div><router-link v-for="item in announcements" :key="item.id" :to="`/announcements/${item.id}`" class="news-row"><time>{{ String(item.publishTime).slice(0,10) }}</time><b>{{ item.title }}</b><span>{{ item.content }}</span><el-icon><ArrowRight /></el-icon></router-link></section>
</template>
