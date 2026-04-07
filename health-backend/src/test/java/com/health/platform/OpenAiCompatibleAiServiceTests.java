package com.health.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.platform.mapper.AiProviderConfigMapper;
import com.health.platform.mapper.AiServiceConfigMapper;
import com.health.platform.service.ai.CommunityReplyContext;
import com.health.platform.service.ai.OpenAiCompatibleAiService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenAiCompatibleAiServiceTests {

    @Test
    void communityFallbackUsesPostAndCommentContent() {
        AiServiceConfigMapper serviceConfigMapper = mock(AiServiceConfigMapper.class);
        AiProviderConfigMapper providerConfigMapper = mock(AiProviderConfigMapper.class);
        when(providerConfigMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        OpenAiCompatibleAiService service = new OpenAiCompatibleAiService(
                new ObjectMapper(),
                serviceConfigMapper,
                providerConfigMapper
        );
        ReflectionTestUtils.setField(service, "provider", "openai-compatible");
        ReflectionTestUtils.setField(service, "baseUrl", "https://example.com");
        ReflectionTestUtils.setField(service, "chatPath", "/chat/completions");
        ReflectionTestUtils.setField(service, "apiKey", "");
        ReflectionTestUtils.setField(service, "model", "test-model");
        ReflectionTestUtils.setField(service, "appName", "tbw");
        ReflectionTestUtils.setField(service, "siteUrl", "http://localhost");

        CommunityReplyContext context = new CommunityReplyContext();
        context.setPostTitle("减脂期间想吃零食怎么办");
        context.setPostContent("帖子提到用水果替代高糖零食，也建议适量坚果和低卡爆米花。");
        context.setTriggerComment("@tbw 这条帖子在说什么");

        String reply = service.generateCommunityReply(context);

        assertFalse(reply.contains("先补充你的目标"));
        assertFalse(reply.isBlank());
    }
}
