export const labels = {
  role: { ADMIN: '管理员', AGENT: '代理师', ASSISTANT: '助理' },
  userStatus: { 1: '启用', 0: '停用' },
  clientType: { COMPANY: '企业', INDIVIDUAL: '个人' },
  caseType: { PATENT: '专利', TRADEMARK: '商标', COPYRIGHT: '著作权' },
  caseStatus: { PENDING: '待启动', PROCESSING: '办理中', WAITING_CLIENT: '待客户', WAITING_OFFICIAL: '待官方', COMPLETED: '已完成', TERMINATED: '已终止' },
  priority: { LOW: '低', NORMAL: '普通', HIGH: '高' },
  memberRole: { PRINCIPAL: '主办人', COLLABORATOR: '协办人' },
  taskStatus: { TODO: '待处理', DOING: '进行中', DONE: '已完成' },
  deadlineStatus: { PENDING: '待处理', DONE: '已完成' },
  documentType: { CLIENT: '客户材料', APPLICATION: '申请文件', OFFICIAL: '官方文件', INTERNAL: '内部文档', OTHER: '其他' },
  feeType: { AGENCY: '代理费', OFFICIAL: '官方费', OTHER: '其他' },
  direction: { RECEIVABLE: '应收', EXPENSE: '支出' },
  feeStatus: { PENDING: '待支付', PAID: '已支付' },
}

export const tagType = (value) => ({
  HIGH: 'danger', PROCESSING: 'primary', DOING: 'primary', PENDING: 'warning',
  WAITING_CLIENT: 'warning', WAITING_OFFICIAL: 'warning', COMPLETED: 'success',
  DONE: 'success', PAID: 'success', TERMINATED: 'info', 0: 'info',
}[value] || 'info')
