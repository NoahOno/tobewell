package com.health.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminAiConfigTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanManageAiServiceAndProviderConfigs() throws Exception {
        String adminToken = login("admin", "123456");
        String serviceKey = "strength_lab_" + System.currentTimeMillis();

        mockMvc.perform(post("/admin/ai-providers/save")
                .header("satoken", adminToken)
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "name": "GitHub Models",
                      "providerType": "openai-compatible",
                      "baseUrl": "https://models.inference.ai.azure.com",
                      "apiKey": "test-key",
                      "model": "gpt-4o-mini",
                      "enabled": true,
                      "isDefault": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/admin/ai-providers")
                .header("satoken", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.name=='GitHub Models')]").exists());

        mockMvc.perform(post("/admin/ai-services/save")
                .header("satoken", adminToken)
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "serviceKey": "%s",
                      "name": "力量训练助手",
                      "description": "偏向力量训练的结构化指导",
                      "tagLabel": "力量进阶",
                      "styleLabel": "结构化强度管理",
                      "systemPrompt": "你是一个强调动作安全和渐进负荷的力量训练教练。",
                      "sortOrder": 50,
                      "enabled": true,
                      "apiConfigId": 1,
                      "defaultIntent": "training_plan"
                    }
                    """.formatted(serviceKey)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/admin/ai-services")
                .header("satoken", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.serviceKey=='" + serviceKey + "')]").exists())
            .andExpect(jsonPath("$.data[0].serviceKey").value("mental_counseling"));

        MvcResult serviceListResult = mockMvc.perform(get("/admin/ai-services")
                .header("satoken", adminToken))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode services = objectMapper.readTree(serviceListResult.getResponse().getContentAsString()).path("data");
        Integer savedServiceId = null;
        for (JsonNode serviceNode : services) {
            if (serviceKey.equals(serviceNode.path("serviceKey").asText())) {
                savedServiceId = serviceNode.path("id").asInt();
                break;
            }
        }

        if (savedServiceId == null) {
            throw new IllegalStateException("Saved AI service not found in admin list");
        }

        mockMvc.perform(post("/admin/ai-services/{id}/toggle", savedServiceId)
                .header("satoken", adminToken))
            .andExpect(status().isOk());

        mockMvc.perform(get("/ai/services")
                .header("satoken", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.serviceKey=='" + serviceKey + "')]").isEmpty())
            .andExpect(jsonPath("$.data.length()").value(Matchers.greaterThanOrEqualTo(4)));
    }

    @Test
    void chatUsesConfiguredServiceKey() throws Exception {
        String token = login("user", "123456");

        mockMvc.perform(post("/ai/chat")
                .header("satoken", token)
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "message": "请帮我制定力量训练方案",
                      "serviceKey": "fitness_coach"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.intentType").value("training_plan"))
            .andExpect(jsonPath("$.data.reply").value(Matchers.containsString("健身教练")));
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "username": "%s",
                      "password": "%s"
                    }
                    """.formatted(username, password)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(body);
        return jsonNode.path("data").asText();
    }
}
