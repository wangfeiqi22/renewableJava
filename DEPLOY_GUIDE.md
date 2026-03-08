# Docker 部署与运维指南 (DEPLOY_GUIDE)

本文档提供了智慧垃圾清运系统的完整 Docker 部署流程、配置说明及故障排查指南。

## 1. 架构概览

系统采用微服务架构，包含以下三个容器服务：
- **renewable_db** (PostgreSQL 15): 核心业务数据库，挂载持久化卷 `db_data`。
- **renewable_backend** (Spring Boot 3): 业务逻辑层，提供 RESTful API，依赖数据库服务。
- **renewable_frontend** (Nginx + Vue 3): 静态资源托管与反向代理，依赖后端服务。

## 2. 部署流程

### 2.1 环境准备
确保服务器已安装：
- Docker Engine 20.10+
- Docker Compose v2+

### 2.2 本地构建与启动 (开发/测试)
在项目根目录下执行：
```bash
docker-compose up --build -d
```

### 2.3 生产环境部署 (基于镜像仓库)

#### 步骤 1: 构建并推送镜像 (在开发机执行)
1. 修改 `publish_images.bat` 中的 `REGISTRY_URL` 为您的私有仓库地址。
2. 运行脚本构建并推送镜像：
   ```cmd
   .\publish_images.bat
   ```

#### 步骤 2: 服务器端运行 (在生产服务器执行)
1. 创建 `docker-compose.prod.yml` (内容如下)：
   ```yaml
   version: '3.8'
   services:
     db:
       image: postgres:15
       # ... (保持配置不变)
     backend:
       image: myregistry.example.com/smart-waste/backend:v4.1.0
       restart: always
       # ... (环境变量配置)
     frontend:
       image: myregistry.example.com/smart-waste/frontend:v4.1.0
       restart: always
       ports:
         - "80:80"
   ```
2. 拉取并启动：
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

### 2.4 验证部署
查看容器状态：
```bash
docker-compose ps
```
正常情况下，三个服务的状态应为 `Up`，且 Health Check 显示 `healthy`。

访问地址：
- **Web 端**: `http://<服务器IP>`
- **API 文档**: `http://<服务器IP>:8080/swagger-ui/index.html`

## 3. 配置说明

### 3.1 环境变量
主要配置位于 `docker-compose.yml` 中：

| 服务 | 变量名 | 默认值 | 说明 |
|------|--------|--------|------|
| db | POSTGRES_PASSWORD | password | 数据库密码，生产环境请修改 |
| backend | SPRING_DATASOURCE_URL | jdbc:postgresql://db:5432/... | 数据库连接串 |
| backend | SPRING_JPA_HIBERNATE_DDL_AUTO | update | 自动建表策略 (validate/update/create) |

### 3.2 端口映射
- **前端 (Web)**: 宿主机 80 -> 容器 80
- **后端 (API)**: 宿主机 8080 -> 容器 8080
- **数据库**: 宿主机 5432 -> 容器 5432

## 4. 日志与监控

### 4.1 实时日志查看
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志 (如 backend)
docker-compose logs -f backend
```

### 4.2 健康检查 (Health Checks)
系统配置了自动健康检查机制：
- **DB**: 每 10s 检查一次 `pg_isready`，失败重试 5 次。
- **Backend**: 启动后 40s 开始检查 `/actuator/health` 接口，每 30s 一次。
- **Frontend**: 依赖 Backend 健康状态，Backend 就绪后才启动。

## 5. 故障排查 (Troubleshooting)

### Q1: 数据库连接失败 (Connection refused)
- **现象**: Backend 日志报错 `Connection to localhost:5432 refused`。
- **原因**: 容器间通信应使用服务名 `db` 而非 `localhost`。
- **解决**: 检查 `SPRING_DATASOURCE_URL` 是否为 `jdbc:postgresql://db:5432/...`。

### Q2: 前端 API 请求 404/502
- **现象**: 浏览器控制台显示 `/api/xxx` 请求失败。
- **原因**: Nginx 反向代理配置错误或后端未启动。
- **解决**:
  1. 检查 `frontend/nginx.conf` 中的 `proxy_pass http://backend:8080/api/;`。
  2. 确认 Backend 容器状态为 `healthy`。

### Q3: 镜像构建过慢
- **原因**: Maven 每次都重新下载依赖。
- **解决**: Backend Dockerfile 已启用分层缓存。首次构建较慢是正常的，后续修改代码（不改 pom.xml）时构建会非常快。

### Q4: 容器频繁重启 (Restarting)
- **原因**: 应用启动报错导致退出。
- **解决**: 使用 `docker-compose logs backend` 查看具体错误堆栈。常见原因是数据库密码不匹配或端口被占用。

## 6. 数据备份与恢复

### 备份
```bash
docker exec -t renewable_db pg_dumpall -c -U postgres > dump_`date +%d-%m-%Y"_"%H_%M_%S`.sql
```

### 恢复
```bash
cat your_dump.sql | docker exec -i renewable_db psql -U postgres
```
