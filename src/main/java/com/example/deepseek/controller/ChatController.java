package com.example.deepseek.controller;

import com.example.deepseek.service.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }

    @GetMapping("/chat/stream")
    @ResponseBody
    public SseEmitter streamChat(@RequestParam String message, HttpSession session) {
        SseEmitter emitter = new SseEmitter();
        chatService.streamChat(message, session, emitter);
        return emitter;
    }
}
 