package com.yxs.springai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/chat")
public class ChatModelController {

    @Autowired
    private DashScopeChatModel chatModel;

    @RequestMapping("/call/string")
    public String callString(String messages) {

        return chatModel.call(messages);
    }

    @RequestMapping("/call/messages")
    public String callMessages(String messages) {
        Message message = new SystemMessage("你是一个很厉害的翻译，请帮我把我的输入翻译成隐晦的英语");

        Message userMessage = new UserMessage(messages);

        return chatModel.call(message, userMessage);
    }

    @RequestMapping("/call/prompt")
    public String callPrompt(String messages) {

        Message message = new SystemMessage("请在回答问题前告诉你是什么模型！");
        Message userMessage = new UserMessage(messages);

        ChatOptions chatOptions = ChatOptions.builder().model("deepseek-v3").temperature(0.7).build();

        Prompt prompt = Prompt.builder().messages(message, userMessage).chatOptions(chatOptions).build();

        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    @RequestMapping("/stream/prompt")
    public Flux<String> streamPrompt(String messages, HttpServletResponse responses) {

        responses.setCharacterEncoding("utf-8");

        Message message = new SystemMessage("请在回答问题前告诉我是什么模型！");
        Message userMessage = new UserMessage(messages);

        ChatOptions chatOptions = ChatOptions.builder().model("deepseek-v3").temperature(0.7).build();

        Prompt prompt = Prompt.builder().messages(message, userMessage).chatOptions(chatOptions).build();

        return chatModel.stream(prompt).map(response -> response.getResult().getOutput().getText());
    }
}
