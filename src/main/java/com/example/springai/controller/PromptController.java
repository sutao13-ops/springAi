package com.example.springai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springai.common.ApiResponse;
import com.example.springai.common.dto.prompt.PromptSaveRequest;
import com.example.springai.common.dto.prompt.PromptTemplate;
import com.example.springai.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板管理控制器
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
@RestController
@RequestMapping("/prompt")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    /**
     * 分页获取模板列表
     */
    @GetMapping("/list")
    public ApiResponse<Page<PromptTemplate>> listPage(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(promptService.listPage(pageNum, pageSize));
    }

    /**
     * 获取所有模板列表
     */
    @GetMapping("/all")
    public ApiResponse<List<PromptTemplate>> listAll() {
        return ApiResponse.success(promptService.listAll());
    }

    /**
     * 按分类获取模板列表
     *
     * @param category 分类
     * @return 模板列表
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<PromptTemplate>> listByCategory(@PathVariable String category) {
        return ApiResponse.success(promptService.listByCategory(category));
    }

    /**
     * 搜索模板
     *
     * @param keyword 关键词
     * @return 模板列表
     */
    @GetMapping("/search")
    public ApiResponse<List<PromptTemplate>> search(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(promptService.search(keyword));
    }

    /**
     * 获取单个模板详情
     *
     * @param id 模板ID
     * @return 模板详情
     */
    @GetMapping("/{id}")
    public ApiResponse<PromptTemplate> getById(@PathVariable String id) {
        PromptTemplate template = promptService.getById(id);
        if (template == null) {
            return ApiResponse.fail("模板不存在");
        }
        return ApiResponse.success(template);
    }

    /**
     * 保存模板（新建或更新）
     *
     * @param request 保存请求
     * @return 保存后的模板
     */
    @PostMapping("/save")
    public ApiResponse<PromptTemplate> save(@RequestBody PromptSaveRequest request) {
        try {
            PromptTemplate template = promptService.save(request);
            return ApiResponse.success(template);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 更新模板
     *
     * @param id      模板ID
     * @param request 更新请求
     * @return 更新后的模板
     */
    @PutMapping("/{id}")
    public ApiResponse<PromptTemplate> update(@PathVariable String id, @RequestBody PromptSaveRequest request) {
        PromptSaveRequest updateRequest = new PromptSaveRequest(
                id, request.name(), request.description(), request.category(),
                request.systemPrompt(), request.userPromptTemplate(),
                request.variables(), request.examples(), request.tags()
        );
        try {
            PromptTemplate template = promptService.save(updateRequest);
            return ApiResponse.success(template);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 删除模板
     *
     * @param id 模板ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        try {
            boolean result = promptService.delete(id);
            return ApiResponse.success(result);
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 预览渲染结果
     * <p>
     * 展示变量替换后的完整提示词
     * </p>
     *
     * @param id          模板ID
     * @param variables   变量值
     * @param userMessage 用户消息
     * @return 预览结果
     */
    @PostMapping("/{id}/preview")
    public ApiResponse<Map<String, String>> preview(@PathVariable String id, @RequestParam(required = false) Map<String, String> variables, @RequestParam(required = false) String userMessage) {
        try {
            Map<String, String> preview = promptService.preview(id, variables, userMessage);
            return ApiResponse.success(preview);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 获取默认模板
     */
    @GetMapping("/default")
    public ApiResponse<PromptTemplate> getDefault() {
        PromptTemplate template = promptService.getDefault();
        if (template == null) {
            return ApiResponse.fail("没有默认模板");
        }
        return ApiResponse.success(template);
    }

    /**
     * 设置默认模板
     *
     * @param id 模板ID
     * @return 操作结果
     */
    @PostMapping("/{id}/default")
    public ApiResponse<Boolean> setDefault(@PathVariable String id) {
        boolean result = promptService.setDefault(id);
        return ApiResponse.success(result);
    }

    /**
     * 获取热门模板
     */
    @GetMapping("/hot")
    public ApiResponse<List<PromptTemplate>> listHot( @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(promptService.listHot(limit));
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ApiResponse<List<String>> listCategories() {
        return ApiResponse.success(promptService.listCategories());
    }
}
