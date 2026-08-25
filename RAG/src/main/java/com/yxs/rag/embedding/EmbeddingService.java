package com.yxs.rag.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
public class EmbeddingService {

    @Autowired
    private EmbeddingModel embeddingModel;

    public float[] getEmbedding(String text) {
        return embeddingModel.embed(text);
    }
}
