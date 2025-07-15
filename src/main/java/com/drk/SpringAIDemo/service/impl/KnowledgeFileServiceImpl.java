package com.drk.SpringAIDemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drk.SpringAIDemo.entity.KnowledgeFileEntity;
import com.drk.SpringAIDemo.mapper.KnowledgeFileMapper;
import com.drk.SpringAIDemo.service.KnowledgeFileService;
import com.drk.SpringAIDemo.service.VectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 知识文件服务实现类
 * @Author drk
 * @Date 2025/7/11 10:33
 * @Version 1.0
 */
@Slf4j
@Service
public class KnowledgeFileServiceImpl extends ServiceImpl<KnowledgeFileMapper, KnowledgeFileEntity> implements KnowledgeFileService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Override
    public KnowledgeFileEntity uploadFile(MultipartFile file, String description) {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        try {
            // 计算文件哈希值
            String fileHash = DigestUtils.md5DigestAsHex(file.getBytes());
            
            // 检查文件是否已存在
            KnowledgeFileEntity existingFile = findByFileHash(fileHash);
            if (existingFile != null) {
                log.info("文件已存在，返回已有文件信息: {}", existingFile.getFileName());
                return existingFile;
            }

            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileSuffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileSuffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileSuffix;
            
            // 保存文件
            Path filePath = uploadDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            // 创建文件实体
            KnowledgeFileEntity fileEntity = new KnowledgeFileEntity();
            fileEntity.setFileName(originalFilename);
            fileEntity.setFilePath(filePath.toString());
            fileEntity.setFileType(file.getContentType());
            fileEntity.setFileSize(file.getSize());
            fileEntity.setFileHash(fileHash);
            fileEntity.setFileSuffix(fileSuffix);
            fileEntity.setDescription(description);
            fileEntity.setStatus(1); // 上传成功
            fileEntity.setCreatedAt(LocalDateTime.now());
            fileEntity.setUpdatedAt(LocalDateTime.now());

            // 保存到数据库
            save(fileEntity);
            
            log.info("文件上传成功: {}", originalFilename);
            return fileEntity;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public KnowledgeFileEntity findByFileHash(String fileHash) {
        QueryWrapper<KnowledgeFileEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("file_hash", fileHash);
        return getOne(queryWrapper);
    }

    @Override
    public List<KnowledgeFileEntity> findByFileNameLike(String fileName) {
        QueryWrapper<KnowledgeFileEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("file_name", fileName);
        queryWrapper.orderByDesc("created_at");
        return list(queryWrapper);
    }

    @Override
    public List<KnowledgeFileEntity> findByFileType(String fileType) {
        QueryWrapper<KnowledgeFileEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("file_type", fileType);
        queryWrapper.orderByDesc("created_at");
        return list(queryWrapper);
    }


    @Override
    public KnowledgeFileEntity uploadFileWithVectorization(MultipartFile file, String description, boolean enableVectorization) {
        // 先上传文件
        KnowledgeFileEntity fileEntity = uploadFile(file, description);

        if (fileEntity != null && enableVectorization) {
            // 异步进行向量化处理
            try {
                // 更新向量化状态为进行中
                updateVectorizationStatus(fileEntity.getId(), 1, null);

                boolean vectorized = vectorStoreService.vectorizeAndStore(fileEntity);
                if (vectorized) {
                    log.info("文件向量化成功: {}", fileEntity.getFileName());
                    // 更新向量化状态为成功
                    updateVectorizationStatus(fileEntity.getId(), 2, LocalDateTime.now());
                } else {
                    log.warn("文件向量化失败: {}", fileEntity.getFileName());
                    // 更新向量化状态为失败
                    updateVectorizationStatus(fileEntity.getId(), 3, LocalDateTime.now());
                }
            } catch (Exception e) {
                log.error("文件向量化过程中发生异常: {}", fileEntity.getFileName(), e);
                // 更新向量化状态为失败
                updateVectorizationStatus(fileEntity.getId(), 3, LocalDateTime.now());
            }
        }

        return fileEntity;
    }

    @Override
    public boolean vectorizeExistingFile(Long id) {
        KnowledgeFileEntity fileEntity = getById(id);
        if (fileEntity == null) {
            log.error("文件不存在: {}", id);
            return false;
        }

        try {
            // 更新向量化状态为进行中
            updateVectorizationStatus(id, 1, null);

            boolean success = vectorStoreService.vectorizeAndStore(fileEntity);
            if (success) {
                log.info("已存在文件向量化成功: {}", fileEntity.getFileName());
                // 更新向量化状态为成功
                updateVectorizationStatus(id, 2, LocalDateTime.now());
            } else {
                log.error("已存在文件向量化失败: {}", fileEntity.getFileName());
                // 更新向量化状态为失败
                updateVectorizationStatus(id, 3, LocalDateTime.now());
            }
            return success;
        } catch (Exception e) {
            log.error("向量化已存在文件时发生异常: {}", fileEntity.getFileName(), e);
            // 更新向量化状态为失败
            updateVectorizationStatus(id, 3, LocalDateTime.now());
            return false;
        }
    }

    @Override
    public List<org.springframework.ai.document.Document> searchSimilarDocuments(String query, int topK, float threshold) {
        try {
            return vectorStoreService.searchSimilarDocuments(query, topK, threshold);
        } catch (Exception e) {
            log.error("搜索相似文档失败，查询: {}", query, e);
            return List.of();
        }
    }

    @Override
    public boolean deleteFileById(Long id) {
        KnowledgeFileEntity fileEntity = getById(id);
        if (fileEntity == null) {
            return false;
        }

        try {
            // 先删除向量数据
            vectorStoreService.deleteByFileId(id);

            // 删除物理文件
            Path filePath = Paths.get(fileEntity.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("物理文件删除成功: {}", fileEntity.getFilePath());
            }

            // 删除数据库记录
            boolean removed = removeById(id);
            if (removed) {
                log.info("文件记录删除成功: {}", fileEntity.getFileName());
            }

            return removed;

        } catch (IOException e) {
            log.error("删除物理文件失败: {}", fileEntity.getFilePath(), e);
            // 即使物理文件删除失败，也删除数据库记录和向量数据
            vectorStoreService.deleteByFileId(id);
            return removeById(id);
        }
    }

    /**
     * 更新文件的向量化状态
     */
    private void updateVectorizationStatus(Long fileId, Integer status, LocalDateTime vectorizationTime) {
        try {
            KnowledgeFileEntity fileEntity = getById(fileId);
            if (fileEntity != null) {
                fileEntity.setVectorizationStatus(status);
                if (vectorizationTime != null) {
                    fileEntity.setVectorizationTime(vectorizationTime);
                }
                updateById(fileEntity);
                log.debug("更新文件向量化状态成功，文件ID: {}, 状态: {}", fileId, status);
            }
        } catch (Exception e) {
            log.error("更新向量化状态失败，文件ID: {}", fileId, e);
        }
    }
}
