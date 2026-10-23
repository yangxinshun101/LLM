package com.yxs.generalagent.controller;

import com.yxs.generalagent.tools.Stock;
import com.yxs.generalagent.tools.StockTools;
import org.apache.logging.log4j.message.Message;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("/react")
public class ReactAgentController {

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ChatModel chatModel;
    /**
     * 自构建一个React Agent需要有工具和记忆，
     * 工具用于扩展Agent的功能，
     * 记忆用于保存Agent的对话历史
     */
    @RequestMapping("/call")
    public String call(String conversationId) {

        //构建ChatOption，将工具和模型
        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(ToolCallbacks.from(new StockTools()))
                .model("deepseek-v4.1-flash-1017")
                .build();

        //构建Prompt
        Prompt prompt = Prompt.builder()
                .messages(List.of(new UserMessage("帮我分析最近三个月特斯拉（TSLA）的股价走势，并结合新闻事件解释可能的影响因素。" +
                        "今天是:" + Calendar.getInstance().getTime())
                        ,new SystemMessage("你是一个智能助手，你擅长使用工具帮我解决问题。" +
                        "约束：时间通过工具获取，不要捏造"))).chatOptions(chatOptions).build();

        //构建记忆
        chatMemory.add(conversationId,prompt.getInstructions());

        Prompt promptWithMemory = new Prompt(chatMemory.get(conversationId),chatOptions);


        return chatModel.call(promptWithMemory).getResult().getOutput().getText();
    }



}
