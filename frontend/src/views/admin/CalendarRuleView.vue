<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listDeadlineRules, saveDeadlineRule } from '../../api/forkc'
import PageHeader from '../../components/PageHeader.vue'
import { ElMessage } from 'element-plus'
import { messageOf } from '../../utils/request'
import { options, text } from '../../utils/enums'

const TRIGGERS = [
  { value: 'OA_ISSUED', label: '官文下发' },
  { value: 'APPLICATION_FILED', label: '申请递交' },
  { value: 'GRANT', label: '授权公告' },
  { value: 'CLIENT_CONFIRM', label: '客户确认' },
  { value: 'FORMAL_EXAM', label: '进入形审' },
]
const DAY_TYPES = [
  { value: 'CALENDAR', label: '自然日' },
  { value: 'WORKDAY', label: '工作日' },
]
const POLICIES = [
  { value: 'SKIP_HOLIDAY', label: '遇节假日顺延' },
  { value: 'KEEP_DATE', label: '固定日期不顺延' },
  { value: 'INTERNAL_ONLY', label: '仅内部节点' },
]

const blank = () => ({
  id: null,
  ruleCode: '',
  businessType: 'INVENTION_PATENT',
  triggerEvent: 'OA_ISSUED',
  baseDays: 30,
  dayType: 'CALENDAR',
  internalOffsetDays: 7,
  adjustmentPolicy: 'SKIP_HOLIDAY',
  effectiveFrom: '',
  effectiveTo: '',
})

const rules = ref([])
const loading = ref(false)
const saving = ref(false)
const form = reactive(blank())

const load = async () => {
  loading.value = true
  try {
    rules.value = (await listDeadlineRules({ pageSize: 100 })).data?.list || []
  } catch (e) {
    rules.value = []
    ElMessage.error(messageOf(e, '无法加载时限规则'))
  } finally {
    loading.value = false
  }
}

const reset = () => Object.assign(form, blank())

const edit = (row) => {
  Object.assign(form, blank(), {
    id: row.id,
    ruleCode: row.ruleCode || '',
    businessType: row.businessType || 'INVENTION_PATENT',
    triggerEvent: row.triggerEvent || 'OA_ISSUED',
    baseDays: row.baseDays ?? 30,
    dayType: row.dayType || 'CALENDAR',
    internalOffsetDays: row.internalOffsetDays ?? 0,
    adjustmentPolicy: row.adjustmentPolicy || 'SKIP_HOLIDAY',
    effectiveFrom: row.effectiveFrom || '',
    effectiveTo: row.effectiveTo || '',
  })
}

const save = async () => {
  if (!form.ruleCode?.trim()) {
    ElMessage.warning('请填写规则编码')
    return
  }
  saving.value = true
  try {
    await saveDeadlineRule({ ...form })
    ElMessage.success(form.id ? '规则已更新' : '规则已新增')
    reset()
    await load()
  } catch (e) {
    ElMessage.error(messageOf(e, '保存规则失败'))
  } finally {
    saving.value = false
  }
}

const triggerLabel = (value) => TRIGGERS.find((item) => item.value === value)?.label || value || '—'
const dayTypeLabel = (value) => DAY_TYPES.find((item) => item.value === value)?.label || value || '—'
const policyLabel = (value) => POLICIES.find((item) => item.value === value)?.label || value || '—'

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader title="时限规则" description="按业务类型配置触发事件、基准天数和日类型；规则只做演示配置，不硬编码真实法定期限">
      <el-button v-if="form.id" @click="reset">取消编辑</el-button>
    </PageHeader>

    <el-form class="surface business-form" label-position="top" :model="form">
      <h2>{{ form.id ? `编辑规则 #${form.id}` : '新增时限规则' }}</h2>
      <div class="form-grid">
        <el-form-item label="规则编码" required>
          <el-input v-model="form.ruleCode" maxlength="80" placeholder="如 OA_RESPONSE_RULE_V1" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="form.businessType">
            <el-option v-for="o in options('caseType')" :key="o.value" v-bind="o" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发事件">
          <el-select v-model="form.triggerEvent">
            <el-option v-for="o in TRIGGERS" :key="o.value" v-bind="o" />
          </el-select>
        </el-form-item>
        <el-form-item label="日类型">
          <el-radio-group v-model="form.dayType">
            <el-radio-button v-for="o in DAY_TYPES" :key="o.value" :value="o.value">{{ o.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="基准天数">
          <el-input-number v-model="form.baseDays" :min="1" :max="365" />
        </el-form-item>
        <el-form-item label="内部提前天数">
          <el-input-number v-model="form.internalOffsetDays" :min="0" :max="180" />
        </el-form-item>
        <el-form-item label="调整策略">
          <el-select v-model="form.adjustmentPolicy">
            <el-option v-for="o in POLICIES" :key="o.value" v-bind="o" />
          </el-select>
        </el-form-item>
        <el-form-item label="生效区间">
          <el-date-picker
            v-model="form.effectiveFrom"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="生效起"
            style="width: 48%"
          />
          <el-date-picker
            v-model="form.effectiveTo"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="生效止"
            style="width: 48%; margin-left: 4%"
          />
        </el-form-item>
      </div>
      <div class="form-footer">
        <el-button @click="reset">清空</el-button>
        <el-button type="primary" :loading="saving" @click="save">{{ form.id ? '保存修改' : '新增规则' }}</el-button>
      </div>
    </el-form>

    <div class="table-wrap" style="margin-top: 18px">
      <el-table v-loading="loading" :data="rules">
        <el-table-column prop="ruleCode" label="规则编码" min-width="180" />
        <el-table-column label="业务类型" width="120">
          <template #default="{ row }">{{ text('caseType', row.businessType) }}</template>
        </el-table-column>
        <el-table-column label="触发事件" width="120">
          <template #default="{ row }">{{ triggerLabel(row.triggerEvent) }}</template>
        </el-table-column>
        <el-table-column label="基准天数" width="100" align="center">
          <template #default="{ row }">{{ row.baseDays ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="日类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.dayType === 'WORKDAY' ? 'success' : 'info'" size="small">{{ dayTypeLabel(row.dayType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内部提前" width="100" align="center">
          <template #default="{ row }">{{ row.internalOffsetDays ?? 0 }} 天</template>
        </el-table-column>
        <el-table-column label="调整策略" min-width="140">
          <template #default="{ row }">{{ policyLabel(row.adjustmentPolicy) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
