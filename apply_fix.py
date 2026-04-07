import sqlite3

conn = sqlite3.connect('health-backend/health.db')
cursor = conn.cursor()

# 读取并执行SQL
with open('fix_plan_actions.sql', 'r', encoding='utf-8') as f:
    sql = f.read()
    
cursor.executescript(sql)
conn.commit()

# 验证结果
cursor.execute('''
    SELECT id, title, 
           CASE 
               WHEN actions IS NULL THEN '❌ Actions为空'
               ELSE '✅ Actions已设置'
           END as status
    FROM training_plan 
    WHERE is_public = 1
''')

print('修复结果:')
for row in cursor.fetchall():
    print(f'  计划{row[0]}: {row[1]} - {row[2]}')

conn.close()
print('\n✅ 修复完成！')
