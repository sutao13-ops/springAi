package com.example.springai.controller;

import com.example.springai.common.ApiResponse;
import com.example.springai.common.ChatRequest;
import com.example.springai.common.ChatResponse;
import com.example.springai.common.dto.prompt.ChatWithPromptRequest;
import com.example.springai.common.dto.session.SessionChatRequest;
import com.example.springai.common.enums.ModelProviderEnum;
import com.example.springai.service.ChatService;
import com.example.springai.service.PromptRedisService;
import com.example.springai.service.SessionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 基于提示词模板的聊天控制器
 *
 * @Author SuTao
 * @Date 2026/3/18
 */
@RestController
@RequestMapping("/chat/redis")
public class ChatWithRedisPromptController {

    private final ChatService chatService;
    private final PromptRedisService promptRedisService;
    private final SessionService sessionService;

    public ChatWithRedisPromptController(ChatService chatService,
                                         PromptRedisService promptRedisService,
                                         SessionService sessionService) {
        this.chatService = chatService;
        this.promptRedisService = promptRedisService;
        this.sessionService = sessionService;
    }

    /**
     * 使用提示词模板聊天（单轮对话）
     *
     * @param templateId 模板ID
     * @param message    用户消息
     * @param variables  模板变量
     * @param provider   模型提供商
     * @return 聊天响应
     */
    @PostMapping("/with-prompt")
    public ApiResponse<ChatResponse> chatWithPrompt(
            @RequestParam String templateId,
            @RequestParam String message,
            @RequestParam(required = false) Map<String, String> variables,
            @RequestParam(required = false, defaultValue = "DOUBAO") String provider) {

        try {
            // 渲染提示词
            String fullPrompt = promptRedisService.render(templateId, variables, message);

            // 调用聊天服务
            ChatResponse response = chatService.chat(
                    new ChatRequest(
                            ModelProviderEnum.valueOf(provider),
                            fullPrompt,
                            null, null, false
                    )
            );

            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 使用提示词模板聊天（请求体方式）
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    @PostMapping("/with-prompt/body")
    public ApiResponse<ChatResponse> chatWithPromptBody(@RequestBody ChatWithPromptRequest request) {
        try {
            String fullPrompt = promptRedisService.render(request.templateId(), request.variables(), request.message());

            ChatResponse response = chatService.chat(
                    new ChatRequest(
                            request.provider(),
                            fullPrompt,
                            null, null, false
                    )
            );

            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 使用提示词模板聊天（多轮对话）
     *
     * @param request 聊天请求（包含 sessionId）
     * @return 聊天响应
     */
    @PostMapping("/with-prompt/session")
    public ApiResponse<ChatResponse> chatWithPromptSession(@RequestBody ChatWithPromptRequest request) {
        try {
            // 如果没有 sessionId，创建新会话
            String sessionId = request.sessionId();
            if (sessionId == null || sessionId.isBlank()) {
                sessionId = sessionService.createSession();
            }

            // 渲染提示词并获取历史
            String fullPrompt = promptRedisService.render(request.templateId(), request.variables(), request.message());

            // 发送消息（携带历史）
            ChatResponse response = sessionService.sendMessage(
                    sessionId,
                    new SessionChatRequest(
                            fullPrompt,
                            request.provider(),
                            false
                    )
            );

            // 将 sessionId 附加到响应中（可以在 ChatResponse 中添加该字段）
            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 使用提示词模板聊天（流式）
     *
     * @param templateId 模板ID
     * @param message    用户消息
     * @param variables  模板变量
     * @param provider   模型提供商
     * @return 流式响应
     */
    @GetMapping(value = "/with-prompt/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatWithPromptStream(
            @RequestParam String templateId,
            @RequestParam String message,
            @RequestParam(required = false) Map<String, String> variables,
            @RequestParam(required = false, defaultValue = "DOUBAO") String provider) {

        try {
            String fullPrompt = promptRedisService.render(templateId, variables, message);

            return chatService.streamChat(
                    new ChatRequest(
                            ModelProviderEnum.valueOf(provider),
                            fullPrompt,
                            null, null, true
                    )
            );
        } catch (IllegalArgumentException e) {
            return Flux.error(e);
        }
    }
}
