<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createStage, updateStage, caseChildren } from '../../../api/case'
import { caseWorkflow as wf, fireWorkflow as fire } from '../../../api/forkc'
import { session } from '../../../utils/session'
import { options, text, formatDate } from '../../../utils/enums'
import { ElMessage } from 'element-plus'
import { messageOf } from '../../../utils/request'
const props = defineProps({ caseId: Number }); const emit = defineEmits(['updated'])
const list = ref([]); const flow = ref({}); const open = ref(false); const edit = ref(null)
const form = reactive({ stageType: 'OTHER', stageName: '', status: 'IN_PROGRESS', description: '' })
const load = async () => {
  list.value = (await caseChildren(props.caseId, 'stages', { pageSize: 100 })).data.list
  try { flow.value = (await wf(props.caseId)).data } catch { flow.value = {} }
}
const save = async () => {
  try {
    if (edit.value) await updateStage(edit.value, form); else await createStage(props.caseId, form)
    ElMessage.success('阶段已保存'); open.value = false; edit.value = null; await load(); emit('updated')
  } catch (e) { ElMessage.error(messageOf(e)) }
}
const showEdit = (s) => { if (s.status === 'COMPLETED') return; edit.value = s.id; Object.assign(form, { stageType: s.stageType, stageName: s.stageName, status: s.status, description: s.description || '' }); open.value = true }
const fireEvent = async (event) => { try { await fire(props.caseId, event, {}); ElMessage.success('流程已推进'); await load(); emit('updated') } catch (e) { ElMessage.error(messageOf(e)) } }
onMounted(load)
</script>
<template>
  <section class="tab-panel">
    <div class="tab-actions">
      <el-button v-for="t in (flow.transitions || [])" :key="t.eventCode" @click="fireEvent(t.eventCode)">推进：{{ t.eventCode }}</el-button>
      <el-button v-if="session.user.role !== 'CLIENT'" type="primary" @click="edit=null;Object.assign(form,{stageType:'OTHER',stageName:'',status:'IN_PROGRESS',description:''});open=true">新增阶段</el-button>
    </div>
    <p v-if="flow.instance" class="muted">当前流程状态：{{ flow.instance.currentState }}（版本 {{ flow.instance.versionId }}）</p>
    <el-timeline>
      <el-timeline-item v-for="s in list" :key="s.id" :timestamp="formatDate(s.startTime)" placement="top" :type="s.status==='COMPLETED'?'success':'primary'">
        <article class="timeline-card"><div><b>{{ s.stageName }}</b><el-tag size="small">{{ text('stageStatus', s.status) }}</el-tag></div>
          <p>{{ s.description || text('stageType', s.stageType) }}</p>
          <el-button v-if="session.user.role!=='CLIENT' && s.status!=='COMPLETED'" link type="primary" @click="showEdit(s)">更新</el-button>
        </article>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-if="!list.length" description="暂无阶段记录" />
    <el-dialog v-model="open" :title="edit?'更新案件阶段':'新增案件阶段'" width="min(520px,94vw)">
      <el-form label-position="top">
        <el-form-item label="阶段类型"><el-select v-model="form.stageType"><el-option v-for="o in options('stageType')" :key="o.value" v-bind="o" /></el-select></el-form-item>
        <el-form-item label="阶段名称"><el-input v-model="form.stageName" /></el-form-item>
        <el-form-item v-if="edit" label="状态"><el-select v-model="form.status"><el-option v-for="o in options('stageStatus')" :key="o.value" v-bind="o" /></el-select></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="open=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </section>
</template>
