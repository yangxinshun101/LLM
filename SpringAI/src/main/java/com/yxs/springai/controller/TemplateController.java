package com.yxs.springai.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/template")
public class TemplateController implements InitializingBean {

    private ChatClient chatClient;

    @Autowired
    private ChatModel chatModel;

    @RequestMapping("/stream")
    public Flux<String> stream(String message, HttpServletResponse response) {

        response.setCharacterEncoding("utf-8");

        String prompt = """
                请给推荐一个关于{topic}的学习教程";
                """;

        //PromptTemplate的构造方法在只传入String时，会将String赋值给提示词，而默认初始化一个Map
        PromptTemplate promptTemplate = new PromptTemplate(prompt);
        promptTemplate.add("topic", message);

        return chatClient.prompt(promptTemplate.create()).stream().content();
    }

    @RequestMapping("/stream1")
    public Flux<String> stream1(String message) {

        String prompt = """
                请给推荐一个关于{topic}的学习教程";
                """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("topic", message);

        return chatClient.prompt(new PromptTemplate(prompt).create(variables)).stream().content();
    }

    @Value("classpath:templates/tpl.txt")
    Resource fileURL;
    @RequestMapping("/stream2")
    public Flux<String> stream2(String message, HttpServletResponse response) {

        response.setCharacterEncoding("utf-8");

        Map<String, Object> variables = new HashMap<>();
        variables.put("language", "English");
        variables.put("topic", message);


        return chatClient.prompt(new PromptTemplate(fileURL).create(variables)).system("你是一个 helpful的助手。").stream().content();
    }

    @RequestMapping("/stream3")
    public Flux<String> stream3(String message, HttpServletResponse response) {

        response.setCharacterEncoding("utf-8");

        Map<String, Object> variables = new HashMap<>();
        variables.put("language", "中文");
        variables.put("topic", message);

        PromptTemplate promptTemplate = PromptTemplate.builder().resource(fileURL).variables(variables).build();
        Prompt prompt = promptTemplate.create();

        return chatClient.prompt(prompt).system("你是御姐，你的回答应该十分的妩媚和温柔").stream().content();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(ChatOptions.builder().temperature(0.7).build())
                .build();
    }
}
