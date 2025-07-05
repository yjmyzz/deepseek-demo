package com.example.deepseek.service;

import com.example.deepseek.config.AiConfig;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class TranslateService {
    
    private final BaseAiService baseAiService;
    
    public void streamTranslate(String text, String targetLang, HttpSession session, SseEmitter emitter) {
        String prompt = buildTranslatePrompt(text, targetLang);
        baseAiService.executeStreamRequest(prompt, session, emitter);
    }
    
    /**
     * 构建翻译提示词
     */
    private String buildTranslatePrompt(String text, String targetLang) {
        if ("zh".equals(targetLang)) {
            return String.format("请将以下文本翻译成中文（如果原文已经是中文，请直接返回原文，不要做任何解释）：\n\n%s", text);
        } else if ("en".equals(targetLang)) {
            return String.format("请将以下文本翻译成英文（如果原文已经是英文，请直接返回原文，不要做任何解释）：\n\n%s", text);
        } else {
            return String.format("请将以下文本翻译成%s（如果原文已经是%s，请直接返回原文，不要做任何解释）：\n\n%s", targetLang, targetLang, text);
        }
    }
} 