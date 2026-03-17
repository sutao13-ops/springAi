package com.example.springai.common;

import java.util.List;

/**
 * 统一聊天响应 DTO
 * <p>
 * 用于封装不同模型提供商（Ollama、豆包、OpenAI等）的响应结果，
 * 实现控制器层与具体 SDK 的解耦
 * </p>
 *
 * @Author SuTao
 * @Date 2026/3/16 19:13
 */
public record ChatResponse(
        // 聊天回复的主要内容
        String content,

        // 使用的模型名称（如：doubao-seed-2-0-lite-260215、gpt-oss:20b）
        String model,

        // 模型提供商（如：doubao、ollama、openai）
        String provider,

        // Token 使用量（输入 + 输出总 token 数）
        long tokensUsed,

        // 候选回复列表（通常只包含一个元素，即 content）
        List<String> choices,

        // 错误信息（正常响应时为 null）
        String error,

        // 推理过程（模型思考过程，仅部分模型如豆包支持）
        String reasoning
) {
    /**
     * 创建基础成功响应（无 token 使用量、无推理过程）
     *
     * @param content  回复内容
     * @param model    模型名称
     * @param provider 提供商代码
     * @return ChatResponse 实例
     */
    public static ChatResponse success(String content, String model, String provider) {
        return new ChatResponse(content, model, provider, 0, List.of(content), null, null);
    }

    /**
     * 创建带 token 使用量的成功响应（无推理过程）
     *
     * @param content     回复内容
     * @param model       模型名称
     * @param provider    提供商代码
     * @param tokensUsed  Token 使用量
     * @return ChatResponse 实例
     */
    public static ChatResponse success(String content, String model, String provider, long tokensUsed) {
        return new ChatResponse(content, model, provider, tokensUsed, List.of(content), null, null);
    }

    /**
     * 创建带推理过程的成功响应（无 token 使用量）
     *
     * @param content   回复内容
     * @param model     模型名称
     * @param provider  提供商代码
     * @param reasoning 推理过程（模型思考过程）
     * @return ChatResponse 实例
     */
    public static ChatResponse success(String content, String model, String provider, String reasoning) {
        return new ChatResponse(content, model, provider, 0, List.of(content), null, reasoning);
    }

    /**
     * 创建完整的成功响应（包含推理过程和 token 使用量）
     *
     * @param content     回复内容
     * @param model       模型名称
     * @param provider    提供商代码
     * @param tokensUsed  Token 使用量
     * @param reasoning   推理过程（模型思考过程）
     * @return ChatResponse 实例
     */
    public static ChatResponse success(String content, String model, String provider, long tokensUsed, String reasoning) {
        return new ChatResponse(content, model, provider, tokensUsed, List.of(content), null, reasoning);
    }

    /**
     * 创建错误响应
     *
     * @param error 错误信息
     * @return ChatResponse 实例（仅包含 error 字段）
     */
    public static ChatResponse error(String error) {
        return new ChatResponse(null, null, null, 0, null, error, null);
    }
}
