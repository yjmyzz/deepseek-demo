package com.example.deepseek.controller;

import com.example.deepseek.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WeatherController 单元测试
 * 作者：菩提树下的杨过
 * 感谢Cursor生成本项目代码
 */
@ExtendWith(MockitoExtension.class)
class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private WeatherController weatherController;

    @BeforeEach
    void setUp() {
        // 基本设置
    }

    @Test
    void testWeatherPage() {
        // 测试天气页面
        String result = weatherController.weatherPage();
        assert "weather".equals(result);
    }

    @Test
    void testGetWeather() {
        // 测试获取天气信息
        String city = "Beijing";
        String expectedResult = "模拟天气：Beijing 晴 25°C";
        
        when(weatherService.getWeather(city, session)).thenReturn(expectedResult);
        
        String result = weatherController.getWeather(city, session, model);
        assert "weather".equals(result);
        verify(weatherService).getWeather(city, session);
        verify(model).addAttribute("weather", expectedResult);
        verify(model).addAttribute("city", city);
    }

    @Test
    void testGetWeatherWithEmptyCity() {
        // 测试空城市名的天气查询
        String city = "";
        String expectedResult = "模拟天气： 晴 25°C";
        
        when(weatherService.getWeather(city, session)).thenReturn(expectedResult);
        
        String result = weatherController.getWeather(city, session, model);
        assert "weather".equals(result);
        verify(weatherService).getWeather(city, session);
        verify(model).addAttribute("weather", expectedResult);
        verify(model).addAttribute("city", city);
    }

    @Test
    void testGetWeatherWithNullCity() {
        // 测试null城市名的天气查询
        String city = null;
        String expectedResult = "模拟天气：null 晴 25°C";
        
        when(weatherService.getWeather(city, session)).thenReturn(expectedResult);
        
        String result = weatherController.getWeather(city, session, model);
        assert "weather".equals(result);
        verify(weatherService).getWeather(city, session);
        verify(model).addAttribute("weather", expectedResult);
        verify(model).addAttribute("city", city);
    }

    @Test
    void testGetWeatherWithChineseCity() {
        // 测试中文城市名的天气查询
        String city = "上海";
        String expectedResult = "模拟天气：上海 晴 25°C";
        
        when(weatherService.getWeather(city, session)).thenReturn(expectedResult);
        
        String result = weatherController.getWeather(city, session, model);
        assert "weather".equals(result);
        verify(weatherService).getWeather(city, session);
        verify(model).addAttribute("weather", expectedResult);
        verify(model).addAttribute("city", city);
    }

    @Test
    void testGetWeatherWithEnglishCity() {
        // 测试英文城市名的天气查询
        String city = "New York";
        String expectedResult = "模拟天气：New York 晴 25°C";
        
        when(weatherService.getWeather(city, session)).thenReturn(expectedResult);
        
        String result = weatherController.getWeather(city, session, model);
        assert "weather".equals(result);
        verify(weatherService).getWeather(city, session);
        verify(model).addAttribute("weather", expectedResult);
        verify(model).addAttribute("city", city);
    }

    @Test
    void testGetWeatherWithSpecialCharacters() {
        // 测试包含特殊字符的城市名
        String city = "São Paulo";
        String expectedResult = "模拟天气：São Paulo 晴 25°C";
        
        when(weatherService.getWeather(city, session)).thenReturn(expectedResult);
        
        String result = weatherController.getWeather(city, session, model);
        assert "weather".equals(result);
        verify(weatherService).getWeather(city, session);
        verify(model).addAttribute("weather", expectedResult);
        verify(model).addAttribute("city", city);
    }
} 