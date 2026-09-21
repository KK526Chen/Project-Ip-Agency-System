<script setup>
import { computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { members } from '../../../utils/mockData'
import { labels } from '../../../utils/enums'

const props = defineProps({ caseId: { type: Number, required: true } })
const rows = computed(() => members.filter((item) => item.caseId === props.caseId))
</script>

<template>
  <section class="tab-panel"><div class="tab-actions"><el-button type="primary" :icon="Plus">添加成员</el-button></div><el-table :data="rows">
    <el-table-column prop="realName" label="成员" min-width="130" />
    <el-table-column label="系统角色" width="130"><template #default="{ row }">{{ labels.role[row.role] }}</template></el-table-column>
    <el-table-column label="案件角色" width="130"><template #default="{ row }">{{ labels.memberRole?.[row.memberRole] || (row.memberRole === 'PRINCIPAL' ? '负责人' : '协办人') }}</template></el-table-column>
    <el-table-column prop="joinTime" label="加入时间" min-width="170" />
    <el-table-column label="操作" width="90"><template #default><el-button link type="danger">移除</el-button></template></el-table-column>
  </el-table><el-empty v-if="!rows.length" description="暂无案件成员" /></section>
</template>
