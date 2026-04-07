package com.health.platform.service.ai;

import lombok.Data;

@Data
public class AiChatRequest {
    private String message;
    private String serviceKey;
    private String presetKey;
    private String presetPrompt;
    private String style;
}
