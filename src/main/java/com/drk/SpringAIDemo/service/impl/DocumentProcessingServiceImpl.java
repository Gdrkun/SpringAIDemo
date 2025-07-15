package com.drk.SpringAIDemo.service.impl;

import com.drk.SpringAIDemo.entity.KnowledgeFileEntity;
import com.drk.SpringAIDemo.service.DocumentProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 文档处理服务实现类
 * @Author drk
 * @Date 2025/7/11 10:33
 * @Version 1.0
 */
@Slf4j
@Service
public class DocumentProcessingServiceImpl implements DocumentProcessingService {

    // 支持的文件类型
    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "text/plain",
            "text/markdown",
            "text/html",
            "application/json",
            "application/xml",
            "text/xml",
            "application/pdf"
    );

    // 文本分割配置
    private static final int DEFAULT_CHUNK_SIZE = 1000;  // 每个片段的字符数
    private static final int DEFAULT_CHUNK_OVERLAP = 200; // 片段重叠字符数

    @Override
    public List<Document> processFile(KnowledgeFileEntity fileEntity) {
        if (!isSupported(fileEntity.getFileType())) {
            log.warn("不支持的文件类型: {}", fileEntity.getFileType());
            return List.of();
        }

        try {
            // 提取文本内容
            String text = extractText(fileEntity);
            
            if (text == null || text.trim().isEmpty()) {
                log.warn("文件内容为空: {}", fileEntity.getFileName());
                return List.of();
            }

            // 构建元数据
            Map<String, Object> metadata = buildMetadata(fileEntity);

            // 分割文本
            List<Document> documents = splitText(text, metadata);
            
            log.info("文件处理完成: {}, 生成文档片段数: {}", 
                    fileEntity.getFileName(), documents.size());
            
            return documents;

        } catch (Exception e) {
            log.error("处理文件失败: {}", fileEntity.getFileName(), e);
            return List.of();
        }
    }

    @Override
    public String extractText(KnowledgeFileEntity fileEntity) {
        try {
            Path filePath = Paths.get(fileEntity.getFilePath());
            
            if (!Files.exists(filePath)) {
                log.error("文件不存在: {}", fileEntity.getFilePath());
                return null;
            }

            // 根据文件类型提取文本
            String fileType = fileEntity.getFileType();
            
            if (fileType.startsWith("text/")) {
                // 纯文本文件
                return Files.readString(filePath);
            } else if ("application/json".equals(fileType)) {
                // JSON 文件
                return extractJsonText(filePath);
            } else if (fileType.contains("xml")) {
                // XML 文件
                return extractXmlText(filePath);
            }

            return null;

        } catch (IOException e) {
            log.error("读取文件失败: {}", fileEntity.getFilePath(), e);
            return null;
        }
    }

    @Override
    public List<Document> splitText(String text, Map<String, Object> metadata) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        List<Document> documents = new ArrayList<>();
        
        // 简单的文本分割策略
        List<String> chunks = splitTextIntoChunks(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
        
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            
            // 为每个片段创建独立的元数据
            Map<String, Object> chunkMetadata = new HashMap<>(metadata);
            chunkMetadata.put("chunkIndex", i);
            chunkMetadata.put("totalChunks", chunks.size());
            chunkMetadata.put("chunkSize", chunk.length());
            
            documents.add(new Document(chunk, chunkMetadata));
        }

        return documents;
    }

    @Override
    public boolean isSupported(String fileType) {
        return fileType != null && SUPPORTED_TYPES.contains(fileType.toLowerCase());
    }

    /**
     * 构建文档元数据
     */
    private Map<String, Object> buildMetadata(KnowledgeFileEntity fileEntity) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileId", fileEntity.getId());
        metadata.put("fileName", fileEntity.getFileName());
        metadata.put("fileType", fileEntity.getFileType());
        metadata.put("fileSize", fileEntity.getFileSize());
        metadata.put("fileHash", fileEntity.getFileHash());
        metadata.put("description", fileEntity.getDescription());
        metadata.put("createdAt", fileEntity.getCreatedAt());
        return metadata;
    }

    /**
     * 提取 JSON 文件的文本内容
     */
    private String extractJsonText(Path filePath) throws IOException {
        String jsonContent = Files.readString(filePath);
        // 简单处理：移除 JSON 格式字符，保留文本内容
        return jsonContent.replaceAll("[{}\\[\\]\":]", " ")
                         .replaceAll(",", "\n")
                         .replaceAll("\\s+", " ")
                         .trim();
    }

    /**
     * 提取 XML 文件的文本内容
     */
    private String extractXmlText(Path filePath) throws IOException {
        String xmlContent = Files.readString(filePath);
        // 简单处理：移除 XML 标签，保留文本内容
        return xmlContent.replaceAll("<[^>]+>", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
    }

    /**
     * 将文本分割成指定大小的片段
     */
    private List<String> splitTextIntoChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        
        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            
            // 尝试在句子边界分割
            if (end < text.length()) {
                int sentenceEnd = findSentenceEnd(text, end);
                if (sentenceEnd > start && sentenceEnd - start < chunkSize * 1.2) {
                    end = sentenceEnd;
                }
            }
            
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            
            // 计算下一个片段的起始位置（考虑重叠）
            start = Math.max(start + 1, end - overlap);
        }

        return chunks;
    }

    /**
     * 查找句子结束位置
     */
    private int findSentenceEnd(String text, int startPos) {
        Pattern sentenceEnd = Pattern.compile("[.!?。！？]\\s+");
        
        // 在起始位置前后查找句子结束标记
        int searchStart = Math.max(0, startPos - 100);
        int searchEnd = Math.min(text.length(), startPos + 100);
        
        String searchText = text.substring(searchStart, searchEnd);
        java.util.regex.Matcher matcher = sentenceEnd.matcher(searchText);
        
        int bestEnd = startPos;
        while (matcher.find()) {
            int actualPos = searchStart + matcher.end();
            if (actualPos <= startPos + 50) { // 不要偏离太远
                bestEnd = actualPos;
            }
        }
        
        return bestEnd;
    }
}
