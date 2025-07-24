package com.drk.SpringAIDemo.config;

/**
 * @Author drk
 * @Date 2025/7/9 11:58
 * @Version 1.0
 */
import com.drk.SpringAIDemo.tools.DateTimeTools;
import com.drk.SpringAIDemo.tools.TimingTools;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
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
    You are a smart Chinese AI assistant. Only use tools if the user explicitly requests a tool-specific action (e.g., asking for the current date or search). Otherwise, answer based on internal knowledge and documents.
    
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
                                 ToolCallbackProvider tools,
                                 DateTimeTools dateTimeTools,
                                 TimingTools timingTools,
                                 QuestionAnswerAdvisor qaAdvisor) {

        return ChatClient.builder(chatModel)
                .defaultSystem(DEFAULT_PROMPT)
                .defaultToolCallbacks(tools)
                .defaultAdvisors(new SimpleLoggerAdvisor(),qaAdvisor)
                //.defaultTools(dateTimeTools, timingTools)
                .build();
    }
}