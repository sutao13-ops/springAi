package com.example.springai.service;

import com.volcengine.ark.runtime.model.responses.response.ResponseObject;

public interface ChatService {

    String chat(String message);

    ResponseObject doubaoChat(String message);

    String doubaoVision(String imageUrl, String prompt);
}
