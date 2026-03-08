# 智慧垃圾清运系统 (Smart Waste Management System)

基于 Spring Boot 3 + Vue 3 的现代化智慧垃圾清运系统，提供智能下单、AI客服、车队调度、监管驾驶舱等全流程解决方案。

## 📋 功能特性

- **用户端**：
  - 注册/登录（个人/物业/司机）
  - 智能下单（生活/建筑/厨余垃圾）
  - 历史订单查询
  - AI 智能客服（支持知识库问答与人工转接）
- **司机端**：
  - 待办任务列表
  - 状态流转（接单 -> 装车 -> 运输 -> 卸货 -> 完成）
  - 拍照留痕（装车/卸货现场照片上传）
- **管理端**：
  - 监管驾驶舱（实时KPI、清运趋势图表）
  - 知识库管理（FAQ 动态配置）
  - 基础数据管理（站点、车队、车辆）

## 🛠 技术栈

- **后端**：Spring Boot 3.0, Spring Data JPA, PostgreSQL, Spring Security (Hash/Token)
- **前端**：Vue 3, Vite, Element Plus, ECharts, Axios
- **部署**：Docker, Docker Compose, Nginx

## 🚀 快速开始

### 前置要求
- Docker & Docker Compose
- Java 17+ (本地开发需)
- Node.js 18+ (本地开发需)

### 一键部署 (推荐)

在项目根目录下运行：

```bash
docker-compose up --build
```

启动后访问：
- **前端页面**: http://localhost
- **API 文档**: http://localhost:8080/swagger-ui/index.html

### 默认账号

系统启动时会自动初始化以下数据：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | `admin` | `admin123` | 拥有所有权限，可访问驾驶舱 |
| 司机 | `driver1` | `123456` | 仅访问司机作业台 |
| 用户 | (需注册) | - | 访问下单与客服 |

## 📂 项目结构

```
renewableJava/
├── backend/            # Spring Boot 后端源码
│   ├── src/main/java/com/renewable/ai/
│   │   ├── config/     # 配置类 (Web, Swagger, Seeder)
│   │   ├── controller/ # REST 控制器
│   │   ├── entity/     # JPA 实体
│   │   ├── service/    # 业务逻辑
│   │   └── repository/ # 数据访问层
│   └── Dockerfile
├── frontend/           # Vue 3 前端源码
│   ├── src/
│   │   ├── views/      # 页面视图 (Login, Order, Chat, Dashboard...)
│   │   ├── api.js      # Axios 封装
│   │   └── router/     # 路由配置
│   └── Dockerfile
├── docker-compose.yml  # 编排文件
└── schema.sql          # 数据库初始化脚本
```

## 🧪 接口文档

后端集成了 Swagger/OpenAPI，启动后访问 `http://localhost:8080/swagger-ui/index.html` 可查看完整接口文档并进行在线调试。

## 📝 开发计划完成度

- [x] MVP 核心流程 (用户/订单)
- [x] AI 客服 (Mock + 知识库)
- [x] 司机作业台 & 拍照上传
- [x] 监管驾驶舱 & 图表展示
- [x] Docker 容器化部署
- [x] 单元测试 (OrderService)
