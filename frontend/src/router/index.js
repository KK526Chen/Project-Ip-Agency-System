import { createRouter, createWebHistory } from 'vue-router'
import { session, homeFor } from '../utils/session'
import MainLayout from '../layout/MainLayout.vue'
import PublicLayout from '../layout/PublicLayout.vue'

const viewModules = import.meta.glob('../views/**/*.vue')
const page = (path) => viewModules[`../views/${path}.vue`]
const roleRoutes = (role) => {
  const lower = role.toLowerCase()
  const common = [
    { path: 'dashboard', component: page('dashboard/DashboardView'), meta: { title: '工作台' } },
    { path: 'cases', component: page('case/CaseListView'), meta: { title: role === 'CLIENT' ? '我的案件' : '案件管理' } },
    { path: 'cases/:id', component: page('case/CaseDetailView'), meta: { title: '案件详情' } },
    { path: 'notifications', component: page('shared/NotificationView'), meta: { title: '消息中心' } },
  ]
  if (role === 'CLIENT') common.push(
    { path: 'profile', component: page('profile/ProfileView'), meta: { title: '客户资料' } },
    { path: 'contacts', component: page('client/ContactView'), meta: { title: '企业联系人' } },
    { path: 'cases/create', component: page('case/CaseFormView'), meta: { title: '提交案件委托' } },
    { path: 'cases/:id/edit', component: page('case/CaseFormView'), meta: { title: '编辑案件委托' } },
    { path: 'bills', component: page('shared/BillingView'), meta: { title: '费用账单' } },
    { path: 'invoices', component: page('shared/InvoiceView'), meta: { title: '发票记录' } },
  )
  if (role === 'AGENT') common.push(
    { path: 'profile', component: page('profile/ProfileView'), meta: { title: '个人资料' } },
    { path: 'deadlines', component: page('shared/DeadlineView'), meta: { title: '我的时限' } },
    { path: 'documents', component: page('shared/DocumentView'), meta: { title: '业务文件' } },
    { path: 'performance', component: page('admin/StatisticsView'), meta: { title: '个人业绩' } },
  )
  if (role === 'ADMIN') common.push(
    { path: 'case-review', component: page('admin/CaseReviewView'), meta: { title: '案件审核' } },
    { path: 'case-assignment', component: page('admin/CaseAssignmentView'), meta: { title: '案件分配' } },
    { path: 'document-review', component: page('admin/DocumentReviewView'), meta: { title: '文件审核' } },
    { path: 'official-documents', component: page('admin/OfficialDocumentView'), meta: { title: '官文录入' } },
    { path: 'deadlines', component: page('shared/DeadlineView'), meta: { title: '时限管理' } },
    { path: 'bills', component: page('shared/BillingView'), meta: { title: '账单管理' } },
    { path: 'invoices', component: page('shared/InvoiceView'), meta: { title: '发票记录' } },
    { path: 'service-products', component: page('admin/ContentManagerView'), meta: { title: '服务产品', resource: 'service-products' } },
    { path: 'success-cases', component: page('admin/ContentManagerView'), meta: { title: '成功案例', resource: 'success-cases' } },
    { path: 'announcements', component: page('admin/ContentManagerView'), meta: { title: '公告管理', resource: 'announcements' } },
    { path: 'users', component: page('admin/UserView'), meta: { title: '用户管理' } },
    { path: 'statistics', component: page('admin/StatisticsView'), meta: { title: '业务统计' } },
    { path: 'external-sync', component: page('admin/ExternalSyncView'), meta: { title: '外部同步' } },
  )
  return { path: `/${lower}`, component: MainLayout, meta: { role }, redirect: `/${lower}/dashboard`, children: common }
}

const routes = [
  { path: '/', component: PublicLayout, children: [
    { path: '', component: page('public/HomeView'), meta: { public: true, title: '首页' } },
    { path: 'services', component: page('public/PublicListView'), meta: { public: true, title: '服务产品', resource: 'services' } },
    { path: 'services/:id', component: page('public/PublicDetailView'), meta: { public: true, title: '服务详情', resource: 'services' } },
    { path: 'success-cases', component: page('public/PublicListView'), meta: { public: true, title: '成功案例', resource: 'success-cases' } },
    { path: 'success-cases/:id', component: page('public/PublicDetailView'), meta: { public: true, title: '案例详情', resource: 'success-cases' } },
    { path: 'announcements', component: page('public/PublicListView'), meta: { public: true, title: '事务所公告', resource: 'announcements' } },
    { path: 'announcements/:id', component: page('public/PublicDetailView'), meta: { public: true, title: '公告详情', resource: 'announcements' } },
  ] },
  { path: '/login', component: page('auth/LoginView'), meta: { public: true, title: '登录' } },
  roleRoutes('CLIENT'), roleRoutes('AGENT'), roleRoutes('ADMIN'),
  { path: '/:pathMatch(.*)*', component: page('shared/NotFoundView'), meta: { public: true, title: '页面不存在' } },
]

const router = createRouter({ history: createWebHistory(), routes, scrollBehavior: () => ({ top: 0 }) })
router.beforeEach((to) => {
  document.title = `${to.meta.title || '知产事务'} · 知产云策`
  if (to.path === '/login' && session.token) return homeFor()
  if (to.meta.public) return true
  if (!session.token || !session.user) return { path: '/login', query: { redirect: to.fullPath } }
  const required = to.matched.find((item) => item.meta.role)?.meta.role
  if (required && required !== session.user.role) return homeFor()
  return true
})
export default router
