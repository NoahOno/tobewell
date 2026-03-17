package com.health.platform;

import com.health.platform.entity.HealthContent;
import com.health.platform.mapper.ContentMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;

@SpringBootTest
class ContentTests {

    @Autowired
    private ContentMapper contentMapper;

    @Test
    void testInsertContent() {
        HealthContent content = new HealthContent();
        content.setTitle("Unit Test Content");
        content.setContent("This is a test content.");
        content.setAuthorId(1);
        content.setCategory("Test");
        content.setCreateTime(LocalDateTime.now());
        content.setUpdateTime(LocalDateTime.now());
        
        int rows = contentMapper.insert(content);
        Assertions.assertEquals(1, rows);
        Assertions.assertNotNull(content.getId());

        HealthContent dbContent = contentMapper.selectById(content.getId());
        Assertions.assertEquals("Unit Test Content", dbContent.getTitle());
    }
}
