<script setup>
import { onMounted, ref } from 'vue'
import { listRisks } from '../../api/forkc'
import PageHeader from '../../components/PageHeader.vue'
const list = ref([])
onMounted(async () => { list.value = (await listRisks({ pageSize: 100 })).data.list })
</script>
<template>
  <div class="page"><PageHeader title="风险中心" description="风险只告警，不自动修改案件主状态" />
    <el-table :data="list"><el-table-column prop="caseId" label="案件" /><el-table-column prop="riskType" label="类型" /><el-table-column prop="levelCode" label="级别" /><el-table-column prop="title" label="说明" /><el-table-column prop="status" label="状态" /></el-table>
  </div>
</template>
