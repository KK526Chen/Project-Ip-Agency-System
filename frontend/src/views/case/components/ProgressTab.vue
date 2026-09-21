<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createStage, caseChildren } from '../../../api/case'
import { session } from '../../../utils/session'
import { options, text, formatDate } from '../../../utils/enums'
import { ElMessage } from 'element-plus'
const props=defineProps({caseId:Number});const list=ref([]);const open=ref(false);const form=reactive({stageType:'OTHER',stageName:'',status:'IN_PROGRESS',description:''})
const load=async()=>list.value=(await caseChildren(props.caseId,'stages',{pageSize:100})).data.list
const save=async()=>{await createStage(props.caseId,form);ElMessage.success('阶段已添加');open.value=false;load()};onMounted(load)
</script>
<template><section class="tab-panel"><div class="tab-actions"><el-button v-if="session.user.role!=='CLIENT'" type="primary" @click="open=true">新增阶段</el-button></div><el-timeline><el-timeline-item v-for="s in list" :key="s.id" :timestamp="formatDate(s.startTime)" placement="top" :type="s.status==='COMPLETED'?'success':'primary'"><article class="timeline-card"><div><b>{{s.stageName}}</b><el-tag size="small">{{text('stageStatus',s.status)}}</el-tag></div><p>{{s.description||text('stageType',s.stageType)}}</p></article></el-timeline-item></el-timeline><el-empty v-if="!list.length" description="暂无阶段记录"/><el-dialog v-model="open" title="新增案件阶段" width="min(520px,94vw)"><el-form label-position="top"><el-form-item label="阶段类型"><el-select v-model="form.stageType"><el-option v-for="o in options('stageType')" :key="o.value" v-bind="o"/></el-select></el-form-item><el-form-item label="阶段名称"><el-input v-model="form.stageName"/></el-form-item><el-form-item label="说明"><el-input v-model="form.description" type="textarea"/></el-form-item></el-form><template #footer><el-button @click="open=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog></section></template>
