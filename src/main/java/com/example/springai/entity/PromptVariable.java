package com.example.springai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 提示词模板变量
 * <p>
 * 定义模板中可替换的变量，如：{{language}}、{{topic}}
 * </p>
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptVariable implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 变量名（如：language）
     */
    private String name;

    /**
     * 变量描述
     */
    private String description;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 创建普通变量
     */
    public static PromptVariable of(String name, String description) {
        return new PromptVariable(name, description, null, false);
    }

    /**
     * 创建必填变量
     */
    public static PromptVariable required(String name, String description) {
        return new PromptVariable(name, description, null, true);
    }

    /**
     * 创建带默认值的变量
     */
    public static PromptVariable withDefault(String name, String description, String defaultValue) {
        return new PromptVariable(name, description, defaultValue, false);
    }
}
