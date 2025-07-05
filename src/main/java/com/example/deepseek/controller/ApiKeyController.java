package com.example.deepseek.controller;

import com.example.deepseek.constant.AiConstants;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ApiKeyController {

    @PostMapping("/setApiKey")
    public String setApiKey(@RequestParam String provider, 
                           @RequestParam(required = false) String apiKey, 
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        // 设置AI提供商
        session.setAttribute(AiConstants.Session.AI_PROVIDER, provider);
        
        // 如果选择的是DeepSeek，验证API Key
        if (AiConstants.Provider.DEEPSEEK.equals(provider)) {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                redirectAttributes.addAttribute("error", "missing_api_key");
                return "redirect:/";
            }
            session.setAttribute(AiConstants.Session.DEEPSEEK_API_KEY, apiKey);
        }
        
        // 标记AI已配置
        session.setAttribute("aiConfigured", "true");
        
        redirectAttributes.addAttribute("success", "true");
        return "redirect:/";
    }

    @PostMapping("/api-key")
    @ResponseBody
    public String setApiKey(@RequestParam String apiKey, HttpSession session) {
        log.info("设置API Key，提供商: {}", (String) session.getAttribute(AiConstants.Session.AI_PROVIDER));
        
        // 设置API Key到session
        if (AiConstants.Provider.DEEPSEEK.equals((String) session.getAttribute(AiConstants.Session.AI_PROVIDER))) {
            session.setAttribute(AiConstants.Session.DEEPSEEK_API_KEY, apiKey);
        }
        
        // 标记AI已配置
        session.setAttribute("aiConfigured", "true");
        
        log.info("API Key设置完成，提供商: {}", (String) session.getAttribute(AiConstants.Session.AI_PROVIDER));
        
        return "success";
    }
    
    @PostMapping("/provider")
    @ResponseBody
    public String setProvider(@RequestParam String provider, HttpSession session) {
        log.info("设置AI提供商: {}", provider);
        
        // 设置AI提供商到session
        session.setAttribute(AiConstants.Session.AI_PROVIDER, provider);
        
        // 标记AI已配置
        session.setAttribute("aiConfigured", "true");
        
        log.info("AI提供商设置完成: {}", provider);
        
        return "success";
    }

    @GetMapping("/api/provider-info")
    @ResponseBody
    public Map<String, String> getProviderInfo(HttpSession session) {
        String provider = (String) session.getAttribute(AiConstants.Session.AI_PROVIDER);
        if (provider == null) {
            provider = "ollama"; // 默认使用本地Ollama
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("provider", provider);
        return response;
    }

    public static boolean isConfigured(HttpSession session) {
        String provider = (String) session.getAttribute(AiConstants.Session.AI_PROVIDER);
        return provider != null && !provider.trim().isEmpty();
    }
} 