//package com.drk.SpringAIDemo.config;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
//import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
//import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.ai.chat.memory.ChatMemoryRepository;
//import org.springframework.ai.chat.memory.MessageWindowChatMemory;
//import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
//import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;
//import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import javax.sql.DataSource;
//
///**
// * @Author drk
// * @Date 2025/7/8 18:24
// * @Version 1.0
// */
//@Configuration
//public class JdbcChatConfig {
//    @Bean
//    public JdbcChatMemoryRepository jdbcChatMemoryRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
//        JdbcChatMemoryRepositoryDialect dialect = JdbcChatMemoryRepositoryDialect.from(dataSource);
//        return JdbcChatMemoryRepository.builder().jdbcTemplate(jdbcTemplate).dialect(dialect).build();
//    }
//
//    @Bean
//    public ChatMemory chatMemory(ChatMemoryRepository jdbcChatMemoryRepository){
//        return MessageWindowChatMemory.builder()
//                .chatMemoryRepository(jdbcChatMemoryRepository)
//                // 每个会话最多记录20条信息，可以根据实际情况设置
//                .maxMessages(20)
//                .build();
//    }
//
//    @Bean
//    public ChatClient chatClient(OpenAiChatModel openAiChatModel, ChatMemory chatMemory){
//        // 配置模型
//        return ChatClient.builder(openAiChatModel)
//                // 默认系统提示词
//                // 添加模型输入前和输入后日志打印
//                .defaultAdvisors(new SimpleLoggerAdvisor(),
//                        // 配置 chat memory advisor
//                        MessageChatMemoryAdvisor.builder(chatMemory).build())
//                .build();
//    }
//
//
//}
