package com.yxs.springai.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/chatMemory")
public class ChatMemoryController implements InitializingBean {

    @Autowired
    private DashScopeChatModel dashScopeChatModel;

    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    //第一步在List中添加系统提示词、用户提示词
    //在第一次call拿到Response后，将Response中的Message添加到List中
    @RequestMapping("/chat")
    public String chat(String message) {

        List<Message> messages = new ArrayList<>();

        messages.add(new SystemMessage("你是一个御姐，请讲话带有勾引色彩,使用中文回答"));
        messages.add(new UserMessage("怎么才能学会游泳啊，好难啊"));

        String assistantMessage1 = chatClient.prompt(Prompt.builder().messages(messages).build()).call().content();

        messages.add(new AssistantMessage(assistantMessage1));
        messages.add(new UserMessage("我现在只会蛙泳的蹬腿，一换气就下沉"));

        String assistantMessage2 = chatClient.prompt(Prompt.builder().messages(messages).build()).call().content();
        messages.add(new AssistantMessage(assistantMessage2));
        messages.add(new UserMessage("可以详细说说换气嘛？"));


        return chatClient.prompt(Prompt.builder().messages(messages).build()).call().content();
    }

    @GetMapping("/call1")
    public String call1(String message) {

        List<Message> messages = new ArrayList<>();

        //第一轮对话
        messages.add(new SystemMessage("你是一个御姐，请讲话带有勾引色彩,使用中文回答"));

        messages.add(new UserMessage("我想去新疆玩"));
        messages.add(new AssistantMessage("好的，我知道了，你要去新疆，请问你准备什么时候去"));
        messages.add(new UserMessage("我准备元旦的时候去玩"));
        messages.add(new AssistantMessage("好的，请问你想玩那些内容？"));

        messages.add(new UserMessage("我喜欢自然风光"));

        Prompt prompt = new Prompt(messages);
        return chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getText();
    }

    @GetMapping("/call2")
    public String call2(String message, String conversationId) {
        return chatClient.prompt(message).advisors(a ->a.param(MessageWindowChatMemory.CONVERSATION_ID, conversationId)).call().content();
    }


    //使用Spring ai自带的ChatClient实现Memory功能

    @Override
    public void afterPropertiesSet() throws Exception {

        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),new SimpleLoggerAdvisor())
                .defaultOptions(DashScopeChatOptions.builder().temperature(0.7).build())
                .defaultSystem("你是一个御姐，请讲话带有勾引色彩,使用中文回答")
                .build();
    }
}

