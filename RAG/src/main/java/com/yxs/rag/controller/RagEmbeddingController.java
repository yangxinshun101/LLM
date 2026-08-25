package com.yxs.rag.controller;

import com.yxs.rag.embedding.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/embedding")
public class RagEmbeddingController {

    @Autowired
    private EmbeddingService EmbeddingService;

    @RequestMapping("/getEmbedding")
    public float[] getEmbedding(String text) {
        return EmbeddingService.getEmbedding(text);
    }
}
