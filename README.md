# 知识产权代理事务所管理系统

本仓库采用前后端分离的单仓库结构。当前已建立 Spring Boot 后端骨架、MySQL 8 初始化脚本和冻结 API；业务模块仍需由 A/B/C/D 成员继续实现。

## 环境要求

- Java 17
- Maven 3.9+
- MySQL 8.x
- Node.js（前端建立后使用）

## 数据库

项目连接已完成初始化的 Aiven MySQL，数据库名固定为 `defaultdb`。日常启动后端时不要重复执行 `schema.sql` 或 `seed.sql`。

## 后端配置

公共配置位于 `backend/src/main/resources/application.yml`，并默认启用 `local` profile。数据库私有配置放在 `application-local.yml`，该文件已被 Git 忽略；首次配置可复制 `application-local.example.yml` 后填写 Aiven 连接信息。

Aiven JDBC URL 必须使用 `defaultdb` 并保留 `sslMode=REQUIRED`。不要提交真实数据库密码、JWT 密钥或其他生产凭据，也不要在日常启动时重复执行 `schema.sql` 或 `seed.sql`。

## 构建与启动

项目已包含 Maven Wrapper，并固定使用 Maven 3.9.9。团队成员应优先使用 Wrapper，不依赖本机安装的 Maven 版本。

Windows PowerShell：

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

前端（另开一个终端）：

```powershell
cd frontend
npm install
npm run dev
```

前端开发服务器默认运行在 `http://localhost:5173`，并将 `/api` 请求代理到 `http://localhost:8080`。生产构建使用 `npm run build`。

macOS / Linux：

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

启动后可访问：

- API：`http://localhost:8080/api`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 演示账号

`seed.sql` 中所有账号的密码均为 `password`：`admin`、`agent01`、`agent02`、`assistant01`、`assistant02`。这些账号仅用于课程演示，不得用于真实系统。

## 接口约定

- 登录：`POST /api/auth/login`。
- 受保护接口使用 `Authorization: Bearer <token>`。
- `pageNum` 从 1 开始，默认 1；`pageSize` 默认 10；selector 不分页。
- 统一响应字段：`success/message/data`。
- 统一分页字段：`list/total/pageNum/pageSize/pages`。
- 9 张核心表均采用 `is_deleted` 逻辑删除，常规查询不返回已删除数据。

## 当前骨架范围

登录、JWT 拦截、当前用户、用户/客户 selector、案件对象级权限和文档上传下载已建立基础实现。其余冻结接口已暴露在 Controller 中，尚未实现的写操作返回 HTTP 501，供各模块在不改变路径和响应合同的前提下继续填充。

公共稳定文件包括 `pom.xml`、`application.yml`、`common/`、`config/`、JWT 相关类、`CaseAccessService`、Overview 骨架和现有数据库合同。成员开发时不要重构这些文件。
