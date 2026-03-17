package com.health.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.platform.entity.HealthComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<HealthComment> {
}
