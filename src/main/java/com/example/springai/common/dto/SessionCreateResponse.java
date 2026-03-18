package com.example.springai.common.dto;

/**
 * 会话创建响应
 *
 * @param sessionId 会话ID
 * @param createdAt 创建时间
 * @param message   提示信息
 * @Author SuTao
 * @Date 2026/3/17
 */
public record SessionCreateResponse(
        String sessionId,
        long createdAt,
        String message
) {
    public static SessionCreateResponse of(String sessionId) {
        return new SessionCreateResponse(
                sessionId,
                System.currentTimeMillis(),
                "会话创建成功"
        );
    }
}
