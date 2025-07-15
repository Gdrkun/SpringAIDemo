package com.drk.SpringAIDemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drk.SpringAIDemo.entity.ChatMessageEntity;
import com.drk.SpringAIDemo.mapper.ChatMessageMapper;
import com.drk.SpringAIDemo.service.ChatMessageService;
import org.springframework.stereotype.Service;

/**
 * @Author drk
 * @Date 2025/7/9 10:24
 * @Version 1.0
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessageEntity> implements ChatMessageService {
}
