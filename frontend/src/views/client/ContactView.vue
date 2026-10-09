<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createContact, deleteContact, listContacts, updateContact } from '../../api/client'
import { messageOf } from '../../utils/request'
import PageHeader from '../../components/PageHeader.vue'; import AppPagination from '../../components/AppPagination.vue'
const state=reactive({list:[],total:0,pageNum:1,pageSize:10,loading:false,error:''});const open=ref(false);const saving=ref(false);const deletingId=ref(null);const formRef=ref(null);const form=reactive({id:null,name:'',position:'',phone:'',email:'',permissionScope:'ALL_CASES',remark:''})
const scopeLabels={ALL_CASES:'全部案件',SPECIFIED_CASES:'指定案件',FEE_ONLY:'仅财务'}
const rules={
  name:[{required:true,whitespace:true,message:'请输入联系人姓名',trigger:'blur'},{max:50,message:'姓名最多 50 字',trigger:'blur'}],
  position:[{max:100,message:'职位最多 100 字',trigger:'blur'}],
  phone:[{max:20,message:'电话最多 20 字',trigger:'blur'}],
  email:[{type:'email',message:'请输入有效邮箱',trigger:'blur'},{max:100,message:'邮箱最多 100 字',trigger:'blur'}],
  permissionScope:[{required:true,message:'请选择权限范围',trigger:'change'}],
  remark:[{max:500,message:'备注最多 500 字',trigger:'blur'}],
}
const load=async()=>{state.loading=true;state.error='';try{const r=await listContacts({pageNum:state.pageNum,pageSize:state.pageSize});state.list=r.data.list;state.total=r.data.total}catch(e){state.list=[];state.total=0;state.error=messageOf(e)}finally{state.loading=false}}
const edit=(row={})=>{if(saving.value)return;Object.assign(form,{id:null,name:'',position:'',phone:'',email:'',permissionScope:'ALL_CASES',remark:'',...row});formRef.value?.clearValidate();open.value=true}
const save=async()=>{
  if(saving.value)return;saving.value=true
  try{
    if(!await formRef.value.validate().catch(()=>false))return
    const payload=Object.fromEntries(['name','position','phone','email','permissionScope','remark'].map(key=>[key,typeof form[key]==='string'?form[key].trim()||null:form[key]]))
    form.id?await updateContact(form.id,payload):await createContact(payload)
    ElMessage.success('联系人已保存');open.value=false;await load()
  }catch(e){ElMessage.error(messageOf(e))}finally{saving.value=false}
}
const remove=async(row)=>{
  if(deletingId.value!==null)return;deletingId.value=row.id
  try{
    await ElMessageBox.confirm(`确认删除联系人“${row.name}”？`,'删除确认',{type:'warning'})
    await deleteContact(row.id);ElMessage.success('已删除')
    state.pageNum=Math.min(state.pageNum,Math.max(1,Math.ceil((state.total-1)/state.pageSize)));await load()
  }catch(e){if(e!=='cancel'&&e!=='close')ElMessage.error(messageOf(e))}finally{deletingId.value=null}
}
onMounted(load)
</script>
<template><div class="page"><PageHeader title="企业联系人" description="维护业务与财务联系人员"><el-button type="primary" :disabled="saving || deletingId!==null" @click="edit()">新增联系人</el-button></PageHeader><el-alert v-if="state.error" :title="state.error" type="error" show-icon :closable="false"/><div class="table-wrap"><el-table v-loading="state.loading" :data="state.list"><el-table-column prop="name" label="姓名"/><el-table-column prop="position" label="职位"/><el-table-column prop="phone" label="电话"/><el-table-column prop="email" label="邮箱" min-width="180"/><el-table-column label="权限范围"><template #default="{row}">{{ scopeLabels[row.permissionScope]||row.permissionScope }}</template></el-table-column><el-table-column label="操作" width="130"><template #default="{row}"><el-button link type="primary" :disabled="saving || deletingId!==null" @click="edit(row)">编辑</el-button><el-button link type="danger" :loading="deletingId===row.id" :disabled="saving || deletingId!==null" @click="remove(row)">删除</el-button></template></el-table-column></el-table></div><AppPagination v-model:page="state.pageNum" v-model:size="state.pageSize" :total="state.total" @change="load"/><el-dialog v-model="open" :title="form.id?'编辑联系人':'新增联系人'" width="min(560px,94vw)" :close-on-click-modal="!saving" :close-on-press-escape="!saving" :show-close="!saving"><el-form ref="formRef" :model="form" :rules="rules" class="form-grid" label-position="top"><el-form-item label="姓名" prop="name"><el-input v-model="form.name"/></el-form-item><el-form-item label="职位" prop="position"><el-input v-model="form.position"/></el-form-item><el-form-item label="电话" prop="phone"><el-input v-model="form.phone"/></el-form-item><el-form-item label="邮箱" prop="email"><el-input v-model="form.email"/></el-form-item><el-form-item label="权限范围" prop="permissionScope"><el-select v-model="form.permissionScope"><el-option label="全部案件" value="ALL_CASES"/><el-option label="指定案件" value="SPECIFIED_CASES"/><el-option label="仅财务" value="FEE_ONLY"/></el-select></el-form-item><el-form-item label="备注" prop="remark"><el-input v-model="form.remark"/></el-form-item></el-form><template #footer><el-button :disabled="saving" @click="open=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template></el-dialog></div></template>
