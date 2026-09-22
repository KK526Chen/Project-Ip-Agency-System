<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createDeadline, listDeadlines, completeDeadline, updateDeadline } from '../../api/deadline'
import { listCases } from '../../api/case'
import { adjustDeadline, deadlineHistory } from '../../api/forkc'
import { session } from '../../utils/session'
import { options, text, formatDate, tagType } from '../../utils/enums'
import PageHeader from '../../components/PageHeader.vue'
import AppPagination from '../../components/AppPagination.vue'
import { messageOf } from '../../utils/request'
const state = reactive({ list: [], total: 0, pageNum: 1, pageSize: 10, status: '', upcoming: false, loading: false })
const cases = ref([]); const open = ref(false); const editId = ref(null); const hist = ref([])
const form = reactive({ caseId: null, deadlineType: 'RESPONSE', taskName: '', officialDeadline: '', internalDeadline: '', priority: 'MEDIUM', description: '' })
const load = async () => { state.loading = true; try { const r = await listDeadlines({ pageNum: state.pageNum, pageSize: state.pageSize, status: state.status || undefined, upcoming: state.upcoming }); state.list = r.data.list; state.total = r.data.total } finally { state.loading = false } }
const complete = async (id) => { try { await completeDeadline(id); ElMessage.success('任务已完成'); load() } catch (e) { ElMessage.error(messageOf(e)) } }
const show = async (row) => {
  editId.value = row?.id || null
  Object.assign(form, row ? { caseId: row.caseId, deadlineType: row.deadlineType, taskName: row.taskName, officialDeadline: row.officialDeadline, internalDeadline: row.internalDeadline, priority: row.priority, description: row.description || '' } : { caseId: null, deadlineType: 'RESPONSE', taskName: '', officialDeadline: '', internalDeadline: '', priority: 'MEDIUM', description: '' })
  hist.value = row ? ((await deadlineHistory(row.id).catch(() => ({ data: [] }))).data || []) : []
  open.value = true
}
const save = async () => {
  try {
    const payload = { ...form, internalDeadline: form.internalDeadline || undefined }
    if (editId.value) await updateDeadline(editId.value, payload); else await createDeadline(payload)
    open.value = false; ElMessage.success('时限已保存'); load()
  } catch (e) { ElMessage.error(messageOf(e)) }
}
onMounted(async () => {
  const path = session.user.role === 'ADMIN' ? '/admin/cases' : '/agent/cases'
  try { cases.value = (await listCases(path, { pageSize: 100 })).data.list } catch { cases.value = [] }
  load()
})
</script>
<template>
  <div class="page">
    <PageHeader title="时限任务" description="跟踪官方期限和内部办理节点"><el-button type="primary" @click="show(null)">新建时限</el-button></PageHeader>
    <div class="toolbar"><div class="filters">
      <el-select v-model="state.status" placeholder="任务状态" clearable @change="load"><el-option v-for="o in options('deadlineStatus')" :key="o.value" v-bind="o" /></el-select>
      <el-checkbox v-model="state.upcoming" @change="load">仅看 7 天内到期</el-checkbox>
    </div></div>
    <div class="table-wrap"><el-table v-loading="state.loading" :data="state.list">
      <el-table-column prop="taskName" label="任务名称" min-width="200" /><el-table-column prop="caseId" label="案件 ID" width="90" />
      <el-table-column prop="deadlineType" label="类型" />
      <el-table-column label="官方期限" width="155"><template #default="{row}">{{ formatDate(row.officialDeadline) }}</template></el-table-column>
      <el-table-column label="优先级" width="90"><template #default="{row}"><el-tag :type="tagType(row.priority)">{{ text('priority', row.priority) }}</el-tag></template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="tagType(row.status)">{{ text('deadlineStatus', row.status) }}</el-tag></template></el-table-column>
      <el-table-column width="140"><template #default="{row}">
        <el-button v-if="row.status!=='COMPLETED'" link type="primary" @click="show(row)">编辑</el-button>
        <el-button v-if="row.status!=='COMPLETED'" link type="success" @click="complete(row.id)">完成</el-button>
      </template></el-table-column>
    </el-table></div>
    <AppPagination v-model:page="state.pageNum" v-model:size="state.pageSize" :total="state.total" @change="load" />
    <el-dialog v-model="open" :title="editId?'编辑时限':'新建时限任务'" width="min(580px,94vw)">
      <el-form class="form-grid" label-position="top">
        <el-form-item label="案件"><el-select v-model="form.caseId" filterable :disabled="!!editId"><el-option v-for="c in cases" :key="c.id" :label="`${c.caseNo||c.id} · ${c.caseName}`" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="时限类型"><el-input v-model="form.deadlineType" /></el-form-item>
        <el-form-item class="span-2" label="任务名称"><el-input v-model="form.taskName" /></el-form-item>
        <el-form-item label="官方期限"><el-date-picker v-model="form.officialDeadline" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="内部期限"><el-date-picker v-model="form.internalDeadline" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="优先级"><el-select v-model="form.priority"><el-option v-for="o in options('priority')" :key="o.value" v-bind="o" /></el-select></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" /></el-form-item>
      </el-form>
      <el-table v-if="hist.length" :data="hist" size="small"><el-table-column prop="changeType" label="类型" /><el-table-column prop="oldValue" label="原值" /><el-table-column prop="newValue" label="新值" /></el-table>
      <template #footer><el-button @click="open=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
