<script setup>
import { onMounted, ref } from 'vue'
import { listExceptions, resolveException } from '../../api/forkc'
import PageHeader from '../../components/PageHeader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
const list = ref([])
const load = async () => { list.value = (await listExceptions({ pageSize: 100 })).data.list }
const resolve = async (row) => { const { value } = await ElMessageBox.prompt('解决说明', '处理异常'); await resolveException(row.id, { resolutionNote: value }); ElMessage.success('已解决'); load() }
onMounted(load)
</script>
<template>
  <div class="page"><PageHeader title="执行异常" description="已完成时限被 OCR 改写等异常在此处理，禁止静默覆盖" />
    <el-table :data="list"><el-table-column prop="exceptionType" label="类型" /><el-table-column prop="caseId" label="案件" /><el-table-column prop="detail" label="详情" /><el-table-column prop="status" label="状态" width="110" />
      <el-table-column width="100"><template #default="{row}"><el-button v-if="row.status!=='RESOLVED' && row.status!=='CLOSED'" link type="primary" @click="resolve(row)">解决</el-button></template></el-table-column>
    </el-table>
  </div>
</template>
