package com.yxs.rag.embedding;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmbeddingService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorStore vectorStore;

    private final int maxDocumentBatchSize = 9;

    public List<float[]> getEmbedding(List<Document> documents) {
        return documents.stream().map(document -> embeddingModel.embed(document)).collect(Collectors.toList());
    }

    public void embeddingAndStore(List<Document> documents){

        for (int i = 0; i < documents.size(); i += this.maxDocumentBatchSize) {
            List<Document> batches =   documents.subList(i, Math.min(i + this.maxDocumentBatchSize, documents.size()));
            vectorStore.add(batches);
        }

    }
}
