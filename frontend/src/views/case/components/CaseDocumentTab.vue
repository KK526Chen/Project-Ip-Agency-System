<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Upload } from '@element-plus/icons-vue'
import { documents } from '../../../utils/mockData'
import { labels } from '../../../utils/enums'

const props = defineProps({ caseId: { type: Number, required: true } })
const rows = computed(() => documents.filter((item) => item.caseId === props.caseId))
</script>

<template>
  <section class="tab-panel"><div class="tab-actions"><el-button type="primary" :icon="Upload">上传文档</el-button></div><el-table :data="rows">
    <el-table-column prop="documentName" label="文档名称" min-width="230" />
    <el-table-column label="类型" width="110"><template #default="{ row }">{{ labels.documentType[row.documentType] }}</template></el-table-column>
    <el-table-column prop="uploaderName" label="上传人" width="110" />
    <el-table-column prop="uploadTime" label="上传时间" width="170" />
    <el-table-column label="操作" width="100"><template #default="{ row }"><el-button link type="primary" :icon="Download" @click="ElMessage.info(`准备下载：${row.documentName}`)">下载</el-button></template></el-table-column>
  </el-table><el-empty v-if="!rows.length" description="暂无案件文档" /></section>
</template>
