package com.example.deepseek.controller;

import com.example.deepseek.service.WeatherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("weather", weather);
        model.addAttribute("city", city);
        return "weather";
    }
} 