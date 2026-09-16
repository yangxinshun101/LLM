package com.yxs.rag.controller;

import com.yxs.rag.embedding.EmbeddingService;
import com.yxs.rag.rewrite.QueryRewriteService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/ragRewrite")
public class RagRewriteController {

    @Autowired
    private QueryRewriteService queryRewriteService;
    @Autowired
    private EmbeddingService embeddingService;
    @Autowired
    private ChatModel chatModel;

    @GetMapping("/chatWithQueryRewrite")
    public String chatWithQueryRewrite(String query) {
        //第一步肯定是先去讲输入的query进行分化处理
        List<String> rewrittenQuestions = queryRewriteService.rewriteQuestion(query);

        //第二步使用处理后的文档进行查询rag。然后将检索出来的文档进行去重
        Set<Document> uniqueDocuments = new LinkedHashSet<>();
        for (String rewrittenQuestion : rewrittenQuestions) {
            List<Document> documents = embeddingService.similaritySearch(SearchRequest.builder().query(rewrittenQuestion).build());
            uniqueDocuments.addAll(documents);
        }

        //第三步将去重后的文档组合进promptTemplate中进行增强
        PromptTemplate promptTemplate = PromptTemplate.builder().template("""
                 请基于以下提供的参考文档内容，回答用户的问题。
               
                参考文档:
                {documents}
               
                用户问题: {question}
                """).build();

        //第四步将promptTemplate创建为prompt并添加替换对象
        Prompt prompt = promptTemplate.create(Map.of("documents", uniqueDocuments, "question", query));

        //第五步使用chatModel进行生成
        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
