package com.drk.SpringAIDemo.utils;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;


public class MessageRowMapper implements RowMapper<Message> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
        // 1. 从数据库获取原始数据
        String typeStr = rs.getString("message_type");
        String content = rs.getString("content");
        String metadataJson = rs.getString("metadata");

        // 2. 将字符串和 JSON 转换为 Java 对象
        MessageType messageType = MessageType.valueOf(typeStr);
        Map<String, Object> metadata;
        try {
            metadata = objectMapper.readValue(metadataJson, Map.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to deserialize message metadata from JSON", e);
        }

        // 3. 根据消息类型，分两步创建对象并填充元数据
        switch (messageType) {
            case USER:
                // 第一步：使用公共构造函数创建对象
                UserMessage userMessage = new UserMessage(content);
                // 第二步：获取元数据Map并添加数据
                if (metadata != null && !metadata.isEmpty()) {
                    userMessage.getMetadata().putAll(metadata);
                }
                return userMessage;

            case ASSISTANT:
                // AssistantMessage 通常有接受元数据的公共构造函数
                // 如果没有，也需要采用和 UserMessage 一样的两步法
                return new AssistantMessage(content, metadata);

            case SYSTEM:
                // 第一步：使用公共构造函数创建对象
                SystemMessage systemMessage = new SystemMessage(content);
                // 第二步：获取元数据Map并添加数据
                if (metadata != null && !metadata.isEmpty()) {
                    systemMessage.getMetadata().putAll(metadata);
                }
                return systemMessage;

            default:
                // 对于未知或不支持的类型，抛出异常
                throw new IllegalArgumentException("Unsupported message type: " + messageType);
        }
    }
}