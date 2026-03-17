package com.example.springai.common.enums;

import lombok.Getter;

/**
 * @Author SuTao
 * @Date 2026/3/16 19:06
 */
@Getter
public enum ModelProviderEnum {
    OLLAMA("ollama", "Ollama 本地模型"),
    DOUBAO("doubao", "豆包大模型"),
    OPENAI("openai", "OpenAI GPT");

    private final String code;
    private final String description;

    ModelProviderEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ModelProviderEnum fromCode(String code) {
        for (ModelProviderEnum provider : values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("不支持的模型提供商: " + code);
    }
}
