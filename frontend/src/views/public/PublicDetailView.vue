<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { publicDetail } from '../../api/public'
import { formatDate, money, text } from '../../utils/enums'
const route=useRoute();const router=useRouter();const item=ref({});const loading=ref(true)
onMounted(async()=>{try{item.value=(await publicDetail(route.meta.resource,route.params.id)).data}finally{loading.value=false}})
</script>
<template><div class="public-page"><article v-loading="loading" class="article-page"><el-button link :icon="ArrowLeft" @click="router.back()">返回列表</el-button><span class="eyebrow">IP AGENCY</span><h1>{{ item.serviceName || item.caseName || item.title }}</h1><div class="article-meta"><span v-if="item.serviceType">{{ text('serviceType',item.serviceType) }}</span><span v-if="item.publishTime">{{ formatDate(item.publishTime) }}</span><span v-if="item.clientIndustry">{{ item.clientIndustry }}</span></div><div v-if="item.description || item.content" class="article-content">{{ item.description || item.content }}</div><dl v-if="route.meta.resource==='services'" class="article-facts"><div><dt>适用对象</dt><dd>{{ item.targetType||'—' }}</dd></div><div><dt>预计周期</dt><dd>{{ item.estimatedCycle||'—' }}</dd></div><div><dt>官方费用</dt><dd>{{ money(item.officialFee) }}</dd></div><div><dt>代理费用</dt><dd>{{ money(item.agencyFee) }}</dd></div><div><dt>服务流程</dt><dd>{{ item.processDesc||'—' }}</dd></div><div><dt>所需材料</dt><dd>{{ item.requiredMaterials||'—' }}</dd></div></dl><div v-if="item.highlights" class="article-block"><h2>案件亮点</h2><p>{{ item.highlights }}</p><h2>处理结果</h2><p>{{ item.result }}</p></div></article></div></template>
