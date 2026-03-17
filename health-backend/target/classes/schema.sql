-- User Table
CREATE TABLE IF NOT EXISTS sys_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    nickname TEXT,
    role TEXT DEFAULT 'USER',
    status INTEGER DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Health Content Table (legacy, now community posts)
CREATE TABLE IF NOT EXISTS health_content (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT,
    author_id INTEGER,
    category TEXT,
    tags TEXT,
    status TEXT DEFAULT 'published',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES sys_user(id)
);

-- Community Post Table  
CREATE TABLE IF NOT EXISTS community_post (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT,
    category TEXT DEFAULT '综合',
    tags TEXT,
    like_count INTEGER DEFAULT 0,
    collection_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    status TEXT DEFAULT 'published',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- User Follow Table
CREATE TABLE IF NOT EXISTS user_follow (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    follower_id INTEGER NOT NULL,
    followee_id INTEGER NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(follower_id, followee_id),
    FOREIGN KEY (follower_id) REFERENCES sys_user(id),
    FOREIGN KEY (followee_id) REFERENCES sys_user(id)
);

-- Index for search performance
CREATE INDEX IF NOT EXISTS idx_post_title ON community_post(title);
CREATE INDEX IF NOT EXISTS idx_post_tags ON community_post(tags);
CREATE INDEX IF NOT EXISTS idx_post_category ON community_post(category);
CREATE INDEX IF NOT EXISTS idx_post_user ON community_post(user_id);

-- Mock Community Posts
INSERT OR IGNORE INTO community_post (user_id, title, content, category, tags, like_count, comment_count)
VALUES
(2, '减脂期间想吃零食怎么办？', '分享几个减脂期满足口腹之欲的小技巧：1. 用水果代替高糖零食；2. 选择低卡路里的爆米花；3. 坚果适量可以增加饱腹感...', '饮食', '减脂,饮食技巧,零食', 23, 5),
(2, '我坚持跑步30天的变化！', '从第一天跑2公里喘不过气，到现在可以轻松跑10公里。分享我这一个月的心得和体型变化数据...', '运动', '跑步,坚持,变化', 56, 12),
(2, '睡眠质量变差了，有什么改善建议？', '最近工作压力大，睡眠质量很差，请问有什么好的方法可以改善睡眠？已经试过不看手机，但效果不明显...', '睡眠', '睡眠,压力,求助', 8, 15),
(2, 'HIIT训练后肌肉酸痛超严重，正常吗？', 'HIIT第二天腿完全废了，这是正常的迟发性肌肉酸痛（DOMS）还是受伤了？分享我的判断方法...', '运动', 'HIIT,肌肉酸痛,恢复', 34, 8);

-- Health Metric Table
CREATE TABLE IF NOT EXISTS health_metric (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    value REAL NOT NULL,
    unit TEXT,
    record_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- Initial Data
INSERT OR IGNORE INTO sys_user (username, password, nickname, role) 
VALUES ('admin', '123456', 'Administrator', 'ADMIN');

INSERT OR IGNORE INTO sys_user (username, password, nickname, role) 
VALUES ('user', '123456', 'Test User', 'USER');

-- Training Plan Table
CREATE TABLE IF NOT EXISTS training_plan (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    status TEXT DEFAULT 'PLANNING', -- PLANNING, IN_PROGRESS, COMPLETED
    category TEXT,
    content TEXT,
    duration TEXT,
    actions TEXT,
    is_public BOOLEAN DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- Default metrics for test user
INSERT OR IGNORE INTO health_metric (user_id, name, value, unit, record_time)
VALUES (2, '体重', 70.5, 'kg', datetime('now', '-1 day'));
INSERT OR IGNORE INTO health_metric (user_id, name, value, unit, record_time)
VALUES (2, '体重', 70.2, 'kg', datetime('now'));
INSERT OR IGNORE INTO health_metric (user_id, name, value, unit, record_time)
VALUES (2, '步数', 8000, '步', datetime('now', '-1 day'));
INSERT OR IGNORE INTO health_metric (user_id, name, value, unit, record_time)
VALUES (2, '步数', 12000, '步', datetime('now'));

/* Mock Training Plans removed to allow clean test environment */

-- Stars Table
CREATE TABLE IF NOT EXISTS health_star (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    target_id INTEGER NOT NULL,
    target_type TEXT NOT NULL, -- 'CONTENT' or 'PLAN'
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, target_id, target_type),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- Comments Table
CREATE TABLE IF NOT EXISTS health_comment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    target_id INTEGER NOT NULL,
    target_type TEXT NOT NULL, -- 'CONTENT' or 'PLAN'
    parent_id INTEGER DEFAULT 0,
    content TEXT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- Subscriptions Table
CREATE TABLE IF NOT EXISTS subscription (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    plan_id INTEGER NOT NULL,
    status TEXT DEFAULT 'ACTIVE', -- 'ACTIVE', 'CANCELLED', 'FINISHED'
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, plan_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (plan_id) REFERENCES training_plan(id)
);

-- Collections Table
CREATE TABLE IF NOT EXISTS collection (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    target_id INTEGER NOT NULL,
    target_type TEXT NOT NULL, -- 'CONTENT' or 'PLAN'
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, target_id, target_type),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- Health Goals Table
CREATE TABLE IF NOT EXISTS health_goal (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL, -- e.g. 'Weight', 'Steps'
    target_value REAL NOT NULL,
    current_value REAL DEFAULT 0,
    unit TEXT,
    deadline DATE,
    status TEXT DEFAULT 'IN_PROGRESS', -- 'IN_PROGRESS', 'ACHIEVED', 'FAILED'
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
-- Workout Record Table
CREATE TABLE IF NOT EXISTS workout_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    duration INTEGER NOT NULL,
    record_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- Extended Health Metrics Mock Data
INSERT OR IGNORE INTO health_metric (user_id, name, value, unit, record_time)
VALUES 
(2, '身高', 175.0, 'cm', datetime('now', '-30 days')),
(2, '心率', 72.0, 'bpm', datetime('now', '-1 hour')),
(2, '最大心率', 185.0, 'bpm', datetime('now', '-2 days')),
(2, '静息心率', 62.0, 'bpm', datetime('now', '-1 day')),
(2, '体重', 72.5, 'kg', datetime('now', '-5 days')),
(2, '体重', 71.8, 'kg', datetime('now', '-2 days')),
(2, '步数', 12000, '步', datetime('now', '-1 day')),
(2, '步数', 9500, '步', datetime('now'));

-- Mock Workout Data
INSERT OR IGNORE INTO workout_record (user_id, type, duration, record_time)
VALUES 
(2, '跑步', 30, datetime('now', '-2 days')),
(2, '骑行', 45, datetime('now', '-1 day')),
(2, '游泳', 40, datetime('now', '-3 days')),
(2, '力量训练', 60, datetime('now'));
