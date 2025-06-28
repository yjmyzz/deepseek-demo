package com.example.deepseek.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    
    private Ollama ollama = new Ollama();
    private Deepseek deepseek = new Deepseek();
    private Weather weather = new Weather();
    
    @Data
    public static class Ollama {
        private String baseUrl = "http://localhost:11434";
        private String apiPath = "/api/chat";
        private String model = "deepseek-r1:7b";
        private int timeout = 30000;
        
        public String getFullUrl() {
            return baseUrl + apiPath;
        }
    }
    
    @Data
    public static class Deepseek {
        private String baseUrl = "https://api.deepseek.com";
        private String apiPath = "/v1/chat/completions";
        private String model = "deepseek-chat";
        private int timeout = 30000;
        
        public String getFullUrl() {
            return baseUrl + apiPath;
        }
    }
    
    @Data
    public static class Weather {
        private String baseUrl = "https://wttr.in";
        private int timeout = 10000;
    }
} 