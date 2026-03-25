package com.example.springai.service.impl;

import com.example.springai.common.dto.prompt.PromptSaveRequest;
import com.example.springai.common.dto.prompt.PromptTemplate;
import com.example.springai.entity.PromptVariable;
import com.example.springai.service.PromptRedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 提示词模板服务实现
 * <p>
 * 使用 Redis 存储模板数据，支持变量的动态替换
 * </p>
 *
 * @Author SuTao
 * @Date 2026/3/18
 */
@Service
public class PromptRedisServiceImpl implements PromptRedisService {

    private static final Logger log = LoggerFactory.getLogger(PromptRedisServiceImpl.class);

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${prompt.redis.key-prefix:prompt:template:}")
    private String keyPrefix;

    @Value("${prompt.redis.list-key:prompt:list}")
    private String listKey;

    private volatile boolean initialized = false;  // 可选：防止重复初始化

    public PromptRedisServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        // 初始化默认模板
//        initDefaultTemplates();
    }

    @PostConstruct
    public void init() {
        initDefaultTemplates();
    }

    private void initDefaultTemplates() {
        // 检查是否已有模板
        if (!listAll().isEmpty()) {
            return;
        }

        // 初始化一些默认模板
        List<PromptTemplate> defaultTemplates = List.of(
                // 代码助手
                PromptTemplate.create(
                        "代码助手",
                        "专业的编程助手，精通多种编程语言",
                        "assistant",
                        "你是一个专业的编程助手，精通 Java、Python、JavaScript、Go 等多种编程语言。" +
                                "你会提供清晰、高效、可维护的代码示例，并解释代码的工作原理。" +
                                "回答时请使用 Markdown 格式，代码块标注语言类型。",
                        null,
                        List.of(
                                PromptVariable.of("language", "编程语言"),
                                PromptVariable.of("task", "具体任务")
                        ),
                        List.of("代码", "编程", "开发")
                ),

                // 翻译助手
                PromptTemplate.create(
                        "翻译助手",
                        "专业的多语言翻译助手",
                        "translation",
                        "你是一个专业的翻译助手，精通中文、英文、日文、韩文等多种语言。" +
                                "翻译时请保持原文的语气和风格，并提供翻译说明。" +
                                "格式：\n【原文】\n【译文】\n【说明】",
                        "请将以下{{source_lang}}文本翻译成{{target_lang}}：\n{{text}}",
                        List.of(
                                PromptVariable.required("source_lang", "源语言"),
                                PromptVariable.required("target_lang", "目标语言"),
                                PromptVariable.required("text", "待翻译文本")
                        ),
                        List.of("翻译", "语言")
                ),

                // 写作助手
                PromptTemplate.create(
                        "写作助手",
                        "专业的文案写作助手",
                        "writing",
                        "你是一个专业的写作助手，擅长各种类型的文案创作。" +
                                "你的文字优美流畅，富有感染力，能够根据需求调整写作风格。",
                        "请帮我写一篇关于{{topic}}的{{style}}文章，字数约{{word_count}}字。",
                        List.of(
                                PromptVariable.required("topic", "主题"),
                                PromptVariable.withDefault("style", "风格", "通俗易懂"),
                                PromptVariable.withDefault("word_count", "字数", "500")
                        ),
                        List.of("写作", "文案", "创作")
                ),

                // 面试助手
                PromptTemplate.create(
                        "面试助手",
                        "模拟面试官进行面试练习",
                        "interview",
                        "你是一位经验丰富的面试官，擅长进行技术面试和行为面试。" +
                                "你会提出有深度的问题，并给予专业的反馈和建议。" +
                                "面试风格：专业、友好、有挑战性。",
                        "我正在准备{{position}}岗位的面试，请帮我模拟面试，领域是{{domain}}。",
                        List.of(
                                PromptVariable.required("position", "应聘岗位"),
                                PromptVariable.required("domain", "技术领域")
                        ),
                        List.of("面试", "求职")
                )
        );

        // 保存默认模板
        for (int i = 0; i < defaultTemplates.size(); i++) {
            PromptTemplate template = defaultTemplates.get(i);
            if (i == 0) {
                template = new PromptTemplate(
                        template.id(), template.name(), template.description(), template.category(),
                        template.systemPrompt(), template.userPromptTemplate(),
                        template.variables(), template.examples(), template.tags(),
                        true, template.createdAt(), template.updatedAt()
                );
            }
            saveToRedis(template);
        }

        log.info("初始化默认提示词模板完成，共 {} 个", defaultTemplates.size());
    }

    @Override
    public List<PromptTemplate> listAll() {
        Set<String> ids = redisTemplate.opsForSet().members(listKey);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return ids.stream()
                .map(this::getFromRedis)
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    // 默认模板排在前面
                    if (a.isDefault()) return -1;
                    if (b.isDefault()) return 1;
                    // 按更新时间倒序
                    return Long.compare(b.updatedAt(), a.updatedAt());
                }).collect(Collectors.toList());
    }

    @Override
    public List<PromptTemplate> listByCategory(String category) {
        return listAll().stream()
                .filter(t -> category.equals(t.category()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PromptTemplate> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return listAll();
        }

        String lowerKeyword = keyword.toLowerCase();
        return listAll().stream()
                .filter(t -> {
                    if (t.name() != null && t.name().toLowerCase().contains(lowerKeyword)) return true;
                    if (t.description() != null && t.description().toLowerCase().contains(lowerKeyword)) return true;
                    if (t.tags() != null && t.tags().stream().anyMatch(tag -> tag.toLowerCase().contains(lowerKeyword)))
                        return true;
                    return false;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PromptTemplate getById(String id) {
        return getFromRedis(id);
    }

    @Override
    public PromptTemplate save(PromptSaveRequest request) {
        PromptTemplate template;

        if (request.isUpdate()) {
            // 更新现有模板
            PromptTemplate existing = getById(request.id());
            if (existing == null) {
                throw new IllegalArgumentException("模板不存在: " + request.id());
            }

            template = new PromptTemplate(
                    existing.id(),
                    request.name() != null ? request.name() : existing.name(),
                    request.description() != null ? request.description() : existing.description(),
                    request.category() != null ? request.category() : existing.category(),
                    request.systemPrompt() != null ? request.systemPrompt() : existing.systemPrompt(),
                    request.userPromptTemplate() != null ? request.userPromptTemplate() : existing.userPromptTemplate(),
                    request.variables() != null ? request.variables() : existing.variables(),
                    request.examples() != null ? request.examples() : existing.examples(),
                    request.tags() != null ? request.tags() : existing.tags(),
                    existing.isDefault(),
                    existing.createdAt(),
                    System.currentTimeMillis()
            );
        } else {
            // 创建新模板
            template = PromptTemplate.create(
                    request.name(),
                    request.description(),
                    request.category(),
                    request.systemPrompt(),
                    request.userPromptTemplate(),
                    request.variables(),
                    request.tags()
            );
        }

        saveToRedis(template);
        log.info("保存提示词模板: {}", template.id());
        return template;
    }

    @Override
    public boolean delete(String id) {
        PromptTemplate template = getById(id);
        if (template == null) {
            return false;
        }

        if (template.isDefault()) {
            throw new IllegalStateException("不能删除默认模板");
        }

        redisTemplate.delete(keyPrefix + id);
        redisTemplate.opsForSet().remove(listKey, id);
        log.info("删除提示词模板: {}", id);
        return true;
    }

    @Override
    public String render(String templateId, Map<String, String> variables, String userMessage) {
        PromptTemplate template = getById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在: " + templateId);
        }

        StringBuilder result = new StringBuilder();

        // 添加系统提示词
        if (template.systemPrompt() != null && !template.systemPrompt().isBlank()) {
            result.append("【系统指令】\n");
            result.append(replaceVariables(template.systemPrompt(), variables));
            result.append("\n\n");
        }

        // 添加用户提示词模板
        if (template.userPromptTemplate() != null && !template.userPromptTemplate().isBlank()) {
            result.append(replaceVariables(template.userPromptTemplate(), variables));
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
        PromptTemplate template = getById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在: " + templateId);
        }

        Map<String, String> result = new LinkedHashMap<>();
        result.put("templateId", templateId);
        result.put("templateName", template.name());
        result.put("systemPrompt", replaceVariables(template.systemPrompt(), variables));
        result.put("userPromptTemplate", replaceVariables(template.userPromptTemplate(), variables));
        result.put("userMessage", userMessage);
        result.put("fullPrompt", render(templateId, variables, userMessage));

        return result;
    }

    @Override
    public PromptTemplate getDefault() {
        return listAll().stream()
                .filter(PromptTemplate::isDefault)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean setDefault(String id) {
        PromptTemplate template = getById(id);
        if (template == null) {
            return false;
        }

        // 移除其他默认标记
        listAll().stream()
                .filter(PromptTemplate::isDefault)
                .filter(t -> !t.id().equals(id))
                .forEach(t -> {
                    PromptTemplate updated = new PromptTemplate(
                            t.id(), t.name(), t.description(), t.category(),
                            t.systemPrompt(), t.userPromptTemplate(),
                            t.variables(), t.examples(), t.tags(),
                            false, t.createdAt(), t.updatedAt()
                    );
                    saveToRedis(updated);
                });

        // 设置新的默认模板
        PromptTemplate updated = new PromptTemplate(
                template.id(), template.name(), template.description(), template.category(),
                template.systemPrompt(), template.userPromptTemplate(),
                template.variables(), template.examples(), template.tags(),
                true, template.createdAt(), System.currentTimeMillis()
        );
        saveToRedis(updated);

        log.info("设置默认模板: {}", id);
        return true;
    }

    // ==================== 私有方法 ====================

    private void saveToRedis(PromptTemplate template) {
        String key = keyPrefix + template.id();
        try {
            String json = objectMapper.writeValueAsString(template);
            redisTemplate.opsForValue().set(key, json);
            redisTemplate.opsForSet().add(listKey, template.id());
        } catch (JsonProcessingException e) {
            log.error("保存模板到Redis失败: {}", template.id(), e);
        }
    }

    private PromptTemplate getFromRedis(String id) {
        String key = keyPrefix + id;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, PromptTemplate.class);
        } catch (JsonProcessingException e) {
            log.error("从Redis读取模板失败: {}", id, e);
            return null;
        }
    }

    /**
     * 替换模板中的变量
     * 支持格式：{{variableName}}
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
