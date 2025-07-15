package com.drk.SpringAIDemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识文件实体类
 * @Author drk
 * @Date 2025/7/11 10:33
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_file")
public class KnowledgeFileEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件类型 (MIME类型)
     */
    private String fileType;

    /**
     * 文件大小 (字节)
     */
    private Long fileSize;

    /**
     * 文件哈希值 (用于去重)
     */
    private String fileHash;

    /**
     * 文件后缀
     */
    private String fileSuffix;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 上传状态 (0-上传中, 1-上传成功, 2-上传失败)
     */
    private Integer status;

    /**
     * 向量化状态 (0-未向量化, 1-向量化中, 2-向量化成功, 3-向量化失败)
     */
    private Integer vectorizationStatus;

    /**
     * 向量化时间
     */
    private LocalDateTime vectorizationTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
