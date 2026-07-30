package com.yxs.springai.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ollama")
public class OllamaChatModelController {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @RequestMapping("/chat")
    public Flux<String> chat(String message, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        return ollamaChatModel.stream(message);
    }
}
