<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { homeFor, setSession } from '../../utils/session'
import { messageOf } from '../../utils/request'
const router = useRouter(); const route = useRoute(); const loading = ref(false); const formRef = ref()
const form = reactive({ username: '', password: '' })
const submit = async () => {
  await formRef.value.validate(); loading.value = true
  try { const { data } = await login(form); setSession(data.token, data.user); ElMessage.success('登录成功'); router.replace(route.query.redirect || homeFor(data.user.role)) }
  catch (error) { ElMessage.error(messageOf(error, '登录失败')) } finally { loading.value = false }
}
</script>
<template>
  <div class="login-page">
    <section class="login-brand"><router-link class="login-logo" to="/"><span class="brand-seal">IP</span> 知产云策</router-link><div><span class="eyebrow">IP AGENCY WORKSPACE</span><h1>让每一件创新<br>都有清晰轨迹</h1><p>从委托、审核到授权与缴费，在同一工作空间安全协作。</p></div><small>知识产权代理事务所管理系统 · V2.1</small></section>
    <section class="login-form-side"><el-form ref="formRef" class="login-form surface" :model="form" label-position="top" @keyup.enter="submit"><h2>欢迎回来</h2><p>使用事务所账号登录</p><el-form-item label="用户名" prop="username" :rules="[{required:true,message:'请输入用户名'}]"><el-input v-model="form.username" :prefix-icon="User" autocomplete="username" /></el-form-item><el-form-item label="密码" prop="password" :rules="[{required:true,message:'请输入密码'}]"><el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password autocomplete="current-password" /></el-form-item><el-button class="login-submit" type="primary" :loading="loading" @click="submit">进入工作台</el-button><router-link class="back-link" to="/">返回公开网站</router-link></el-form></section>
  </div>
</template>
