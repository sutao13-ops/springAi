package com.example.springai.common.dto;

import com.example.springai.common.enums.ModelProviderEnum;

/**
 * 会话聊天请求
 *
 * @param message  用户消息
 * @param provider 模型提供商（可选，默认 DOUBAO）
 * @param stream   是否流式返回（可选，默认 false）
 * @Author SuTao
 * @Date 2026/3/17
 */
public record SessionChatRequest(
        String message,
        ModelProviderEnum provider,
        Boolean stream
) {
    /**
     * 紧凑构造器：设置默认值
     */
    public SessionChatRequest {
        if (provider == null) {
            provider = ModelProviderEnum.DOUBAO;
        }
        if (stream == null) {
            stream = false;
        }
    }

    /**
     * 简化创建方法
     */
    public static SessionChatRequest of(String message) {
        return new SessionChatRequest(message, null, null);
    }

    /**
     * 指定模型创建
     */
    public static SessionChatRequest of(String message, ModelProviderEnum provider) {
        return new SessionChatRequest(message, provider, false);
    }
}
