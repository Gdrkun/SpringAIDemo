package com.drk.SpringAIDemo.service;

import com.drk.SpringAIDemo.entity.KnowledgeFileEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 知识文件服务测试类
 * @Author drk
 * @Date 2025/7/11 10:33
 * @Version 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
public class KnowledgeFileServiceTest {

    @Autowired
    private KnowledgeFileService knowledgeFileService;

    @Test
    public void testUploadFile() {
        // 创建模拟文件
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        // 上传文件
        KnowledgeFileEntity result = knowledgeFileService.uploadFile(file, "测试文件");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("test.txt", result.getFileName());
        assertEquals("text/plain", result.getFileType());
        assertEquals(11L, result.getFileSize());
        assertEquals("测试文件", result.getDescription());
        assertEquals(1, result.getStatus());
    }

    @Test
    public void testFindByFileNameLike() {
        // 先上传一个文件
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "search_test.txt",
                "text/plain",
                "Search Test Content".getBytes()
        );
        knowledgeFileService.uploadFile(file, "搜索测试文件");

        // 搜索文件
        List<KnowledgeFileEntity> results = knowledgeFileService.findByFileNameLike("search");
        
        // 验证结果
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(f -> f.getFileName().contains("search")));
    }

    @Test
    public void testDeleteFile() {
        // 先上传一个文件
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "delete_test.txt",
                "text/plain",
                "Delete Test Content".getBytes()
        );
        KnowledgeFileEntity uploaded = knowledgeFileService.uploadFile(file, "删除测试文件");

        // 删除文件
        boolean deleted = knowledgeFileService.deleteFileById(uploaded.getId());
        
        // 验证结果
        assertTrue(deleted);
        assertNull(knowledgeFileService.getById(uploaded.getId()));
    }
}
