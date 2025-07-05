package com.example.deepseek.service;

import com.example.deepseek.config.AiConfig;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final AiConfig aiConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 直接调用wttr.in API获取天气信息（原有方式）
     */
    public String getWeather(String city, HttpSession session) {
        try {
            // 使用wttr.in API获取天气信息
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String weatherUrl = aiConfig.getWeather().getBaseUrl() + "/" + encodedCity + "?format=3";

            log.info("正在查询天气信息: {}", weatherUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "DeepSeek-Demo/1.0");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    weatherUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String weatherInfo = response.getBody().trim();
                log.info("天气查询成功: {}", weatherInfo);
                return formatWeatherInfo(city, weatherInfo);
            } else {
                log.warn("天气API返回错误状态码: {}", response.getStatusCode());
                return "抱歉，无法获取 " + city + " 的天气信息，请稍后重试。";
            }

        } catch (ResourceAccessException e) {
            log.error("天气API连接失败: {}", e.getMessage());
            return "抱歉，天气服务暂时不可用，请稍后重试。";
        } catch (Exception e) {
            log.error("天气查询异常: {}", e.getMessage(), e);
            return "抱歉，查询天气信息时出现错误，请稍后重试。";
        }
    }

    /**
     * 智能天气查询 - 通过AI模型进行天气查询
     */
    public String getWeatherViaAI(String city, HttpSession session) {
        try {
            // 获取AI提供商信息
            String provider = (String) session.getAttribute("aiProvider");

            // 检查是否已选择AI提供商
            if (provider == null || provider.trim().isEmpty()) {
                return "请先选择AI提供商（本地Ollama或远程DeepSeek）后再进行天气查询。";
            }

            // 构建智能天气查询提示词
            String prompt = String.format("""
                    你是一个智能天气助手。请帮我查询 %s 的天气信息。
                    
                    你可以通过以下方式获取天气数据：
                    - 访问 https://wttr.in/%s?format=3 获取实时天气信息
                    - 或者使用其他可靠的天气数据源
                    
                    请以友好的方式向我介绍 %s 的天气情况，包括温度、天气状况、湿度、风速等信息。
                    如果无法获取信息，请告诉我可能的原因。
                    
                    请直接回答，不要解释查询过程。
                    """, city, city, city);

            if ("ollama".equals(provider)) {
                // 使用Ollama进行智能查询
                return callOllamaForWeather(prompt, city, session);
            } else if ("deepseek".equals(provider)) {
                // 使用DeepSeek进行智能查询
                return callDeepSeekForWeather(prompt, city, session);
            } else {
                return "不支持的AI提供商：" + provider + "，请选择本地Ollama或远程DeepSeek。";
            }

        } catch (Exception e) {
            log.error("MCP天气查询异常: {}", e.getMessage(), e);
            // 如果MCP方式失败，回退到直接查询
            return "智能查询失败，正在使用直接查询方式...\n\n" + getWeather(city, session);
        }
    }

    /**
     * 通过Ollama进行智能天气查询
     */
    private String callOllamaForWeather(String prompt, String city, HttpSession session) {
        try {
            String ollamaUrl = aiConfig.getOllama().getBaseUrl() + aiConfig.getOllama().getApiPath();

            // 构建Ollama请求 - 修复JSON格式问题
            String requestBody = String.format(
                    "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"你是一个智能天气助手。当用户询问天气时，你需要：1. 访问wttr.in获取实时天气数据 2. 以友好的方式介绍天气情况 3. 如果无法获取数据，说明原因并建议重试。\"},{\"role\":\"user\",\"content\":\"%s\"}],\"stream\":false}",
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
                // 解析Ollama响应
                return parseOllamaResponse(response.getBody());
            } else {
                log.warn("Ollama调用失败: {}", response.getStatusCode());
                return "Ollama智能查询服务暂时不可用，请稍后重试。";
            }

        } catch (Exception e) {
            log.error("Ollama智能查询异常: {}", e.getMessage(), e);
            return "Ollama智能查询失败，请稍后重试。";
        }
    }

    /**
     * 通过DeepSeek进行智能天气查询
     */
    private String callDeepSeekForWeather(String prompt, String city, HttpSession session) {
        try {
            String apiKey = (String) session.getAttribute("deepseekApiKey");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return "请先设置DeepSeek API Key。";
            }

            String deepseekUrl = aiConfig.getDeepseek().getBaseUrl() + aiConfig.getDeepseek().getApiPath();

            // 构建DeepSeek请求 - 修复JSON格式问题
            String requestBody = String.format(
                    "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"你是一个智能天气助手。当用户询问天气时，你需要：1. 访问wttr.in获取实时天气数据 2. 以友好的方式介绍天气情况 3. 如果无法获取数据，说明原因并建议重试。\"},{\"role\":\"user\",\"content\":\"%s\"}],\"stream\":false}",
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
                // 解析DeepSeek响应
                return parseDeepSeekResponse(response.getBody());
            } else {
                log.warn("DeepSeek调用失败: {}", response.getStatusCode());
                return "DeepSeek智能查询服务暂时不可用，请稍后重试。";
            }

        } catch (Exception e) {
            log.error("DeepSeek智能查询异常: {}", e.getMessage(), e);
            return "DeepSeek智能查询失败，请稍后重试。";
        }
    }

    /**
     * 解析Ollama响应
     */
    private String parseOllamaResponse(String response) {
        try {
            log.info("Ollama响应: {}", response);

            // 简单的JSON解析，提取message内容
            if (response.contains("\"message\"")) {
                int start = response.indexOf("\"content\":\"") + 11;
                int end = response.indexOf("\"", start);
                if (start > 10 && end > start) {
                    String content = response.substring(start, end);
                    // 解码转义字符
                    content = content.replace("\\n", "\n").replace("\\\"", "\"");
                    // 过滤<think>标签及其内容
                    content = filterThinkTags(content);
                    return content;
                }
            }

            // 如果无法解析，返回原始响应
            return "AI助手回复：\n" + response;

        } catch (Exception e) {
            log.error("解析Ollama响应失败: {}", e.getMessage());
            return "解析AI响应时出现错误。";
        }
    }

    /**
     * 解析DeepSeek响应
     */
    private String parseDeepSeekResponse(String response) {
        try {
            log.info("DeepSeek响应: {}", response);

            // 简单的JSON解析，提取message内容
            if (response.contains("\"message\"")) {
                int start = response.indexOf("\"content\":\"") + 11;
                int end = response.indexOf("\"", start);
                if (start > 10 && end > start) {
                    String content = response.substring(start, end);
                    // 解码转义字符
                    content = content.replace("\\n", "\n").replace("\\\"", "\"");
                    // 过滤<think>标签及其内容
                    content = filterThinkTags(content);
                    return content;
                }
            }

            // 如果无法解析，返回原始响应
            return "AI助手回复：\n" + response;

        } catch (Exception e) {
            log.error("解析DeepSeek响应失败: {}", e.getMessage());
            return "解析AI响应时出现错误。";
        }
    }

    /**
     * 过滤<think>标签及其内容
     */
    private String filterThinkTags(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // 先处理Unicode转义的标签
        content = content.replace("\\u003cthink\\u003e", "<think>");
        content = content.replace("\\u003c/think\\u003e", "</think>");

        StringBuilder result = new StringBuilder();
        int start = 0;

        while (true) {
            // 查找<think>标签的开始位置
            int thinkStart = content.indexOf("<think>", start);
            if (thinkStart == -1) {
                // 没有找到<think>标签，添加剩余内容
                result.append(content.substring(start));
                break;
            }

            // 添加<think>标签前的内容
            result.append(content.substring(start, thinkStart));

            // 查找</think>标签的结束位置
            int thinkEnd = content.indexOf("</think>", thinkStart);
            if (thinkEnd == -1) {
                // 没有找到结束标签，跳过这个<think>标签
                start = thinkStart + 7;
                continue;
            }

            // 跳过整个<think>...</think>块
            start = thinkEnd + 8;
        }

        return result.toString().trim();
    }

    /**
     * 格式化天气信息
     */
    private String formatWeatherInfo(String city, String rawWeatherInfo) {
        StringBuilder formattedInfo = new StringBuilder();
        formattedInfo.append("🌤️ ").append(city).append(" 天气信息\n\n");

        // 解析wttr.in返回的格式3数据
        String[] lines = rawWeatherInfo.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            // 解码URL编码的字符
            String decodedLine = decodeUrlEncodedText(line.trim());

            // 添加emoji和格式化
            String formattedLine = decodedLine;
            if (formattedLine.contains("°C")) {
                formattedLine = "🌡️ " + formattedLine;
            } else if (formattedLine.contains("km/h")) {
                formattedLine = "💨 " + formattedLine;
            } else if (formattedLine.contains("%")) {
                formattedLine = "💧 " + formattedLine;
            } else if (formattedLine.contains("hPa")) {
                formattedLine = "📊 " + formattedLine;
            }

            formattedInfo.append(formattedLine).append("\n");
        }

        return formattedInfo.toString();
    }

    /**
     * 解码URL编码的文本
     */
    private String decodeUrlEncodedText(String text) {
        try {
            // 查找并解码URL编码的部分
            StringBuilder result = new StringBuilder();
            int start = 0;

            while (true) {
                int percentIndex = text.indexOf('%', start);
                if (percentIndex == -1) {
                    // 没有更多编码字符，添加剩余部分
                    result.append(text.substring(start));
                    break;
                }

                // 添加编码前的部分
                result.append(text.substring(start, percentIndex));

                // 查找编码序列的结束位置
                int end = percentIndex;
                while (end < text.length() && end < percentIndex + 9 &&
                        text.charAt(end) == '%' ||
                        (end > percentIndex && Character.isLetterOrDigit(text.charAt(end)))) {
                    end++;
                }

                // 尝试解码
                try {
                    String encodedPart = text.substring(percentIndex, end);
                    String decodedPart = URLDecoder.decode(encodedPart, StandardCharsets.UTF_8);
                    result.append(decodedPart);
                } catch (Exception e) {
                    // 解码失败，保持原样
                    result.append(text.substring(percentIndex, end));
                }

                start = end;
            }

            return result.toString();
        } catch (Exception e) {
            log.warn("URL解码失败，返回原文本: {}", text);
            return text;
        }
    }
} 
 