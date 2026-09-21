<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Lock, Connection } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const response = await login(form)
    sessionStorage.setItem('token', response.data.token)
    sessionStorage.setItem('user', JSON.stringify(response.data.user))
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/dashboard')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败，请检查账号信息')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <section class="login-brand">
      <div class="login-kicker"><el-icon size="20"><Connection /></el-icon><span>IP AGENCY WORKSPACE</span></div>
      <div><h1>知识产权代理事务所管理系统</h1><p>统一管理客户、案件、执行节点、业务文档与费用记录，让每一项专业服务都有清晰轨迹。</p></div>
      <span class="login-footer">IP Agency Management · V1.1</span>
    </section>
    <section class="login-form-side">
      <el-form ref="formRef" class="login-form" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <h2>账号登录</h2><p>进入事务管理工作台</p>
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" :prefix-icon="User" autocomplete="username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password autocomplete="current-password" /></el-form-item>
        <el-button class="login-submit" type="primary" :loading="loading" @click="submit">登录</el-button>
      </el-form>
    </section>
  </div>
</template>
