# 集成测试与单元测试用例文档 (AI 问答系统)

本系统采用 `Vitest` 和 `Vue Test Utils` 编写单元测试，集成测试主要验证系统级联流程。

## 1. 单元测试用例 (`ChatView.test.js`)
测试目标：验证 `ChatView.vue` 与内部的 `AiChatWidget.vue` 渲染及基础交互。

- **Test Case 1.1:** `renders the chat widget component`
  - 前置条件：设置 LocalStorage 用户数据并 Mock 路由
  - 预期结果：`AiChatWidget` 子组件被成功挂载，父容器包含 `.chat-page` 样式类，整体结构呈现全屏。
- **Test Case 1.2:** `contains the EcoClear brand header within the widget`
  - 预期结果：组件渲染文本中包含 "EcoClear" 与 "您好，请问有什么可以帮您？"。
- **Test Case 1.3:** `contains the microphone input card for user typing`
  - 预期结果：存在文本输入框 (`input[type="text"]`) 且其 Placeholder 包含 "说出或输入您的需求..."。

## 2. 集成测试用例
测试目标：验证用户登录 -> 获取 Token -> 页面自动跳转 -> AI 自动建立会话 -> 发送/接收消息完整链路。

### **Integration Test 1: 登录与自动跳转到 AI 问答界面**
- **步骤：**
  1. 打开 `/login`
  2. 输入正确的用户名和密码
  3. 点击“登录”按钮
- **预期结果：**
  - 成功接收后端 `200 OK` 响应，解析出 `Token` 和 `User` 对象。
  - 前端将其存入 `LocalStorage`。
  - Vue Router `beforeEach` 拦截通过，系统自动执行 `router.push('/chat')`。
  - URL 地址栏变为 `/chat`，页面加载 `ChatView.vue`。

### **Integration Test 2: AI 会话自动加载与初始化**
- **步骤：**
  1. 进入 `/chat` 界面后（`onMounted` 触发）
  2. 系统尝试获取用户已有的 `Session ID` (`GET /api/ai/sessions/user/{userId}`)
  3. 若无，则调用新建会话 (`POST /api/ai/sessions`)
- **预期结果：**
  - 成功获取或创建 `sessionId`。
  - 如果是新建会话，页面中央展示 1:1 设计图要求的空白大脑动画与欢迎语。

### **Integration Test 3: AI 意图识别与自动跳转**
- **步骤：**
  1. 用户在输入框中输入 "我要清运装修垃圾"
  2. 点击发送图标
- **预期结果：**
  - 用户消息立即以气泡形式显示在聊天历史区（右侧绿色气泡）。
  - AI 处于 Loading（打字机动画）状态。
  - 调用 `POST /api/ai/chat` 接口后，收到 AI 回复，例如 "好的，正在为您跳转..." 以及 `actionPayload: '/order/create'`。
  - 前端气泡渲染 AI 回复内容（左侧白色气泡）。
  - 延迟 1.5 秒后，前端自动触发 `router.push('/order/create')`，跳转至下单页面。

### **Integration Test 4: Web Speech API 语音交互**
- **步骤：**
  1. 点击麦克风图标
  2. 允许浏览器麦克风权限
  3. 说出需求
- **预期结果：**
  - 麦克风图标变为活跃状态，显示语音波纹动画。
  - 识别完成或静音后，输入框填入语音转写文本。
  - 系统自动触发 `sendMessage`，无需手动点击发送。

## 3. 运行测试指令
```bash
# 执行前端全部单元测试
npm run test:unit
```