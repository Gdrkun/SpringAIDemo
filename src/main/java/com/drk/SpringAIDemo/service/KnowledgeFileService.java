package com.drk.SpringAIDemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drk.SpringAIDemo.entity.KnowledgeFileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识文件服务接口
 * @Author drk
 * @Date 2025/7/11 10:33
 * @Version 1.0
 */
public interface KnowledgeFileService extends IService<KnowledgeFileEntity> {

    /**
     * 上传文件
     * @param file 上传的文件
     * @param description 文件描述
     * @return 文件实体
     */
    KnowledgeFileEntity uploadFile(MultipartFile file, String description);

    /**
     * 根据文件哈希查找文件
     * @param fileHash 文件哈希值
     * @return 文件实体
     */
    KnowledgeFileEntity findByFileHash(String fileHash);

    /**
     * 根据文件名模糊查询
     * @param fileName 文件名
     * @return 文件列表
     */
    List<KnowledgeFileEntity> findByFileNameLike(String fileName);

    /**
     * 根据文件类型查询
     * @param fileType 文件类型
     * @return 文件列表
     */
    List<KnowledgeFileEntity> findByFileType(String fileType);

    /**
     * 删除文件（包括物理文件）
     * @param id 文件ID
     * @return 是否删除成功
     */
    boolean deleteFileById(Long id);

    /**
     * 上传文件并向量化
     * @param file 上传的文件
     * @param description 文件描述
     * @param enableVectorization 是否启用向量化
     * @return 文件实体
     */
    KnowledgeFileEntity uploadFileWithVectorization(MultipartFile file, String description, boolean enableVectorization);

    /**
     * 对已存在的文件进行向量化
     * @param id 文件ID
     * @return 是否成功
     */
    boolean vectorizeExistingFile(Long id);

    /**
     * 搜索相关文档
     * @param query 查询文本
     * @param topK 返回结果数量
     * @param threshold 相似度阈值
     * @return 相关文档列表
     */
    List<org.springframework.ai.document.Document> searchSimilarDocuments(String query, int topK, float threshold);
}
