-- 修复训练计划的actions字段
-- 这些JSON数据定义了计划的训练日程安排

-- 计划1: 四周减脂挑战计划 (28天，每周3练)
UPDATE training_plan 
SET actions = '[
  {"type": "训练", "courseId": 1, "title": "唤醒晨间瑜伽"},
  {"type": "训练", "courseId": 2, "title": "心肺燃烧HIIT"},
  {"type": "训练", "courseId": 3, "title": "居家腹肌雕刻"},
  {"type": "训练", "courseId": 1, "title": "唤醒晨间瑜伽"},
  {"type": "训练", "courseId": 2, "title": "心肺燃烧HIIT"},
  {"type": "训练", "courseId": 3, "title": "居家腹肌雕刻"},
  {"type": "训练", "courseId": 1, "title": "唤醒晨间瑜伽"},
  {"type": "训练", "courseId": 2, "title": "心肺燃烧HIIT"},
  {"type": "训练", "courseId": 3, "title": "居家腹肌雕刻"},
  {"type": "训练", "courseId": 1, "title": "唤醒晨间瑜伽"},
  {"type": "训练", "courseId": 2, "title": "心肺燃烧HIIT"},
  {"type": "训练", "courseId": 3, "title": "居家腹肌雕刻"}
]'
WHERE id = 1;

-- 计划2: 零基础增肌入门 (28天，每周3练)
UPDATE training_plan 
SET actions = '[
  {"type": "训练", "courseId": 2, "title": "心肺燃烧HIIT"},
  {"type": "训练", "courseId": 3, "title": "居家腹肌雕刻"},
  {"type": "训练", "courseId": 1, "title": "唤醒晨间瑜伽"},
  {"type": "训练", "courseId": 2, "title": "心肺燃烧HIIT"},
  {"type": "训练", "courseId": 3, "title": "居家腹肌雕刻"},
  {"type": "训练", "courseId": 1, "title": "唤醒晨间瑜伽"},
  {"type": "训练", "courseId": 2, "title": "心肺燃烧HIIT"},
  {"type": "训练", "courseId": 3, "title": "居家腹肌雕刻"},
  {"type": "训练", "courseId": 1, "title": "唤醒晨间瑜伽"},
  {"type": "训练", "courseId": 2, "title": "心肺燃烧HIIT"},
  {"type": "训练", "courseId": 3, "title": "居家腹肌雕刻"},
  {"type": "训练", "courseId": 1, "title": "唤醒晨间瑜伽"}
]'
WHERE id = 2;

-- 验证更新结果
SELECT id, title, 
       CASE 
           WHEN actions IS NULL THEN '❌ Actions为空'
           WHEN json_valid(actions) THEN '✅ Actions有效 (' || json_array_length(actions) || '个训练日)'
           ELSE '❌ Actions JSON格式错误'
       END as status
FROM training_plan 
WHERE is_public = 1;
