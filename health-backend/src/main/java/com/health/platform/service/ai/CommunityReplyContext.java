package com.health.platform.service.ai;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommunityReplyContext {
    private String postTitle;
    private String postContent;
    private String triggerComment;
    private List<String> recentComments = new ArrayList<>();
}
