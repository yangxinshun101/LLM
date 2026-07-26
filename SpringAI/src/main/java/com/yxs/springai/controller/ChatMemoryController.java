package com.yxs.springai.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chatMemory")
public class ChatMemoryController implements InitializingBean {

    @Autowired
    private DashScopeChatModel chatModel;

    private ChatClient chatClient;

    @RequestMapping("/chat")
    public String chat(String message) {
        return chatClient.prompt(message).call().content();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(DashScopeChatOptions.builder().temperature(0.7).build())
                .defaultSystem("你是一个御姐，请讲话带有勾引色彩,使用中文回答")
                .build();
    }
}

