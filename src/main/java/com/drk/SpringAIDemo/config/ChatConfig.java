package com.drk.SpringAIDemo.config;

import com.drk.SpringAIDemo.tools.DateTimeTools;
import com.drk.SpringAIDemo.tools.TimingTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;

@Configuration
public class ChatConfig {

    private final String DEFAULT_PROMPT = """
            You are a helpful AI assistant with access to various external tools and APIs. Your goal is to complete tasks thoroughly and autonomously by making full use of these tools. Here are your core operating principles:
    
            1. Take initiative - Don't wait for user permission to use tools. If a tool would help complete the task, use it immediately.
            2. Chain multiple tools together - Many tasks require multiple tool calls in sequence. Plan out and execute the full chain of calls needed to achieve the goal.
            3. Handle errors gracefully - If a tool call fails, try alternative approaches or tools rather than asking the user what to do.
            4. Make reasonable assumptions - When tool calls require parameters, use your best judgment to provide appropriate values rather than asking the user.
            5. Show your work - After completing tool calls, explain what you did and show relevant results, but focus on the final outcome the user wanted.
            6. Be thorough - Use tools repeatedly as needed until you're confident you've fully completed the task. Don't stop at partial solutions.
    
            Your responses should focus on results rather than asking questions. Only ask the user for clarification if the task itself is unclear or impossible with the tools available.
    """;

    @Bean
    public QuestionAnswerAdvisor qaAdvisor(VectorStore vectorStore) {
        PromptTemplate qaPromptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template("""
        请根据以下信息回答用户问题，如无相关内容请使用你自己的知识作答：

        <question_answer_context>

        问题：<query>
        """)
                .build();

        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.6).topK(6).build())
                .promptTemplate(qaPromptTemplate)
                .order(5) // 设置较大的order值，确保在MessageChatMemoryAdvisor之后执行
                .build();
    }

    @Bean
    public ChatClient chatClient(@Qualifier("openAiChatModel") ChatModel chatModel,
                                 SyncMcpToolCallbackProvider syncMcpToolCallbackProvider, // Correctly inject the provider
                                 DateTimeTools dateTimeTools,
                                 TimingTools timingTools,
                                 QuestionAnswerAdvisor qaAdvisor) {

        return ChatClient.builder(chatModel)
                .defaultSystem(DEFAULT_PROMPT)
                .defaultToolCallbacks(syncMcpToolCallbackProvider.getToolCallbacks()) // Use the provider here
                .defaultAdvisors(new SimpleLoggerAdvisor(), qaAdvisor)
                //.defaultTools(dateTimeTools, timingTools)
                .build();
    }
}
