package com.example.deepseek.service;

import com.example.deepseek.config.AiConfig;
import com.example.deepseek.constant.AiConstants;
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
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    
    private final AiConfig aiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Random random = new Random();
    
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
     * 通过AI智能查询天气信息
     */
    public String getWeatherViaAI(String city, HttpSession session) {
        try {
            // 获取AI提供商信息
            String provider = (String) session.getAttribute(AiConstants.Session.AI_PROVIDER);
            
            // 检查是否已选择AI提供商
            if (provider == null || provider.trim().isEmpty()) {
                return AiConstants.ErrorMessage.SELECT_PROVIDER_FIRST;
            }
            
            // 构建智能天气查询提示词
            String prompt = String.format("""
                你是一个智能天气助手。请帮我查询 %s 的天气信息。
                
                请提供以下信息：
                1. 当前天气状况
                2. 温度范围
                3. 湿度
                4. 风力
                5. 未来几天的天气预报
                6. 出行建议
                
                请以友好的方式回答，并包含相关的emoji表情。
                """, city);
            
            if (AiConstants.Provider.OLLAMA.equals(provider)) {
                // 使用Ollama进行智能查询
                return callOllamaForWeather(prompt, city, session);
            } else if (AiConstants.Provider.DEEPSEEK.equals(provider)) {
                // 使用DeepSeek进行智能查询
                return callDeepSeekForWeather(prompt, city, session);
            } else {
                return String.format(AiConstants.ErrorMessage.UNSUPPORTED_PROVIDER, provider);
            }
            
        } catch (Exception e) {
            log.error("AI天气查询异常: {}", e.getMessage(), e);
            return "智能天气查询失败，请稍后重试。";
        }
    }
    
    /**
     * 通过Ollama进行智能天气查询
     */
    private String callOllamaForWeather(String prompt, String city, HttpSession session) {
        try {
            String ollamaUrl = aiConfig.getOllama().getBaseUrl() + aiConfig.getOllama().getApiPath();
            
            String systemPrompt = """
                你是一个智能天气助手。请根据用户提供的城市信息，提供详细的天气信息和建议。
                请包含当前天气状况、温度、湿度、风力、未来天气预报和出行建议。
                使用友好的语言和相关的emoji表情。
                """;
            
            String requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"%s\"},{\"role\":\"user\",\"content\":\"%s\"}],\"stream\":false}",
                aiConfig.getOllama().getModel(), 
                systemPrompt.replace("\"", "\\\"").replace("\n", "\\n"),
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
                return parseOllamaResponse(response.getBody());
            } else {
                log.warn(AiConstants.LogMessage.OLLAMA_CALL_FAILED, response.getStatusCode());
                return "天气查询服务暂时不可用，请稍后重试。";
            }
            
        } catch (Exception e) {
            log.error(AiConstants.LogMessage.OLLAMA_QUERY_EXCEPTION, e.getMessage(), e);
            return "天气查询失败，请稍后重试。";
        }
    }
    
    /**
     * 通过DeepSeek进行智能天气查询
     */
    private String callDeepSeekForWeather(String prompt, String city, HttpSession session) {
        try {
            String apiKey = (String) session.getAttribute(AiConstants.Session.DEEPSEEK_API_KEY);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return AiConstants.ErrorMessage.SET_DEEPSEEK_API_KEY;
            }
            
            String deepseekUrl = aiConfig.getDeepseek().getBaseUrl() + aiConfig.getDeepseek().getApiPath();
            
            String systemPrompt = """
                你是一个智能天气助手。请根据用户提供的城市信息，提供详细的天气信息和建议。
                请包含当前天气状况、温度、湿度、风力、未来天气预报和出行建议。
                使用友好的语言和相关的emoji表情。
                """;
            
            String requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"%s\"},{\"role\":\"user\",\"content\":\"%s\"}],\"stream\":false}",
                aiConfig.getDeepseek().getModel(), 
                systemPrompt.replace("\"", "\\\"").replace("\n", "\\n"),
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
                return parseDeepSeekResponse(response.getBody());
            } else {
                log.warn(AiConstants.LogMessage.DEEPSEEK_CALL_FAILED, response.getStatusCode());
                return "天气查询服务暂时不可用，请稍后重试。";
            }
            
        } catch (Exception e) {
            log.error(AiConstants.LogMessage.DEEPSEEK_QUERY_EXCEPTION, e.getMessage(), e);
            return "天气查询失败，请稍后重试。";
        }
    }
    
    /**
     * 解析Ollama响应
     */
    private String parseOllamaResponse(String response) {
        try {
            if (response.contains(AiConstants.ResponseParse.CONTENT_FIELD)) {
                int startIndex = response.indexOf(AiConstants.ResponseParse.CONTENT_FIELD) + 11;
                int endIndex = response.indexOf("\"", startIndex);
                if (endIndex == -1) {
                    endIndex = response.length() - 1;
                }
                String content = response.substring(startIndex, endIndex);
                return filterThinkTags(content.replace("\\n", "\n").replace("\\\"", "\""));
            }
            return AiConstants.ResponseParse.CANNOT_PARSE_RESPONSE;
        } catch (Exception e) {
            log.error(AiConstants.LogMessage.PARSE_OLLAMA_RESPONSE_FAILED, e.getMessage());
            return AiConstants.ResponseParse.PARSE_ERROR;
        }
    }
    
    /**
     * 解析DeepSeek响应
     */
    private String parseDeepSeekResponse(String response) {
        try {
            if (response.contains(AiConstants.ResponseParse.CONTENT_FIELD)) {
                int startIndex = response.indexOf(AiConstants.ResponseParse.CONTENT_FIELD) + 11;
                int endIndex = response.indexOf("\"", startIndex);
                if (endIndex == -1) {
                    endIndex = response.length() - 1;
                }
                String content = response.substring(startIndex, endIndex);
                return filterThinkTags(content.replace("\\n", "\n").replace("\\\"", "\""));
            }
            return AiConstants.ResponseParse.CANNOT_PARSE_RESPONSE;
        } catch (Exception e) {
            log.error(AiConstants.LogMessage.PARSE_DEEPSEEK_RESPONSE_FAILED, e.getMessage());
            return AiConstants.ResponseParse.PARSE_ERROR;
        }
    }
    
    /**
     * 过滤思考标签
     */
    private String filterThinkTags(String content) {
        return content.replaceAll(AiConstants.Regex.THINK_TAGS, "").trim();
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
 