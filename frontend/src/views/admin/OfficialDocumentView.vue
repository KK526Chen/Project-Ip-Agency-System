<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadDocument, confirmOcr, listDocuments } from '../../api/document'
import { listCases } from '../../api/case'
import { listOcrJobs, confirmOcrJob } from '../../api/forkc'
import { messageOf } from '../../utils/request'
import PageHeader from '../../components/PageHeader.vue'
const form = reactive({ caseId: null, remark: '' }); const file = ref(); const loading = ref(false); const cases = ref([]); const docs = ref([]); const jobs = ref([]); const ocr = reactive({ officialDeadline: '', ocrText: 'official', ocrExtractedJson: '{}' }); const currentDoc = ref(null)
const loadCases = async () => { cases.value = (await listCases('/admin/cases', { pageSize: 100 })).data.list }
const refresh = async () => {
  if (form.caseId) {
    try { docs.value = (await listDocuments({ caseId: form.caseId, documentType: 'OFFICIAL', pageSize: 100 })).data.list } catch (e) { docs.value = []; ElMessage.error(messageOf(e, '官文列表加载失败')) }
  } else docs.value = []
  try { jobs.value = (await listOcrJobs({ caseId: form.caseId || undefined, pageSize: 100 })).data.list || [] }
  catch (e) { jobs.value = []; ElMessage.error(messageOf(e, 'OCR 任务加载失败')) }
}
const upload = async () => {
  if (!file.value || !form.caseId) return ElMessage.warning('请选择案件和官文文件'); loading.value = true
  try {
    const data = new FormData(); data.append('file', file.value); data.append('caseId', form.caseId); data.append('documentType', 'OFFICIAL'); if (form.remark) data.append('remark', form.remark)
    const r = await uploadDocument(data); ElMessage.success('官文已上传，尚未创建法律时限'); currentDoc.value = r.data; file.value = null; form.remark = ''; await refresh()
  } catch (e) { ElMessage.error(messageOf(e)) } finally { loading.value = false }
}
const saveOcr = async (docId) => {
  try { await confirmOcr(docId, { ...ocr }); ElMessage.success('已确认，若未完成则幂等写入一条答复时限'); await refresh() } catch (e) { ElMessage.error(messageOf(e)) }
}
onMounted(async () => { await loadCases(); await refresh() })
</script>
<template>
  <div class="page narrow-page">
    <PageHeader title="官文录入" description="上传后生成阶段和通知；确认 OCR 后才创建 Deadline" />
    <section class="surface business-form">
      <el-alert title="未确认 OCR 前不会创建法律时限或费用。重复确认同一官文不会插入第二条未完成时限。" type="info" :closable="false" />
      <el-form label-position="top">
        <el-form-item label="关联案件" required>
          <el-select v-model="form.caseId" filterable placeholder="选择案件" @change="refresh">
            <el-option v-for="c in cases" :key="c.id" :label="`${c.caseNo || c.id} · ${c.caseName}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择官文文件" required><input type="file" accept=".pdf,.doc,.docx,.jpg,.jpeg,.png,.txt" @change="file=$event.target.files[0]" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
        <el-button type="primary" :loading="loading" @click="upload">上传官文</el-button>
      </el-form>
      <h3>本案官文</h3>
      <el-table :data="docs" size="small">
        <el-table-column prop="documentName" label="文件" /><el-table-column prop="ocrStatus" label="OCR" />
        <el-table-column width="160"><template #default="{row}"><el-button link type="primary" @click="currentDoc=row">确认提取</el-button></template></el-table-column>
      </el-table>
      <div v-if="currentDoc" style="margin-top:16px">
        <el-form label-position="top">
          <el-form-item label="官方期限"><el-date-picker v-model="ocr.officialDeadline" type="date" value-format="YYYY-MM-DD" /></el-form-item>
          <el-form-item label="OCR 文本"><el-input v-model="ocr.ocrText" /></el-form-item>
          <el-button type="primary" @click="saveOcr(currentDoc.id)">确认并生成时限</el-button>
        </el-form>
      </div>
      <h3>OCR 任务</h3>
      <el-table :data="jobs" size="small"><el-table-column prop="id" label="Job" /><el-table-column prop="status" label="状态" /><el-table-column prop="documentId" label="文档" /></el-table>
    </section>
  </div>
</template>
