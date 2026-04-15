package com.health.platform.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.entity.AiProviderConfig;
import com.health.platform.entity.AiServiceConfig;
import com.health.platform.mapper.AiProviderConfigMapper;
import com.health.platform.mapper.AiServiceConfigMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiCompatibleAiService implements AiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final AiServiceConfigMapper aiServiceConfigMapper;
    private final AiProviderConfigMapper aiProviderConfigMapper;

    @Value("${ai.provider:openai-compatible}")
    private String provider;

    @Value("${ai.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${ai.chat-path:/chat/completions}")
    private String chatPath;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:google/gemma-3-27b-it:free}")
    private String model;

    @Value("${ai.app-name:tbw-health-platform}")
    private String appName;

    @Value("${ai.site-url:http://localhost:3000}")
    private String siteUrl;

    public OpenAiCompatibleAiService(
            ObjectMapper objectMapper,
            AiServiceConfigMapper aiServiceConfigMapper,
            AiProviderConfigMapper aiProviderConfigMapper
    ) {
        this.objectMapper = objectMapper;
        this.aiServiceConfigMapper = aiServiceConfigMapper;
        this.aiProviderConfigMapper = aiProviderConfigMapper;
    }

    @Override
    public AiChatResult chat(Integer userId, AiChatRequest request) {
        String message = request != null ? safeText(request.getMessage()) : "";
        AiServiceConfig serviceConfig = resolveServiceConfig(request);
        String intentType = detectIntent(message);
        if (serviceConfig != null && StringUtils.hasText(serviceConfig.getDefaultIntent()) && "chat".equals(intentType)) {
            intentType = serviceConfig.getDefaultIntent();
        }

        AiChatResult result = new AiChatResult();
        result.setIntentType(intentType);

        if ("training_plan".equals(intentType)) {
            result.setDraftPayload(generateStructuredPayload("plan", message, request, serviceConfig));
            result.setReply((serviceConfig != null ? serviceConfig.getName() : "AI 助手") + " 已生成训练计划草稿，确认后可加入个人库。");
            return result;
        }

        if ("course".equals(intentType)) {
            result.setDraftPayload(generateStructuredPayload("course", message, request, serviceConfig));
            result.setReply((serviceConfig != null ? serviceConfig.getName() : "AI 助手") + " 已生成训练课程草稿，确认后可加入个人库。");
            return result;
        }

        result.setReply(generateChatReply(message, request, serviceConfig));
        return result;
    }

    @Override
    public String generateCommunityReply(CommunityReplyContext context) {
        String fallback = buildCommunityFallback(context);
        AiProviderConfig providerConfig = resolveProviderConfig(null);
        if (!isConfigured(providerConfig)) {
            return fallback;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are tbw smart assistant inside a public health community thread. ");
        prompt.append("Answer in concise, helpful Chinese based on the post and comment context. ");
        prompt.append("Do not repeat or summarize the post unless the user explicitly asks what it means. ");
        prompt.append("Prefer directly giving practical advice, judgment, or next steps for the trigger comment. ");
        prompt.append("Do not claim to be a doctor. Avoid exaggerated claims. Mention medical review when safety is unclear.\n");
        prompt.append("Post title: ").append(safeText(context.getPostTitle())).append("\n");
        prompt.append("Post body: ").append(safeText(context.getPostContent())).append("\n");
        prompt.append("Trigger comment: ").append(safeText(context.getTriggerComment())).append("\n");
        if (context.getRecentComments() != null && !context.getRecentComments().isEmpty()) {
            prompt.append("Recent comments:\n");
            for (String item : context.getRecentComments()) {
                prompt.append("- ").append(safeText(item)).append("\n");
            }
        }
        prompt.append("Reply requirements:\n");
        prompt.append("1. Directly answer the trigger comment instead of restating the post.\n");
        prompt.append("2. If the post is asking for help, give 2-4 concrete suggestions.\n");
        prompt.append("3. Keep it under 120 Chinese characters unless safety explanation is necessary.\n");
        String reply = requestTextCompletion(prompt.toString(), providerConfig);
        return StringUtils.hasText(reply) ? reply.trim() : fallback;
    }

    private String generateChatReply(String message, AiChatRequest request, AiServiceConfig serviceConfig) {
        AiProviderConfig providerConfig = resolveProviderConfig(serviceConfig);
        if (!isConfigured(providerConfig)) {
            return buildChatFallback(message, request, serviceConfig);
        }

        String prompt = buildSystemPrompt(serviceConfig, request)
                + "\nUser request: " + message;
        String reply = requestTextCompletion(prompt, providerConfig);
        return StringUtils.hasText(reply) ? reply.trim() : buildChatFallback(message, request, serviceConfig);
    }

    private JsonNode generateStructuredPayload(String type, String message, AiChatRequest request, AiServiceConfig serviceConfig) {
        AiProviderConfig providerConfig = resolveProviderConfig(serviceConfig);
        if (!isConfigured(providerConfig)) {
            return buildStructuredFallback(type);
        }

        String prompt = buildSystemPrompt(serviceConfig, request) + "\n"
                + "Return JSON only, no markdown. "
                + ("plan".equals(type)
                ? "Create a training plan JSON with fields: title, description, category, duration, audience, actions. Each action contains name and sets."
                : "Create a training course JSON with fields: title, description, category, difficulty, durationMinutes, audience, actionsJson. Each action contains name and sets.")
                + " User request: " + message
                + (StringUtils.hasText(request.getStyle()) ? " Style: " + request.getStyle() : "");

        String content = requestTextCompletion(prompt, providerConfig);
        if (!StringUtils.hasText(content)) {
            return buildStructuredFallback(type);
        }

        try {
            return objectMapper.readTree(stripCodeFence(content));
        } catch (JsonProcessingException e) {
            return buildStructuredFallback(type);
        }
    }

    private String requestTextCompletion(String prompt, AiProviderConfig providerConfig) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(providerConfig.getApiKey());
            if ("openrouter".equalsIgnoreCase(providerConfig.getProviderType()) || safeText(providerConfig.getBaseUrl()).contains("openrouter.ai")) {
                headers.set("HTTP-Referer", siteUrl);
                headers.set("X-Title", appName);
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", providerConfig.getModel());
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "You are a practical health assistant. Reply in Chinese."));
            messages.add(Map.of("role", "user", "content", prompt));
            body.put("messages", messages);
            body.put("temperature", 0.7);

            ResponseEntity<String> response = restTemplate.exchange(
                    providerConfig.getBaseUrl() + chatPath,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            return contentNode.isMissingNode() ? null : contentNode.asText();
        } catch (RestClientException | JsonProcessingException e) {
            return null;
        }
    }

    private boolean isConfigured(AiProviderConfig providerConfig) {
        return providerConfig != null
                && StringUtils.hasText(providerConfig.getApiKey())
                && StringUtils.hasText(providerConfig.getBaseUrl())
                && StringUtils.hasText(providerConfig.getModel());
    }

    private String detectIntent(String message) {
        if (message.contains("训练计划") || message.contains("锻炼计划") || (message.contains("计划") && message.contains("训练"))) {
            return "training_plan";
        }
        if (message.contains("训练课程") || message.contains("课程") || message.contains("单次训练")) {
            return "course";
        }
        return "chat";
    }

    private JsonNode buildStructuredFallback(String type) {
        try {
            if ("plan".equals(type)) {
                return objectMapper.readTree("""
                    {
                      "title": "AI Plan Draft",
                      "description": "A four-week beginner training plan generated from your goal.",
                      "category": "综合训练",
                      "duration": "4周",
                      "audience": "训练新手",
                      "actions": [
                        { "name": "深蹲", "sets": "4组 x 12次" },
                        { "name": "俯卧撑", "sets": "4组 x 10次" },
                        { "name": "快走", "sets": "20分钟" }
                      ]
                    }
                    """);
            }

            return objectMapper.readTree("""
                {
                  "title": "AI Course Draft",
                  "description": "A single workout course generated from your request.",
                  "category": "体能训练",
                  "difficulty": "初级",
                  "durationMinutes": 25,
                  "audience": "普通用户",
                  "actionsJson": [
                    { "name": "热身拉伸", "sets": "5分钟" },
                    { "name": "开合跳", "sets": "3组 x 30秒" },
                    { "name": "平板支撑", "sets": "3组 x 40秒" }
                  ]
                }
                """);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build AI fallback payload", e);
        }
    }

    private String buildChatFallback(String message, AiChatRequest request, AiServiceConfig serviceConfig) {
        String style = StringUtils.hasText(serviceConfig != null ? serviceConfig.getStyleLabel() : null)
                ? serviceConfig.getStyleLabel()
                : (StringUtils.hasText(request.getStyle()) ? request.getStyle() : "清晰直接");
        String serviceName = serviceConfig != null ? serviceConfig.getName() : "AI 助手";
        return serviceName + " 当前仍在使用未配置完成的临时回复模式，先按「" + style + "」给你建议：\n"
                + "1. 先说清楚目标、频率、时长、器械和伤病限制。\n"
                + "2. 如果你想要我直接生成训练计划或训练课程，可以明确说“帮我生成”。\n"
                + "3. 当前问题是：" + safeText(message);
    }

    private String buildCommunityFallback(CommunityReplyContext context) {
        String title = safeText(context != null ? context.getPostTitle() : null);
        String body = safeText(context != null ? context.getPostContent() : null);
        String trigger = safeText(context != null ? context.getTriggerComment() : null).toLowerCase();
        String merged = (title + " " + body + " " + trigger).toLowerCase();

        if (trigger.contains("说什么") || trigger.contains("什么意思") || trigger.contains("讲什么")) {
            return "这条帖子主要是在讨论“" + (StringUtils.hasText(title) ? title : "健康问题") + "”，核心是在求可执行的改善建议。";
        }
        if (containsSleepKeywords(title, body, trigger)) {
            return "如果是压力大导致睡眠变差，先固定起床时间、下午后停咖啡因、睡前1小时别继续工作；若持续2周以上，建议线下评估。";
        }
        if (containsSorenessKeywords(merged)) {
            return "想预防运动后肌肉酸痛，重点做3件事：训练前热身5到10分钟、强度循序加量别突然冲太猛、练后补水并做轻度拉伸；若是刺痛或持续加重，要暂停并排查受伤。";
        }
        if (containsDietKeywords(merged)) {
            return "饮食类问题优先抓3点：先保证总热量别失控、蛋白质和蔬菜先吃、把最容易失守的一餐提前准备好，这样通常比单纯硬扛更有效。";
        }
        if (containsRunningKeywords(merged)) {
            return "如果是跑步相关问题，先把配速放慢、单次里程别突然加太多、跑后做小腿和臀腿放松；出现关节刺痛或落地疼，优先休息观察。";
        }
        if (trigger.contains("适合") || trigger.contains("能不能") || trigger.contains("可以吗")) {
            return "能不能做通常取决于你的基础、频率和是否有疼痛限制。一般原则是先从低强度、小剂量开始，过程中没有明显不适再逐步加量。";
        }
        return "结合帖子内容，建议先从诱因、频率和身体反应三个点排查，再优先做低风险、容易坚持的调整；如果已经明显影响日常状态，尽量尽早线下评估。";
    }

    private boolean containsSleepKeywords(String title, String body, String trigger) {
        String merged = (safeText(title) + " " + safeText(body) + " " + safeText(trigger)).toLowerCase();
        return merged.contains("睡眠") || merged.contains("失眠") || merged.contains("睡不着") || merged.contains("睡不好");
    }

    private boolean containsSorenessKeywords(String merged) {
        return merged.contains("酸痛") || merged.contains("肌肉酸") || merged.contains("doms") || merged.contains("恢复");
    }

    private boolean containsDietKeywords(String merged) {
        return merged.contains("饮食") || merged.contains("减脂") || merged.contains("零食") || merged.contains("外卖") || merged.contains("营养");
    }

    private boolean containsRunningKeywords(String merged) {
        return merged.contains("跑步") || merged.contains("配速") || merged.contains("里程") || merged.contains("慢跑");
    }

    private String stripCodeFence(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstLineEnd = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
                return trimmed.substring(firstLineEnd + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }

    private AiServiceConfig resolveServiceConfig(AiChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getServiceKey())) {
            return null;
        }
        return aiServiceConfigMapper.selectOne(new LambdaQueryWrapper<AiServiceConfig>()
                .eq(AiServiceConfig::getServiceKey, request.getServiceKey())
                .eq(AiServiceConfig::getEnabled, true)
                .last("LIMIT 1"));
    }

    private AiProviderConfig resolveProviderConfig(AiServiceConfig serviceConfig) {
        if (serviceConfig != null && serviceConfig.getApiConfigId() != null) {
            AiProviderConfig provider = aiProviderConfigMapper.selectById(serviceConfig.getApiConfigId());
            if (provider != null && Boolean.TRUE.equals(provider.getEnabled())) {
                return provider;
            }
        }
        AiProviderConfig defaultProvider = aiProviderConfigMapper.selectOne(new LambdaQueryWrapper<AiProviderConfig>()
                .eq(AiProviderConfig::getIsDefault, true)
                .eq(AiProviderConfig::getEnabled, true)
                .last("LIMIT 1"));
        if (defaultProvider != null) {
            return defaultProvider;
        }
        AiProviderConfig config = new AiProviderConfig();
        config.setProviderType(provider);
        config.setBaseUrl(baseUrl);
        config.setApiKey(apiKey);
        config.setModel(model);
        config.setEnabled(true);
        return config;
    }

    private String buildSystemPrompt(AiServiceConfig serviceConfig, AiChatRequest request) {
        StringBuilder prompt = new StringBuilder();
        if (serviceConfig != null && StringUtils.hasText(serviceConfig.getSystemPrompt())) {
            prompt.append(serviceConfig.getSystemPrompt()).append("\n");
        }
        if (request != null && StringUtils.hasText(request.getPresetPrompt())) {
            prompt.append(request.getPresetPrompt()).append("\n");
        }
        if (serviceConfig != null && StringUtils.hasText(serviceConfig.getStyleLabel())) {
            prompt.append("当前交流风格：").append(serviceConfig.getStyleLabel()).append("\n");
        }
        return prompt.toString().trim();
    }
}
