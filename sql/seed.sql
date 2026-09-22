-- =========================================================
-- 知识产权代理事务所管理系统
-- Seed Data V2.1
-- Target: MySQL 8.x / Aiven defaultdb
-- 说明：
-- 1. 请先执行 schema.sql。
-- 2. 本文件仅插入演示数据，不删除表、不重建表。
-- 3. 所有测试账号登录密码统一为：123456
-- 4. 数据库中保存的是 BCrypt 哈希，不是明文密码。
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO sys_user
(id, username, password_hash, real_name, role, phone, email, status, last_login_time)
VALUES
(1, 'admin', '$2a$10$Rgi1uGAcAxIXGJmlzWcfqOOd5kpQ01ay3pZH9jZzkYeTZVjzr6GnC', '系统管理员', 'ADMIN', '13800000001', 'admin@example.com', 1, NOW()),
(2, 'agent_zhang', '$2a$10$Rgi1uGAcAxIXGJmlzWcfqOOd5kpQ01ay3pZH9jZzkYeTZVjzr6GnC', '张代理', 'AGENT', '13800000002', 'zhang.agent@example.com', 1, NOW()),
(3, 'agent_li', '$2a$10$Rgi1uGAcAxIXGJmlzWcfqOOd5kpQ01ay3pZH9jZzkYeTZVjzr6GnC', '李代理', 'AGENT', '13800000003', 'li.agent@example.com', 1, NOW()),
(4, 'agent_wang', '$2a$10$Rgi1uGAcAxIXGJmlzWcfqOOd5kpQ01ay3pZH9jZzkYeTZVjzr6GnC', '王代理', 'AGENT', '13800000004', 'wang.agent@example.com', 1, NOW()),
(5, 'client_huawei', '$2a$10$Rgi1uGAcAxIXGJmlzWcfqOOd5kpQ01ay3pZH9jZzkYeTZVjzr6GnC', '华星科技', 'CLIENT', '13800000005', 'contact@huaxing.example.com', 1, NOW()),
(6, 'client_bio', '$2a$10$Rgi1uGAcAxIXGJmlzWcfqOOd5kpQ01ay3pZH9jZzkYeTZVjzr6GnC', '华南生物医药', 'CLIENT', '13800000006', 'contact@bio.example.com', 1, NOW()),
(7, 'client_person', '$2a$10$Rgi1uGAcAxIXGJmlzWcfqOOd5kpQ01ay3pZH9jZzkYeTZVjzr6GnC', '陈明', 'CLIENT', '13800000007', 'chenming@example.com', 1, NOW());

INSERT INTO client_profile
(id, user_id, client_type, client_name, credit_or_id_no, registered_address, contact_address,
 primary_contact_name, primary_contact_phone, primary_contact_email, industry,
 technical_preference, invoice_title, taxpayer_no, bank_name, bank_account)
VALUES
(1, 5, 'COMPANY', '华星科技有限公司', '91440101TEST000001',
 '广州市天河区创新大道1号', '广州市天河区创新大道1号',
 '赵工', '13910000001', 'zhao@huaxing.example.com', '软件互联网',
 '人工智能、图像识别、通信', '华星科技有限公司', '91440101TEST000001',
 '中国银行广州分行', '622200000000000001'),
(2, 6, 'COMPANY', '华南生物医药有限公司', '91440101TEST000002',
 '广州市黄埔区科学城88号', '广州市黄埔区科学城88号',
 '刘经理', '13910000002', 'liu@bio.example.com', '生物医药',
 '生物医药、医疗器械', '华南生物医药有限公司', '91440101TEST000002',
 '工商银行广州分行', '622200000000000002'),
(3, 7, 'INDIVIDUAL', '陈明', '440100199901010001',
 '广州市番禺区大学城', '广州市番禺区大学城',
 '陈明', '13910000003', 'chenming@example.com', '个人',
 '机械设计、智能硬件', '陈明', NULL, NULL, NULL);

INSERT INTO client_contact
(id, client_id, name, position, phone, email, permission_scope, remark)
VALUES
(1, 1, '赵工', '研发负责人', '13910000001', 'zhao@huaxing.example.com', 'ALL_CASES', '负责技术材料'),
(2, 1, '钱会计', '财务负责人', '13910000011', 'finance@huaxing.example.com', 'FEE_ONLY', '仅查看费用'),
(3, 2, '刘经理', '法务负责人', '13910000002', 'liu@bio.example.com', 'ALL_CASES', '主要联系人');

INSERT INTO agent_profile
(id, user_id, employee_no, license_no, department, professional_field, practice_years,
 education, ipc_scope, profile, signature_path)
VALUES
(1, 2, 'A001', 'PATENT-20260001', 'PATENT', '软件、人工智能、通信', 5,
 '硕士', 'G06F,G06N,H04L', '擅长软件与人工智能相关专利申请。', NULL),
(2, 3, 'A002', 'PATENT-20260002', 'PATENT', '机械、自动化、新能源', 7,
 '硕士', 'F16H,B25J,H02J', '擅长机械与自动化领域专利业务。', NULL),
(3, 4, 'A003', 'TRADEMARK-20260001', 'TRADEMARK', '商标、版权', 4,
 '本科', NULL, '负责商标及版权业务。', NULL);

INSERT INTO service_product
(id, service_no, service_name, service_type, target_type, description, process_desc,
 official_fee, agency_fee, estimated_cycle, required_materials, advantages, status)
VALUES
(1, 'SVC-PAT-001', '发明专利申请', 'PATENT_APPLICATION',
 '个人、企业、高校、科研院所',
 '提供发明专利检索、撰写、提交及审查意见答复服务。',
 '需求沟通 -> 技术交底 -> 文件撰写 -> 客户确认 -> 提交 -> 审查跟进',
 900.00, 6000.00, '12-36个月',
 '技术交底书、申请人信息、发明人信息',
 '专业代理人全流程跟进', 1),
(2, 'SVC-TM-001', '商标注册', 'TRADEMARK_REGISTRATION',
 '个人、企业',
 '提供商标检索、分类选择、申请及后续跟进服务。',
 '商标检索 -> 类别确认 -> 材料提交 -> 形式审查 -> 实质审查',
 300.00, 1800.00, '8-12个月',
 '商标图样、申请主体信息',
 '尼斯分类辅助选择', 1),
(3, 'SVC-CR-001', '软件著作权登记', 'COPYRIGHT_REGISTRATION',
 '个人、企业、高校、科研院所',
 '提供软件著作权材料整理和登记服务。',
 '材料准备 -> 文档审核 -> 提交登记 -> 获取证书',
 0.00, 1200.00, '1-2个月',
 '源代码、说明文档、主体证明',
 '流程简洁、材料模板完善', 1);

INSERT INTO success_case
(id, case_no, case_name, service_type, client_industry, technical_field,
 highlights, result, grant_date, description, publish_status)
VALUES
(1, 'PUB-2026-001', 'AI图像识别发明专利授权案例', 'PATENT_APPLICATION',
 '软件互联网', 'G06N 人工智能',
 '针对算法创新点重新组织权利要求层级。',
 '成功授权', '2026-06-15',
 '为软件企业完成从技术交底到授权的全流程代理。', 1),
(2, 'PUB-2026-002', '新能源企业商标注册案例', 'TRADEMARK_REGISTRATION',
 '新能源', '尼斯第9类、第42类',
 '完成多类别商标布局。',
 '注册成功', '2026-07-20',
 '协助企业完成品牌核心类别保护。', 1);

INSERT INTO announcement
(id, announcement_no, title, announcement_type, target_scope, content,
 publish_time, start_date, end_date, is_top, status, publisher_user_id)
VALUES
(1, 'ANN-2026-001', '国庆期间业务受理安排', 'BUSINESS', 'ALL',
 '国庆期间线上委托正常受理，线下业务办理时间将适当调整。',
 NOW(), CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1, 1, 1),
(2, 'ANN-2026-002', '系统维护通知', 'SYSTEM', 'ALL',
 '本周日晚间将进行系统维护，请提前保存业务资料。',
 NOW(), CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 0, 1, 1);

INSERT INTO case_info
(id, case_no, case_name, client_id, service_product_id, case_type, technical_field,
 application_no, principal_agent_id, status, current_stage, priority_level,
 submit_time, accept_time, expected_next_official_date, confidential_review, description)
VALUES
(1, 'PAT-2026-0001', '一种基于深度学习的图像识别方法',
 1, 1, 'INVENTION_PATENT', 'G06N 人工智能',
 'CN202610000001', 1, 'SUBSTANTIVE_EXAM', '实质审查', 'HIGH',
 '2026-08-01 10:00:00', '2026-08-03 09:00:00', '2026-10-10', 0,
 '华星科技人工智能算法相关发明专利。'),
(2, 'PAT-2026-0002', '一种自动化机械夹持装置',
 3, 1, 'UTILITY_MODEL', 'B25J 机器人机械手',
 'CN202620000002', 2, 'PROCESSING', '文件撰写', 'MEDIUM',
 '2026-09-10 14:30:00', '2026-09-11 10:00:00', NULL, 0,
 '个人客户实用新型专利申请。'),
(3, NULL, '一种药物缓释组合物及其制备方法',
 2, 1, 'INVENTION_PATENT', 'A61K 医药制剂',
 NULL, NULL, 'PENDING_REVIEW', '委托审核', 'URGENT',
 '2026-09-20 16:00:00', NULL, NULL, 0,
 '客户新提交的发明专利委托，等待管理员审核。');

INSERT INTO case_party
(id, case_id, party_type, name, id_no, nationality, address, is_primary, remark)
VALUES
(1, 1, 'APPLICANT', '华星科技有限公司', '91440101TEST000001', '中国',
 '广州市天河区创新大道1号', 1, NULL),
(2, 1, 'INVENTOR', '赵明', '440100198801010001', '中国',
 '广州市天河区', 1, '第一发明人'),
(3, 1, 'INVENTOR', '孙强', '440100199001010002', '中国',
 '深圳市南山区', 0, NULL),
(4, 2, 'APPLICANT', '陈明', '440100199901010001', '中国',
 '广州市番禺区大学城', 1, NULL),
(5, 2, 'INVENTOR', '陈明', '440100199901010001', '中国',
 '广州市番禺区大学城', 1, '第一发明人'),
(6, 3, 'APPLICANT', '华南生物医药有限公司', '91440101TEST000002', '中国',
 '广州市黄埔区科学城88号', 1, NULL),
(7, 3, 'INVENTOR', '刘晓', '440100198501010003', '中国',
 '广州市黄埔区', 1, '第一发明人');

INSERT INTO case_priority
(id, case_id, country, priority_no, priority_date)
VALUES
(1, 1, '中国', 'CN202510000001', '2025-10-08');

INSERT INTO case_assignment
(id, case_id, agent_id, assigned_by_user_id, assignment_role, assign_time,
 end_time, reason, is_current)
VALUES
(1, 1, 1, 1, 'PRINCIPAL', '2026-08-03 10:00:00', NULL, '软件领域匹配', 1),
(2, 2, 2, 1, 'PRINCIPAL', '2026-09-11 11:00:00', NULL, '机械领域匹配', 1);

INSERT INTO case_stage
(id, case_id, stage_type, stage_name, start_time, end_time, handler_user_id, status, description)
VALUES
(1, 1, 'SUBMISSION', '委托提交', '2026-08-01 10:00:00', '2026-08-01 10:00:00', 5, 'COMPLETED',
 '客户完成案件委托提交'),
(2, 1, 'ACCEPTANCE', '受理立案', '2026-08-03 09:00:00', '2026-08-03 10:00:00', 1, 'COMPLETED',
 '管理员审核通过并正式立案'),
(3, 1, 'PRELIMINARY_EXAM', '初步审查', '2026-08-20 09:00:00', '2026-09-01 17:00:00', 2, 'COMPLETED',
 '完成形式及初步审查'),
(4, 1, 'SUBSTANTIVE_EXAM', '实质审查', '2026-09-02 09:00:00', NULL, 2, 'IN_PROGRESS',
 '等待并处理审查意见'),
(5, 2, 'SUBMISSION', '委托提交', '2026-09-10 14:30:00', '2026-09-10 14:30:00', 7, 'COMPLETED',
 '个人客户提交委托'),
(6, 2, 'ACCEPTANCE', '受理立案', '2026-09-11 10:00:00', '2026-09-11 11:00:00', 1, 'COMPLETED',
 '管理员完成审核立案'),
(7, 2, 'OTHER', '文件撰写', '2026-09-12 09:00:00', NULL, 3, 'IN_PROGRESS',
 '代理人正在撰写申请文件'),
(8, 3, 'SUBMISSION', '委托提交', '2026-09-20 16:00:00', '2026-09-20 16:00:00', 6, 'COMPLETED',
 '客户完成委托提交');

INSERT INTO case_document
(id, case_id, stage_id, document_no, document_name, document_type, source_type,
 file_path, original_file_name, file_size, mime_type, uploader_user_id,
 review_status, official_issue_date, receive_time, official_deadline,
 fee_amount_extracted, ocr_status, ocr_text, ocr_extracted_json,
 external_system_code, external_document_id, remark)
VALUES
(1, 1, 1, NULL, '技术交底书.docx', 'TECHNICAL_DISCLOSURE', 'CLIENT',
 'uploads/case-1/technical/技术交底书.docx', '技术交底书.docx', 245760,
 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 5,
 'APPROVED', NULL, NULL, NULL, NULL, 'NOT_STARTED', NULL, NULL,
 NULL, NULL, '客户提交技术交底书'),
(2, 1, 4, 'OA-2026-0001', '第一次审查意见通知书.pdf', 'OFFICIAL', 'OFFICIAL',
 'uploads/case-1/official/第一次审查意见通知书.pdf', '第一次审查意见通知书.pdf', 512000,
 'application/pdf', 1,
 'NOT_REQUIRED', '2026-09-18', '2026-09-19 09:00:00', '2026-10-19',
 0.00, 'MANUAL',
 '模拟OCR文本：请申请人在指定期限内答复审查意见。',
 JSON_OBJECT('documentTitle','第一次审查意见通知书','issueDate','2026-09-18','deadline','2026-10-19'),
 'CPC', 'CPC-DOC-2026-0001', '管理员录入官方文件'),
(3, 2, 7, NULL, '实用新型申请文件初稿.docx', 'APPLICATION', 'AGENT',
 'uploads/case-2/application/实用新型申请文件初稿.docx', '实用新型申请文件初稿.docx', 196000,
 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 3,
 'PENDING', NULL, NULL, NULL, NULL, 'NOT_STARTED', NULL, NULL,
 NULL, NULL, '等待内部审核'),
(4, 3, 8, NULL, '药物缓释技术交底书.pdf', 'TECHNICAL_DISCLOSURE', 'CLIENT',
 'uploads/case-3/technical/药物缓释技术交底书.pdf', '药物缓释技术交底书.pdf', 620000,
 'application/pdf', 6,
 'PENDING', NULL, NULL, NULL, NULL, 'NOT_STARTED', NULL, NULL,
 NULL, NULL, '客户提交，等待委托审核');

INSERT INTO deadline_task
(id, case_id, agent_id, document_id, deadline_type, task_name,
 official_deadline, internal_deadline, status, priority, remind_type,
 completed_time, description)
VALUES
(1, 1, 1, 2, 'OFFICE_ACTION_RESPONSE', '答复第一次审查意见',
 '2026-10-19 23:59:59', '2026-10-10 18:00:00',
 'IN_PROGRESS', 'HIGH', 'SYSTEM', NULL,
 '根据审查意见通知书准备答复材料'),
(2, 2, 2, NULL, 'APPLICATION_DRAFT', '完成实用新型申请文件撰写',
 '2026-09-30 18:00:00', '2026-09-27 18:00:00',
 'IN_PROGRESS', 'MEDIUM', 'SYSTEM', NULL,
 '完成权利要求书、说明书和附图'),
(3, 1, 1, NULL, 'ANNUAL_FEE', '缴纳年度专利年费',
 '2027-08-31 23:59:59', '2027-08-20 18:00:00',
 'NOT_STARTED', 'LOW', 'SYSTEM', NULL,
 '年度费用提醒');

INSERT INTO review_record
(id, case_id, target_type, target_id, reviewer_user_id, review_type,
 review_result, review_comment, review_time)
VALUES
(1, 1, 'CASE', 1, 1, 'CASE_ACCEPTANCE', 'APPROVED',
 '资料完整，准予立案。', '2026-08-03 09:30:00'),
(2, 2, 'CASE', 2, 1, 'CASE_ACCEPTANCE', 'APPROVED',
 '资料完整，准予立案。', '2026-09-11 10:30:00'),
(3, 2, 'DOCUMENT', 3, 1, 'INTERNAL_QUALITY', 'MINOR_REVISION',
 '请补充附图标记说明。', '2026-09-20 10:00:00');

INSERT INTO fee_bill
(id, bill_no, case_id, client_id, fee_type, fee_item, amount, discount_amount,
 payable_amount, status, due_date, remark, created_by_user_id)
VALUES
(1, 'BILL-2026-0001', 1, 1, 'OFFICIAL', '发明专利申请官方费用',
 900.00, 0.00, 900.00, 'PAID', '2026-08-10', '官方申请费用', 1),
(2, 'BILL-2026-0002', 1, 1, 'AGENCY', '发明专利代理服务费',
 6000.00, 500.00, 5500.00, 'INVOICED', '2026-08-15', '老客户优惠500元', 1),
(3, 'BILL-2026-0003', 2, 3, 'AGENCY', '实用新型代理服务费',
 3000.00, 0.00, 3000.00, 'PENDING_PAYMENT', '2026-09-30', '等待客户支付', 1);

INSERT INTO payment_record
(id, bill_id, payment_no, payment_method, amount, payment_time,
 status, external_transaction_no, remark)
VALUES
(1, 1, 'PAY-2026-0001', 'SIMULATED', 900.00, '2026-08-05 11:00:00',
 'SUCCESS', 'MOCK-TXN-0001', '课程演示模拟支付'),
(2, 2, 'PAY-2026-0002', 'SIMULATED', 5500.00, '2026-08-06 14:00:00',
 'SUCCESS', 'MOCK-TXN-0002', '课程演示模拟支付');

INSERT INTO invoice_record
(id, bill_id, invoice_no, invoice_type, invoice_title, taxpayer_no,
 amount, issue_time, file_path, status)
VALUES
(1, 2, 'INV-2026-0001', 'NORMAL', '华星科技有限公司', '91440101TEST000001',
 5500.00, '2026-08-07 10:00:00',
 'uploads/invoices/INV-2026-0001.pdf', 'ISSUED');

INSERT INTO notification
(id, user_id, notification_type, title, content, business_type, business_id,
 is_read, read_time, send_channel, send_status)
VALUES
(1, 5, 'OFFICIAL_DOCUMENT', '新官文到达',
 '案件 PAT-2026-0001 收到《第一次审查意见通知书》，请及时查看。',
 'DOCUMENT', 2, 0, NULL, 'SYSTEM', 'SUCCESS'),
(2, 2, 'DEADLINE', '时限任务提醒',
 '案件 PAT-2026-0001 的审查意见答复任务即将到期。',
 'DEADLINE', 1, 0, NULL, 'SYSTEM', 'SUCCESS'),
(3, 7, 'PAYMENT', '账单待支付',
 '案件 PAT-2026-0002 有一笔代理服务费账单待支付。',
 'BILL', 3, 0, NULL, 'SYSTEM', 'SUCCESS'),
(4, 6, 'CASE_STATUS', '案件委托已提交',
 '您的案件《一种药物缓释组合物及其制备方法》正在等待管理员审核。',
 'CASE', 3, 1, '2026-09-20 17:00:00', 'SYSTEM', 'SUCCESS');

INSERT INTO user_device
(id, user_id, device_type, device_token, push_platform, device_name,
 last_active_time, status)
VALUES
(1, 5, 'WEB', 'demo-web-token-client-1', 'WEB_PUSH', '华星科技手机浏览器', NOW(), 1),
(2, 2, 'WEB', 'demo-web-token-agent-1', 'WEB_PUSH', '张代理手机浏览器', NOW(), 1);

INSERT INTO external_case_binding
(id, case_id, external_system_id, external_case_id, external_application_no,
 sync_status, last_sync_time, last_error)
VALUES
(1, 1, 1, 'CPC-CASE-2026-0001', 'CN202610000001', 'SUCCESS',
 '2026-09-19 09:30:00', NULL);

INSERT INTO external_sync_task
(id, external_system_id, business_type, business_id, sync_type,
 request_payload, response_payload, status, retry_count, max_retry_count,
 next_retry_time, last_error, create_time, start_time, finish_time)
VALUES
(1, 1, 'CASE', 1, 'STATUS_QUERY',
 JSON_OBJECT('caseId',1,'applicationNo','CN202610000001'),
 JSON_OBJECT('status','SUBSTANTIVE_EXAM','message','Mock CPC response'),
 'SUCCESS', 0, 3, NULL, NULL,
 '2026-09-19 09:20:00', '2026-09-19 09:20:01', '2026-09-19 09:20:02'),
(2, 1, 'DOCUMENT', 2, 'PULL',
 JSON_OBJECT('externalDocumentId','CPC-DOC-2026-0001'),
 JSON_OBJECT('downloaded',TRUE,'documentName','第一次审查意见通知书'),
 'SUCCESS', 0, 3, NULL, NULL,
 '2026-09-19 09:25:00', '2026-09-19 09:25:01', '2026-09-19 09:25:03');

INSERT INTO operation_log
(id, user_id, username, role, module, operation, business_type, business_id,
 request_method, request_path, ip_address, device_type, user_agent,
 detail_json, result, error_message, create_time)
VALUES
(1, 5, 'client_huawei', 'CLIENT', 'CASE', 'SUBMIT_CASE', 'CASE', 1,
 'POST', '/api/client/cases', '192.168.1.10', 'PC', 'DemoBrowser',
 JSON_OBJECT('caseName','一种基于深度学习的图像识别方法'), 'SUCCESS', NULL,
 '2026-08-01 10:00:00'),
(2, 1, 'admin', 'ADMIN', 'CASE', 'APPROVE_CASE', 'CASE', 1,
 'POST', '/api/admin/cases/1/review', '192.168.1.20', 'PC', 'DemoBrowser',
 JSON_OBJECT('result','APPROVED'), 'SUCCESS', NULL,
 '2026-08-03 09:30:00'),
(3, 1, 'admin', 'ADMIN', 'CASE', 'ASSIGN_AGENT', 'CASE', 1,
 'POST', '/api/admin/cases/1/assign', '192.168.1.20', 'PC', 'DemoBrowser',
 JSON_OBJECT('agentId',1), 'SUCCESS', NULL,
 '2026-08-03 10:00:00'),
(4, 5, 'client_huawei', 'CLIENT', 'NOTIFICATION', 'READ_NOTIFICATION', 'NOTIFICATION', 1,
 'PUT', '/api/notifications/1/read', '192.168.1.11', 'MOBILE', 'MobileDemoBrowser',
 NULL, 'SUCCESS', NULL, '2026-09-20 08:00:00');

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'sys_user' AS table_name, COUNT(*) AS row_count FROM sys_user
UNION ALL
SELECT 'client_profile', COUNT(*) FROM client_profile
UNION ALL
SELECT 'agent_profile', COUNT(*) FROM agent_profile
UNION ALL
SELECT 'service_product', COUNT(*) FROM service_product
UNION ALL
SELECT 'case_info', COUNT(*) FROM case_info
UNION ALL
SELECT 'case_document', COUNT(*) FROM case_document
UNION ALL
SELECT 'deadline_task', COUNT(*) FROM deadline_task
UNION ALL
SELECT 'review_record', COUNT(*) FROM review_record
UNION ALL
SELECT 'fee_bill', COUNT(*) FROM fee_bill
UNION ALL
SELECT 'payment_record', COUNT(*) FROM payment_record
UNION ALL
SELECT 'invoice_record', COUNT(*) FROM invoice_record
UNION ALL
SELECT 'notification', COUNT(*) FROM notification;

-- 测试账号，密码均为 123456：
-- admin
-- agent_zhang
-- agent_li
-- agent_wang
-- client_huawei
-- client_bio
-- client_person
