<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { caseChildren, createCase, getCase, updateCase } from '../../api/case'
import { publicList } from '../../api/public'
import { options } from '../../utils/enums'
import { messageOf } from '../../utils/request'
import PageHeader from '../../components/PageHeader.vue'
const route=useRoute();const router=useRouter();const saving=ref(false);const loading=ref(false);const products=ref([]);const caseId=computed(()=>route.params.id?Number(route.params.id):null);const form=reactive({caseName:'',caseType:'INVENTION_PATENT',serviceProductId:null,technicalField:'',priorityLevel:'MEDIUM',confidentialReview:0,description:'',parties:[{partyType:'APPLICANT',name:'',isPrimary:1}],priorities:[]})
const addParty=()=>form.parties.push({partyType:'INVENTOR',name:'',isPrimary:0});const addPriority=()=>form.priorities.push({country:'CN',priorityNo:'',priorityDate:''})
const save=async()=>{saving.value=true;try{const payload={caseName:form.caseName,caseType:form.caseType,serviceProductId:form.serviceProductId||null,technicalField:form.technicalField,priorityLevel:form.priorityLevel,confidentialReview:form.confidentialReview,description:form.description,parties:form.parties.map(({partyType,name,idNo,address,nationality,isPrimary,remark})=>({partyType,name,idNo,address,nationality,isPrimary,remark})),priorities:form.priorities.map(({country,priorityNo,priorityDate})=>({country,priorityNo,priorityDate}))};const r=caseId.value?await updateCase(caseId.value,payload):await createCase(payload);ElMessage.success(caseId.value?'委托资料已更新':'委托草稿已创建');router.replace(`/client/cases/${caseId.value||r.data.id}`)}catch(e){ElMessage.error(messageOf(e))}finally{saving.value=false}}
onMounted(async()=>{
  try { products.value = (await publicList('services', { pageSize: 100 })).data.list || [] } catch { products.value = [] }
  if(!caseId.value){
    if(form.serviceProductId && !products.value.some(p=>p.id===form.serviceProductId)) form.serviceProductId=null
    return
  }
  loading.value=true
  try{
    const [detail,parties,priorities]=await Promise.all([getCase('/client/cases',caseId.value),caseChildren(caseId.value,'parties',{pageSize:100}),caseChildren(caseId.value,'priorities',{pageSize:100})])
    Object.assign(form,detail.data,{parties:parties.data.list?.length?parties.data.list:[{partyType:'APPLICANT',name:'',isPrimary:1}],priorities:priorities.data.list||[]})
    if(form.serviceProductId && !products.value.some(p=>p.id===form.serviceProductId)) form.serviceProductId=null
  }catch(e){ElMessage.error(messageOf(e));router.replace('/client/cases')}finally{loading.value=false}
})
</script>
<template><div class="page narrow-page"><PageHeader title="提交案件委托" description="先保存委托资料，确认材料后再提交审核"/><el-form class="surface business-form" :model="form" label-position="top"><h2>案件基本信息</h2><div class="form-grid"><el-form-item class="span-2" label="案件名称" required><el-input v-model="form.caseName" maxlength="300" show-word-limit/></el-form-item><el-form-item label="案件类型" required><el-select v-model="form.caseType"><el-option v-for="o in options('caseType')" :key="o.value" v-bind="o"/></el-select></el-form-item><el-form-item label="服务产品（可选）"><el-select v-model="form.serviceProductId" clearable filterable placeholder="不选也可以保存"><el-option v-for="p in products" :key="p.id" :label="`${p.id} · ${p.serviceName}`" :value="p.id"/></el-select></el-form-item><el-form-item label="技术领域"><el-input v-model="form.technicalField"/></el-form-item><el-form-item label="优先级"><el-select v-model="form.priorityLevel"><el-option v-for="o in options('priority')" :key="o.value" v-bind="o"/></el-select></el-form-item><el-form-item label="保密审查"><el-switch v-model="form.confidentialReview" :active-value="1" :inactive-value="0"/></el-form-item><el-form-item class="span-2" label="案件说明"><el-input v-model="form.description" type="textarea" :rows="4"/></el-form-item></div><div class="subform-heading"><h2>当事人</h2><el-button @click="addParty">添加当事人</el-button></div><div v-for="(p,i) in form.parties" :key="i" class="repeater-row"><el-select v-model="p.partyType"><el-option v-for="o in options('partyType')" :key="o.value" v-bind="o"/></el-select><el-input v-model="p.name" placeholder="姓名或主体名称"/><el-checkbox v-model="p.isPrimary" :true-value="1" :false-value="0">第一主体</el-checkbox><el-button v-if="form.parties.length>1" link type="danger" @click="form.parties.splice(i,1)">移除</el-button></div><div class="subform-heading"><h2>优先权（可选）</h2><el-button @click="addPriority">添加优先权</el-button></div><div v-for="(p,i) in form.priorities" :key="i" class="repeater-row"><el-input v-model="p.country" placeholder="国家/地区"/><el-input v-model="p.priorityNo" placeholder="优先权号"/><el-date-picker v-model="p.priorityDate" value-format="YYYY-MM-DD" placeholder="优先权日"/><el-button link type="danger" @click="form.priorities.splice(i,1)">移除</el-button></div><div class="form-footer"><el-button @click="router.back()">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存委托草稿</el-button></div></el-form></div></template>
