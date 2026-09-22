<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listWorkflows, createWorkflow, newWorkflowVersion, publishWorkflowVersion } from '../../api/forkc'
import PageHeader from '../../components/PageHeader.vue'
import { ElMessage } from 'element-plus'
const list = ref([]); const form = reactive({ code: '', name: '', businessType: 'INVENTION_PATENT' })
const load = async () => { list.value = (await listWorkflows({ pageSize: 100 })).data.list }
onMounted(load)
</script>
<template>
  <div class="page"><PageHeader title="流程定义" description="已发布版本不可改；新案使用最新 published 版本，旧案保留原 versionId" />
    <el-form inline><el-input v-model="form.code" placeholder="编码" /><el-input v-model="form.name" placeholder="名称" /><el-button type="primary" @click="createWorkflow(form).then(load)">新建</el-button></el-form>
    <el-table :data="list"><el-table-column prop="code" label="编码" /><el-table-column prop="name" label="名称" /><el-table-column prop="businessType" label="业务类型" />
      <el-table-column width="220"><template #default="{row}"><el-button link @click="newWorkflowVersion(row.id).then(()=>ElMessage.success('已新建草稿版本'))">新版本</el-button></template></el-table-column>
    </el-table>
  </div>
</template>
