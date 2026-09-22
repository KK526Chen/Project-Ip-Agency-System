<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { listCases, assignCase, caseChildren } from '../../api/case'
import { listAdmin } from '../../api/admin'
import { recommendAgents } from '../../api/forkc'
import { text, tagType } from '../../utils/enums'
import PageHeader from '../../components/PageHeader.vue'
import AppPagination from '../../components/AppPagination.vue'
import { messageOf } from '../../utils/request'
const reasonPresets = ['专业领域匹配', 'IPC 范围匹配', '案件类型与部门对应', '当前无在办案件', '在办负荷较低', '近7日时限较少', '执业经验充足', '同类案件经验', '客户指定', '负荷均衡', '临时顶岗']
const state = reactive({ list: [], total: 0, pageNum: 1, pageSize: 10, status: 'PENDING_ASSIGNMENT', loading: false })
const agents = ref([]); const recs = ref([]); const history = ref([]); const open = ref(false); const current = ref({}); const form = reactive({ agentId: null, reasonTags: [], reasonExtra: '' }); const reassign = ref(false)
const agentOptions = computed(() => recs.value.length ? recs.value : agents.value)
const reasonOptions = computed(() => {
  const set = new Set(reasonPresets)
  recs.value.forEach((row) => (row.reasons || []).forEach((item) => set.add(item)))
  form.reasonTags.forEach((item) => set.add(item))
  return [...set]
})
const selectedRec = computed(() => recs.value.find((row) => (row.agentId || row.id) === form.agentId) || recs.value[0])
const composedReason = () => [...form.reasonTags, form.reasonExtra].map((item) => String(item || '').trim()).filter(Boolean).join('；')
const load = async () => { state.loading = true; try { const r = await listCases('/admin/cases', { pageNum: state.pageNum, pageSize: state.pageSize, status: state.status || undefined }); state.list = r.data.list; state.total = r.data.total } finally { state.loading = false } }
const fillReasons = (agentId) => {
  const rec = recs.value.find((row) => (row.agentId || row.id) === agentId)
  form.reasonTags = [...(rec?.reasons || [])]
}
const show = async (row) => {
  current.value = row; reassign.value = row.status !== 'PENDING_ASSIGNMENT'
  Object.assign(form, { agentId: null, reasonTags: [], reasonExtra: '' }); open.value = true
  try { recs.value = (await recommendAgents(row.id)).data } catch { recs.value = agents.value.map(a => ({ ...a, agentId: a.id, matchScore: 0, reasons: [], currentCaseCount: '—', openWorkItemCount: '—', deadline7DaysCount: '—' })) }
  try { history.value = (await caseChildren(row.id, 'assignments', { pageSize: 100 })).data.list } catch { history.value = [] }
  form.agentId = recs.value[0]?.agentId || recs.value[0]?.id || null
  fillReasons(form.agentId)
}
const save = async () => { try { await assignCase(current.value.id, { agentId: form.agentId, reason: composedReason() }, reassign.value); ElMessage.success('代理人分配成功'); open.value = false; load() } catch (e) { ElMessage.error(messageOf(e)) } }
watch(() => form.agentId, (id) => { if (open.value && id) fillReasons(id) })
onMounted(async () => { agents.value = (await listAdmin('agents', { pageSize: 100 })).data.list; load() })
</script>
<template>
  <div class="page">
    <PageHeader title="案件分配" description="分配或调整主办代理人，历史记录永久保留" />
    <div class="toolbar"><el-select v-model="state.status" placeholder="案件状态" clearable @change="load"><el-option label="待分配" value="PENDING_ASSIGNMENT" /><el-option label="办理中" value="PROCESSING" /></el-select></div>
    <div class="table-wrap"><el-table v-loading="state.loading" :data="state.list">
      <el-table-column prop="caseNo" label="案件编号" width="160" /><el-table-column prop="caseName" label="案件名称" min-width="230" />
      <el-table-column label="类型" width="110"><template #default="{row}">{{ text('caseType', row.caseType) }}</template></el-table-column>
      <el-table-column prop="principalAgentId" label="当前代理人 ID" width="130" />
      <el-table-column label="状态" width="110"><template #default="{row}"><el-tag :type="tagType(row.status)">{{ text('caseStatus', row.status) }}</el-tag></template></el-table-column>
      <el-table-column width="100"><template #default="{row}"><el-button link type="primary" @click="show(row)">{{ row.status === 'PENDING_ASSIGNMENT' ? '分配' : '调整' }}</el-button></template></el-table-column>
    </el-table></div>
    <AppPagination v-model:page="state.pageNum" v-model:size="state.pageSize" :total="state.total" @change="load" />
    <el-dialog v-model="open" :title="reassign ? '调整主办代理人' : '分配主办代理人'" width="min(720px,94vw)">
      <p class="dialog-lead">{{ current.caseName }}</p>
      <el-form label-position="top">
        <el-form-item label="代理人（含部门 / 负载 / 匹配分）">
          <el-select v-model="form.agentId" filterable style="width:100%">
            <el-option v-for="a in agentOptions" :key="a.agentId || a.id" :value="a.agentId || a.id"
              :label="`${a.employeeNo} · ${a.department || '—'} · ${a.professionalField || '代理人'} · 在办${a.currentCaseCount ?? '—'} · WI${a.openWorkItemCount ?? '—'} · 7日时限${a.deadline7DaysCount ?? '—'} · ${a.matchScore ?? ''}`" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="selectedRec" label="系统建议">
          <span class="muted">{{ selectedRec.reasons?.length ? selectedRec.reasons.join('；') : '无自动命中项，可手选或输入补充' }}</span>
        </el-form-item>
        <el-form-item label="推荐理由（可多选，可输入其它）">
          <el-select v-model="form.reasonTags" multiple filterable allow-create default-first-option clearable placeholder="选择或输入理由" style="width:100%">
            <el-option v-for="item in reasonOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="form.reasonExtra" type="textarea" :rows="2" placeholder="可再写其它分配原因，会一并保存" />
        </el-form-item>
      </el-form>
      <h3>分配历史</h3>
      <el-table :data="history" size="small"><el-table-column prop="agentId" label="代理人" /><el-table-column prop="assignmentRole" label="角色" /><el-table-column prop="isCurrent" label="当前" /><el-table-column prop="reason" label="原因" /></el-table>
      <template #footer><el-button @click="open = false">取消</el-button><el-button type="primary" @click="save">确认分配</el-button></template>
    </el-dialog>
  </div>
</template>
