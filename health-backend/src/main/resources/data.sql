-- Initial seed data for health-platform
-- This file is loaded separately from schema.sql to keep schema creation and dataset initialization separated.

-- Admin and user accounts
INSERT OR IGNORE INTO sys_user (id, username, password, nickname, avatar, role, status)
VALUES
    (1, 'admin', '123456', 'Administrator', NULL, 'ADMIN', 1),
    (2, 'user1', '123456', '小明', 'http://localhost:8080/uploads/avatar_basketball.png', 'USER', 1),
    (3, 'user2', '123456', '李四', 'http://localhost:8080/uploads/avatar_girl.png', 'USER', 1),
    (4, 'user3', '123456', '王五', 'http://localhost:8080/uploads/avatar_hanfu.png', 'USER', 1);

-- Community posts
INSERT OR IGNORE INTO community_post (id, user_id, title, content, category, tags, like_count, comment_count, view_count, images)
VALUES
    (1, 2, '减脂期间想吃零食怎么办？', '分享几个减脂期满足口腹之欲的小技巧：1. 用水果代替高糖零食；2. 选择低卡路里的爆米花；3. 坚果适量可以增加饱腹感。', '饮食', '减脂,饮食技巧,零食', 23, 5, 1024, 'http://localhost:8080/uploads/post_snacks.png'),
    (2, 2, '我坚持跑步30天的变化！', '从第一天跑2公里喘不过气，到现在可以轻松跑10公里。分享我这一个月的心得和体型变化数据。', '运动', '跑步,坚持,变化', 56, 12, 1880, 'http://localhost:8080/uploads/post_running.png'),
    (3, 3, '睡眠质量变差了，有什么改善建议？', '最近工作压力大，睡眠质量很差，请问有什么好的方法可以改善睡眠？已经试过不看手机，但效果不明显。', '睡眠', '睡眠,压力,求助', 8, 15, 760, NULL),
    (4, 4, 'HIIT训练后肌肉酸痛超严重，正常吗？', 'HIIT第二天腿完全废了，这是正常的迟发性肌肉酸痛（DOMS）还是受伤了？分享我的判断方法。', '运动', 'HIIT,肌肉酸痛,恢复', 34, 8, 930, 'http://localhost:8080/uploads/post_hit_soreness.png'),
    (5, 3, '如何在办公室保持健康饮食？', '工作日午餐总是外卖，想要更健康一些，有没有便捷且不会太贵的建议？', '饮食', '办公,健康饮食,外卖', 17, 3, 640, NULL);

-- Health metrics for user1
INSERT OR IGNORE INTO health_metric (user_id, name, value, unit, record_time)
VALUES
    (2, '体重', 70.5, 'kg', datetime('now', '-7 days')),
    (2, '体重', 70.2, 'kg', datetime('now', '-1 day')),
    (2, '体重', 69.8, 'kg', datetime('now')),
    (2, '步数', 8200, '步', datetime('now', '-1 day')),
    (2, '步数', 11600, '步', datetime('now')),
    (2, '身高', 175.0, 'cm', datetime('now', '-30 days')),
    (2, '心率', 72.0, 'bpm', datetime('now', '-1 hour')),
    (2, '静息心率', 62.0, 'bpm', datetime('now', '-1 day'));

-- Workout records for user1
INSERT OR IGNORE INTO workout_record (user_id, type, duration, record_time)
VALUES
    (2, '跑步', 30, datetime('now', '-2 days')),
    (2, '骑行', 45, datetime('now', '-1 day')),
    (2, '游泳', 40, datetime('now', '-3 days')),
    (2, '力量训练', 60, datetime('now'));

-- Exercise library entries
INSERT OR IGNORE INTO exercise (id, name, muscle, type, equipment, difficulty, instruction, common_errors, recommended_sets, image_url, cover_image, is_public, duration)
VALUES
    (1, '深蹲 (Squat)', '腿部', '力量', '无器械', '初级', '双脚打开与肩同宽，腰背挺直，下蹲至大腿与地面平行。', '["膝盖内扣", "弯腰驼背", "重心不稳"]', '3-4组，每组12-15次', 'http://localhost:8080/uploads/squat_flow.png', 'http://localhost:8080/uploads/squat_cover.png', 1, '12分钟'),
    (2, '平板支撑 (Plank)', '核心', '力量', '无器械', '初级', '手肘撑地，身体呈一条直线，收紧腹部和臀部。', '["塌腰", "撅屁股", "低头或仰头"]', '3-4组，每组30-60秒', 'http://localhost:8080/uploads/plank_flow.png', 'http://localhost:8080/uploads/plank_cover.png', 1, '10分钟'),
    (3, '开合跳 (Jumping Jacks)', '全身', '有氧', '无器械', '初级', '跳跃时双脚分开，同时双手举过头顶击掌，落地时并拢。', '["落地过重", "手臂伸不直"]', '3组，每组30-45秒', 'http://localhost:8080/uploads/jumping_jacks_flow.png', 'http://localhost:8080/uploads/jumping_jacks_cover.png', 1, '8分钟'),
    (4, '波比跳 (Burpees)', '全身', '有氧', '无器械', '高级', '下蹲、后踢腿成俯卧撑姿势，完成一个俯卧撑后收腿向上跳跃。', '["核心没有收紧", "跳跃高度不够"]', '3组，每组10-15次', 'http://localhost:8080/uploads/burpees_flow.png', 'http://localhost:8080/uploads/burpees_cover.png', 1, '15分钟'),
    (5, '卧推 (Bench Press)', '胸部', '力量', '杠铃', '中级', '平躺在长椅上，双手握住杠铃，缓慢下放至胸口，然后推起。', '["手腕弯曲", "腰部过度反弓", "下放速度过快"]', '3组，每组8-12次', 'http://localhost:8080/uploads/bench_press_flow.png', 'http://localhost:8080/uploads/bench_press_cover.png', 1, '15分钟'),
    (6, '哑铃飞鸟 (Dumbbell Flyes)', '胸部', '力量', '哑铃', '中级', '仰卧，双手持哑铃，手臂微屈，像拥抱一棵大树一样向外展开。', '["手臂伸得过直", "下放幅度过大导致肩膀受伤"]', '3组，每组10-15次', 'http://localhost:8080/uploads/dumbbell_fly_flow.png', 'http://localhost:8080/uploads/dumbbell_fly_cover.png', 1, '14分钟'),
    (7, '引体向上 (Pull-ups)', '背部', '力量', '单杠', '高级', '双手握住单杠，收紧核心，背部发力将身体向上拉起，直到下巴过杠。', '["利用惯性甩动身体", "手臂发力过多"]', '4组，每组力竭', 'http://localhost:8080/uploads/pullups_flow.png', 'http://localhost:8080/uploads/pullups_cover.png', 1, '12分钟'),
    (8, '哑铃划船 (Dumbbell Row)', '背部', '力量', '哑铃', '中级', '单膝跪在长椅上，另一只手持哑铃向后上方拉起，背部发力。', '["身体过度扭转", "依靠手臂力量拉起"]', '4组，每组10-12次', 'http://localhost:8080/uploads/dumbbell_row_flow.png', 'http://localhost:8080/uploads/dumbbell_row_cover.png', 1, '14分钟');

-- AI provider and service configuration
INSERT OR IGNORE INTO ai_provider_config (id, name, provider_type, base_url, api_key, model, enabled, is_default)
VALUES
    (1, 'OpenRouter Free', 'openrouter', 'https://openrouter.ai/api/v1', '', 'google/gemma-3-27b-it:free', 1, 1);

INSERT OR IGNORE INTO ai_service_config (id, service_key, name, description, tag_label, style_label, system_prompt, sort_order, enabled, api_config_id, default_intent)
VALUES
    (1, 'mental_counseling', '心理咨询', '情绪疏导、压力管理、正念引导。', '情绪支持', '温和共情', '你是一个耐心、克制、共情的心理健康助手。关注情绪识别、压力调节和可执行的小建议，不做医学诊断。', 10, 1, 1, 'chat'),
    (2, 'fitness_coach', '健身教练', '围绕训练目标给出结构化指导。', '训练指导', '结构化教练', '你是一个专业训练教练。回答要结构化、强调动作安全、训练频率和渐进负荷。', 20, 1, 1, 'training_plan'),
    (3, 'rehab_coach', '康复指导', '偏保守的恢复训练建议。', '康复管理', '风险边界清晰', '你是一个保守的康复训练助手。优先提醒风险边界、疼痛反馈和必要时就医，不鼓励带伤硬练。', 30, 1, 1, 'chat'),
    (4, 'nutrition_coach', '营养指导', '饮食策略与可执行替换建议。', '饮食策略', '可执行规划', '你是一个营养规划助手。强调可持续饮食策略、替换方案和生活化执行，不夸大效果。', 40, 1, 1, 'chat');

-- Course content
INSERT OR IGNORE INTO course (id, title, description, category, difficulty, duration_minutes, actions_json, is_public, creator_id, cover_image, audience, create_time)
VALUES
    (1, '唤醒晨间瑜伽', '每天早晨15分钟的全身拉伸与瑜伽跟练，帮助唤醒身体活力。', '瑜伽拉伸', '初级', 15, '[{"name":"平板支撑 (Plank)","sets":"2组，每组40秒","rest":"20秒"},{"name":"深蹲 (Squat)","sets":"2组，每组15次","rest":"25秒"},{"name":"开合跳 (Jumping Jacks)","sets":"2组，每组30秒","rest":"20秒"}]', 1, 1, 'http://localhost:8080/uploads/course_morning_yoga.png', '久坐上班族 / 晨练新手', datetime('now')),
    (2, '心肺燃烧HIIT', '高强度间歇训练，快速燃脂。', '减脂', '高级', 20, '[{"name":"开合跳 (Jumping Jacks)","sets":"3组，每组45秒","rest":"20秒"},{"name":"波比跳 (Burpees)","sets":"3组，每组12次","rest":"30秒"},{"name":"平板支撑 (Plank)","sets":"3组，每组45秒","rest":"20秒"},{"name":"深蹲 (Squat)","sets":"3组，每组20次","rest":"30秒"}]', 1, 1, 'http://localhost:8080/uploads/course_hiit.png', '减脂进阶 / 心肺提升', datetime('now')),
    (3, '居家腹肌雕刻', '睡前10分钟核心燃脂挑战。', '塑形', '中级', 10, '[{"name":"平板支撑 (Plank)","sets":"3组，每组50秒","rest":"20秒"},{"name":"开合跳 (Jumping Jacks)","sets":"3组，每组30秒","rest":"15秒"},{"name":"波比跳 (Burpees)","sets":"2组，每组10次","rest":"30秒"}]', 1, 1, 'http://localhost:8080/uploads/course_home_abs.png', '居家训练 / 核心塑形', datetime('now')),
    (4, '家庭核心强化', '15分钟核心稳定训练，适合在家做。', '塑形', '中级', 15, '[{"name":"平板支撑 (Plank)","sets":"3组，每组45秒","rest":"20秒"},{"name":"深蹲 (Squat)","sets":"3组，每组15次","rest":"20秒"},{"name":"开合跳 (Jumping Jacks)","sets":"3组，每组30秒","rest":"20秒"}]', 0, 4, 'http://localhost:8080/uploads/course_home_abs.png', '家庭场景 / 核心稳定', datetime('now'));

-- Training plans
INSERT OR IGNORE INTO training_plan (id, user_id, title, description, category, duration, actions, is_public, start_date, end_date, cover_image, audience)
VALUES
    (1, 1, '四周减脂挑战计划', '针对新手的四周平缓减脂周期。', '减脂', '4周', '[{"type":"训练","title":"心肺燃烧HIIT","courseId":2,"courseTitle":"心肺燃烧HIIT"},{"type":"休息","title":"主动恢复"},{"type":"训练","title":"居家腹肌雕刻","courseId":3,"courseTitle":"居家腹肌雕刻"},{"type":"休息","title":"拉伸恢复"},{"type":"训练","title":"唤醒晨间瑜伽","courseId":1,"courseTitle":"唤醒晨间瑜伽"},{"type":"休息","title":"步行恢复"},{"type":"训练","title":"心肺燃烧HIIT","courseId":2,"courseTitle":"心肺燃烧HIIT"}]', 1, date('now'), date('now', '+28 days'), 'http://localhost:8080/uploads/plan_4week_fat_loss.png', '减脂新手 / 居家训练'),
    (2, 1, '零基础增肌入门', '快速掌握力量训练的核心要领。', '增肌', '6周', '[{"type":"训练","title":"家庭核心强化","courseId":4,"courseTitle":"家庭核心强化"},{"type":"休息","title":"轻松恢复"},{"type":"训练","title":"居家腹肌雕刻","courseId":3,"courseTitle":"居家腹肌雕刻"},{"type":"休息","title":"拉伸恢复"},{"type":"训练","title":"家庭核心强化","courseId":4,"courseTitle":"家庭核心强化"},{"type":"休息","title":"休息日"},{"type":"训练","title":"唤醒晨间瑜伽","courseId":1,"courseTitle":"唤醒晨间瑜伽"}]', 1, date('now'), date('now', '+42 days'), 'http://localhost:8080/uploads/plan_beginner_muscle_build.png', '增肌入门 / 力量基础'),
    (3, 2, '周末快速燃脂计划', '一套适合零基础用户的三天燃脂循环训练。', '减脂', '3天', '[{"type":"训练","title":"心肺燃烧HIIT","courseId":2,"courseTitle":"心肺燃烧HIIT"},{"type":"训练","title":"居家腹肌雕刻","courseId":3,"courseTitle":"居家腹肌雕刻"},{"type":"训练","title":"唤醒晨间瑜伽","courseId":1,"courseTitle":"唤醒晨间瑜伽"}]', 0, date('now'), date('now', '+3 days'), 'http://localhost:8080/uploads/plan_4week_fat_loss.png', '周末减脂 / 零基础');

-- Activity definitions
INSERT OR IGNORE INTO activity (id, title, cover_image, description_html, start_time, end_time, activity_type, template_type, template_id, topic_name, reward_points, required_days, status)
VALUES
    (1, '晨间早起早打卡', 'http://localhost:8080/uploads/activity_early_rise.png', '<p>每天早上8点前过来打卡签到，记录你今日的情绪与状态，迎接美好的一天！</p>', datetime('now', '-2 days'), datetime('now', '+14 days'), 1, NULL, NULL, NULL, 50, 7, 'ONLINE'),
    (2, '四周马甲线速成挑战', 'http://localhost:8080/uploads/activity_ab_challenge.png', '<p>加入本周期的官方专属减脂计划，坚持4周完成训练并记录体脂即可获得永久头像框！</p>', datetime('now', '-5 days'), datetime('now', '+23 days'), 2, 'PLAN', 1, NULL, 500, 21, 'ONLINE'),
    (3, '春日低脂便当大赏', 'http://localhost:8080/uploads/activity_lowfat_bento.png', '<p>拍下你今天亲自制作的低脂便当，带上话题分享到社区，我们将为最具创意的50名用户颁发奖品！</p>', datetime('now', '-1 day'), datetime('now', '+10 days'), 3, NULL, NULL, '#春日低脂便当', 100, 3, 'ONLINE');

-- Community relationships
INSERT OR IGNORE INTO user_follow (follower_id, followee_id)
VALUES
    (2, 3),
    (3, 2),
    (4, 2);

INSERT OR IGNORE INTO collection (user_id, target_id, target_type, target_title)
VALUES
    (2, 1, 'POST', '减脂期间想吃零食怎么办？'),
    (3, 2, 'POST', '我坚持跑步30天的变化！');

-- Pending shared training submission requests
INSERT OR IGNORE INTO resource_submission (id, submitter_id, resource_type, resource_id, status, note, reviewer_id, create_time)
VALUES
    (1, 2, 'PLAN', 3, 'PENDING', '请审核我这套周末燃脂计划，适合初学者。', NULL, datetime('now', '-2 days')),
    (2, 3, 'COURSE', 4, 'PENDING', '这是一套家庭核心训练课程，希望加入共享库。', NULL, datetime('now', '-1 day'));
