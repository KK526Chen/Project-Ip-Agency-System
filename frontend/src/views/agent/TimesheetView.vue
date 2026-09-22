<script setup>
import { onMounted, ref } from 'vue'
import { listTimesheets, createTimesheet } from '../../api/forkc'
import PageHeader from '../../components/PageHeader.vue'
const list = ref([])
onMounted(async () => { list.value = (await listTimesheets({ pageSize: 100 })).data.list })
</script>
<template>
  <div class="page"><PageHeader title="工时填报" description="工时汇总到 WorkItem.actualHours，不覆盖预估" />
    <el-table :data="list"><el-table-column prop="workItemId" label="工作项" /><el-table-column prop="caseId" label="案件" /><el-table-column prop="hours" label="小时" /><el-table-column prop="workDate" label="日期" /><el-table-column prop="description" label="说明" /></el-table>
  </div>
</template>
