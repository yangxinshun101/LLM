package com.yxs.rag.reader;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class MarkdownReaderStrategy implements DocumentReaderStrategy {
    @Override
    public boolean supports(File file) {

        String name = file.getName().toLowerCase();
        return name.endsWith(".md");
    }

    /**
     * 读取markdown文件，并返回一个Document对象。document的对象中包含文件的文本内容、元数据等信息。
     * 主要是配置看到底读什么内容，
     * 例如：是否包含代码块、引用、水平线分割的文档等。
     * 代码块是指在markdown文件中用```括起来的内容。
     * 引用是指在markdown文件中用>括起来的内容。
     * @param file
     * @return
     * @throws IOException
     */
    @Override
    public List<Document> read(File file) throws IOException {
        // 读取配置
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                // 水平线分割生成新文档
                .withHorizontalRuleCreateDocument(false)
                // 不包含代码块
                .withIncludeCodeBlock(false)
                // 不包含引用
                .withIncludeBlockquote(false)
                // 添加文件名元数据
                .withAdditionalMetadata("filename", file.getName())
                .build();
        Resource resource = new FileSystemResource(file);
        return new MarkdownDocumentReader(resource,config).get();
    }
}
