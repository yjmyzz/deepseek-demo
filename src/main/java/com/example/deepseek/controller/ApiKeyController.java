package com.example.deepseek.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequiredArgsConstructor
public class ApiKeyController {

    @PostMapping("/setApiKey")
    public String setApiKey(@RequestParam String provider, 
                           @RequestParam(required = false) String apiKey, 
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        // 设置AI提供商
        session.setAttribute("aiProvider", provider);
        
        // 如果选择的是DeepSeek，验证API Key
        if ("deepseek".equals(provider)) {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                redirectAttributes.addAttribute("error", "missing_api_key");
                return "redirect:/";
            }
            session.setAttribute("deepseekApiKey", apiKey);
        }
        
        redirectAttributes.addAttribute("success", "true");
        return "redirect:/";
    }


    


    @GetMapping("/api/provider-info")
    @ResponseBody
    public Map<String, String> getProviderInfo(HttpSession session) {
        String provider = (String) session.getAttribute("aiProvider");
        if (provider == null) {
            provider = "ollama"; // 默认使用本地Ollama
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("provider", provider);
        return response;
    }
} 