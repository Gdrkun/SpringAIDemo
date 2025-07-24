package com.drk.SpringAIDemo.component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.drk.SpringAIDemo.entity.ChatMessageEntity;

import com.drk.SpringAIDemo.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author drk
 * @Date 2025/7/9 9:33
 * @Version 1.0
 */
@Component("InMySqlChatMemory")
public class InMySqlChatMemory implements ChatMemory {


    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;
    // 使用构造函数注入，并添加 @Lazy 注解
    @Autowired
    public InMySqlChatMemory(@Lazy ChatMessageService chatMessageService, ObjectMapper objectMapper) {
        this.chatMessageService = chatMessageService;
        this.objectMapper = objectMapper;
    }



    @Override
    public void add(String conversationId, List<Message> messages) {
        var memoriesList=new ArrayList<ChatMessageEntity>();
        messages.forEach(message -> {
            var chatMessageEntity=new ChatMessageEntity();
            chatMessageEntity.setConversationId(conversationId);
            chatMessageEntity.setMessageType(message.getMessageType().name());
            chatMessageEntity.setContent(message.getText());
            try {
                chatMessageEntity.setMetadata(objectMapper.writeValueAsString(message.getMetadata()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize metadata to JSON", e);
            }
            memoriesList.add(chatMessageEntity);
        });
        chatMessageService.saveBatch(memoriesList);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<ChatMessageEntity> list = chatMessageService.list(new QueryWrapper<ChatMessageEntity>().eq("conversation_id", conversationId));
        if (CollectionUtils.isEmpty(list)){
            return List.of();
        }

        return list.stream().map(chatMessageEntity -> {
            String type = chatMessageEntity.getMessageType();
            String content = chatMessageEntity.getContent();
            Message message;
            switch (type) {
                case "SYSTEM":
                    message = new SystemMessage(content);
                    break;
                case "USER":
                    message = new UserMessage(content);
                    break;
                case "ASSISTANT":
                    message = new AssistantMessage(content);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message type: " + type);
            }
            return message;
        }).toList();

    }

    @Override
    public void clear(String conversationId) {
        chatMessageService.remove(new QueryWrapper<ChatMessageEntity>().eq(StringUtils.isNoneBlank(conversationId),"conversation_id", conversationId));
    }

    /**
     * 获取所有用户对话列表
     */
    public List<Map<String, Object>>  findConversationIds() {
        return chatMessageService.list()
                .stream()
                .filter(chatMessageEntity -> chatMessageEntity.getMessageType().equals("USER"))
                .collect(Collectors.groupingBy(ChatMessageEntity::getConversationId))
                .entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("conversationId", entry.getKey());
                    map.put("messages", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据对话ID获取消息列表
     */
    public List<Message> findByConversationId(String conversationId) {
        return get(conversationId);
    }

    /**
     * 删除指定对话的所有消息
     */
    public void deleteByConversationId(String conversationId) {
        clear(conversationId);
    }
}
