package com.drk.SpringAIDemo.service;

import com.drk.SpringAIDemo.entity.KnowledgeFileEntity;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 文档处理服务接口
 * @Author drk
 * @Date 2025/7/11 10:33
 * @Version 1.0
 */
public interface DocumentProcessingService {

    /**
     * 处理文件并转换为文档对象
     * @param fileEntity 文件实体
     * @return 文档列表（可能被分割成多个片段）
     */
    List<Document> processFile(KnowledgeFileEntity fileEntity);

    /**
     * 从文件中提取文本内容
     * @param fileEntity 文件实体
     * @return 提取的文本内容
     */
    String extractText(KnowledgeFileEntity fileEntity);

    /**
     * 将文本分割成适合向量化的片段
     * @param text 原始文本
     * @param metadata 元数据
     * @return 文档片段列表
     */
    List<Document> splitText(String text, java.util.Map<String, Object> metadata);

    /**
     * 检查文件类型是否支持处理
     * @param fileType 文件类型
     * @return 是否支持
     */
    boolean isSupported(String fileType);
}
