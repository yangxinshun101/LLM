package com.yxs.rag.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.yxs.rag.embedding.EmbeddingService;
import com.yxs.rag.reader.PdfMultimodalProcessor;
import com.yxs.rag.splitter.ModalTextSplitter;
import org.simpleframework.xml.Element;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rag/image")
public class RagImageController {

    @Autowired
    private ChatModel chatModel;


    @RequestMapping("/imageWithAlibaba")
    public String imageWithAlibaba(String filePath) throws URISyntaxException {
        //接收图片
        List<Media> media = List.of(new Media(MediaType.IMAGE_PNG, new URI(filePath)));

        UserMessage userMessage =  UserMessage.builder().text("请描述图片信息").media(media).build();
        //构建chatClient 调用百联的多模态大模型
        String result = chatModel.call(new Prompt(userMessage, DashScopeChatOptions.builder().withModel("qwen3-vl-plus").withMultiModel(true).build())).getResult().getOutput().getText();
        //返回结果
        return result;
    }

    @RequestMapping("/imageWithClient")
    public String imageWithClient(String filePath) throws URISyntaxException {

        //先接受图片
        List<Media> media = List.of(new Media(MediaType.IMAGE_PNG, new URI(filePath)));

        //将Media构建为用户提示词
        UserMessage userMessage = UserMessage.builder().text("请描述图片信息").media(media).build();

        ChatClient chatClient = ChatClient.builder(chatModel).defaultOptions(OpenAiChatOptions.builder().model("qwen3-vl-plus").build()).build();

        //返回结果
        return chatClient.prompt(new Prompt(userMessage)).call().content();
    }


    /**
     * 多模态RAG智能体实现流程
     *
     * 1、将文档或网站图片数据进行数字化
     *
     * 2、将数字化数据进行向量化
     *
     * 3、将向量化数据进行存储
     *
     * 4、接收用户输入
     *
     * 5、根据用户问题，进行增强检索
     */
    @Autowired
    private PdfMultimodalProcessor pdfMultimodalProcessor;

    @Autowired
    private EmbeddingService embeddingService;


    @RequestMapping("/processFile")
    public String processFile(String filePath) throws Exception {
        //这里拿到的是，整个PDF的文字以及图片的<image>描述的拼接
        String processPdf = pdfMultimodalProcessor.processPdf(new File(filePath));

        //将整页文字进行切片
        ModalTextSplitter modalTextSplitter = new ModalTextSplitter(300, 20);
        List<Document> documents = modalTextSplitter.split(new Document(processPdf));

        embeddingService.embeddingAndStore(documents);

        return "处理完成";
    }

    @RequestMapping("chat")
    public String chat(String question) {

        //接收用户输入,查询向量数据库检索出相关文档
        List<Document> documents = embeddingService.similaritySearch(SearchRequest.builder().query(question).similarityThreshold(0.3).topK(5).build());

        //查询出相关文档，后构建Template，并交给大模型进行回答
        PromptTemplate promptTemplate = new PromptTemplate("""
                ## 角色定位
                你是一位专业的RAG问答助手。请根据提供的上下文信息，详细、准确地回答用户的问题。如果参考文档没有内容，请务必不要胡编乱造，请直接说明"没有找到相关信息"。
                
                ## 任务要求：
                1. 请基于以下提供的参考文档内容，回答用户的问题。
                2. 如果参考文档中没有相关信息，请直接说明"没有找到相关信息"，不要编造内容。
                3. 如果有了参考文档内容，请务必尽量回答问题。有可能用户的输入比较随意，你可以先尝试回答用户的问题，猜测他的实际需求，先给出回复，你需要尽量去贴合用户的问题需求。
                
                ## 格式要求：
                1. 你的所有回答必须使用Markdown格式进行排版。
                2. 上下文信息中包含了图片描述标签，格式为：`<image src="URL" description="多模态描述"></image>`。
                3. 如果图片与用户提问高度相关，请将此标签转换为标准的Markdown图片格式 `![图片](URL)`。
                4. 仅在必要时包含图片，请注意千万不要输出重复的内容和图片，图片确保最终生成的URL不要重复。
                
                ## 参考文档:
                {context}
                
                ## 用户问题:
                {query}
                
                注意：如果参考文档下面的内容为空，请直接回答“没有找到相关信息”。
                
                """);

        Prompt prompt = promptTemplate.create(Map.of("context", documents, "query", question));

        //返回结果
        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
