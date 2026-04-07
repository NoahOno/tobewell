package com.health.platform.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class AiChatResult {
    private String reply;
    private String intentType;
    private JsonNode draftPayload;
}
