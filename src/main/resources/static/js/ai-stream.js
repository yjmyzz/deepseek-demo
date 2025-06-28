/**
 * AI流式处理通用库
 * 支持Ollama和DeepSeek的流式交互
 * 作者：菩提树下的杨过
 * 感谢Cursor生成本项目代码
 */

class AIStreamHandler {
    constructor(options = {}) {
        this.options = {
            // 默认配置
            endpoint: '/chat/stream',
            messageParam: 'message',
            onMessage: null,
            onError: null,
            onComplete: null,
            onStart: null,
            onEnd: null,
            // UI元素配置
            sendButton: null,
            statusElement: null,
            typingIndicator: null,
            resultContainer: null,
            resultContent: null,
            // 按钮状态配置
            buttonTexts: {
                default: '发送',
                loading: '发送中...',
                translate: '🔄 开始翻译',
                translating: '翻译中...',
                query: '🔍 查询天气',
                querying: '查询中...'
            },
            // 状态文本配置
            statusTexts: {
                connecting: '正在连接...',
                connected: '连接成功，正在接收回复...',
                error: '连接错误，请重试',
                complete: '处理完成'
            },
            // 自动滚动配置
            autoScroll: true,
            scrollContainer: null,
            // 消息处理配置
            messageCounter: 0,
            enableMessageCounter: false,
            // 空格处理配置
            smartSpaceHandling: true,
            // 错误处理配置
            showErrorInResult: true,
            errorMessage: '处理失败，请重试',
            // 超时配置
            timeout: 30000,
            ...options
        };
        
        this.eventSource = null;
        this.isStreaming = false;
        this.timeoutId = null;
        this.currentMessageId = null;
    }

    /**
     * 开始流式处理
     * @param {Object} params - 请求参数
     * @param {string} params.message - 消息内容（聊天）
     * @param {string} params.text - 文本内容（翻译）
     * @param {string} params.targetLang - 目标语言（翻译）
     * @param {string} params.city - 城市名称（天气）
     * @param {string} customEndpoint - 自定义端点
     */
    startStream(params, customEndpoint = null) {
        if (this.isStreaming) {
            console.log('正在流式传输中，忽略新请求');
            return;
        }

        console.log('开始流式传输:', params);
        
        this.isStreaming = true;
        this.updateUIState('loading');
        
        // 创建消息容器（如果需要）
        if (this.options.enableMessageCounter) {
            this.createMessageContainer();
        }
        
        // 关闭之前的连接
        this.closeConnection();
        
        // 构建请求URL
        const endpoint = customEndpoint || this.options.endpoint;
        const url = this.buildRequestUrl(endpoint, params);
        console.log('创建EventSource:', url);
        
        // 创建EventSource
        this.eventSource = new EventSource(url);
        this.setupEventListeners();
        
        // 设置超时
        this.setupTimeout();
        
        // 调用开始回调
        if (this.options.onStart) {
            this.options.onStart(params);
        }
    }

    /**
     * 构建请求URL
     */
    buildRequestUrl(endpoint, params) {
        const urlParams = new URLSearchParams();
        
        // 根据参数类型构建URL
        if (params.message) {
            urlParams.append(this.options.messageParam, params.message);
        } else if (params.text && params.targetLang) {
            urlParams.append('text', params.text);
            urlParams.append('targetLang', params.targetLang);
        } else if (params.city) {
            urlParams.append('city', params.city);
        }
        
        const queryString = urlParams.toString();
        return queryString ? `${endpoint}?${queryString}` : endpoint;
    }

    /**
     * 设置事件监听器
     */
    setupEventListeners() {
        this.eventSource.onopen = (event) => {
            console.log('EventSource连接已打开');
            this.updateStatus(this.options.statusTexts.connected);
            this.hideTypingIndicator();
            this.clearTimeout();
        };

        this.eventSource.onmessage = (event) => {
            console.log('收到消息:', event.data);
            this.handleMessage(event.data);
        };

        this.eventSource.onerror = (event) => {
            console.error('EventSource错误:', event);
            this.handleError(event);
        };

        // 监听流结束
        this.eventSource.addEventListener('done', (event) => {
            console.log('流传输完成');
            this.endStream();
        });

        // 监听翻译完成
        this.eventSource.addEventListener('complete', (event) => {
            console.log('翻译完成');
            this.endStream();
        });
    }

    /**
     * 处理接收到的消息
     */
    handleMessage(data) {
        // 调用自定义消息处理函数
        if (this.options.onMessage) {
            this.options.onMessage(data, this);
        } else {
            // 默认消息处理逻辑
            this.appendContent(data);
        }
        
        // 自动滚动
        if (this.options.autoScroll) {
            this.scrollToBottom();
        }
    }

    /**
     * 追加内容到结果容器
     */
    appendContent(content) {
        if (!this.options.resultContent) return;

        if (this.options.smartSpaceHandling) {
            this.appendWithSmartSpacing(content);
        } else {
            this.options.resultContent.textContent += content;
        }
    }

    /**
     * 智能空格处理
     */
    appendWithSmartSpacing(newContent) {
        const container = this.options.resultContent;
        const currentText = container.innerText;
        
        // 如果新内容以空格开头，直接追加
        if (newContent.startsWith(' ')) {
            container.innerHTML += newContent;
        } else {
            // 检查是否需要添加空格
            let shouldAddSpace = false;
            if (currentText.length > 0) {
                const lastChar = currentText[currentText.length - 1];
                // 如果最后一个字符是字母或数字，且新内容以大写字母开头，添加空格
                if (/[a-zA-Z0-9]/.test(lastChar) && /[A-Z]/.test(newContent[0])) {
                    shouldAddSpace = true;
                }
            }
            
            if (shouldAddSpace) {
                container.innerHTML += ' ' + newContent;
            } else {
                container.innerHTML += newContent;
            }
        }
    }

    /**
     * 创建消息容器（用于聊天功能）
     */
    createMessageContainer() {
        this.options.messageCounter++;
        this.currentMessageId = `aiResponse_${this.options.messageCounter}`;
        
        const aiMessageDiv = document.createElement('div');
        aiMessageDiv.className = 'message ai';
        
        const aiContentDiv = document.createElement('div');
        aiContentDiv.className = 'message-content';
        aiContentDiv.id = this.currentMessageId;
        aiContentDiv.style.whiteSpace = 'pre-wrap';
        aiContentDiv.textContent = '';
        
        aiMessageDiv.appendChild(aiContentDiv);
        
        // 添加到聊天容器
        const chatMessages = document.getElementById('chatMessages');
        if (chatMessages) {
            chatMessages.appendChild(aiMessageDiv);
            this.scrollToBottom();
        }
    }

    /**
     * 处理错误
     */
    handleError(event) {
        this.updateStatus(this.options.statusTexts.error);
        this.hideTypingIndicator();
        
        // 显示错误信息
        if (this.options.showErrorInResult && this.options.resultContent) {
            this.options.resultContent.textContent += '\n' + this.options.errorMessage;
        }
        
        // 调用错误回调
        if (this.options.onError) {
            this.options.onError(event, this);
        }
        
        this.endStream();
    }

    /**
     * 结束流式传输
     */
    endStream() {
        console.log('结束流式传输');
        
        this.closeConnection();
        this.clearTimeout();
        
        this.isStreaming = false;
        this.updateUIState('default');
        this.updateStatus(this.options.statusTexts.complete);
        
        // 3秒后清除状态
        setTimeout(() => {
            this.updateStatus('');
        }, 3000);
        
        // 调用结束回调
        if (this.options.onEnd) {
            this.options.onEnd();
        }
    }

    /**
     * 关闭连接
     */
    closeConnection() {
        if (this.eventSource) {
            console.log('关闭EventSource连接');
            this.eventSource.close();
            this.eventSource = null;
        }
    }

    /**
     * 设置超时
     */
    setupTimeout() {
        if (this.options.timeout > 0) {
            this.timeoutId = setTimeout(() => {
                console.log('请求超时');
                this.handleError({ type: 'timeout' });
            }, this.options.timeout);
        }
    }

    /**
     * 清除超时
     */
    clearTimeout() {
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
            this.timeoutId = null;
        }
    }

    /**
     * 更新UI状态
     */
    updateUIState(state) {
        if (!this.options.sendButton) return;
        
        const button = this.options.sendButton;
        const texts = this.options.buttonTexts;
        
        switch (state) {
            case 'loading':
                button.disabled = true;
                button.textContent = texts.loading;
                break;
            case 'translating':
                button.disabled = true;
                button.textContent = texts.translating;
                break;
            case 'querying':
                button.disabled = true;
                button.textContent = texts.querying;
                break;
            case 'default':
            default:
                button.disabled = false;
                button.textContent = texts.default;
                break;
        }
    }

    /**
     * 更新状态文本
     */
    updateStatus(text) {
        if (this.options.statusElement) {
            this.options.statusElement.textContent = text;
        }
    }

    /**
     * 显示输入指示器
     */
    showTypingIndicator() {
        if (this.options.typingIndicator) {
            this.options.typingIndicator.style.display = 'block';
        }
    }

    /**
     * 隐藏输入指示器
     */
    hideTypingIndicator() {
        if (this.options.typingIndicator) {
            this.options.typingIndicator.style.display = 'none';
        }
    }

    /**
     * 滚动到底部
     */
    scrollToBottom() {
        const container = this.options.scrollContainer || 
                         document.getElementById('chatMessages') || 
                         this.options.resultContainer;
        
        if (container) {
            container.scrollTop = container.scrollHeight;
        }
    }

    /**
     * 添加消息（用于聊天功能）
     */
    addMessage(content, type = 'user') {
        const chatMessages = document.getElementById('chatMessages');
        if (!chatMessages) return;
        
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;
        
        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';
        contentDiv.style.whiteSpace = 'pre-wrap';
        contentDiv.innerText = content;
        
        messageDiv.appendChild(contentDiv);
        chatMessages.appendChild(messageDiv);
        
        this.scrollToBottom();
    }

    /**
     * 获取当前消息ID
     */
    getCurrentMessageId() {
        return this.currentMessageId;
    }

    /**
     * 检查是否正在流式传输
     */
    isCurrentlyStreaming() {
        return this.isStreaming;
    }

    /**
     * 销毁实例
     */
    destroy() {
        this.closeConnection();
        this.clearTimeout();
        this.isStreaming = false;
    }
}

/**
 * 工具函数：处理键盘事件
 */
function handleKeyPress(event, callback) {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        if (callback) callback();
    }
}

/**
 * 工具函数：获取输入值
 */
function getInputValue(inputId) {
    const input = document.getElementById(inputId);
    return input ? input.value.trim() : '';
}

/**
 * 工具函数：清空输入
 */
function clearInput(inputId) {
    const input = document.getElementById(inputId);
    if (input) input.value = '';
}

/**
 * 工具函数：显示结果容器
 */
function showResultContainer(containerId, contentId) {
    const container = document.getElementById(containerId);
    const content = document.getElementById(contentId);
    
    if (container) container.style.display = 'block';
    if (content) content.textContent = '';
}

/**
 * 工具函数：获取提供商信息
 */
function getProviderInfo() {
    return fetch('/api/provider-info')
        .then(response => response.json())
        .catch(error => {
            console.log('获取提供商信息失败，使用默认值');
            return { provider: 'ollama' };
        });
}

/**
 * 工具函数：更新提供商徽章
 */
function updateProviderBadge(badgeId) {
    getProviderInfo().then(data => {
        const badge = document.getElementById(badgeId);
        if (!badge) return;
        
        if (data.provider === 'ollama') {
            badge.innerHTML = '🖥️ 本地 Ollama';
            badge.style.background = 'linear-gradient(135deg, #667eea, #764ba2)';
        } else {
            badge.innerHTML = '☁️ 远程 DeepSeek';
            badge.style.background = 'linear-gradient(135deg, #f093fb, #f5576c)';
        }
    });
}

// 导出到全局作用域
window.AIStreamHandler = AIStreamHandler;
window.AIUtils = {
    handleKeyPress,
    getInputValue,
    clearInput,
    showResultContainer,
    getProviderInfo,
    updateProviderBadge
}; 