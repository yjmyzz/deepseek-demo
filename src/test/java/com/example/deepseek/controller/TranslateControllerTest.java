package com.example.deepseek.controller;

import com.example.deepseek.service.TranslateService;
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
 * TranslateController 单元测试
 * 作者：菩提树下的杨过
 * 感谢Cursor生成本项目代码
 */
@ExtendWith(MockitoExtension.class)
class TranslateControllerTest {

    @Mock
    private TranslateService translateService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private TranslateController translateController;

    @BeforeEach
    void setUp() {
        // 基本设置
    }

    @Test
    void testTranslatePage() {
        // 测试翻译页面
        String result = translateController.translatePage();
        assert "translate".equals(result);
    }

    @Test
    void testStreamTranslate() {
        // 测试流式翻译
        String text = "Hello, how are you?";
        String targetLang = "zh";
        
        SseEmitter result = translateController.translateStream(text, targetLang, session);
        assert result != null;
        verify(translateService).streamTranslate(text, targetLang, session, result);
    }

    @Test
    void testStreamTranslateWithEmptyText() {
        // 测试空文本的流式翻译
        String text = "";
        String targetLang = "zh";
        
        SseEmitter result = translateController.translateStream(text, targetLang, session);
        assert result != null;
        verify(translateService).streamTranslate(text, targetLang, session, result);
    }

    @Test
    void testStreamTranslateWithNullText() {
        // 测试null文本的流式翻译
        String text = null;
        String targetLang = "zh";
        
        SseEmitter result = translateController.translateStream(text, targetLang, session);
        assert result != null;
        verify(translateService).streamTranslate(text, targetLang, session, result);
    }

    @Test
    void testStreamTranslateWithChineseText() {
        // 测试中文文本翻译为英文
        String text = "你好，你好吗？";
        String targetLang = "en";
        
        SseEmitter result = translateController.translateStream(text, targetLang, session);
        assert result != null;
        verify(translateService).streamTranslate(text, targetLang, session, result);
    }

    @Test
    void testStreamTranslateWithEnglishText() {
        // 测试英文文本翻译为中文
        String text = "Hello, how are you?";
        String targetLang = "zh";
        
        SseEmitter result = translateController.translateStream(text, targetLang, session);
        assert result != null;
        verify(translateService).streamTranslate(text, targetLang, session, result);
    }

    @Test
    void testStreamTranslateWithSpecialCharacters() {
        // 测试包含特殊字符的文本
        String text = "Hello! How are you? 你好！";
        String targetLang = "zh";
        
        SseEmitter result = translateController.translateStream(text, targetLang, session);
        assert result != null;
        verify(translateService).streamTranslate(text, targetLang, session, result);
    }
} 