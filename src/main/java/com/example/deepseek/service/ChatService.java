package com.example.deepseek.service;

import com.example.deepseek.config.AiConfig;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final BaseAiService baseAiService;

    public void streamChat(String message, HttpSession session, SseEmitter emitter) {
        baseAiService.executeStreamRequest(message, session, emitter);
    }
} 