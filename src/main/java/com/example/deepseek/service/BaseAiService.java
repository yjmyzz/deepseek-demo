package com.example.deepseek.service;

import com.example.deepseek.config.AiConfig;
import com.example.deepseek.constant.AiConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaseAiService {
    
    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    /**
     * 执行流式请求
     */
    public void executeStreamRequest(String prompt, HttpSession session, SseEmitter emitter) {
        String provider = getProviderFromSession(session);
        
        if (AiConstants.Provider.OLLAMA.equals(provider)) {
            executeOllamaStreamRequest(prompt, session, emitter);
        } else if (AiConstants.Provider.DEEPSEEK.equals(provider)) {
            executeDeepSeekStreamRequest(prompt, session, emitter);
        } else {
            sendError(emitter, "不支持的AI提供商: " + provider);
        }
    }
    
    /**
     * 从Session获取提供商
     */
    private String getProviderFromSession(HttpSession session) {
        String provider = (String) session.getAttribute(AiConstants.Session.AI_PROVIDER);
        if (provider == null || provider.trim().isEmpty()) {
            provider = AiConstants.Provider.OLLAMA; // 默认使用本地Ollama
        }
        return provider;
    }
    
    /**
     * 执行Ollama流式请求
     */
    private void executeOllamaStreamRequest(String prompt, HttpSession session, SseEmitter emitter) {
        executorService.submit(() -> {
            try {
                String ollamaUrl = aiConfig.getOllama().getBaseUrl() + aiConfig.getOllama().getApiPath();
                
                String requestBody = String.format(
                    "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"stream\":true}",
                    aiConfig.getOllama().getModel(),
                    prompt.replace("\"", "\\\"").replace("\n", "\\n")
                );
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                
                HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
                
                ResponseEntity<String> response = restTemplate.exchange(
                    ollamaUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    processOllamaStreamResponse(response.getBody(), emitter);
                } else {
                    sendError(emitter, "Ollama服务调用失败");
                }
                
            } catch (Exception e) {
                log.error("Ollama流式请求异常: {}", e.getMessage(), e);
                sendError(emitter, "Ollama服务异常: " + e.getMessage());
            }
        });
    }
    
    /**
     * 执行DeepSeek流式请求
     */
    private void executeDeepSeekStreamRequest(String prompt, HttpSession session, SseEmitter emitter) {
        executorService.submit(() -> {
            try {
                String apiKey = (String) session.getAttribute(AiConstants.Session.DEEPSEEK_API_KEY);
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    sendError(emitter, AiConstants.ErrorMessage.SET_DEEPSEEK_API_KEY);
                    return;
                }
                
                String deepseekUrl = aiConfig.getDeepseek().getBaseUrl() + aiConfig.getDeepseek().getApiPath();
                
                String requestBody = String.format(
                    "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"stream\":true}",
                    aiConfig.getDeepseek().getModel(),
                    prompt.replace("\"", "\\\"").replace("\n", "\\n")
                );
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("Authorization", "Bearer " + apiKey);
                
                HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
                
                ResponseEntity<String> response = restTemplate.exchange(
                    deepseekUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    processDeepSeekStreamResponse(response.getBody(), emitter);
                } else {
                    sendError(emitter, "DeepSeek服务调用失败");
                }
                
            } catch (Exception e) {
                log.error("DeepSeek流式请求异常: {}", e.getMessage(), e);
                sendError(emitter, "DeepSeek服务异常: " + e.getMessage());
            }
        });
    }
    
    /**
     * 处理Ollama流式响应
     */
    private void processOllamaStreamResponse(String response, SseEmitter emitter) {
        try {
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if ("[DONE]".equals(data)) {
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        break;
                    }
                    
                    // 解析JSON数据
                    if (data.contains("\"content\":")) {
                        String content = extractContent(data);
                        if (content != null && !content.trim().isEmpty()) {
                            emitter.send(SseEmitter.event().data(content));
                        }
                    }
                }
            }
            emitter.complete();
        } catch (Exception e) {
            log.error("处理Ollama流式响应异常: {}", e.getMessage(), e);
            sendError(emitter, "处理响应异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理DeepSeek流式响应
     */
    private void processDeepSeekStreamResponse(String response, SseEmitter emitter) {
        try {
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if ("[DONE]".equals(data)) {
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        break;
                    }
                    
                    // 解析JSON数据
                    if (data.contains("\"content\":")) {
                        String content = extractContent(data);
                        if (content != null && !content.trim().isEmpty()) {
                            emitter.send(SseEmitter.event().data(content));
                        }
                    }
                }
            }
            emitter.complete();
        } catch (Exception e) {
            log.error("处理DeepSeek流式响应异常: {}", e.getMessage(), e);
            sendError(emitter, "处理响应异常: " + e.getMessage());
        }
    }
    
    /**
     * 提取内容字段
     */
    private String extractContent(String jsonData) {
        try {
            if (jsonData.contains(AiConstants.ResponseParse.CONTENT_FIELD)) {
                int startIndex = jsonData.indexOf(AiConstants.ResponseParse.CONTENT_FIELD) + 11;
                int endIndex = jsonData.indexOf("\"", startIndex);
                if (endIndex == -1) {
                    endIndex = jsonData.length() - 1;
                }
                String content = jsonData.substring(startIndex, endIndex);
                return filterThinkTags(content.replace("\\n", "\n").replace("\\\"", "\""));
            }
        } catch (Exception e) {
            log.error("提取内容异常: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 过滤思考标签
     */
    private String filterThinkTags(String content) {
        return content.replaceAll(AiConstants.Regex.THINK_TAGS, "").trim();
    }
    
    /**
     * 发送错误信息
     */
    private void sendError(SseEmitter emitter, String errorMessage) {
        try {
            emitter.send(SseEmitter.event().data("错误: " + errorMessage));
            emitter.complete();
        } catch (IOException e) {
            log.error("发送错误信息失败: {}", e.getMessage());
        }
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
        return provider; // 返回null如果用户未选择
    }
} 