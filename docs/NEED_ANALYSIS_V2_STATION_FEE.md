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

COMMENT ON TABLE biz_waste_types IS '垃圾类型字典表';
COMMENT ON COLUMN biz_waste_types.code IS '垃圾类型编码（建筑垃圾、生活垃圾等）';
COMMENT ON COLUMN biz_waste_types.name IS '垃圾类型名称';
COMMENT ON COLUMN biz_waste_types.category IS '分类：建筑类、生活类、工业类、医疗类、危险类';
COMMENT ON COLUMN biz_waste_types.status IS '状态：1-启用，0-禁用';

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

COMMENT ON TABLE biz_station_waste_types IS '清运站可接收垃圾类型关联表';
COMMENT ON COLUMN biz_station_waste_types.station_id IS '清运站ID';
COMMENT ON COLUMN biz_station_waste_types.waste_type_id IS '垃圾类型ID';
COMMENT ON COLUMN biz_station_waste_types.price_per_cubic IS '单价（元/立方米）';
COMMENT ON COLUMN biz_station_waste_types.status IS '状态：1-启用，0-禁用';

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
CREATE INDEX IF NOT EXISTS idx_waste_price_history_changed_at ON biz_waste_price_history(changed_at);

COMMENT ON TABLE biz_waste_price_history IS '垃圾类型价格历史记录表';
COMMENT ON COLUMN biz_waste_price_history.station_waste_type_id IS '清运站垃圾类型配置ID';
COMMENT ON COLUMN biz_waste_price_history.old_price IS '调整前价格';
COMMENT ON COLUMN biz_waste_price_history.new_price IS '调整后价格';
COMMENT ON COLUMN biz_waste_price_history.changed_by IS '操作人ID';
COMMENT ON COLUMN biz_waste_price_history.change_reason IS '调整原因';

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

COMMENT ON TABLE biz_station_addresses IS '清运站详细地址表';
COMMENT ON COLUMN biz_station_addresses.province IS '省份';
COMMENT ON COLUMN biz_station_addresses.city IS '城市';
COMMENT ON COLUMN biz_station_addresses.district IS '区县';
COMMENT ON COLUMN biz_station_addresses.street IS '街道';
COMMENT ON COLUMN biz_station_addresses.door_no IS '门牌号';
COMMENT ON COLUMN biz_station_addresses.full_address IS '完整地址';
COMMENT ON COLUMN biz_station_addresses.lat IS '纬度';
COMMENT ON COLUMN biz_station_addresses.lon IS '经度';
COMMENT ON COLUMN biz_station_addresses.map_provider IS '地图服务商（AMAP、BAIDU、TENCENT）';
COMMENT ON COLUMN biz_station_addresses.map_place_id IS '地图平台地点ID';
COMMENT ON COLUMN biz_station_addresses.is_verified IS '是否已验证';

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

COMMENT ON TABLE biz_fee_config IS '费用计算配置表';
COMMENT ON COLUMN biz_fee_config.config_key IS '配置键';
COMMENT ON COLUMN biz_fee_config.config_value IS '配置值';

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

COMMENT ON TABLE biz_order_fee_details IS '订单费用明细表';
COMMENT ON COLUMN biz_order_fee_details.order_id IS '订单ID';
COMMENT ON COLUMN biz_order_fee_details.transport_fee IS '运输费用';
COMMENT ON COLUMN biz_order_fee_details.disposal_fee IS '消纳费用';
COMMENT ON COLUMN biz_order_fee_details.handling_fee IS '搬运费用';
COMMENT ON COLUMN biz_order_fee_details.total_fee IS '总费用';
COMMENT ON COLUMN biz_order_fee_details.distance_km IS '运输距离（公里）';
COMMENT ON COLUMN biz_order_fee_details.estimated_volume IS '预估方量';
COMMENT ON COLUMN biz_order_fee_details.waste_type_price IS '垃圾类型单价';
COMMENT ON COLUMN biz_order_fee_details.forklift_required IS '是否需要叉车';
COMMENT ON COLUMN biz_order_fee_details.container_count IS '收集箱数量';
COMMENT ON COLUMN biz_order_fee_details.fee_breakdown IS '费用明细JSON';
COMMENT ON COLUMN biz_order_fee_details.is_confirmed IS '是否已确认';
COMMENT ON COLUMN biz_order_fee_details.confirmed_by IS '确认人ID';
COMMENT ON COLUMN biz_order_fee_details.manual_adjustment IS '人工调整金额';

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

COMMENT ON COLUMN biz_orders.matched_station_id IS '匹配清运站ID';
COMMENT ON COLUMN biz_orders.match_distance_km IS '匹配距离（公里）';
COMMENT ON COLUMN biz_orders.match_algorithm IS '匹配算法（GIS、MANUAL）';
COMMENT ON COLUMN biz_orders.fee_calculation_status IS '费用计算状态（PENDING、CALCULATED、CONFIRMED）';
COMMENT ON COLUMN biz_orders.pickup_address_province IS '取货地址-省份';
COMMENT ON COLUMN biz_orders.pickup_address_city IS '取货地址-城市';
COMMENT ON COLUMN biz_orders.pickup_address_district IS '取货地址-区县';

-- 添加外键约束（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_orders_matched_station'
    ) THEN
        ALTER TABLE biz_orders ADD CONSTRAINT fk_orders_matched_station
            FOREIGN KEY (matched_station_id) REFERENCES biz_stations(id);
    END IF;
END $$;

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
-- 9. 为 sys_users 表添加角色（如果不存在）
-- =============================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_sys_users_role'
    ) THEN
        ALTER TABLE sys_users DROP CONSTRAINT IF EXISTS sys_users_role_check;
        ALTER TABLE sys_users ADD CONSTRAINT sys_users_role_check
            CHECK (role IN ('individual', 'property', 'street', 'station', 'fleet', 'driver', 'vip', 'admin'));
    END IF;
END $$;

-- =============================================
-- 10. 创建更新触发器（自动更新 updated_at）
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_biz_waste_types_updated_at ON biz_waste_types;
CREATE TRIGGER update_biz_waste_types_updated_at
    BEFORE UPDATE ON biz_waste_types
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_biz_station_waste_types_updated_at ON biz_station_waste_types;
CREATE TRIGGER update_biz_station_waste_types_updated_at
    BEFORE UPDATE ON biz_station_waste_types
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_biz_station_addresses_updated_at ON biz_station_addresses;
CREATE TRIGGER update_biz_station_addresses_updated_at
    BEFORE UPDATE ON biz_station_addresses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_biz_fee_config_updated_at ON biz_fee_config;
CREATE TRIGGER update_biz_fee_config_updated_at
    BEFORE UPDATE ON biz_fee_config
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- 11. 创建视图（方便查询）
-- =============================================

-- 清运站可接收垃圾类型视图
CREATE OR REPLACE VIEW v_station_waste_types AS
SELECT
    swt.id,
    swt.station_id,
    s.name AS station_name,
    swt.waste_type_id,
    wt.code AS waste_type_code,
    wt.name AS waste_type_name,
    wt.category AS waste_category,
    wt.description AS waste_description,
    swt.price_per_cubic,
    swt.status,
    swt.created_at,
    swt.updated_at
FROM biz_station_waste_types swt
JOIN biz_stations s ON swt.station_id = s.id
JOIN biz_waste_types wt ON swt.waste_type_id = wt.id;

-- 订单费用明细视图
CREATE OR REPLACE VIEW v_order_fee_details AS
SELECT
    ofd.*,
    o.order_no,
    o.waste_type,
    o.est_volume,
    o.pickup_address,
    s.name AS station_name,
    s.address AS station_address
FROM biz_order_fee_details ofd
JOIN biz_orders o ON ofd.order_id = o.id
LEFT JOIN biz_stations s ON o.matched_station_id = s.id;

-- =============================================
-- 12. 创建序列（如果需要）
-- =============================================
CREATE SEQUENCE IF NOT EXISTS seq_biz_waste_types START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_biz_station_waste_types START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_biz_waste_price_history START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_biz_station_addresses START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_biz_fee_config START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_biz_order_fee_details START WITH 1 INCREMENT BY 1;

-- =============================================
-- 13. 权限设置（根据实际需求调整）
-- =============================================
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- =============================================
-- 完成
-- =============================================
