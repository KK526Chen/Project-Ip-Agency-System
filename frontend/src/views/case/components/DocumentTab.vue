<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listDocuments, uploadDocument, downloadDocument } from '../../../api/document'
import { saveDownload } from '../../../utils/download'
import { session } from '../../../utils/session'
import { options, text, formatDate, tagType } from '../../../utils/enums'
import { messageOf } from '../../../utils/request'
const props=defineProps({caseId:Number});const list=ref([]);const open=ref(false);const uploading=ref(false);const file=ref();const form=reactive({documentType:session.user.role==='CLIENT'?'SUPPLEMENT':'APPLICATION',remark:''})
const allowed=()=>session.user.role==='CLIENT'?options('documentType').filter(o=>['TECHNICAL_DISCLOSURE','TRADEMARK_IMAGE','SUPPLEMENT','OTHER'].includes(o.value)):options('documentType').filter(o=>o.value!=='OFFICIAL')
const load=async()=>list.value=(await listDocuments({caseId:props.caseId,pageSize:100})).data.list
const upload=async()=>{if(!file.value)return ElMessage.warning('请选择文件');uploading.value=true;try{const data=new FormData();data.append('file',file.value);data.append('caseId',props.caseId);data.append('documentType',form.documentType);if(form.remark)data.append('remark',form.remark);await uploadDocument(data);ElMessage.success('上传成功');open.value=false;file.value=null;load()}catch(e){ElMessage.error(messageOf(e))}finally{uploading.value=false}}
const download=async(row)=>{try{saveDownload(await downloadDocument(row.id),row.documentName)}catch(e){ElMessage.error(messageOf(e,'下载失败'))}}
onMounted(load)
</script>
<template><section class="tab-panel"><div class="tab-actions"><el-button type="primary" @click="open=true">上传文件</el-button></div><el-table :data="list"><el-table-column prop="documentName" label="文件名" min-width="210"/><el-table-column label="类型" width="130"><template #default="{row}">{{text('documentType',row.documentType)}}</template></el-table-column><el-table-column label="审核" width="110"><template #default="{row}"><el-tag :type="tagType(row.reviewStatus)">{{text('reviewStatus',row.reviewStatus)}}</el-tag></template></el-table-column><el-table-column label="上传时间" width="150"><template #default="{row}">{{formatDate(row.createTime)}}</template></el-table-column><el-table-column width="80"><template #default="{row}"><el-button link type="primary" @click="download(row)">下载</el-button></template></el-table-column></el-table><el-dialog v-model="open" title="上传业务文件" width="min(520px,94vw)"><el-form label-position="top"><el-form-item label="文件类型"><el-select v-model="form.documentType"><el-option v-for="o in allowed()" :key="o.value" v-bind="o"/></el-select></el-form-item><el-form-item label="选择文件"><input type="file" accept=".pdf,.doc,.docx,.jpg,.jpeg,.png" @change="file=$event.target.files[0]"/></el-form-item><el-form-item label="备注"><el-input v-model="form.remark"/></el-form-item></el-form><template #footer><el-button @click="open=false">取消</el-button><el-button type="primary" :loading="uploading" @click="upload">上传</el-button></template></el-dialog></section></template>
