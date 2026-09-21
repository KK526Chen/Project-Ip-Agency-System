export const clients = [
  { id: 1, clientName: '星河科技有限公司', clientType: 'COMPANY', contactName: '陈经理', phone: '139****0001', email: 'contact@xinghe.example', address: '上海市浦东新区', remark: '重点企业客户', creatorId: 2, createTime: '2026-09-01 09:20', updateTime: '2026-09-18 16:40' },
  { id: 2, clientName: '远山文化有限公司', clientType: 'COMPANY', contactName: '周女士', phone: '139****0002', email: 'contact@yuanshan.example', address: '杭州市西湖区', remark: '', creatorId: 3, createTime: '2026-09-05 10:10', updateTime: '2026-09-17 11:25' },
  { id: 3, clientName: '刘明', clientType: 'INDIVIDUAL', contactName: '刘明', phone: '139****0003', email: 'liuming@example.com', address: '南京市鼓楼区', remark: '', creatorId: 2, createTime: '2026-09-10 14:30', updateTime: '2026-09-16 09:10' },
]

export const cases = [
  { id: 1, caseNo: 'PAT-2026-0001', caseName: '智能检索方法发明专利申请', clientId: 1, clientName: '星河科技有限公司', caseType: 'PATENT', businessType: '发明专利申请', applicationNo: 'CN2026******', principalId: 2, principalName: '张代理', status: 'PROCESSING', priority: 'HIGH', startDate: '2026-09-01', closeDate: '', description: '核心算法与检索流程专利申请', createTime: '2026-09-01 10:00', updateTime: '2026-09-19 16:20' },
  { id: 2, caseNo: 'TM-2026-0001', caseName: '远山文字商标注册', clientId: 2, clientName: '远山文化有限公司', caseType: 'TRADEMARK', businessType: '商标注册', applicationNo: '', principalId: 3, principalName: '李代理', status: 'WAITING_CLIENT', priority: 'NORMAL', startDate: '2026-09-05', closeDate: '', description: '第 35 类文字商标', createTime: '2026-09-05 11:00', updateTime: '2026-09-18 14:10' },
  { id: 3, caseNo: 'CR-2026-0001', caseName: '摄影作品著作权登记', clientId: 3, clientName: '刘明', caseType: 'COPYRIGHT', businessType: '著作权登记', applicationNo: '', principalId: 2, principalName: '张代理', status: 'PENDING', priority: 'LOW', startDate: '2026-09-10', closeDate: '', description: '系列摄影作品登记', createTime: '2026-09-10 15:00', updateTime: '2026-09-16 09:10' },
]

export const tasks = [
  { id: 1, caseId: 1, caseName: '智能检索方法发明专利申请', title: '收集技术交底材料', description: '联系客户补齐技术材料', assigneeId: 4, assigneeName: '王助理', creatorId: 2, status: 'DOING', priority: 'HIGH', dueDate: '2026-09-25', createTime: '2026-09-02 09:00', updateTime: '2026-09-19 10:30' },
  { id: 2, caseId: 1, caseName: '智能检索方法发明专利申请', title: '撰写权利要求书', description: '完成初稿', assigneeId: 2, assigneeName: '张代理', creatorId: 2, status: 'TODO', priority: 'HIGH', dueDate: '2026-10-05', createTime: '2026-09-03 14:00', updateTime: '2026-09-18 15:20' },
  { id: 3, caseId: 2, caseName: '远山文字商标注册', title: '核对商标类别', description: '确认尼斯分类', assigneeId: 5, assigneeName: '赵助理', creatorId: 3, status: 'DONE', priority: 'NORMAL', dueDate: '2026-09-18', createTime: '2026-09-06 10:00', updateTime: '2026-09-18 13:00' },
]

export const deadlines = [
  { id: 1, caseId: 1, caseName: '智能检索方法发明专利申请', deadlineName: '提交申请文件', deadlineDate: '2026-10-15', status: 'PENDING', responsibleId: 2, responsibleName: '张代理', remark: '内部计划期限', createTime: '2026-09-02 10:00', updateTime: '2026-09-18 10:00' },
  { id: 2, caseId: 2, caseName: '远山文字商标注册', deadlineName: '客户确认标样', deadlineDate: '2026-09-30', status: 'PENDING', responsibleId: 3, responsibleName: '李代理', remark: '', createTime: '2026-09-06 11:00', updateTime: '2026-09-17 12:00' },
]

export const documents = [
  { id: 1, caseId: 1, caseName: '智能检索方法发明专利申请', documentName: '技术交底书-v2.docx', documentType: 'CLIENT', filePath: '/uploads/demo-1.docx', uploaderId: 4, uploaderName: '王助理', remark: '客户修订版', uploadTime: '2026-09-18 15:40', updateTime: '2026-09-18 15:40' },
  { id: 2, caseId: 2, caseName: '远山文字商标注册', documentName: '商标图样.png', documentType: 'APPLICATION', filePath: '/uploads/demo-2.png', uploaderId: 5, uploaderName: '赵助理', remark: '', uploadTime: '2026-09-17 10:20', updateTime: '2026-09-17 10:20' },
]

export const fees = [
  { id: 1, caseId: 1, caseName: '智能检索方法发明专利申请', feeType: 'AGENCY', amount: 8000, direction: 'RECEIVABLE', status: 'PENDING', payDate: '', operatorId: 2, remark: '代理服务费', createTime: '2026-09-02 11:00', updateTime: '2026-09-18 10:00' },
  { id: 2, caseId: 1, caseName: '智能检索方法发明专利申请', feeType: 'OFFICIAL', amount: 950, direction: 'EXPENSE', status: 'PAID', payDate: '2026-09-10', operatorId: 2, remark: '申请官费', createTime: '2026-09-10 09:00', updateTime: '2026-09-10 09:00' },
]

export const users = [
  { id: 1, username: 'admin', realName: '系统管理员', role: 'ADMIN', phone: '138****0001', email: 'admin@example.com', status: 1, createTime: '2026-08-20 09:00' },
  { id: 2, username: 'agent01', realName: '张代理', role: 'AGENT', phone: '138****0002', email: 'agent01@example.com', status: 1, createTime: '2026-08-20 09:10' },
  { id: 4, username: 'assistant01', realName: '王助理', role: 'ASSISTANT', phone: '138****0004', email: 'assistant01@example.com', status: 1, createTime: '2026-08-20 09:20' },
]

export const progress = [
  { id: 1, caseId: 1, taskId: 1, content: '已收到第一版技术交底书，完成内容核查。', progressPercent: 40, operatorId: 4, operatorName: '王助理', createTime: '2026-09-18 15:30' },
  { id: 2, caseId: 1, taskId: 1, content: '已向客户发送补充材料清单。', progressPercent: 60, operatorId: 2, operatorName: '张代理', createTime: '2026-09-19 10:20' },
]

export const members = [
  { id: 1, caseId: 1, userId: 2, realName: '张代理', role: 'AGENT', memberRole: 'PRINCIPAL', joinTime: '2026-09-01 10:00' },
  { id: 2, caseId: 1, userId: 4, realName: '王助理', role: 'ASSISTANT', memberRole: 'COLLABORATOR', joinTime: '2026-09-02 09:00' },
]
