# 通用后台管理系统模板

这是一个可直接二次开发的前后端分离后台管理系统模板。前端基于
[Art Design Pro](https://github.com/Daymychen/art-design-pro) 的 Vue 3 界面与动态路由能力，后端使用
Spring Boot、MyBatis-Plus、Spring Security、JWT、Flyway 和 MySQL。

当前版本：`1.1.0`。优化范围和验收步骤见 [优化实施计划](docs/优化实施计划.md)。升级需执行新增 V7 迁移并重新登录。

## 已实现范围

- JWT 登录、刷新令牌、当前用户信息
- 后端动态菜单和按钮权限
- 用户查询、新增、编辑、注销
- 角色查询、新增、编辑、删除
- 角色菜单授权
- 菜单/按钮树查询、新增、编辑、删除
- 个人资料维护和登录密码修改
- 公告分页查询、新增、编辑、发布、撤回、置顶和删除
- 操作与登录日志统一采集、分页筛选、单条删除和清空
- 文件上传、分页查询、下载和物理删除，包含类型、大小和路径安全校验
- 消息发送、个人收件箱、未读统计、单条已读和全部已读
- 顶部消息下拉、未读红点以及中英文管理页面
- 统一响应、错误分类、可检索错误编号、参数校验和全局异常处理
- MySQL 表结构与种子数据的 Flyway 迁移
- 前端开发代理、生产环境 API 配置

## 目录结构

```text
manage-system/
├─ frontend/                 # Vue 3 + TypeScript + Vite + Element Plus
├─ backend/                  # Spring Boot + MyBatis-Plus
├─ docs/                     # 中文项目文档
└─ sql/                      # 手动创建数据库入口
```

## 本地启动

### 1. 创建数据库

在项目根目录执行：

```powershell
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS manage_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

只需要创建空数据库。后端首次启动时，Flyway 会按版本自动执行 `backend/src/main/resources/db/migration/`
下的全部迁移文件。

### 2. 启动后端

在项目根目录执行：

```powershell
cd .\backend
$env:DB_PASSWORD = '你的本地MySQL密码'
$env:JWT_SECRET = ([guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N'))
# 可选；未设置时分别使用 backend/data/files 和 backend/data/avatars
$env:FILE_STORAGE_ROOT = 'F:\manage-system-data\files'
$env:AVATAR_STORAGE_ROOT = 'F:\manage-system-data\avatars'
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080`。

### 3. 启动前端

在项目根目录执行：

```powershell
cd .\frontend
pnpm install
pnpm dev
```

前端默认地址：`http://localhost:5173`。

## 初始账号

| 账号 | 密码 | 角色 | 权限范围 |
|---|---|---|---|
| `admin` | `Admin@123` | 超级管理员 | 全部管理功能，包含日志管理 |
| `operator` | `Admin@123` | 系统管理员 | 工作台、个人中心、用户、公告、文件和消息管理 |

首次登录后应通过右上角头像进入个人中心修改演示账号密码。

## 验证命令

```powershell
# 前端
cd .\frontend
pnpm lint
pnpm build

# 后端
cd ..\backend
mvn clean test package
```

## 说明

- 前端保留 Art Design Pro 的演示组件和页面源码，默认后端菜单只开放工作台和系统管理。
- 前端原项目使用 MIT License，许可文件保留在 `frontend/LICENSE`。
- 数据库密码和 JWT 密钥不会写入代码或文档；`backend/.env.example` 仅提供变量名示例。
- 维护备注：`maorongkang@gmail.com`。
