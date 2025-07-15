package com.drk.SpringAIDemo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件上传响应类
 * @Author drk
 * @Date 2025/7/11 10:33
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件后缀
     */
    private String fileSuffix;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 上传状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否为重复文件
     */
    private Boolean isDuplicate;

    /**
     * 响应消息
     */
    private String message;
}
