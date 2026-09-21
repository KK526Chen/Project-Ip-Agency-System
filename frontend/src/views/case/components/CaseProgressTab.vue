<script setup>
import { computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { progress } from '../../../utils/mockData'

const props = defineProps({ caseId: { type: Number, required: true } })
const rows = computed(() => progress.filter((item) => item.caseId === props.caseId))
</script>

<template>
  <section class="tab-panel"><div class="tab-actions"><el-button type="primary" :icon="Plus">登记进度</el-button></div>
    <el-timeline v-if="rows.length" class="progress-timeline"><el-timeline-item v-for="item in rows" :key="item.id" :timestamp="item.createTime" placement="top" color="#176b57">
      <div class="progress-entry"><div><strong>{{ item.operatorName }}</strong><p>{{ item.content }}</p></div><el-progress type="circle" :percentage="item.progressPercent" :width="54" :stroke-width="5" /></div>
    </el-timeline-item></el-timeline>
    <el-empty v-else description="暂无进度记录" />
  </section>
</template>
