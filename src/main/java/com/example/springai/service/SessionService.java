package com.example.springai.service;

import com.example.springai.common.ChatResponse;
import com.example.springai.common.dto.session.ChatMessage;
import com.example.springai.common.dto.session.SessionChatRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 会话管理服务接口
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
public interface SessionService {

    /**
     * 创建新会话
     *
     * @return 会话ID
     */
    String createSession();

    /**
     * 发送消息（携带历史）
     *
     * @param sessionId 会话ID
     * @param request   聊天请求
     * @return 聊天响应
     */
    ChatResponse sendMessage(String sessionId, SessionChatRequest request);

    /**
     * 流式发送消息（携带历史）
     *
     * @param sessionId 会话ID
     * @param request   聊天请求
     * @return 流式响应
     */
    Flux<String> sendMessageStream(String sessionId, SessionChatRequest request);

    /**
     * 获取会话历史
     *
     * @param sessionId 会话ID
     * @return 消息历史列表
     */
    List<ChatMessage> getHistory(String sessionId);

    /**
     * 清除会话
     *
     * @param sessionId 会话ID
     * @return 是否成功
     */
    boolean deleteSession(String sessionId);

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 是否存在
     */
    boolean existsSession(String sessionId);
}
