//package com.drk.SpringAIDemo.config;
//
//import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
//import org.springframework.ai.tool.ToolCallbackProvider;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
///**
// * MCP配置类
// * 确保MCP工具回调提供者被正确注册
// *
// * @Author drk
// * @Date 2025/7/3
// * @Version 1.0
// */
//@Configuration
//@ConditionalOnClass(AsyncMcpToolCallbackProvider.class)
//@ConditionalOnProperty(name = "spring.ai.mcp.client.enabled", havingValue = "true", matchIfMissing = false)
//public class McpConfig {
//
//    /**
//     * 配置MCP工具回调提供者
//     * 使用@Primary确保这个Bean被优先使用
//     * 添加条件注解避免Bean创建冲突
//     */
//    @Bean
//    @Primary
//    @ConditionalOnClass(AsyncMcpToolCallbackProvider.class)
//    public ToolCallbackProvider mcpToolCallbackProvider(AsyncMcpToolCallbackProvider asyncMcpToolCallbackProvider) {
//        return asyncMcpToolCallbackProvider;
//    }
//}
