import sqlite3
import json
from datetime import datetime

db_path = 'd:/Coding/graduationProj002/health-backend/health.db'

def check_training_plans():
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    print("=" * 80)
    print("训练计划订阅诊断工具")
    print("=" * 80)
    
    # 1. 检查公开的训练计划
    print("\n1. 公开的训练计划库:")
    cursor.execute("""
        SELECT id, title, description, actions, is_public, user_id
        FROM training_plan 
        WHERE is_public = 1
        ORDER BY id
    """)
    public_plans = cursor.fetchall()
    if public_plans:
        for plan in public_plans:
            print(f"\n  计划ID: {plan[0]}")
            print(f"  标题: {plan[1]}")
            print(f"  描述: {plan[2][:50] if plan[2] else '无'}")
            print(f"  创建者ID: {plan[5]}")
            if plan[3]:
                try:
                    actions = json.loads(plan[3])
                    print(f"  Actions数量: {len(actions)}")
                    print(f"  Actions示例: {json.dumps(actions[:2], ensure_ascii=False, indent=4)}")
                except:
                    print(f"  Actions JSON解析失败: {plan[3][:100]}")
            else:
                print(f"  ⚠️  Actions为空！")
    else:
        print("  ⚠️  没有找到公开的训练计划！")
    
    # 2. 检查用户的计划
    print("\n2. 用户订阅的计划 (user_id=2):")
    cursor.execute("""
        SELECT id, title, status, is_subscribed, start_date, source_id, actions
        FROM training_plan 
        WHERE user_id = 2
        ORDER BY start_date DESC
    """)
    user_plans = cursor.fetchall()
    if user_plans:
        for plan in user_plans:
            print(f"\n  计划ID: {plan[0]}")
            print(f"  标题: {plan[1]}")
            print(f"  状态: {plan[2]}")
            print(f"  已订阅: {plan[3]}")
            print(f"  开始日期: {plan[4]}")
            print(f"  来源计划ID: {plan[5]}")
            if plan[6]:
                try:
                    actions = json.loads(plan[6])
                    print(f"  Actions数量: {len(actions)}")
                except:
                    print(f"  ⚠️  Actions JSON解析失败")
    else:
        print("  ⚠️  用户还没有订阅任何计划！")
    
    # 3. 检查日程安排
    print("\n3. 用户的日程安排 (user_id=2, 最近20条):")
    cursor.execute("""
        SELECT ds.id, ds.plan_id, ds.date, ds.title, ds.status, ds.source_type, tp.title as plan_title
        FROM daily_schedule ds
        LEFT JOIN training_plan tp ON ds.plan_id = tp.id
        WHERE ds.user_id = 2
        ORDER BY ds.date DESC
        LIMIT 20
    """)
    schedules = cursor.fetchall()
    if schedules:
        for sched in schedules:
            print(f"\n  日程ID: {sched[0]}")
            print(f"  计划ID: {sched[1]} (计划标题: {sched[6] or '未知'})")
            print(f"  日期: {sched[2]}")
            print(f"  标题: {sched[3]}")
            print(f"  状态: {sched[4]}")
            print(f"  来源类型: {sched[5]}")
    else:
        print("  ⚠️  用户没有任何日程安排！")
    
    # 4. 统计信息
    print("\n4. 统计信息:")
    cursor.execute("SELECT COUNT(*) FROM training_plan WHERE is_public = 1")
    print(f"  公开计划总数: {cursor.fetchone()[0]}")
    
    cursor.execute("SELECT COUNT(*) FROM training_plan WHERE user_id = 2")
    print(f"  用户计划总数: {cursor.fetchone()[0]}")
    
    cursor.execute("SELECT COUNT(*) FROM training_plan WHERE user_id = 2 AND is_subscribed = 1")
    print(f"  用户已订阅计划: {cursor.fetchone()[0]}")
    
    cursor.execute("SELECT COUNT(*) FROM training_plan WHERE user_id = 2 AND status = 'ACTIVE'")
    print(f"  用户活跃计划: {cursor.fetchone()[0]}")
    
    cursor.execute("SELECT COUNT(*) FROM daily_schedule WHERE user_id = 2")
    print(f"  用户日程总数: {cursor.fetchone()[0]}")
    
    cursor.execute("""
        SELECT COUNT(*) FROM daily_schedule ds
        JOIN training_plan tp ON ds.plan_id = tp.id
        WHERE ds.user_id = 2 AND tp.is_subscribed = 1
    """)
    print(f"  已订阅计划的日程: {cursor.fetchone()[0]}")
    
    # 5. 诊断建议
    print("\n" + "=" * 80)
    print("诊断建议:")
    print("=" * 80)
    
    if not public_plans:
        print("❌ 问题: 没有公开的训练计划")
        print("   解决: 需要在数据库中添加公开的训练计划，或通过管理员界面创建")
    
    if not user_plans:
        print("❌ 问题: 用户没有订阅任何计划")
        print("   解决: 从探索页面订阅一个训练计划")
    
    if user_plans and not schedules:
        print("❌ 问题: 有计划但没有日程")
        print("   解决: 检查计划的actions字段是否正确，查看后端日志")
        for plan in user_plans:
            if not plan[6]:
                print(f"   - 计划ID {plan[0]} 的actions为空")
    
    if schedules:
        schedule_count = len(schedules)
        print(f"✅ 用户有 {schedule_count} 条日程记录")
        
        # 检查是否有未来的日程
        today = datetime.now().strftime('%Y-%m-%d')
        cursor.execute("""
            SELECT COUNT(*) FROM daily_schedule 
            WHERE user_id = 2 AND date >= ?
        """, (today,))
        future_count = cursor.fetchone()[0]
        print(f"✅ 未来日程数量: {future_count}")
    
    conn.close()
    print("\n" + "=" * 80)

if __name__ == '__main__':
    check_training_plans()
