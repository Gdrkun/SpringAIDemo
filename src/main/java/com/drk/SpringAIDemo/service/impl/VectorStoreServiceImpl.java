package com.drk.SpringAIDemo.service.impl;

import com.drk.SpringAIDemo.entity.KnowledgeFileEntity;
import com.drk.SpringAIDemo.service.DocumentProcessingService;
import com.drk.SpringAIDemo.service.VectorStoreService;
import com.drk.SpringAIDemo.utils.TextCleanerUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.text.BreakIterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 向量存储服务实现类
 * @Author drk
 * @Date 2025/7/11 10:33
 * @Version 1.0
 */
@Slf4j
@Service
public class VectorStoreServiceImpl implements VectorStoreService {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private DocumentProcessingService documentProcessingService;
    @Autowired
    private ApplicationContext context;
    @Qualifier("ollamaEmbeddingModel")
    @Autowired
    private EmbeddingModel embeddingModel;

    @PostConstruct
    public void checkBeans() {
        // 查看是否存在 EmbeddingModel Bean
        EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
        System.out.println("EmbeddingModel 类型: " + embeddingModel.getClass().getName());
    }

    @Override
    public boolean vectorizeAndStore(KnowledgeFileEntity fileEntity) {
        try {
            log.info("开始向量化文件: {}", fileEntity.getFileName());

            // 处理文件，获取文档片段
            //List<Document> documents = documentProcessingService.processFile(fileEntity);

            File file = new File(fileEntity.getFilePath());
            FileSystemResource resource = new FileSystemResource(file);
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
            List<Document> tempDocuments =  tikaDocumentReader.read();

            List<Document> collect = tempDocuments.stream().map(
                            document -> new Document(
                                        TextCleanerUtil.clean(document.getText(),fileEntity.getFileSuffix().substring(1)),
                                        document.getMetadata())

                            ).toList();

            //标记文本分割器，将文本切分成更小的块
            TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
            List<Document> documents = tokenTextSplitter.apply(collect);

            if (CollectionUtils.isEmpty(documents)) {
                log.warn("文件处理后没有生成文档片段: {}", fileEntity.getFileName());
                return false;
            }
            AtomicInteger index = new AtomicInteger(0);
            //存储时给对应的向量数据添加文件ID，方便后续删除和查找
            List<Document> documentWithIds = documents.stream().peek(document -> {
                document.getMetadata().put("fileId", fileEntity.getId().toString());
                document.getMetadata().put("fileName", fileEntity.getFileName());
                document.getMetadata().put("chunkIndex", String.valueOf(index.getAndIncrement()));
            }).toList();

            // 存储到向量数据库
            boolean success = storeDocuments(documentWithIds);

            if (success) {
                log.info("文件向量化成功: {}, 存储文档片段数: {}",
                        fileEntity.getFileName(), documents.size());
            } else {
                log.error("文件向量化失败: {}", fileEntity.getFileName());
            }

            return success;

        } catch (Exception e) {
            log.error("向量化文件时发生异常: {}", fileEntity.getFileName(), e);
            return false;
        }
    }

    @Override
    public boolean storeDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return false;
        }

        try {
            // 批量添加文档到向量数据库
            vectorStore.add(documents);
            log.info("成功存储 {} 个文档片段到向量数据库", documents.size());
            return true;

        } catch (Exception e) {
            log.error("存储文档到向量数据库失败", e);
            return false;
        }
    }

    @Override
    public List<Document> searchSimilarDocuments(String query, int topK, float threshold) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(threshold)
                    .build();

            List<Document> results = vectorStore.similaritySearch(searchRequest);
            
            log.debug("向量搜索完成，查询: {}, 返回结果数: {}", query, results.size());
            
            return results;

        } catch (Exception e) {
            log.error("向量搜索失败，查询: {}", query, e);
            return List.of();
        }
    }

    @Override
    public boolean deleteByFileId(Long fileId) {
        try {
            // 搜索包含指定文件ID的所有文档
            List<Document> documentsToDelete = findDocumentsByFileId(fileId);
            
            if (CollectionUtils.isEmpty(documentsToDelete)) {
                log.info("未找到文件ID为 {} 的向量数据", fileId);
                return true; // 没有数据也算成功
            }

            // 删除文档（注意：Qdrant 的删除需要文档ID）
            return deleteDocuments(documentsToDelete);

        } catch (Exception e) {
            log.error("根据文件ID删除向量数据失败: {}", fileId, e);
            return false;
        }
    }

    @Override
    public boolean deleteByFileHash(String fileHash) {
        try {
            // 搜索包含指定文件哈希的所有文档
            List<Document> documentsToDelete = findDocumentsByFileHash(fileHash);
            
            if (CollectionUtils.isEmpty(documentsToDelete)) {
                log.info("未找到文件哈希为 {} 的向量数据", fileHash);
                return true; // 没有数据也算成功
            }

            // 删除文档
            return deleteDocuments(documentsToDelete);

        } catch (Exception e) {
            log.error("根据文件哈希删除向量数据失败: {}", fileHash, e);
            return false;
        }
    }

    @Override
    public long getDocumentCount() {
        try {
            // 注意：这是一个简化的实现，实际的文档计数可能需要根据具体的向量数据库实现
            // 对于 Qdrant，可能需要调用特定的 API
            log.info("获取向量数据库文档总数");
            return 0; // 暂时返回0，需要根据实际的向量数据库API实现
        } catch (Exception e) {
            log.error("获取文档总数失败", e);
            return 0;
        }
    }

    /**
     * 根据文件ID查找文档
     */
    private List<Document> findDocumentsByFileId(Long fileId) {
        // 这里使用一个通用的搜索方法
        // 实际实现可能需要根据向量数据库的特性进行调整
        try {
            // 使用元数据过滤搜索
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("*") // 通配符查询
                    .topK(1000) // 设置一个较大的数量
                    .filterExpression("fileId == '" + fileId.toString() + "'") // 元数据过滤
                    .build();

            return vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.warn("根据文件ID搜索文档失败，使用备用方法: {}", fileId);
            // 备用方法：搜索所有文档并在内存中过滤
            return searchAllDocuments().stream()
                    .filter(doc -> fileId.equals(doc.getMetadata().get("fileId")))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 根据文件哈希查找文档
     */
    private List<Document> findDocumentsByFileHash(String fileHash) {
        try {
            // 使用元数据过滤搜索
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("*") // 通配符查询
                    .topK(1000) // 设置一个较大的数量
                    .filterExpression("fileHash == '" + fileHash + "'") // 元数据过滤
                    .build();

            return vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.warn("根据文件哈希搜索文档失败，使用备用方法: {}", fileHash);
            // 备用方法：搜索所有文档并在内存中过滤
            return searchAllDocuments().stream()
                    .filter(doc -> fileHash.equals(doc.getMetadata().get("fileHash")))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 搜索所有文档（备用方法）
     */
    private List<Document> searchAllDocuments() {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("*")
                    .topK(10000) // 设置一个很大的数量
                    .similarityThreshold(0.0f) // 最低阈值
                    .build();

            return vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.error("搜索所有文档失败", e);
            return List.of();
        }
    }

    /**
     * 删除文档列表
     */
    private boolean deleteDocuments(List<Document> documents) {
        try {
            List<String> documentIds = documents.stream()
                    .map(Document::getId)
                    .toList();

            if (!documentIds.isEmpty()) {
                // 调用向量数据库的删除方法
                vectorStore.delete(documentIds);
                log.info("删除了 {} 个文档", documentIds.size());
            }

            return true;

        } catch (Exception e) {
            log.error("删除文档失败", e);
            return false;
        }
    }
}
