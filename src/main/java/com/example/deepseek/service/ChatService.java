package com.example.deepseek.service;

import com.example.deepseek.config.AiConfig;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ChatService extends BaseAiService {

    public ChatService(AiConfig aiConfig) {
        super(aiConfig);
    }

    public void streamChat(String message, HttpSession session, SseEmitter emitter) {
        String provider = getDefaultProvider(session);
        executeStreamRequest(message, session, emitter, provider, "聊天");
    }
} 