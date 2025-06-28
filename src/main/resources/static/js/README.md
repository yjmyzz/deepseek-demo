# AI流式处理通用库使用指南

## 概述

`ai-stream.js` 是一个通用的AI流式处理JavaScript库，用于简化Ollama和DeepSeek的流式交互逻辑。该库抽象了常见的流式处理模式，提供了统一的API接口。

## 特性

- 🚀 **统一API**: 为聊天、翻译、天气查询等功能提供统一的流式处理接口
- 🔧 **高度可配置**: 支持自定义端点、UI元素、按钮文本、状态文本等
- 🎯 **智能空格处理**: 自动处理英文单词间的空格问题
- ⚡ **自动滚动**: 支持自动滚动到底部
- 🛡️ **错误处理**: 完善的错误处理和超时机制
- 📱 **响应式设计**: 支持移动端和桌面端
- 🔄 **状态管理**: 自动管理UI状态（按钮禁用/启用、状态显示等）

## 快速开始

### 1. 引入库文件

```html
<script src="/js/ai-stream.js"></script>
```

### 2. 创建处理器实例

```javascript
const aiHandler = new AIStreamHandler({
    // 基本配置
    endpoint: '/chat/stream',
    messageParam: 'message',
    
    // UI元素配置
    sendButton: document.getElementById('sendBtn'),
    statusElement: document.getElementById('status'),
    resultContainer: document.getElementById('resultContainer'),
    resultContent: document.getElementById('resultContent'),
    
    // 自定义回调
    onMessage: function(data, handler) {
        // 自定义消息处理逻辑
    },
    onError: function(event, handler) {
        // 自定义错误处理逻辑
    }
});
```

### 3. 开始流式处理

```javascript
// 聊天功能
aiHandler.startStream({ message: '你好' });

// 翻译功能
aiHandler.startStream({ 
    text: 'Hello world', 
    targetLang: 'zh' 
});

// 天气查询
aiHandler.startStream({ city: '北京' });
```

## 配置选项

### 基本配置

| 选项 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `endpoint` | string | '/chat/stream' | 流式处理端点 |
| `messageParam` | string | 'message' | 消息参数名 |
| `timeout` | number | 30000 | 超时时间（毫秒） |

### UI元素配置

| 选项 | 类型 | 说明 |
|------|------|------|
| `sendButton` | HTMLElement | 发送按钮元素 |
| `statusElement` | HTMLElement | 状态显示元素 |
| `typingIndicator` | HTMLElement | 输入指示器元素 |
| `resultContainer` | HTMLElement | 结果容器元素 |
| `resultContent` | HTMLElement | 结果内容元素 |
| `scrollContainer` | HTMLElement | 滚动容器元素 |

### 按钮文本配置

```javascript
buttonTexts: {
    default: '发送',
    loading: '发送中...',
    translate: '🔄 开始翻译',
    translating: '翻译中...',
    query: '🔍 查询天气',
    querying: '查询中...'
}
```

### 状态文本配置

```javascript
statusTexts: {
    connecting: '正在连接...',
    connected: '连接成功，正在接收回复...',
    error: '连接错误，请重试',
    complete: '处理完成'
}
```

## 回调函数

### onMessage(data, handler)
处理接收到的消息数据。

```javascript
onMessage: function(data, handler) {
    // data: 接收到的消息内容
    // handler: AIStreamHandler实例
    console.log('收到消息:', data);
}
```

### onError(event, handler)
处理错误事件。

```javascript
onError: function(event, handler) {
    // event: 错误事件对象
    // handler: AIStreamHandler实例
    console.error('发生错误:', event);
}
```

### onStart(params)
流式处理开始时的回调。

```javascript
onStart: function(params) {
    // params: 请求参数
    console.log('开始处理:', params);
}
```

### onEnd()
流式处理结束时的回调。

```javascript
onEnd: function() {
    console.log('处理结束');
}
```

## 工具函数

### AIUtils.handleKeyPress(event, callback)
处理键盘事件，支持Enter键触发回调。

```javascript
<input onkeypress="handleKeyPress(event, sendMessage)">
```

### AIUtils.getInputValue(inputId)
获取输入框的值。

```javascript
const message = AIUtils.getInputValue('messageInput');
```

### AIUtils.clearInput(inputId)
清空输入框。

```javascript
AIUtils.clearInput('messageInput');
```

### AIUtils.showResultContainer(containerId, contentId)
显示结果容器。

```javascript
AIUtils.showResultContainer('resultContainer', 'resultContent');
```

### AIUtils.updateProviderBadge(badgeId)
更新AI提供商徽章。

```javascript
AIUtils.updateProviderBadge('providerBadge');
```

## 使用示例

### 聊天功能

```javascript
const chatHandler = new AIStreamHandler({
    endpoint: '/chat/stream',
    messageParam: 'message',
    sendButton: document.getElementById('sendBtn'),
    statusElement: document.getElementById('status'),
    typingIndicator: document.getElementById('typingIndicator'),
    enableMessageCounter: true,
    
    onMessage: function(data, handler) {
        const messageId = handler.getCurrentMessageId();
        const aiResponse = document.getElementById(messageId);
        if (aiResponse) {
            aiResponse.innerHTML += data;
        }
    }
});

function sendMessage() {
    const message = AIUtils.getInputValue('messageInput');
    if (message) {
        chatHandler.addMessage(message, 'user');
        AIUtils.clearInput('messageInput');
        chatHandler.startStream({ message: message });
    }
}
```

### 翻译功能

```javascript
const translateHandler = new AIStreamHandler({
    endpoint: '/translate/stream',
    sendButton: document.getElementById('translateBtn'),
    resultContainer: document.getElementById('resultContainer'),
    resultContent: document.getElementById('resultContent'),
    
    buttonTexts: {
        default: '🔄 开始翻译',
        loading: '翻译中...'
    }
});

function startTranslate() {
    const text = AIUtils.getInputValue('textInput');
    const targetLang = document.getElementById('targetLangSelect').value;
    
    if (text) {
        AIUtils.showResultContainer('resultContainer', 'resultContent');
        translateHandler.startStream({ 
            text: text, 
            targetLang: targetLang 
        });
    }
}
```

## 最佳实践

1. **错误处理**: 始终提供 `onError` 回调来处理错误情况
2. **状态管理**: 使用库提供的状态管理功能，避免手动管理UI状态
3. **资源清理**: 在页面卸载时调用 `destroy()` 方法清理资源
4. **用户体验**: 提供适当的加载状态和错误提示
5. **性能优化**: 避免在 `onMessage` 回调中执行耗时操作

## 注意事项

- 确保在页面加载完成后再创建处理器实例
- 流式处理期间避免重复调用 `startStream()`
- 注意处理网络连接中断的情况
- 合理设置超时时间，避免长时间等待

## 更新日志

### v1.0.0
- 初始版本发布
- 支持聊天、翻译、天气查询功能
- 提供统一的流式处理API
- 包含完整的错误处理和状态管理

---

**感谢Cursor生成本项目代码！** 🚀 