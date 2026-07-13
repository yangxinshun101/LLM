package com.yxs.springai.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/Stream")
public class HttpClientController {


    @GetMapping("/Sse")
    public SseEmitter Sse() {
        SseEmitter emitter = new SseEmitter(6000L);

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {

            try {
                for (int i = 0; i < 1000; i++) {
                    emitter.send("测试流式发送" + i);
                    Thread.sleep(500);
                }
            } catch (IOException e) {
                emitter.completeWithError(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                emitter.complete();
            }

        });
        return emitter;
    }

    @GetMapping("/entity")
    public ResponseEntity<StreamingResponseBody> chat() {
        StreamingResponseBody body = outputStream -> {
            for (int i = 0; i < 1000; i++) {
                String data = "测试流式发送" + i + "\n";
                outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .body(body);
    }


}
