package com.example.springai.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提示词模板实体
 * <p>
 * 对应数据库表：prompt_template
 * </p>
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
@Data
@Accessors(chain = true)
@TableName(value = "prompt_template", autoResultMap = true)
public class PromptTemplateEntity {

    /**
     * 模板ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 分类（如：assistant, code, writing, translation）
     */
    private String category;

    /**
     * 系统提示词（角色设定）
     */
    private String systemPrompt;

    /**
     * 用户提示词模板（支持变量：{{variable}}）
     */
    private String userPromptTemplate;

    /**
     * 变量列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<PromptVariable> variables;

    /**
     * 使用示例（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<PromptExample> examples;

    /**
     * 标签（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 是否默认模板
     */
    private Boolean isDefault;

    /**
     * 使用次数
     */
    private Integer useCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
