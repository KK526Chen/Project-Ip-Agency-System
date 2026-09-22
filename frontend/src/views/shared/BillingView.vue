<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBills, createBill, confirmBill, payBill, issueInvoice, listPayments } from '../../api/billing'
import { session } from '../../utils/session'
import { options, text, money, tagType, formatDate } from '../../utils/enums'
import PageHeader from '../../components/PageHeader.vue'
import AppPagination from '../../components/AppPagination.vue'
import { messageOf } from '../../utils/request'

const role = session.user.role
const billPath = role === 'CLIENT' ? '/client/bills' : '/admin/bills'
const paymentPath = role === 'CLIENT' ? '/client/payments' : '/admin/payments'
const state = reactive({ list: [], total: 0, pageNum: 1, pageSize: 10, status: '', loading: false })
const paymentState = reactive({ billId: null, list: [], total: 0, pageNum: 1, pageSize: 5, loading: false })
const open = ref(false)
const form = reactive({ caseId: null, feeType: 'AGENCY', feeItem: '', amount: 0, discountAmount: 0, dueDate: '', remark: '' })

const load = async () => {
  state.loading = true
  try {
    const r = await listBills(billPath, { pageNum: state.pageNum, pageSize: state.pageSize, status: state.status || undefined })
    state.list = r.data.list
    state.total = r.data.total
  } finally {
    state.loading = false
  }
}
const loadPayments = async (billId = paymentState.billId) => {
  if (!billId) return
  paymentState.loading = true
  paymentState.billId = billId
  try {
    const r = await listPayments(paymentPath, { pageNum: paymentState.pageNum, pageSize: paymentState.pageSize, billId })
    paymentState.list = r.data.list
    paymentState.total = r.data.total
  } finally {
    paymentState.loading = false
  }
}
const expandPayments = (row, expandedRows) => {
  if (expandedRows.some((item) => item.id === row.id)) loadPayments(row.id)
}
const save = async () => {
  try {
    await createBill({ ...form, dueDate: form.dueDate || undefined })
    open.value = false
    ElMessage.success('账单已创建')
    load()
  } catch (e) {
    ElMessage.error(messageOf(e))
  }
}
const confirm = async (id) => {
  await confirmBill(id)
  ElMessage.success('账单已确认')
  load()
}
const pay = async (id) => {
  await ElMessageBox.confirm('这是课程演示的模拟支付，不会连接真实支付平台。确认支付？', '模拟支付')
  await payBill(id)
  ElMessage.success('模拟支付成功')
  load()
  loadPayments(id)
}
const invoice = async (id) => {
  await issueInvoice(id)
  ElMessage.success('模拟发票已开具')
  load()
}

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader :title="role === 'CLIENT' ? '费用账单' : '账单管理'" description="账单确认、模拟支付、支付流水与开票状态">
      <el-button v-if="role === 'ADMIN'" type="primary" @click="open = true">创建账单</el-button>
    </PageHeader>

    <div class="toolbar">
      <div class="filters">
        <el-select v-model="state.status" placeholder="账单状态" clearable @change="load">
          <el-option v-for="o in options('billStatus')" :key="o.value" v-bind="o" />
        </el-select>
      </div>
    </div>

    <div class="table-wrap">
      <el-table v-loading="state.loading" :data="state.list" @expand-change="expandPayments">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="nested-panel">
              <div class="nested-panel__head">
                <strong>支付流水</strong>
                <el-button link type="primary" @click="loadPayments(row.id)">刷新流水</el-button>
              </div>
              <el-table v-loading="paymentState.loading && paymentState.billId === row.id" :data="paymentState.billId === row.id ? paymentState.list : []" size="small">
                <el-table-column prop="paymentNo" label="流水号" min-width="220" />
                <el-table-column prop="paymentMethod" label="方式" width="110" />
                <el-table-column label="金额" width="120">
                  <template #default="{ row: payRow }">{{ money(payRow.amount) }}</template>
                </el-table-column>
                <el-table-column label="状态" width="110">
                  <template #default="{ row: payRow }"><el-tag :type="tagType(payRow.status)">{{ payRow.status }}</el-tag></template>
                </el-table-column>
                <el-table-column label="支付时间" width="170">
                  <template #default="{ row: payRow }">{{ formatDate(payRow.paymentTime) }}</template>
                </el-table-column>
              </el-table>
              <el-empty v-if="paymentState.billId === row.id && !paymentState.loading && !paymentState.list.length" description="暂无支付流水" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="billNo" label="账单编号" min-width="190" />
        <el-table-column prop="feeItem" label="费用项目" min-width="160" />
        <el-table-column label="原始金额" width="110"><template #default="{ row }">{{ money(row.amount) }}</template></el-table-column>
        <el-table-column label="应付金额" width="110"><template #default="{ row }">{{ money(row.payableAmount) }}</template></el-table-column>
        <el-table-column label="状态" width="120"><template #default="{ row }"><el-tag :type="tagType(row.status)">{{ text('billStatus', row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="190">
          <template #default="{ row }">
            <el-button v-if="role === 'CLIENT' && row.status === 'PENDING_CONFIRM'" link @click="confirm(row.id)">确认账单</el-button>
            <el-button v-if="role === 'CLIENT' && row.status === 'PENDING_PAYMENT'" link type="success" @click="pay(row.id)">模拟支付</el-button>
            <el-button v-if="role === 'ADMIN' && row.status === 'PAID'" link type="primary" @click="invoice(row.id)">开具发票</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <AppPagination v-model:page="state.pageNum" v-model:size="state.pageSize" :total="state.total" @change="load" />

    <el-dialog v-model="open" title="创建费用账单" width="min(580px, 94vw)">
      <el-form class="form-grid" label-position="top">
        <el-form-item label="案件 ID"><el-input-number v-model="form.caseId" :min="1" /></el-form-item>
        <el-form-item label="费用类型"><el-select v-model="form.feeType"><el-option v-for="o in options('feeType')" :key="o.value" v-bind="o" /></el-select></el-form-item>
        <el-form-item class="span-2" label="费用项目"><el-input v-model="form.feeItem" /></el-form-item>
        <el-form-item label="原始金额"><el-input-number v-model="form.amount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="优惠金额"><el-input-number v-model="form.discountAmount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="缴费截止日"><el-date-picker v-model="form.dueDate" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item class="span-2" label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="save">保存账单</el-button>
      </template>
    </el-dialog>
  </div>
</template>
