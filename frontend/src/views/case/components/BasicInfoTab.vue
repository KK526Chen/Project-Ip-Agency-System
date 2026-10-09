<script setup>
import { onMounted, ref } from 'vue'
import { caseChildren } from '../../../api/case'
import { addCollaborator, removeCollaborator } from '../../../api/forkc'
import { listAdmin } from '../../../api/admin'
import { publicDetail } from '../../../api/public'
import { formatDate, text } from '../../../utils/enums'
import { session } from '../../../utils/session'
import { ElMessage } from 'element-plus'
import { messageOf } from '../../../utils/request'

const props = defineProps({ caseInfo: Object })
const emit = defineEmits(['updated'])
const parties = ref([])
const priorities = ref([])
const reviews = ref([])
const assignments = ref([])
const agents = ref([])
const agentId = ref(null)
const serviceName = ref('')

const load = async () => {
  const id = props.caseInfo.id
  const [p, r, v, a] = await Promise.all([
    caseChildren(id, 'parties', { pageSize: 100 }),
    caseChildren(id, 'priorities', { pageSize: 100 }),
    caseChildren(id, 'reviews', { pageSize: 100 }),
    caseChildren(id, 'assignments', { pageSize: 100 }),
  ])
  parties.value = p.data.list
  priorities.value = r.data.list
  reviews.value = v.data.list
  assignments.value = a.data.list
  if (!props.caseInfo.serviceProductId) return
  try {
    serviceName.value = (await publicDetail('services', props.caseInfo.serviceProductId)).data.serviceName
  } catch {
    serviceName.value = '已选择'
  }
}
const add = async () => {
  try {
    await addCollaborator(props.caseInfo.id, { agentId: agentId.value, reason: '协办' })
    ElMessage.success('已添加协办')
    load()
  } catch (e) {
    ElMessage.error(messageOf(e))
  }
}
const remove = async (row) => {
  try {
    await removeCollaborator(props.caseInfo.id, row.id)
    ElMessage.success('已结束协办')
    load()
  } catch (e) {
    ElMessage.error(messageOf(e))
  }
}
onMounted(async () => {
  load()
  if (session.user.role === 'ADMIN') {
    try { agents.value = (await listAdmin('agents', { pageSize: 100 })).data.list } catch { agents.value = [] }
  }
})
</script>

<template>
  <section class="tab-panel">
    <dl class="description-grid">
      <div class="description-item"><dt>案件名称</dt><dd>{{ caseInfo.caseName }}</dd></div>
      <div class="description-item"><dt>案件类型</dt><dd>{{ text('caseType', caseInfo.caseType) }}</dd></div>
      <div class="description-item"><dt>服务产品</dt><dd>{{ serviceName || '未选择' }}</dd></div>
      <div class="description-item"><dt>技术领域 / 分类</dt><dd>{{ caseInfo.technicalField || '—' }}</dd></div>
      <div class="description-item"><dt>申请/注册号</dt><dd>{{ caseInfo.applicationNo || '—' }}</dd></div>
      <div class="description-item"><dt>承办代理人</dt><dd>{{ caseInfo.principalAgentName || '未分配' }}</dd></div>
      <div class="description-item"><dt>优先级</dt><dd>{{ text('priority', caseInfo.priorityLevel) }}</dd></div>
      <div class="description-item"><dt>保密审查</dt><dd>{{ caseInfo.confidentialReview ? '已请求' : '未请求' }}</dd></div>
      <div class="description-item"><dt>提交时间</dt><dd>{{ formatDate(caseInfo.submitTime) }}</dd></div>
      <div class="description-item"><dt>受理时间</dt><dd>{{ formatDate(caseInfo.acceptTime) }}</dd></div>
      <div class="description-item"><dt>预计下次官文</dt><dd>{{ caseInfo.expectedNextOfficialDate || '—' }}</dd></div>
      <div class="description-item description-wide"><dt>案件说明</dt><dd>{{ caseInfo.description || '—' }}</dd></div>
    </dl>
    <h3>当前分配</h3>
    <div v-if="session.user.role!=='CLIENT'" class="tab-actions" style="justify-content:flex-start;margin-bottom:8px">
      <el-select v-model="agentId" placeholder="选择协办" filterable style="width:240px"><el-option v-for="a in agents" :key="a.id" :label="`${a.employeeNo} · ${a.department||''}`" :value="a.id" /></el-select>
      <el-button type="primary" @click="add">添加协办</el-button>
    </div>
    <el-table :data="assignments" size="small">
      <el-table-column prop="agentId" label="代理人" /><el-table-column prop="assignmentRole" label="角色" />
      <el-table-column prop="isCurrent" label="当前" /><el-table-column prop="reason" label="原因" />
      <el-table-column v-if="session.user.role!=='CLIENT'" width="90"><template #default="{row}"><el-button v-if="row.isCurrent===1 && row.assignmentRole==='COLLABORATOR'" link type="danger" @click="remove(row)">移除</el-button></template></el-table-column>
    </el-table>
    <div class="split-panels">
      <div>
        <h3>当事人</h3>
        <el-table :data="parties" size="small">
          <el-table-column label="类型" width="100"><template #default="{ row }">{{ text('partyType', row.partyType) }}</template></el-table-column>
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column prop="idNo" label="证件号" min-width="140" />
          <el-table-column prop="nationality" label="国籍" width="90" />
          <el-table-column label="第一主体" width="90"><template #default="{ row }">{{ row.isPrimary ? '是' : '否' }}</template></el-table-column>
        </el-table>
      </div>
      <div>
        <h3>优先权</h3>
        <el-table :data="priorities" size="small">
          <el-table-column prop="country" label="国家/地区" />
          <el-table-column prop="priorityNo" label="优先权号" />
          <el-table-column prop="priorityDate" label="优先权日" />
        </el-table>
      </div>
    </div>
    <h3>审核历史</h3>
    <el-table :data="reviews" size="small">
      <el-table-column prop="reviewType" label="审核类型" />
      <el-table-column prop="reviewResult" label="结果" />
      <el-table-column prop="reviewComment" label="意见" />
      <el-table-column label="时间"><template #default="{ row }">{{ formatDate(row.reviewTime) }}</template></el-table-column>
    </el-table>
  </section>
</template>
