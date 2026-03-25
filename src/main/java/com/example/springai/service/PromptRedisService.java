package com.example.springai.service;

import com.example.springai.common.dto.prompt.PromptSaveRequest;
import com.example.springai.common.dto.prompt.PromptTemplate;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板服务接口
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
public interface PromptRedisService {

    /**
     * 获取所有模板列表
     *
     * @return 模板列表
     */
    List<PromptTemplate> listAll();

    /**
     * 按分类获取模板列表
     *
     * @param category 分类
     * @return 模板列表
     */
    List<PromptTemplate> listByCategory(String category);

    /**
     * 搜索模板
     *
     * @param keyword 关键词
     * @return 模板列表
     */
    List<PromptTemplate> search(String keyword);

    /**
     * 获取单个模板
     *
     * @param id 模板ID
     * @return 模板详情
     */
    PromptTemplate getById(String id);

    /**
     * 保存模板（新建或更新）
     *
     * @param request 保存请求
     * @return 保存后的模板
     */
    PromptTemplate save(PromptSaveRequest request);

    /**
     * 删除模板
     *
     * @param id 模板ID
     * @return 是否成功
     */
    boolean delete(String id);

    /**
     * 渲染提示词模板
     * <p>
     * 将变量值替换到模板中
     * </p>
     *
     * @param templateId 模板ID
     * @param variables  变量值
     * @param userMessage 用户消息
     * @return 渲染后的完整提示词
     */
    String render(String templateId, Map<String, String> variables, String userMessage);

    /**
     * 预览渲染结果
     *
     * @param templateId 模板ID
     * @param variables  变量值
     * @param userMessage 用户消息
     * @return 预览结果（包含 systemPrompt 和 userPrompt）
     */
    Map<String, String> preview(String templateId, Map<String, String> variables, String userMessage);

    /**
     * 获取默认模板
     *
     * @return 默认模板
     */
    PromptTemplate getDefault();

    /**
     * 设置默认模板
     *
     * @param id 模板ID
     * @return 是否成功
     */
    boolean setDefault(String id);
}
