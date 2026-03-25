package com.example.springai.common.dto.prompt;

import com.example.springai.entity.PromptExample;
import com.example.springai.entity.PromptVariable;

import java.util.List;

/**
 * 提示词保存请求
 *
 * @Author SuTao
 * @Date 2026/3/18
 */
public record PromptSaveRequest(
        String id,                      // 有则更新，无则创建
        String name,
        String description,
        String category,
        String systemPrompt,
        String userPromptTemplate,
        List<PromptVariable> variables,
        List<PromptExample> examples,
        List<String> tags
) {
    /**
     * 是否为更新操作
     */
    public boolean isUpdate() {
        return id != null && !id.isBlank();
    }
}
