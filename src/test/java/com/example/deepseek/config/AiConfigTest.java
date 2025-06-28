package com.example.deepseek.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AiConfig 单元测试
 * 作者：菩提树下的杨过
 * 感谢Cursor生成本项目代码
 */
@ExtendWith(MockitoExtension.class)
class AiConfigTest {

    private AiConfig aiConfig;

    @BeforeEach
    void setUp() {
        aiConfig = new AiConfig();
    }

    @Test
    void testOllamaConfig() {
        // 测试Ollama配置
        AiConfig.Ollama ollama = aiConfig.getOllama();
        assertNotNull(ollama);
        assertEquals("http://localhost:11434", ollama.getBaseUrl());
        assertEquals("/api/chat", ollama.getApiPath());
        assertEquals("deepseek-r1:7b", ollama.getModel());
        assertEquals(30000, ollama.getTimeout());
        assertEquals("http://localhost:11434/api/chat", ollama.getFullUrl());
    }

    @Test
    void testDeepseekConfig() {
        // 测试DeepSeek配置
        AiConfig.Deepseek deepseek = aiConfig.getDeepseek();
        assertNotNull(deepseek);
        assertEquals("https://api.deepseek.com", deepseek.getBaseUrl());
        assertEquals("/v1/chat/completions", deepseek.getApiPath());
        assertEquals("deepseek-chat", deepseek.getModel());
        assertEquals(30000, deepseek.getTimeout());
        assertEquals("https://api.deepseek.com/v1/chat/completions", deepseek.getFullUrl());
    }

    @Test
    void testWeatherConfig() {
        // 测试天气配置
        AiConfig.Weather weather = aiConfig.getWeather();
        assertNotNull(weather);
        assertEquals("https://wttr.in", weather.getBaseUrl());
        assertEquals(10000, weather.getTimeout());
    }

    @Test
    void testOllamaConfigSetters() {
        // 测试Ollama配置的setter方法
        AiConfig.Ollama ollama = new AiConfig.Ollama();
        ollama.setBaseUrl("http://test:11434");
        ollama.setApiPath("/test/api");
        ollama.setModel("test-model");
        ollama.setTimeout(60000);
        
        assertEquals("http://test:11434", ollama.getBaseUrl());
        assertEquals("/test/api", ollama.getApiPath());
        assertEquals("test-model", ollama.getModel());
        assertEquals(60000, ollama.getTimeout());
        assertEquals("http://test:11434/test/api", ollama.getFullUrl());
    }

    @Test
    void testDeepseekConfigSetters() {
        // 测试DeepSeek配置的setter方法
        AiConfig.Deepseek deepseek = new AiConfig.Deepseek();
        deepseek.setBaseUrl("https://test.deepseek.com");
        deepseek.setApiPath("/test/v1/chat/completions");
        deepseek.setModel("test-deepseek-model");
        deepseek.setTimeout(60000);
        
        assertEquals("https://test.deepseek.com", deepseek.getBaseUrl());
        assertEquals("/test/v1/chat/completions", deepseek.getApiPath());
        assertEquals("test-deepseek-model", deepseek.getModel());
        assertEquals(60000, deepseek.getTimeout());
        assertEquals("https://test.deepseek.com/test/v1/chat/completions", deepseek.getFullUrl());
    }

    @Test
    void testWeatherConfigSetters() {
        // 测试天气配置的setter方法
        AiConfig.Weather weather = new AiConfig.Weather();
        weather.setBaseUrl("https://test.weather.com");
        weather.setTimeout(20000);
        
        assertEquals("https://test.weather.com", weather.getBaseUrl());
        assertEquals(20000, weather.getTimeout());
    }

    @Test
    void testAiConfigSetters() {
        // 测试AiConfig的setter方法
        AiConfig.Ollama ollama = new AiConfig.Ollama();
        AiConfig.Deepseek deepseek = new AiConfig.Deepseek();
        AiConfig.Weather weather = new AiConfig.Weather();
        
        aiConfig.setOllama(ollama);
        aiConfig.setDeepseek(deepseek);
        aiConfig.setWeather(weather);
        
        assertSame(ollama, aiConfig.getOllama());
        assertSame(deepseek, aiConfig.getDeepseek());
        assertSame(weather, aiConfig.getWeather());
    }
} 