package com.drk.SpringAIDemo.service;

import com.drk.SpringAIDemo.entity.KnowledgeFileEntity;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 向量存储服务接口
 * @Author drk
 * @Date 2025/7/11 10:33
 * @Version 1.0
 */
public interface VectorStoreService {

    /**
     * 将文件向量化并存储到向量数据库
     * @param fileEntity 文件实体
     * @return 是否成功
     */
    boolean vectorizeAndStore(KnowledgeFileEntity fileEntity);

    /**
     * 将文档列表存储到向量数据库
     * @param documents 文档列表
     * @return 是否成功
     */
    boolean storeDocuments(List<Document> documents);

    /**
     * 根据查询搜索相关文档
     * @param query 查询文本
     * @param topK 返回结果数量
     * @param threshold 相似度阈值
     * @return 相关文档列表
     */
    List<Document> searchSimilarDocuments(String query, int topK, float threshold);

    /**
     * 根据文件ID删除向量数据
     * @param fileId 文件ID
     * @return 是否成功
     */
    boolean deleteByFileId(Long fileId);

    /**
     * 根据文件哈希删除向量数据
     * @param fileHash 文件哈希
     * @return 是否成功
     */
    boolean deleteByFileHash(String fileHash);

    /**
     * 获取向量数据库中的文档总数
     * @return 文档总数
     */
    long getDocumentCount();
}
