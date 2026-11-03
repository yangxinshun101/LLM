package com.yxs.generalagent.controller;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.yxs.generalagent.tools.Stock;
import com.yxs.generalagent.tools.StockTools;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
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

    @Autowired
    private ToolCallingManager toolCallingManager;
    @RequestMapping("/chatWithSpringAi")
    public String chatWithSpringAi(String conversationId) {
        //构建一次对话，并给于调用的tools
        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                .model("deepseek-v4.1-flash-1017")
                .toolCallbacks(ToolCallbacks.from(new StockTools()))
                .internalToolExecutionEnabled(false)
                .build();

        //定义提示词，要求按照React架构运行
        Prompt prompt = new Prompt(
                List.of(new SystemMessage("你是一个基于React架构（Reasoning-Act-Observation）的智能助手，你擅长使用工具帮我解决问题。" +
                        "你的工作流程是：" +
                        "1、思考：先根据用户的提问进行思考，推理出下一步需要进行的具体系统" +
                        "2、行动：做具体的行动，这一步可以使用工具" +
                        "3、观察：记录前一步行动的结果。你可以进行多轮思考和行动。如果要使用工具，请务必调用工具，不要自己随便捏造结果。"
                        + "约束：时间通过工具获取，不要捏造"), new UserMessage("帮我分析最近三个月特斯拉（TSLA）的股价走势，并结合新闻事件解释可能的影响因素。")),
                chatOptions);


        //添加提示词到记忆
        chatMemory.add(conversationId, prompt.getInstructions());

        Prompt promptWithMemory = new Prompt(chatMemory.get(conversationId), chatOptions);

        ChatResponse call = chatModel.call(promptWithMemory);

        //创建ChatMemory，将对话历史保存起来
        chatMemory.add(conversationId, call.getResult().getOutput());

        //构建一个while true循环，使用chatResponse的Toolcall判断是否需要再次调用模型
        while (call.hasToolCalls()){
            //调用Tool的执行方法，执行大模型需要调用的tools
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, call);

            chatMemory.add(conversationId, toolExecutionResult.conversationHistory()
                    .get(toolExecutionResult.conversationHistory().size() -1));

            //将问题与回答拼成新的提示词
            promptWithMemory = new Prompt(chatMemory.get(conversationId), chatOptions);

            call = chatModel.call(promptWithMemory);

            chatMemory.add(conversationId, call.getResult().getOutput());
        }

        System.out.println("===========对话历史==========");
        for (Message message11 : chatMemory.get(conversationId)) {
            System.out.println(message11);
        }

        return call.getResult().getOutput().getText();
    }


    //创建一个基于Spring AI alibaba的React Agent
    @RequestMapping("/chatWithSpringAiAlibaba")
    public String chatWithSpringAiAlibaba(String conversationId) throws GraphRunnerException {

        //先构建一个系统提示词
        SystemMessage systemMessage = new SystemMessage("你是一个基于React架构（Reasoning-Act-Observation）的智能助手，你擅长使用工具帮我解决问题。" +
                "你的工作流程是：" +
                "1、思考：先根据用户的提问进行思考，推理出下一步需要进行的具体系统" +
                "2、行动：做具体的行动，这一步可以使用工具" +
                "3、观察：记录前一步行动的结果。你可以进行多轮思考和行动。如果要使用工具，请务必调用工具，不要自己随便捏造结果。" +
                "约束：时间通过工具获取，不要捏造");

        ReactAgent agent = ReactAgent.builder()
                // 设置React Agent的名称
                .name("React Agent")
                // 设置React Agent使用的模型
                .model(chatModel)
                // 设置React Agent使用的工具
                .tools(ToolCallbacks.from(new StockTools()))
                .systemPrompt(systemMessage.getText())
                .saver(new MemorySaver())
                .build();

        RunnableConfig config = RunnableConfig.builder()
                .threadId(conversationId)
                .build();

        return agent.call("帮我分析最近三个月特斯拉（TSLA）的股价走势，并结合新闻事件解释可能的影响因素。", config).getText();
    }

    @Autowired
    private DataSource dataSource;

    @GetMapping("/chatWithMySqlSaver")
    public String chatWithMySqlSaver(String conversationId,String msg) throws GraphRunnerException {
        String systemMessage = "你是一个智能的助手，是一个狂热的龙族迷，请根据用户的问题，回答龙族的问题。";

        ReactAgent agent = ReactAgent.builder()
                .name("React Agent")
                .model(chatModel)
                .tools(ToolCallbacks.from(new StockTools()))
                .systemPrompt(systemMessage)
                .saver(new MysqlSaver.Builder().dataSource(dataSource).build())
                .build();

        RunnableConfig config = RunnableConfig.builder()
                .threadId(conversationId)
                .build();

        return agent.call(msg, config).getText();
    }
}
