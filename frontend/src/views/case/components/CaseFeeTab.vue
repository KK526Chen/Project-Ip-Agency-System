<script setup>
import { computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { fees } from '../../../utils/mockData'
import { labels, tagType } from '../../../utils/enums'

const props = defineProps({ caseId: { type: Number, required: true } })
const rows = computed(() => fees.filter((item) => item.caseId === props.caseId))
</script>

<template>
  <section class="tab-panel"><div class="tab-actions"><el-button type="primary" :icon="Plus">新增费用</el-button></div><el-table :data="rows">
    <el-table-column label="费用类型" width="120"><template #default="{ row }">{{ labels.feeType[row.feeType] }}</template></el-table-column>
    <el-table-column label="方向" width="100"><template #default="{ row }">{{ labels.direction[row.direction] }}</template></el-table-column>
    <el-table-column label="金额" width="130" align="right"><template #default="{ row }"><strong>¥ {{ Number(row.amount).toLocaleString() }}</strong></template></el-table-column>
    <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="tagType(row.status)" effect="plain">{{ labels.feeStatus[row.status] }}</el-tag></template></el-table-column>
    <el-table-column prop="payDate" label="支付日期" width="120"><template #default="{ row }">{{ row.payDate || '-' }}</template></el-table-column>
    <el-table-column prop="remark" label="备注" min-width="160" />
    <el-table-column label="操作" width="90"><template #default><el-button link type="primary">编辑</el-button></template></el-table-column>
  </el-table><el-empty v-if="!rows.length" description="暂无费用记录" /></section>
</template>
