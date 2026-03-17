package com.example.springai.controller;

import com.example.springai.common.ApiResponse;
import com.example.springai.service.ChatService;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public ApiResponse<String> chat(@RequestParam(value = "message", defaultValue = "介绍一下自己") String message) {
        return ApiResponse.success(chatService.chat(message));
    }

    @GetMapping("/doubao/chat")
    public ApiResponse<ResponseObject> doubaoChat(@RequestParam(value = "message", defaultValue = "介绍一下自己") String message) {
        return ApiResponse.success(chatService.doubaoChat(message));
    }


    @GetMapping(value = "/doubao/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doubaoStreamChat(@RequestParam(value = "message", defaultValue = "介绍一下自己") String message) {
        return chatService.doubaoStreamChat(message);
    }

    @GetMapping("/doubao/vision")
    public ApiResponse<String> doubaoVision(@RequestParam("imageUrl") String imageUrl, @RequestParam("prompt") String prompt) {
        return ApiResponse.success(chatService.doubaoVision(imageUrl, prompt));
    }

}


