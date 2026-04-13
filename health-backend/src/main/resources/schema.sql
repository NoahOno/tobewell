-- User Table
CREATE TABLE IF NOT EXISTS sys_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    nickname TEXT,
    avatar TEXT,
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
    images TEXT,
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
    source_id INTEGER,
    is_subscribed BOOLEAN DEFAULT 0,
    cover_image TEXT,
    audience TEXT,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

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
    target_type TEXT NOT NULL, -- 'CONTENT', 'PLAN', 'POST', 'COURSE'
    target_title TEXT,
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
    audience TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES sys_user(id)
);

-- Resource Submission Table (User submit private plan/course for admin review to become public library)
CREATE TABLE IF NOT EXISTS resource_submission (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    submitter_id INTEGER NOT NULL,
    resource_type TEXT NOT NULL, -- 'PLAN' or 'COURSE'
    resource_id INTEGER NOT NULL,
    status TEXT DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    note TEXT,
    reviewer_id INTEGER,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    review_time DATETIME,
    UNIQUE(submitter_id, resource_type, resource_id, status),
    FOREIGN KEY (submitter_id) REFERENCES sys_user(id),
    FOREIGN KEY (reviewer_id) REFERENCES sys_user(id)
);

CREATE TABLE IF NOT EXISTS ai_provider_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    provider_type TEXT NOT NULL,
    base_url TEXT,
    api_key TEXT,
    model TEXT,
    extra_headers_json TEXT,
    enabled BOOLEAN DEFAULT 1,
    is_default BOOLEAN DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_service_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service_key TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    tag_label TEXT,
    style_label TEXT,
    system_prompt TEXT,
    sort_order INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT 1,
    api_config_id INTEGER,
    default_intent TEXT DEFAULT 'chat',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (api_config_id) REFERENCES ai_provider_config(id)
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
    cover_image TEXT,
    is_public BOOLEAN DEFAULT 0,
    duration TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

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
    
    activity_type INTEGER NOT NULL DEFAULT 1, -- 1: General, 2: Content, 3: Topic
    template_type TEXT, -- Used for type 2: 'PLAN' or 'COURSE'
    template_id INTEGER, -- Used for type 2
    topic_name TEXT, -- Used for type 3
    count_mode TEXT, -- Challenge count mode: COUNT (by times) or DAYS (by days)
    topic_stat_mode TEXT, -- Topic stat mode: SHARED, DAYS, COUNT
    reward_points INTEGER DEFAULT 0,
    
    required_days INTEGER NOT NULL DEFAULT 7,
    pinned INTEGER DEFAULT 0, -- 1 pinned, 0 normal
    status TEXT DEFAULT 'DRAFT', -- DRAFT/ONLINE/OFFLINE
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

-- Activity task rows: either linked to daily_schedule (positive daily_schedule_id) or synthetic negative id for check-ins / generated challenge days
CREATE TABLE IF NOT EXISTS activity_task (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    participation_id INTEGER NOT NULL,
    daily_schedule_id INTEGER,
    scheduled_date DATE,
    task_date DATE,
    task_type TEXT,
    task_id INTEGER,
    task_index INTEGER,
    status TEXT DEFAULT 'PENDING',
    completed_time DATETIME,
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
