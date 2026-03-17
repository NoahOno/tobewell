package com.health.platform.config;

import cn.dev33.satoken.exception.NotLoginException;
import com.health.platform.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public Result<String> handleNotLoginException(NotLoginException e) {
        log.warn("Not Login Exception: {}", e.getMessage());
        return Result.error(401, "登录已失效，请重新登录");
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);
        return Result.error("System Error: " + e.getMessage());
    }
}
