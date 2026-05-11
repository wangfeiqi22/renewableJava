# AI拍照识别功能部署指南

## ⚠️ 重要说明

由于当前环境中没有安装Maven或Gradle，无法自动重新编译Java代码。需要手动执行以下步骤来完成AI识别功能的部署。

---

## 📋 部署步骤

### 步骤1: 安装Maven（如果尚未安装）

**macOS:**
```bash
# 使用Homebrew安装
brew install maven

# 验证安装
mvn -version
```

**Linux:**
```bash
sudo apt-get install maven
```

**Windows:**
- 下载Maven: https://maven.apache.org/download.cgi
- 解压并配置环境变量

### 步骤2: 重新编译后端项目

```bash
cd /Users/ripple/renewableJava/backend

# 清理并编译
mvn clean package -DskipTests

# 验证JAR文件已更新
ls -lh target/ai-customer-service-0.0.1-SNAPSHOT.jar
```

### 步骤3: 配置MiniMax API密钥（可选）

如果需要使用真实的MiniMax AI识别，需要配置API密钥：

**编辑配置文件:**
```bash
vim src/main/resources/application.properties
```

**添加以下配置:**
```properties
# MiniMax AI配置
minimax.api.key=YOUR_MINIMAX_API_KEY
minimax.api.url=https://api.minimax.chat
minimax.group-id=YOUR_GROUP_ID
```

**获取API密钥:**
1. 访问 MiniMax 开放平台: https://platform.minimax.chat/
2. 注册并登录账号
3. 创建API密钥
4. 替换上述配置中的 `YOUR_MINIMAX_API_KEY`

### 步骤4: 停止当前运行的服务

```bash
# 查找进程
ps aux | grep "ai-customer-service"

# 停止服务（替换 PID 为实际进程ID）
kill <PID>
```

### 步骤5: 重启后端服务

```bash
cd /Users/ripple/renewableJava/backend

# 启动服务
java -jar target/ai-customer-service-0.0.1-SNAPSHOT.jar
```

### 步骤6: 前端自动编译（已自动完成）

前端代码已经修改完成，会自动重新编译（Vite的热重载）。

---

## 🔧 已完成的修改

### 1. 后端 - MiniMaxAiService.java
**文件位置:** `backend/src/main/java/com/renewable/ai/service/MiniMaxAiService.java`

**功能:**
- 集成了MiniMax AI API调用
- 支持配置API密钥
- 如果未配置密钥，自动使用模拟结果
- 提供了垃圾分类的智能识别

### 2. 后端 - AiController.java
**文件位置:** `backend/src/main/java/com/renewable/ai/controller/AiController.java`

**修改:**
- 注入了MiniMaxAiService
- 调用真实AI服务进行识别
- 添加了完善的错误处理

### 3. 前端 - DriverTaskView.vue
**文件位置:** `frontend/src/views/DriverTaskView.vue`

**修改:**
- 修改了handleAiAnalyze函数
- 调用后端 `/api/ai/recognize-waste` 接口
- 解析AI识别结果并填充表单

---

## 🧪 测试AI识别功能

### 方法1: 使用测试脚本

```bash
python3 /Users/ripple/renewableJava/test_ai_analyze.py
```

### 方法2: 手动测试

1. **访问系统:** http://localhost:5173
2. **登录:** 使用自由司机账号
   ```
   用户名: freelance_driver_1777912107
   密码: Test123456
   ```
3. **进入自主建单:** 访问 http://localhost:5173/driver/task
4. **点击拍照识别:** 点击"拍照智能识别"按钮
5. **上传照片:** 选择任意图片文件
6. **查看结果:** AI会自动识别并填充表单

---

## 📊 API接口说明

### 端点: POST /api/ai/recognize-waste

**请求:**
```
Content-Type: multipart/form-data

参数:
- file: 图片文件 (必填)
- userId: 用户ID (可选)
```

**响应:**
```json
{
  "itemName": "塑料瓶",
  "category": "recyclable",
  "confidence": 85,
  "advice": "请倒空液体后压扁投入可回收物收集容器。",
  "environmentalTips": "回收1吨塑料可节约石油6吨，减少二氧化碳排放3吨！",
  "abTestGroup": "A_Standard"
}
```

---

## 🎯 分类映射

| API返回类别 | 内部类别 | 说明 |
|------------|---------|------|
| recyclable | household | 可回收物 |
| hazardous | construction | 有害垃圾 |
| wet | kitchen | 厨余垃圾 |
| dry | construction | 干垃圾 |
| construction | construction | 建筑垃圾 |
| household | household | 生活垃圾 |
| kitchen | kitchen | 厨余垃圾 |
| bulky | bulky | 大件垃圾 |

---

## ❓ 常见问题

### Q1: 编译失败怎么办？
**A:** 确保已安装Maven并配置了JAVA_HOME环境变量。

### Q2: 如何获取MiniMax API密钥？
**A:** 访问 https://platform.minimax.chat/ 注册并创建API密钥。

### Q3: 如果不想使用真实AI怎么办？
**A:** 不配置API密钥即可，系统会自动使用模拟结果。

### Q4: API调用失败怎么办？
**A:** 检查网络连接和API密钥配置，确保MiniMax服务可用。

---

## 📞 支持

如有问题，请检查：
1. 后端日志: 查看控制台输出的日志信息
2. 前端控制台: 按F12查看浏览器控制台错误
3. 网络请求: 使用浏览器开发者工具查看API调用
