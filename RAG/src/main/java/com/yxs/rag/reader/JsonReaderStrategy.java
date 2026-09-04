package com.yxs.rag.reader;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class JsonReaderStrategy implements DocumentReaderStrategy {
    @Override
    public boolean supports(File file) {

        String name = file.getName().toLowerCase();
        return name.endsWith(".json");
    }

    /**
     * 这个JSON的读取器，就只有一个作用，将目标的JSON文件读取成Document对象，可配置的内容为jsonKeysToUse，在传入Resource的时候，传入可变参的String，即可配置需要读取的JSON字段
     * 其主要作用为读物目标的所有json内容。
     * @param file
     * @return
     * @throws IOException
     */
    @Override
    public List<Document> read(File file) throws IOException {
        Resource resource = new FileSystemResource(file);
        JsonReader jsonReader = new JsonReader(resource);
        return jsonReader.get();
    }
}
