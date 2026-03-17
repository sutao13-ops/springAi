package com.example.springai.service.impl;

import com.example.springai.common.ChatRequest;
import com.example.springai.common.ChatResponse;
import com.example.springai.common.enums.ModelProviderEnum;
import com.example.springai.service.ChatService;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChoice;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.model.responses.constant.ResponsesConstants;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemImage;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemText;
import com.volcengine.ark.runtime.model.responses.content.OutputContentItemText;
import com.volcengine.ark.runtime.model.responses.content.ReasoningSummaryPart;
import com.volcengine.ark.runtime.model.responses.item.ItemEasyMessage;
import com.volcengine.ark.runtime.model.responses.item.ItemOutputMessage;
import com.volcengine.ark.runtime.model.responses.item.ItemReasoning;
import com.volcengine.ark.runtime.model.responses.item.MessageContent;
import com.volcengine.ark.runtime.model.responses.request.CreateResponsesRequest;
import com.volcengine.ark.runtime.model.responses.request.ResponsesInput;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import com.volcengine.ark.runtime.service.ArkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天服务实现类
 * <p>
 * 统一封装 Ollama、豆包等模型提供商的调用逻辑，
 * 通过依赖注入获取 ArkService 单例实例
 * </p>
 *
 * @Author SuTao
 * @Date 2026/3/16
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatClient chatClient;
    // 注入单例 ArkService
    private final ArkService arkService;

    @Value("${ark.api.model}")
    private String arkModel;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder, ArkService arkService) {
        this.chatClient = chatClientBuilder.defaultOptions(OllamaOptions.create().withModel("gpt-oss:20b")).build();
        this.arkService = arkService;  // 构造器注入
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            log.info("接收聊天请求 - provider: {}, message: {}", request.provider(), request.message());

            switch (request.provider()) {
                case OLLAMA:
                    String ollamaResponse = chatOllama(request.message());
                    return ChatResponse.success(ollamaResponse, "gpt-oss:20b", ModelProviderEnum.OLLAMA.getCode());
                case DOUBAO:
                    return chatDoubao(request.message());
                case OPENAI:
                    throw new UnsupportedOperationException("OpenAI 支持尚未实现");
                default:
                    throw new IllegalArgumentException("不支持的模型提供商: " + request.provider());
            }
        } catch (Exception e) {
            log.error("聊天请求处理失败", e);
            return ChatResponse.error("处理失败: " + e.getMessage());
        }
    }

    @Override
    public Flux<String> streamChat(ChatRequest request) {
        log.info("接收流式聊天请求 - provider: {}, message: {}", request.provider(), request.message());

        switch (request.provider()) {
            case DOUBAO:
                return streamDoubao(request.message());
            case OLLAMA:
                return Flux.error(new UnsupportedOperationException("Ollama 流式支持尚未实现"));
            case OPENAI:
                return Flux.error(new UnsupportedOperationException("OpenAI 流式支持尚未实现"));
            default:
                return Flux.error(new IllegalArgumentException("不支持的流式模型提供商: " + request.provider()));
        }
    }

    @Override
    public ChatResponse vision(String imageUrl, String prompt) {
        if (!hasText(imageUrl) || !hasText(prompt)) {
            return ChatResponse.error("imageUrl 和 prompt 不能为空");
        }

        try {
            log.info("接收豆包识图请求，imageUrl：{}，prompt：{}", imageUrl, prompt);
            CreateResponsesRequest request = CreateResponsesRequest.builder()
                    .model(arkModel)
                    .input(ResponsesInput.builder().addListItem(
                                    ItemEasyMessage.builder()
                                            .role(ResponsesConstants.MESSAGE_ROLE_USER)
                                            .content(MessageContent.builder()
                                                    .addListItem(InputContentItemImage.builder().imageUrl(imageUrl).build())
                                                    .addListItem(InputContentItemText.builder().text(prompt).build())
                                                    .build())
                                            .build())
                            .build())
                    .build();
            ResponseObject response = arkService.createResponse(request);

            // 提取内容
            String content = extractContent(response);
            String reasoning = extractReasoning(response);
            long tokensUsed = extractTokensUsed(response);

            log.info("豆包识图响应成功，tokens: {}", tokensUsed);
            return ChatResponse.success(content, arkModel, ModelProviderEnum.DOUBAO.getCode(), tokensUsed, reasoning);
        } catch (Exception e) {
            log.error("豆包识图接口执行失败", e);
            return ChatResponse.error("识图失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法（内部实现） ====================

    /**
     * 从 ResponseObject 中提取文本内容
     */
    private String extractContent(ResponseObject response) {
        if (response.getOutput() == null || response.getOutput().isEmpty()) {
            return "";
        }

        try {
            for (Object item : response.getOutput()) {
                if (item instanceof ItemOutputMessage message) {
                    if (message.getContent() != null && !message.getContent().isEmpty()) {
                        OutputContentItemText textItem = (OutputContentItemText) message.getContent().get(0);
                        return textItem.getText() != null ? textItem.getText() : "";
                    }
                }
            }
        } catch (ClassCastException e) {
            log.warn("响应内容类型转换失败", e);
        }

        return "";
    }

    /**
     * 从 ResponseObject 中提取推理过程
     */
    private String extractReasoning(ResponseObject response) {
        if (response.getOutput() == null || response.getOutput().isEmpty()) {
            return null;
        }

        try {
            for (Object item : response.getOutput()) {
                if (item instanceof ItemReasoning reasoning) {
                    if (reasoning.getSummary() != null && !reasoning.getSummary().isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (Object summaryItem : reasoning.getSummary()) {
                            if (summaryItem instanceof ReasoningSummaryPart part) {
                                if (part.getText() != null) {
                                    sb.append(part.getText());
                                }
                            }
                        }
                        return sb.length() > 0 ? sb.toString() : null;
                    }
                }
            }
        } catch (ClassCastException e) {
            log.warn("推理内容类型转换失败", e);
        }

        return null;
    }

    /**
     * 从 ResponseObject 中提取 token 使用量
     */
    private long extractTokensUsed(ResponseObject response) {
        if (response.getUsage() != null) {
            return response.getUsage().getTotalTokens();
        }
        return 0;
    }

    private String chatOllama(String message) {
        try {
            log.info("接收 Ollama 聊天请求：{}", message);
            String response = chatClient.prompt(message).call().content();
            log.info("Ollama 返回响应：{}", response);
            return response;
        } catch (Exception e) {
            log.error("Ollama 聊天接口执行失败", e);
            throw new RuntimeException("Ollama 聊天失败: " + e.getMessage(), e);
        }
    }

    private ChatResponse chatDoubao(String message) {
        try {
            log.info("接收豆包文本请求：{}", message);
            CreateResponsesRequest request = CreateResponsesRequest.builder()
                    .model(arkModel)
                    .input(ResponsesInput.builder().stringValue(message).build())
                    .build();
            ResponseObject response = arkService.createResponse(request);

            String content = extractContent(response);
            String reasoning = extractReasoning(response);
            long tokensUsed = extractTokensUsed(response);

            log.info("豆包文本响应成功，tokens: {}", tokensUsed);
            return ChatResponse.success(content, arkModel, ModelProviderEnum.DOUBAO.getCode(), tokensUsed, reasoning);
        } catch (Exception e) {
            log.error("豆包文本接口执行失败", e);
            throw new RuntimeException("豆包文本接口执行失败: " + e.getMessage(), e);
        }
        // 注意：不再调用 arkService.shutdownExecutor()
    }

    private Flux<String> streamDoubao(String message) {
        log.info("接收豆包流式请求：{}", message);

        return Flux.create(sink -> {
            try {
                List<ChatMessage> messages = new ArrayList<>();
                messages.add(ChatMessage.builder().role(ChatMessageRole.USER).content(message).build());

                ChatCompletionRequest request = ChatCompletionRequest.builder()
                        .model(arkModel)
                        .messages(messages)
                        .stream(Boolean.TRUE)
                        .build();

                arkService.streamChatCompletion(request).blockingForEach(chunk -> {
                    List<ChatCompletionChoice> choices = chunk.getChoices();
                    if (choices != null && !choices.isEmpty()) {
                        String text = choices.get(0).getMessage().getContent().toString();
                        if (text != null && !text.isEmpty()) {
                            sink.next(text);
                        }
                    }
                });

                log.info("豆包流式响应结束");
                sink.complete();
            } catch (Exception e) {
                log.error("豆包流式接口执行失败", e);
                sink.error(e);
            }
            // 注意：不再调用 arkService.shutdownExecutor()
        });
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
