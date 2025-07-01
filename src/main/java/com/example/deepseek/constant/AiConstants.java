package com.example.deepseek.constant;

/**
 * AI相关常量类
 * 统一管理项目中的硬编码字符串
 */
public final class AiConstants {
    
    private AiConstants() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * Session属性常量
     */
    public static final class Session {
        /** AI提供商 */
        public static final String AI_PROVIDER = "aiProvider";
        /** DeepSeek API Key */
        public static final String DEEPSEEK_API_KEY = "deepseekApiKey";
        
        private Session() {}
    }
    
    /**
     * AI提供商常量
     */
    public static final class Provider {
        /** 本地Ollama */
        public static final String OLLAMA = "ollama";
        /** 远程DeepSeek */
        public static final String DEEPSEEK = "deepseek";
        
        private Provider() {}
    }
    
    /**
     * 错误消息常量
     */
    public static final class ErrorMessage {
        /** 请先选择AI提供商 */
        public static final String SELECT_PROVIDER_FIRST = "请先选择AI提供商（本地Ollama或远程DeepSeek）后再进行查询。";
        /** 不支持的AI提供商 */
        public static final String UNSUPPORTED_PROVIDER = "不支持的AI提供商：%s，请选择本地Ollama或远程DeepSeek。";
        /** 请先设置DeepSeek API Key */
        public static final String SET_DEEPSEEK_API_KEY = "请先设置DeepSeek API Key。";
        /** 智能查询失败，正在使用直接查询方式 */
        public static final String FALLBACK_TO_DIRECT_QUERY = "智能查询失败，正在使用直接查询方式...\n\n";
        /** 智能查询服务暂时不可用 */
        public static final String SERVICE_UNAVAILABLE = "智能查询服务暂时不可用，正在使用直接查询方式...\n\n";
        
        private ErrorMessage() {}
    }
    
    /**
     * 日志消息常量
     */
    public static final class LogMessage {
        /** Ollama调用失败 */
        public static final String OLLAMA_CALL_FAILED = "Ollama调用失败: {}";
        /** DeepSeek调用失败 */
        public static final String DEEPSEEK_CALL_FAILED = "DeepSeek调用失败: {}";
        /** Ollama智能查询异常 */
        public static final String OLLAMA_QUERY_EXCEPTION = "Ollama智能查询异常: {}";
        /** DeepSeek智能查询异常 */
        public static final String DEEPSEEK_QUERY_EXCEPTION = "DeepSeek智能查询异常: {}";
        /** 解析Ollama响应失败 */
        public static final String PARSE_OLLAMA_RESPONSE_FAILED = "解析Ollama响应失败: {}";
        /** 解析DeepSeek响应失败 */
        public static final String PARSE_DEEPSEEK_RESPONSE_FAILED = "解析DeepSeek响应失败: {}";
        /** 工具函数执行异常 */
        public static final String TOOL_EXECUTION_EXCEPTION = "工具函数执行异常: {}";
        /** 处理工具函数调用异常 */
        public static final String PROCESS_TOOL_CALLS_EXCEPTION = "处理工具函数调用异常: {}";
        
        private LogMessage() {}
    }
    
    /**
     * 工具函数常量
     */
    public static final class ToolFunction {
        /** 查询订单API */
        public static final String QUERY_ORDER_API = "query_order_api";
        /** 格式化订单信息 */
        public static final String FORMAT_ORDER_INFO = "format_order_info";
        
        private ToolFunction() {}
    }
    
    /**
     * 订单状态常量
     */
    public static final class OrderStatus {
        /** 待付款 */
        public static final String PENDING_PAYMENT = "待付款";
        /** 处理中 */
        public static final String PROCESSING = "处理中";
        /** 已发货 */
        public static final String SHIPPED = "已发货";
        /** 已完成 */
        public static final String COMPLETED = "已完成";
        /** 已取消 */
        public static final String CANCELLED = "已取消";
        
        private OrderStatus() {}
    }
    
    /**
     * 订单状态对应的Emoji
     */
    public static final class OrderStatusEmoji {
        /** 待付款 */
        public static final String PENDING_PAYMENT = "⏳";
        /** 处理中 */
        public static final String PROCESSING = "🔄";
        /** 已发货 */
        public static final String SHIPPED = "📦";
        /** 已完成 */
        public static final String COMPLETED = "✅";
        /** 已取消 */
        public static final String CANCELLED = "❌";
        /** 未知状态 */
        public static final String UNKNOWN = "❓";
        
        private OrderStatusEmoji() {}
    }
    
    /**
     * 正则表达式常量
     */
    public static final class Regex {
        /** 11位数字订单号 */
        public static final String ORDER_NUMBER = "\\d{11}";
        /** 思考标签 */
        public static final String THINK_TAGS = "\\<think\\>.*?\\</think\\>|\\<thinking\\>.*?\\</thinking\\>";
        
        private Regex() {}
    }
    
    /**
     * 响应解析常量
     */
    public static final class ResponseParse {
        /** content字段 */
        public static final String CONTENT_FIELD = "\"content\":";
        /** 无法解析AI响应 */
        public static final String CANNOT_PARSE_RESPONSE = "无法解析AI响应";
        /** 解析AI响应时出现错误 */
        public static final String PARSE_ERROR = "解析AI响应时出现错误";
        
        private ResponseParse() {}
    }
} 