import sqlite3

conn = sqlite3.connect('health-backend/health.db')
cursor = conn.cursor()

print("=" * 80)
print("清理旧的订阅数据，准备重新测试")
print("=" * 80)

# 1. 删除用户之前的计划副本（这些没有正确的actions）
print("\n1. 删除用户旧的计划副本...")
cursor.execute("DELETE FROM daily_schedule WHERE user_id = 2")
deleted_schedules = cursor.rowcount
print(f"   删除了 {deleted_schedules} 条旧日程")

cursor.execute("DELETE FROM training_plan WHERE user_id = 2")
deleted_plans = cursor.rowcount
print(f"   删除了 {deleted_plans} 个旧计划")

conn.commit()

# 2. 验证清理结果
print("\n2. 验证清理结果...")
cursor.execute("SELECT COUNT(*) FROM training_plan WHERE user_id = 2")
print(f"   用户计划数: {cursor.fetchone()[0]}")

cursor.execute("SELECT COUNT(*) FROM daily_schedule WHERE user_id = 2")
print(f"   用户日程数: {cursor.fetchone()[0]}")

# 3. 显示可用的公开计划
print("\n3. 可用的公开训练计划:")
cursor.execute("SELECT id, title, json_array_length(actions) as action_count FROM training_plan WHERE is_public = 1")
for row in cursor.fetchall():
    print(f"   计划{row[0]}: {row[1]} ({row[2]}个训练日)")

print("\n" + "=" * 80)
print("✅ 清理完成！现在可以重新订阅训练计划了")
print("=" * 80)
print("\n测试步骤:")
print("1. 重启后端服务（让新的日志代码生效）")
print("2. 打开浏览器开发者工具 (F12)")
print("3. 登录系统 (user / 123456)")
print("4. 导航到'探索' -> '训练计划'")
print("5. 选择一个计划并点击'加入训练'")
print("6. 观察前端控制台和后端控制台的日志")
print("7. 验证训练管理和训练日历页面")

conn.close()
