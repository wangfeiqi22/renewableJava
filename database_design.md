# 智慧垃圾清运系统数据库设计文档

## 1. 设计概述
本数据库设计基于《智慧垃圾清运系统需求规格说明书 v4.1》，采用 PostgreSQL 数据库。设计遵循第三范式（3NF），确保数据的一致性、完整性和扩展性。

## 2. 命名规范
- 表名：小写字母，下划线分隔，复数形式（如 `users`, `orders`）。
- 字段名：小写字母，下划线分隔（如 `user_id`, `created_at`）。
- 主键：`id` 或 `xxx_id`（自增或 UUID）。
- 外键：`xxx_id`，关联对应表的主键。

## 3. E-R 关系图概览
- **用户中心**：用户与角色多对一，用户与扩展信息一对一。
- **订单中心**：订单关联用户（下单人）、司机、车辆、清运站。
- **运力中心**：车队包含多名司机和多辆车。
- **商城中心**：清运站发布商品，用户购买生成商城订单。

## 4. 数据表详细设计

### 4.1 用户与权限模块

#### `sys_users` (系统用户表)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGSERIAL | PK | 用户ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| password_hash | VARCHAR(100) | NOT NULL | 密码哈希 |
| phone | VARCHAR(20) | UNIQUE, NOT NULL | 手机号 |
| role | VARCHAR(20) | NOT NULL | 角色：individual, property, street, station, fleet, driver, vip, admin |
| status | SMALLINT | DEFAULT 0 | 状态：0-待审核, 1-正常, 2-驳回, 3-禁用 |
| avatar_url | VARCHAR(255) | | 头像URL |
| created_at | TIMESTAMP | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 更新时间 |

#### `sys_user_profiles` (用户扩展信息表 - 用于审核)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| user_id | BIGINT | PK, FK(sys_users) | 用户ID |
| real_name | VARCHAR(50) | | 真实姓名/负责人姓名 |
| id_card_no | VARCHAR(20) | | 身份证号 |
| company_name | VARCHAR(100) | | 企业名称/物业名称/车队名称 |
| license_img_url | VARCHAR(255) | | 营业执照/身份证扫描件 |
| address | VARCHAR(200) | | 联系地址 |
| audit_remark | VARCHAR(255) | | 审核驳回理由 |
| audit_time | TIMESTAMP | | 审核时间 |

### 4.2 业务核心模块

#### `biz_orders` (清运订单表)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGSERIAL | PK | 订单ID |
| order_no | VARCHAR(32) | UNIQUE, NOT NULL | 订单编号 |
| creator_id | BIGINT | FK(sys_users) | 下单人ID |
| type | SMALLINT | NOT NULL | 类型：1-智能下单, 2-物业单, 3-街道单, 4-VIP单, 5-个人自建单 |
| status | SMALLINT | DEFAULT 10 | 状态：10-待接单, 20-已接单/已派单, 30-前往装车, 40-运输中, 50-已到站, 60-已完成, 90-已取消 |
| waste_type | VARCHAR(50) | | 垃圾类型（AI识别结果） |
| waste_desc | TEXT | | 垃圾描述 |
| est_weight | DECIMAL(10,2) | | 预估重量(kg) |
| est_volume | DECIMAL(10,2) | | 预估方量(m³) |
| pickup_address | VARCHAR(255) | NOT NULL | 起运地详细地址 |
| pickup_lat | DECIMAL(9,6) | | 起运地纬度 |
| pickup_lon | DECIMAL(9,6) | | 起运地经度 |
| station_id | BIGINT | FK(biz_stations) | 目的地清运站ID |
| fleet_id | BIGINT | FK(biz_fleets) | 接单车队ID |
| driver_id | BIGINT | FK(sys_users) | 执行司机ID |
| vehicle_id | BIGINT | FK(biz_vehicles) | 执行车辆ID |
| created_at | TIMESTAMP | DEFAULT NOW() | 创建时间 |

#### `biz_order_photos` (订单照片/存证表)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGSERIAL | PK | ID |
| order_id | BIGINT | FK(biz_orders) | 订单ID |
| node_type | VARCHAR(20) | NOT NULL | 节点：create(下单), load(装车), station_entry(进站), unload(卸货) |
| file_url | VARCHAR(255) | NOT NULL | 图片URL |
| gps_lat | DECIMAL(9,6) | | 拍摄点纬度 |
| gps_lon | DECIMAL(9,6) | | 拍摄点经度 |
| taken_at | TIMESTAMP | NOT NULL | 拍摄时间 |
| is_watermarked | BOOLEAN | DEFAULT FALSE | 是否已添加水印 |
| exif_data | JSONB | | 原始EXIF数据 |

#### `biz_stations` (清运站表)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGSERIAL | PK | 站点ID |
| name | VARCHAR(100) | NOT NULL | 站点名称 |
| type | SMALLINT | NOT NULL | 类型：1-清云站(终端), 2-中转站 |
| manager_id | BIGINT | FK(sys_users) | 站长ID |
| address | VARCHAR(255) | | 地址 |
| lat | DECIMAL(9,6) | | 纬度 |
| lon | DECIMAL(9,6) | | 经度 |
| region_code | VARCHAR(20) | | 行政区划代码 |

### 4.3 运力管理模块

#### `biz_fleets` (车队表)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGSERIAL | PK | 车队ID |
| name | VARCHAR(100) | NOT NULL | 车队名称 |
| manager_id | BIGINT | FK(sys_users) | 管理员ID |

#### `biz_vehicles` (车辆表)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGSERIAL | PK | 车辆ID |
| fleet_id | BIGINT | FK(biz_fleets) | 所属车队ID (个人司机可为空或虚拟车队) |
| plate_no | VARCHAR(20) | UNIQUE, NOT NULL | 车牌号 |
| type | VARCHAR(20) | | 车辆类型 |
| load_capacity | DECIMAL(10,2) | | 载重(吨) |
| status | SMALLINT | DEFAULT 1 | 状态：1-正常, 2-维修中 |

### 4.4 商城与VIP模块

#### `biz_products` (再生资源商品表)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGSERIAL | PK | 商品ID |
| station_id | BIGINT | FK(biz_stations) | 所属站点 |
| name | VARCHAR(100) | NOT NULL | 商品名称 |
| price | DECIMAL(10,2) | NOT NULL | 单价 |
| stock | DECIMAL(10,2) | DEFAULT 0 | 库存量 |
| unit | VARCHAR(10) | | 单位(吨/kg) |
| status | SMALLINT | DEFAULT 1 | 状态：1-上架, 0-下架 |

#### `biz_vip_contracts` (VIP合同表)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGSERIAL | PK | 合同ID |
| vip_user_id | BIGINT | FK(sys_users) | VIP客户ID |
| contract_no | VARCHAR(50) | UNIQUE | 合同编号 |
| start_date | DATE | | 开始日期 |
| end_date | DATE | | 结束日期 |
| content | TEXT | | 合同条款/服务标准 |

### 4.5 AI客服模块 (支撑 PRD v4.1 新增需求)

#### `ai_chat_sessions` (会话记录表)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | VARCHAR(64) | PK | 会话ID (UUID) |
| user_id | BIGINT | FK(sys_users) | 用户ID |
| created_at | TIMESTAMP | DEFAULT NOW() | 开始时间 |

#### `ai_chat_messages` (消息记录表)
| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGSERIAL | PK | 消息ID |
| session_id | VARCHAR(64) | FK(ai_chat_sessions) | 会话ID |
| sender_type | VARCHAR(10) | NOT NULL | 发送者：user, ai, human_agent |
| content | TEXT | NOT NULL | 消息内容 |
| msg_type | VARCHAR(10) | DEFAULT 'text' | 类型：text, image, voice |
| is_read | BOOLEAN | DEFAULT FALSE | 是否已读 |
| created_at | TIMESTAMP | DEFAULT NOW() | 发送时间 |
