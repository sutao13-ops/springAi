package com.example.springai.controller;

import com.volcengine.ark.runtime.model.responses.constant.ResponsesConstants;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemImage;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemText;
import com.volcengine.ark.runtime.model.responses.item.ItemEasyMessage;
import com.volcengine.ark.runtime.model.responses.item.MessageContent;
import com.volcengine.ark.runtime.model.responses.request.CreateResponsesRequest;
import com.volcengine.ark.runtime.model.responses.request.ResponsesInput;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

public class demo {
    public static void main(String[] args) {
        Properties properties = loadYamlProperties();
        String apiKey = requireProperty(properties, "ark.api.key");
        String baseUrl = requireProperty(properties, "ark.api.base-url");
        String model = requireProperty(properties, "ark.api.model");
        String imageUrl = "https://ark-project.tos-cn-beijing.volces.com/doc_image/ark_demo_img_1.png";
        String prompt = "你看见了什么？";


        ArkService arkService = ArkService.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        try {
            CreateResponsesRequest request = CreateResponsesRequest.builder()
                    .model(model)
                    .input(ResponsesInput.builder().addListItem(
                            ItemEasyMessage.builder().role(ResponsesConstants.MESSAGE_ROLE_USER).content(
                                    MessageContent.builder()
                                            .addListItem(InputContentItemImage.builder().imageUrl(imageUrl).build())
                                            .addListItem(InputContentItemText.builder().text(prompt).build())
                                            .build()
                            ).build()
                    ).build())
                    .build();

            ResponseObject resp = arkService.createResponse(request);
            System.out.println(resp);
        } finally {
            arkService.shutdownExecutor();
        }
    }

    private static Properties loadYamlProperties() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yaml"));
        Properties properties = yaml.getObject();
        if (properties == null) {
            throw new IllegalStateException("无法读取 application.yaml");
        }
        return properties;
    }

    private static String requireProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("缺少配置项: " + key);
        }
        return value;
    }
}