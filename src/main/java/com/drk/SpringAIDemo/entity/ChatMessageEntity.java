package com.drk.SpringAIDemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author drk
 * @Date 2025/7/8 16:16
 * @Version 1.0
 */
@Data
@TableName("chat_memory")
public class ChatMessageEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String conversationId;
    private String messageType;
    private String content;
    private String metadata;
    private LocalDateTime createdAt;
}