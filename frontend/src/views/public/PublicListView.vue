<script setup>
import { computed, reactive, watch } from 'vue'
import { useRoute } from 'vue-router'
import { publicList } from '../../api/public'
import { options, text, formatDate } from '../../utils/enums'
import AppPagination from '../../components/AppPagination.vue'
const route=useRoute(); const state=reactive({loading:false,list:[],total:0,pageNum:1,pageSize:9,keyword:'',category:''})
const resource=computed(()=>route.meta.resource)
const config=computed(()=>({services:{title:'专业服务',desc:'从确权到保护，构建完整的知识产权服务体系',field:'serviceType',group:'serviceType'},'success-cases':{title:'成功案例',desc:'以专业判断回应每一次创新委托',field:'serviceType',group:'serviceType'},announcements:{title:'事务公告',desc:'了解最新政策、业务与系统动态',field:'announcementType',group:'announcementType'}}[resource.value]))
const load=async()=>{state.loading=true;try{const params={pageNum:state.pageNum,pageSize:state.pageSize,keyword:state.keyword||undefined,category:state.category||undefined};const r=await publicList(resource.value,params);Object.assign(state,{list:r.data.list,total:r.data.total})}finally{state.loading=false}}
watch(resource,()=>{state.pageNum=1;state.category='';load()},{immediate:true})
</script>
<template><div class="public-page"><section class="public-banner"><span class="eyebrow">IP AGENCY</span><h1>{{ config.title }}</h1><p>{{ config.desc }}</p></section><section class="public-section"><div class="public-filters"><el-input v-model="state.keyword" placeholder="搜索关键词" clearable @keyup.enter="state.pageNum=1;load()"/><el-select v-model="state.category" placeholder="全部分类" clearable @change="state.pageNum=1;load()"><el-option v-for="o in options(config.group)" :key="o.value" v-bind="o"/></el-select><el-button type="primary" @click="state.pageNum=1;load()">查询</el-button></div><div v-loading="state.loading" class="content-grid"><router-link v-for="item in state.list" :key="item.id" :to="`/${resource}/${item.id}`" class="content-card"><span class="card-category">{{ text(config.group,item[config.field]) }}</span><h2>{{ item.serviceName || item.caseName || item.title }}</h2><p>{{ item.description || item.content || item.highlights || '查看详情' }}</p><div class="card-meta"><span v-if="item.agencyFee">代理费 ¥{{ item.agencyFee }}</span><span v-if="item.clientIndustry">{{ item.clientIndustry }}</span><time v-if="item.publishTime">{{ formatDate(item.publishTime) }}</time><b>查看详情 →</b></div></router-link></div><AppPagination v-model:page="state.pageNum" v-model:size="state.pageSize" :total="state.total" @change="load"/></section></div></template>
