package com.health.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.platform.entity.WorkoutRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkoutMapper extends BaseMapper<WorkoutRecord> {
}
