# 单元测试说明

## 概述

本项目使用Mockito框架和JUnit 5编写了完整的单元测试，覆盖了以下组件：

### 测试覆盖范围

#### 配置层 (Config)
- `AiConfigTest` - AI配置类测试
  - 测试Ollama、DeepSeek、Weather配置的getter/setter方法
  - 测试URL拼接逻辑
  - 测试默认值设置

#### 服务层 (Service)
- `BaseAiServiceTest` - 基础AI服务测试
  - 测试流式请求执行
  - 测试提供商判断逻辑
  - 测试默认提供商获取

- `ChatServiceTest` - 聊天服务测试
  - 测试Ollama和DeepSeek聊天功能
  - 测试空消息和null消息处理
  - 测试默认提供商使用

- `TranslateServiceTest` - 翻译服务测试
  - 测试中英文翻译功能
  - 测试相同语言处理
  - 测试空文本和null文本处理

- `WeatherServiceTest` - 天气服务测试
  - 测试天气信息获取
  - 测试中英文城市名处理
  - 测试空城市名和null城市名处理

#### 控制器层 (Controller)
- `ChatControllerTest` - 聊天控制器测试
  - 测试页面路由
  - 测试流式聊天接口
  - 测试特殊字符处理

- `TranslateControllerTest` - 翻译控制器测试
  - 测试页面路由
  - 测试流式翻译接口
  - 测试不同语言组合

- `WeatherControllerTest` - 天气控制器测试
  - 测试页面路由
  - 测试天气查询接口
  - 测试Model属性设置

## 运行测试

### 运行所有测试
```bash
mvn test
```

### 运行特定测试类
```bash
mvn test -Dtest=ChatServiceTest
```

### 运行测试套件
```bash
mvn test -Dtest=AllTests
```

### 生成测试报告
```bash
mvn surefire-report:report
```

## 测试特点

### Mockito使用
- 使用`@Mock`注解创建模拟对象
- 使用`@InjectMocks`注解自动注入依赖
- 使用`when().thenReturn()`设置模拟行为
- 使用`verify()`验证方法调用

### 测试场景覆盖
- 正常流程测试
- 边界条件测试（空值、null值）
- 异常情况测试
- 特殊字符处理测试

### 异步操作处理
- 对于涉及网络请求的异步操作，测试主要验证方法调用不抛出异常
- 在测试环境中网络连接失败是正常的，因此使用try-catch包装

## 测试数据

### 模拟配置
- Ollama: `http://localhost:11434/api/chat`
- DeepSeek: `https://api.deepseek.com/v1/chat/completions`
- Weather: `https://wttr.in`

### 测试用例
- 中英文文本翻译
- 各种城市名天气查询
- 不同AI提供商的聊天功能
- 配置参数的getter/setter验证

## 注意事项

1. **网络依赖**: 部分测试涉及网络请求，在无网络环境下可能失败，这是正常的
2. **异步操作**: 流式处理相关的测试主要验证方法调用，不验证具体的流式输出
3. **Mock对象**: 所有外部依赖都使用Mock对象，确保测试的独立性
4. **测试隔离**: 每个测试方法都是独立的，不依赖其他测试的执行结果

## 扩展测试

如需添加新的测试用例：

1. 在对应的测试类中添加新的`@Test`方法
2. 使用`@BeforeEach`设置测试环境
3. 使用Mockito模拟依赖对象
4. 使用断言验证测试结果
5. 更新`AllTests`测试套件（如需要）

## 作者信息

- **作者**: 菩提树下的杨过
- **特别感谢**: Cursor生成本项目代码
- **测试框架**: Mockito + JUnit 5
- **构建工具**: Maven 