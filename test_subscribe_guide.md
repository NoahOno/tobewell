# 训练计划订阅Bug修复测试指南

## 修复内容

### 1. 前端修复 (Explore.vue)
- ✅ 修复日期格式：将Date对象转换为 YYYY-MM-DD 字符串
- ✅ 添加详细的控制台日志
- ✅ 添加错误提示
- ✅ 跳转时携带正确的tab参数

### 2. 后端修复 (TrainingPlanController.java & ScheduleServiceImpl.java)
- ✅ 添加详细的订阅流程日志
- ✅ 记录请求参数、计划信息、用户ID
- ✅ 记录日程生成的详细过程

### 3. 前端修复 (Training.vue)
- ✅ 训练管理页面显示正确的数据源（activePlans而非favoritePlans）
- ✅ 添加"查看日程"和"取消计划"功能

## 测试步骤

### 步骤1：重启后端服务
```bash
# 确保新的日志代码生效
cd health-backend
# 重启Spring Boot应用
```

### 步骤2：清除浏览器缓存并打开开发者工具
1. 打开浏览器开发者工具 (F12)
2. 切换到 Console 标签
3. 清除控制台历史记录

### 步骤3：登录系统
- 用户名：user
- 密码：123456

### 步骤4：订阅训练计划
1. 导航到"探索"页面
2. 切换到"训练计划"标签
3. 选择一个训练计划（如"减脂训练计划"）
4. 点击"加入训练"
5. 在弹出的对话框中：
   - 选择开始日期（建议选择今天或明天）
   - 选择每周训练日（至少选择3天，如周一、周三、周五）
6. 点击"生成个人计划实例"

### 步骤5：观察控制台日志

#### 前端控制台应该显示：
```
[Subscribe] Plan ID: <计划ID>
[Subscribe] Start Date: 2024-XX-XX
[Subscribe] Weekly Days: ["MONDAY", "WEDNESDAY", "FRIDAY"]
[Subscribe] Response: {code: 200, message: "success", ...}
```

#### 后端控制台应该显示：
```
[TrainingPlanController] Subscribe request - Plan ID: XX
[TrainingPlanController] Request body - startDate: 2024-XX-XX, weeklyDays: [MONDAY, WEDNESDAY, FRIDAY], activate: true
[TrainingPlanController] Original plan - Title: XXX, Actions: [...]
[TrainingPlanController] User ID: 2
[TrainingPlanController] Creating new plan clone
[TrainingPlanController] Clone plan - StartDate: 2024-XX-XX, Status: ACTIVE
[TrainingPlanController] Inserted clone with ID: XX
[TrainingPlanController] Generating schedule with weeklyDays: [MONDAY, WEDNESDAY, FRIDAY]
[ScheduleService] Generating schedule for plan: XX, userId: 2
[ScheduleService] Start date: 2024-XX-XX, weeklyDays: [MONDAY, WEDNESDAY, FRIDAY]
[ScheduleService] Actions JSON: [...]
[ScheduleService] Deleted X old schedules
[ScheduleService] Found X plan days
[ScheduleService] Processing day 0, date: 2024-XX-XX, dayOfWeek: MONDAY, type: 训练
[ScheduleService] Created schedule: XXX on 2024-XX-XX
...
[ScheduleService] Schedule generation completed. Created X schedules
[TrainingPlanController] Schedule generation completed
```

### 步骤6：验证训练管理页面
1. 页面应自动跳转到"训练管理"
2. 应该能看到刚订阅的计划
3. 计划状态应显示"进行中"
4. 应显示开始日期和持续时间

### 步骤7：验证训练日历
1. 点击"查看日程"按钮
2. 切换到"训练日程"标签
3. 在日历中应该能看到对应日期的训练安排
4. 点击具体日期，右侧应显示该日期的训练任务

## 常见问题排查

### 问题1：后端日志显示"Failed to parse actions JSON"
**原因**：训练计划的actions字段为空或格式不正确
**解决**：
```sql
-- 检查数据库中的训练计划
SELECT id, title, actions FROM training_plan WHERE is_public = 1;
```
确保actions字段是有效的JSON数组，格式如：
```json
[
  {"type": "训练", "courseId": 1, "title": "上肢训练"},
  {"type": "训练", "courseId": 2, "title": "下肢训练"}
]
```

### 问题2：后端日志显示"No plan days found"
**原因**：actions数组为空
**解决**：检查训练计划是否配置了训练内容

### 问题3：创建了0个日程（Created 0 schedules）
**原因**：
1. 开始日期是过去的时间
2. weeklyDays与计划中的训练日不匹配
3. 计划中没有type为"训练"的项

**解决**：
- 确保开始日期是今天或未来
- 确保选择了正确的每周训练日
- 检查actions中的type字段是否为"训练"

### 问题4：前端看不到计划
**原因**：
1. 前端数据未刷新
2. 计划状态不是ACTIVE或PLANNING
3. isSubscribed字段为false

**解决**：
```sql
-- 检查用户的计划
SELECT id, title, status, is_subscribed, start_date 
FROM training_plan 
WHERE user_id = 2 
ORDER BY start_date DESC;
```

### 问题5：日历上看不到日程
**原因**：
1. 日程生成失败
2. 日程的date字段不在当前查看的月份范围内
3. 日程的plan_id不正确

**解决**：
```sql
-- 检查生成的日程
SELECT id, plan_id, date, title, status 
FROM daily_schedule 
WHERE user_id = 2 
ORDER BY date DESC 
LIMIT 20;
```

## 数据库检查SQL

```sql
-- 1. 查看所有公开的训练计划
SELECT id, title, description, actions, is_public 
FROM training_plan 
WHERE is_public = 1;

-- 2. 查看用户的所有计划
SELECT id, title, status, is_subscribed, start_date, source_id 
FROM training_plan 
WHERE user_id = 2 
ORDER BY start_date DESC;

-- 3. 查看用户的所有日程
SELECT ds.id, ds.plan_id, ds.date, ds.title, ds.status, ds.source_type
FROM daily_schedule ds
WHERE ds.user_id = 2
ORDER BY ds.date DESC
LIMIT 50;

-- 4. 查看特定计划的日程
SELECT id, date, title, status, course_id
FROM daily_schedule
WHERE plan_id = <计划ID>
ORDER BY date;

-- 5. 检查日程关联的计划是否存在
SELECT ds.id, ds.plan_id, ds.date, ds.title, tp.title as plan_title
FROM daily_schedule ds
LEFT JOIN training_plan tp ON ds.plan_id = tp.id
WHERE ds.user_id = 2
ORDER BY ds.date DESC
LIMIT 20;
```

## 预期结果

✅ 订阅成功后：
1. 前端显示"已成功加入训练计划！"
2. 自动跳转到训练管理的"我的计划"标签
3. 能看到新订阅的计划，状态为"进行中"
4. 点击"查看日程"能看到日历上的训练安排
5. 后端日志显示成功创建了N个日程

## 如果仍然失败

请提供以下信息：
1. 前端控制台的完整日志
2. 后端控制台的完整日志
3. 数据库检查结果（使用上述SQL）
4. 订阅的训练计划ID
5. 选择的开始日期和每周训练日
