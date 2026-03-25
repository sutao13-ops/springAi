package com.example.springai.common.dto.prompt;

import com.example.springai.common.enums.ModelProviderEnum;

import java.util.Map;

/**
 * 使用提示词模板聊天请求
 *
 * @param templateId 模板ID
 * @param message    用户消息
 * @param variables  模板变量值
 * @param provider   模型提供商
 * @param sessionId  会话ID（可选，用于多轮对话）
 * @Author SuTao
 * @Date 2026/3/18
 */
public record ChatWithPromptRequest(
        String templateId,
        String message,
        Map<String, String> variables,
        ModelProviderEnum provider,
        String sessionId
) {
    public ChatWithPromptRequest {
        if (provider == null) {
            provider = ModelProviderEnum.DOUBAO;
        }
    }
}
