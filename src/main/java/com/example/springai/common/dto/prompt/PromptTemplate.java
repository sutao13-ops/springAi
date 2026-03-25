package com.example.springai.common.dto.prompt;

import com.example.springai.entity.PromptExample;
import com.example.springai.entity.PromptVariable;
import com.example.springai.entity.PromptTemplateEntity;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * 提示词模板
 * <p>
 * 用于管理和复用系统提示词、用户提示词模板
 * </p>
 *
 * @param id                 模板ID
 * @param name               模板名称
 * @param description        模板描述
 * @param category           分类（如：assistant、code、writing、translation）
 * @param systemPrompt       系统提示词（角色设定）
 * @param userPromptTemplate 用户提示词模板（支持变量：{{variable}}）
 * @param variables          变量列表
 * @param examples           使用示例
 * @param tags               标签
 * @param isDefault          是否默认模板
 * @param createdAt          创建时间
 * @param updatedAt          更新时间
 * @Author SuTao
 * @Date 2026/3/18
 */
public record PromptTemplate(
        String id,
        String name,
        String description,
        String category,
        String systemPrompt,
        String userPromptTemplate,
        List<PromptVariable> variables,
        List<PromptExample> examples,
        List<String> tags,
        boolean isDefault,
        long createdAt,
        long updatedAt
) {
    /**
     * 创建新模板（简化版）
     */
    public static PromptTemplate of(String name, String systemPrompt) {
        long now = Instant.now().toEpochMilli();
        String id = "prompt_" + System.currentTimeMillis();
        return new PromptTemplate(
                id, name, null, null,
                systemPrompt, null,
                List.of(), List.of(), List.of(),
                false, now, now
        );
    }

    /**
     * 创建新模板（完整版）
     */
    public static PromptTemplate create(String name, String description, String category,
                                        String systemPrompt, String userPromptTemplate,
                                        List<PromptVariable> variables, List<String> tags) {
        long now = Instant.now().toEpochMilli();
        String id = "prompt_" + now + "_" + (int) (Math.random() * 10000);
        return new PromptTemplate(
                id, name, description, category,
                systemPrompt, userPromptTemplate,
                variables != null ? variables : List.of(),
                List.of(),
                tags != null ? tags : List.of(),
                false, now, now
        );
    }

    /**
     * 更新时间戳
     */
    public PromptTemplate withUpdatedTime() {
        return new PromptTemplate(
                id, name, description, category,
                systemPrompt, userPromptTemplate,
                variables, examples, tags,
                isDefault, createdAt, Instant.now().toEpochMilli()
        );
    }

    /**
     * 从数据库实体转换为 DTO
     */
    public static PromptTemplate fromEntity(PromptTemplateEntity entity) {
        if (entity == null) {
            return null;
        }
        long createdAt = entity.getCreatedAt() != null
                ? entity.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : 0L;
        long updatedAt = entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : 0L;
        return new PromptTemplate(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getSystemPrompt(),
                entity.getUserPromptTemplate(),
                entity.getVariables() != null ? entity.getVariables() : List.of(),
                entity.getExamples() != null ? entity.getExamples() : List.of(),
                entity.getTags() != null ? entity.getTags() : List.of(),
                Boolean.TRUE.equals(entity.getIsDefault()),
                createdAt,
                updatedAt
        );
    }
}
