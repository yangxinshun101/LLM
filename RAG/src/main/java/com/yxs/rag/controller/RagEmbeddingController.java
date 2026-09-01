package com.yxs.rag.controller;

import com.yxs.rag.cleaner.DocumentCleaner;
import com.yxs.rag.embedding.EmbeddingService;
import com.yxs.rag.reader.DocumentReaderFactory;
import com.yxs.rag.splitter.OverlapParagraphTextSplitter;
import org.mozilla.universalchardet.ReaderFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/embedding")
public class RagEmbeddingController {

    @Autowired
    private EmbeddingService EmbeddingService;
    @Autowired
    private DocumentReaderFactory documentReaderFactory;


    @RequestMapping("/test")
    public String test() {
        for (float[] i : EmbeddingService.getEmbedding(List.of(new Document("test")))) {
            System.out.println(i);
        }
        return "success";
    }

    @RequestMapping("/embeddingAndStore")
    public String embeddingAndStore(String text) {

        List<Document> read;
        try {
            read = documentReaderFactory.read(new File(text));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //数据清洗
        List<Document> documents = DocumentCleaner.cleanDocuments(read);

        //将数据进行切片
        List<Document> documentList = documents.stream().flatMap(doc -> {
            return new OverlapParagraphTextSplitter(258, 50).split(doc).stream();
        }).collect(Collectors.toList());

        //将切片的内容进行向量化存储
        EmbeddingService.embeddingAndStore(documentList);

        return "Success";
    }
}
