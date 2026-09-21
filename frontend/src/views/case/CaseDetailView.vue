<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Edit } from '@element-plus/icons-vue'
import { cases } from '../../utils/mockData'
import { labels, tagType } from '../../utils/enums'
import BasicInfoTab from './components/BasicInfoTab.vue'
import CaseMemberTab from './components/CaseMemberTab.vue'
import CaseTaskTab from './components/CaseTaskTab.vue'
import CaseProgressTab from './components/CaseProgressTab.vue'
import CaseDeadlineTab from './components/CaseDeadlineTab.vue'
import CaseDocumentTab from './components/CaseDocumentTab.vue'
import CaseFeeTab from './components/CaseFeeTab.vue'

const route = useRoute()
const router = useRouter()
const activeTab = ref('basic')
const caseId = computed(() => Number(route.params.id))
const caseInfo = computed(() => cases.find((item) => item.id === caseId.value) || cases[0])
</script>

<template>
  <div class="page">
    <el-button link :icon="ArrowLeft" @click="router.push('/cases')">返回案件列表</el-button>
    <div class="detail-summary">
      <div>
        <span class="mono">{{ caseInfo.caseNo }}</span>
        <h1>{{ caseInfo.caseName }}</h1>
        <div class="summary-meta"><span>客户：{{ caseInfo.clientName }}</span><span>负责人：{{ caseInfo.principalName }}</span><span>立案日期：{{ caseInfo.startDate }}</span></div>
      </div>
      <div class="detail-summary-actions"><el-tag :type="tagType(caseInfo.status)" effect="plain">{{ labels.caseStatus[caseInfo.status] }}</el-tag><el-button :icon="Edit">编辑案件</el-button></div>
    </div>
    <el-tabs v-model="activeTab" class="detail-tabs">
      <el-tab-pane label="基本信息" name="basic"><BasicInfoTab :case-info="caseInfo" /></el-tab-pane>
      <el-tab-pane label="案件成员" name="members"><CaseMemberTab :case-id="caseId" /></el-tab-pane>
      <el-tab-pane label="任务" name="tasks"><CaseTaskTab :case-id="caseId" /></el-tab-pane>
      <el-tab-pane label="进度" name="progress"><CaseProgressTab :case-id="caseId" /></el-tab-pane>
      <el-tab-pane label="期限" name="deadlines"><CaseDeadlineTab :case-id="caseId" /></el-tab-pane>
      <el-tab-pane label="文档" name="documents"><CaseDocumentTab :case-id="caseId" /></el-tab-pane>
      <el-tab-pane label="费用" name="fees"><CaseFeeTab :case-id="caseId" /></el-tab-pane>
    </el-tabs>
  </div>
</template>
