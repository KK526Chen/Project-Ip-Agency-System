<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { listCalendars, listCalendarDays, upsertCalendarDay } from '../../api/forkc'
import PageHeader from '../../components/PageHeader.vue'
import { ElMessage } from 'element-plus'
import { messageOf } from '../../utils/request'

const pad = (n) => String(n).padStart(2, '0')
const ymd = (value) => {
  const d = value instanceof Date ? value : new Date(value)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const calendars = ref([])
const calendarId = ref(null)
const cursor = ref(new Date())
const days = ref([])
const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const picked = ref('')
const holidayName = ref('')
const isWorkday = ref(0)

const current = computed(() => calendars.value.find((item) => item.id === calendarId.value) || null)
const monthKey = computed(() => {
  const d = cursor.value instanceof Date ? cursor.value : new Date(cursor.value)
  return `${d.getFullYear()}-${d.getMonth()}`
})
const dayMap = computed(() => {
  const map = {}
  for (const day of days.value) {
    const key = String(day.dayDate || '').slice(0, 10)
    if (key) map[key] = day
  }
  return map
})
const monthOverrides = computed(() => {
  const d = cursor.value instanceof Date ? cursor.value : new Date(cursor.value)
  const prefix = `${d.getFullYear()}-${pad(d.getMonth() + 1)}`
  return days.value
    .filter((item) => String(item.dayDate || '').startsWith(prefix))
    .sort((a, b) => String(a.dayDate).localeCompare(String(b.dayDate)))
})

const kindOf = (dayStr) => {
  const ov = dayMap.value[dayStr]
  if (ov) return Number(ov.isWorkday) === 1 ? 'makeup' : 'holiday'
  const date = new Date(`${dayStr}T00:00:00`)
  const week = date.getDay()
  if (week === 0 || week === 6) return 'weekend'
  return 'work'
}
const markOf = (dayStr) => {
  const ov = dayMap.value[dayStr]
  if (ov) return ov.holidayName || (Number(ov.isWorkday) === 1 ? '补班' : '休息')
  return kindOf(dayStr) === 'weekend' ? '休' : ''
}

const monthRange = () => {
  const d = cursor.value instanceof Date ? cursor.value : new Date(cursor.value)
  const from = new Date(d.getFullYear(), d.getMonth(), -7)
  const to = new Date(d.getFullYear(), d.getMonth() + 1, 14)
  return { from: ymd(from), to: ymd(to) }
}

const loadCalendars = async () => {
  try {
    calendars.value = (await listCalendars({ pageSize: 100 })).data?.list || []
    if (!calendarId.value && calendars.value.length) calendarId.value = calendars.value[0].id
  } catch (e) {
    calendars.value = []
    ElMessage.error(messageOf(e, '无法加载工作日历'))
  }
}

const loadDays = async () => {
  if (!calendarId.value) {
    days.value = []
    return
  }
  loading.value = true
  try {
    const r = await listCalendarDays(calendarId.value, monthRange())
    days.value = Array.isArray(r.data) ? r.data : (r.data?.list || [])
  } catch (e) {
    days.value = []
    ElMessage.error(messageOf(e, '无法加载日历覆盖日'))
  } finally {
    loading.value = false
  }
}

const openDay = (dayStr) => {
  picked.value = dayStr
  const ov = dayMap.value[dayStr]
  holidayName.value = ov?.holidayName || ''
  isWorkday.value = ov ? Number(ov.isWorkday) : (kindOf(dayStr) === 'weekend' ? 0 : 1)
  dialog.value = true
}

const saveDay = async (workday) => {
  if (!calendarId.value || !picked.value) return
  saving.value = true
  try {
    await upsertCalendarDay(calendarId.value, {
      dayDate: picked.value,
      isWorkday: workday,
      holidayName: holidayName.value || (workday ? '补班' : '节假日'),
      overrideType: workday ? 'MAKEUP' : 'HOLIDAY',
    })
    ElMessage.success(workday ? '已设为工作日' : '已设为休息日')
    dialog.value = false
    await loadDays()
  } catch (e) {
    ElMessage.error(messageOf(e, '写入日历失败'))
  } finally {
    saving.value = false
  }
}

watch([calendarId, monthKey], loadDays)
onMounted(loadCalendars)
</script>

<template>
  <div class="page">
    <PageHeader title="工作日历" description="按月份标注节假日与补班；工作日计算会读取这些覆盖，默认周一至周五为工作日">
      <el-select v-if="calendars.length" v-model="calendarId" style="width: 220px">
        <el-option v-for="item in calendars" :key="item.id" :label="item.name || item.code" :value="item.id" />
      </el-select>
    </PageHeader>

    <el-empty v-if="!calendars.length" description="还没有工作日历，请先在库中配置 business_calendar" />

    <div v-else class="cal-layout" v-loading="loading">
      <section class="cal-board">
        <div class="cal-board-head">
          <div>
            <small>{{ current?.code || 'CALENDAR' }}</small>
            <strong>{{ current?.name || '工作日历' }}</strong>
          </div>
          <el-tag :type="current?.status ? 'success' : 'info'" size="small">{{ current?.status ? '启用中' : '未启用' }}</el-tag>
        </div>
        <el-calendar v-model="cursor">
          <template #date-cell="{ data }">
            <button class="cal-cell" :class="[kindOf(data.day), { muted: data.type !== 'current' }]" type="button" @click.stop="openDay(data.day)">
              <span class="cal-num">{{ data.day.slice(-2) }}</span>
              <em v-if="markOf(data.day)">{{ markOf(data.day) }}</em>
            </button>
          </template>
        </el-calendar>
      </section>

      <aside class="cal-side">
        <div class="legend">
          <h3>图例</h3>
          <p><i class="dot work" />工作日</p>
          <p><i class="dot weekend" />默认周末休息</p>
          <p><i class="dot holiday" />节假日覆盖</p>
          <p><i class="dot makeup" />补班覆盖</p>
        </div>
        <div class="override-list">
          <h3>本月覆盖</h3>
          <p v-if="!monthOverrides.length" class="muted">本月尚未单独标注，按周一至周五计工作日。</p>
          <button v-for="item in monthOverrides" :key="item.id" class="override-item" type="button" @click="openDay(String(item.dayDate).slice(0, 10))">
            <b>{{ String(item.dayDate).slice(0, 10) }}</b>
            <span>{{ item.holidayName || (Number(item.isWorkday) === 1 ? '补班' : '休息') }}</span>
          </button>
        </div>
      </aside>
    </div>

    <el-dialog v-model="dialog" :title="picked ? `${picked} 日覆盖` : '日覆盖'" width="min(420px, 92vw)">
      <p class="dialog-lead">点击日期后可覆盖默认周末规则：设为节假日或补班。</p>
      <el-form label-position="top">
        <el-form-item label="显示名称">
          <el-input v-model="holidayName" maxlength="100" placeholder="如 国庆节 / 调休上班" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :loading="saving" @click="saveDay(0)">设为休息日</el-button>
        <el-button type="primary" :loading="saving" @click="saveDay(1)">设为工作日</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.cal-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 18px;
  align-items: start;
}
.cal-board {
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: linear-gradient(180deg, #fffdf8 0%, #fff 48%);
  box-shadow: 0 16px 36px rgba(92, 62, 18, .06);
}
.cal-board-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px 8px;
}
.cal-board-head small {
  display: block;
  color: var(--gold);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1.4px;
}
.cal-board-head strong {
  display: block;
  margin-top: 4px;
  font-family: Georgia, "Songti SC", serif;
  font-size: 22px;
}
.cal-board :deep(.el-calendar) {
  background: transparent;
}
.cal-board :deep(.el-calendar__header) {
  padding: 8px 18px 12px;
  border-bottom: 1px dashed #eadfce;
}
.cal-board :deep(.el-calendar__body) {
  padding: 8px 12px 16px;
}
.cal-cell {
  display: flex;
  width: 100%;
  min-height: 72px;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
  padding: 8px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  cursor: pointer;
  text-align: left;
}
.cal-num {
  font-family: Georgia, serif;
  font-size: 18px;
}
.cal-cell em {
  font-style: normal;
  font-size: 11px;
  color: inherit;
}
.cal-cell.work { color: #1d3a32; }
.cal-cell.weekend { background: #f4f1ea; color: #8a7b66; }
.cal-cell.holiday { background: #fdecea; color: #b42318; }
.cal-cell.makeup { background: #e8f5ef; color: #176b57; }
.cal-cell.muted { opacity: .42; }
.cal-side {
  display: grid;
  gap: 14px;
}
.legend, .override-list {
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #123d33;
  color: #edf4f1;
}
.override-list {
  background: #fff;
  color: var(--ink);
}
.legend h3, .override-list h3 {
  margin: 0 0 12px;
  font-size: 14px;
}
.legend p {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
  color: #c9d9d4;
  font-size: 13px;
}
.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.dot.work { background: #edf4f1; }
.dot.weekend { background: #cbbfa8; }
.dot.holiday { background: #f97066; }
.dot.makeup { background: #67c39a; }
.override-item {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border: 0;
  border-bottom: 1px solid #edf0ef;
  background: transparent;
  cursor: pointer;
  text-align: left;
}
.override-item span { color: var(--muted); font-size: 12px; }
@media (max-width: 960px) {
  .cal-layout { grid-template-columns: 1fr; }
}
</style>
