<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { caseChildren, createCase, getCase, submitCase, updateCase } from '../../api/case'
import { getClientProfile } from '../../api/client'
import { publicList } from '../../api/public'
import { options, text } from '../../utils/enums'
import { messageOf } from '../../utils/request'
import PageHeader from '../../components/PageHeader.vue'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const loading = ref(false)
const services = ref([])
const caseId = computed(() => route.params.id ? Number(route.params.id) : null)
const form = reactive({
  caseName: '',
  caseType: 'INVENTION_PATENT',
  serviceProductId: null,
  technicalField: '',
  priorityLevel: 'MEDIUM',
  confidentialReview: 0,
  description: '',
  parties: [{ partyType: 'APPLICANT', name: '', idNo: '', nationality: '中国', address: '', isPrimary: 1, remark: '' }],
  priorities: [],
})

const nameLabel = computed(() => form.caseType === 'TRADEMARK' ? '商标名称' : '案件名称')
const fieldLabel = computed(() => {
  if (form.caseType === 'TRADEMARK') return '尼斯分类（商品/服务类别）'
  if (form.caseType === 'DESIGN_PATENT') return '洛迦诺分类号'
  if (form.caseType === 'COPYRIGHT') return '作品类别'
  return '技术领域（IPC 分类号）'
})
const submitHint = computed(() => {
  if (form.caseType === 'TRADEMARK') return '提交审核前需要：服务产品、至少一名申请人、尼斯分类。商标图样在保存后到案件详情的「文件与官文」上传。'
  if (form.caseType === 'DESIGN_PATENT') return '提交审核前需要：服务产品、至少一名申请人、至少一名设计人。'
  if (form.caseType === 'INVENTION_PATENT' || form.caseType === 'UTILITY_MODEL') return '提交审核前需要：服务产品、至少一名申请人、至少一名发明人。技术交底书在保存后到案件详情的「文件与官文」上传。'
  return '提交审核前需要：服务产品，以及至少一名申请人。'
})
const serviceOptions = computed(() => services.value.filter((item) => serviceMatches(item.serviceType, form.caseType)))

const serviceMatches = (serviceType, caseType) => {
  if (serviceType === 'PATENT_APPLICATION') return ['INVENTION_PATENT', 'UTILITY_MODEL', 'DESIGN_PATENT'].includes(caseType)
  if (serviceType === 'TRADEMARK_REGISTRATION') return caseType === 'TRADEMARK'
  if (serviceType === 'COPYRIGHT_REGISTRATION') return caseType === 'COPYRIGHT'
  return true
}
const primaryLabel = (type) => ({ APPLICANT: '第一申请人', INVENTOR: '第一发明人', DESIGNER: '第一设计人', RIGHT_HOLDER: '第一权利人' }[type] || '第一主体')
const blank = (value) => !value || !String(value).trim()
const extraPartyType = () => {
  if (form.caseType === 'DESIGN_PATENT') return 'DESIGNER'
  if (form.caseType === 'TRADEMARK' || form.caseType === 'COPYRIGHT') return 'RIGHT_HOLDER'
  return 'INVENTOR'
}
const addParty = () => form.parties.push({ partyType: extraPartyType(), name: '', idNo: '', nationality: '', address: '', isPrimary: 0, remark: '' })
const addPriority = () => form.priorities.push({ country: '中国', priorityNo: '', priorityDate: '' })
const onCaseTypeChange = () => {
  if (form.serviceProductId && !serviceOptions.value.some((item) => item.id === form.serviceProductId)) form.serviceProductId = null
}
const dropUnknownProduct = () => {
  if (form.serviceProductId && !services.value.some((item) => item.id === form.serviceProductId)) form.serviceProductId = null
}
const useProfile = async () => {
  try {
    const profile = (await getClientProfile()).data
    let applicant = form.parties.find((item) => item.partyType === 'APPLICANT')
    if (!applicant) {
      applicant = { partyType: 'APPLICANT', name: '', idNo: '', nationality: '中国', address: '', isPrimary: 1, remark: '' }
      form.parties.unshift(applicant)
    }
    applicant.name = profile.clientName || applicant.name
    applicant.idNo = profile.creditOrIdNo || ''
    applicant.address = profile.registeredAddress || profile.contactAddress || ''
    applicant.nationality = applicant.nationality || '中国'
    applicant.isPrimary = 1
    ElMessage.success('已填入客户资料中的申请人')
  } catch (error) {
    ElMessage.error(messageOf(error, '请先完善客户资料'))
  }
}
const warn = (message) => { ElMessage.warning(message); return false }
const validate = (andSubmit) => {
  if (blank(form.caseName)) return warn(`请填写${nameLabel.value}`)
  if (!form.parties.length) return warn('请至少添加一名当事人')
  const primaryCount = {}
  for (const party of form.parties) {
    if (blank(party.name)) return warn('请填写每位当事人的姓名或主体名称')
    if (party.isPrimary === 1) primaryCount[party.partyType] = (primaryCount[party.partyType] || 0) + 1
    if (primaryCount[party.partyType] > 1) return warn('同一当事人类型只能指定一名第一主体')
  }
  const numbers = new Set()
  for (const item of form.priorities) {
    if (blank(item.country) || blank(item.priorityNo) || !item.priorityDate) return warn('优先权需填写国家/地区、优先权号和优先权日')
    if (numbers.has(item.priorityNo.trim())) return warn('优先权号不能重复')
    numbers.add(item.priorityNo.trim())
  }
  if (!andSubmit) return true
  if (!form.serviceProductId) return warn('提交前请选择服务产品')
  if (!form.parties.some((item) => item.partyType === 'APPLICANT')) return warn('提交前请填写至少一名申请人')
  if (['INVENTION_PATENT', 'UTILITY_MODEL'].includes(form.caseType) && !form.parties.some((item) => item.partyType === 'INVENTOR')) return warn('专利申请请填写至少一名发明人')
  if (form.caseType === 'DESIGN_PATENT' && !form.parties.some((item) => item.partyType === 'DESIGNER')) return warn('外观设计请填写至少一名设计人')
  if (form.caseType === 'TRADEMARK' && blank(form.technicalField)) return warn('商标案件请填写尼斯分类')
  return true
}
const payloadOf = () => ({
  caseName: form.caseName.trim(),
  caseType: form.caseType,
  serviceProductId: form.serviceProductId || null,
  technicalField: form.technicalField?.trim() || null,
  priorityLevel: form.priorityLevel,
  confidentialReview: form.confidentialReview,
  description: form.description?.trim() || null,
  parties: form.parties.map(({ partyType, name, idNo, nationality, address, isPrimary, remark }) => ({
    partyType, name: name.trim(), idNo: idNo?.trim() || null, nationality: nationality?.trim() || null, address: address?.trim() || null, isPrimary, remark: remark?.trim() || null,
  })),
  priorities: form.priorities.map(({ country, priorityNo, priorityDate }) => ({ country: country.trim(), priorityNo: priorityNo.trim(), priorityDate })),
})
const save = async (andSubmit = false) => {
  if (!validate(andSubmit)) return
  saving.value = true
  let id = caseId.value
  try {
    const result = id ? await updateCase(id, payloadOf()) : await createCase(payloadOf())
    id = id || result.data.id
    if (andSubmit) await submitCase(id)
    ElMessage.success(andSubmit ? '委托已提交审核' : (caseId.value ? '委托资料已更新' : '委托草稿已创建'))
    router.replace(`/client/cases/${id}`)
  } catch (error) {
    ElMessage.error(messageOf(error))
    if (id && !caseId.value) router.replace(`/client/cases/${id}/edit`)
  } finally {
    saving.value = false
  }
}
onMounted(async () => {
  loading.value = true
  try {
    const listed = await publicList('services', { pageSize: 100 })
    services.value = listed.data.list || []
    if (!caseId.value) {
      dropUnknownProduct()
      return
    }
    const [detail, parties, priorities] = await Promise.all([
      getCase('/client/cases', caseId.value),
      caseChildren(caseId.value, 'parties', { pageSize: 100 }),
      caseChildren(caseId.value, 'priorities', { pageSize: 100 }),
    ])
    Object.assign(form, detail.data, {
      parties: parties.data.list?.length ? parties.data.list : form.parties,
      priorities: priorities.data.list || [],
    })
    dropUnknownProduct()
  } catch (error) {
    ElMessage.error(messageOf(error))
    if (caseId.value) router.replace('/client/cases')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="page commission-page">
    <PageHeader title="提交案件委托" description="先保存委托资料。草稿和退回状态可以继续修改，确认后再提交审核。" />
    <el-alert class="commission-hint" type="info" :closable="false" :title="submitHint" />
    <el-form class="surface business-form" :model="form" label-position="top">
      <h2>案件基本信息</h2>
      <div class="form-grid">
        <el-form-item class="span-2" :label="nameLabel" required>
          <el-input v-model="form.caseName" maxlength="300" show-word-limit />
        </el-form-item>
        <el-form-item label="案件类型" required>
          <el-select v-model="form.caseType" @change="onCaseTypeChange">
            <el-option v-for="item in options('caseType')" :key="item.value" v-bind="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务产品">
          <el-select v-model="form.serviceProductId" clearable filterable placeholder="选择在售服务">
            <el-option v-for="item in serviceOptions" :key="item.id" :label="`${item.serviceName}（${text('serviceType', item.serviceType)}）`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="fieldLabel">
          <el-input v-model="form.technicalField" maxlength="300" :placeholder="form.caseType === 'TRADEMARK' ? '例如：第9类 科学仪器、第42类 科学技术服务' : '例如：G06N 人工智能'" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priorityLevel">
            <el-option v-for="item in options('priority')" :key="item.value" v-bind="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="保密审查请求">
          <el-switch v-model="form.confidentialReview" :active-value="1" :inactive-value="0" active-text="请求" inactive-text="不请求" />
        </el-form-item>
        <el-form-item class="span-2" label="案件说明">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="2000" show-word-limit placeholder="技术背景、委托要求或商标含义" />
        </el-form-item>
      </div>

      <div class="subform-heading">
        <h2>当事人</h2>
        <div>
          <el-button @click="useProfile">从客户资料填入申请人</el-button>
          <el-button @click="addParty">添加当事人</el-button>
        </div>
      </div>
      <article v-for="(party, index) in form.parties" :key="index" class="party-card">
        <div class="form-grid">
          <el-form-item label="类型">
            <el-select v-model="party.partyType">
              <el-option v-for="item in options('partyType')" :key="item.value" v-bind="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="姓名或主体名称" required>
            <el-input v-model="party.name" maxlength="200" />
          </el-form-item>
          <el-form-item label="身份证号 / 统一社会信用代码">
            <el-input v-model="party.idNo" maxlength="100" />
          </el-form-item>
          <el-form-item label="国籍">
            <el-input v-model="party.nationality" maxlength="100" />
          </el-form-item>
          <el-form-item class="span-2" label="地址">
            <el-input v-model="party.address" maxlength="500" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="party.remark" maxlength="500" />
          </el-form-item>
          <el-form-item label=" ">
            <el-checkbox v-model="party.isPrimary" :true-value="1" :false-value="0">{{ primaryLabel(party.partyType) }}</el-checkbox>
            <el-button v-if="form.parties.length > 1" link type="danger" @click="form.parties.splice(index, 1)">移除</el-button>
          </el-form-item>
        </div>
      </article>

      <div class="subform-heading">
        <h2>优先权（可选，一案可多条）</h2>
        <el-button @click="addPriority">添加优先权</el-button>
      </div>
      <p v-if="!form.priorities.length" class="muted">没有在先申请时可以不填。</p>
      <article v-for="(item, index) in form.priorities" :key="index" class="party-card">
        <div class="form-grid">
          <el-form-item label="国家/地区">
            <el-input v-model="item.country" maxlength="100" />
          </el-form-item>
          <el-form-item label="优先权号">
            <el-input v-model="item.priorityNo" maxlength="100" />
          </el-form-item>
          <el-form-item label="优先权日">
            <el-date-picker v-model="item.priorityDate" value-format="YYYY-MM-DD" placeholder="选择日期" />
          </el-form-item>
          <el-form-item label=" ">
            <el-button link type="danger" @click="form.priorities.splice(index, 1)">移除</el-button>
          </el-form-item>
        </div>
      </article>

      <div class="form-footer">
        <el-button @click="router.back()">取消</el-button>
        <el-button :loading="saving" @click="save(false)">保存草稿</el-button>
        <el-button type="primary" :loading="saving" @click="save(true)">保存并提交审核</el-button>
      </div>
    </el-form>
  </div>
</template>

<style scoped>
.commission-page { max-width: 980px; }
.commission-page :deep(.el-select),
.commission-page :deep(.el-date-editor) { width: 100%; }
.commission-hint { margin-bottom: 14px; }
.party-card { margin-top: 12px; padding: 4px 14px; border: 1px solid var(--line); border-radius: 8px; background: #fbfcfb; }
.party-card .el-button { margin-left: 12px; }
</style>
