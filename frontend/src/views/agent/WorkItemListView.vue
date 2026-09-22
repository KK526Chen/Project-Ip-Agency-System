<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listWorkItems, startWorkItem, completeWorkItem, listTimesheets, createTimesheet } from '../../api/forkc'
import PageHeader from '../../components/PageHeader.vue'
import { ElMessage } from 'element-plus'
import { messageOf } from '../../utils/request'
const list = ref([]); const times = ref([]); const form = reactive({ workItemId: null, hours: 1 })
const load = async () => { list.value = (await listWorkItems({ pageSize: 100 })).data.list; times.value = (await listTimesheets({ pageSize: 100 })).data.list }
onMounted(load)
</script>
<template>
  <div class="page">
    <PageHeader title="我的工作项" description="执行分配给自己的办案任务" />
    <el-table :data="list"><el-table-column prop="title" label="任务" /><el-table-column prop="status" label="状态" width="120" /><el-table-column prop="caseId" label="案件" width="90" />
      <el-table-column width="180"><template #default="{row}"><el-button v-if="row.status==='TODO'" link @click="startWorkItem(row.id,{}).then(load)">开始</el-button><el-button v-if="row.status!=='DONE'" link type="success" @click="completeWorkItem(row.id).then(load)">完成</el-button></template></el-table-column>
    </el-table>
    <h3 style="margin-top:24px">工时</h3>
    <el-form inline><el-input-number v-model="form.workItemId" placeholder="工作项 ID" /><el-input-number v-model="form.hours" :min="0.5" :max="24" /><el-button type="primary" @click="createTimesheet(form).then(()=>{ElMessage.success('已登记');load()}).catch(e=>ElMessage.error(messageOf(e)))">登记</el-button></el-form>
    <el-table :data="times"><el-table-column prop="workItemId" label="工作项" /><el-table-column prop="hours" label="小时" /><el-table-column prop="workDate" label="日期" /></el-table>
  </div>
</template>
