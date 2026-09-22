<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listWorkItems, createWorkItem, startWorkItem, completeWorkItem, addDependency, createTimesheet } from '../../../api/forkc'
import { session } from '../../../utils/session'
import { ElMessage } from 'element-plus'
import { messageOf } from '../../../utils/request'
import { formatDate, tagType } from '../../../utils/enums'
const props = defineProps({ caseId: Number }); const list = ref([]); const open = ref(false)
const form = reactive({ title: '', workType: 'DRAFT', description: '' })
const load = async () => { list.value = (await listWorkItems({ caseId: props.caseId, pageSize: 100 })).data.list }
const save = async () => { try { await createWorkItem({ caseId: props.caseId, ...form }); open.value = false; load(); ElMessage.success('任务已创建') } catch (e) { ElMessage.error(messageOf(e)) } }
const start = async (row) => { try { await startWorkItem(row.id, {}); load(); ElMessage.success('已开始') } catch (e) { ElMessage.error(messageOf(e)) } }
const done = async (row) => { try { await completeWorkItem(row.id); load(); ElMessage.success('已完成') } catch (e) { ElMessage.error(messageOf(e)) } }
const dep = async (row) => { const pred = Number(prompt('前置任务 ID')); if (!pred) return; try { await addDependency(row.id, { predecessorId: pred }); ElMessage.success('依赖已添加') } catch (e) { ElMessage.error(messageOf(e)) } }
const hours = async (row) => { const h = Number(prompt('登记工时（小时）')); if (!h) return; try { await createTimesheet({ workItemId: row.id, hours: h }); ElMessage.success('工时已登记') } catch (e) { ElMessage.error(messageOf(e)) } }
onMounted(load)
</script>
<template>
  <section class="tab-panel">
    <div class="tab-actions"><el-button v-if="session.user.role!=='CLIENT'" type="primary" @click="open=true">新建工作项</el-button></div>
    <el-table :data="list">
      <el-table-column prop="title" label="任务" min-width="180" /><el-table-column prop="workType" label="类型" width="120" />
      <el-table-column prop="status" label="状态" width="110"><template #default="{row}"><el-tag :type="tagType(row.status)">{{ row.status }}</el-tag></template></el-table-column>
      <el-table-column prop="assigneeAgentId" label="执行人" width="90" /><el-table-column label="截止" width="140"><template #default="{row}">{{ formatDate(row.dueTime) }}</template></el-table-column>
      <el-table-column prop="actualHours" label="实际工时" width="90" />
      <el-table-column v-if="session.user.role!=='CLIENT'" width="280"><template #default="{row}">
        <el-button v-if="row.status==='TODO'" link type="primary" @click="start(row)">开始</el-button>
        <el-button v-if="row.status!=='DONE' && row.status!=='CANCELLED'" link type="success" @click="done(row)">完成</el-button>
        <el-button link @click="dep(row)">依赖</el-button>
        <el-button link @click="hours(row)">工时</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="open" title="新建工作项" width="min(480px,94vw)">
      <el-form label-position="top"><el-form-item label="标题"><el-input v-model="form.title" /></el-form-item><el-form-item label="类型"><el-input v-model="form.workType" /></el-form-item><el-form-item label="说明"><el-input v-model="form.description" type="textarea" /></el-form-item></el-form>
      <template #footer><el-button @click="open=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </section>
</template>
