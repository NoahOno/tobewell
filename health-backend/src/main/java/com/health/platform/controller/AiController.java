package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.health.platform.common.Result;
import com.health.platform.service.ai.AiChatRequest;
import com.health.platform.service.ai.AiChatResult;
import com.health.platform.service.ai.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI")
@RestController
@RequestMapping("/ai")
@SaCheckLogin
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "Chat with AI and generate plan/course drafts")
    @PostMapping("/chat")
    public Result<AiChatResult> chat(@RequestBody AiChatRequest request) {
        return Result.success(aiService.chat(StpUtil.getLoginIdAsInt(), request));
    }
}
