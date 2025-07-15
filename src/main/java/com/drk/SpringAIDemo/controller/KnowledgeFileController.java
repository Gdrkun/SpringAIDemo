package com.drk.SpringAIDemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drk.SpringAIDemo.entity.KnowledgeFileEntity;
import com.drk.SpringAIDemo.pojo.ApiResponse;
import com.drk.SpringAIDemo.pojo.FileUploadResponse;
import com.drk.SpringAIDemo.service.KnowledgeFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 知识文件控制器
 * @Author drk
 * @Date 2025/7/11 10:33
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
public class KnowledgeFileController {

    @Autowired
    private KnowledgeFileService knowledgeFileService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "enableVectorization", defaultValue = "true") boolean enableVectorization) {
        
        try {
            KnowledgeFileEntity fileEntity = knowledgeFileService.uploadFileWithVectorization(file, description, enableVectorization);
            
            // 检查是否为重复文件
            boolean isDuplicate = knowledgeFileService.findByFileHash(fileEntity.getFileHash()) != null;
            
            FileUploadResponse response = new FileUploadResponse();
            response.setId(fileEntity.getId());
            response.setFileName(fileEntity.getFileName());
            response.setFileType(fileEntity.getFileType());
            response.setFileSize(fileEntity.getFileSize());
            response.setFileSuffix(fileEntity.getFileSuffix());
            response.setDescription(fileEntity.getDescription());
            response.setStatus(fileEntity.getStatus());
            response.setCreatedAt(fileEntity.getCreatedAt());
            response.setIsDuplicate(isDuplicate);
            response.setMessage(isDuplicate ? "文件已存在，返回已有文件信息" : "文件上传成功");
            
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            KnowledgeFileEntity fileEntity = knowledgeFileService.getById(id);
            if (fileEntity == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(fileEntity.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileEntity.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("文件下载失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/{id}")
    public ApiResponse<KnowledgeFileEntity> getFileInfo(@PathVariable Long id) {
        KnowledgeFileEntity fileEntity = knowledgeFileService.getById(id);
        if (fileEntity == null) {
            return ApiResponse.error("文件不存在");
        }
        return ApiResponse.success(fileEntity);
    }

    /**
     * 分页查询文件列表
     */
    @GetMapping("/list")
    public ApiResponse<Page<KnowledgeFileEntity>> getFileList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String fileType) {
        
        Page<KnowledgeFileEntity> page = new Page<>(current, size);
        QueryWrapper<KnowledgeFileEntity> queryWrapper = new QueryWrapper<>();
        
        if (fileName != null && !fileName.trim().isEmpty()) {
            queryWrapper.like("file_name", fileName);
        }
        
        if (fileType != null && !fileType.trim().isEmpty()) {
            queryWrapper.eq("file_type", fileType);
        }
        
        queryWrapper.orderByDesc("created_at");
        
        Page<KnowledgeFileEntity> result = knowledgeFileService.page(page, queryWrapper);
        return ApiResponse.success(result);
    }

    /**
     * 根据文件名搜索
     */
    @GetMapping("/search")
    public ApiResponse<List<KnowledgeFileEntity>> searchFiles(@RequestParam String fileName) {
        List<KnowledgeFileEntity> files = knowledgeFileService.findByFileNameLike(fileName);
        return ApiResponse.success(files);
    }

    /**
     * 根据文件类型查询
     */
    @GetMapping("/by-type")
    public ApiResponse<List<KnowledgeFileEntity>> getFilesByType(@RequestParam String fileType) {
        List<KnowledgeFileEntity> files = knowledgeFileService.findByFileType(fileType);
        return ApiResponse.success(files);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFile(@PathVariable Long id) {
        boolean deleted = knowledgeFileService.deleteFileById(id);
        if (deleted) {
            return ApiResponse.success("文件删除成功", null);
        } else {
            return ApiResponse.error("文件删除失败");
        }
    }

    /**
     * 批量删除文件
     */
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteFiles(@RequestBody List<Long> ids) {
        try {
            int successCount = 0;
            for (Long id : ids) {
                if (knowledgeFileService.deleteFileById(id)) {
                    successCount++;
                }
            }
            return ApiResponse.success("批量删除完成，成功删除 " + successCount + " 个文件", null);
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            return ApiResponse.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 更新文件描述
     */
    @PutMapping("/{id}/description")
    public ApiResponse<KnowledgeFileEntity> updateFileDescription(
            @PathVariable Long id, 
            @RequestParam String description) {
        
        KnowledgeFileEntity fileEntity = knowledgeFileService.getById(id);
        if (fileEntity == null) {
            return ApiResponse.error("文件不存在");
        }
        
        fileEntity.setDescription(description);
        boolean updated = knowledgeFileService.updateById(fileEntity);
        
        if (updated) {
            return ApiResponse.success("描述更新成功", fileEntity);
        } else {
            return ApiResponse.error("描述更新失败");
        }
    }

    /**
     * 对已存在的文件进行向量化
     */
    @PostMapping("/{id}/vectorize")
    public ApiResponse<Void> vectorizeFile(@PathVariable Long id) {
        boolean success = knowledgeFileService.vectorizeExistingFile(id);
        if (success) {
            return ApiResponse.success("文件向量化成功", null);
        } else {
            return ApiResponse.error("文件向量化失败");
        }
    }

    /**
     * 搜索相似文档
     */
    @GetMapping("/search-similar")
    public ApiResponse<List<Document>> searchSimilarDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(defaultValue = "0.7") float threshold) {

        try {
            List<Document> documents = knowledgeFileService.searchSimilarDocuments(query, topK, threshold);
            return ApiResponse.success(documents);
        } catch (Exception e) {
            log.error("搜索相似文档失败", e);
            return ApiResponse.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 批量向量化文件
     */
    @PostMapping("/batch-vectorize")
    public ApiResponse<Void> batchVectorizeFiles(@RequestBody List<Long> ids) {
        try {
            int successCount = 0;
            for (Long id : ids) {
                if (knowledgeFileService.vectorizeExistingFile(id)) {
                    successCount++;
                }
            }
            return ApiResponse.success("批量向量化完成，成功处理 " + successCount + " 个文件", null);
        } catch (Exception e) {
            log.error("批量向量化失败", e);
            return ApiResponse.error("批量向量化失败: " + e.getMessage());
        }
    }
}
