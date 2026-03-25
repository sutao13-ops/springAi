package com.example.springai.config;

import com.example.springai.entity.PromptTemplateEntity;
import com.example.springai.entity.PromptVariable;
import com.example.springai.mapper.PromptTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 初始化默认提示词模板
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PromptTemplateMapper promptTemplateMapper;

    @Override
    public void run(String... args) {
        // 检查是否已有数据
        Long count = promptTemplateMapper.selectCount(null);
        if (count > 0) {
            log.info("已有提示词模板数据，跳过初始化");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 初始化默认模板
        List<PromptTemplateEntity> defaultTemplates = List.of(
                // 代码助手
                new PromptTemplateEntity()
                        .setName("代码助手")
                        .setDescription("专业的编程助手，精通多种编程语言")
                        .setCategory("assistant")
                        .setSystemPrompt("你是一个专业的编程助手，精通 Java、Python、JavaScript、Go 等多种编程语言。" +
                                "你会提供清晰、高效、可维护的代码示例，并解释代码的工作原理。" +
                                "回答时请使用 Markdown 格式，代码块标注语言类型。")
                        .setVariables(List.of(
                                PromptVariable.of("language", "编程语言"),
                                PromptVariable.of("task", "具体任务")
                        ))
                        .setTags(List.of("代码", "编程", "开发"))
                        .setIsDefault(true)
                        .setUseCount(0)
                        .setCreatedAt(now)
                        .setUpdatedAt(now),

                // 翻译助手
                new PromptTemplateEntity()
                        .setName("翻译助手")
                        .setDescription("专业的多语言翻译助手")
                        .setCategory("translation")
                        .setSystemPrompt("你是一个专业的翻译助手，精通中文、英文、日文、韩文等多种语言。" +
                                "翻译时请保持原文的语气和风格，并提供翻译说明。")
                        .setUserPromptTemplate("请将以下{{source_lang}}文本翻译成{{target_lang}}：\n{{text}}")
                        .setVariables(List.of(
                                PromptVariable.required("source_lang", "源语言"),
                                PromptVariable.required("target_lang", "目标语言"),
                                PromptVariable.required("text", "待翻译文本")
                        ))
                        .setTags(List.of("翻译", "语言"))
                        .setIsDefault(false)
                        .setUseCount(0)
                        .setCreatedAt(now)
                        .setUpdatedAt(now),

                // 写作助手
                new PromptTemplateEntity()
                        .setName("写作助手")
                        .setDescription("专业的文案写作助手")
                        .setCategory("writing")
                        .setSystemPrompt("你是一个专业的写作助手，擅长各种类型的文案创作。" +
                                "你的文字优美流畅，富有感染力，能够根据需求调整写作风格。")
                        .setUserPromptTemplate("请帮我写一篇关于{{topic}}的{{style}}文章，字数约{{word_count}}字。")
                        .setVariables(List.of(
                                PromptVariable.required("topic", "主题"),
                                PromptVariable.withDefault("style", "风格", "通俗易懂"),
                                PromptVariable.withDefault("word_count", "字数", "500")
                        ))
                        .setTags(List.of("写作", "文案", "创作"))
                        .setIsDefault(false)
                        .setUseCount(0)
                        .setCreatedAt(now)
                        .setUpdatedAt(now),

                // 面试助手
                new PromptTemplateEntity()
                        .setName("面试助手")
                        .setDescription("模拟面试官进行面试练习")
                        .setCategory("interview")
                        .setSystemPrompt("你是一位经验丰富的面试官，擅长进行技术面试和行为面试。" +
                                "你会提出有深度的问题，并给予专业的反馈和建议。" +
                                "面试风格：专业、友好、有挑战性。")
                        .setUserPromptTemplate("我正在准备{{position}}岗位的面试，请帮我模拟面试，领域是{{domain}}。")
                        .setVariables(List.of(
                                PromptVariable.required("position", "应聘岗位"),
                                PromptVariable.required("domain", "技术领域")
                        ))
                        .setTags(List.of("面试", "求职"))
                        .setIsDefault(false)
                        .setUseCount(0)
                        .setCreatedAt(now)
                        .setUpdatedAt(now)
        );

        // 批量插入
        for (PromptTemplateEntity template : defaultTemplates) {
            promptTemplateMapper.insert(template);
        }

        log.info("初始化默认提示词模板完成，共 {} 个", defaultTemplates.size());
    }
}
