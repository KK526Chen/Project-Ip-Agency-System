<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { users } from '../../utils/mockData'
import { labels } from '../../utils/enums'

const query = reactive({ keyword: '', role: '', status: '' })
const dialog = ref(false)
const form = reactive({ username: '', realName: '', role: 'ASSISTANT', phone: '', email: '', password: '' })
const filtered = computed(() => users.filter((item) =>
  (!query.keyword || `${item.username}${item.realName}${item.phone}`.includes(query.keyword)) &&
  (!query.role || item.role === query.role) &&
  (query.status === '' || item.status === query.status),
))
const reset = () => Object.assign(query, { keyword: '', role: '', status: '' })
const submit = () => {
  if (!form.username || !form.realName || !form.password) return ElMessage.warning('请填写必填信息')
  ElMessage.success('用户已创建')
  dialog.value = false
}
</script>

<template>
  <div class="page">
    <div class="page-heading"><div><h1>用户管理</h1><p>维护团队账号、角色和启用状态</p></div><el-button type="primary" :icon="Plus" @click="dialog = true">新增用户</el-button></div>
    <div class="toolbar"><div class="filters">
      <el-input v-model="query.keyword" class="filter-input" placeholder="用户名、姓名或手机号" clearable :prefix-icon="Search" />
      <el-select v-model="query.role" class="filter-select" placeholder="角色" clearable><el-option v-for="(text, value) in labels.role" :key="value" :label="text" :value="value" /></el-select>
      <el-select v-model="query.status" class="filter-select" placeholder="账号状态" clearable><el-option label="启用" :value="1" /><el-option label="停用" :value="0" /></el-select>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div><span class="muted">共 {{ filtered.length }} 人</span></div>
    <div class="table-wrap"><el-table :data="filtered" stripe>
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column label="角色" width="110"><template #default="{ row }"><el-tag effect="plain">{{ labels.role[row.role] }}</el-tag></template></el-table-column>
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="190" />
      <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status ? 'success' : 'info'" effect="plain">{{ row.status ? '启用' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="130" fixed="right"><template #default="{ row }"><div class="action-cell"><el-button link type="primary">编辑</el-button><el-button link :type="row.status ? 'danger' : 'success'">{{ row.status ? '停用' : '启用' }}</el-button></div></template></el-table-column>
    </el-table></div>
    <div class="pagination-row"><el-pagination background layout="total, prev, pager, next" :total="filtered.length" :page-size="10" /></div>

    <el-dialog v-model="dialog" title="新增用户" width="min(600px, 94vw)"><el-form :model="form" label-position="top"><el-row :gutter="16">
      <el-col :span="12"><el-form-item label="用户名" required><el-input v-model="form.username" /></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="姓名" required><el-input v-model="form.realName" /></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="角色"><el-select v-model="form.role"><el-option v-for="(text, value) in labels.role" :key="value" :label="text" :value="value" /></el-select></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="初始密码" required><el-input v-model="form.password" type="password" show-password /></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
    </el-row></el-form><template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" @click="submit">创建</el-button></template></el-dialog>
  </div>
</template>
