package com.example.deepseek.controller;

import com.example.deepseek.service.WeatherService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WeatherController {
    
    private final WeatherService weatherService;

    @GetMapping("/weather")
    public String weatherPage() {
        return "weather";
    }

    @PostMapping("/weather")
    public String getWeather(@RequestParam String city, HttpSession session, Model model) {
        String weather = weatherService.getWeather(city, session);
        model.addAttribute("weatherInfo", weather);
        model.addAttribute("city", city);
        return "weather";
    }
    
    /**
     * 流式天气查询接口
     */
    @GetMapping(value = "/weather/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void getWeatherStream(@RequestParam String city, 
                                HttpSession session, 
                                HttpServletResponse response) {
        try {
            log.info("开始流式天气查询: {}", city);
            
            // 设置SSE响应头
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            response.setHeader("Access-Control-Allow-Origin", "*");
            
            PrintWriter writer = response.getWriter();
            
            // 发送初始消息
            writer.write("data: 正在查询 " + city + " 的天气信息...\n\n");
            writer.flush();
            
            // 获取天气信息
            String weatherInfo = weatherService.getWeather(city, session);
            
            // 模拟流式输出
            String[] lines = weatherInfo.split("\n");
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
            
            log.info("流式天气查询完成: {}", city);
            
        } catch (Exception e) {
            log.error("流式天气查询异常: {}", e.getMessage(), e);
            try {
                PrintWriter writer = response.getWriter();
                writer.write("data: 查询天气信息时出现错误，请稍后重试。\n\n");
                writer.write("data: [DONE]\n\n");
                writer.flush();
            } catch (IOException ex) {
                log.error("发送错误信息失败: {}", ex.getMessage());
            }
        }
    }
} 