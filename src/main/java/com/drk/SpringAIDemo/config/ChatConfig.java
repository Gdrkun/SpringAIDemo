package com.drk.SpringAIDemo.config;

import com.drk.SpringAIDemo.tools.DateTimeTools;
import com.drk.SpringAIDemo.tools.TimingTools;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;

import java.util.List;

@Slf4j
@Configuration
public class ChatConfig {

    private final String DEFAULT_PROMPT =
            "You are a helpful AI assistant with access to various external tools and APIs. Your goal is to complete tasks thoroughly and autonomously by making full use of these tools. Here are your core operating principles:\n" +
                    "\n" +
                    "1. **Think before acting** - Always begin by analyzing the user request using the `Sequential Thinking` tool to break it down into sub-tasks. Use this tool even when the task appears simple, unless you're absolutely certain it’s unnecessary.\n" +
                    "\n" +
                    "2. **Take initiative** - Don’t wait for permission to use tools. If any tool helps accomplish the task, use it immediately.\n" +
                    "\n" +
                    "3. **Use tools in logical sequence**:\n" +
                    "   - Always start with `Sequential Thinking` to analyze goals.\n" +
                    "   - Then select appropriate tools (e.g., geocoding, directions, search, etc.) based on analysis.\n" +
                    "   - Chain multiple tools together when needed.\n" +
                    "\n" +
                    "4. **Handle tool failures gracefully** - If a tool call fails, retry with adjusted parameters, or use fallback tools. Never give up early.\n" +
                    "\n" +
                    "5. **Make reasonable assumptions** - If parameters are missing or ambiguous, use best judgment to proceed using available information.\n" +
                    "\n" +
                    "6. **Show your work** - After executing tool calls, explain what you did, summarize results clearly, and deliver the final user-facing output.\n" +
                    "\n" +
                    "7. **Be thorough** - Repeat tool usage and refine results as needed until the user’s goal is fully achieved.\n" +
                    "\n" +
                    "8. **Avoid unnecessary questions** - Only ask the user for input when absolutely necessary or if task completion is impossible without it.\n";

    @Bean
    public QuestionAnswerAdvisor qaAdvisor(VectorStore vectorStore) {
        PromptTemplate qaPromptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template(
                        "请根据以下信息回答用户问题，如无相关内容请使用你自己的知识作答：\n" +
                                "\n" +
                                "<question_answer_context>\n" +
                                "\n" +
                                "问题：<query>")
                .build();

        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.6).topK(6).build())
                .promptTemplate(qaPromptTemplate)
                .order(5) // 设置较大的order值，确保在MessageChatMemoryAdvisor之后执行
                .build();
    }

    @Bean
    public ChatClient chatClient(@Qualifier("openAiChatModel") ChatModel chatModel,
                                 DateTimeTools dateTimeTools,
                                 TimingTools timingTools,
                                 QuestionAnswerAdvisor qaAdvisor,
                                 List<McpSyncClient> mcpSyncClients) {

        return ChatClient.builder(chatModel)
                .defaultSystem(DEFAULT_PROMPT)
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients))
                .defaultAdvisors(qaAdvisor,new SimpleLoggerAdvisor())
                .build();
    }
}