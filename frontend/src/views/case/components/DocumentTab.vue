<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listDocuments, uploadDocument, downloadDocument, confirmOcr } from '../../../api/document'
import { uploadVersion } from '../../../api/forkc'
import { saveDownload } from '../../../utils/download'
import { session } from '../../../utils/session'
import { options, text, formatDate, tagType } from '../../../utils/enums'
import { messageOf } from '../../../utils/request'
const props = defineProps({ caseId: Number }); const list = ref([]); const open = ref(false); const ocrOpen = ref(false); const revOpen = ref(false)
const uploading = ref(false); const file = ref(); const current = ref({}); const ocr = reactive({ ocrText: '', officialDeadline: '', officialIssueDate: '', feeAmountExtracted: '', ocrExtractedJson: '{}' })
const form = reactive({ documentType: session.user.role === 'CLIENT' ? 'SUPPLEMENT' : 'APPLICATION', remark: '' })
const allowed = () => session.user.role === 'CLIENT'
  ? options('documentType').filter(o => ['TECHNICAL_DISCLOSURE', 'TRADEMARK_IMAGE', 'SUPPLEMENT', 'OTHER'].includes(o.value))
  : session.user.role === 'AGENT'
    ? options('documentType').filter(o => ['APPLICATION', 'OFFICE_ACTION_RESPONSE', 'INTERNAL', 'SUPPLEMENT', 'OTHER'].includes(o.value))
    : options('documentType')
const load = async () => { list.value = (await listDocuments({ caseId: props.caseId, pageSize: 100 })).data.list }
const upload = async () => {
  if (!file.value) return ElMessage.warning('请选择文件'); uploading.value = true
  try {
    const data = new FormData(); data.append('file', file.value); data.append('caseId', props.caseId); data.append('documentType', form.documentType); if (form.remark) data.append('remark', form.remark)
    await uploadDocument(data); ElMessage.success('上传成功'); open.value = false; file.value = null; await load()
  } catch (e) { ElMessage.error(messageOf(e)) } finally { uploading.value = false }
}
const download = async (row) => { try { saveDownload(await downloadDocument(row.id), row.documentName) } catch (e) { ElMessage.error(messageOf(e, '下载失败')) } }
const showOcr = (row) => { current.value = row; Object.assign(ocr, { ocrText: row.ocrText || '', officialDeadline: row.officialDeadline || '', officialIssueDate: row.officialIssueDate || '', feeAmountExtracted: row.feeAmountExtracted || '', ocrExtractedJson: row.ocrExtractedJson || '{}' }); ocrOpen.value = true }
const saveOcr = async () => { try { await confirmOcr(current.value.id, { ...ocr }); ElMessage.success('OCR 已确认'); ocrOpen.value = false; load() } catch (e) { ElMessage.error(messageOf(e)) } }
const showRev = (row) => { current.value = row; file.value = null; revOpen.value = true }
const saveRev = async () => {
  if (!file.value) return ElMessage.warning('请选择修订文件')
  const data = new FormData(); data.append('file', file.value)
  try { await uploadVersion(current.value.id, data); ElMessage.success('已创建修订版，原文件未覆盖'); revOpen.value = false; load() } catch (e) { ElMessage.error(messageOf(e)) }
}
onMounted(load)
</script>
<template>
  <section class="tab-panel">
    <div class="tab-actions"><el-button type="primary" @click="open=true">上传文件</el-button></div>
    <el-table :data="list">
      <el-table-column prop="documentName" label="文件名" min-width="210" />
      <el-table-column label="类型" width="130"><template #default="{row}">{{ text('documentType', row.documentType) }}</template></el-table-column>
      <el-table-column label="审核" width="110"><template #default="{row}"><el-tag :type="tagType(row.reviewStatus)">{{ text('reviewStatus', row.reviewStatus) }}</el-tag></template></el-table-column>
      <el-table-column label="OCR" width="100"><template #default="{row}">{{ row.ocrStatus || '—' }}</template></el-table-column>
      <el-table-column label="上传时间" width="150"><template #default="{row}">{{ formatDate(row.createTime) }}</template></el-table-column>
      <el-table-column width="220"><template #default="{row}">
        <el-button link type="primary" @click="download(row)">下载</el-button>
        <el-button v-if="session.user.role!=='CLIENT' && row.documentType==='OFFICIAL'" link type="primary" @click="showOcr(row)">确认提取</el-button>
        <el-button v-if="session.user.role!=='CLIENT' && ['MINOR_REVISION','MAJOR_REVISION','RESUBMIT_REQUIRED'].includes(row.reviewStatus)" link type="warning" @click="showRev(row)">创建修订版</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="open" title="上传业务文件" width="min(520px,94vw)">
      <el-form label-position="top">
        <el-form-item label="文件类型"><el-select v-model="form.documentType"><el-option v-for="o in allowed()" :key="o.value" v-bind="o" /></el-select></el-form-item>
        <el-form-item label="选择文件"><input type="file" accept=".pdf,.doc,.docx,.jpg,.jpeg,.png,.txt" @change="file=$event.target.files[0]" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="open=false">取消</el-button><el-button type="primary" :loading="uploading" @click="upload">上传</el-button></template>
    </el-dialog>
    <el-dialog v-model="ocrOpen" title="确认官文提取字段" width="min(520px,94vw)">
      <el-form label-position="top">
        <el-form-item label="官方期限"><el-date-picker v-model="ocr.officialDeadline" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="发文日期"><el-date-picker v-model="ocr.officialIssueDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="提取费用"><el-input v-model="ocr.feeAmountExtracted" /></el-form-item>
        <el-form-item label="OCR 文本"><el-input v-model="ocr.ocrText" type="textarea" /></el-form-item>
        <el-form-item label="JSON"><el-input v-model="ocr.ocrExtractedJson" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="ocrOpen=false">取消</el-button><el-button type="primary" @click="saveOcr">确认</el-button></template>
    </el-dialog>
    <el-dialog v-model="revOpen" title="创建修订版（不覆盖原文件）" width="min(480px,94vw)">
      <input type="file" @change="file=$event.target.files[0]" />
      <template #footer><el-button @click="revOpen=false">取消</el-button><el-button type="primary" @click="saveRev">上传 Vn+1</el-button></template>
    </el-dialog>
  </section>
</template>
