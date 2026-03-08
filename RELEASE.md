# Release Notes - Smart Waste Management System v4.1

**Version:** 4.1.0  
**Build Date:** 2026-02-08  
**Environment:** Docker / Spring Boot 3 / Vue 3

## 🌟 New Features

### 1. User Module
- **Registration/Login**: Role-based access (User, Driver, Admin).
- **Security**: JWT-based authentication with SHA-256 password hashing.

### 2. Smart Order System
- **Order Creation**: Support for Household, Construction, Bulky, and Kitchen waste.
- **Order History**: View past orders and status updates.

### 3. AI Customer Service
- **Smart Chat**: Context-aware auto-replies.
- **Knowledge Base**: Configurable FAQ system (Pricing, Guide, etc.).
- **History**: Persistent chat logs per session.

### 4. Driver Workbench
- **Task List**: Real-time view of assigned orders.
- **Workflow**: Status transition (Loading -> Transit -> Arrived -> Done).
- **Evidence**: Photo upload capability for Loading/Unloading sites.

### 5. Admin Dashboard
- **Cockpit**: Real-time statistics (Total Orders, Pending, Completed).
- **Visualization**: ECharts integration for waste volume trends.
- **Management**: Station & Fleet management (CRUD).

## 📦 Deployment

### Prerequisites
- Docker Engine 20.10+
- Docker Compose v2+

### Quick Start
1. Extract the release archive.
2. Run `docker-compose up --build -d`.
3. Access the application:
   - Frontend: http://localhost
   - API Docs: http://localhost:8080/swagger-ui/index.html

### Default Credentials
- **Admin**: `admin` / `admin123`
- **Driver**: `driver1` / `123456`

## 🔧 Upgrade Notes
- Database schema will automatically initialize on the first run via `schema.sql`.
- Ensure port 80 and 8080 are free on the host machine.
