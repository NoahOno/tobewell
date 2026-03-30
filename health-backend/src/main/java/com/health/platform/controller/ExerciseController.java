package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.Exercise;
import com.health.platform.mapper.ExerciseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Exercise Repository")
@RestController
@RequestMapping("/exercise")
@SaCheckLogin
public class ExerciseController {

    @Autowired
    private ExerciseMapper exerciseMapper;

    @Operation(summary = "Get all exercises")
    @GetMapping("/list")
    public Result<List<Exercise>> list(@RequestParam(required = false) String keyword, 
                                     @RequestParam(required = false) String muscle,
                                     @RequestParam(required = false) String type,
                                     @RequestParam(required = false) String equipment,
                                     @RequestParam(required = false) String difficulty) {
        LambdaQueryWrapper<Exercise> wrapper = new LambdaQueryWrapper<Exercise>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Exercise::getName, keyword);
        }
        if (muscle != null && !muscle.equals("全部")) {
            wrapper.eq(Exercise::getMuscle, muscle);
        }
        if (type != null && !type.equals("全部")) {
            wrapper.eq(Exercise::getType, type);
        }
        if (equipment != null && !equipment.equals("全部")) {
            wrapper.eq(Exercise::getEquipment, equipment);
        }
        if (difficulty != null && !difficulty.equals("全部")) {
            wrapper.eq(Exercise::getDifficulty, difficulty);
        }
        
        wrapper.orderByDesc(Exercise::getCreateTime);
        return Result.success(exerciseMapper.selectList(wrapper));
    }

    @Operation(summary = "Get exercise detail")
    @GetMapping("/{id}")
    public Result<Exercise> detail(@PathVariable Integer id) {
        return Result.success(exerciseMapper.selectById(id));
    }

    @Operation(summary = "Admin: Create/Update exercise")
    @PostMapping("/save")
    @SaCheckRole("ADMIN")
    public Result<Void> save(@RequestBody Exercise exercise) {
        if (exercise.getId() == null) {
            if (exercise.getIsPublic() == null) exercise.setIsPublic(true); exerciseMapper.insert(exercise);
        } else {
            exerciseMapper.updateById(exercise);
        }
        return Result.success();
    }

    @Operation(summary = "Admin: Offline exercise")
    @DeleteMapping("/{id}")
    @SaCheckRole("ADMIN")
    public Result<Void> delete(@PathVariable Integer id) {
        Exercise e = exerciseMapper.selectById(id);
        if (e != null) { e.setIsPublic(false); exerciseMapper.updateById(e); }
        return Result.success();
    }
}
