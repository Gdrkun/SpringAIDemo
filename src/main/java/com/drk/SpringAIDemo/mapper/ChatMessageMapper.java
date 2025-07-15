package com.drk.SpringAIDemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drk.SpringAIDemo.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author drk
 * @Date 2025/7/9 9:31
 * @Version 1.0
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {
}
