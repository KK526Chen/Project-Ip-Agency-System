<script setup>
import { computed, reactive, ref } from 'vue'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { tasks, cases, users } from '../../utils/mockData'
import { labels, tagType } from '../../utils/enums'

const query = reactive({ keyword: '', status: '', priority: '' })
const dialog = ref(false)
const form = reactive({ caseId: '', title: '', description: '', assigneeId: '', status: 'TODO', priority: 'NORMAL', dueDate: '' })
const filtered = computed(() => tasks.filter((item) => (!query.keyword || `${item.title}${item.caseName}`.includes(query.keyword)) && (!query.status || item.status === query.status) && (!query.priority || item.priority === query.priority)))
const reset = () => Object.assign(query, { keyword: '', status: '', priority: '' })
</script>

<template><div class="page">
  <div class="page-heading"><div><h1>任务管理</h1><p>安排案件任务并跟踪执行状态</p></div><el-button type="primary" :icon="Plus" @click="dialog = true">新增任务</el-button></div>
  <div class="toolbar"><div class="filters"><el-input v-model="query.keyword" class="filter-input" placeholder="任务或案件名称" clearable :prefix-icon="Search" /><el-select v-model="query.status" class="filter-select" placeholder="任务状态" clearable><el-option v-for="(text, value) in labels.taskStatus" :key="value" :label="text" :value="value" /></el-select><el-select v-model="query.priority" class="filter-select" placeholder="优先级" clearable><el-option v-for="(text, value) in labels.priority" :key="value" :label="text" :value="value" /></el-select><el-button :icon="Refresh" @click="reset">重置</el-button></div><span class="muted">共 {{ filtered.length }} 项</span></div>
  <div class="table-wrap"><el-table :data="filtered" stripe><el-table-column prop="title" label="任务" min-width="180" /><el-table-column prop="caseName" label="关联案件" min-width="220" show-overflow-tooltip /><el-table-column prop="assigneeName" label="负责人" width="100" /><el-table-column label="状态" width="95"><template #default="{ row }"><el-tag :type="tagType(row.status)" effect="plain">{{ labels.taskStatus[row.status] }}</el-tag></template></el-table-column><el-table-column label="优先级" width="85"><template #default="{ row }">{{ labels.priority[row.priority] }}</template></el-table-column><el-table-column prop="dueDate" label="截止日期" width="110" /><el-table-column label="操作" width="130"><template #default><el-button link type="primary">编辑</el-button><el-button link type="danger">删除</el-button></template></el-table-column></el-table></div>
  <div class="pagination-row"><el-pagination background layout="total, prev, pager, next" :total="filtered.length" :page-size="10" /></div>
  <el-dialog v-model="dialog" title="新增任务" width="min(600px, 94vw)"><el-form :model="form" label-position="top"><el-form-item label="关联案件" required><el-select v-model="form.caseId"><el-option v-for="item in cases" :key="item.id" :label="item.caseName" :value="item.id" /></el-select></el-form-item><el-form-item label="任务标题" required><el-input v-model="form.title" /></el-form-item><el-form-item label="任务说明"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item><el-row :gutter="16"><el-col :span="12"><el-form-item label="负责人" required><el-select v-model="form.assigneeId"><el-option v-for="item in users" :key="item.id" :label="item.realName" :value="item.id" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="截止日期"><el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" /></el-form-item></el-col></el-row></el-form><template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" @click="dialog = false">保存</el-button></template></el-dialog>
</div></template>
