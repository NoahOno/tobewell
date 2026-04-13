package com.health.platform.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.entity.Course;
import com.health.platform.entity.TrainingPlan;
import com.health.platform.mapper.CourseMapper;
import com.health.platform.mapper.TrainingMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SeedDataRepairRunner implements ApplicationRunner {

    private final CourseMapper courseMapper;
    private final TrainingMapper trainingMapper;

    public SeedDataRepairRunner(CourseMapper courseMapper, TrainingMapper trainingMapper) {
        this.courseMapper = courseMapper;
        this.trainingMapper = trainingMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        repairSeedCourses();
        repairSeedPlans();
        repairDerivedCourses();
        repairDerivedPlans();
    }

    private void repairSeedCourses() {
        Map<Integer, String> courseActions = Map.of(
                1, "[{\"name\":\"平板支撑 (Plank)\",\"sets\":\"2组，每组40秒\",\"rest\":\"20秒\"},{\"name\":\"深蹲 (Squat)\",\"sets\":\"2组，每组15次\",\"rest\":\"25秒\"},{\"name\":\"开合跳 (Jumping Jacks)\",\"sets\":\"2组，每组30秒\",\"rest\":\"20秒\"}]",
                2, "[{\"name\":\"开合跳 (Jumping Jacks)\",\"sets\":\"3组，每组45秒\",\"rest\":\"20秒\"},{\"name\":\"波比跳 (Burpees)\",\"sets\":\"3组，每组12次\",\"rest\":\"30秒\"},{\"name\":\"平板支撑 (Plank)\",\"sets\":\"3组，每组45秒\",\"rest\":\"20秒\"},{\"name\":\"深蹲 (Squat)\",\"sets\":\"3组，每组20次\",\"rest\":\"30秒\"}]",
                3, "[{\"name\":\"平板支撑 (Plank)\",\"sets\":\"3组，每组50秒\",\"rest\":\"20秒\"},{\"name\":\"开合跳 (Jumping Jacks)\",\"sets\":\"3组，每组30秒\",\"rest\":\"15秒\"},{\"name\":\"波比跳 (Burpees)\",\"sets\":\"2组，每组10次\",\"rest\":\"30秒\"}]",
                4, "[{\"name\":\"平板支撑 (Plank)\",\"sets\":\"3组，每组45秒\",\"rest\":\"20秒\"},{\"name\":\"深蹲 (Squat)\",\"sets\":\"3组，每组15次\",\"rest\":\"20秒\"},{\"name\":\"开合跳 (Jumping Jacks)\",\"sets\":\"3组，每组30秒\",\"rest\":\"20秒\"}]"
        );
        Map<Integer, String> courseAudience = Map.of(
                1, "久坐上班族 / 晨练新手",
                2, "减脂进阶 / 心肺提升",
                3, "居家训练 / 核心塑形",
                4, "家庭场景 / 核心稳定"
        );

        for (Integer id : courseActions.keySet()) {
            Course course = courseMapper.selectById(id);
            if (course == null) continue;

            boolean changed = false;
            if (isBlank(course.getActionsJson())) {
                course.setActionsJson(courseActions.get(id));
                changed = true;
            }
            if (isBlank(course.getAudience())) {
                course.setAudience(courseAudience.get(id));
                changed = true;
            }
            if (changed) {
                courseMapper.updateById(course);
            }
        }
    }

    private void repairSeedPlans() {
        Map<Integer, String> planActions = Map.of(
                1, "[{\"type\":\"训练\",\"title\":\"心肺燃烧HIIT\",\"courseId\":2,\"courseTitle\":\"心肺燃烧HIIT\"},{\"type\":\"休息\",\"title\":\"主动恢复\"},{\"type\":\"训练\",\"title\":\"居家腹肌雕刻\",\"courseId\":3,\"courseTitle\":\"居家腹肌雕刻\"},{\"type\":\"休息\",\"title\":\"拉伸恢复\"},{\"type\":\"训练\",\"title\":\"唤醒晨间瑜伽\",\"courseId\":1,\"courseTitle\":\"唤醒晨间瑜伽\"},{\"type\":\"休息\",\"title\":\"步行恢复\"},{\"type\":\"训练\",\"title\":\"心肺燃烧HIIT\",\"courseId\":2,\"courseTitle\":\"心肺燃烧HIIT\"}]",
                2, "[{\"type\":\"训练\",\"title\":\"家庭核心强化\",\"courseId\":4,\"courseTitle\":\"家庭核心强化\"},{\"type\":\"休息\",\"title\":\"轻松恢复\"},{\"type\":\"训练\",\"title\":\"居家腹肌雕刻\",\"courseId\":3,\"courseTitle\":\"居家腹肌雕刻\"},{\"type\":\"休息\",\"title\":\"拉伸恢复\"},{\"type\":\"训练\",\"title\":\"家庭核心强化\",\"courseId\":4,\"courseTitle\":\"家庭核心强化\"},{\"type\":\"休息\",\"title\":\"休息日\"},{\"type\":\"训练\",\"title\":\"唤醒晨间瑜伽\",\"courseId\":1,\"courseTitle\":\"唤醒晨间瑜伽\"}]",
                3, "[{\"type\":\"训练\",\"title\":\"心肺燃烧HIIT\",\"courseId\":2,\"courseTitle\":\"心肺燃烧HIIT\"},{\"type\":\"训练\",\"title\":\"居家腹肌雕刻\",\"courseId\":3,\"courseTitle\":\"居家腹肌雕刻\"},{\"type\":\"训练\",\"title\":\"唤醒晨间瑜伽\",\"courseId\":1,\"courseTitle\":\"唤醒晨间瑜伽\"}]"
        );
        Map<Integer, String> planCover = Map.of(
                1, "http://localhost:8080/uploads/plan_4week_fat_loss.png",
                2, "http://localhost:8080/uploads/plan_beginner_muscle_build.png",
                3, "http://localhost:8080/uploads/plan_4week_fat_loss.png"
        );
        Map<Integer, String> planAudience = Map.of(
                1, "减脂新手 / 居家训练",
                2, "增肌入门 / 力量基础",
                3, "周末减脂 / 零基础"
        );

        for (Integer id : planActions.keySet()) {
            TrainingPlan plan = trainingMapper.selectById(id);
            if (plan == null) continue;

            boolean changed = false;
            if (isBlank(plan.getActions())) {
                plan.setActions(planActions.get(id));
                changed = true;
            }
            if (isBlank(plan.getCoverImage())) {
                plan.setCoverImage(planCover.get(id));
                changed = true;
            }
            if (isBlank(plan.getAudience())) {
                plan.setAudience(planAudience.get(id));
                changed = true;
            }
            if (changed) {
                trainingMapper.updateById(plan);
            }
        }
    }

    private void repairDerivedCourses() {
        List<Course> privateCourses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getIsPublic, false));

        for (Course course : privateCourses) {
            if (!isBlank(course.getActionsJson()) && !isBlank(course.getCoverImage()) && !isBlank(course.getAudience())) {
                continue;
            }

            Course source = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                    .eq(Course::getTitle, course.getTitle())
                    .eq(Course::getIsPublic, true)
                    .last("limit 1"));
            if (source == null) continue;

            boolean changed = false;
            if (isBlank(course.getActionsJson()) && !isBlank(source.getActionsJson())) {
                course.setActionsJson(source.getActionsJson());
                changed = true;
            }
            if (isBlank(course.getCoverImage()) && !isBlank(source.getCoverImage())) {
                course.setCoverImage(source.getCoverImage());
                changed = true;
            }
            if (isBlank(course.getAudience()) && !isBlank(source.getAudience())) {
                course.setAudience(source.getAudience());
                changed = true;
            }
            if (changed) {
                courseMapper.updateById(course);
            }
        }
    }

    private void repairDerivedPlans() {
        List<TrainingPlan> userPlans = trainingMapper.selectList(new LambdaQueryWrapper<TrainingPlan>()
                .isNotNull(TrainingPlan::getUserId));

        for (TrainingPlan plan : userPlans) {
            if (!isBlank(plan.getActions()) && !isBlank(plan.getCoverImage()) && !isBlank(plan.getAudience())) {
                continue;
            }

            TrainingPlan source = null;
            if (plan.getSourceId() != null) {
                source = trainingMapper.selectById(plan.getSourceId());
            }
            if (source == null) {
                source = trainingMapper.selectOne(new LambdaQueryWrapper<TrainingPlan>()
                        .eq(TrainingPlan::getTitle, plan.getTitle())
                        .eq(TrainingPlan::getIsPublic, true)
                        .last("limit 1"));
            }
            if (source == null) continue;

            boolean changed = false;
            if (isBlank(plan.getActions()) && !isBlank(source.getActions())) {
                plan.setActions(source.getActions());
                changed = true;
            }
            if (isBlank(plan.getCoverImage()) && !isBlank(source.getCoverImage())) {
                plan.setCoverImage(source.getCoverImage());
                changed = true;
            }
            if (isBlank(plan.getAudience()) && !isBlank(source.getAudience())) {
                plan.setAudience(source.getAudience());
                changed = true;
            }
            if (changed) {
                trainingMapper.updateById(plan);
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
