package com.example.springai.controller;

import com.example.springai.common.ApiResponse;
import com.example.springai.common.ChatRequest;
import com.example.springai.common.ChatResponse;
import com.example.springai.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 统一聊天接口
     *
     * 示例请求:
     * {
     *   "provider": "DOUBAO",
     *   "message": "你好",
     *   "stream": false
     * }
     */
    @PostMapping("/chat")
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ApiResponse.success(chatService.chat(request));
    }

    /**
     * 统一流式聊天接口
     */
    @PostMapping(value = "/doubao/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest request) {
        return chatService.streamChat(request);
    }

    /**
     * 视觉识别接口
     */
    @PostMapping("/vision")
    public ApiResponse<ChatResponse> vision(@RequestParam("imageUrl") String imageUrl, @RequestParam("prompt") String prompt) {
        return ApiResponse.success(chatService.vision(imageUrl, prompt));
    }
}
