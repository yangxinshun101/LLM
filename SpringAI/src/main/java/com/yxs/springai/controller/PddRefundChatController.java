package com.yxs.springai.controller;

import com.yxs.springai.model.ChatStatus;
import com.yxs.springai.model.OrderChat;
import com.yxs.springai.tools.OrderTool;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/pddOrderChat")
public class PddRefundChatController implements InitializingBean {
    @Autowired
    private ChatModel dashScopeChatModel;

    private ChatClient chatClient;
    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private OrderTool orderTool;

    @Value("classpath:templates/pdd_refund_system_prompt.pt")
    private Resource systemPrompt;

    @GetMapping("/newChat")
    private OrderChat newChat(String userId, String orderId, HttpServletResponse response){
        response.setCharacterEncoding("utf-8");

        UUID chatId = UUID.randomUUID();

        return chatClient.prompt(
                String.format("我要咨询订单相关的售后问题，我的用户id是%s,我的订单号是: %s ,本地的对话Id是 %s，当前状态是 %s",
                        userId, orderId, chatId, ChatStatus.CHAT_START.name()))
                .call().entity(OrderChat.class);
    }

    @RequestMapping("/ask")
    private Flux<String> ask(String chatId, String question, HttpServletResponse response){
        response.setCharacterEncoding("utf-8");

        return chatClient.prompt().user(question)
                .tools(orderTool)
                .advisors(spec->{
                    spec.param(CONVERSATION_ID, chatId);
                    spec.param("chat_memory_retrieve_size", 10);
                })
                .stream().content();
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), new SimpleLoggerAdvisor())
                .defaultOptions(ChatOptions.builder().temperature(0.7).build()).build();
    }
}
