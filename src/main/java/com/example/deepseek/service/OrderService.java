package com.example.deepseek.service;

import com.example.deepseek.config.AiConfig;
import com.example.deepseek.controller.OrderController;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final AiConfig aiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    
    // 模拟订单数据库
    private static final Map<String, OrderController.OrderInfo> orderDatabase = new HashMap<>();
    private static final Random random = new Random();
    
    static {
        // 初始化一些模拟订单数据
        orderDatabase.put("12345678901", new OrderController.OrderInfo(
            "12345678901", AiConstants.OrderStatus.SHIPPED, "2024-01-15 10:30:00", "2024-01-16 14:20:00", 
            "张三", "iPhone 15 Pro", 8999.00));
        orderDatabase.put("12345678902", new OrderController.OrderInfo(
            "12345678902", AiConstants.OrderStatus.PENDING_PAYMENT, "2024-01-16 09:15:00", "2024-01-16 09:15:00", 
            "李四", "MacBook Air", 12999.00));
        orderDatabase.put("12345678903", new OrderController.OrderInfo(
            "12345678903", AiConstants.OrderStatus.COMPLETED, "2024-01-14 16:45:00", "2024-01-17 11:30:00", 
            "王五", "AirPods Pro", 1899.00));
        orderDatabase.put("12345678904", new OrderController.OrderInfo(
            "12345678904", AiConstants.OrderStatus.PROCESSING, "2024-01-17 13:20:00", "2024-01-17 15:45:00", 
            "赵六", "iPad Air", 4799.00));
        orderDatabase.put("12345678905", new OrderController.OrderInfo(
            "12345678905", AiConstants.OrderStatus.CANCELLED, "2024-01-16 20:10:00", "2024-01-17 09:30:00", 
            "钱七", "Apple Watch", 3299.00));
    }
    
    /**
     * 直接查询订单状态（模拟数据库查询）
     */
    public OrderController.OrderInfo getOrderStatus(String orderNumber) {
        log.info("查询订单状态: {}", orderNumber);
        
        // 验证订单号格式
        if (!orderNumber.matches(AiConstants.Regex.ORDER_NUMBER)) {
            throw new IllegalArgumentException("订单号格式错误，请输入11位数字");
        }
        
        // 模拟数据库查询延迟
        try {
            Thread.sleep(random.nextInt(500) + 200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        OrderController.OrderInfo orderInfo = orderDatabase.get(orderNumber);
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在，请检查订单号是否正确");
        }
        
        log.info("订单查询成功: {}", orderInfo.getStatus());
        return orderInfo;
    }
    
    /**
     * MCP方式获取订单信息 - 通过AI模型智能查询
     */
    public String getOrderStatusViaMCP(String orderNumber, HttpSession session) {
        try {
            // 获取AI提供商信息
            String provider = (String) session.getAttribute(AiConstants.Session.AI_PROVIDER);
            
            // 检查是否已选择AI提供商
            if (provider == null || provider.trim().isEmpty()) {
                return AiConstants.ErrorMessage.SELECT_PROVIDER_FIRST;
            }
            
            // 验证订单号格式
            if (!orderNumber.matches(AiConstants.Regex.ORDER_NUMBER)) {
                return "订单号格式错误，请输入11位数字。";
            }
            
            // 构建智能订单查询提示词，包含工具函数定义
            String prompt = String.format("""
                你是一个智能订单助手。请帮我查询订单号 %s 的状态信息。
                
                你可以使用以下工具函数来获取和处理订单数据：
                
                1. %s(orderNumber: string) -> object
                   描述：查询指定订单号的详细信息
                   参数：orderNumber - 11位数字订单号
                   返回：订单信息对象，包含状态、客户、商品、金额等
                   示例：%s("%s")
                
                2. %s(orderData: object) -> string
                   描述：将订单数据格式化为友好的显示文本
                   参数：orderData - 订单数据对象
                   返回：格式化的订单信息文本
                
                使用步骤：
                1. 调用 %s("%s") 获取订单数据
                2. 使用 %s(orderData) 格式化数据
                3. 以友好的方式向用户介绍订单详情
                
                请直接回答，不要解释查询过程。
                """, orderNumber, AiConstants.ToolFunction.QUERY_ORDER_API, orderNumber, orderNumber, 
                AiConstants.ToolFunction.FORMAT_ORDER_INFO, AiConstants.ToolFunction.QUERY_ORDER_API, 
                orderNumber, AiConstants.ToolFunction.FORMAT_ORDER_INFO);
            
            if (AiConstants.Provider.OLLAMA.equals(provider)) {
                // 使用Ollama进行智能查询
                return callOllamaForOrder(prompt, orderNumber, session);
            } else if (AiConstants.Provider.DEEPSEEK.equals(provider)) {
                // 使用DeepSeek进行智能查询
                return callDeepSeekForOrder(prompt, orderNumber, session);
            } else {
                return String.format(AiConstants.ErrorMessage.UNSUPPORTED_PROVIDER, provider);
            }
            
        } catch (Exception e) {
            log.error(AiConstants.LogMessage.TOOL_EXECUTION_EXCEPTION, e.getMessage(), e);
            // 如果MCP方式失败，回退到直接查询
            try {
                OrderController.OrderInfo orderInfo = getOrderStatus(orderNumber);
                return AiConstants.ErrorMessage.FALLBACK_TO_DIRECT_QUERY + formatOrderInfo(orderInfo);
            } catch (Exception ex) {
                return AiConstants.ErrorMessage.FALLBACK_TO_DIRECT_QUERY + ex.getMessage();
            }
        }
    }
    
    /**
     * 通过Ollama进行智能订单查询
     */
    private String callOllamaForOrder(String prompt, String orderNumber, HttpSession session) {
        try {
            String ollamaUrl = aiConfig.getOllama().getBaseUrl() + aiConfig.getOllama().getApiPath();
            
            // 构建Ollama请求，包含工具函数定义
            String systemPrompt = String.format("""
                你是一个智能订单助手。你可以使用以下工具函数：
                
                工具函数：
                1. %s(orderNumber: string) -> object
                   描述：查询指定订单号的详细信息
                   参数：orderNumber - 11位数字订单号
                   返回：订单信息对象，包含状态、客户、商品、金额等
                
                2. %s(orderData: object) -> string
                   描述：将订单数据格式化为友好的显示文本
                   参数：orderData - 订单数据对象
                   返回：格式化的订单信息文本
                
                使用示例：
                当用户询问订单时，你应该：
                1. 调用 %s(orderNumber) 获取订单数据
                2. 使用 %s(orderData) 格式化数据
                3. 以友好的方式向用户介绍订单详情
                
                请直接回答用户的问题，不要解释查询过程。
                """, AiConstants.ToolFunction.QUERY_ORDER_API, AiConstants.ToolFunction.FORMAT_ORDER_INFO,
                AiConstants.ToolFunction.QUERY_ORDER_API, AiConstants.ToolFunction.FORMAT_ORDER_INFO);
            
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
                // 解析Ollama响应
                String aiResponse = parseOllamaResponse(response.getBody());
                
                // 检查是否需要执行工具函数调用
                String finalResponse = processToolCalls(aiResponse, orderNumber);
                
                // 如果AI响应为空或没有实际内容，使用原始数据
                if (finalResponse == null || finalResponse.trim().isEmpty() || 
                    finalResponse.contains("无法解析") || finalResponse.contains("解析AI响应时出现错误")) {
                    return getDirectOrderInfo(orderNumber);
                }
                
                return finalResponse;
            } else {
                log.warn(AiConstants.LogMessage.OLLAMA_CALL_FAILED, response.getStatusCode());
                return AiConstants.ErrorMessage.SERVICE_UNAVAILABLE + getDirectOrderInfo(orderNumber);
            }
            
        } catch (Exception e) {
            log.error(AiConstants.LogMessage.OLLAMA_QUERY_EXCEPTION, e.getMessage(), e);
            return AiConstants.ErrorMessage.FALLBACK_TO_DIRECT_QUERY + getDirectOrderInfo(orderNumber);
        }
    }
    
    /**
     * 通过DeepSeek进行智能订单查询
     */
    private String callDeepSeekForOrder(String prompt, String orderNumber, HttpSession session) {
        try {
            String apiKey = (String) session.getAttribute(AiConstants.Session.DEEPSEEK_API_KEY);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return AiConstants.ErrorMessage.SET_DEEPSEEK_API_KEY;
            }
            
            String deepseekUrl = aiConfig.getDeepseek().getBaseUrl() + aiConfig.getDeepseek().getApiPath();
            
            // 构建DeepSeek请求，包含工具函数定义
            String systemPrompt = String.format("""
                你是一个智能订单助手。你可以使用以下工具函数：
                
                工具函数：
                1. %s(orderNumber: string) -> object
                   描述：查询指定订单号的详细信息
                   参数：orderNumber - 11位数字订单号
                   返回：订单信息对象，包含状态、客户、商品、金额等
                
                2. %s(orderData: object) -> string
                   描述：将订单数据格式化为友好的显示文本
                   参数：orderData - 订单数据对象
                   返回：格式化的订单信息文本
                
                使用示例：
                当用户询问订单时，你应该：
                1. 调用 %s(orderNumber) 获取订单数据
                2. 使用 %s(orderData) 格式化数据
                3. 以友好的方式向用户介绍订单详情
                
                请直接回答用户的问题，不要解释查询过程。
                """, AiConstants.ToolFunction.QUERY_ORDER_API, AiConstants.ToolFunction.FORMAT_ORDER_INFO,
                AiConstants.ToolFunction.QUERY_ORDER_API, AiConstants.ToolFunction.FORMAT_ORDER_INFO);
            
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
                // 解析DeepSeek响应
                String aiResponse = parseDeepSeekResponse(response.getBody());
                
                // 检查是否需要执行工具函数调用
                String finalResponse = processToolCalls(aiResponse, orderNumber);
                
                // 如果AI响应为空或没有实际内容，使用原始数据
                if (finalResponse == null || finalResponse.trim().isEmpty() || 
                    finalResponse.contains("无法解析") || finalResponse.contains("解析AI响应时出现错误")) {
                    return getDirectOrderInfo(orderNumber);
                }
                
                return finalResponse;
            } else {
                log.warn(AiConstants.LogMessage.DEEPSEEK_CALL_FAILED, response.getStatusCode());
                return AiConstants.ErrorMessage.SERVICE_UNAVAILABLE + getDirectOrderInfo(orderNumber);
            }
            
        } catch (Exception e) {
            log.error(AiConstants.LogMessage.DEEPSEEK_QUERY_EXCEPTION, e.getMessage(), e);
            return AiConstants.ErrorMessage.FALLBACK_TO_DIRECT_QUERY + getDirectOrderInfo(orderNumber);
        }
    }
    
    /**
     * 处理工具函数调用
     */
    private String processToolCalls(String aiResponse, String orderNumber) {
        try {
            // 检查AI响应中是否包含工具函数调用
            if (aiResponse.contains(AiConstants.ToolFunction.QUERY_ORDER_API) || 
                aiResponse.contains(AiConstants.ToolFunction.FORMAT_ORDER_INFO)) {
                log.info("检测到工具函数调用，执行工具函数");
                
                // 执行查询订单API
                OrderController.OrderInfo orderInfo = getOrderStatus(orderNumber);
                
                // 格式化订单信息
                String formattedInfo = formatOrderInfo(orderInfo);
                
                // 返回格式化后的信息
                return formattedInfo;
            }
            
            // 如果没有工具函数调用，直接返回AI响应
            return aiResponse;
            
        } catch (Exception e) {
            log.error(AiConstants.LogMessage.PROCESS_TOOL_CALLS_EXCEPTION, e.getMessage(), e);
            return "工具函数调用失败: " + e.getMessage();
        }
    }
    
    /**
     * 解析Ollama响应
     */
    private String parseOllamaResponse(String response) {
        try {
            // 简单的JSON解析，提取content字段
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
            // 简单的JSON解析，提取content字段
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
        // 移除可能的思考标签
        return content.replaceAll(AiConstants.Regex.THINK_TAGS, "").trim();
    }
    
    /**
     * 格式化订单信息 - 改为public以便MCP工具调用
     */
    public String formatOrderInfo(OrderController.OrderInfo orderInfo) {
        return String.format("""
            📦 订单详情
            
            🏷️ 订单号：%s
            📊 订单状态：%s
            👤 客户姓名：%s
            🛍️ 商品名称：%s
            💰 订单金额：¥%.2f
            📅 创建时间：%s
            🔄 更新时间：%s
            
            """, 
            orderInfo.getOrderNumber(),
            getStatusEmoji(orderInfo.getStatus()) + " " + orderInfo.getStatus(),
            orderInfo.getCustomerName(),
            orderInfo.getProductName(),
            orderInfo.getAmount(),
            orderInfo.getCreateTime(),
            orderInfo.getUpdateTime()
        );
    }
    
    /**
     * 获取状态对应的emoji
     */
    private String getStatusEmoji(String status) {
        switch (status) {
            case AiConstants.OrderStatus.PENDING_PAYMENT: return AiConstants.OrderStatusEmoji.PENDING_PAYMENT;
            case AiConstants.OrderStatus.PROCESSING: return AiConstants.OrderStatusEmoji.PROCESSING;
            case AiConstants.OrderStatus.SHIPPED: return AiConstants.OrderStatusEmoji.SHIPPED;
            case AiConstants.OrderStatus.COMPLETED: return AiConstants.OrderStatusEmoji.COMPLETED;
            case AiConstants.OrderStatus.CANCELLED: return AiConstants.OrderStatusEmoji.CANCELLED;
            default: return AiConstants.OrderStatusEmoji.UNKNOWN;
        }
    }
    
    /**
     * 直接获取订单信息（用于回退）
     */
    private String getDirectOrderInfo(String orderNumber) {
        try {
            OrderController.OrderInfo orderInfo = getOrderStatus(orderNumber);
            return formatOrderInfo(orderInfo);
        } catch (Exception e) {
            return "订单查询失败：" + e.getMessage();
        }
    }
} 