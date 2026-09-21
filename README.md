# 知识产权代理事务所管理系统

## 项目简介

本项目是面向知识产权代理事务所的数据库课程设计，采用 Vue 3 与 Spring Boot 前后端分离架构，围绕客户、案件、文件、时限、费用、通知和团队用户实现业务管理。

当前版本定位为 **第一阶段课程演示版（Phase 1）**。系统优先保证数据库关系、核心业务流转、对象级权限和历史数据留存，不接入真实支付、税务、OCR、短信或官方申报平台。

## 当前状态

- 支持游客公开访问，以及 `CLIENT`、`AGENT`、`ADMIN` 三类登录角色。
- 已实现 JWT 登录、BCrypt 密码、角色权限和案件对象级访问控制。
- 已实现公开服务产品、成功案例和事务所公告。
- 已实现客户资料、企业联系人、案件委托创建、编辑和提交。
- 已实现管理员案件审核、立案、代理人分配与重新分配。
- 已实现代理人案件处理、阶段时间轴、文件上传下载和时限任务。
- 已实现官文上传与案件关联、人工确认 OCR 字段及站内通知。
- 已实现账单确认、模拟支付、模拟开票和基础统计。
- 前端核心业务均调用真实后端 API，不再使用 `mockData.js`。
- PC 与移动端使用同一套响应式 Web 页面。
- CPC、商标和专利缴费系统已提供 Adapter、同步任务和 Mock 演示，尚未接入官方真实接口。

## 技术栈

- 前端：Vue 3、Vite 5、Vue Router 4、Element Plus、Axios、JavaScript。
- 后端：Java 17、Spring Boot 3.3.5、MyBatis-Plus 3.5.7、MySQL、JJWT、BCrypt、Springdoc OpenAPI。
- 构建：npm、Maven Wrapper 3.9.9。

## 目录结构

```text
.
├─ backend/                    Spring Boot 后端
│  ├─ src/main/java/          控制器、服务、实体、Mapper 与外部 Adapter
│  ├─ src/test/java/          单元测试和数据库集成测试
│  └─ README.md               后端接口、权限及业务约定
├─ frontend/                   Vue 3 前端
│  ├─ src/api/                Axios API 封装
│  ├─ src/components/         通用组件
│  ├─ src/layout/             公开端和工作台布局
│  ├─ src/views/              公开端及三类角色页面
│  └─ README.md               前端运行和移动端说明
├─ docs/PRD_V2.1.md           课程版需求与实现范围
├─ sql/schema.sql             V2.1 数据库结构
└─ sql/seed.sql               演示数据
```

## 环境与配置

建议使用：

- JDK 17
- Node.js 20 LTS 或 22 LTS
- npm 8+
- MySQL 8.x

首次配置后端时复制本地配置示例：

```powershell
Copy-Item backend/src/main/resources/application-local.example.yml `
  backend/src/main/resources/application-local.yml
```

在 `application-local.yml` 中填写数据库连接和 JWT 密钥。该文件已被 `.gitignore` 忽略，不得提交真实密码、密钥或证书。

数据库已经初始化时，不要重复执行 `schema.sql` 或 `seed.sql`。新环境需要初始化时，应先确认目标数据库，再依次执行结构和演示数据脚本。

## 启动项目

启动后端：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

启动前端：

```powershell
cd frontend
npm install
npm run dev
```

访问地址：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`
- Swagger UI：`http://localhost:8080/swagger-ui.html`

移动端局域网测试：

```powershell
cd frontend
npm run dev -- --host 0.0.0.0
```

手机和电脑连接同一局域网后，访问 `http://<电脑局域网IP>:5173`。

## 演示账号

仅在已经执行 `sql/seed.sql` 的演示数据库中可用，统一密码为 `123456`：

| 角色 | 用户名示例 |
| --- | --- |
| 管理员 | `admin` |
| 代理人 | `agent_zhang`、`agent_li`、`agent_wang` |
| 客户 | `client_huawei`、`client_bio`、`client_person` |

这些账号只用于本地或课程演示，部署到其它环境时必须更换密码或禁用。

## 测试与构建

后端离线测试：

```powershell
cd backend
.\mvnw.cmd test
```

连接现有 V2 数据库执行集成测试：

```powershell
cd backend
.\mvnw.cmd '-Dv2.integration=true' test
```

集成测试在事务中运行并回滚，不执行数据库初始化脚本。前端生产构建：

```powershell
cd frontend
npm run build
```

## 第一阶段边界

以下能力采用模拟或预留方式，不作为当前阶段的真实生产能力：

- OCR：保存状态和提取字段，由人工录入或确认。
- 支付：模拟支付成功，不连接支付宝、微信或银行商户系统。
- 发票：保存模拟发票记录，不连接真实税务平台。
- 外部通知：站内通知真实实现，短信、邮件和系统级 Push 未接入。
- 外部系统：通过 Adapter、同步任务和 Mock 演示 CPC、商标及缴费系统交互。
- 报表：提供基础表格统计，未实现 PDF、Excel 导出。

更完整的后端接口与业务状态说明见 [backend/README.md](backend/README.md)，前端运行说明见 [frontend/README.md](frontend/README.md)。

## 版本控制约定

- 应提交源码、SQL、需求文档、Maven Wrapper、`package-lock.json` 和配置示例。
- 不提交 `node_modules`、`dist`、`target`、上传文件和 IDE 本地配置。
- 不提交 `application-local.yml`、真实数据库密码、JWT 密钥或外部系统凭据。
- 提交前检查 `git status`，确认新增源码和删除的 V1 文件都是本次版本的一部分。
- 提交前至少执行后端测试和前端生产构建。
