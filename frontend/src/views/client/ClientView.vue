<script setup>
import { computed, reactive, ref } from 'vue'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { clients } from '../../utils/mockData'
import { labels } from '../../utils/enums'

const query = reactive({ keyword: '', clientType: '' })
const dialog = ref(false)
const form = reactive({ clientName: '', clientType: 'COMPANY', contactName: '', phone: '', email: '', address: '', remark: '' })
const filtered = computed(() => clients.filter((item) => (!query.keyword || item.clientName.includes(query.keyword)) && (!query.clientType || item.clientType === query.clientType)))
const reset = () => Object.assign(query, { keyword: '', clientType: '' })
</script>

<template>
  <div class="page">
    <div class="page-heading"><div><h1>客户管理</h1><p>维护客户资料及联系信息</p></div><el-button type="primary" :icon="Plus" @click="dialog = true">新增客户</el-button></div>
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="query.keyword" class="filter-input" placeholder="客户名称" clearable :prefix-icon="Search" />
        <el-select v-model="query.clientType" class="filter-select" placeholder="客户类型" clearable><el-option label="企业" value="COMPANY" /><el-option label="个人" value="INDIVIDUAL" /></el-select>
        <el-button :icon="Refresh" @click="reset">重置</el-button>
      </div><span class="muted">共 {{ filtered.length }} 位客户</span>
    </div>
    <div class="table-wrap"><el-table :data="filtered" stripe>
      <el-table-column prop="clientName" label="客户名称" min-width="180" />
      <el-table-column label="类型" width="90"><template #default="{ row }"><el-tag effect="plain">{{ labels.clientType[row.clientType] }}</el-tag></template></el-table-column>
      <el-table-column prop="contactName" label="联系人" width="110" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="updateTime" label="最近更新" width="150" />
      <el-table-column label="操作" width="130" fixed="right"><template #default><div class="action-cell"><el-button link type="primary">查看</el-button><el-button link type="primary">编辑</el-button></div></template></el-table-column>
    </el-table></div>
    <div class="pagination-row"><el-pagination background layout="total, prev, pager, next" :total="filtered.length" :page-size="10" /></div>
    <el-dialog v-model="dialog" title="新增客户" width="min(560px, 92vw)">
      <el-form :model="form" label-position="top"><el-row :gutter="16">
        <el-col :span="16"><el-form-item label="客户名称" required><el-input v-model="form.clientName" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="客户类型" required><el-select v-model="form.clientType"><el-option label="企业" value="COMPANY" /><el-option label="个人" value="INDIVIDUAL" /></el-select></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item></el-col>
        <el-col :span="24"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
        <el-col :span="24"><el-form-item label="地址"><el-input v-model="form.address" /></el-form-item></el-col>
      </el-row></el-form>
      <template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" @click="dialog = false">保存</el-button></template>
    </el-dialog>
  </div>
</template>
