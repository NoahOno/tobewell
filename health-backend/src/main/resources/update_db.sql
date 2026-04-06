DROP TABLE IF EXISTS training_record;
CREATE TABLE training_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    source_type TEXT DEFAULT 'SCHEDULE',
    source_id INTEGER NOT NULL,
    complete_duration INTEGER,
    difficulty TEXT,
    feeling TEXT,
    record_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- Add count_mode and topic_stat_mode columns to activity table
ALTER TABLE activity ADD COLUMN count_mode TEXT;
ALTER TABLE activity ADD COLUMN topic_stat_mode TEXT;

-- Update existing challenge activities (type 2) to use DAYS mode by default
UPDATE activity SET count_mode = 'DAYS' WHERE activity_type = 2 AND count_mode IS NULL;

-- Update existing topic activities (type 3) to use COUNT mode by default
UPDATE activity SET topic_stat_mode = 'COUNT' WHERE activity_type = 3 AND topic_stat_mode IS NULL;

-- Type 1 check-in activities: default count mode (if column exists from prior migrations)
UPDATE activity SET count_mode = 'DAYS' WHERE activity_type = 1 AND (count_mode IS NULL OR count_mode = '');

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

INSERT INTO course (title, description, category, difficulty, duration_minutes, actions_json, is_public) 
VALUES 
('15分钟暴汗燃脂跑', '核心有氧快速燃脂，无需器械', '通用', '初级', 15, '[{"name":"高抬腿","sets":"3组x40秒"},{"name":"开合跳","sets":"3组x40秒"}]', 1),
('核心力量雕刻', '专注腹部与核心肌群的撕裂感', '通用', '中级', 20, '[{"name":"卷腹","sets":"4组x15次"},{"name":"平板支撑","sets":"3组x60秒"}]', 1);
