# 知识产权代理事务所管理系统

## 1. 项目简介

本项目是面向知识产权代理事务所的内部业务管理系统，采用前后端分离的单仓库结构，用于管理客户、案件、任务、进度、期限、文档、费用和团队用户。

## 2. 当前状态

- 后端骨架可以启动并连接已初始化的 Aiven MySQL `defaultdb`。
- 已实现 JWT 登录、当前用户查询、本人密码修改、BCrypt 密码校验、用户/客户选择器、案件权限判断及文档上传下载。
- 多数业务列表仍返回空分页，客户、案件、任务、进度、期限、费用和用户管理的大部分写接口仍返回 HTTP `501 Not Implemented`。
- 工作台统计目前返回占位数据；管理员新增用户接口尚未实现。
- 前端已完成登录、工作台及主要业务页面和案件详情七个页签。
- 前端登录使用真实后端接口，主要业务页面暂时使用 `frontend/src/utils/mockData.js`，页面上的新增、编辑和删除大多尚未持久化。

## 3. 技术栈

后端：Java 17、Spring Boot 3.3.5、MyBatis-Plus 3.5.7、MySQL、JJWT、BCrypt、Springdoc OpenAPI、Maven Wrapper 3.9.9。

前端：Vue 3、Vite 5、Vue Router 4、Element Plus、Axios、JavaScript。

## 4. 目录结构

```text
.
├─ backend/                         Spring Boot 后端与 Maven Wrapper
├─ frontend/                        Vue 3 前端
│  ├─ src/api/                      接口封装
│  ├─ src/layout/                   公共布局
│  ├─ src/views/                    页面与案件详情页签
│  └─ src/utils/mockData.js         临时演示数据
├─ docs/                            PRD 和开发文档
├─ sql/schema.sql                   数据库结构脚本
└─ README.md
```

## 5. 配置与启动

环境要求：JDK 17、Node.js 20 LTS、npm 8 或更高版本。后端统一使用 Maven Wrapper，无需单独安装 Maven。

数据库已经在 Aiven 初始化完成。不要创建数据库，也不要重复执行 `sql/schema.sql` 或测试数据脚本。

首次配置后端时，复制私有配置示例：

```powershell
Copy-Item backend/src/main/resources/application-local.example.yml `
  backend/src/main/resources/application-local.yml
```

在 `application-local.yml` 中填写 Aiven 连接信息，JDBC URL 必须连接 `defaultdb` 并保留 `sslMode=REQUIRED`。该文件已被 `.gitignore` 忽略，禁止提交真实密码。

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

## 6. 测试账号 / 认证说明

- 仓库不保存固定测试账号或明文密码，账号信息以当前 Aiven 数据库为准。
- 数据库密码字段必须保存 BCrypt 哈希，不能保存明文密码。
- 登录接口为 `POST /api/auth/login`。
- 受保护接口使用 `Authorization: Bearer <token>`。
- 前端将 token 和当前用户信息保存在 `sessionStorage`，收到 HTTP `401` 时会清除会话并跳转登录页。
- 当前用户可通过 `POST /api/auth/password` 修改密码，后端会自动生成新的 BCrypt 哈希。

## 7. 团队开发规范

- 后端构建和启动统一优先使用 `mvnw` / `mvnw.cmd`。
- 开发前先确认对应后端接口是否仍返回空分页或 HTTP `501`。
- 前端接入真实接口时逐步替换 `mockData.js`，保持现有字段名、枚举值和 API 路径不变。
- 不得自动重建远程数据库，不得重复执行结构或测试数据脚本。
- 不得提交 `application-local.yml`、真实密码、JWT 密钥、上传文件、`node_modules`、`dist` 或 `target`。
- 提交前至少运行与改动相关的测试；后端使用 `.\mvnw.cmd test`，前端使用 `npm run build`。
