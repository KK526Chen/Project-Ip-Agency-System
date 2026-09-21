export const labels = {
  role: { CLIENT: '客户', AGENT: '代理人', ADMIN: '管理员' },
  userStatus: { 1: '启用', 0: '停用' },
  clientType: { INDIVIDUAL: '个人', COMPANY: '企业', UNIVERSITY: '高校', RESEARCH_INSTITUTE: '科研院所' },
  caseType: { INVENTION_PATENT: '发明专利', UTILITY_MODEL: '实用新型', DESIGN_PATENT: '外观设计', TRADEMARK: '商标', COPYRIGHT: '著作权', INVALIDATION: '无效宣告', INFRINGEMENT_LITIGATION: '侵权诉讼', OTHER: '其他' },
  caseStatus: { SUBMITTED: '待提交', PENDING_REVIEW: '待审核', RETURNED: '已退回', PENDING_ASSIGNMENT: '待分配', PROCESSING: '办理中', FORMAL_EXAM: '形式审查', SUBSTANTIVE_EXAM: '实质审查', PRELIMINARY_PASSED: '初审通过', GRANTED: '已授权', REJECTED: '已驳回', REEXAMINATION: '复审中', WITHDRAWN: '已撤回', EXPIRED: '已失效', CLOSED: '已结案' },
  priority: { URGENT: '紧急', HIGH: '高', MEDIUM: '中', LOW: '低' },
  partyType: { APPLICANT: '申请人', INVENTOR: '发明人', DESIGNER: '设计人', RIGHT_HOLDER: '权利人' },
  stageType: { SUBMISSION: '委托提交', ACCEPTANCE: '受理', PRELIMINARY_EXAM: '初审', PUBLICATION: '公开/公告', SUBSTANTIVE_EXAM: '实审', OFFICE_ACTION: '审查意见', CLIENT_RESPONSE: '客户答复', GRANT: '授权/注册', CERTIFICATE: '证书发放', OTHER: '其他' },
  stageStatus: { NOT_STARTED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成' },
  documentType: { TECHNICAL_DISCLOSURE: '技术交底书', TRADEMARK_IMAGE: '商标图样', APPLICATION: '申请文件', OFFICE_ACTION_RESPONSE: '官文答复', OFFICIAL: '官方文件', SUPPLEMENT: '补充材料', INTERNAL: '内部文件', CERTIFICATE: '证书', OTHER: '其他' },
  reviewStatus: { NOT_REQUIRED: '无需审核', PENDING: '待审核', APPROVED: '已通过', MINOR_REVISION: '小幅修改', MAJOR_REVISION: '重大修改', REJECTED: '已拒绝', RESUBMIT_REQUIRED: '需重新提交' },
  deadlineStatus: { NOT_STARTED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成', OVERDUE: '已逾期' },
  feeType: { OFFICIAL: '官方费用', AGENCY: '代理费', EXPEDITE: '加急费', OTHER: '其他' },
  billStatus: { PENDING_CONFIRM: '待确认', PENDING_PAYMENT: '待支付', PAID: '已支付', INVOICED: '已开票', REFUNDED: '已退款', CANCELLED: '已取消' },
  invoiceStatus: { PENDING: '待开具', ISSUED: '已开具', VOID: '已作废' },
  notificationType: { DEADLINE: '时限提醒', OFFICIAL_DOCUMENT: '官文到达', CASE_STATUS: '案件状态', MATERIAL_REVIEW: '材料审核', PAYMENT: '费用通知', SYSTEM: '系统消息' },
  serviceType: { PATENT_APPLICATION: '专利申请', TRADEMARK_REGISTRATION: '商标注册', COPYRIGHT_REGISTRATION: '著作权登记', IP_STANDARD: '知识产权贯标', IP_PROTECTION: '知识产权保护', PATENT_ANALYSIS: '专利分析' },
  announcementType: { POLICY: '政策', BUSINESS: '业务', PROMOTION: '活动', RECRUITMENT: '招聘', SYSTEM: '系统', TRAINING: '培训' },
}
export const options = (group) => Object.entries(labels[group] || {}).map(([value, label]) => ({ value, label }))
export const text = (group, value) => labels[group]?.[value] || value || '—'
export const tagType = (value) => ({ URGENT: 'danger', HIGH: 'danger', OVERDUE: 'danger', REJECTED: 'danger', RETURNED: 'warning', PENDING_REVIEW: 'warning', PENDING_ASSIGNMENT: 'warning', PENDING: 'warning', PENDING_CONFIRM: 'warning', PENDING_PAYMENT: 'warning', PROCESSING: 'primary', IN_PROGRESS: 'primary', FORMAL_EXAM: 'primary', SUBSTANTIVE_EXAM: 'primary', APPROVED: 'success', COMPLETED: 'success', GRANTED: 'success', PAID: 'success', INVOICED: 'success', ISSUED: 'success', CLOSED: 'info', CANCELLED: 'info', 0: 'info' }[value] || 'info')
export const formatDate = (value) => value ? String(value).replace('T', ' ').slice(0, 16) : '—'
export const money = (value) => `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}`
