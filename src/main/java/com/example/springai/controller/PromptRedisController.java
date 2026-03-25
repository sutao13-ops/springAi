package com.example.springai.controller;

import com.example.springai.common.ApiResponse;
import com.example.springai.common.dto.prompt.PromptSaveRequest;
import com.example.springai.common.dto.prompt.PromptTemplate;
import com.example.springai.service.PromptRedisService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板管理控制器
 * <p>
 * 提供模板的增删改查、预览、渲染等功能
 * </p>
 *
 * @Author SuTao
 * @Date 2026/3/18
 */
@RestController
@RequestMapping("/prompt/redis")
public class PromptRedisController {

    private final PromptRedisService promptRedisService;

    public PromptRedisController(PromptRedisService promptRedisService) {
        this.promptRedisService = promptRedisService;
    }

    /**
     * 获取所有模板列表
     *
     * @return 模板列表
     */
    @GetMapping("/list")
    public ApiResponse<List<PromptTemplate>> listAll() {
        return ApiResponse.success(promptRedisService.listAll());
    }

    /**
     * 按分类获取模板列表
     *
     * @param category 分类
     * @return 模板列表
     */
    @GetMapping("/list/{category}")
    public ApiResponse<List<PromptTemplate>> listByCategory(@PathVariable String category) {
        return ApiResponse.success(promptRedisService.listByCategory(category));
    }

    /**
     * 搜索模板
     *
     * @param keyword 关键词
     * @return 模板列表
     */
    @GetMapping("/search")
    public ApiResponse<List<PromptTemplate>> search(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(promptRedisService.search(keyword));
    }

    /**
     * 获取单个模板详情
     *
     * @param id 模板ID
     * @return 模板详情
     */
    @GetMapping("/{id}")
    public ApiResponse<PromptTemplate> getById(@PathVariable String id) {
        PromptTemplate template = promptRedisService.getById(id);
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
            PromptTemplate template = promptRedisService.save(request);
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
            PromptTemplate template = promptRedisService.save(updateRequest);
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
            boolean result = promptRedisService.delete(id);
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
    public ApiResponse<Map<String, String>> preview(
            @PathVariable String id,
            @RequestParam(required = false) Map<String, String> variables,
            @RequestParam(required = false) String userMessage) {
        try {
            Map<String, String> preview = promptRedisService.preview(id, variables, userMessage);
            return ApiResponse.success(preview);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 获取默认模板
     *
     * @return 默认模板
     */
    @GetMapping("/default")
    public ApiResponse<PromptTemplate> getDefault() {
        PromptTemplate template = promptRedisService.getDefault();
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
        boolean result = promptRedisService.setDefault(id);
        return ApiResponse.success(result);
    }
}
