package com.health.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.platform.entity.CommunityPost;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<CommunityPost> {
}
