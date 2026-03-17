package com.example.springai.service;

import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import reactor.core.publisher.Flux;

public interface ChatService {

    String chat(String message);

    ResponseObject doubaoChat(String message);

    String doubaoVision(String imageUrl, String prompt);

    // 新增：豆包流式对话，返回 Flux<String> 逐块推送
    Flux<String> doubaoStreamChat(String message);
}
