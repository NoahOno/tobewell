import sqlite3
conn = sqlite3.connect('d:/Coding/graduationProj002/health-backend/health.db')
cursor = conn.cursor()
print("Activities:")
cursor.execute("SELECT id, title, status FROM activity")
for row in cursor.fetchall():
    print(row)
print("\nPosts:")
cursor.execute("SELECT id, title, status FROM community_post")
for row in cursor.fetchall():
    print(row)
conn.close()
