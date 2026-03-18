package com.example.springai.controller;

import com.example.springai.common.ApiResponse;
import com.example.springai.common.ChatResponse;
import com.example.springai.common.dto.ChatMessage;
import com.example.springai.common.dto.SessionChatRequest;
import com.example.springai.common.dto.SessionCreateResponse;
import com.example.springai.service.SessionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 会话管理控制器
 * <p>
 * 提供多轮对话会话的创建、消息发送、历史查询、会话删除等功能
 * </p>
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
@RestController
@RequestMapping("/chat/session")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * 创建新会话
     * <p>
     * 返回唯一的 sessionId，用于后续的消息发送和历史管理
     * </p>
     *
     * @return 会话创建响应
     */
    @PostMapping("/create")
    public ApiResponse<SessionCreateResponse> createSession() {
        String sessionId = sessionService.createSession();
        return ApiResponse.success(SessionCreateResponse.of(sessionId));
    }

    /**
     * 发送消息（携带历史上下文）
     * <p>
     * 自动维护对话历史，支持多轮对话
     * </p>
     *
     * @param sessionId 会话ID
     * @param request   聊天请求
     * @return 聊天响应
     */
    @PostMapping("/{id}/send")
    public ApiResponse<ChatResponse> sendMessage(@PathVariable("id") String sessionId, @RequestBody SessionChatRequest request) {
        ChatResponse response = sessionService.sendMessage(sessionId, request);
        return ApiResponse.success(response);
    }

    /**
     * 流式发送消息（携带历史上下文）
     *
     * @param sessionId 会话ID
     * @param request   聊天请求
     * @return 流式响应
     */
    @PostMapping(value = "/{id}/send/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessageStream(@PathVariable("id") String sessionId, @RequestBody SessionChatRequest request) {
        return sessionService.sendMessageStream(sessionId, request);
    }

    /**
     * 获取会话历史
     *
     * @param sessionId 会话ID
     * @return 消息历史列表
     */
    @GetMapping("/{id}/history")
    public ApiResponse<List<ChatMessage>> getHistory(@PathVariable("id") String sessionId) {
        List<ChatMessage> history = sessionService.getHistory(sessionId);
        return ApiResponse.success(history);
    }

    /**
     * 删除会话
     * <p>
     * 清除会话及其历史消息
     * </p>
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteSession(@PathVariable("id") String sessionId) {
        boolean result = sessionService.deleteSession(sessionId);
        return ApiResponse.success(result);
    }

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 是否存在
     */
    @GetMapping("/{id}/exists")
    public ApiResponse<Boolean> existsSession(@PathVariable("id") String sessionId) {
        boolean exists = sessionService.existsSession(sessionId);
        return ApiResponse.success(exists);
    }
}
