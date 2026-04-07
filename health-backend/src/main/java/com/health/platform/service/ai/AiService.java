package com.health.platform.service.ai;

public interface AiService {
    AiChatResult chat(Integer userId, AiChatRequest request);

    String generateCommunityReply(CommunityReplyContext context);
}
