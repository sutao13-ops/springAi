package com.example.springai.service.impl;

import com.example.springai.common.ChatRequest;
import com.example.springai.common.ChatResponse;
import com.example.springai.common.dto.session.ChatMessage;
import com.example.springai.common.dto.session.SessionChatRequest;
import com.example.springai.service.ChatService;
import com.example.springai.service.SessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 会话管理服务实现
 * <p>
 * 使用 Redis 存储会话历史，支持多轮对话上下文保持
 * </p>
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
@Service
public class SessionServiceImpl implements SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    @Value("${session.redis.key-prefix:chat:session:}")
    private String keyPrefix;

    @Value("${session.redis.ttl:3600}")
    private long sessionTtl;

    public SessionServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, ChatService chatService) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.chatService = chatService;
    }

    @Override
    public String createSession() {
        String sessionId = generateSessionId();
        // 初始化空历史
        saveHistory(sessionId, new ArrayList<>());
        log.info("创建新会话: {}", sessionId);
        return sessionId;
    }

    @Override
    public ChatResponse sendMessage(String sessionId, SessionChatRequest request) {
        // 1. 验证会话
        if (!existsSession(sessionId)) {
            log.warn("会话不存在: {}", sessionId);
            return ChatResponse.error("会话不存在或已过期");
        }

        // 2. 获取历史消息
        List<ChatMessage> history = getHistory(sessionId);
        log.info("会话 {} 历史消息数: {}", sessionId, history.size());

        // 3. 构建完整消息（历史 + 当前）
        String contextMessage = buildContextMessage(history, request.message());

        // 4. 调用模型
        ChatResponse response = chatService.chat(
                new ChatRequest(
                        request.provider(),
                        contextMessage,
                        null,
                        null,
                        false
                )
        );

        // 5. 保存到历史
        if (response.error() == null) {
            history.add(ChatMessage.user(request.message()));
            history.add(ChatMessage.assistant(response.content()));
            saveHistory(sessionId, history);
            log.info("会话 {} 已更新历史，当前消息数: {}", sessionId, history.size());
        }

        return response;
    }

    @Override
    public Flux<String> sendMessageStream(String sessionId, SessionChatRequest request) {
        // 1. 验证会话
        if (!existsSession(sessionId)) {
            return Flux.error(new IllegalStateException("会话不存在或已过期"));
        }

        // 2. 获取历史消息
        List<ChatMessage> history = getHistory(sessionId);

        // 3. 构建完整消息
        String contextMessage = buildContextMessage(history, request.message());

        // 4. 流式调用
        StringBuilder fullResponse = new StringBuilder();

        return chatService.streamChat(
                        new ChatRequest(request.provider(),
                                contextMessage,
                                null,
                                null,
                                true
                        )
                ).doOnNext(fullResponse::append)  // 收集完整响应
                .doOnComplete(() -> {
                    // 5. 流式完成后保存历史
                    history.add(ChatMessage.user(request.message()));
                    history.add(ChatMessage.assistant(fullResponse.toString()));
                    saveHistory(sessionId, history);
                    log.info("会话 {} 流式响应完成，已保存历史", sessionId);
                });
    }

    @Override
    public List<ChatMessage> getHistory(String sessionId) {
        String key = getSessionKey(sessionId);
        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("解析会话历史失败: {}", sessionId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean deleteSession(String sessionId) {
        String key = getSessionKey(sessionId);
        Boolean deleted = redisTemplate.delete(key);
        log.info("删除会话: {}, 结果: {}", sessionId, deleted);
        return Boolean.TRUE.equals(deleted);
    }

    @Override
    public boolean existsSession(String sessionId) {
        String key = getSessionKey(sessionId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ==================== 私有方法 ====================

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取 Redis Key
     */
    private String getSessionKey(String sessionId) {
        return keyPrefix + sessionId;
    }

    /**
     * 保存历史到 Redis(自动续期)
     */
    private void saveHistory(String sessionId, List<ChatMessage> history) {
        String key = getSessionKey(sessionId);
        try {
            String json = objectMapper.writeValueAsString(history);
            // 每次保存都会重置 TTL
            redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(sessionTtl));
        } catch (JsonProcessingException e) {
            log.error("保存会话历史失败: {}", sessionId, e);
        }
    }

    /**
     * 构建带上下文的消息
     * <p>
     * 将历史消息与当前消息合并，形成完整的多轮对话上下文
     * </p>
     */
    private String buildContextMessage(List<ChatMessage> history, String currentMessage) {
        if (history.isEmpty()) {
            return currentMessage;
        }

        StringBuilder context = new StringBuilder();

        // 添加历史对话
        context.append("【历史对话】\n");
        for (ChatMessage msg : history) {
            String roleLabel = switch (msg.role()) {
                case "user" -> "用户";
                case "assistant" -> "AI";
                case "system" -> "系统";
                default -> msg.role();
            };
            context.append(roleLabel).append(": ").append(msg.content()).append("\n\n");
        }

        // 添加当前消息
        context.append("【当前问题】\n");
        context.append(currentMessage);

        return context.toString();
    }
}
