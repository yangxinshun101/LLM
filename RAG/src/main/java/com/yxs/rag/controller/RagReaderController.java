package com.yxs.rag.controller;

import com.yxs.rag.reader.DocumentReaderFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ragReader")
public class RagReaderController {

    @Autowired
    private DocumentReaderFactory documentReaderFactory;

    @RequestMapping("/read")
    public String read(String filePath){
        StringBuffer buffer = new StringBuffer();

        try {
            List<Document> read = documentReaderFactory.read(new File(filePath));
            read.forEach(document -> buffer.append(document.getText())
                    .append("=============\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(buffer.toString());

        return buffer.toString();
    }
}
