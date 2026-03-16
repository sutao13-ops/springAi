package com.example.springai.service;

import com.volcengine.ark.runtime.model.responses.constant.ResponsesConstants;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemImage;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemText;
import com.volcengine.ark.runtime.model.responses.item.ItemEasyMessage;
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

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatClient chatClient;

    @Value("${ark.api.key}")
    private String arkApiKey;

    @Value("${ark.api.base-url}")
    private String arkBaseUrl;

    @Value("${ark.api.model}")
    private String arkModel;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultOptions(OllamaOptions.create().withModel("gpt-oss:20b"))
                .build();
    }

    @Override
    public String chat(String message) {
        try {
            log.info("接收 Ollama 聊天请求：{}", message);
            String response = chatClient.prompt(message).call().content();
            log.info("Ollama 返回响应：{}", response);
            return response;
        } catch (Exception e) {
            log.error("Ollama 聊天接口执行失败", e);
            return "接口执行失败：" + e.getMessage();
        }
    }

    @Override
    public ResponseObject doubaoChat(String message) {
        ArkService arkService = createArkService();
        try {
            log.info("接收豆包文本请求：{}", message);
            CreateResponsesRequest request = CreateResponsesRequest.builder()
                    .model(arkModel)
                    .input(ResponsesInput.builder().stringValue(message).build())
                    .build();
            ResponseObject response = arkService.createResponse(request);
            log.info("豆包文本响应成功");
            return response;
        } catch (Exception e) {
            log.error("豆包文本接口执行失败", e);
            throw new IllegalStateException("豆包文本接口执行失败：" + e.getMessage(), e);
        } finally {
            arkService.shutdownExecutor();
        }
    }

    @Override
    public String doubaoVision(String imageUrl, String prompt) {
        if (!hasText(imageUrl) || !hasText(prompt)) {
            return "接口执行失败：imageUrl 和 prompt 不能为空";
        }
        ArkService arkService = createArkService();
        try {
            log.info("接收豆包识图请求，imageUrl：{}，prompt：{}", imageUrl, prompt);
            CreateResponsesRequest request = CreateResponsesRequest.builder().model(arkModel).input(ResponsesInput.builder().addListItem(
                            ItemEasyMessage.builder()
                                    .role(ResponsesConstants.MESSAGE_ROLE_USER)
                                    .content(MessageContent.builder()
                                            .addListItem(InputContentItemImage.builder().imageUrl(imageUrl).build())
                                            .addListItem(InputContentItemText.builder().text(prompt).build())
                                            .build())
                                    .build()
                    ).build())
                    .build();
            ResponseObject response = arkService.createResponse(request);
            log.info("豆包识图响应成功");
            return String.valueOf(response);
        } catch (Exception e) {
            log.error("豆包识图接口执行失败", e);
            return "接口执行失败：" + e.getMessage();
        } finally {
            arkService.shutdownExecutor();
        }
    }

    private ArkService createArkService() {
        return ArkService.builder()
                .apiKey(arkApiKey)
                .baseUrl(arkBaseUrl)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
