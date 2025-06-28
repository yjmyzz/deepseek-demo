package com.example.deepseek;

import com.example.deepseek.config.AiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiConfig.class)
public class DeepseekDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeepseekDemoApplication.class, args);
    }
} 