import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/auth/LoginView.vue'), meta: { public: true, title: '登录' } },
  {
    path: '/', component: MainLayout, redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/dashboard/DashboardView.vue'), meta: { title: '首页总览' } },
      { path: 'clients', component: () => import('../views/client/ClientView.vue'), meta: { title: '客户管理' } },
      { path: 'cases', component: () => import('../views/case/CaseView.vue'), meta: { title: '案件管理' } },
      { path: 'cases/:id', component: () => import('../views/case/CaseDetailView.vue'), meta: { title: '案件详情' } },
      { path: 'tasks', component: () => import('../views/task/TaskView.vue'), meta: { title: '任务管理' } },
      { path: 'deadlines', component: () => import('../views/deadline/DeadlineView.vue'), meta: { title: '期限管理' } },
      { path: 'documents', component: () => import('../views/document/DocumentView.vue'), meta: { title: '文档管理' } },
      { path: 'fees', component: () => import('../views/fee/FeeView.vue'), meta: { title: '费用管理' } },
      { path: 'users', component: () => import('../views/user/UserView.vue'), meta: { title: '用户管理', role: 'ADMIN' } },
      { path: 'profile', component: () => import('../views/profile/ProfileView.vue'), meta: { title: '个人信息' } },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  document.title = `${to.meta.title || '管理后台'} - 知产事务管理`
  if (to.meta.public) return true
  const token = sessionStorage.getItem('token')
  if (!token) return { path: '/login', query: { redirect: to.fullPath } }
  const user = JSON.parse(sessionStorage.getItem('user') || '{}')
  if (to.meta.role && user.role !== to.meta.role) return '/dashboard'
  return true
})

export default router
