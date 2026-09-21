<script setup>
import { labels } from '../../../utils/enums'

defineProps({ caseInfo: { type: Object, required: true } })
</script>

<template>
  <section class="tab-panel">
    <dl class="description-grid">
      <div class="description-item"><dt>案件编号</dt><dd class="mono">{{ caseInfo.caseNo }}</dd></div>
      <div class="description-item"><dt>申请号</dt><dd>{{ caseInfo.applicationNo || '-' }}</dd></div>
      <div class="description-item"><dt>客户</dt><dd>{{ caseInfo.clientName }}</dd></div>
      <div class="description-item"><dt>案件类型</dt><dd>{{ labels.caseType[caseInfo.caseType] }}</dd></div>
      <div class="description-item"><dt>业务类型</dt><dd>{{ caseInfo.businessType }}</dd></div>
      <div class="description-item"><dt>负责人</dt><dd>{{ caseInfo.principalName }}</dd></div>
      <div class="description-item"><dt>优先级</dt><dd>{{ labels.priority[caseInfo.priority] }}</dd></div>
      <div class="description-item"><dt>立案日期</dt><dd>{{ caseInfo.startDate }}</dd></div>
      <div class="description-item"><dt>结案日期</dt><dd>{{ caseInfo.closeDate || '-' }}</dd></div>
      <div class="description-item"><dt>最后更新</dt><dd>{{ caseInfo.updateTime }}</dd></div>
      <div class="description-item description-wide"><dt>案件说明</dt><dd>{{ caseInfo.description || '-' }}</dd></div>
    </dl>
  </section>
</template>
