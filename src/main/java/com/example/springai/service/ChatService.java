package com.example.springai.service;

import com.example.springai.common.ChatRequest;
import com.example.springai.common.ChatResponse;
import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * 统一聊天接口
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 统一流式聊天接口
     */
    Flux<String> streamChat(ChatRequest request);

    /**
     * 视觉识别接口
     */
    ChatResponse vision(String imageUrl, String prompt);
}
