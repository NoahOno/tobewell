package com.health.platform.config;

import cn.dev33.satoken.stp.StpInterface;
import com.health.platform.entity.SysUser;
import com.health.platform.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token Custom Permission Validation
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // Return permissions here if needed
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // Get user role from database
        SysUser user = userMapper.selectById(loginId.toString());
        List<String> list = new ArrayList<>();
        if (user != null) {
            list.add(user.getRole());
        }
        return list;
    }
}
