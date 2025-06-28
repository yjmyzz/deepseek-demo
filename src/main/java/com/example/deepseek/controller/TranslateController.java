package com.example.deepseek.controller;

import com.example.deepseek.service.TranslateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@RequiredArgsConstructor
public class TranslateController {
    
    private final TranslateService translateService;

    @GetMapping("/translate")
    public String translatePage() {
        return "translate";
    }
    
    @GetMapping("/translate/stream")
    @ResponseBody
    public SseEmitter translateStream(@RequestParam String text, @RequestParam String targetLang, HttpSession session) {
        SseEmitter emitter = new SseEmitter();
        translateService.streamTranslate(text, targetLang, session, emitter);
        return emitter;
    }
} 