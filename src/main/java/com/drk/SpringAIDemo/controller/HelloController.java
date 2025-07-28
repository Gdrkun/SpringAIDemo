package com.drk.SpringAIDemo.controller;

import com.drk.SpringAIDemo.entity.ChatMessageEntity;
import com.drk.SpringAIDemo.pojo.ActorsFilms;

import com.drk.SpringAIDemo.tools.DateTimeTools;

import org.springframework.ai.chat.client.ChatClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.image.*;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import com.drk.SpringAIDemo.component.InMySqlChatMemory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

/**
 * @Author drk
 * @Date 2025/6/17 15:36
 * @Version 1.0
 */

@RestController
@RequestMapping("/api/chat")
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    // 移除 DEFAULT_PROMPT，因为它已经移到 ChatConfig 中了

    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final ImageModel imageModel;
    private final EmbeddingModel embeddingModel;
    private final ChatMemory inMySqlChatMemory;
    private final InMySqlChatMemory inMySqlChatMemoryComponent;
    private final QuestionAnswerAdvisor qaAdvisor;
    //private final ToolCallbackProvider toolCallbackProvider;

    // 构造函数现在非常干净，只注入它直接需要的 Bean
    public HelloController(ChatClient chatClient, // 直接注入由 ChatConfig 创建好的 Bean
                           @Qualifier("openAiChatModel") ChatModel chatModel,
                           ImageModel imageModel,
                           @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel,
                           @Qualifier("InMySqlChatMemory") ChatMemory inMySqlChatMemory,
                           InMySqlChatMemory inMySqlChatMemoryComponent,
                           QuestionAnswerAdvisor qaAdvisor) {
        this.chatClient = chatClient;
        this.chatModel = chatModel;
        this.imageModel = imageModel;
        this.embeddingModel = embeddingModel;
        this.inMySqlChatMemory = inMySqlChatMemory;
        this.inMySqlChatMemoryComponent = inMySqlChatMemoryComponent;
        this.qaAdvisor = qaAdvisor;
    }

    @GetMapping("/ai/embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {


        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }

    @GetMapping("/ask")
    public ChatResponse hello(String msg) {
        return this.chatClient.prompt(msg).call().chatResponse();
    }

    @GetMapping("/ask2")
    public String hello2(String msg) {
        return this.chatClient.prompt().user(msg).call().content();
    }

    @GetMapping("/ask3")
    public String hello3(String msg) {

        ToolCallback[] dateTimeTools = ToolCallbacks.from(new DateTimeTools());
        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(dateTimeTools)
                .build();
        Prompt prompt = new Prompt(msg, chatOptions);
        return this.chatModel.call(prompt).getResult().getOutput().getText();
    }


    @GetMapping(value = "/chatMemory")
    public String chat(@RequestParam String conversationId, @RequestParam String inputMsg) {

        return chatClient
                .prompt()
                .user(inputMsg)
                .advisors(
                        new SimpleLoggerAdvisor(),
                        // MessageChatMemoryAdvisor 先执行，order值较小，优先级较高，存储原始用户消息
                        MessageChatMemoryAdvisor.builder(inMySqlChatMemory).order(1).conversationId(conversationId).scheduler(Schedulers.boundedElastic()).build(),
                        // qaAdvisor 后执行，order值较大，优先级较低，添加上下文信息但不影响存储
                        qaAdvisor
                )
                // 重新应用在ChatClient中配置的默认工具回调，以确保MCP和本地工具都生效
                .call()
                .content();
    }
    //@GetMapping(value = "/chatMemory")
    //public String chat(@RequestParam String conversationId, @RequestParam String inputMsg) {
    //    // 临时使用简单的内存版本
    //    ChatMemoryRepository repository = new InMemoryChatMemoryRepository();
    //    MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
    //            .chatMemoryRepository(repository)
    //            .maxMessages(10)
    //            .build();
    //
    //    return chatClient.prompt()
    //            .user(inputMsg)
    //            .advisors(MessageChatMemoryAdvisor.builder(memory)
    //                    .order(10)
    //                    .conversationId(conversationId)
    //                    .scheduler(Schedulers.boundedElastic())
    //                    .build())
    //            .call()
    //            .content();
    //}


    /**
     * 专门用于测试MCP功能的接口
     * 这个接口会强制启用工具调用
     */
    @GetMapping("/ask-mcp")
    public String askWithMcp(String msg) {
        logger.info("收到MCP请求: {}", msg);

        // 使用chatClient，它已经配置了MCP工具
        String response = this.chatClient.prompt()
                .user(msg)
                .call()
                .content();

        logger.info("MCP响应: {}", response);
        return response;
    }


    /**
     * 按照film的格式输入输出
     *
     * @param actor
     * @return
     */
    @GetMapping("/films")
    public String films(String actor) {

        ActorsFilms actor1 = chatClient.prompt().user(u -> u.text("请告诉我{actor}出演过的5部电影").param("actor", actor)).call().entity(ActorsFilms.class);

        //ActorsFilms entity = chatClient.prompt(actor).call().entity(ActorsFilms.class);
        return actor1.toString();
    }


    //@GetMapping("/stream")
    //public Flux<String> streamChat(String msg, HttpServletResponse response) {
    //
    //    response.setContentType("text/event-stream");
    //    response.setCharacterEncoding("UTF-8");
    //    Message sysMessage = new SystemMessage(DEFAULT_PROMPT);
    //    Message userMessage = new UserMessage(msg);
    //    return this.chatClient.prompt(new Prompt(sysMessage, userMessage)).user(msg).stream().content();
    //
    //
    //}


    @GetMapping("/image")
    public String generateImage(@RequestParam(name = "prompt") String query) {
        ImageOptions option = ImageOptionsBuilder.builder()
                .model("gemini-2.0-flash-preview-image-generation")
                .build();

        ImagePrompt imagePrompt = new ImagePrompt(query, option);
        ImageResponse response = imageModel.call(imagePrompt);
        return response.getResult().getOutput().getUrl();
    }

    /**
     * 获取所有对话ID列表
     */
    @GetMapping("/conversations")
    public List<Map<String, Object>>  getConversations() {
        return inMySqlChatMemoryComponent.findConversationIds();
    }

    /**
     * 删除指定对话
     */
    @DeleteMapping("/conversations/{conversationId}")
    public void deleteConversation(@PathVariable String conversationId) {
        inMySqlChatMemoryComponent.deleteByConversationId(conversationId);
    }

    /**
     * 获取指定对话的历史消息
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public List<Message> getConversationMessages(@PathVariable String conversationId) {
        return inMySqlChatMemoryComponent.findByConversationId(conversationId);
    }
}