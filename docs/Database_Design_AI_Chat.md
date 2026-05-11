# 数据库设计文档 (AI 问答系统)

本系统基于现有的 MySQL 数据库架构，主要涉及用户表与 AI 问答相关的会话/消息表。

## 1. 用户表 (`biz_users`)
用于存储系统的各类用户（管理员、司机、清运站等）。

| 字段名 | 数据类型 | 长度 | 约束 | 描述 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 20 | PRIMARY KEY, AUTO_INCREMENT | 用户唯一标识 |
| username | VARCHAR | 50 | UNIQUE, NOT NULL | 登录用户名 |
| password_hash | VARCHAR | 255 | NOT NULL | 密码哈希值（BCrypt） |
| role | VARCHAR | 20 | NOT NULL | 角色 (admin, driver, property, station, user等) |
| phone | VARCHAR | 20 | | 联系电话 |
| status | INT | 1 | DEFAULT 1 | 状态：0=待审核，1=正常，2=拒绝 |
| created_at | DATETIME | | DEFAULT CURRENT_TIMESTAMP | 注册时间 |
| updated_at | DATETIME | | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

## 2. AI会话表 (`ai_chat_sessions`)
用于记录用户与AI的每一次对话会话，方便多轮上下文追溯。

| 字段名 | 数据类型 | 长度 | 约束 | 描述 |
| --- | --- | --- | --- | --- |
| id | VARCHAR | 36 | PRIMARY KEY | 会话唯一标识 (UUID) |
| user_id | BIGINT | 20 | NOT NULL, FOREIGN KEY | 关联的用户ID |
| title | VARCHAR | 100 | | 会话标题（可由AI自动生成摘要） |
| is_active | BOOLEAN | 1 | DEFAULT TRUE | 是否为当前活跃会话 |
| created_at | DATETIME | | DEFAULT CURRENT_TIMESTAMP | 会话创建时间 |
| updated_at | DATETIME | | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 会话最后更新时间 |

## 3. AI消息表 (`ai_chat_messages`)
用于记录单次对话中的每一条消息记录。

| 字段名 | 数据类型 | 长度 | 约束 | 描述 |
| --- | --- | --- | --- | --- |
| id | BIGINT | 20 | PRIMARY KEY, AUTO_INCREMENT | 消息ID |
| session_id | VARCHAR | 36 | NOT NULL, FOREIGN KEY | 所属会话ID |
| sender_type | VARCHAR | 10 | NOT NULL | 发送者类型：'user' 或 'ai' |
| content | TEXT | | NOT NULL | 消息正文内容 |
| action_payload| VARCHAR | 255 | | 附带的动作指令（例如需要跳转的前端路由路径） |
| created_at | DATETIME | | DEFAULT CURRENT_TIMESTAMP | 消息产生时间 |

## 4. 关系说明
- `biz_users` 1 : N `ai_chat_sessions` (一个用户可以发起多次独立对话)
- `ai_chat_sessions` 1 : N `ai_chat_messages` (一个会话包含多条消息往来)
