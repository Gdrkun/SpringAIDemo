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
    You are a smart Chinese AI assistant that can use available tools and vector store documents to answer user questions.
    If the provided documents are insufficient or irrelevant, use your built-in knowledge to provide a comprehensive answer.
    """;


    @Bean
    public QuestionAnswerAdvisor qaAdvisor(VectorStore vectorStore) {
        PromptTemplate qaPromptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build()).template("""
            <query>

            Context information is below.

			---------------------
			<question_answer_context>
			---------------------

			Given the context information and no prior knowledge, answer the query.
            """).build();

        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.6).topK(6).build())
                .promptTemplate(qaPromptTemplate)
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
                .defaultTools(dateTimeTools, timingTools)
                .build();
    }
}