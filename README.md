# DeepSeek Demo - AI功能演示项目

一个基于Spring Boot的现代化AI功能演示项目，支持智能对话、语言翻译和天气查询功能。本项目展示了如何集成多种AI服务，提供流畅的用户体验。

## 🎯 项目特色

- **🤖 双AI支持**: 同时支持本地Ollama和远程DeepSeek两种AI提供商
- **⚡ 流式交互**: 实时流式输出，提供类似ChatGPT的对话体验
- **🎨 现代化UI**: 美观的响应式界面，支持毛玻璃效果和流畅动画
- **🔧 高度可配置**: 通过配置文件轻松管理AI服务参数
- **📱 移动友好**: 完美适配桌面和移动设备
- **🚀 开箱即用**: 简单配置即可快速体验AI功能

## 🚀 功能特性

### 智能对话 💬
- 实时流式对话，支持长文本输出
- 智能过滤AI思考过程（`<think>`标签）
- 支持上下文对话
- 优雅的打字机效果

### 语言翻译 🌐
- 支持中英文双向翻译
- 智能处理相同语言的情况
- 流式翻译输出
- 准确的翻译结果

### 天气查询 🌤️
- 基于AI的天气信息生成
- 支持全球城市查询
- 友好的信息展示

### 双AI提供商 🔄
- **本地Ollama**: 快速、免费、隐私安全，支持离线使用
- **远程DeepSeek**: 功能强大、模型最新，支持更多高级功能

## 🛠️ 技术栈

### 后端技术
- **Spring Boot 3.1.9**: 现代化的Java Web框架
- **Spring WebFlux**: 响应式编程，支持流式处理
- **Thymeleaf**: 服务端模板引擎
- **Lombok**: 简化Java代码，减少样板代码
- **Jackson**: JSON数据处理

### 前端技术
- **HTML5 + CSS3**: 现代化Web标准
- **JavaScript (ES6+)**: 原生JavaScript，无框架依赖
- **Server-Sent Events (SSE)**: 实现流式数据推送
- **响应式设计**: 完美适配各种设备

### AI服务
- **Ollama**: 本地AI服务，支持多种模型
- **DeepSeek API**: 云端AI服务，功能强大

### 开发工具
- **Maven**: 项目构建和依赖管理
- **Java 21**: 最新的LTS版本
- **Spring Boot DevTools**: 开发时热重载

## 📋 系统要求

- **Java**: 21+ (推荐使用最新LTS版本)
- **Maven**: 3.6+ 
- **内存**: 至少4GB RAM
- **存储**: 至少2GB可用空间
- **网络**: 用于下载依赖和访问远程AI服务

### 可选要求
- **Ollama**: 用于本地AI服务（推荐安装）

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd deepseek-demo
```

### 2. 编译运行

```bash
# 清理并编译
mvn clean compile

# 启动应用
mvn spring-boot:run
```

### 3. 访问应用

打开浏览器访问: http://localhost:8081

## 🔧 配置说明

### 应用配置 (`application.yml`)

```yaml
server:
  port: 8081

ai:
  ollama:
    base-url: http://localhost:11434
    api-path: /api/chat
    model: deepseek-r1:7b
    timeout: 30000
  deepseek:
    base-url: https://api.deepseek.com
    api-path: /v1/chat/completions
    model: deepseek-chat
    timeout: 30000
  weather:
    base-url: https://wttr.in
    timeout: 10000
```

### 本地Ollama配置 (推荐)

1. **安装Ollama**
   ```bash
   # Windows (使用WSL2)
   curl -fsSL https://ollama.ai/install.sh | sh
   
   # macOS
   brew install ollama
   
   # Linux
   curl -fsSL https://ollama.ai/install.sh | sh
   ```

2. **下载DeepSeek模型**
   ```bash
   ollama pull deepseek-r1:7b
   ```

3. **启动Ollama服务**
   ```bash
   ollama serve
   ```

4. **在应用中选择本地Ollama**
   - 访问首页
   - 选择"本地 Ollama"选项
   - 无需输入API Key
   - 点击"开始使用"

### 远程DeepSeek配置

1. **获取API Key**
   - 访问 [DeepSeek官网](https://platform.deepseek.com)
   - 注册账号并获取API Key

2. **在应用中配置**
   - 访问首页
   - 选择"远程 DeepSeek"选项
   - 输入你的API Key
   - 点击"开始使用"

## 📱 使用指南

### 智能对话

1. 点击首页的"智能对话"卡片
2. 在聊天界面输入你的问题
3. 按Enter键或点击发送按钮
4. 享受AI的流式回复

**特色功能**:
- 实时流式输出
- 智能过滤思考过程
- 支持长文本对话
- 优雅的动画效果

### 语言翻译

1. 点击首页的"语言翻译"卡片
2. 选择源语言和目标语言
3. 输入要翻译的文本
4. 点击翻译按钮

**特色功能**:
- 智能处理相同语言
- 流式翻译输出
- 准确的翻译结果
- 支持专业术语

### 天气查询

1. 点击首页的"天气查询"卡片
2. 输入城市名称
3. 点击查询按钮
4. 查看AI生成的天气信息

## 🎨 界面特色

### 设计理念
- **现代化**: 采用最新的Web设计趋势
- **简洁性**: 清晰的视觉层次和直观的操作流程
- **一致性**: 统一的设计语言和交互模式

### 视觉效果
- **渐变背景**: 动态渐变色彩，营造科技感
- **毛玻璃效果**: 现代化的半透明效果
- **流畅动画**: 丰富的交互动画和过渡效果
- **响应式布局**: 完美适配各种屏幕尺寸

### 用户体验
- **直观操作**: 简洁明了的用户界面
- **即时反馈**: 实时的操作反馈和状态提示
- **错误处理**: 友好的错误提示和恢复机制

## 🔍 项目结构

```
deepseek-demo/
├── src/main/java/com/example/deepseek/
│   ├── config/              # 配置类
│   │   └── AiConfig.java    # AI服务配置
│   ├── controller/          # 控制器层
│   │   ├── ApiKeyController.java
│   │   ├── ChatController.java
│   │   ├── HomeController.java
│   │   ├── TranslateController.java
│   │   └── WeatherController.java
│   ├── service/             # 服务层
│   │   ├── BaseAiService.java    # AI服务基类
│   │   ├── ChatService.java
│   │   ├── TranslateService.java
│   │   └── WeatherService.java
│   └── DeepseekDemoApplication.java
├── src/main/resources/
│   ├── static/
│   │   ├── css/             # 样式文件
│   │   │   └── style.css
│   │   └── js/              # JavaScript库
│   │       ├── ai-stream.js     # AI流式处理通用库
│   │       └── README.md        # 库使用说明
│   ├── templates/           # 页面模板
│   │   ├── index.html       # 首页
│   │   ├── chat.html        # 聊天页面
│   │   ├── translate.html   # 翻译页面
│   │   └── weather.html     # 天气页面
│   └── application.yml      # 配置文件
├── pom.xml                  # Maven配置
└── README.md               # 项目说明
```

## 🎯 前端架构

### AI流式处理通用库

项目使用自研的 `ai-stream.js` 通用库来抽象Ollama和DeepSeek的流式处理逻辑：

#### 核心特性
- **统一API**: 为聊天、翻译等功能提供统一的流式处理接口
- **高度可配置**: 支持自定义端点、UI元素、按钮文本等
- **智能空格处理**: 自动处理英文单词间的空格问题
- **自动状态管理**: 自动管理UI状态（按钮禁用/启用、状态显示等）
- **完善错误处理**: 包含超时机制和错误恢复

#### 使用示例

```javascript
// 创建聊天处理器
const chatHandler = new AIStreamHandler({
    endpoint: '/chat/stream',
    messageParam: 'message',
    sendButton: document.getElementById('sendBtn'),
    statusElement: document.getElementById('status'),
    enableMessageCounter: true,
    
    onMessage: function(data, handler) {
        const messageId = handler.getCurrentMessageId();
        const aiResponse = document.getElementById(messageId);
        if (aiResponse) {
            aiResponse.innerHTML += data;
        }
    }
});

// 开始流式对话
chatHandler.startStream({ message: '你好' });
```

#### 工具函数

```javascript
// 键盘事件处理
handleKeyPress(event, sendMessage);

// 输入值获取和清空
const message = AIUtils.getInputValue('messageInput');
AIUtils.clearInput('messageInput');

// 结果容器显示
AIUtils.showResultContainer('resultContainer', 'resultContent');

// 提供商徽章更新
AIUtils.updateProviderBadge('providerBadge');
```

### 代码重构成果

通过前端重构，实现了以下改进：

1. **代码复用**: 将重复的流式处理逻辑抽象到通用库中
2. **维护性提升**: 统一的API接口，便于维护和扩展
3. **功能增强**: 添加了超时处理、智能空格处理等高级功能
4. **开发效率**: 新功能开发时可直接使用通用库，减少重复代码
5. **用户体验**: 更稳定的错误处理和状态管理

详细的使用指南请参考：`src/main/resources/static/js/README.md`

## 🌟 核心特性详解

### 双AI提供商架构

项目采用策略模式实现双AI提供商支持：

```java
// 基类定义统一接口
public abstract class BaseAiService {
    protected void executeStreamRequest(String prompt, HttpSession session, 
                                      SseEmitter emitter, String provider, String requestType) {
        if ("ollama".equals(provider)) {
            executeOllamaStreamRequest(prompt, emitter, requestType);
        } else {
            executeDeepSeekStreamRequest(prompt, session, emitter, requestType);
        }
    }
}
```

### 流式处理机制

使用Server-Sent Events (SSE)实现流式输出：

```javascript
const eventSource = new EventSource(`/chat/stream?message=${encodeURIComponent(message)}`);
eventSource.onmessage = function(event) {
    appendMessage(event.data);
};
```

### 配置化管理

通过`@ConfigurationProperties`实现配置的自动绑定：

```java
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    private Ollama ollama = new Ollama();
    private Deepseek deepseek = new Deepseek();
    private Weather weather = new Weather();
}
```

## 🔧 开发指南

### 环境搭建

1. **安装Java 21**
   ```bash
   # 下载并安装Java 21
   # 设置JAVA_HOME环境变量
   ```

2. **安装Maven**
   ```bash
   # 下载并安装Maven 3.6+
   # 设置MAVEN_HOME环境变量
   ```

3. **IDE配置**
   - 推荐使用IntelliJ IDEA或Eclipse
   - 安装Lombok插件
   - 配置Java 21和Maven

### 添加新功能

1. **创建控制器**
   ```java
   @Controller
   @RequiredArgsConstructor
   public class NewFeatureController {
       private final NewFeatureService service;
       
       @GetMapping("/new-feature")
       public String newFeaturePage() {
           return "new-feature";
       }
   }
   ```

2. **实现服务层**
   ```java
   @Service
   public class NewFeatureService extends BaseAiService {
       public NewFeatureService(AiConfig aiConfig) {
           super(aiConfig);
       }
   }
   ```

3. **添加页面模板**
   ```html
   <!-- src/main/resources/templates/new-feature.html -->
   <!DOCTYPE html>
   <html xmlns:th="http://www.thymeleaf.org">
   <!-- 页面内容 -->
   </html>
   ```

4. **添加样式**
   ```css
   /* src/main/resources/static/css/style.css */
   .new-feature-container {
       /* 样式定义 */
   }
   ```

### 代码规范

- 使用Lombok减少样板代码
- 遵循Spring Boot最佳实践
- 使用构造函数注入
- 保持代码简洁和可读性

## 🐛 故障排除

### 常见问题

1. **Ollama连接失败**
   ```
   错误: Ollama API 错误: 404 Not Found
   解决: 确保Ollama服务正在运行，检查端口11434
   ```

2. **端口占用**
   ```
   错误: Port 8081 was already in use
   解决: 修改application.yml中的server.port配置
   ```

3. **API Key错误**
   ```
   错误: 抱歉，请求失败，请检查API Key是否正确
   解决: 检查DeepSeek API Key是否有效
   ```

### 调试技巧

1. **查看日志**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
   ```

2. **浏览器调试**
   - 打开开发者工具
   - 查看Network标签页
   - 检查Console错误信息

3. **测试AI服务**
   ```bash
   # 测试Ollama
   curl -X POST http://localhost:11434/api/chat \
     -H "Content-Type: application/json" \
     -d '{"model":"deepseek-r1:7b","messages":[{"role":"user","content":"Hello"}]}'
   ```

## 📄 许可证

本项目仅供学习和演示使用。

## 🙏 特别感谢

**本项目代码由 [Cursor](https://cursor.sh) 智能编程助手生成**

感谢Cursor提供的强大AI编程能力，帮助快速构建了这个功能完整的AI演示项目。Cursor不仅生成了高质量的代码，还提供了详细的注释和最佳实践建议，大大提升了开发效率。

### Cursor的优势
- 🤖 **智能代码生成**: 基于自然语言描述生成完整代码
- 🔧 **上下文理解**: 深度理解项目结构和需求
- 📝 **详细注释**: 自动生成清晰的代码注释
- 🎯 **最佳实践**: 遵循行业标准和最佳实践
- ⚡ **快速迭代**: 支持快速修改和优化

### 项目开发过程
本项目从零开始，完全由Cursor协助开发：
1. **项目初始化**: Cursor帮助创建Spring Boot项目结构
2. **功能实现**: 逐步实现聊天、翻译、天气查询功能
3. **代码优化**: 使用Lombok简化代码，重构服务层
4. **问题解决**: 解决流式输出、错误处理等技术难题
5. **文档编写**: 生成详细的README文档

## 👨‍💻 作者

**菩提树下的杨过** - [博客](http://yjmyzz.cnblogs.com)

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个项目！

### 贡献指南
1. Fork本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开Pull Request

## 📞 支持

如果你在使用过程中遇到问题，请：

1. **检查环境配置**
   - 确认Java版本为21+
   - 确认Maven版本为3.6+
   - 检查网络连接

2. **查看日志信息**
   - 启动时查看控制台输出
   - 检查错误堆栈信息

3. **提交Issue**
   - 详细描述问题现象
   - 提供环境信息
   - 附上相关日志

## 🚀 未来计划

- [ ] 支持更多AI模型
- [ ] 添加用户认证系统
- [ ] 实现对话历史记录
- [ ] 支持文件上传和处理
- [ ] 添加更多翻译语言
- [ ] 优化移动端体验

---

**享受AI带来的智能体验！** 🚀

*本项目展示了现代Web开发与AI技术结合的无限可能。感谢Cursor让这一切变得如此简单！* 