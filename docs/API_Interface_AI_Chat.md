# API 接口文档 (AI 问答系统)

本系统采用 RESTful 风格的 API，数据交互格式均为 JSON (`application/json`)。请求中需要使用 JWT Token 在 `Authorization` 头部进行权限认证。

---

## 1. 用户认证与登录
**接口：** `POST /api/auth/login`
**描述：** 验证用户凭据并生成 JWT Token 登录会话。

### 请求体 (Request Body)
```json
{
  "username": "user1",
  "password": "123"
}
```

### 响应 (Response) - `200 OK`
```json
{
  "user": {
    "id": 1,
    "username": "user1",
    "role": "individual"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 2. 获取用户AI对话历史会话
**接口：** `GET /api/ai/sessions/user/{userId}`
**描述：** 查询当前用户所有活跃的AI对话会话列表。

### 请求参数
- **Path Param `userId`:** 用户唯一ID

### 响应 (Response) - `200 OK`
```json
[
  {
    "id": "e3b0c442-989b-464c-86c0-bb821213f5d5",
    "userId": 1,
    "createdAt": "2024-04-07T10:00:00Z"
  }
]
```

---

## 3. 创建新AI对话会话
**接口：** `POST /api/ai/sessions`
**描述：** 为当前用户新建一个全新的AI对话环境。

### 请求体 (Request Body)
```json
{
  "userId": 1
}
```

### 响应 (Response) - `200 OK`
```json
{
  "id": "f5a892b1-12c4-4a2b-b230-aa3190823bbd",
  "userId": 1,
  "createdAt": "2024-04-07T10:05:00Z"
}
```

---

## 4. 获取指定会话的聊天历史记录
**接口：** `GET /api/ai/history/{sessionId}`
**描述：** 调取当前会话上下文内所有的聊天消息（包括用户的和AI返回的）。

### 请求参数
- **Path Param `sessionId`:** 会话唯一UUID

### 响应 (Response) - `200 OK`
```json
[
  {
    "senderType": "user",
    "content": "我想预约清运服务",
    "createdAt": "2024-04-07T10:05:10Z"
  },
  {
    "senderType": "ai",
    "content": "好的，正在为您跳转到预约清运界面...",
    "actionPayload": "/order/create",
    "createdAt": "2024-04-07T10:05:15Z"
  }
]
```

---

## 5. 发送消息给AI助手
**接口：** `POST /api/ai/chat`
**描述：** 用户发送文本或语音识别结果给AI进行语义分析，并返回回复与路由动作。

### 请求体 (Request Body)
```json
{
  "sessionId": "f5a892b1-12c4-4a2b-b230-aa3190823bbd",
  "content": "我想预约清运服务"
}
```

### 响应 (Response) - `200 OK`
```json
{
  "senderType": "ai",
  "content": "好的，正在为您跳转到预约清运界面...",
  "actionPayload": "/order/create",
  "createdAt": "2024-04-07T10:05:15Z"
}
```