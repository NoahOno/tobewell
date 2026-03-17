package com.health.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.platform.entity.HealthGoal;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoalMapper extends BaseMapper<HealthGoal> {
}
