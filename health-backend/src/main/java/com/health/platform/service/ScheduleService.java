package com.health.platform.service;

import com.health.platform.entity.TrainingPlan;
import java.util.List;

public interface ScheduleService {
    void generateSchedule(TrainingPlan userPlan, List<String> weeklyDays);
}
