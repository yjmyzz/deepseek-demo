package com.example.deepseek.controller;

import com.example.deepseek.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ChatController 单元测试
 * 作者：菩提树下的杨过
 * 感谢Cursor生成本项目代码
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
        // 基本设置
    }

    @Test
    void testChatPage() {
        // 测试聊天页面
        String result = chatController.chatPage();
        assert "chat".equals(result);
    }

    @Test
    void testStreamChat() {
        // 测试流式聊天
        String message = "Hello, how are you?";
        
        SseEmitter result = chatController.streamChat(message, session);
        assert result != null;
        verify(chatService).streamChat(message, session, result);
    }

    @Test
    void testStreamChatWithEmptyMessage() {
        // 测试空消息的流式聊天
        String message = "";
        
        SseEmitter result = chatController.streamChat(message, session);
        assert result != null;
        verify(chatService).streamChat(message, session, result);
    }

    @Test
    void testStreamChatWithNullMessage() {
        // 测试null消息的流式聊天
        String message = null;
        
        SseEmitter result = chatController.streamChat(message, session);
        assert result != null;
        verify(chatService).streamChat(message, session, result);
    }

    @Test
    void testStreamChatWithSpecialCharacters() {
        // 测试包含特殊字符的消息
        String message = "Hello! How are you? 你好！";
        
        SseEmitter result = chatController.streamChat(message, session);
        assert result != null;
        verify(chatService).streamChat(message, session, result);
    }
} 