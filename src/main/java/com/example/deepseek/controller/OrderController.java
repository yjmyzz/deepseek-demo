package com.example.deepseek.controller;

import com.example.deepseek.service.OrderService;
import com.example.deepseek.constant.AiConstants;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;

    @GetMapping("/order")
    public String orderPage() {
        return "order";
    }

    @PostMapping("/order")
    public String getOrderStatus(@RequestParam String orderNumber, HttpSession session, Model model) {
        String orderInfo = orderService.getOrderStatusViaMCP(orderNumber, session);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("orderNumber", orderNumber);
        return "order";
    }
    
    /**
     * 流式订单查询接口
     */
    @GetMapping(value = "/order/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void getOrderStatusStream(@RequestParam String orderNumber, 
                                   HttpSession session, 
                                   HttpServletResponse response) {
        try {
            log.info("开始流式订单查询: {}", orderNumber);
            
            // 设置SSE响应头
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            response.setHeader("Access-Control-Allow-Origin", "*");
            
            PrintWriter writer = response.getWriter();
            
            // 发送初始消息
            writer.write("data: 正在智能查询订单 " + orderNumber + " 的状态信息...\n\n");
            writer.flush();
            
            // 获取订单信息 - 使用MCP方式
            String orderInfo = orderService.getOrderStatusViaMCP(orderNumber, session);
            
            // 模拟流式输出
            String[] lines = orderInfo.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    writer.write("data: " + line + "\n\n");
                    writer.flush();
                    
                    // 添加小延迟模拟流式效果
                    Thread.sleep(100);
                }
            }
            
            // 发送完成信号
            writer.write("data: [DONE]\n\n");
            writer.flush();
            
            log.info("流式订单查询完成: {}", orderNumber);
            
        } catch (Exception e) {
            log.error("流式订单查询异常: {}", e.getMessage(), e);
            try {
                PrintWriter writer = response.getWriter();
                writer.write("data: 查询订单信息时出现错误，请稍后重试。\n\n");
                writer.write("data: [DONE]\n\n");
                writer.flush();
            } catch (IOException ex) {
                log.error("发送错误信息失败: {}", ex.getMessage());
            }
        }
    }
    
    /**
     * RESTful API - 根据订单号查询订单状态
     */
    @GetMapping("/api/order/{orderNumber}")
    @ResponseBody
    public OrderResponse getOrderStatusApi(@PathVariable String orderNumber) {
        log.info("API查询订单状态: {}", orderNumber);
        
        // 验证订单号格式（11位数字）
        if (!orderNumber.matches(AiConstants.Regex.ORDER_NUMBER)) {
            return new OrderResponse(false, "订单号格式错误，请输入11位数字", null);
        }
        
        try {
            OrderInfo orderInfo = orderService.getOrderStatus(orderNumber);
            return new OrderResponse(true, "查询成功", orderInfo);
        } catch (Exception e) {
            log.error("API订单查询异常: {}", e.getMessage(), e);
            return new OrderResponse(false, "查询失败：" + e.getMessage(), null);
        }
    }
    
    /**
     * 订单响应类
     */
    @Data
    @AllArgsConstructor
    public static class OrderResponse {
        private boolean success;
        private String message;
        private OrderInfo data;
    }
    
    /**
     * 订单信息类
     */
    @Data
    @AllArgsConstructor
    public static class OrderInfo {
        private String orderNumber;
        private String status;
        private String createTime;
        private String updateTime;
        private String customerName;
        private String productName;
        private double amount;
    }
} 