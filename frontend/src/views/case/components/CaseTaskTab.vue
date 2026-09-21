<script setup>
import { computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { tasks } from '../../../utils/mockData'
import { labels, tagType } from '../../../utils/enums'

const props = defineProps({ caseId: { type: Number, required: true } })
const rows = computed(() => tasks.filter((item) => item.caseId === props.caseId))
</script>

<template>
  <section class="tab-panel"><div class="tab-actions"><el-button type="primary" :icon="Plus">新建任务</el-button></div><el-table :data="rows">
    <el-table-column prop="title" label="任务名称" min-width="220" />
    <el-table-column prop="assigneeName" label="负责人" width="110" />
    <el-table-column label="优先级" width="90"><template #default="{ row }"><el-tag :type="tagType(row.priority)" effect="plain">{{ labels.priority[row.priority] }}</el-tag></template></el-table-column>
    <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="tagType(row.status)" effect="plain">{{ labels.taskStatus[row.status] }}</el-tag></template></el-table-column>
    <el-table-column prop="dueDate" label="截止日期" width="120" />
    <el-table-column label="操作" width="90"><template #default><el-button link type="primary">编辑</el-button></template></el-table-column>
  </el-table><el-empty v-if="!rows.length" description="暂无关联任务" /></section>
</template>
