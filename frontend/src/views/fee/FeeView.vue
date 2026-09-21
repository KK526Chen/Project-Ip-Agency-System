<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { cases, fees } from '../../utils/mockData'
import { labels, tagType } from '../../utils/enums'

const query = reactive({ keyword: '', direction: '', status: '' })
const dialog = ref(false)
const form = reactive({ caseId: '', feeType: 'AGENCY', amount: 0, direction: 'RECEIVABLE', status: 'PENDING', payDate: '', remark: '' })
const filtered = computed(() => fees.filter((item) =>
  (!query.keyword || item.caseName.includes(query.keyword)) &&
  (!query.direction || item.direction === query.direction) &&
  (!query.status || item.status === query.status),
))
const total = computed(() => filtered.value.reduce((sum, item) => sum + Number(item.amount), 0))
const reset = () => Object.assign(query, { keyword: '', direction: '', status: '' })
const submit = () => {
  if (!form.caseId || form.amount <= 0) return ElMessage.warning('请填写案件和有效金额')
  ElMessage.success('费用记录已保存')
  dialog.value = false
}
</script>

<template>
  <div class="page">
    <div class="page-heading"><div><h1>费用管理</h1><p>登记案件应收、支出及付款状态</p></div><el-button type="primary" :icon="Plus" @click="dialog = true">新增费用</el-button></div>
    <div class="toolbar"><div class="filters">
      <el-input v-model="query.keyword" class="filter-input" placeholder="搜索案件名称" clearable :prefix-icon="Search" />
      <el-select v-model="query.direction" class="filter-select" placeholder="收支方向" clearable><el-option v-for="(text, value) in labels.direction" :key="value" :label="text" :value="value" /></el-select>
      <el-select v-model="query.status" class="filter-select" placeholder="付款状态" clearable><el-option v-for="(text, value) in labels.feeStatus" :key="value" :label="text" :value="value" /></el-select>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div><span class="muted">当前合计 ¥ {{ total.toLocaleString() }}</span></div>
    <div class="table-wrap"><el-table :data="filtered" stripe>
      <el-table-column prop="caseName" label="所属案件" min-width="230" show-overflow-tooltip />
      <el-table-column label="费用类型" width="100"><template #default="{ row }">{{ labels.feeType[row.feeType] }}</template></el-table-column>
      <el-table-column label="方向" width="90"><template #default="{ row }"><el-tag :type="row.direction === 'RECEIVABLE' ? 'success' : 'warning'" effect="plain">{{ labels.direction[row.direction] }}</el-tag></template></el-table-column>
      <el-table-column label="金额" width="120" align="right"><template #default="{ row }"><strong>¥ {{ Number(row.amount).toLocaleString() }}</strong></template></el-table-column>
      <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="tagType(row.status)" effect="plain">{{ labels.feeStatus[row.status] }}</el-tag></template></el-table-column>
      <el-table-column prop="payDate" label="支付日期" width="110"><template #default="{ row }">{{ row.payDate || '-' }}</template></el-table-column>
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="100" fixed="right"><template #default><div class="action-cell"><el-button link type="primary">编辑</el-button><el-button link type="danger">删除</el-button></div></template></el-table-column>
    </el-table></div>
    <div class="pagination-row"><el-pagination background layout="total, prev, pager, next" :total="filtered.length" :page-size="10" /></div>

    <el-dialog v-model="dialog" title="新增费用" width="min(620px, 94vw)"><el-form :model="form" label-position="top"><el-row :gutter="16">
      <el-col :span="24"><el-form-item label="所属案件" required><el-select v-model="form.caseId" filterable><el-option v-for="item in cases" :key="item.id" :label="item.caseName" :value="item.id" /></el-select></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="费用类型"><el-select v-model="form.feeType"><el-option v-for="(text, value) in labels.feeType" :key="value" :label="text" :value="value" /></el-select></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="金额" required><el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="收支方向"><el-select v-model="form.direction"><el-option v-for="(text, value) in labels.direction" :key="value" :label="text" :value="value" /></el-select></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="支付状态"><el-select v-model="form.status"><el-option v-for="(text, value) in labels.feeStatus" :key="value" :label="text" :value="value" /></el-select></el-form-item></el-col>
      <el-col :span="12"><el-form-item label="支付日期"><el-date-picker v-model="form.payDate" type="date" value-format="YYYY-MM-DD" /></el-form-item></el-col>
      <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item></el-col>
    </el-row></el-form><template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template></el-dialog>
  </div>
</template>
