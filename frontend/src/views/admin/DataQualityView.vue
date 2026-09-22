<script setup>
import { ref } from 'vue'
import { scanDataQuality } from '../../api/forkc'
import PageHeader from '../../components/PageHeader.vue'
const list = ref([])
const scan = async () => { list.value = (await scanDataQuality()).data }
</script>
<template>
  <div class="page"><PageHeader title="数据质量" description="只报告不偷偷修复"><el-button type="primary" @click="scan">扫描</el-button></PageHeader>
    <el-table :data="list"><el-table-column prop="issueCode" label="问题" /><el-table-column prop="caseId" label="案件" /><el-table-column prop="detail" label="说明" /><el-table-column prop="status" label="状态" /></el-table>
  </div>
</template>
