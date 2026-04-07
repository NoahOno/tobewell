package com.health.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.platform.entity.CommunityPost;
import com.health.platform.entity.HealthComment;
import com.health.platform.mapper.CommentMapper;
import com.health.platform.mapper.PostMapper;
import com.health.platform.service.ai.AiChatResult;
import com.health.platform.service.ai.AiService;
import com.health.platform.service.ai.CommunityReplyContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CommunityAiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @MockBean
    private AiService aiService;

    private String token;
    private Integer postId;

    @BeforeEach
    void setUp() throws Exception {
        token = login();

        CommunityPost post = new CommunityPost();
        post.setUserId(2);
        post.setTitle("AI integration test post");
        post.setContent("Need threaded comments and AI replies.");
        post.setCategory("test");
        post.setStatus("published");
        post.setLikeCount(0);
        post.setCollectionCount(0);
        post.setCommentCount(0);
        post.setViewCount(0);
        post.setCreateTime(LocalDateTime.now());
        postMapper.insert(post);
        postId = post.getId();
    }

    @Test
    void getCommentsReturnsTopLevelCommentsWithReplies() throws Exception {
        HealthComment topLevel = new HealthComment();
        topLevel.setUserId(2);
        topLevel.setTargetId(postId);
        topLevel.setTargetType("POST");
        topLevel.setParentId(0);
        topLevel.setContent("Top level question");
        commentMapper.insert(topLevel);

        HealthComment reply = new HealthComment();
        reply.setUserId(1);
        reply.setTargetId(postId);
        reply.setTargetType("POST");
        reply.setParentId(topLevel.getId());
        reply.setContent("Nested reply");
        commentMapper.insert(reply);

        mockMvc.perform(get("/community/post/{id}/comments", postId)
                .header("satoken", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].content").value("Top level question"))
            .andExpect(jsonPath("$.data[0].replies.length()").value(1))
            .andExpect(jsonPath("$.data[0].replies[0].content").value("Nested reply"));
    }

    @Test
    void commentWithAtTbwCreatesAiReplyUsingOnlyPostAndTriggerComment() throws Exception {
        given(aiService.generateCommunityReply(any())).willReturn("tbw 智能助手回答");

        mockMvc.perform(post("/community/post/{id}/comment", postId)
                .header("satoken", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "content": "@tbw 这条帖子在说什么"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/community/post/{id}/comments", postId)
                .header("satoken", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].content").value("@tbw 这条帖子在说什么"))
            .andExpect(jsonPath("$.data[0].replies.length()").value(1))
            .andExpect(jsonPath("$.data[0].replies[0].content").value("tbw 智能助手回答"))
            .andExpect(jsonPath("$.data[0].replies[0].nickname").value("tbw 智能助手"));

        ArgumentCaptor<CommunityReplyContext> contextCaptor = ArgumentCaptor.forClass(CommunityReplyContext.class);
        verify(aiService).generateCommunityReply(contextCaptor.capture());
        CommunityReplyContext captured = contextCaptor.getValue();

        Assertions.assertEquals("AI integration test post", captured.getPostTitle());
        Assertions.assertEquals("Need threaded comments and AI replies.", captured.getPostContent());
        Assertions.assertEquals("@tbw 这条帖子在说什么", captured.getTriggerComment());
        Assertions.assertTrue(captured.getRecentComments() == null || captured.getRecentComments().isEmpty());
    }

    @Test
    void aiChatReturnsStructuredDraftPayload() throws Exception {
        AiChatResult result = new AiChatResult();
        result.setReply("已为你生成训练计划草稿");
        result.setIntentType("training_plan");
        result.setDraftPayload(objectMapper.readTree("""
            {
              "title": "四周减脂计划",
              "description": "适合初学者的训练安排"
            }
            """));
        given(aiService.chat(eq(2), any())).willReturn(result);

        mockMvc.perform(post("/ai/chat")
                .header("satoken", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "message": "帮我生成一个减脂训练计划"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reply").value("已为你生成训练计划草稿"))
            .andExpect(jsonPath("$.data.intentType").value("training_plan"))
            .andExpect(jsonPath("$.data.draftPayload.title").value("四周减脂计划"));
    }

    private String login() throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "user",
                      "password": "123456"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(body);
        return jsonNode.path("data").asText();
    }
}
