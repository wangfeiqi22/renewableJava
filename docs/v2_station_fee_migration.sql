-- =============================================
-- 清运站管理与费用计算功能增强 - 数据库迁移脚本
-- 版本: v2.0
-- 日期: 2025-01-01
-- =============================================

-- =============================================
-- 1. 垃圾类型字典表
-- =============================================
CREATE TABLE IF NOT EXISTS biz_waste_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 2. 清运站可接收垃圾类型关联表
-- =============================================
CREATE TABLE IF NOT EXISTS biz_station_waste_types (
    id BIGSERIAL PRIMARY KEY,
    station_id BIGINT NOT NULL,
    waste_type_id BIGINT NOT NULL,
    price_per_cubic DECIMAL(10,2) NOT NULL,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(station_id, waste_type_id)
);

CREATE INDEX IF NOT EXISTS idx_station_waste_types_station ON biz_station_waste_types(station_id);
CREATE INDEX IF NOT EXISTS idx_station_waste_types_waste_type ON biz_station_waste_types(waste_type_id);

-- =============================================
-- 3. 垃圾类型价格历史记录表
-- =============================================
CREATE TABLE IF NOT EXISTS biz_waste_price_history (
    id BIGSERIAL PRIMARY KEY,
    station_waste_type_id BIGINT NOT NULL,
    old_price DECIMAL(10,2),
    new_price DECIMAL(10,2) NOT NULL,
    changed_by BIGINT,
    change_reason VARCHAR(255),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_waste_price_history_station_waste_type ON biz_waste_price_history(station_waste_type_id);

-- =============================================
-- 4. 清运站详细地址表
-- =============================================
CREATE TABLE IF NOT EXISTS biz_station_addresses (
    id BIGSERIAL PRIMARY KEY,
    station_id BIGINT NOT NULL UNIQUE,
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    street VARCHAR(200),
    door_no VARCHAR(100),
    full_address VARCHAR(500),
    lat DECIMAL(9,6),
    lon DECIMAL(9,6),
    map_provider VARCHAR(20),
    map_place_id VARCHAR(100),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_station_addresses_station ON biz_station_addresses(station_id);
CREATE INDEX IF NOT EXISTS idx_station_addresses_location ON biz_station_addresses(lat, lon);

-- =============================================
-- 5. 费用计算配置表
-- =============================================
CREATE TABLE IF NOT EXISTS biz_fee_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 预置费用配置
INSERT INTO biz_fee_config (config_key, config_value, description) VALUES
('transport_base_price', '200', '运输起步价（元）'),
('transport_base_km', '5', '起步包含公里数'),
('transport_price_per_km', '18', '超出起步里程单价（元/公里）'),
('forklift_fee', '100', '叉车服务费（元/次）'),
('container_fee', '50', '收集箱服务费（元/个）')
ON CONFLICT (config_key) DO NOTHING;

-- =============================================
-- 6. 订单费用明细表
-- =============================================
CREATE TABLE IF NOT EXISTS biz_order_fee_details (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    transport_fee DECIMAL(10,2) DEFAULT 0,
    disposal_fee DECIMAL(10,2) DEFAULT 0,
    handling_fee DECIMAL(10,2) DEFAULT 0,
    total_fee DECIMAL(10,2) DEFAULT 0,
    distance_km DECIMAL(10,2),
    estimated_volume DECIMAL(10,2),
    waste_type_price DECIMAL(10,2),
    forklift_required BOOLEAN DEFAULT FALSE,
    container_count INTEGER DEFAULT 0,
    fee_breakdown JSONB,
    is_confirmed BOOLEAN DEFAULT FALSE,
    confirmed_at TIMESTAMP,
    confirmed_by BIGINT,
    manual_adjustment DECIMAL(10,2) DEFAULT 0,
    adjustment_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_fee_details_order ON biz_order_fee_details(order_id);

-- =============================================
-- 7. 修改现有表结构
-- =============================================

-- 为 biz_orders 表添加新字段
ALTER TABLE biz_orders ADD COLUMN IF NOT EXISTS matched_station_id BIGINT;
ALTER TABLE biz_orders ADD COLUMN IF NOT EXISTS match_distance_km DECIMAL(10,2);
ALTER TABLE biz_orders ADD COLUMN IF NOT EXISTS match_algorithm VARCHAR(50);
ALTER TABLE biz_orders ADD COLUMN IF NOT EXISTS fee_calculation_status VARCHAR(20) DEFAULT 'PENDING';
ALTER TABLE biz_orders ADD COLUMN IF NOT EXISTS pickup_address_province VARCHAR(50);
ALTER TABLE biz_orders ADD COLUMN IF NOT EXISTS pickup_address_city VARCHAR(50);
ALTER TABLE biz_orders ADD COLUMN IF NOT EXISTS pickup_address_district VARCHAR(50);

-- =============================================
-- 8. 预置垃圾类型数据
-- =============================================
INSERT INTO biz_waste_types (code, name, category, description) VALUES
('CONSTRUCTION', '建筑垃圾', '建筑类', '建筑施工产生的废弃物，包括砖块、混凝土、钢筋等'),
('HOUSEHOLD', '生活垃圾', '生活类', '日常生活产生的废弃物，包括厨余垃圾、塑料、纸张等'),
('INDUSTRIAL', '工业垃圾', '工业类', '工业生产产生的废弃物，包括金属废料、化工废料等'),
('MEDICAL', '医疗垃圾', '医疗类', '医疗机构产生的废弃物，包括针头、纱布、药品残液等'),
('HAZARDOUS', '危险废物', '危险类', '具有危险特性的废弃物，包括电池、油漆、溶剂等'),
('RENOVATION', '装修垃圾', '建筑类', '房屋装修产生的废弃物，包括木板、油漆桶、瓷砖等'),
('GARDEN', '园林垃圾', '生活类', '园林绿化产生的废弃物，包括树枝、草坪修剪物等'),
('ELECTRONIC', '电子垃圾', '工业类', '废旧电子电器产品，包括电脑、手机、电视等')
ON CONFLICT (code) DO NOTHING;

-- =============================================
-- 9. 创建视图
-- =============================================
CREATE OR REPLACE VIEW v_station_waste_types AS
SELECT
    swt.id,
    swt.station_id,
    s.name AS station_name,
    swt.waste_type_id,
    wt.code AS waste_type_code,
    wt.name AS waste_type_name,
    wt.category AS waste_category,
    swt.price_per_cubic,
    swt.status
FROM biz_station_waste_types swt
JOIN biz_stations s ON swt.station_id = s.id
JOIN biz_waste_types wt ON swt.waste_type_id = wt.id;

CREATE OR REPLACE VIEW v_order_fee_details AS
SELECT
    ofd.*,
    o.order_no,
    o.waste_type,
    o.est_volume,
    s.name AS station_name
FROM biz_order_fee_details ofd
JOIN biz_orders o ON ofd.order_id = o.id
LEFT JOIN biz_stations s ON o.matched_station_id = s.id;

-- =============================================
-- 完成
-- =============================================
