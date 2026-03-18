package com.example.springai.common.dto;

import java.time.Instant;

/**
 * 聊天消息（用于会话历史）
 *
 * @param role      角色
 * @param content   消息内容
 * @param timestamp 时间戳
 * @Author SuTao
 * @Date 2026/3/17
 */
public record ChatMessage(
        String role,
        String content,
        long timestamp
) {
    public static ChatMessage user(String content) {
        return new ChatMessage("user", content, Instant.now().toEpochMilli());
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content, Instant.now().toEpochMilli());
    }

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content, Instant.now().toEpochMilli());
    }
}
