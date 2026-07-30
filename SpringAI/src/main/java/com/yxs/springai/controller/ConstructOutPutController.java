package com.yxs.springai.controller;


import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.yxs.springai.model.Companies;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/constructOutPut")
public class ConstructOutPutController implements InitializingBean {

    @Autowired
    private ChatModel dashScopeChatModel;

    private ChatClient chatClient;

    //在写Spring Ai的项目时，第一步先创建 chatModel，再基于InitializeBean接口初始化一个ChatClient


    @RequestMapping("/chat")
    public String chat(String message) {
        BeanOutputConverter<Object> objectBeanOutputConverter = new BeanOutputConverter<>(Object.class);


        PromptTemplate promptTemplate = new PromptTemplate("""
                请给我推荐一些Java大厂
                {format}
                """);

        return chatClient.prompt(promptTemplate.create(Map.of("format", objectBeanOutputConverter.getFormat()))).call().content();
    }

    @RequestMapping("/covert")
    public String convert(String message) {
        BeanOutputConverter<Companies> objectBeanOutputConverter = new BeanOutputConverter<>(Companies.class);


        PromptTemplate promptTemplate = new PromptTemplate("""
                请给我推荐一些{message}的Java大厂
                {format}
                """);

        Map map = new HashMap<>();
        map.put("format", objectBeanOutputConverter.getFormat());
        map.put("message", message);

        String content = chatClient.prompt(promptTemplate.create(map)).call().content();

        Companies companies = objectBeanOutputConverter.convert(content);
        return companies.name() + "" + companies.address() + "" + companies.resume() + " " + companies.description();
    }


    @RequestMapping("/entity")
    public String entity(String message) {

        PromptTemplate promptTemplate = new PromptTemplate("""
                请给我推荐一些{message}的Java大厂
                """);

        Map map = new HashMap<>();
        map.put("message", message);

        Companies companies = chatClient.prompt(promptTemplate.create(map)).call().entity(Companies.class);

        return companies.name() + "-" + companies.address() + "-" + companies.resume() + "-" + companies.description();
    }
    @RequestMapping("/list")
    public String list(String message) {

        PromptTemplate promptTemplate = new PromptTemplate("""
                请给我推荐一些{message}的Java大厂
                """);

        Map map = new HashMap<>();
        map.put("message", message);

        List<Companies> companies = chatClient.prompt(promptTemplate.create(map)).call().entity(new ParameterizedTypeReference<>() {
        });

        return companies.toString();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(DashScopeChatOptions.builder().temperature(0.7).build())
                .defaultSystem("你是一个御姐，请讲话带有勾引色彩,使用中文回答").build();
    }

}
