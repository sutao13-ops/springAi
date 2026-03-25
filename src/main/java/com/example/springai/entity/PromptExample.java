package com.example.springai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 提示词使用示例
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptExample implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 输入示例
     */
    private String input;

    /**
     * 输出示例
     */
    private String output;

    /**
     * 变量值示例
     */
    private Map<String, String> variables;

    /**
     * 创建简单示例
     */
    public static PromptExample of(String input, String output) {
        return new PromptExample(input, output, Map.of());
    }

    /**
     * 创建带变量的示例
     */
    public static PromptExample withVariables(String input, String output, Map<String, String> variables) {
        return new PromptExample(input, output, variables);
    }
}
