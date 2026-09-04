package com.yxs.rag.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ragRetriever")
public class RagRetrieverController implements InitializingBean {

    @Autowired
    private ChatModel chatModel;
    @Autowired
    private VectorStore vectorStore;
    private ChatClient chatClient;


    @GetMapping("/retrieveAdvisor")
    public String retrieveAdvisor(String query) {
        return chatClient.prompt(query).call().content();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        PromptTemplate promptTemplate = PromptTemplate.builder().template(
                "请基于以下提供的参考文档内容，回答用户的问题。\n" +
                        "如果参考文档中没有相关信息，请直接说明\"没有找到相关信息\"，不要编造内容。\n" +
                        "\n" +
                        "参考文档:\n" +
                        "{question_answer_context}\n" +
                        "\n" +
                        "用户问题: {query}").build();

        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.5).topK(5).build())
                .promptTemplate(promptTemplate)
                .build();

        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(questionAnswerAdvisor)
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build())
                .build();
    }
}
