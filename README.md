# 健康管理平台 (Health Platform)

这是一个基于 Java + Vue 3 的健康管理原型系统，采用了 MCP 设计理念，具备完整的 RBAC 权限管理。

## 技术栈

### 后端 (health-backend)
- **Spring Boot 3.2.2**
- **MyBatis-Plus**: 简化的持久层操作
- **Sa-Token**: 轻量级权限认证框架 (RBAC 实现)
- **SQLite**: 嵌入式数据库，无需安装，即插即用
- **Springdoc OpenAPI**: 自动生成的 Swagger API 文档

### 前端 (health-frontend)
- **Vue 3 + TypeScript**
- **Vite**: 极速构建工具
- **Element Plus**: 现代化 UI 组件库
- **Axios**: API 请求处理
- **Vue Router**: 路由管理

## 核心功能

1. **用户认证**: 登录、登出、当前用户信息获取。
2. **RBAC 权限**:
   - `普通用户`: 只能管理自己发布的健康内容。
   - `管理员`: 可以查看并管理（修改/删除）全平台所有用户的内容。
3. **数据概览**: 可视化的健康指标仪表盘（模拟数据）。
4. **内容管理**: 标题、分类（运动/饮食/心态/睡眠）、内容的增删改查。

## 快速开始

### 后端运行
1. 进入 `health-backend` 目录。
2. 确保已安装 JDK 17 和 Maven。
3. 运行 `mvn spring-boot:run`。
4. 数据库文件 `health.db` 会在根目录下自动生成。

### 前端运行
1. 进入 `health-frontend` 目录。
2. 运行 `npm install`。
3. 运行 `npm run dev`。
4. 访问 `http://localhost:3000`。

## 默认账户
- **管理员**: `admin` / `123456`
- **普通用户**: `user` / `123456`
