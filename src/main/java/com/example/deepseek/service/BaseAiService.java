package com.example.deepseek.service;

import com.example.deepseek.config.AiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@RequiredArgsConstructor
public abstract class BaseAiService {
    
    protected final AiConfig aiConfig;
    protected final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 执行流式AI请求
     */
    protected void executeStreamRequest(String prompt, HttpSession session, SseEmitter emitter, 
                                      String provider, String requestType) {
        if ("ollama".equals(provider)) {
            executeOllamaStreamRequest(prompt, emitter, requestType);
        } else {
            executeDeepSeekStreamRequest(prompt, session, emitter, requestType);
        }
    }
    
    /**
     * 执行Ollama流式请求
     */
    private void executeOllamaStreamRequest(String prompt, SseEmitter emitter, String requestType) {
        String url = aiConfig.getOllama().getFullUrl();
        Map<String, Object> requestBody = buildOllamaRequestBody(prompt);
        
        WebClient webClient = buildOllamaWebClient(url);
        AtomicBoolean inThinkBlock = new AtomicBoolean(false);
        
        webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(
                        chunk -> {
                            System.out.println("[Ollama" + requestType + "原始流] " + chunk);
                            processOllamaChunk(chunk, emitter, inThinkBlock);
                        },
                        error -> {
                            System.err.println("Ollama" + requestType + " API 错误: " + error.getMessage());
                            handleOllamaError(error, emitter);
                        },
                        () -> completeEmitter(emitter)
                );
    }
    
    /**
     * 执行DeepSeek流式请求
     */
    private void executeDeepSeekStreamRequest(String prompt, HttpSession session, SseEmitter emitter, 
                                            String requestType) {
        String apiKey = (String) session.getAttribute("deepseekApiKey");
        
        if (apiKey == null || apiKey.isEmpty()) {
            handleMissingApiKey(emitter);
            return;
        }
        
        String url = aiConfig.getDeepseek().getFullUrl();
        Map<String, Object> requestBody = buildDeepSeekRequestBody(prompt);
        
        WebClient webClient = buildDeepSeekWebClient(url, apiKey);
        
        webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(
                        chunk -> {
                            System.out.println("[DeepSeek" + requestType + "原始流] " + chunk);
                            processDeepSeekChunk(chunk, emitter);
                        },
                        error -> {
                            System.err.println("DeepSeek" + requestType + " API 错误: " + error.getMessage());
                            handleDeepSeekError(error, emitter);
                        },
                        () -> completeEmitter(emitter)
                );
    }
    
    /**
     * 构建Ollama请求体
     */
    private Map<String, Object> buildOllamaRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getOllama().getModel());
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        return requestBody;
    }
    
    /**
     * 构建DeepSeek请求体
     */
    private Map<String, Object> buildDeepSeekRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getDeepseek().getModel());
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        return requestBody;
    }
    
    /**
     * 构建Ollama WebClient
     */
    private WebClient buildOllamaWebClient(String url) {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
    
    /**
     * 构建DeepSeek WebClient
     */
    private WebClient buildDeepSeekWebClient(String url, String apiKey) {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
    
    /**
     * 处理Ollama响应块
     */
    private void processOllamaChunk(String chunk, SseEmitter emitter, AtomicBoolean inThinkBlock) {
        try {
            JsonNode node = objectMapper.readTree(chunk);
            if (node.has("message")) {
                JsonNode messageNode = node.get("message");
                if (messageNode.has("content")) {
                    String content = messageNode.get("content").asText();
                    content = processThinkTags(content, inThinkBlock);
                    
                    if (!content.isEmpty()) {
                        emitter.send(content);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略解析异常，继续流
        }
    }
    
    /**
     * 处理DeepSeek响应块
     */
    private void processDeepSeekChunk(String chunk, SseEmitter emitter) {
        try {
            JsonNode node = objectMapper.readTree(chunk);
            if (node.has("choices")) {
                JsonNode choices = node.get("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode delta = choices.get(0).get("delta");
                    if (delta != null && delta.has("content")) {
                        String content = delta.get("content").asText();
                        emitter.send(content);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略解析异常，继续流
        }
    }
    
    /**
     * 处理<think>标签
     */
    private String processThinkTags(String content, AtomicBoolean inThinkBlock) {
        if (content.contains("<think>")) {
            inThinkBlock.set(true);
            content = content.replaceAll("<think>.*", "");
        }
        
        if (content.contains("</think>")) {
            inThinkBlock.set(false);
            content = content.replaceAll(".*</think>", "");
        }
        
        if (inThinkBlock.get()) {
            return "";
        }
        
        return content.replaceAll("\\n+", "\n").trim();
    }
    
    /**
     * 处理Ollama错误
     */
    private void handleOllamaError(Throwable error, SseEmitter emitter) {
        try {
            emitter.send("抱歉，本地Ollama服务未启动或连接失败。请确保Ollama已安装并运行在 " + aiConfig.getOllama().getBaseUrl());
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
    
    /**
     * 处理DeepSeek错误
     */
    private void handleDeepSeekError(Throwable error, SseEmitter emitter) {
        try {
            emitter.send("抱歉，请求失败，请检查API Key是否正确");
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
    
    /**
     * 处理缺失的API Key
     */
    private void handleMissingApiKey(SseEmitter emitter) {
        try {
            emitter.send("请先设置 DeepSeek API Key");
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
    
    /**
     * 完成Emitter
     */
    private void completeEmitter(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
    
    /**
     * 获取默认提供商
     */
    protected String getDefaultProvider(HttpSession session) {
        String provider = (String) session.getAttribute("aiProvider");
        return provider != null ? provider : "ollama";
    }
} 