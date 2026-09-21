<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Key, User } from '@element-plus/icons-vue'
import { labels } from '../../utils/enums'

const stored = JSON.parse(sessionStorage.getItem('user') || '{}')
const profile = reactive({ username: stored.username || 'admin', realName: stored.realName || '系统管理员', role: stored.role || 'ADMIN', phone: stored.phone || '138****0001', email: stored.email || 'admin@example.com' })
const passwordDialog = ref(false)
const password = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const saveProfile = () => ElMessage.success('个人信息已保存')
const savePassword = () => {
  if (!password.oldPassword || !password.newPassword) return ElMessage.warning('请完整填写密码')
  if (password.newPassword !== password.confirmPassword) return ElMessage.warning('两次新密码不一致')
  ElMessage.success('密码已更新')
  passwordDialog.value = false
}
</script>

<template>
  <div class="page profile-page">
    <div class="page-heading"><div><h1>个人信息</h1><p>查看账号身份并维护联系信息</p></div></div>
    <div class="profile-layout">
      <aside class="profile-aside">
        <div class="profile-avatar">{{ profile.realName.slice(0, 1) }}</div>
        <strong>{{ profile.realName }}</strong>
        <span>@{{ profile.username }}</span>
        <el-tag effect="plain">{{ labels.role[profile.role] }}</el-tag>
      </aside>
      <section class="profile-form-panel">
        <div class="section-title"><h2><el-icon><User /></el-icon> 基本资料</h2><el-button type="primary" @click="saveProfile">保存修改</el-button></div>
        <el-form :model="profile" label-position="top" class="profile-form"><el-row :gutter="18">
          <el-col :xs="24" :sm="12"><el-form-item label="用户名"><el-input v-model="profile.username" disabled /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="角色"><el-input :model-value="labels.role[profile.role]" disabled /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="姓名"><el-input v-model="profile.realName" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="手机号"><el-input v-model="profile.phone" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item></el-col>
        </el-row></el-form>
        <div class="security-row"><div><strong>登录密码</strong><span>建议定期更新密码，保障账号安全</span></div><el-button :icon="Key" @click="passwordDialog = true">修改密码</el-button></div>
      </section>
    </div>
    <el-dialog v-model="passwordDialog" title="修改密码" width="min(460px, 94vw)"><el-form :model="password" label-position="top">
      <el-form-item label="当前密码"><el-input v-model="password.oldPassword" type="password" show-password /></el-form-item>
      <el-form-item label="新密码"><el-input v-model="password.newPassword" type="password" show-password /></el-form-item>
      <el-form-item label="确认新密码"><el-input v-model="password.confirmPassword" type="password" show-password /></el-form-item>
    </el-form><template #footer><el-button @click="passwordDialog = false">取消</el-button><el-button type="primary" @click="savePassword">确认修改</el-button></template></el-dialog>
  </div>
</template>
