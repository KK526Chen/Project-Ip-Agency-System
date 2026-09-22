<script setup>
import { onMounted, ref } from 'vue'
import { caseRisks } from '../../../api/forkc'
import { tagType } from '../../../utils/enums'
const props = defineProps({ caseId: Number }); const list = ref([])
onMounted(async () => { try { list.value = (await caseRisks(props.caseId)).data.list || (await caseRisks(props.caseId)).data } catch { list.value = [] } })
</script>
<template>
  <section class="tab-panel">
    <el-table :data="Array.isArray(list) ? list : []">
      <el-table-column prop="riskType" label="类型" /><el-table-column label="级别" width="100"><template #default="{row}"><el-tag :type="tagType(row.levelCode)">{{ row.levelCode }}</el-tag></template></el-table-column>
      <el-table-column prop="title" label="说明" min-width="220" /><el-table-column prop="status" label="状态" width="100" />
    </el-table>
    <el-empty v-if="!(Array.isArray(list) && list.length)" description="暂无风险，逾期时限不会自动终止案件" />
  </section>
</template>
