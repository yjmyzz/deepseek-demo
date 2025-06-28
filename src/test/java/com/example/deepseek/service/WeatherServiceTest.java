package com.example.deepseek.service;

import com.example.deepseek.config.AiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WeatherService 单元测试
 * 作者：菩提树下的杨过
 * 感谢Cursor生成本项目代码
 */
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private AiConfig aiConfig;

    @Mock
    private AiConfig.Ollama ollamaConfig;

    @Mock
    private AiConfig.Deepseek deepseekConfig;

    @Mock
    private AiConfig.Weather weatherConfig;

    @Mock
    private HttpSession session;

    @Mock
    private SseEmitter emitter;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        // 配置Mock对象
        when(aiConfig.getOllama()).thenReturn(ollamaConfig);
        when(aiConfig.getDeepseek()).thenReturn(deepseekConfig);
        when(aiConfig.getWeather()).thenReturn(weatherConfig);
        
        when(ollamaConfig.getBaseUrl()).thenReturn("http://localhost:11434");
        when(ollamaConfig.getApiPath()).thenReturn("/api/chat");
        when(ollamaConfig.getModel()).thenReturn("deepseek-r1:7b");
        when(ollamaConfig.getFullUrl()).thenReturn("http://localhost:11434/api/chat");
        
        when(deepseekConfig.getBaseUrl()).thenReturn("https://api.deepseek.com");
        when(deepseekConfig.getApiPath()).thenReturn("/v1/chat/completions");
        when(deepseekConfig.getModel()).thenReturn("deepseek-chat");
        when(deepseekConfig.getFullUrl()).thenReturn("https://api.deepseek.com/v1/chat/completions");
        
        when(weatherConfig.getBaseUrl()).thenReturn("https://wttr.in");
        when(weatherConfig.getTimeout()).thenReturn(10000);
    }

    @Test
    void testGetWeatherWithOllama() {
        // 测试使用Ollama获取天气信息
        String city = "Beijing";
        String provider = "ollama";
        
        when(session.getAttribute("provider")).thenReturn(provider);
        when(session.getAttribute("deepseekApiKey")).thenReturn("test-api-key");
        
        String result = weatherService.getWeather(city, session);
        assert result.contains("Beijing");
        assert result.contains("test-api-key");
        verify(session).getAttribute("deepseekApiKey");
    }

    @Test
    void testGetWeatherWithDeepseek() {
        // 测试使用DeepSeek获取天气信息
        String city = "Beijing";
        String provider = "deepseek";
        
        when(session.getAttribute("provider")).thenReturn(provider);
        when(session.getAttribute("deepseekApiKey")).thenReturn("test-api-key");
        
        String result = weatherService.getWeather(city, session);
        assert result.contains("Beijing");
        assert result.contains("test-api-key");
        verify(session).getAttribute("deepseekApiKey");
    }

    @Test
    void testGetWeatherWithDefaultProvider() {
        // 测试使用默认提供商获取天气信息
        String city = "Beijing";
        
        when(session.getAttribute("provider")).thenReturn(null);
        when(session.getAttribute("deepseekApiKey")).thenReturn("test-api-key");
        
        String result = weatherService.getWeather(city, session);
        assert result.contains("Beijing");
        assert result.contains("test-api-key");
        verify(session).getAttribute("deepseekApiKey");
    }

    @Test
    void testGetWeatherWithEmptyCity() {
        // 测试空城市名的情况
        String city = "";
        
        when(session.getAttribute("deepseekApiKey")).thenReturn("test-api-key");
        
        String result = weatherService.getWeather(city, session);
        assert result.contains("test-api-key");
        verify(session).getAttribute("deepseekApiKey");
    }

    @Test
    void testGetWeatherWithNullCity() {
        // 测试null城市名的情况
        String city = null;
        
        when(session.getAttribute("deepseekApiKey")).thenReturn("test-api-key");
        
        String result = weatherService.getWeather(city, session);
        assert result.contains("test-api-key");
        verify(session).getAttribute("deepseekApiKey");
    }

    @Test
    void testGetWeatherWithChineseCity() {
        // 测试中文城市名
        String city = "上海";
        String provider = "ollama";
        
        when(session.getAttribute("provider")).thenReturn(provider);
        when(session.getAttribute("deepseekApiKey")).thenReturn("test-api-key");
        
        String result = weatherService.getWeather(city, session);
        assert result.contains("上海");
        assert result.contains("test-api-key");
        verify(session).getAttribute("deepseekApiKey");
    }

    @Test
    void testGetWeatherWithEnglishCity() {
        // 测试英文城市名
        String city = "New York";
        String provider = "ollama";
        
        when(session.getAttribute("provider")).thenReturn(provider);
        when(session.getAttribute("deepseekApiKey")).thenReturn("test-api-key");
        
        String result = weatherService.getWeather(city, session);
        assert result.contains("New York");
        assert result.contains("test-api-key");
        verify(session).getAttribute("deepseekApiKey");
    }
} 