package com.yxs.rag.controller;

import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.yxs.rag.cleaner.DocumentCleaner;
import com.yxs.rag.reader.DocumentReaderFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/rag/metaData")
public class RagMetaDataController implements InitializingBean {

    @Autowired
    private ChatModel chatModel;
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private DocumentReaderFactory documentReaderFactory;

    @RequestMapping("/embedding")
    public String embedding(String filePath, String fileName) throws IOException {

        //1.加载文档
        List<Document> documentList = documentReaderFactory.read(new File(filePath));

        //2.清洗并切片
        List<Document> documents = DocumentCleaner.cleanDocuments(documentList);

        List<Document> split = new RecursiveCharacterTextSplitter(200, new String[]{"。"}).split(documents);

        split.forEach(document -> document.getMetadata().put("fileName", fileName));

        //3.生成向量,因为embedding模型中有默认的分片文档大小，所以在实际分片时要执行窗口限制
        for (int i = 0; i < split.size(); i += 9) {
            List<Document> subList = split.subList(i, Math.min(i + 9, split.size()));
            //4.保存向量
            vectorStore.add(subList);
        }

        return "success";
    }

    @RequestMapping("/query")
    public Flux<String> query(String query, String fileName, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt(query).advisors(advisorSpec -> advisorSpec.param("qa_filter_expression", "fileName == '" + fileName + "'")).stream().content();
    }


    private ChatClient chatClient;
    @Override
    public void afterPropertiesSet() throws Exception {
        PromptTemplate promptTemplate = new PromptTemplate("请根据文档内容" +
                "{question_answer_context}" +
                "回答问题: {query}"
        );
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(promptTemplate)
                .build();

        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(questionAnswerAdvisor)
                .build();
    }
}
