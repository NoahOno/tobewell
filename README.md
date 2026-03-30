# 健康管理平台项目说明

## 1. 项目概述

本项目是一个前后端分离的健康管理平台，围绕“健康数据记录 + 训练计划执行 + 社区互动 + 后台治理”展开，当前仓库由两个子项目组成：

- `health-backend`：Spring Boot 3 + MyBatis-Plus + Sa-Token + SQLite
- `health-frontend`：Vue 3 + TypeScript + Vite + Element Plus + ECharts

当前实现已经不再只是最初的内容管理原型，而是扩展为包含训练计划、课程库、活动中心、日历排期、社区互动和后台审核的综合系统。

## 2. 技术栈

### 后端

- Spring Boot 3.2.2
- MyBatis-Plus 3.5.5
- Sa-Token 1.37.0
- SQLite
- Springdoc OpenAPI

### 前端

- Vue 3
- TypeScript
- Vite 5
- Element Plus
- Axios
- Vue Router
- ECharts / vue-echarts
- Pinia（已安装，当前使用较少）

## 3. 当前项目结构

```text
graduationProj002
├─ health-backend    # 后端服务
├─ health-frontend   # 前端应用
├─ Docs              # 过程文档/规划文档
└─ README.md         # 当前项目说明
```

## 4. 当前已实现的功能模块

### 4.1 账户与权限

已实现：

- 用户注册、登录、退出登录
- 获取当前登录用户信息
- 修改昵称、修改密码
- 基于 Sa-Token 的登录鉴权
- 基于角色的后台权限控制

角色现状：

- `USER`：普通用户
- `ADMIN`：后台管理员

默认账号：

- 管理员：`admin / 123456`
- 普通用户：`user / 123456`

### 4.2 健康数据记录与看板

已实现：

- 健康指标录入
- 指标最新记录查询
- 指标历史记录查询
- 首页健康数据看板
- 趋势图展示
- BMI、心率、身高体重等模块化展示
- 运动记录单独录入
- 运动时长与分类统计

前端页面：

- `/app/dashboard`：健康总览看板
- `/app/records`：手动录入健康指标

后端接口：

- `/metric/list`
- `/metric/latest`
- `/metric/record`
- `/workout/record`
- `/workout/stats`

### 4.3 训练计划与课程库

已实现：

- 公共训练计划库浏览与搜索
- 公共课程库浏览与搜索
- 训练计划订阅
- 训练计划克隆为个人实例
- 课程订阅到个人课程库
- 用户创建/编辑私有训练计划
- 用户创建/编辑私有课程
- 收藏计划/课程
- 训练计划提交入库申请

前端页面：

- `/app/explore`：训练计划库、课程库、健康服务入口
- `/app/training`：我的训练、收藏、我的创建、训练总览

后端接口：

- `/training/library`
- `/training/list`
- `/training/current`
- `/training/save`
- `/training/subscribe/{id}`
- `/training/subscribe/{id}/frequency`
- `/training/unsubscribe/{id}`
- `/course/library`
- `/course/my`
- `/course/save`
- `/course/subscribe/{id}`
- `/course/{id}/complete`
- `/course/{id}`
- `/resource/submit`
- `/resource/my-submissions`
- `/interaction/collections`
- `/interaction/collect`

实现特点：

- 订阅公共训练计划时，后端会复制为用户自己的私有计划实例，再生成日程
- 订阅公共课程时，后端会复制为用户自己的私有课程副本
- 用户自建私有计划/课程可以提交后台审核，通过后进入公共资源库

### 4.4 训练日历与执行闭环

已实现：

- 今日训练任务获取
- 指定日期范围日程查询
- 单次课程预约到多个日期
- 日程完成、跳过、延期、暂停、恢复、重置
- 训练反馈记录
- 训练沉浸式执行流程
- 训练总览、最近训练、连续训练天数、最近 7 天趋势

前端页面：

- `/app/training`

后端接口：

- `/daily/today`
- `/daily/range`
- `/daily/{id}/complete`
- `/daily/{id}/feedback`
- `/daily/feedback`
- `/daily/{id}/skip`
- `/daily/{id}/postpone`
- `/daily/{id}/pause`
- `/daily/{id}/resume`
- `/daily/{id}/reset`
- `/daily/records`
- `/daily/course`
- `/daily/{id}`
- `/training/dashboard/summary`
- `/training/dashboard/trend`
- `/training/dashboard/recent`

### 4.5 社区互动

已实现：

- 社区帖子列表
- 推荐 / 关注 / 热门 / 我的发帖切换
- 发帖、删帖
- 评论帖子
- 帖子点赞
- 帖子收藏
- 用户关注/取关
- 用户卡片与关注统计
- 社区搜索
- 热门帖子侧栏

前端页面：

- `/app/community`

后端接口：

- `/community/posts`
- `/community/search`
- `/community/post`
- `/community/post/{id}/comments`
- `/community/post/{id}/comment`
- `/community/post/{id}/like`
- `/community/follow/{userId}`
- `/community/user/{id}`
- `/community/hot`
- `/interaction/collect`

### 4.6 活动中心

已实现：

- 活动列表
- 热门活动
- 活动详情
- 活动报名
- 活动生成连续训练任务
- 活动完成后生成动态内容
- 活动完成用户展示
- 活动动态转发到社区

前端页面：

- 社区页中的“活动中心”标签

后端接口：

- `/activity/list`
- `/activity/trending`
- `/activity/{id}`
- `/activity/{id}/completed`
- `/activity/{id}/apply`

实现特点：

- 活动可绑定 `PLAN` 或 `COURSE` 模板
- 用户报名后，系统会按活动要求自动生成连续日程
- 当关联任务全部完成时，会自动写入活动完成动态

### 4.7 后台管理

已实现：

- 用户列表管理
- 用户角色/状态编辑
- 用户删除
- 社区帖子审核/删除
- 评论清理
- 私有资源入库审核
- 动作库管理
- 公共训练计划管理
- 公共课程管理
- 活动管理
- 活动置顶、下线、统计查看

前端页面：

- `/admin/users`
- `/admin/content`
- `/admin/training`

后端接口：

- `/admin/users`
- `/admin/user/update`
- `/admin/user/{id}`
- `/admin/posts`
- `/admin/post/{id}`
- `/admin/comments`
- `/admin/comment/{id}`
- `/admin/plans`
- `/admin/plan/save`
- `/admin/plan/{id}`
- `/admin/courses`
- `/admin/course/save`
- `/admin/course/{id}`
- `/exercise/list`
- `/exercise/save`
- `/exercise/{id}`
- `/admin/activities`
- `/admin/activities/{id}/offline`
- `/admin/activities/{id}/pin`
- `/admin/activities/{id}/analytics`
- `/resource/admin/submissions`
- `/resource/admin/submissions/{id}/approve`
- `/resource/admin/submissions/{id}/reject`

## 5. 前端当前路由

### 公共路由

- `/`：首页
- `/login`：登录
- `/register`：注册

### 用户端路由

- `/app/dashboard`：健康总览
- `/app/records`：健康记录录入
- `/app/community`：社区
- `/app/explore`：发现页/训练资源库
- `/app/training`：训练中心
- `/app/exercises`：动作库浏览
- `/app/profile`：个人资料
- `/app/collections`：收藏页
- `/app/explore/webai`：Web AI 对话页

### 管理端路由

- `/admin/users`
- `/admin/content`
- `/admin/training`

## 6. 数据库当前核心表

根据 `schema.sql`，当前主要数据表包括：

- `sys_user`：用户
- `community_post`：社区帖子
- `user_follow`：用户关注关系
- `collection`：收藏
- `health_metric`：健康指标
- `workout_record`：运动记录
- `training_plan`：训练计划
- `course`：课程
- `daily_schedule`：训练日程
- `training_record`：训练记录/反馈
- `resource_submission`：资源入库申请
- `exercise`：动作库
- `activity`：活动
- `activity_participation`：活动报名
- `activity_task`：活动任务映射
- `activity_dynamic`：活动完成动态

## 7. 当前功能实现状态说明

### 前后端已打通

- 登录注册与鉴权
- 健康指标记录与看板
- 社区发帖/评论/点赞/收藏/关注
- 训练计划库、课程库、订阅与个人实例化
- 训练日程查看与训练反馈
- 活动报名与活动任务生成
- 后台用户/内容/训练资源/活动管理
- 资源入库审核

### 后端已提供，前端已有入口或部分接入

- 训练记录列表 `/daily/records`
- 训练分类 `/training/categories`
- 活动完成统计与动态联动

### 当前仍是占位或未完全打通

- 收藏页 `/app/collections` 目前仍为占位页，未真正展示收藏数据
- `WebAIChat.vue` 调用了 `/ai/chat`，但当前后端控制器中未发现对应接口
- `Content.vue` 为较早期“内容流”页面，当前主路由未接入，项目主线已转向社区页与训练系统
- 部分筛选项是前端展示增强逻辑，不完全依赖后端结构化字段

## 8. 运行方式

### 启动后端

```bash
cd health-backend
mvn spring-boot:run
```

默认端口：

- `http://localhost:8080`

说明：

- 数据库使用 SQLite
- 启动时会根据 `schema.sql` 初始化表结构
- 数据库文件默认在 `health-backend/health.db`

### 启动前端

```bash
cd health-frontend
npm install
npm run dev
```

默认访问地址：

- `http://localhost:3000`

前端通过 `/api` 代理访问后端接口。

## 9. 适合继续迭代的方向

- 完成收藏页真实数据展示
- 补齐 Web AI 后端接口或移除前端入口
- 补充训练计划/课程更细颗粒度的结构化字段
- 为关键模块补充自动化测试
- 对训练、社区、活动模块补充统一状态说明和异常处理

## 10. 当前文档更新结论

当前项目已经形成以下主线能力：

1. 用户登录后可记录健康数据并查看趋势
2. 用户可从公共训练计划/课程库中订阅内容并生成个人训练安排
3. 用户可在训练中心完成训练、反馈状态、管理日历日程
4. 用户可在社区中发帖、评论、点赞、收藏、关注
5. 用户可报名活动并自动获得连续任务
6. 管理员可治理用户、内容、资源库和活动

这份 README 已按当前代码实现同步更新，可作为现阶段项目介绍与功能说明文档使用。
