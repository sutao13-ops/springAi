package com.example.springai.common;

import com.example.springai.common.enums.ModelProviderEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author SuTao
 * @Date 2026/3/16 19:10
 */
public record ChatRequest(
        // 组件（不可变的私有 final 字段）
        @NotNull(message = "provider 不能为空") ModelProviderEnum provider, // 模型提供商（枚举）
        @NotBlank(message = "message 不能为空") String message, // 聊天消息（核心）
        String imageUrl,             // 图片URL（视觉请求用）
        String prompt,               // 提示词
        boolean stream               // 是否流式返回结果
) {
    // 1. 紧凑构造器（Compact Constructor）：重写默认构造逻辑，做参数校验/默认值
    public ChatRequest {
        // provider 为空时，默认设为 OLLAMA
        if (provider == null) {
            provider = ModelProviderEnum.OLLAMA;
        }
        // message 为空/空白时抛异常，保证核心参数合法
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("message 不能为空");
        }
    }

    // 2. 静态工厂方法：简化对象创建（语义化，替代重载构造器）
    // 场景1：创建普通聊天请求（无图片、无自定义prompt、非流式）
    public static ChatRequest of(ModelProviderEnum provider, String message) {
        return new ChatRequest(provider, message, null, null, false);
    }

    // 场景2：创建普通聊天请求（支持流式返回）
    public static ChatRequest of(ModelProviderEnum provider, String message, boolean stream) {
        return new ChatRequest(provider, message, null, null, stream);
    }

    // 场景3：创建视觉请求（图片+prompt，固定使用豆包模型，非流式）
    public static ChatRequest ofVision(String imageUrl, String prompt) {
        return new ChatRequest(ModelProviderEnum.DOUBAO, null, imageUrl, prompt, false);
    }
}