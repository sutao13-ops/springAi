package com.example.springai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public interface PromptService {

    /**
     * 分页获取模板列表
     */
    Page<PromptTemplate> listPage(int pageNum, int pageSize);

    /**
     * 获取所有模板列表
     */
    List<PromptTemplate> listAll();

    /**
     * 按分类获取模板列表
     */
    List<PromptTemplate> listByCategory(String category);

    /**
     * 搜索模板
     */
    List<PromptTemplate> search(String keyword);

    /**
     * 获取单个模板
     */
    PromptTemplate getById(String id);

    /**
     * 保存模板（新建或更新）
     */
    PromptTemplate save(PromptSaveRequest request);

    /**
     * 删除模板
     */
    boolean delete(String id);

    /**
     * 渲染提示词模板
     */
    String render(String templateId, Map<String, String> variables, String userMessage);

    /**
     * 预览渲染结果
     */
    Map<String, String> preview(String templateId, Map<String, String> variables, String userMessage);

    /**
     * 获取默认模板
     */
    PromptTemplate getDefault();

    /**
     * 设置默认模板
     */
    boolean setDefault(String id);

    /**
     * 获取热门模板
     */
    List<PromptTemplate> listHot(int limit);

    /**
     * 获取所有分类
     */
    List<String> listCategories();
}
