<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { cases, clients, users } from '../../utils/mockData'
import { labels, tagType } from '../../utils/enums'

const router = useRouter()
const query = reactive({ keyword: '', caseType: '', status: '' })
const dialog = ref(false)
const form = reactive({ caseName: '', clientId: '', caseType: 'PATENT', businessType: '', principalId: '', priority: 'NORMAL', startDate: '' })
const filtered = computed(() => cases.filter((item) => (!query.keyword || `${item.caseNo}${item.caseName}`.includes(query.keyword)) && (!query.caseType || item.caseType === query.caseType) && (!query.status || item.status === query.status)))
const reset = () => Object.assign(query, { keyword: '', caseType: '', status: '' })
</script>

<template>
  <div class="page">
    <div class="page-heading"><div><h1>案件管理</h1><p>跟踪案件状态、负责人和办理节点</p></div><el-button type="primary" :icon="Plus" @click="dialog = true">新建案件</el-button></div>
    <div class="toolbar"><div class="filters">
      <el-input v-model="query.keyword" class="filter-input" placeholder="案件编号或名称" clearable :prefix-icon="Search" />
      <el-select v-model="query.caseType" class="filter-select" placeholder="案件类型" clearable><el-option v-for="(text, value) in labels.caseType" :key="value" :label="text" :value="value" /></el-select>
      <el-select v-model="query.status" class="filter-select" placeholder="案件状态" clearable><el-option v-for="(text, value) in labels.caseStatus" :key="value" :label="text" :value="value" /></el-select>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div><span class="muted">共 {{ filtered.length }} 件</span></div>
    <div class="table-wrap"><el-table :data="filtered" stripe @row-dblclick="(row) => router.push(`/cases/${row.id}`)">
      <el-table-column prop="caseNo" label="案件编号" width="145"><template #default="{ row }"><span class="mono">{{ row.caseNo }}</span></template></el-table-column>
      <el-table-column prop="caseName" label="案件名称" min-width="220" show-overflow-tooltip />
      <el-table-column prop="clientName" label="客户" min-width="150" show-overflow-tooltip />
      <el-table-column label="类型" width="90"><template #default="{ row }">{{ labels.caseType[row.caseType] }}</template></el-table-column>
      <el-table-column prop="principalName" label="负责人" width="100" />
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="tagType(row.status)" effect="plain">{{ labels.caseStatus[row.status] }}</el-tag></template></el-table-column>
      <el-table-column label="优先级" width="85"><template #default="{ row }"><el-tag :type="tagType(row.priority)" effect="plain">{{ labels.priority[row.priority] }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="130" fixed="right"><template #default="{ row }"><div class="action-cell"><el-button link type="primary" @click="router.push(`/cases/${row.id}`)">详情</el-button><el-button link type="primary">编辑</el-button></div></template></el-table-column>
    </el-table></div>
    <div class="pagination-row"><el-pagination background layout="total, prev, pager, next" :total="filtered.length" :page-size="10" /></div>
    <el-dialog v-model="dialog" title="新建案件" width="min(640px, 94vw)"><el-form :model="form" label-position="top"><el-row :gutter="16">
      <el-col :span="24"><el-form-item label="案件名称" required><el-input v-model="form.caseName" /></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="客户" required><el-select v-model="form.clientId"><el-option v-for="item in clients" :key="item.id" :label="item.clientName" :value="item.id" /></el-select></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="案件类型" required><el-select v-model="form.caseType"><el-option v-for="(text, value) in labels.caseType" :key="value" :label="text" :value="value" /></el-select></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="业务类型" required><el-input v-model="form.businessType" /></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="负责人" required><el-select v-model="form.principalId"><el-option v-for="item in users.filter(u => u.role === 'AGENT')" :key="item.id" :label="item.realName" :value="item.id" /></el-select></el-form-item></el-col>
    </el-row></el-form><template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" @click="dialog = false">创建</el-button></template></el-dialog>
  </div>
</template>
