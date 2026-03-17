# 健康管理平台 - 完整工作计划

> **项目类型**: MVP原型 | **主题**: 健康管理平台 | **架构**: 前后端分离

## 项目概述

### 技术栈
**后端 (Java)**
- Spring Boot 3.x
- MyBatis-Plus 3.5.15+
- Sa-Token 1.37+ (认证授权)
- SQLite (数据库)
- Springdoc OpenAPI 2.x (API文档)
- JUnit 5 (单元测试)

**前端 (Vue3)**
- Vue 3 + Composition API
- TypeScript
- Element Plus
- Vite
- Axios
- Pinia (状态管理)
- Vue Router 4
- Vitest (单元测试)

### 功能模块
1. **用户注册/登录/登出**
2. **角色管理**（普通用户、管理员）
3. **用户信息管理**（个人资料修改、密码修改）

### 项目规模
**MVP原型** - 快速验证核心流程，包含基础架构和核心功能

### 测试策略
**核心模块包含单元测试**
- 后端：JUnit 5 单元测试
- 前端：Vitest 单元测试

---

## 执行计划

### 阶段1: 项目初始化和后端架构搭建 (预计 2-3 天)

#### TODO 1.1: 创建Spring Boot后端项目
**描述**: 初始化Spring Boot项目，配置基础依赖和目录结构
**任务**:
- [ ] 使用Spring Initializr创建项目（Spring Boot 3.2.x）
- [ ] 添加依赖：Spring Web, SQLite JDBC, Lombok
- [ ] 配置application.yml（端口8080，SQLite数据库连接）
- [ ] 创建基础包结构：controller, service, mapper, entity, config
**交付物**:
- 可运行的Spring Boot基础项目
- 项目目录结构符合规范
- 配置文件正确设置
**测试**:
- [ ] 启动应用，验证控制台无错误
- [ ] 访问 http://localhost:8080/actuator/health 返回UP状态
**技术要点**:
- Spring Boot 3.x版本兼容性
- SQLite内存/文件模式配置
- 项目分层架构设计

#### TODO 1.2: 集成MyBatis-Plus
**描述**: 集成MyBatis-Plus ORM框架，配置代码生成器和基础CRUD操作
**任务**:
- [ ] 添加MyBatis-Plus和SQLite方言依赖
- [ ] 配置MyBatis-Plus Config（分页插件、性能分析插件）
- [ ] 创建BaseEntity（包含createTime, updateTime, deleted字段）
- [ ] 配置application.yml中的MP全局设置
- [ ] 测试基础CRUD操作
**交付物**:
- MyBatis-Plus配置完成
- BaseEntity基类
- 可工作的CRUD示例
**测试**:
- [ ] 编写JUnit测试：测试插入、查询、更新、删除操作
- [ ] 验证分页插件工作正常
- [ ] 测试乐观锁（version字段）机制
**技术要点**:
- MyBatis-Plus 3.5.15+新特性
- SQLite方言适配
- 自动填充（createTime/updateTime）
- 逻辑删除实现

#### TODO 1.3: 集成Sa-Token认证授权框架
**描述**: 集成Sa-Token实现登录认证、权限校验和会话管理
**任务**:
- [ ] 添加Sa-Token Spring Boot Starter依赖
- [ ] 配置Sa-Token（token有效期、Session配置）
- [ ] 创建LoginController实现登录/登出接口
- [ ] 配置Sa-Token拦截器（排除登录接口）
- [ ] 实现基于注解的权限校验（@SaCheckRole, @SaCheckPermission）
**交付物**:
- Sa-Token配置完成
- 登录/登出接口
- 权限拦截器
- 角色权限校验示例
**测试**:
- [ ] 编写JUnit测试：测试登录成功/失败场景
- [ ] 测试Token续期机制
- [ ] 测试权限注解拦截功能
- [ ] 测试登出后Token失效
**技术要点**:
- Sa-Token 1.37+新特性
- JWT模式 vs 分布式Session模式选择
- RBAC权限模型实现
- Token自动续期策略

#### TODO 1.4: 设计数据库结构和实体类
**描述**: 设计RBAC权限系统的数据库表结构，创建对应的实体类和Mapper
**任务**:
- [ ] 设计数据库ER图（用户表、角色表、权限表、关联表）
- [ ] 创建用户表（sys_user）：id, username, password, email, phone, status, create_time等
- [ ] 创建角色表（sys_role）：id, role_name, role_code, description
- [ ] 创建用户-角色关联表（sys_user_role）：user_id, role_id
- [ ] 创建实体类（User, Role）继承BaseEntity
- [ ] 创建Mapper接口并配置XML（或使用@Mapper注解）
- [ ] 创建数据库初始化SQL脚本（data.sql/schema.sql）
**交付物**:
- 完整的数据库设计文档
- 5张核心表的实体类和Mapper
- 数据库初始化脚本
**测试**:
- [ ] 编写JUnit测试：测试各表的CRUD操作
- [ ] 测试用户-角色关联查询
- [ ] 验证数据库约束（唯一性、外键）
**技术要点**:
- RBAC标准模型设计
- 雪花算法ID生成
- 数据库索引优化
- 数据初始化策略

#### TODO 1.5: 实现用户管理和认证接口
**描述**: 实现用户注册、登录、信息管理等核心业务接口
**任务**:
- [ ] 实现用户注册接口（/api/auth/register）：校验用户名唯一性、密码加密存储
- [ ] 实现用户登录接口（/api/auth/login）：验证用户名密码、生成Sa-Token、记录登录时间
- [ ] 实现用户登出接口（/api/auth/logout）：清除Sa-Token会话
- [ ] 实现获取当前用户信息接口（/api/user/info）：返回用户基本信息和角色列表
- [ ] 实现修改个人信息接口（/api/user/update）：允许修改邮箱、手机号等
- [ ] 实现修改密码接口（/api/user/change-password）：旧密码验证+新密码更新
- [ ] 实现管理员查询用户列表接口（/api/admin/users）：分页查询、条件筛选
- [ ] 实现管理员修改用户状态接口（/api/admin/users/{id}/status）：启用/禁用用户
- [ ] 配置接口权限注解（@SaCheckLogin, @SaCheckRole("ADMIN")）
**交付物**:
- 完整的用户认证API（注册/登录/登出）
- 用户管理API（CRUD+修改密码）
- 管理员专用API
- 接口权限控制
**测试**:
- [ ] 编写JUnit测试：测试注册流程（成功/用户名已存在/参数校验）
- [ ] 测试登录流程（成功/密码错误/用户禁用）
- [ ] 测试密码修改（旧密码验证/新密码更新）
- [ ] 测试管理员接口权限（无权限访问被拒绝）
- [ ] 测试分页查询功能
**技术要点**:
- 密码加密（BCryptPasswordEncoder）
- 参数校验（@Valid + BindingResult）
- 全局异常处理（@ControllerAdvice）
- 统一响应格式（Result/ResponseBodyAdvice）
- 接口版本控制

#### TODO 1.6: 集成Springdoc OpenAPI和接口文档
**描述**: 配置Springdoc OpenAPI自动生成API文档，提供在线接口调试界面
**任务**:
- [ ] 添加Springdoc OpenAPI Starter依赖（springdoc-openapi-starter-webmvc-ui）
- [ ] 配置OpenAPI基本信息（标题、版本、描述、联系人）
- [ ] 配置API分组（公开接口、用户接口、管理接口）
- [ ] 添加注解描述接口（@Operation, @ApiResponse, @Parameter）
- [ ] 配置JWT认证方式（ bearer token）
- [ ] 配置全局响应格式和错误码说明
- [ ] 配置接口排序和标签分组
- [ ] 添加示例请求/响应数据（@ExampleObject）
**交付物**:
- 可访问的Swagger UI文档（/swagger-ui.html）
- 完整的API描述和示例
- 支持在线接口调试
- 分组清晰的接口组织
**测试**:
- [ ] 验证Swagger UI可正常访问
- [ ] 测试通过Swagger UI进行接口调试
- [ ] 验证请求参数和响应格式说明准确
- [ ] 测试认证接口的token传递
**技术要点**:
- OpenAPI 3.0规范
- Springdoc与Spring Boot 3.x兼容性
- 接口版本管理
- 安全认证配置（SecurityScheme）
- 自定义OperationCustomizer

---

### 阶段2: 前端项目初始化和基础架构搭建 (预计 2-3 天)

#### TODO 2.1: 创建Vue3前端项目
**描述**: 使用Vite初始化Vue3+TypeScript项目，配置基础目录结构
**任务**:
- [ ] 使用npm create vite@latest创建项目（选择Vue + TypeScript）
- [ ] 配置vite.config.ts（路径别名、端口、代理）
- [ ] 安装并配置TypeScript（tsconfig.json）
- [ ] 安装并配置ESLint + Prettier（代码规范）
- [ ] 创建基础目录结构（src/views, src/components, src/api, src/stores等）
- [ ] 安装并配置path别名（@/指向src/）
- [ ] 创建环境配置文件（.env.development, .env.production）
**交付物**:
- 可运行的Vue3+TypeScript基础项目
- 规范的目录结构和代码规范
- 开发环境配置完成
**测试**:
- [ ] 运行npm run dev启动开发服务器
- [ ] 访问http://localhost:5173显示Vue欢迎页
- [ ] 验证TypeScript编译无错误
- [ ] 验证ESLint检查通过
**技术要点**:
- Vite 5.x配置和优化
- Vue 3.4+新特性
- TypeScript 5.x严格模式
- ESLint 9.x扁平配置

#### TODO 2.2: 集成Element Plus UI框架
**描述**: 安装并配置Element Plus组件库，设置主题和按需导入
**任务**:
- [ ] 安装Element Plus（npm install element-plus）
- [ ] 配置自动导入（unplugin-auto-import + unplugin-vue-components）
- [ ] 配置Element Plus主题颜色（主色调、辅色）
- [ ] 配置国际化（中文语言包）
- [ ] 创建Element Plus类型声明文件（解决TS类型问题）
- [ ] 安装Element Plus图标库（@element-plus/icons-vue）
- [ ] 配置图标自动导入
**交付物**:
- Element Plus集成完成并可正常使用
- 自定义主题配置
- 组件和图标自动导入配置
**测试**:
- [ ] 在App.vue中使用el-button组件，验证显示正常
- [ ] 使用Element Plus图标，验证显示正常
- [ ] 验证TypeScript能正确识别组件类型
- [ ] 验证主题色已生效
**技术要点**:
- Element Plus 2.5+新特性
- 自动导入插件配置
- 主题定制（CSS变量）
- 暗黑模式支持

#### TODO 2.3: 配置Axios HTTP客户端
**描述**: 安装并配置Axios，设置拦截器处理请求/响应和错误
**任务**:
- [ ] 安装Axios（npm install axios）
- [ ] 创建axios实例配置文件（src/api/request.ts）
- [ ] 配置基础URL（根据环境变量）
- [ ] 配置请求拦截器（添加Token到Header）
- [ ] 配置响应拦截器（统一错误处理、Token过期处理）
- [ ] 配置请求超时（10秒）
- [ ] 创建API模块目录（src/api/modules/）
- [ ] 封装通用响应类型（ApiResponse<T>）
**交付物**:
- Axios配置完成
- 请求/响应拦截器
- 类型安全的API调用方式
**测试**:
- [ ] 创建一个测试API调用，验证请求拦截器添加Token
- [ ] 模拟401响应，验证响应拦截器处理Token过期
- [ ] 模拟网络错误，验证错误提示
- [ ] 验证TypeScript类型推断正确
**技术要点**:
- Axios拦截器链
- JWT Token管理
- 错误重试机制
- 请求取消（AbortController）

#### TODO 2.4: 配置Vue Router路由
**描述**: 安装并配置Vue Router 4，实现路由守卫和权限控制
**任务**:
- [ ] 安装Vue Router 4（npm install vue-router@4）
- [ ] 创建路由配置文件（src/router/index.ts）
- [ ] 配置路由模式（history模式）
- [ ] 定义基础路由（/login, /register, /404）
- [ ] 创建布局组件（Layout：包含侧边栏、顶部导航）
- [ ] 配置嵌套路由（/dashboard, /profile等）
- [ ] 实现路由守卫（beforeEach：检查登录状态、权限）
- [ ] 配置动态路由加载（根据用户角色动态添加路由）
- [ ] 创建路由白名单（/login, /register无需登录）
**交付物**:
- 完整的路由配置
- 路由守卫权限控制
- 布局组件和页面组件
**测试**:
- [ ] 访问/login，验证显示登录页
- [ ] 未登录时访问/dashboard，验证重定向到/login
- [ ] 登录后访问/dashboard，验证正常显示
- [ ] 验证路由守卫能正确获取用户信息
**技术要点**:
- Vue Router 4组合式API
- 路由守卫异步处理
- 动态路由添加（addRoute）
- 路由过渡动画

#### TODO 2.5: 配置Pinia状态管理
**描述**: 安装并配置Pinia，创建用户状态管理和权限状态
**任务**:
- [ ] 安装Pinia（npm install pinia）
- [ ] 创建Pinia实例（src/stores/index.ts）
- [ ] 创建用户Store（src/stores/user.ts）：
  - [ ] state: token, userInfo, roles
  - [ ] actions: login, logout, getUserInfo, updateProfile
  - [ ] getters: isLoggedIn, isAdmin, username
- [ ] 创建权限Store（src/stores/permission.ts）：
  - [ ] state: routes, addRoutes
  - [ ] actions: generateRoutes（根据角色生成可访问路由）
  - [ ] getters: accessibleRoutes
- [ ] 配置Pinia持久化（可选：pinia-plugin-persistedstate）
- [ ] 在Vue应用中使用Pinia
**交付物**:
- Pinia状态管理配置
- 用户状态Store
- 权限状态Store
**测试**:
- [ ] 调用userStore.login()，验证token和userInfo被正确存储
- [ ] 调用userStore.logout()，验证状态被清空
- [ ] 调用permissionStore.generateRoutes()，验证根据角色过滤路由
- [ ] 验证getters计算属性正确
**技术要点**:
- Pinia组合式API（defineStore）
- Store间相互调用
- 状态持久化策略
- TypeScript类型推断

#### TODO 2.6: 创建登录/注册页面
**描述**: 实现用户登录和注册的前端页面，集成后端API
**任务**:
- [ ] 创建登录页面（src/views/login/index.vue）：
  - [ ] Element Plus表单（用户名、密码）
  - [ ] 表单验证规则（必填、长度限制）
  - [ ] 登录按钮（loading状态）
  - [ ] 记住密码功能（localStorage）
  - [ ] 跳转到注册页面链接
- [ ] 创建注册页面（src/views/register/index.vue）：
  - [ ] 表单字段（用户名、密码、确认密码、邮箱、手机号）
  - [ ] 表单验证（密码强度、两次密码一致、邮箱格式）
  - [ ] 用户名重复检查（异步验证）
  - [ ] 注册按钮（调用后端API）
  - [ ] 注册成功跳转到登录页
- [ ] 调用后端API（使用api/modules/auth.ts）
- [ ] 集成用户Store（登录成功后存储用户信息）
- [ ] 添加页面样式（响应式布局，适配移动端）
**交付物**:
- 登录页面（功能完整，包含验证）
- 注册页面（功能完整，包含验证）
- 与后端API集成
**测试**:
- [ ] 输入正确用户名密码，验证登录成功并跳转
- [ ] 输入错误密码，验证显示错误提示
- [ ] 验证表单必填项未填时显示提示
- [ ] 测试注册流程（用户名已存在提示）
- [ ] 验证注册成功后跳转到登录页
**技术要点**:
- Element Plus表单验证（rules）
- 异步表单验证（asyncValidator）
- 表单数据双向绑定（v-model）
- 页面跳转和参数传递
- 响应式布局（Element Plus栅格系统）

#### TODO 2.7: 创建用户仪表盘和个人信息页面
**描述**: 实现用户登录后的主页面和个人信息管理页面
**任务**:
- [ ] 创建布局组件（src/layout/index.vue）：
  - [ ] 侧边栏菜单（根据角色动态显示）
  - [ ] 顶部导航栏（显示用户名、头像、下拉菜单）
  - [ ] 面包屑导航
  - [ ] 主内容区域（router-view）
  - [ ] 页脚
- [ ] 创建仪表盘页面（src/views/dashboard/index.vue）：
  - [ ] 欢迎信息（根据时间显示不同问候语）
  - [ ] 用户统计卡片（健康数据概览 - 预留）
  - [ ] 系统公告区域
  - [ ] 快速入口（跳转链接）
- [ ] 创建个人信息页面（src/views/profile/index.vue）：
  - [ ] 个人信息表单（只读字段：用户名；可编辑：邮箱、手机号、昵称）
  - [ ] 头像上传功能（预留接口）
  - [ ] 表单验证和保存
- [ ] 创建修改密码页面（src/views/profile/password.vue）：
  - [ ] 旧密码输入
  - [ ] 新密码输入（密码强度提示）
  - [ ] 确认新密码（一致性验证）
  - [ ] 提交修改
- [ ] 集成后端API（userApi, profileApi）
- [ ] 添加路由和菜单配置
**交付物**:
- 布局组件（侧边栏+顶部导航）
- 仪表盘页面
- 个人信息管理页面
- 修改密码页面
**测试**:
- [ ] 验证侧边栏根据角色显示不同菜单
- [ ] 测试个人信息修改并保存
- [ ] 测试修改密码流程（旧密码错误提示）
- [ ] 验证面包屑导航随路由变化
- [ ] 测试页面响应式布局（窗口缩放）
**技术要点**:
- 动态路由和菜单生成
- Element Plus布局组件（Container, Aside, Header, Main）
- 表单数据回显和提交
- 文件上传组件（el-upload）
- 面包屑导航实现

---

### 阶段3: 管理后台和权限管理 (预计 2-3 天)

#### TODO 3.1: 实现用户管理（管理员功能）
**描述**: 实现管理员对用户的管理功能：查询、禁用、重置密码等
**任务**:
- [ ] 创建用户列表页面（src/views/admin/users/index.vue）：
  - [ ] 搜索表单（用户名、状态、时间范围）
  - [ ] 数据表格（用户名、角色、状态、创建时间、操作列）
  - [ ] 分页组件
  - [ ] 刷新按钮
- [ ] 实现查看用户详情功能：
  - [ ] 抽屉或弹窗显示用户详细信息
  - [ ] 显示用户健康数据概览（预留）
- [ ] 实现修改用户状态功能：
  - [ ] 启用/禁用用户（switch开关）
  - [ ] 确认对话框
  - [ ] 调用后端API更新状态
- [ ] 实现重置密码功能：
  - [ ] 重置密码按钮
  - [ ] 确认对话框（提示新密码或发送到邮箱）
  - [ ] 调用后端API重置密码
- [ ] 集成后端API（adminUserApi）
- [ ] 添加管理员路由和菜单
**交付物**:
- 用户管理列表页面
- 用户详情查看功能
- 用户状态管理功能
- 重置密码功能
**测试**:
- [ ] 测试用户列表分页和搜索功能
- [ ] 测试启用/禁用用户功能
- [ ] 测试重置密码流程
- [ ] 验证非管理员用户无法访问管理页面
- [ ] 测试表格数据刷新
**技术要点**:
- Element Plus表格组件（el-table）
- 分页和搜索功能实现
- 抽屉组件（el-drawer）
- 弹窗确认（ElMessageBox）
- 状态切换（el-switch）

#### TODO 3.2: 实现角色和权限管理
**描述**: 实现角色管理和权限分配功能（基础版本，支持角色分配）
**任务**:
- [ ] 创建角色管理页面（简化版）（src/views/admin/roles/index.vue）：
  - [ ] 角色列表（角色名称、角色编码、描述、操作）
  - [ ] 添加角色按钮和弹窗表单
  - [ ] 编辑角色功能
  - [ ] 删除角色功能（检查是否有关联用户）
- [ ] 实现用户角色分配功能：
  - [ ] 在用户管理页面添加"分配角色"按钮
  - [ ] 弹窗显示角色列表（多选框）
  - [ ] 保存用户角色关联
- [ ] 创建权限定义（简化版）：
  - [ ] 在代码中定义权限常量（user:create, user:update等）
  - [ ] 与Sa-Token权限注解配合使用
- [ ] 实现前端权限控制：
  - [ ] 创建权限指令（v-permission）
  - [ ] 路由元信息配置权限（meta: { permissions: ['user:create'] }）
  - [ ] 菜单根据权限动态显示
- [ ] 集成后端API（roleApi, permissionApi）
**交付物**:
- 角色管理页面（简化版）
- 用户角色分配功能
- 前端权限控制实现
- 权限指令和路由守卫
**测试**:
- [ ] 测试创建/编辑/删除角色
- [ ] 测试给用户分配角色
- [ ] 测试无权限用户无法看到特定菜单
- [ ] 测试无权限按钮不显示
- [ ] 测试后端接口权限拦截
**技术要点**:
- 角色-权限模型设计
- Vue自定义指令（v-permission）
- 动态菜单生成
- 路由守卫权限校验
- Element Plus Tree组件（权限树）

---

### 阶段4: 单元测试和项目优化 (预计 1-2 天)

#### TODO 4.1: 编写后端单元测试
**描述**: 为核心业务逻辑编写JUnit 5单元测试，确保代码质量
**任务**:
- [ ] 配置测试环境：
  - [ ] 添加JUnit 5、Mockito、AssertJ依赖
  - [ ] 配置H2内存数据库（测试专用）
  - [ ] 创建application-test.yml测试配置
  - [ ] 配置测试数据初始化脚本
- [ ] 编写UserService单元测试：
  - [ ] 测试用户注册（成功/用户名已存在/参数校验）
  - [ ] 测试用户登录（成功/密码错误/用户不存在）
  - [ ] 测试用户信息修改
  - [ ] 测试密码修改（旧密码验证）
  - [ ] 使用Mockito模拟Mapper层
- [ ] 编写AuthController单元测试：
  - [ ] 测试登录接口（各种场景）
  - [ ] 测试注册接口
  - [ ] 测试登出接口
  - [ ] 使用@WebMvcTest和MockMvc
- [ ] 编写集成测试：
  - [ ] 测试完整的注册-登录-修改密码流程
  - [ ] 使用@SpringBootTest和TestRestTemplate
  - [ ] 验证数据库状态变化
- [ ] 配置测试覆盖率工具（JaCoCo）
**交付物**:
- JUnit 5单元测试套件（覆盖率>70%核心业务）
- 集成测试用例
- 测试报告和覆盖率报告
**测试**:
- [ ] 运行mvn test，所有测试通过
- [ ] 查看测试覆盖率报告（target/site/jacoco/index.html）
- [ ] 验证核心业务流程测试覆盖
**技术要点**:
- JUnit 5新特性（@Nested, @DisplayName）
- Mockito模拟和验证
- @Sql注解初始化测试数据
- 测试切片（@WebMvcTest, @DataJpaTest）
- 测试容器（Testcontainers）SQLite支持

#### TODO 4.2: 编写前端单元测试
**描述**: 使用Vitest为前端组件和工具函数编写单元测试
**任务**:
- [ ] 配置测试环境：
  - [ ] 安装Vitest、@vue/test-utils、jsdom
  - [ ] 配置vite.config.ts测试环境
  - [ ] 创建test/setup.ts初始化文件
  - [ ] 配置测试覆盖率（@vitest/coverage-v8）
- [ ] 编写组件单元测试：
  - [ ] 测试LoginForm组件（输入渲染、表单验证、提交事件）
  - [ ] 测试RegisterForm组件
  - [ ] 测试UserProfile组件（数据展示、编辑模式）
  - [ ] 使用@vue/test-utils挂载和交互
- [ ] 编写工具函数测试：
  - [ ] 测试格式化工具函数（日期、金额等）
  - [ ] 测试验证函数（邮箱、手机号格式）
  - [ ] 测试storage工具（localStorage封装）
- [ ] 编写Store单元测试：
  - [ ] 测试userStore（login, logout, updateProfile actions）
  - [ ] 测试permissionStore
  - [ ] 使用createPinia创建测试实例
- [ ] 编写API模块测试：
  - [ ] 使用MSW（Mock Service Worker）模拟API响应
  - [ ] 测试authApi（login, register）
  - [ ] 测试错误处理
**交付物**:
- Vitest单元测试套件（核心组件和工具）
- 测试覆盖率报告
- MSW模拟API配置
**测试**:
- [ ] 运行npm run test:unit，所有测试通过
- [ ] 查看测试覆盖率报告
- [ ] 验证组件渲染和交互测试
**技术要点**:
- Vitest与Jest语法兼容
- @vue/test-utils组件测试
- MSW模拟HTTP请求
- Vue Testing Library查询方式
- 测试覆盖率阈值配置

#### TODO 4.3: 项目优化和代码审查
**描述**: 进行代码优化、性能优化和代码规范检查
**任务**:
- [ ] 后端优化：
  - [ ] 使用SonarLint或Checkstyle检查代码规范
  - [ ] 优化SQL查询（添加索引、避免N+1问题）
  - [ ] 配置连接池（HikariCP参数优化）
  - [ ] 添加日志配置（Slf4j + Logback）
  - [ ] 配置跨域（CORS）
  - [ ] 配置全局异常处理（更详细的错误信息）
- [ ] 前端优化：
  - [ ] 使用ESLint + Prettier统一代码风格
  - [ ] 配置自动导入（unplugin-auto-import）
  - [ ] 优化打包配置（代码分割、懒加载）
  - [ ] 添加性能优化（v-memo、懒加载图片）
  - [ ] 配置环境变量（不同环境不同配置）
  - [ ] 添加错误监控（全局错误处理）
- [ ] 代码审查：
  - [ ] 检查硬编码值（提取到配置文件）
  - [ ] 检查重复代码（提取公共函数/组件）
  - [ ] 检查潜在安全问题（SQL注入、XSS防护）
  - [ ] 检查注释完整性（关键逻辑添加注释）
**交付物**:
- 优化后的代码（符合规范）
- 性能优化配置
- 代码审查报告
**测试**:
- [ ] 运行代码规范检查，无严重警告
- [ ] 验证打包体积优化效果
- [ ] 测试性能优化后的响应速度
**技术要点**:
- SonarQube代码质量分析
- Webpack/Vite打包优化
- 前端性能指标（Lighthouse）
- 安全最佳实践（OWASP）

---

## 项目总结

### 交付清单

#### 后端交付物
1. [ ] **Spring Boot项目**（可运行）
2. [ ] **数据库设计**（RBAC模型，5张核心表）
3. [ ] **API接口**（完整的用户管理和认证接口）
4. [ ] **API文档**（Swagger UI可访问）
5. [ ] **单元测试**（JUnit 5，核心业务流程覆盖）

#### 前端交付物
1. [ ] **Vue3项目**（可运行）
2. [ ] **UI界面**（登录、注册、仪表盘、个人中心、用户管理）
3. [ ] **状态管理**（Pinia Store）
4. [ ] **路由权限**（动态路由+权限控制）
5. [ ] **单元测试**（Vitest，核心组件覆盖）

### 项目统计

| 阶段 | 任务数 | 预计天数 | 关键产出 |
|------|--------|----------|----------|
| 阶段1 | 6 | 2-3天 | 后端基础架构+核心API |
| 阶段2 | 7 | 2-3天 | 前端基础架构+用户界面 |
| 阶段3 | 2 | 2-3天 | 管理后台+权限管理 |
| 阶段4 | 3 | 1-2天 | 单元测试+项目优化 |
| **合计** | **18** | **7-11天** | **完整MVP系统** |

### 核心技术亮点

1. **现代化技术栈**: Spring Boot 3.x + Vue 3 + TypeScript
2. **轻量级认证**: Sa-Token替代Spring Security，简单易用
3. **RBAC权限模型**: 标准角色-权限设计，支持扩展
4. **前后端分离**: RESTful API + CORS跨域
5. **完整测试覆盖**: JUnit 5 + Vitest单元测试
6. **自动化文档**: Springdoc OpenAPI自动生成Swagger文档

### 后续扩展建议

1. **健康数据模块**: 体重、血压、血糖记录
2. **数据可视化**: 健康趋势图表（ECharts）
3. **消息通知**: 系统公告、健康提醒
4. **文件上传**: 头像上传、健康报告附件
5. **移动端适配**: 响应式优化或开发H5版本

---

## 风险评估

| 风险 | 可能性 | 影响 | 缓解措施 |
|------|--------|------|----------|
| 技术栈学习成本 | 中 | 中 | 提供详细文档和示例代码 |
| 数据库设计复杂 | 低 | 高 | 采用标准RBAC模型，参考成熟方案 |
| 前后端联调问题 | 中 | 中 | 使用Swagger文档，定义好接口规范 |
| 测试覆盖不足 | 低 | 中 | 制定测试计划，核心流程必须覆盖 |

---

## 质量标准

### 代码质量
- [ ] 符合阿里巴巴Java开发规范
- [ ] ESLint + Prettier前端代码规范
- [ ] 关键代码添加详细注释
- [ ] SonarLint扫描无严重警告

### 功能完整
- [ ] 用户注册/登录/登出流程完整
- [ ] 角色权限控制有效
- [ ] 用户信息可正常修改
- [ ] 管理员功能完整可用

### 测试覆盖
- [ ] 后端核心业务JUnit测试覆盖≥70%
- [ ] 前端核心组件Vitest测试覆盖≥60%
- [ ] 集成测试覆盖主要业务流程

### 文档完整
- [ ] README.md（项目说明、启动方式）
- [ ] API文档（Swagger UI可访问）
- [ ] 数据库设计文档（ER图）
- [ ] 部署文档（环境要求、部署步骤）

---

*计划生成时间: 2026年2月15日*
*版本: MVP v1.0*
*状态: 待执行*
