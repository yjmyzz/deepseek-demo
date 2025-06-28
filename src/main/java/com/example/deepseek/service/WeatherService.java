package com.example.deepseek.service;

import com.example.deepseek.config.AiConfig;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    
    private final AiConfig aiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    
    public String getWeather(String city, HttpSession session) {
        try {
            // 使用wttr.in API获取天气信息
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String weatherUrl = aiConfig.getWeather().getBaseUrl() + "/" + encodedCity + "?format=3";
            
            log.info("正在查询天气信息: {}", weatherUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "DeepSeek-Demo/1.0");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                weatherUrl,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String weatherInfo = response.getBody().trim();
                log.info("天气查询成功: {}", weatherInfo);
                return formatWeatherInfo(city, weatherInfo);
            } else {
                log.warn("天气API返回错误状态码: {}", response.getStatusCode());
                return "抱歉，无法获取 " + city + " 的天气信息，请稍后重试。";
            }
            
        } catch (ResourceAccessException e) {
            log.error("天气API连接失败: {}", e.getMessage());
            return "抱歉，天气服务暂时不可用，请稍后重试。";
        } catch (Exception e) {
            log.error("天气查询异常: {}", e.getMessage(), e);
            return "抱歉，查询天气信息时出现错误，请稍后重试。";
        }
    }
    
    /**
     * 格式化天气信息
     */
    private String formatWeatherInfo(String city, String rawWeatherInfo) {
        StringBuilder formattedInfo = new StringBuilder();
        formattedInfo.append("🌤️ ").append(city).append(" 天气信息\n\n");
        
        // 解析wttr.in返回的格式3数据
        String[] lines = rawWeatherInfo.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            // 添加emoji和格式化
            String formattedLine = line.trim();
            if (formattedLine.contains("°C")) {
                formattedLine = "🌡️ " + formattedLine;
            } else if (formattedLine.contains("km/h")) {
                formattedLine = "💨 " + formattedLine;
            } else if (formattedLine.contains("%")) {
                formattedLine = "💧 " + formattedLine;
            } else if (formattedLine.contains("hPa")) {
                formattedLine = "📊 " + formattedLine;
            }
            
            formattedInfo.append(formattedLine).append("\n");
        }
        
        return formattedInfo.toString();
    }
} 
 