# 知识产权代理事务所管理系统 PRD V2.1

**定位：数据库课程设计核心功能版**  
**依据：课程《知识产权代理事务所管理系统》功能说明及其它说明**  
**技术栈：Vue 3 + Vite + Element Plus + Axios / Spring Boot + MyBatis-Plus / MySQL / JWT + BCrypt**

---

## 1. 项目目标

本项目重点体现**数据库建模、业务数据关系、历史数据留存、索引设计、权限控制、文件元数据管理、移动端通知数据以及外部系统同步设计**。

系统不追求完整实现课程说明中的全部真实第三方能力，而是优先跑通以下核心业务闭环：

**游客查看公开信息 → 客户登录并提交案件委托 → 管理员审核立案并分配代理人 → 代理人处理案件、文件与时限 → 客户查看进度、官文和费用 → 管理员管理账单与统计。**

课程说明中涉及 OCR、真实支付、电子发票、短信/APP Push、CPC/商标/缴费平台真实接口等能力，采用“**数据库建模完整 + 业务流程可演示 + 外部能力接口预留或模拟**”的策略。

---

## 2. 角色与权限

系统包含四类角色：

- **VISITOR 游客**：无需登录，可查看服务产品、成功案例、事务所公告。
- **CLIENT 客户**：维护客户资料、提交案件委托、查看本人案件、进度、官文、时限、账单和通知。
- **AGENT 代理人**：维护个人资料、处理分配案件、上传申请/答复文件、管理时限任务、查看个人业绩。
- **ADMIN 管理员**：审核案件、分配代理人、审核业务文件、管理账单、录入官文、发布公告、查看统计。

权限必须由后端执行对象级校验：

- CLIENT 只能访问自己的客户资料和案件。
- AGENT 只能访问当前分配给自己的案件及相关业务数据。
- ADMIN 可访问全局业务数据。
- VISITOR 仅访问公开接口。

---

## 3. 核心实现范围

### 3.1 必须完整实现

1. 登录认证与角色权限。
2. 游客查看服务产品、成功案例、公告。
3. 客户资料和企业联系人管理。
4. 客户提交案件委托。
5. 管理员审核、立案和代理人分配。
6. 代理人查看并处理分配案件。
7. 案件阶段/进度时间轴。
8. 文件上传、下载及数据库元数据管理。
9. 官文录入及案件关联。
10. 时限任务与临期提醒。
11. 费用账单与模拟支付状态流转。
12. 站内通知。
13. 基础统计查询。
14. PC 和移动端响应式 Web 访问。

### 3.2 简化实现

- OCR：保留 OCR 状态、提取结果字段和接口；基础版本允许人工录入/确认识别结果。
- 在线支付：模拟支付宝/微信/银行支付成功，不连接真实商户系统。
- 电子发票：保存发票记录，可生成模拟文件，不连接真实税务系统。
- APP/短信/邮件通知：真实实现站内通知；外部推送留接口。
- 报表：至少支持表格统计，可选 Excel/CSV 导出。

### 3.3 仅做接口与数据库支持

- 国家知识产权局 CPC/电子申请系统。
- 商标网上申请系统。
- 专利缴费相关外部系统。

真实接入依赖外部系统 API、账号、授权和密钥，因此课程版本通过 Adapter + Mock + 同步任务表演示。

---

## 4. 数据规模与数据库设计约束

课程说明给出的主要规模：

- 注册用户约 5 万。
- 注册客户约 8000 家。
- 代理人约 200 人，管理员约 20 人。
- 管理案件约 20 万件，每年新增约 3 万件。
- 每年产生申请/答复文件约 10 万份。
- 每年接收和处理官方文件约 15 万份。
- 每年产生费用账单约 8 万笔。
- 每年产生时限任务约 20 万条。
- 高峰同时在线约 500 人。
- 案件和官文永久保留。
- 财务数据至少保留 10 年。
- 操作日志至少保留 5 年。

因此数据库设计遵循：

1. 大列表全部分页。
2. 高频筛选和关联字段建立索引。
3. 文件本体不存 MySQL BLOB，只存文件元数据与路径/对象存储 Key。
4. 案件状态关闭不等于删除；核心历史数据原则上不物理删除。
5. 分配、阶段、审核、同步等历史过程独立建表，避免覆盖历史。
6. 外部系统同步采用任务表解耦。

---

## 5. 数据库核心表（24 张）

### 5.1 用户与客户域

#### 1）sys_user
登录用户。

关键字段：

```text
id
username
password_hash
real_name
role              CLIENT / AGENT / ADMIN
phone
email
status
create_time
update_time
is_deleted
```

索引：

```text
UNIQUE(username)
INDEX(role)
INDEX(phone)
INDEX(email)
```

#### 2）client_profile
客户主体资料。

```text
id
user_id
client_type       INDIVIDUAL / COMPANY / UNIVERSITY / RESEARCH_INSTITUTE
client_name
credit_or_id_no
registered_address
contact_address
industry
technical_field
invoice_title
taxpayer_no
bank_name
bank_account
create_time
update_time
is_deleted
```

#### 3）client_contact
企业子联系人。

```text
id
client_id
name
position
phone
email
permission_scope
create_time
update_time
is_deleted
```

#### 4）agent_profile
代理人职业资料。

```text
id
user_id
employee_no
license_no
department
professional_field
practice_years
ipc_scope
education
profile
signature_path
create_time
update_time
is_deleted
```

---

### 5.2 公开数据域

#### 5）service_product
服务产品。

```text
id
service_no
service_name
service_type
target_type
description
process_desc
official_fee
agency_fee
estimated_cycle
required_materials
advantages
status
create_time
update_time
is_deleted
```

#### 6）success_case
成功案例。

```text
id
case_no
case_name
service_type
client_industry
technical_field
highlights
result
grant_date
description
create_time
update_time
is_deleted
```

#### 7）announcement
事务所公告。

```text
id
announcement_no
title
announcement_type
publish_time
start_date
end_date
content
target_scope
is_top
status
create_time
update_time
is_deleted
```

---

### 5.3 案件域

#### 8）case_info
案件主表，预计约 20 万条并持续增长。

```text
id
case_no
case_name
client_id
service_product_id
case_type
technical_field
application_no
principal_agent_id
status
current_stage
submit_time
accept_time
expected_next_official_date
confidential_review
description
create_time
update_time
is_deleted
```

重点索引：

```text
UNIQUE(case_no)
INDEX(client_id)
INDEX(principal_agent_id)
INDEX(status)
INDEX(case_type)
INDEX(application_no)
INDEX(submit_time)
INDEX(expected_next_official_date)
INDEX(client_id, status)
INDEX(principal_agent_id, status)
```

#### 9）case_party
统一保存申请人、发明人、设计人、权利人，减少重复表结构。

```text
id
case_id
party_type         APPLICANT / INVENTOR / DESIGNER / RIGHT_HOLDER
name
id_no
nationality
address
is_primary
create_time
update_time
is_deleted
```

#### 10）case_priority
优先权信息，一案可多条。

```text
id
case_id
country
priority_no
priority_date
create_time
update_time
is_deleted
```

#### 11）case_assignment
案件代理人分配历史。

```text
id
case_id
agent_id
assigned_by
assign_time
end_time
reason
is_current
create_time
update_time
```

代理人调整时关闭旧记录并新增记录，不覆盖历史。

#### 12）case_stage
案件完整进度时间轴。

```text
id
case_id
stage_type
stage_name
start_time
end_time
handler_id
status
description
create_time
update_time
```

典型阶段：委托提交、受理、初审、公开/公告、实审、审查意见、客户答复、授权/注册、证书发放。

#### 13）case_document
统一保存业务文件、技术交底书、商标图样、申请文件、答复文件、官方文件、补充材料、证书等元数据。

```text
id
case_id
document_no
document_name
document_type
source_type
file_path
file_size
mime_type
issue_date
receive_date
official_deadline
uploader_id
review_status
ocr_status
ocr_text
ocr_extracted_json
external_system
external_document_id
create_time
update_time
is_deleted
```

重点索引：

```text
INDEX(case_id)
INDEX(document_type)
INDEX(issue_date)
INDEX(official_deadline)
INDEX(case_id, document_type)
```

#### 14）deadline_task
时限任务，每年约 20 万条。

```text
id
case_id
agent_id
deadline_type
task_name
official_deadline
internal_deadline
status
priority
completed_time
description
create_time
update_time
is_deleted
```

重点索引：

```text
INDEX(case_id)
INDEX(agent_id)
INDEX(status)
INDEX(official_deadline)
INDEX(agent_id, status, official_deadline)
```

#### 15）review_record
统一保存案件委托、申请文件、答复文件和补充材料审核记录。

```text
id
case_id
target_type
target_id
reviewer_id
review_result
review_comment
review_time
create_time
```

---

### 5.4 财务域

#### 16）fee_bill
费用账单。

```text
id
bill_no
case_id
client_id
fee_type
fee_item
amount
discount_amount
payable_amount
status
due_date
remark
create_time
update_time
is_deleted
```

#### 17）payment_record
支付流水。

```text
id
bill_id
payment_no
payment_method
amount
payment_time
status
external_transaction_no
create_time
```

课程版本使用 SIMULATED 模式完成状态流转。

#### 18）invoice_record
发票记录。

```text
id
bill_id
invoice_no
invoice_type
invoice_title
taxpayer_no
amount
issue_time
file_path
status
create_time
```

财务数据不物理删除，按课程要求至少保留 10 年。

---

### 5.5 通知与移动端域

#### 19）notification
站内通知，是移动端支持的核心数据表。

```text
id
user_id
notification_type
title
content
business_type
business_id
is_read
read_time
create_time
```

典型通知：

```text
DEADLINE
OFFICIAL_DOCUMENT
CASE_STATUS
PAYMENT
SYSTEM
```

#### 20）user_device
为未来真实系统级 Push 预留设备信息。

```text
id
user_id
device_type       WEB / ANDROID / IOS
device_token
push_platform
last_active_time
status
create_time
update_time
```

课程版本不要求接真实 FCM/APNs/厂商 Push，但数据库结构支持后续扩展。

---

### 5.6 外部系统域

#### 21）external_system
登记外部系统。

```text
id
system_code       CPC / TRADEMARK / PATENT_PAYMENT
system_name
system_type
base_url
status
create_time
update_time
```

#### 22）external_case_binding
本地案件与外部系统案件的映射。

```text
id
case_id
external_system_id
external_case_id
external_application_no
sync_status
last_sync_time
create_time
update_time
```

#### 23）external_sync_task
外部系统异步/重试同步任务。

```text
id
external_system_id
business_type
business_id
sync_type
request_payload
response_payload
status            PENDING / PROCESSING / SUCCESS / FAILED
retry_count
next_retry_time
create_time
finish_time
```

同步流程：

```text
本地业务操作
→ 先保存 MySQL
→ 创建 external_sync_task
→ ExternalSystemAdapter 调用外部 API
→ SUCCESS / FAILED
→ 失败可重试
```

课程版本可使用 Mock Adapter 模拟 CPC/商标/缴费平台返回。

---

### 5.7 审计域

#### 24）operation_log
操作日志，按课程要求至少保留 5 年。

```text
id
user_id
role
module
operation
business_type
business_id
request_method
request_path
ip_address
device_type
result
create_time
```

建议通过 Spring AOP 自动记录关键增删改、审核、分配、支付、文件操作。

---

## 6. 文件存储设计

文件本体不存 MySQL BLOB。

MySQL 只保存：

```text
document_name
file_path / object_key
file_size
mime_type
case_id
uploader_id
metadata...
```

课程开发环境：

```text
backend/uploads/
├─ 2026/
│  └─ case-10001/
│     ├─ technical/
│     ├─ application/
│     ├─ official/
│     └─ supplement/
```

`backend/uploads/` 必须加入 `.gitignore`。

未来部署可把本地文件存储实现替换为 MinIO / OSS / COS / S3，而数据库结构保持不变。

---

## 7. 外部系统接入方式

采用统一适配器层：

```text
Spring Boot
    ↓
ExternalSystemAdapter
    ├─ CpcAdapter
    ├─ TrademarkAdapter
    └─ PatentPaymentAdapter
    ↓
外部 REST/HTTP API
```

真实接入条件：

- 外部系统提供 API。
- 获得账号/机构授权。
- 获得 API Key、Token、证书或其它凭据。

若课程环境没有真实授权，则实现：

```text
接口定义
+ Mock Adapter
+ external_case_binding
+ external_sync_task
```

演示“提交 → 同步任务 → 模拟外部响应 → 更新同步状态”的完整数据库流程。

---

## 8. PC 与移动端支持

不开发独立 Android/iOS App，采用**响应式 Web/H5**：

```text
PC 浏览器 ─┐
           ├→ Vue 3 → Spring Boot REST API → MySQL
手机浏览器 ┘
```

移动端重点页面：

- 消息中心。
- 时限提醒。
- 官文到达通知。
- 案件状态简要查看。
- 待办任务简要查看（代理人）。

开发演示：电脑运行前后端，手机与电脑连接同一局域网，通过电脑局域网 IP 访问 Vite 前端。

如未来需要系统级 Push，可结合 `user_device` 接入 Web Push / FCM / APNs / 厂商推送；不是本课程核心实现范围。

---

## 9. 主要业务流程

### 9.1 客户委托

```text
CLIENT 登录
→ 维护客户资料
→ 选择服务类型/案件类型
→ 填写案件资料及当事人
→ 上传技术交底书/商标图样
→ 提交委托
→ PENDING_REVIEW
```

### 9.2 管理员审核与分配

```text
ADMIN 审核
→ 审核通过：生成案件编号
→ 创建/更新案件
→ 分配代理人
→ 新增 case_assignment
→ PROCESSING
```

审核不通过时保存 review_record 并退回客户修改。

### 9.3 代理人处理

```text
AGENT 查看本人案件
→ 上传申请/答复文件
→ 更新 case_stage
→ 创建/完成 deadline_task
→ 文件进入审核
```

### 9.4 官文与时限

```text
管理员录入/上传官方文件
→ case_document
→ 关联案件
→ 创建/更新 case_stage
→ 创建 deadline_task
→ 生成 notification
```

### 9.5 费用

```text
ADMIN 创建 fee_bill
→ CLIENT 确认
→ 模拟支付 payment_record
→ bill = PAID
→ invoice_record
```

---

## 10. API 统一规范

### 10.1 响应

```json
{
  "success": true,
  "message": "操作成功",
  "data": {}
}
```

### 10.2 分页

请求：

```text
pageNum=1
pageSize=10
```

响应：

```json
{
  "list": [],
  "total": 0,
  "pageNum": 1,
  "pageSize": 10,
  "pages": 0
}
```

### 10.3 JWT

```text
Authorization: Bearer <token>
```

### 10.4 主要接口组

```text
/api/public/*
/api/auth/*
/api/client/*
/api/agent/*
/api/admin/*
/api/cases/*
/api/documents/*
/api/deadlines/*
/api/bills/*
/api/notifications/*
/api/external-sync/*
```

---

## 11. 前端路由与页面

### 公共端

```text
/services
/success-cases
/announcements
/login
```

### 客户端

```text
/client/dashboard
/client/profile
/client/contacts
/client/cases
/client/cases/create
/client/cases/:id
/client/bills
/client/notifications
```

### 代理人端

```text
/agent/dashboard
/agent/profile
/agent/cases
/agent/cases/:id
/agent/deadlines
/agent/documents
/agent/performance
/agent/notifications
```

### 管理员端

```text
/admin/dashboard
/admin/case-review
/admin/case-assignment
/admin/document-review
/admin/official-documents
/admin/deadlines
/admin/bills
/admin/announcements
/admin/service-products
/admin/success-cases
/admin/users
/admin/statistics
```

移动端使用同一路由和 API，通过响应式布局适配。

---

## 12. 性能设计

对于约 20 万案件和持续增长的文件、时限、日志数据：

1. 所有业务列表分页。
2. 使用 `case_id`、`client_id`、`agent_id`、`status`、`official_deadline` 等索引。
3. 高频组合查询建立组合索引。
4. 使用 MyBatis-Plus 分页，不一次返回全量数据。
5. Spring Boot 使用 HikariCP 数据库连接池。
6. 文件与数据库分离。
7. 统计尽量使用数据库聚合 SQL。
8. 当前规模不需要分库分表、Redis 集群或微服务。

---

## 13. 数据保留与删除策略

- 案件：永久保留，关闭用状态表示，不物理删除。
- 官方文件：永久保留。
- 财务数据：至少 10 年。
- 操作日志：至少 5 年。
- 普通配置/公开内容可逻辑删除。

核心历史表如 `case_assignment`、`case_stage`、`review_record`、`payment_record`、`operation_log` 原则上只新增，不覆盖关键历史。

---

## 14. 5 人模块划分

### 组长

负责：

- `schema_v2.sql` 与测试数据。
- 角色、枚举和 API 契约冻结。
- JWT / 权限公共逻辑。
- 公共 Router / Layout / Axios。
- `operation_log` 公共记录方案。
- 外部系统 Adapter 公共接口。
- Dashboard/Overview 聚合。
- PR 审核、联调、最终 UI。

### 模块 A：公开端 + 客户资料

```text
service_product
success_case
announcement
client_profile
client_contact
```

### 模块 B：客户委托 + 案件核心

```text
case_info
case_party
case_priority
```

### 模块 C：代理人办案 + 阶段/文档/时限

```text
agent_profile
case_assignment
case_stage
case_document
deadline_task
```

### 模块 D：管理员审核 + 财务 + 通知

```text
review_record
fee_bill
payment_record
invoice_record
notification
```

外部系统同步表由组长先建公共骨架，需要具体业务接入时由相关模块调用。

---

## 15. 核心验收闭环

优先跑通：

```text
游客查看服务产品
→ 客户登录并维护资料
→ 客户提交案件委托并上传文件
→ 管理员审核立案
→ 管理员分配代理人
→ 代理人查看案件并更新阶段
→ 管理员/代理人录入官文并创建时限
→ 客户在 PC/手机查看进度和通知
→ 管理员创建账单
→ 客户模拟支付
```

在此基础上再扩展 OCR、导出、系统级 Push、真实外部 API 等非核心能力。

---

## 16. 开发冻结原则

四名成员正式开发前，应由组长先冻结：

1. `docs/PRD_V2.1.md`
2. `sql/schema_v2.sql`
3. V2 测试数据
4. Role / Status / Type 枚举
5. API 基础路径与 DTO 关键字段
6. JWT 权限模型
7. 前端公共路由与 Layout
8. 文件存储接口
9. ExternalSystemAdapter 接口

数据库字段、公共 API、公共配置如确需修改，必须先由组长确认再更新 PRD/SQL。
