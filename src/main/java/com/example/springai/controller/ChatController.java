package com.example.springai.controller;

import com.example.springai.common.ApiResponse;
import com.example.springai.common.ChatRequest;
import com.example.springai.common.ChatResponse;
import com.example.springai.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class ChatController {

//    private final ChatService chatService;
//
//    public ChatController(ChatService chatService) {
//        this.chatService = chatService;
//    }

    private final ChatService chatService;
    private final StringRedisTemplate redisTemplate;  // 添加Redis模板

    public ChatController(ChatService chatService, StringRedisTemplate redisTemplate) {
        this.chatService = chatService;
        this.redisTemplate = redisTemplate;  // 注入Redis模板
    }

    /**
     * 统一聊天接口
     *
     * 示例请求:
     * {
     *   "provider": "DOUBAO",
     *   "message": "你好",
     *   "stream": false
     * }
     */
    @PostMapping("/chat")
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ApiResponse.success(chatService.chat(request));
    }

    /**
     * 统一流式聊天接口
     */
    @PostMapping(value = "/doubao/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest request) {
        return chatService.streamChat(request);
    }

    /**
     * 视觉识别接口
     */
    @PostMapping("/vision")
    public ApiResponse<ChatResponse> vision(@RequestParam("imageUrl") String imageUrl, @RequestParam("prompt") String prompt) {
        return ApiResponse.success(chatService.vision(imageUrl, prompt));
    }


    /**
     * 测试Redis配置
     */
    @GetMapping("/test-config")
    public String testRedisConfig(
            @Value("${spring.data.redis.host:未配置}") String host,
            @Value("${spring.data.redis.port:未配置}") String port,
            @Value("${spring.data.redis.password:未配置}") String password
    ) {
        return String.format("Redis配置 - host: %s, port: %s, password: %s", host, port, password);
    }


    /**
     * 测试Redis实际连接
     */
    @GetMapping("/test-redis")
    public String testRedisConnection() {
        try {
            // 测试写入
            redisTemplate.opsForValue().set("test:key", "hello-redis");
            // 测试读取
            String value = redisTemplate.opsForValue().get("test:key");
            // 测试删除
            redisTemplate.delete("test:key");

            return "Redis连接成功！写入并读取的值: " + value;
        } catch (Exception e) {
            return "Redis连接失败: " + e.getClass().getName() + " - " + e.getMessage();
        }
    }
}
