<script setup>
import { computed, reactive, ref } from 'vue'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { deadlines, cases, users } from '../../utils/mockData'
import { labels, tagType } from '../../utils/enums'

const query = reactive({ caseId: '', status: '' })
const dialog = ref(false)
const form = reactive({ caseId: '', deadlineName: '', deadlineDate: '', status: 'PENDING', responsibleId: '', remark: '' })
const filtered = computed(() => deadlines.filter((item) => (!query.caseId || item.caseId === query.caseId) && (!query.status || item.status === query.status)))
const reset = () => Object.assign(query, { caseId: '', status: '' })
</script>

<template><div class="page">
  <div class="page-heading"><div><h1>期限管理</h1><p>集中查看关键期限与负责人</p></div><el-button type="primary" :icon="Plus" @click="dialog = true">新增期限</el-button></div>
  <div class="toolbar"><div class="filters"><el-select v-model="query.caseId" class="filter-input" placeholder="关联案件" clearable><el-option v-for="item in cases" :key="item.id" :label="item.caseName" :value="item.id" /></el-select><el-select v-model="query.status" class="filter-select" placeholder="期限状态" clearable><el-option v-for="(text, value) in labels.deadlineStatus" :key="value" :label="text" :value="value" /></el-select><el-button :icon="Refresh" @click="reset">重置</el-button></div><span class="muted">共 {{ filtered.length }} 项</span></div>
  <div class="table-wrap"><el-table :data="filtered" stripe><el-table-column prop="deadlineName" label="期限名称" min-width="170" /><el-table-column prop="caseName" label="关联案件" min-width="230" show-overflow-tooltip /><el-table-column prop="deadlineDate" label="截止日期" width="115" /><el-table-column prop="responsibleName" label="负责人" width="100" /><el-table-column label="状态" width="95"><template #default="{ row }"><el-tag :type="tagType(row.status)" effect="plain">{{ labels.deadlineStatus[row.status] }}</el-tag></template></el-table-column><el-table-column prop="remark" label="备注" min-width="140" /><el-table-column label="操作" width="130"><template #default><el-button link type="primary">编辑</el-button><el-button link type="danger">删除</el-button></template></el-table-column></el-table></div>
  <div class="pagination-row"><el-pagination background layout="total, prev, pager, next" :total="filtered.length" :page-size="10" /></div>
  <el-dialog v-model="dialog" title="新增期限" width="min(560px, 94vw)"><el-form :model="form" label-position="top"><el-form-item label="关联案件" required><el-select v-model="form.caseId"><el-option v-for="item in cases" :key="item.id" :label="item.caseName" :value="item.id" /></el-select></el-form-item><el-form-item label="期限名称" required><el-input v-model="form.deadlineName" /></el-form-item><el-row :gutter="16"><el-col :span="12"><el-form-item label="截止日期" required><el-date-picker v-model="form.deadlineDate" type="date" value-format="YYYY-MM-DD" /></el-form-item></el-col><el-col :span="12"><el-form-item label="负责人" required><el-select v-model="form.responsibleId"><el-option v-for="item in users" :key="item.id" :label="item.realName" :value="item.id" /></el-select></el-form-item></el-col></el-row></el-form><template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" @click="dialog = false">保存</el-button></template></el-dialog>
</div></template>
