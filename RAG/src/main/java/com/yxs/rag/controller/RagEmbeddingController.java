package com.yxs.rag.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.pgvector.PGvector;
import com.yxs.rag.cleaner.DocumentCleaner;
import com.yxs.rag.embedding.EmbeddingService;
import com.yxs.rag.reader.DocumentReaderFactory;
import com.yxs.rag.splitter.OverlapParagraphTextSplitter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/embedding")
public class RagEmbeddingController{

    @Autowired
    private EmbeddingService EmbeddingService;
    @Autowired
    private DocumentReaderFactory documentReaderFactory;
    @Autowired
    private ChatModel chatModel;



    @RequestMapping("/test")
    public String test() {
        for (float[] i : EmbeddingService.getEmbedding(List.of(new Document("test")))) {
            System.out.println(i);
        }
        return "success";
    }

    @RequestMapping("/embeddingAndStore")
    public String embeddingAndStore(String text) {

        List<Document> read;
        try {
            read = documentReaderFactory.read(new File(text));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //数据清洗
        List<Document> documents = DocumentCleaner.cleanDocuments(read);

        //将数据进行切片
        List<Document> documentList = documents.stream().flatMap(doc -> {
            return new OverlapParagraphTextSplitter(258, 50).split(doc).stream();
        }).collect(Collectors.toList());

        //将切片的内容进行向量化存储
        EmbeddingService.embeddingAndStore(documentList);

        return "Success";
    }

    @RequestMapping("/search")
    public Flux<String> retrieve(String query, @RequestParam Double  similarityThreshold, HttpServletResponse httpServletResponse) {

        httpServletResponse.setCharacterEncoding("utf-8");

        //根据问题将问题进行向量化，并检索出相似的文档
        List<Document> documents = EmbeddingService.similaritySearch(SearchRequest.builder()
                .query(query)
                .similarityThreshold(similarityThreshold)
                .build());

        String documentContent = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n=========文档分隔线===========\n\n"));

        //构建提示词模版，将问题和检索的内容进行拼接，并生成新的提示词
        PromptTemplate promptTemplate = PromptTemplate.builder().template(
                "请基于以下提供的参考文档内容，回答用户的问题。\n" +
                "如果参考文档中没有相关信息，请直接说明\"没有找到相关信息\"，不要编造内容。\n" +
                "\n" +
                "参考文档:\n" +
                "{documents}\n" +
                "\n" +
                "用户问题: {question}").build();

        Prompt prompt = promptTemplate.create(Map.of("documents", documentContent, "question", query));



        return chatModel.stream(prompt).map(response-> response.getResult().getOutput().getText());

    }


}
