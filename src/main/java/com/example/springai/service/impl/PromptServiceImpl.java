package com.example.springai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springai.common.dto.prompt.PromptSaveRequest;
import com.example.springai.common.dto.prompt.PromptTemplate;
import com.example.springai.entity.PromptTemplateEntity;
import com.example.springai.mapper.PromptTemplateMapper;
import com.example.springai.service.PromptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 提示词模板服务实现
 * <p>
 * 使用 PostgreSQL + MyBatis-Plus 存储模板数据
 * </p>
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    private final PromptTemplateMapper promptTemplateMapper;

    @Override
    public Page<PromptTemplate> listPage(int pageNum, int pageSize) {
        Page<PromptTemplateEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PromptTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PromptTemplateEntity::getIsDefault)
                .orderByDesc(PromptTemplateEntity::getCreatedAt);

        Page<PromptTemplateEntity> entityPage = promptTemplateMapper.selectPage(page, wrapper);

        Page<PromptTemplate> resultPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        resultPage.setRecords(entityPage.getRecords().stream()
                .map(PromptTemplate::fromEntity)
                .collect(Collectors.toList()));

        return resultPage;
    }

    @Override
    public List<PromptTemplate> listAll() {
        LambdaQueryWrapper<PromptTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PromptTemplateEntity::getIsDefault)
                .orderByDesc(PromptTemplateEntity::getUpdatedAt);

        return promptTemplateMapper.selectList(wrapper).stream()
                .map(PromptTemplate::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromptTemplate> listByCategory(String category) {
        LambdaQueryWrapper<PromptTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplateEntity::getCategory, category)
                .orderByDesc(PromptTemplateEntity::getUpdatedAt);

        return promptTemplateMapper.selectList(wrapper).stream()
                .map(PromptTemplate::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromptTemplate> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return listAll();
        }

        LambdaQueryWrapper<PromptTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(PromptTemplateEntity::getName, keyword)
                .or()
                .like(PromptTemplateEntity::getDescription, keyword)
                .orderByDesc(PromptTemplateEntity::getUpdatedAt);

        return promptTemplateMapper.selectList(wrapper).stream()
                .map(PromptTemplate::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PromptTemplate getById(String id) {
        PromptTemplateEntity entity = promptTemplateMapper.selectById(id);
        return PromptTemplate.fromEntity(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromptTemplate save(PromptSaveRequest request) {
        PromptTemplateEntity entity;
        LocalDateTime now = LocalDateTime.now();

        if (request.isUpdate()) {
            // 更新
            entity = promptTemplateMapper.selectById(request.id());
            if (entity == null) {
                throw new IllegalArgumentException("模板不存在: " + request.id());
            }

            entity.setName(request.name() != null ? request.name() : entity.getName())
                    .setDescription(request.description() != null ? request.description() : entity.getDescription())
                    .setCategory(request.category() != null ? request.category() : entity.getCategory())
                    .setSystemPrompt(request.systemPrompt() != null ? request.systemPrompt() : entity.getSystemPrompt())
                    .setUserPromptTemplate(request.userPromptTemplate() != null ? request.userPromptTemplate() : entity.getUserPromptTemplate())
                    .setVariables(request.variables() != null ? request.variables() : entity.getVariables())
                    .setExamples(request.examples() != null ? request.examples() : entity.getExamples())
                    .setTags(request.tags() != null ? request.tags() : entity.getTags())
                    .setUpdatedAt(now);

            promptTemplateMapper.updateById(entity);
        } else {
            // 新建
            entity = new PromptTemplateEntity()
                    .setName(request.name())
                    .setDescription(request.description())
                    .setCategory(request.category())
                    .setSystemPrompt(request.systemPrompt())
                    .setUserPromptTemplate(request.userPromptTemplate())
                    .setVariables(request.variables() != null ? request.variables() : List.of())
                    .setExamples(request.examples() != null ? request.examples() : List.of())
                    .setTags(request.tags() != null ? request.tags() : List.of())
                    .setIsDefault(false)
                    .setUseCount(0)
                    .setCreatedAt(now)
                    .setUpdatedAt(now);

            promptTemplateMapper.insert(entity);
        }

        log.info("保存提示词模板: {}", entity.getId());
        return PromptTemplate.fromEntity(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String id) {
        PromptTemplateEntity entity = promptTemplateMapper.selectById(id);
        if (entity == null) {
            return false;
        }

        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            throw new IllegalStateException("不能删除默认模板");
        }

        int rows = promptTemplateMapper.deleteById(id);
        log.info("删除提示词模板: {}, 影响行数: {}", id, rows);
        return rows > 0;
    }

    @Override
    public String render(String templateId, Map<String, String> variables, String userMessage) {
        PromptTemplateEntity entity = promptTemplateMapper.selectById(templateId);
        if (entity == null) {
            throw new IllegalArgumentException("模板不存在: " + templateId);
        }

        // 增加使用次数
        promptTemplateMapper.incrementUseCount(templateId);

        StringBuilder result = new StringBuilder();

        // 添加系统提示词
        if (entity.getSystemPrompt() != null && !entity.getSystemPrompt().isBlank()) {
            result.append("【系统指令】\n");
            result.append(replaceVariables(entity.getSystemPrompt(), variables));
            result.append("\n\n");
        }

        // 添加用户提示词模板
        if (entity.getUserPromptTemplate() != null && !entity.getUserPromptTemplate().isBlank()) {
            result.append(replaceVariables(entity.getUserPromptTemplate(), variables));
            result.append("\n\n");
        }

        // 添加用户消息
        if (userMessage != null && !userMessage.isBlank()) {
            result.append("【用户输入】\n");
            result.append(userMessage);
        }

        return result.toString();
    }

    @Override
    public Map<String, String> preview(String templateId, Map<String, String> variables, String userMessage) {
        PromptTemplateEntity entity = promptTemplateMapper.selectById(templateId);
        if (entity == null) {
            throw new IllegalArgumentException("模板不存在: " + templateId);
        }

        Map<String, String> result = new LinkedHashMap<>();
        result.put("templateId", templateId);
        result.put("templateName", entity.getName());
        result.put("systemPrompt", replaceVariables(entity.getSystemPrompt(), variables));
        result.put("userPromptTemplate", replaceVariables(entity.getUserPromptTemplate(), variables));
        result.put("userMessage", userMessage);
        result.put("fullPrompt", render(templateId, variables, userMessage));

        return result;
    }

    @Override
    public PromptTemplate getDefault() {
        LambdaQueryWrapper<PromptTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplateEntity::getIsDefault, true);

        PromptTemplateEntity entity = promptTemplateMapper.selectOne(wrapper);
        return PromptTemplate.fromEntity(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefault(String id) {
        PromptTemplateEntity entity = promptTemplateMapper.selectById(id);
        if (entity == null) {
            return false;
        }

        // 清除所有默认标记
        promptTemplateMapper.clearAllDefault();

        // 设置新的默认模板
        entity.setIsDefault(true);
        entity.setUpdatedAt(LocalDateTime.now());
        promptTemplateMapper.updateById(entity);

        log.info("设置默认模板: {}", id);
        return true;
    }

    @Override
    public List<PromptTemplate> listHot(int limit) {
        LambdaQueryWrapper<PromptTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PromptTemplateEntity::getUseCount)
                .last("LIMIT " + limit);

        return promptTemplateMapper.selectList(wrapper).stream()
                .map(PromptTemplate::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listCategories() {
        LambdaQueryWrapper<PromptTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(PromptTemplateEntity::getCategory)
                .isNotNull(PromptTemplateEntity::getCategory)
                .groupBy(PromptTemplateEntity::getCategory);

        return promptTemplateMapper.selectList(wrapper).stream()
                .map(PromptTemplateEntity::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    /**
     * 替换模板中的变量
     */
    private String replaceVariables(String template, Map<String, String> variables) {
        if (template == null || template.isBlank()) {
            return "";
        }

        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String value = variables.getOrDefault(variableName, "");
            result = result.replace("{{" + variableName + "}}", value);
        }

        return result;
    }
}
