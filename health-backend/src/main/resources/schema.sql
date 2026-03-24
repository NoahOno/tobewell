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

-- Daily Schedule Table (For Training Plan Execution)
CREATE TABLE IF NOT EXISTS daily_schedule (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    source_type TEXT DEFAULT 'PLAN', -- 'PLAN' or 'COURSE'
    plan_id INTEGER,
    course_id INTEGER,
    date DATE NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    actions TEXT,
    status TEXT DEFAULT 'PENDING', -- PENDING, COMPLETED, SKIPPED
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (plan_id) REFERENCES training_plan(id)
);

-- Training Record Table (For Daily Schedule Check-in or Single Course Completion)
CREATE TABLE IF NOT EXISTS training_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    source_type TEXT DEFAULT 'SCHEDULE', -- 'SCHEDULE' or 'COURSE'
    source_id INTEGER NOT NULL,          -- daily_schedule(id) or course(id)
    complete_duration INTEGER,
    difficulty TEXT, -- TOO_EASY, GOOD, TOO_HARD
    feeling TEXT,
    record_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- Course Table (Single standalone workouts)
CREATE TABLE IF NOT EXISTS course (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    category TEXT,
    difficulty TEXT,
    duration_minutes INTEGER,
    actions_json TEXT,
    is_public BOOLEAN DEFAULT 0,
    creator_id INTEGER,
    cover_image TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES sys_user(id)
);
-- Exercise Library Table
CREATE TABLE IF NOT EXISTS exercise (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    muscle TEXT,
    type TEXT,
    equipment TEXT,
    difficulty TEXT,
    instruction TEXT,
    common_errors TEXT, -- JSON string
    recommended_sets TEXT,
    image_url TEXT,
    video_url TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Mock Data for Exercises
INSERT OR IGNORE INTO exercise (id, name, muscle, type, equipment, difficulty, instruction, common_errors, recommended_sets)
VALUES 
(1, '深蹲 (Squat)', '腿部', '力量', '无器械', '初级', '双脚打开与肩同宽，腰背挺直，下蹲至大腿与地面平行。', '["膝盖内扣", "弯腰驼背", "重心不稳"]', '3-4组，每组12-15次'),
(2, '平板支撑 (Plank)', '核心', '力量', '无器械', '初级', '手肘撑地，身体呈一条直线，收紧腹部和臀部。', '["塌腰", "撅屁股", "低头或仰头"]', '3-4组，每组30-60秒'),
(3, '开合跳 (Jumping Jacks)', '全身', '有氧', '无器械', '初级', '跳跃时双脚分开，同时双手举过头顶击掌，落地时并拢。', '["落地过重", "手臂伸不直"]', '3组，每组30-45秒'),
(4, '波比跳 (Burpees)', '全身', '有氧', '无器械', '高级', '下蹲、后踢腿成俯卧撑姿势，完成一个俯卧撑后收腿向上跳跃。', '["核心没有收紧", "跳跃高度不够"]', '3组，每组10-15次'),
(5, '杠铃卧推 (Bench Press)', '胸部', '力量', '杠铃', '中级', '平躺在长椅上，双手握住杠铃，缓慢下放至胸口，然后推起。', '["手腕弯曲", "腰部过度反弓", "下放速度过快"]', '4组，每组8-12次'),
(6, '哑铃飞鸟 (Dumbbell Flyes)', '胸部', '力量', '哑铃', '中级', '仰卧，双手持哑铃，手臂微屈，像拥抱一棵大树一样向外展开。', '["手臂伸得过直", "下放幅度过大导致肩膀受伤"]', '3组，每组10-15次'),
(7, '引体向上 (Pull-ups)', '背部', '力量', '单杠', '高级', '双手握住单杠，收紧核心，背部发力将身体向上拉起，直到下巴过杠。', '["利用惯性甩动身体", "手臂发力过多"]', '4组，每组力竭'),
(8, '哑铃划船 (Dumbbell Row)', '背部', '力量', '哑铃', '中级', '单膝跪在长椅上，另一只手持哑铃向后上方拉起，背部发力。', '["身体过度扭转", "依靠手臂力量拉起"]', '4组，每组10-12次');

-- Activity System (Trending/Administration/Community Integration)
-- The activity feature reuses daily_schedule + training_record for task execution.

-- Activity Base Table
CREATE TABLE IF NOT EXISTS activity (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    cover_image TEXT,
    description_html TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    template_type TEXT NOT NULL, -- 'PLAN' or 'COURSE'
    template_id INTEGER NOT NULL, -- training_plan.id or course.id
    required_days INTEGER NOT NULL DEFAULT 7,
    pinned INTEGER DEFAULT 0, -- 1 pinned, 0 normal
    status TEXT DEFAULT 'ONLINE', -- ONLINE/OFFLINE
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- User Participation Table
CREATE TABLE IF NOT EXISTS activity_participation (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    activity_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    status TEXT DEFAULT 'APPLIED', -- APPLIED/COMPLETED/CANCELLED
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_time DATETIME,
    UNIQUE(activity_id, user_id),
    FOREIGN KEY (activity_id) REFERENCES activity(id)
);

-- Bind each activity day to a concrete daily_schedule row
CREATE TABLE IF NOT EXISTS activity_task (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    participation_id INTEGER NOT NULL,
    daily_schedule_id INTEGER NOT NULL,
    scheduled_date DATE NOT NULL,
    task_index INTEGER NOT NULL,
    status TEXT DEFAULT 'PENDING', -- PENDING/COMPLETED/SKIPPED
    completed_time DATETIME,
    UNIQUE(daily_schedule_id),
    UNIQUE(participation_id, task_index),
    FOREIGN KEY (participation_id) REFERENCES activity_participation(id)
);

-- Activity completed content (for forwarding)
CREATE TABLE IF NOT EXISTS activity_dynamic (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    activity_id INTEGER NOT NULL,
    participation_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (activity_id) REFERENCES activity(id),
    FOREIGN KEY (participation_id) REFERENCES activity_participation(id)
);

CREATE INDEX IF NOT EXISTS idx_activity_status ON activity(status);
CREATE INDEX IF NOT EXISTS idx_activity_pinned ON activity(pinned);
CREATE INDEX IF NOT EXISTS idx_activity_start_time ON activity(start_time);
CREATE INDEX IF NOT EXISTS idx_activity_participation_activity ON activity_participation(activity_id);
CREATE INDEX IF NOT EXISTS idx_activity_task_participation ON activity_task(participation_id);
CREATE INDEX IF NOT EXISTS idx_activity_dynamic_activity ON activity_dynamic(activity_id);
