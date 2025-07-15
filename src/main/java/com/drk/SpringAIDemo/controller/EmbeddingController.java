package com.drk.SpringAIDemo.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Author drk
 * @Date 2025/6/19 9:46
 * @Version 1.0
 */
@RestController
@RequestMapping("/ai/api")
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;
    @Autowired
    VectorStore vectorStore;

    public EmbeddingController( @Qualifier("ollamaEmbeddingModel")EmbeddingModel embeddingModel){
        this.embeddingModel=embeddingModel;
    }


    @RequestMapping("/embedding")
    public Map embed(String message){

        List<Document> documents = List.of(new Document(message), new Document("Hello World"),new Document("Test The World",Map.of("id",1)));
        vectorStore.add(documents);

        List<Document> results  = vectorStore.similaritySearch(SearchRequest.builder().query("test").similarityThreshold(0.8f).topK(2).build());
        System.out.println(results);
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message,"Hello World"));
        return Map.of("embedding", embeddingResponse);
    }





}
