知识产权代理事务所管理系统

产品需求文档（PRD）

版本：V1.1
适用技术栈：Vue 3 + Element Plus + Spring Boot + MyBatis-Plus + MySQL
协作方式：主仓库骨架 + 成员 Fork + Pull Request

# 1. 引言

## 1.1 项目背景

知识产权代理事务所在日常业务中，需要同时管理客户、知识产权案件、代理人员、办理任务、关键期限、业务文档以及费用记录。课程设计目标不是复刻真实事务所全部复杂流程，而是围绕“客户—案件—执行—资料与费用”建立一条完整、可演示、可查询的数据闭环。

## 1.2 项目目标

统一管理客户及其关联案件；

统一管理专利、商标、版权等知识产权代理案件；

支持案件负责人、协办人员、任务、进度和关键期限；

支持案件文档上传下载与费用登记；

提供角色权限和工作台统计；

保证模块边界清晰，适合 5 人 AI Coding 协作开发。

## 1.3 使用角色

| 角色 | 主要职责 |
| --- | --- |
| 管理员 | 用户管理、查看全部业务数据和统计信息 |
| 代理师 | 维护客户和案件，分配成员与任务，管理期限、文档和费用 |
| 助理 | 查看参与案件，处理本人任务，记录进度并上传文档 |

# 2. 技术栈与项目形态

| 层次 | 技术 | 约定 |
| --- | --- | --- |
| 前端 | Vue 3 + Vite + Vue Router + Element Plus + Axios | 目录 frontend，开发端口建议 5173 |
| 后端 | Spring Boot 3 + MyBatis-Plus + SpringDoc OpenAPI | 目录 backend，端口 8080 |
| 数据库 | MySQL 8.x | 数据库 ip_agency_management |
| 构建 | npm / Maven | 前后端独立启动 |

项目采用单仓库（monorepo）组织，前端与后端在同一 GitHub 仓库中，通过固定目录隔离。

project-root/
├─ frontend/
├─ backend/
├─ sql/
│  ├─ schema.sql
│  └─ seed.sql
├─ docs/
│  └─ PRD.docx
├─ README.md
└─ .gitignore

# 3. 主仓库骨架与 UI 基线

## 3.1 前期统一 UI 原则

前期 UI 的目标是“结构统一、组件统一、功能可用”，不追求最终视觉效果。各成员可以根据自己的功能调整表格、表单和弹窗，但不得私自改变全局布局、主题变量和导航结构。最终由组长统一进行视觉优化。

统一后台布局：左侧 Sidebar + 顶部 Header + 右侧 Main Content；

统一使用 Element Plus；

列表页默认采用“页面标题 + 查询栏 + 操作栏 + 表格 + 分页/占位”的结构；

新增/编辑默认使用 Dialog 或 Drawer，不自行引入新的 UI 框架；

公共颜色、间距、字体写入统一样式文件，业务模块只写局部样式；

成员开发阶段允许使用 mock 数据，但数据字段名必须与后端 DTO / API 合同一致。

## 3.2 前端固定目录

frontend/src/
├─ api/                 # 按业务拆分 API 文件
│  ├─ auth.js
│  ├─ client.js
│  ├─ case.js
│  ├─ task.js
│  ├─ progress.js
│  ├─ deadline.js
│  ├─ document.js
│  ├─ fee.js
│  ├─ user.js
│  └─ overview.js
├─ components/
├─ layout/
│  ├─ MainLayout.vue
│  ├─ Sidebar.vue
│  └─ HeaderBar.vue
├─ router/
│  └─ index.js
├─ utils/
│  └─ request.js
├─ views/
│  ├─ dashboard/
│  │  └─ DashboardView.vue
│  ├─ client/
│  │  └─ ClientView.vue
│  ├─ case/
│  │  ├─ CaseView.vue
│  │  ├─ CaseDetailView.vue          # 只负责详情页容器与 Tabs
│  │  └─ detail-tabs/
│  │     ├─ BasicInfoTab.vue
│  │     ├─ CaseMemberTab.vue
│  │     ├─ CaseTaskTab.vue
│  │     ├─ CaseProgressTab.vue
│  │     ├─ CaseDeadlineTab.vue
│  │     ├─ CaseDocumentTab.vue
│  │     └─ CaseFeeTab.vue
│  ├─ task/
│  │  └─ TaskView.vue
│  ├─ deadline/
│  │  └─ DeadlineView.vue
│  ├─ document/
│  │  └─ DocumentView.vue
│  ├─ fee/
│  │  └─ FeeView.vue
│  ├─ user/
│  │  └─ UserView.vue
│  ├─ profile/
│  │  └─ ProfileView.vue
│  └─ auth/
│     └─ LoginView.vue
├─ styles/
│  └─ index.css
├─ App.vue
└─ main.js

## 3.3 前端共享文件约束

| 文件/目录 | 规则 |
| --- | --- |
| layout/ | 组长先搭好。成员不得重构，只能在明确需要时提交小改动 |
| router/index.js | 成员只追加本模块路由，不重排、不重写其他模块 |
| utils/request.js | 请求封装固定后尽量不修改 |
| styles/ | 全局主题由组长维护；业务样式优先 scoped |
| App.vue / main.js | 除接入明确公共能力外不修改 |
| api/*.js | 每个模块只修改自己的 API 文件 |

案件详情页强制采用“CaseDetailView.vue 容器 + detail-tabs/ 独立 Tab 子组件”结构。任何成员不得把多个业务 Tab 的完整实现重新合并进 CaseDetailView.vue；各模块成员只维护自己负责的 Tab 子组件和对应 API 文件。

# 4. 后端工程骨架与编码约束

## 4.1 固定包结构

backend/src/main/java/com/ipagency/
├─ common/
│  ├─ ApiResponse.java
│  └─ BusinessException.java
├─ config/
├─ controller/
├─ service/
│  └─ impl/
├─ mapper/
├─ entity/
├─ dto/
├─ vo/
└─ IpAgencyApplication.java

## 4.2 新建业务类的位置

| 类型 | 位置 | 命名示例 |
| --- | --- | --- |
| Entity | entity/ | ClientInfo.java |
| Mapper | mapper/ | ClientInfoMapper.java |
| Service | service/ | ClientInfoService.java |
| ServiceImpl | service/impl/ | ClientInfoServiceImpl.java |
| Controller | controller/ | ClientController.java |
| 请求 DTO | dto/ | ClientSaveRequest.java |
| 返回 VO | vo/ | ClientDetailVO.java |

## 4.3 后端共享文件约束

ApiResponse、异常处理、跨域配置、MyBatis-Plus 通用配置由组长在骨架阶段确定；

成员不得为了单一模块改统一响应格式；

Controller 仅负责参数接收、权限前置判断和返回结果，业务逻辑放 Service；

不得跨模块直接操作其他模块 Mapper；需要关联数据时优先调用对应 Service；

实体字段与 schema.sql 一致，禁止在个人分支私自增加数据库字段后直接提交。

后端必须执行对象级权限校验（防水平越权）：用户访问案件及其任务、进度、期限、文档、费用前，必须确认其对对应 case_id 具有查看或操作权限；仅隐藏前端按钮不视为安全控制。

所有受保护接口必须从 JWT 中获取当前登录用户身份，不得接受前端传入 currentUserId 作为权限判断依据。

# 5. 数据库设计

## 5.1 核心关系

sys_user
   │
   ├──────────────┐
   ↓              ↓
client_info ──→ case_info
                 │
        ┌────────┼────────┬──────────┬──────────┐
        ↓        ↓        ↓          ↓          ↓
  case_member case_task case_deadline case_document case_fee
                  │
                  ↓
            case_progress

## 5.2 sys_user

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 登录名 |
| password | VARCHAR(100) | NOT NULL | 密码 |
| real_name | VARCHAR(50) | NOT NULL | 姓名 |
| role | VARCHAR(20) | NOT NULL | ADMIN / AGENT / ASSISTANT |
| phone | VARCHAR(20) | NULL | 电话 |
| email | VARCHAR(100) | NULL | 邮箱 |
| status | TINYINT | DEFAULT 1 | 启用状态：1=ENABLED，0=DISABLED |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP; AUTO UPDATE | 最后更新时间 |
| is_deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除：0=未删除，1=已删除 |

## 5.3 client_info

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK, AUTO_INCREMENT | 客户ID |
| client_name | VARCHAR(100) | NOT NULL | 客户名称 |
| client_type | VARCHAR(20) | NOT NULL | COMPANY / INDIVIDUAL |
| contact_name | VARCHAR(50) | NULL | 联系人 |
| phone | VARCHAR(20) | NULL | 联系电话 |
| email | VARCHAR(100) | NULL | 邮箱 |
| address | VARCHAR(255) | NULL | 地址 |
| remark | VARCHAR(500) | NULL | 备注 |
| creator_id | BIGINT | FK | 创建人 |
| create_time | DATETIME |  | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP; AUTO UPDATE | 最后更新时间 |
| is_deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除：0=未删除，1=已删除 |

## 5.4 case_info

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK, AUTO_INCREMENT | 案件ID |
| case_no | VARCHAR(50) | UNIQUE, NOT NULL | 内部编号 |
| case_name | VARCHAR(150) | NOT NULL | 案件名称 |
| client_id | BIGINT | FK, NOT NULL | 客户 |
| case_type | VARCHAR(30) | NOT NULL | PATENT / TRADEMARK / COPYRIGHT |
| business_type | VARCHAR(50) | NOT NULL | 申请/变更/转让/答复等 |
| application_no | VARCHAR(100) | NULL | 官方申请号 |
| principal_id | BIGINT | FK, NOT NULL | 主负责人 |
| status | VARCHAR(30) | NOT NULL | PENDING / PROCESSING / WAITING_CLIENT / WAITING_OFFICIAL / COMPLETED / TERMINATED |
| priority | VARCHAR(20) | DEFAULT NORMAL | 优先级 |
| start_date | DATE | NULL | 开始日期 |
| close_date | DATE | NULL | 结案日期 |
| description | VARCHAR(1000) | NULL | 案件说明 |
| create_time | DATETIME |  | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP; AUTO UPDATE | 最后更新时间 |
| is_deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除：0=未删除，1=已删除 |

## 5.5 case_member

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| case_id | BIGINT | FK, NOT NULL | 案件 |
| user_id | BIGINT | FK, NOT NULL | 成员 |
| member_role | VARCHAR(20) | NOT NULL | PRINCIPAL / COLLABORATOR |
| join_time | DATETIME |  | 加入时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP; AUTO UPDATE | 最后更新时间 |
| is_deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除：0=未删除，1=已删除 |

## 5.6 case_task

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK, AUTO_INCREMENT | 任务ID |
| case_id | BIGINT | FK, NOT NULL | 案件 |
| title | VARCHAR(100) | NOT NULL | 标题 |
| description | VARCHAR(1000) | NULL | 说明 |
| assignee_id | BIGINT | FK, NOT NULL | 负责人 |
| creator_id | BIGINT | FK, NOT NULL | 创建人 |
| status | VARCHAR(20) | NOT NULL | TODO / DOING / DONE |
| priority | VARCHAR(20) | NOT NULL | LOW / NORMAL / HIGH |
| due_date | DATE | NULL | 截止日期 |
| create_time | DATETIME |  | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP; AUTO UPDATE | 最后更新时间 |
| is_deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除：0=未删除，1=已删除 |

## 5.7 case_progress

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| case_id | BIGINT | FK, NOT NULL | 案件 |
| task_id | BIGINT | FK, NULL | 关联任务 |
| content | VARCHAR(1000) | NOT NULL | 进度说明 |
| progress_percent | INT | NULL | 0-100 |
| operator_id | BIGINT | FK, NOT NULL | 记录人 |
| create_time | DATETIME |  | 时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP; AUTO UPDATE | 最后更新时间 |
| is_deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除：0=未删除，1=已删除 |

## 5.8 case_deadline

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| case_id | BIGINT | FK, NOT NULL | 案件 |
| deadline_name | VARCHAR(100) | NOT NULL | 期限名称 |
| deadline_date | DATE | NOT NULL | 截止日 |
| status | VARCHAR(20) | NOT NULL | PENDING / DONE |
| responsible_id | BIGINT | FK, NOT NULL | 负责人 |
| remark | VARCHAR(500) | NULL | 备注 |
| create_time | DATETIME |  | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP; AUTO UPDATE | 最后更新时间 |
| is_deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除：0=未删除，1=已删除 |

## 5.9 case_document

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| case_id | BIGINT | FK, NOT NULL | 案件 |
| document_name | VARCHAR(150) | NOT NULL | 文件名 |
| document_type | VARCHAR(50) | NOT NULL | CLIENT / APPLICATION / OFFICIAL / INTERNAL / OTHER |
| file_path | VARCHAR(500) | NOT NULL | 存储路径 |
| uploader_id | BIGINT | FK, NOT NULL | 上传人 |
| remark | VARCHAR(500) | NULL | 说明 |
| upload_time | DATETIME |  | 上传时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP; AUTO UPDATE | 最后更新时间 |
| is_deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除：0=未删除，1=已删除 |

## 5.10 case_fee

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| case_id | BIGINT | FK, NOT NULL | 案件 |
| fee_type | VARCHAR(30) | NOT NULL | AGENCY / OFFICIAL / OTHER |
| amount | DECIMAL(10,2) | NOT NULL | 金额 |
| direction | VARCHAR(20) | NOT NULL | RECEIVABLE / EXPENSE |
| status | VARCHAR(20) | NOT NULL | PENDING / PAID |
| pay_date | DATE | NULL | 支付日期 |
| operator_id | BIGINT | FK, NOT NULL | 记录人 |
| remark | VARCHAR(500) | NULL | 备注 |
| create_time | DATETIME |  | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP; AUTO UPDATE | 最后更新时间 |
| is_deleted | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除：0=未删除，1=已删除 |

## 5.11 统一逻辑删除、更新时间与枚举约定

所有核心业务表统一包含 update_time 与 is_deleted。业务查询默认只返回 is_deleted=0 的记录；删除接口默认执行逻辑删除，不直接 DELETE 物理数据。MyBatis-Plus 使用 @TableLogic 统一处理逻辑删除。

case_member 必须建立联合唯一键 UNIQUE(case_id, user_id)，避免同一用户被重复加入同一案件。

sys_user.status：1=ENABLED，0=DISABLED；sys_user.role：ADMIN / AGENT / ASSISTANT。

client_info.client_type：COMPANY / INDIVIDUAL。

case_info.case_type：PATENT / TRADEMARK / COPYRIGHT；case_info.status：PENDING / PROCESSING / WAITING_CLIENT / WAITING_OFFICIAL / COMPLETED / TERMINATED；case_info.priority：LOW / NORMAL / HIGH。

case_member.member_role：PRINCIPAL / COLLABORATOR。

case_task.status：TODO / DOING / DONE；case_task.priority：LOW / NORMAL / HIGH。

case_deadline.status：PENDING / DONE。

case_document.document_type：CLIENT / APPLICATION / OFFICIAL / INTERNAL / OTHER。

case_fee.fee_type：AGENCY / OFFICIAL / OTHER；direction：RECEIVABLE / EXPENSE；status：PENDING / PAID。

# 6. 业务流程与页面

录入客户；

创建知识产权案件；

指定主负责人和协办人员；

建立案件任务及负责人；

设置关键期限；

记录办理进度；

上传案件文档；

登记费用；

案件结案；

工作台统计同步更新。

## 6.1 页面结构

工作台
├─ 首页总览
├─ 我的任务
└─ 即将到期
客户与案件
├─ 客户管理
└─ 案件管理
案件执行
├─ 任务管理
├─ 期限管理
├─ 进度记录
└─ 文档管理
财务管理
└─ 费用管理
系统管理
├─ 用户管理
└─ 个人信息

## 6.2 案件详情页

案件详情页作为业务聚合入口，采用 Tab：基本信息 / 案件成员 / 任务 / 办理进度 / 期限 / 文档 / 费用。CaseDetailView.vue 只负责读取 caseId、加载案件概况、渲染 Tabs 和传递必要参数；每个 Tab 必须由 detail-tabs/ 下独立子组件实现，并调用本模块 API。禁止把七个 Tab 的完整业务逻辑集中写入 CaseDetailView.vue。

# 7. 权限设计

| 功能 | 管理员 | 代理师 | 助理 |
| --- | --- | --- | --- |
| 用户管理 | 全部 | 无 | 无 |
| 客户查看 | 全部 | 全部 | 可查看参与案件客户 |
| 客户新增/编辑 | 是 | 是 | 否 |
| 案件查看 | 全部 | 参与/负责案件 | 参与案件 |
| 案件新增/编辑 | 是 | 主办案件 | 否 |
| 案件成员管理 | 是 | 主办案件 | 否 |
| 任务管理 | 是 | 是 | 仅本人任务状态 |
| 进度记录 | 是 | 是 | 是 |
| 期限管理 | 是 | 是 | 查看 |
| 文档上传 | 是 | 是 | 是 |
| 费用管理 | 是 | 是 | 否 |

# 8. API 合同（冻结版）

以下路径作为多人协作时的基础合同。除组长统一调整外，成员不得随意改路径、HTTP 方法或核心字段命名；前端 mock 数据必须与这些接口字段保持一致。

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 认证 | POST | /api/auth/login | 登录 |
| 认证 | GET | /api/auth/profile | 当前用户 |
| 客户 | GET | /api/clients | 列表/筛选 |
| 客户 | GET | /api/clients/{id} | 详情 |
| 客户 | POST | /api/clients | 新增 |
| 客户 | PUT | /api/clients/{id} | 修改 |
| 客户 | DELETE | /api/clients/{id} | 删除 |
| 案件 | GET | /api/cases | 列表/筛选 |
| 案件 | GET | /api/cases/{id} | 详情 |
| 案件 | POST | /api/cases | 新增 |
| 案件 | PUT | /api/cases/{id} | 修改 |
| 案件 | DELETE | /api/cases/{id} | 删除 |
| 案件成员 | GET | /api/cases/{caseId}/members | 成员列表 |
| 案件成员 | POST | /api/cases/{caseId}/members | 添加成员 |
| 案件成员 | DELETE | /api/cases/{caseId}/members/{id} | 移除成员 |
| 任务 | GET | /api/tasks | 列表 |
| 任务 | GET | /api/tasks/{id} | 详情 |
| 任务 | POST | /api/tasks | 新增 |
| 任务 | PUT | /api/tasks/{id} | 修改 |
| 任务 | DELETE | /api/tasks/{id} | 删除 |
| 进度 | GET | /api/progress | 列表 |
| 进度 | POST | /api/progress | 新增 |
| 进度 | DELETE | /api/progress/{id} | 删除 |
| 期限 | GET | /api/deadlines | 列表 |
| 期限 | POST | /api/deadlines | 新增 |
| 期限 | PUT | /api/deadlines/{id} | 修改 |
| 期限 | DELETE | /api/deadlines/{id} | 删除 |
| 文档 | GET | /api/documents | 列表 |
| 文档 | POST | /api/documents/upload | 上传 |
| 文档 | GET | /api/documents/{id}/download | 下载 |
| 文档 | DELETE | /api/documents/{id} | 删除 |
| 费用 | GET | /api/fees | 列表 |
| 费用 | POST | /api/fees | 新增 |
| 费用 | PUT | /api/fees/{id} | 修改 |
| 费用 | DELETE | /api/fees/{id} | 删除 |
| 用户 | GET | /api/users | 列表 |
| 用户 | POST | /api/users | 新增 |
| 用户 | PUT | /api/users/{id} | 修改 |
| 用户 | PUT | /api/users/{id}/status | 启停 |
| 工作台 | GET | /api/overview | 统计 |
| 工作台 | GET | /api/overview/my-tasks | 我的任务 |
| 工作台 | GET | /api/overview/deadlines | 即将到期 |
| 认证 | POST | /api/auth/password | 修改当前用户密码 |
| 用户 | GET | /api/users/selector | 用户下拉；仅返回启用用户，字段 id/realName/role |
| 客户 | GET | /api/clients/selector | 客户下拉；字段 id/clientName/clientType |

## 8.1 统一响应格式

{
  "success": true,
  "message": "操作成功",
  "data": {}
}

## 8.2 标准分页规范

所有列表型 GET 接口默认支持分页参数 pageNum 与 pageSize；pageNum 从 1 开始，pageSize 默认 10。业务筛选参数在分页参数之外按模块追加，例如 keyword、status、caseId。下拉 selector 接口不使用标准分页。

分页响应统一放在 ApiResponse.data 中：
{
  "success": true,
  "message": "操作成功",
  "data": {
    "list": [],
    "total": 0,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 0
  }
}

## 8.3 JWT 鉴权规范

POST /api/auth/login 登录成功后返回 token 与 user；token 为 JWT。

除登录接口外，受保护接口统一使用请求头 Authorization: Bearer <token>。

前端 token 保存于 sessionStorage，并由 utils/request.js 的 Axios 请求拦截器统一附加；收到 401 时清理登录状态并跳转登录页。

后端必须从 JWT 解析当前用户 id、role，不得信任前端提交的 currentUserId、operatorId、creatorId 等字段来判定权限；必要的操作人字段由后端基于 JWT 写入或校验。

## 8.4 文件上传与下载规范

上传接口：POST /api/documents/upload，Content-Type=multipart/form-data。参数：file（MultipartFile，必填）、caseId（Long，必填）、documentType（CLIENT / APPLICATION / OFFICIAL / INTERNAL / OTHER，必填）、remark（String，可选）。uploaderId 不由前端提交，以 JWT 当前用户为准。

下载接口：GET /api/documents/{id}/download，无请求体；必须携带 JWT。后端先校验当前用户对该文档所属案件的访问权限，再返回文件二进制流，并设置 Content-Type 与 Content-Disposition。

前端下载统一使用 Axios blob 响应处理，不在 URL 中携带 token。

## 8.5 前端 mock 规则

mock 对象字段必须使用数据库/API 既定英文名，不创建第二套字段；

页面中展示中文枚举时使用前端映射，不把数据库值改成中文；

API 接通后只替换数据来源，不重写整个页面；

禁止为了让 mock 好写而修改 API 合同。

Selector 接口用于表单下拉选择，不返回完整实体：/api/users/selector 返回 [{id, realName, role}]，默认只含 status=1 且 is_deleted=0 的用户；/api/clients/selector 返回 [{id, clientName, clientType}]，默认只含 is_deleted=0 的客户。可选支持 keyword 模糊查询。

# 9. 功能模块详细需求

## 9.1 登录与个人信息

用户名密码登录；

显示个人资料；

修改密码及联系方式。

## 9.2 工作台

管理员查看客户数、案件数、办理中案件数、待办任务、近期到期；

普通用户查看本人负责案件、本人任务和近期到期事项。

## 9.3 客户管理

客户列表、名称搜索和类型筛选；

新增、编辑客户；

客户详情显示基本资料及关联案件。

## 9.4 案件管理

案件列表支持名称、客户、类型、状态、负责人筛选；

新增/编辑案件；

案件详情聚合成员、任务、进度、期限、文档和费用；

案件编号可采用 PAT-YYYY-XXXX / TM-YYYY-XXXX / CR-YYYY-XXXX。

## 9.5 任务与进度

任务新增、编辑、删除、负责人和截止日期；

助理只修改本人任务状态；

进度记录支持关联案件和可选任务，按时间倒序展示。

## 9.6 期限管理

记录期限名称、日期、负责人和状态；

前端计算剩余天数；

1-7 天标记即将到期，<=0 且未完成标记已到期。

## 9.7 文档管理

上传、列表、下载、删除；

文件类型：客户材料、申请文件、官方文件、内部文档、其他；

数据库保存元信息与文件路径。

## 9.8 费用管理

费用类型：代理费、官方费、其他；

方向：应收、支出；

状态：待支付、已支付；

支持按案件、费用类型、状态筛选。

## 9.9 用户管理

管理员新增、编辑、启停用户；

参与过案件的用户原则上不物理删除。

# 10. 多人协作与 AI Coding 规范

## 10.1 分工边界

| 模块 | 业务范围 | 主要表 | 主要前端目录 |
| --- | --- | --- | --- |
| A 用户与客户 | 登录/个人信息/用户/客户 | sys_user, client_info | views/user, views/profile, views/client |
| B 案件核心 | 案件/案件成员/案件详情骨架 | case_info, case_member | views/case |
| C 执行管理 | 任务/进度/期限 | case_task, case_progress, case_deadline | views/task, views/deadline；case 详情对应子组件 |
| D 文档与费用 | 文档上传下载/费用 | case_document, case_fee | views/document, views/fee；case 详情对应子组件 |
| 组长公共集成 | 主仓库骨架 / schema / 公共配置 / Overview / PR 审核 / 最终 UI | 跨模块只读聚合，不新增独立业务表 | layout, router 基线, views/dashboard, api/overview |

组长负责主仓库骨架、数据库 schema 初版、公共配置、路由/Layout 基线、接口合同审核、PR 审核、最终 UI 统一与集成收尾。若时间允许，组长可以额外承担 schema.sql / seed.sql 的维护，不建议再独立承担一个完整业务模块。 工作台 Overview 作为跨模块聚合接口，也由组长负责：包括 DashboardView.vue、api/overview.js、OverviewController / OverviewService，以及 /api/overview、/api/overview/my-tasks、/api/overview/deadlines 三个接口的最终集成。四名成员只提供本模块可复用的查询能力，不直接修改 Overview 公共聚合逻辑。

## 10.2 PR 冲突预防规则

每人只修改自己业务目录与对应 api 文件；

需要修改公共文件时，在 PR 描述中单独列出“公共文件变更”；

router/index.js 只追加本模块路由，不格式化全文件；

禁止使用 AI 工具一次性重构整个 src 或后端 package；

禁止无必要修改 pom.xml、package.json、全局样式、request.js；

新增依赖必须在 PR 说明理由；

数据库变更先改 docs/数据库变更说明或提交 issue，由组长合并到 schema.sql；

一个 PR 对应一个明确模块/功能，避免跨模块顺手修改；

合并前必须 rebase/merge 最新 main 并本地启动验证。

涉及授权的接口 PR 必须包含后端权限校验；仅通过前端隐藏按钮、禁用控件或过滤数据不能作为权限控制。

案件详情各 Tab 按模块归属独立修改；非本模块成员不得顺手修改其他 detail-tabs 子组件。

## 10.3 Codex 使用规则

每次让 Codex 开发前先读取 PRD、README 和本模块目录；

明确告诉 Codex“不要重构其他模块，不要修改公共文件，除非需求明确”；

要求 Codex 优先复用现有公共组件、响应格式和 API 命名；

生成代码后人工检查 git diff，特别关注 package.json、pom.xml、router、request.js、全局 CSS；

不得直接接受大范围自动格式化产生的无关 diff。

## 10.4 后端防水平越权要求

任何通过 id、caseId、taskId、documentId、feeId 等定位资源的接口，都必须在 Service 层完成对象级权限校验。

管理员可访问全部未删除业务数据；代理师与助理只能访问其有权查看的案件及该案件下的子资源，具体范围以第 7 章权限矩阵为准。

更新、删除操作除“可见性”外还必须验证“可操作性”，例如助理只能更新本人任务状态，不能借助直接构造请求修改他人任务或案件核心字段。

子资源接口应先反查所属 case_id，再统一调用案件权限校验方法；禁止只根据前端传来的 userId 判断所有权。

逻辑删除记录默认视为不存在，权限校验与业务查询均不得返回 is_deleted=1 数据。

# 11. 验收场景

1. 管理员创建代理师和助理账号；

2. 代理师登录并新增客户；

3. 为客户创建发明专利申请案件；

4. 指定主办代理师与协办人员；

5. 建立“收集材料”“撰写申请文件”等任务；

6. 建立关键期限；

7. 助理登录后查看并更新本人任务；

8. 记录办理进度并上传技术交底书；

9. 代理师登记代理服务费与官方申请费；

10. 案件完成后修改为已完成；

11. 工作台统计同步变化。

# 12. 开发顺序与冻结原则

1. 组长创建 GitHub 主仓库；

2. 使用统一提示词生成 frontend / backend 基础项目；

3. 组长运行项目并清理生成结果；

4. 建立 schema.sql、seed.sql 和基础数据；

5. 提交 main 骨架版本并打标签（建议 skeleton-v1）；

6. 4 名成员从该版本 fork / 建分支；

7. 成员先用 mock / 固定接口完成本模块；

8. 逐模块联调真实后端；

9. 组长审核 PR 并合并；

10. 全部功能稳定后，组长统一 UI 风格与答辩展示。

并行开发后原则上冻结：数据库核心表名与字段、API 路径与 HTTP 方法、统一响应结构、前端全局 Layout、后端基础包结构、JWT 请求头、标准分页、selector 返回字段及文件上传参数。确需调整时，由组长统一更新 PRD、前后端提示词与骨架后通知全员。
