package com.yxs.springai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat/client")
public class ChatClientController implements InitializingBean {

    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;

    @RequestMapping("/call")
    public String call(String message) {

        //prompt里面其实只会定义提示词和ChatOption
        Prompt prompt = Prompt.builder().messages(new SystemMessage("请用英文回答问题"), new UserMessage(message)).build();

        return chatClient.prompt(prompt).call().content();
    }

    @RequestMapping("/stream")
    public Flux<String> stream(String message, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");

        Prompt prompt = Prompt.builder().messages(new UserMessage(message)).build();

        return chatClient.prompt(prompt).system("你是北航的博导，请指导我如何申请北航的博士。").stream().content();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = ChatClient.builder(chatModel).defaultAdvisors(
                        new SimpleLoggerAdvisor()
                ).defaultSystem("请用英文回答问题")
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .temperature(0.7)
                                .build())
                .build();
    }
}
