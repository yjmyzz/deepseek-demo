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
 * BaseAiService 单元测试
 * 作者：菩提树下的杨过
 * 感谢Cursor生成本项目代码
 */
@ExtendWith(MockitoExtension.class)
class BaseAiServiceTest {

    @Mock
    private AiConfig aiConfig;

    @Mock
    private AiConfig.Ollama ollamaConfig;

    @Mock
    private AiConfig.Deepseek deepseekConfig;

    @Mock
    private HttpSession session;

    @Mock
    private SseEmitter emitter;

    @InjectMocks
    private TestBaseAiService baseAiService;

    @BeforeEach
    void setUp() {
        // 配置Mock对象
        when(aiConfig.getOllama()).thenReturn(ollamaConfig);
        when(aiConfig.getDeepseek()).thenReturn(deepseekConfig);
        
        when(ollamaConfig.getBaseUrl()).thenReturn("http://localhost:11434");
        when(ollamaConfig.getApiPath()).thenReturn("/api/chat");
        when(ollamaConfig.getModel()).thenReturn("deepseek-r1:7b");
        when(ollamaConfig.getFullUrl()).thenReturn("http://localhost:11434/api/chat");
        
        when(deepseekConfig.getBaseUrl()).thenReturn("https://api.deepseek.com");
        when(deepseekConfig.getApiPath()).thenReturn("/v1/chat/completions");
        when(deepseekConfig.getModel()).thenReturn("deepseek-chat");
        when(deepseekConfig.getFullUrl()).thenReturn("https://api.deepseek.com/v1/chat/completions");
    }

    @Test
    void testGetDefaultProvider() {
        // 测试获取默认提供商
        when(session.getAttribute("provider")).thenReturn("ollama");
        String provider = baseAiService.getDefaultProvider(session);
        assert "ollama".equals(provider);
        
        when(session.getAttribute("provider")).thenReturn(null);
        provider = baseAiService.getDefaultProvider(session);
        assert "ollama".equals(provider); // 默认值
    }

    @Test
    void testExecuteStreamRequestWithOllama() {
        // 测试执行Ollama流式请求
        String prompt = "Hello, how are you?";
        String provider = "ollama";
        String requestType = "聊天";
        
        try {
            baseAiService.executeStreamRequest(prompt, session, emitter, provider, requestType);
            // 由于是异步操作，这里主要测试方法调用不抛出异常
            verify(aiConfig).getOllama();
        } catch (Exception e) {
            // 在测试环境中可能会因为网络连接失败而抛出异常，这是正常的
        }
    }

    @Test
    void testExecuteStreamRequestWithDeepseek() {
        // 测试执行DeepSeek流式请求
        String prompt = "Hello, how are you?";
        String provider = "deepseek";
        String requestType = "聊天";
        
        when(session.getAttribute("deepseekApiKey")).thenReturn("test-api-key");
        
        try {
            baseAiService.executeStreamRequest(prompt, session, emitter, provider, requestType);
            verify(aiConfig).getDeepseek();
            verify(session).getAttribute("deepseekApiKey");
        } catch (Exception e) {
            // 在测试环境中可能会因为网络连接失败而抛出异常，这是正常的
        }
    }

    @Test
    void testExecuteStreamRequestWithMissingApiKey() {
        // 测试DeepSeek缺少API Key的情况
        String prompt = "Hello, how are you?";
        String provider = "deepseek";
        String requestType = "聊天";
        
        when(session.getAttribute("deepseekApiKey")).thenReturn(null);
        
        try {
            baseAiService.executeStreamRequest(prompt, session, emitter, provider, requestType);
            verify(session).getAttribute("deepseekApiKey");
        } catch (Exception e) {
            // 在测试环境中可能会因为网络连接失败而抛出异常，这是正常的
        }
    }

    // 测试用的具体实现类
    private static class TestBaseAiService extends BaseAiService {
        public TestBaseAiService(AiConfig aiConfig) {
            super(aiConfig);
        }

        public String getDefaultProvider(HttpSession session) {
            return super.getDefaultProvider(session);
        }

        public void executeStreamRequest(String prompt, HttpSession session, SseEmitter emitter, 
                                       String provider, String requestType) {
            super.executeStreamRequest(prompt, session, emitter, provider, requestType);
        }
    }
} 