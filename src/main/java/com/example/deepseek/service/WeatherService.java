package com.example.deepseek.service;

import com.example.deepseek.config.AiConfig;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherService {
    
    private final AiConfig aiConfig;
    
    public String getWeather(String city, HttpSession session) {
        String apiKey = (String) session.getAttribute("deepseekApiKey");
        // 这里调用 MCP 天气 API，传入 city，返回天气信息
        // 使用配置的天气API基础URL
        return "模拟天气：" + city + " 晴 25°C（API Key: " + apiKey + "，Weather API: " + aiConfig.getWeather().getBaseUrl() + "）";
    }
} 
 