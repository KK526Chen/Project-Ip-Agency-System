<script setup>
import { computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { deadlines } from '../../../utils/mockData'
import { labels, tagType } from '../../../utils/enums'

const props = defineProps({ caseId: { type: Number, required: true } })
const rows = computed(() => deadlines.filter((item) => item.caseId === props.caseId))
</script>

<template>
  <section class="tab-panel"><div class="tab-actions"><el-button type="primary" :icon="Plus">新增期限</el-button></div><el-table :data="rows">
    <el-table-column prop="deadlineName" label="期限事项" min-width="200" />
    <el-table-column prop="deadlineDate" label="期限日期" width="120" />
    <el-table-column prop="responsibleName" label="负责人" width="110" />
    <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="tagType(row.status)" effect="plain">{{ labels.deadlineStatus[row.status] }}</el-tag></template></el-table-column>
    <el-table-column prop="remark" label="备注" min-width="160" />
    <el-table-column label="操作" width="90"><template #default><el-button link type="primary">处理</el-button></template></el-table-column>
  </el-table><el-empty v-if="!rows.length" description="暂无期限记录" /></section>
</template>
