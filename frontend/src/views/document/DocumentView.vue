<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Download, Plus, Refresh, Search, UploadFilled } from '@element-plus/icons-vue'
import { cases, documents } from '../../utils/mockData'
import { labels } from '../../utils/enums'

const query = reactive({ keyword: '', caseId: '', documentType: '' })
const dialog = ref(false)
const fileList = ref([])
const form = reactive({ caseId: '', documentType: 'CLIENT', remark: '' })
const filtered = computed(() => documents.filter((item) =>
  (!query.keyword || `${item.documentName}${item.caseName}`.includes(query.keyword)) &&
  (!query.caseId || item.caseId === query.caseId) &&
  (!query.documentType || item.documentType === query.documentType),
))

const reset = () => Object.assign(query, { keyword: '', caseId: '', documentType: '' })
const submit = () => {
  if (!form.caseId || !fileList.value.length) return ElMessage.warning('请选择案件并添加文件')
  ElMessage.success('文档已加入上传队列')
  dialog.value = false
  fileList.value = []
}
const download = (row) => ElMessage.info(`准备下载：${row.documentName}`)
const remove = (row) => ElMessageBox.confirm(`确认删除“${row.documentName}”？`, '删除文档', { type: 'warning' })
  .then(() => ElMessage.success('文档已删除'))
  .catch(() => {})
</script>

<template>
  <div class="page">
    <div class="page-heading">
      <div><h1>文档管理</h1><p>集中管理案件材料、申请文件与官方文书</p></div>
      <el-button type="primary" :icon="Plus" @click="dialog = true">上传文档</el-button>
    </div>
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="query.keyword" class="filter-input" placeholder="文档名称或案件" clearable :prefix-icon="Search" />
        <el-select v-model="query.caseId" class="filter-input" placeholder="所属案件" clearable><el-option v-for="item in cases" :key="item.id" :label="item.caseName" :value="item.id" /></el-select>
        <el-select v-model="query.documentType" class="filter-select" placeholder="文档类型" clearable><el-option v-for="(text, value) in labels.documentType" :key="value" :label="text" :value="value" /></el-select>
        <el-button :icon="Refresh" @click="reset">重置</el-button>
      </div>
      <span class="muted">共 {{ filtered.length }} 份</span>
    </div>
    <div class="table-wrap"><el-table :data="filtered" stripe>
      <el-table-column prop="documentName" label="文档名称" min-width="210" show-overflow-tooltip />
      <el-table-column prop="caseName" label="所属案件" min-width="220" show-overflow-tooltip />
      <el-table-column label="类型" width="100"><template #default="{ row }">{{ labels.documentType[row.documentType] }}</template></el-table-column>
      <el-table-column prop="uploaderName" label="上传人" width="100" />
      <el-table-column prop="uploadTime" label="上传时间" width="160" />
      <el-table-column prop="remark" label="备注" min-width="130" show-overflow-tooltip />
      <el-table-column label="操作" width="130" fixed="right"><template #default="{ row }"><div class="action-cell"><el-button link type="primary" :icon="Download" @click="download(row)">下载</el-button><el-button link type="danger" :icon="Delete" @click="remove(row)">删除</el-button></div></template></el-table-column>
    </el-table></div>
    <div class="pagination-row"><el-pagination background layout="total, prev, pager, next" :total="filtered.length" :page-size="10" /></div>

    <el-dialog v-model="dialog" title="上传文档" width="min(560px, 94vw)">
      <el-form :model="form" label-position="top">
        <el-form-item label="所属案件" required><el-select v-model="form.caseId" filterable><el-option v-for="item in cases" :key="item.id" :label="item.caseName" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="文档类型"><el-select v-model="form.documentType"><el-option v-for="(text, value) in labels.documentType" :key="value" :label="text" :value="value" /></el-select></el-form-item>
        <el-form-item label="文件" required><el-upload v-model:file-list="fileList" drag action="#" :auto-upload="false" :limit="1"><el-icon class="el-icon--upload"><UploadFilled /></el-icon><div class="el-upload__text">拖拽文件到此处，或点击选择</div></el-upload></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" @click="submit">确认上传</el-button></template>
    </el-dialog>
  </div>
</template>
