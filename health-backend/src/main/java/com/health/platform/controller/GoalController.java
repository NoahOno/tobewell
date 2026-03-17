package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.HealthGoal;
import com.health.platform.mapper.GoalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goal")
@SaCheckLogin
public class GoalController {

    @Autowired
    private GoalMapper goalMapper;

    @PostMapping
    public Result<Void> saveGoal(@RequestBody HealthGoal goal) {
        goal.setUserId(StpUtil.getLoginIdAsInt());
        if (goal.getId() == null) {
            goalMapper.insert(goal);
        } else {
            goalMapper.updateById(goal);
        }
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<HealthGoal>> getGoals() {
        Integer userId = StpUtil.getLoginIdAsInt();
        List<HealthGoal> list = goalMapper.selectList(new LambdaQueryWrapper<HealthGoal>()
                .eq(HealthGoal::getUserId, userId)
                .orderByDesc(HealthGoal::getCreateTime));
        return Result.success(list);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteGoal(@PathVariable Integer id) {
        goalMapper.deleteById(id);
        return Result.success();
    }
}
