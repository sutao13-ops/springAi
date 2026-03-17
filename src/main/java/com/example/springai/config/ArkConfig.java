package com.example.springai.config;

import com.volcengine.ark.runtime.service.ArkService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 火山引擎 ARK SDK 配置类
 * <p>
 * 将 ArkService 注册为 Spring Bean，实现单例化管理，
 * 避免每次请求都创建新的实例
 * </p>
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
@Configuration // 告诉Spring这是一个配置类
public class ArkConfig {

    private static final Logger log = LoggerFactory.getLogger(ArkConfig.class);

    @Value("${ark.api.key}")
    private String arkApiKey;

    @Value("${ark.api.base-url}")
    private String arkBaseUrl;

    /**
     * ArkService 单例实例
     */
    private ArkService arkService;

    /**
     * 创建 ArkService Bean
     *
     * @return ArkService 实例
     */
    @Bean // 告诉Spring“这个方法返回的对象要交给容器管理”
    public ArkService arkService() {
        log.info("初始化 ArkService - baseUrl: {}", arkBaseUrl); // 打印初始化日志
        this.arkService = ArkService.builder() // 用建造者模式创建ArkService实例
                .apiKey(arkApiKey) // 传入从配置文件读的密钥
                .baseUrl(arkBaseUrl) // 传入从配置文件读的服务地址
                .build(); // 构建实例
        return this.arkService; // 返回实例，Spring会把它存到容器里
    }
    /**
     * 应用关闭时优雅关闭 ArkService 执行器
     */
    @PreDestroy
    public void destroy() {
        if (arkService != null) {
            log.info("关闭 ArkService 执行器");
            arkService.shutdownExecutor();
        }
    }
}
