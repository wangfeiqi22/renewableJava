# 清运站管理与费用计算 API 接口文档

## 1. 概述

本文档描述了清运站管理和费用计算功能的RESTful API接口。

**基础URL**: `http://localhost:8080/api`

---

## 2. 垃圾类型管理接口

### 2.1 获取所有可用垃圾类型

获取系统中所有启用的垃圾类型列表。

**请求**

```
GET /api/stations/waste-types
```

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "code": "CONSTRUCTION",
      "name": "建筑垃圾",
      "category": "建筑类",
      "description": "建筑施工产生的废弃物",
      "status": 1
    },
    {
      "id": 2,
      "code": "HOUSEHOLD",
      "name": "生活垃圾",
      "category": "生活类",
      "status": 1
    }
  ]
}
```

---

### 2.2 获取清运站的垃圾类型配置

获取指定清运站可接收的垃圾类型列表。

**请求**

```
GET /api/stations/{stationId}/waste-types
```

**路径参数**

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| stationId | Long | 是 | 清运站ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "stationId": 1,
      "wasteTypeId": 1,
      "pricePerCubic": 50.00,
      "status": 1,
      "wasteType": {
        "code": "CONSTRUCTION",
        "name": "建筑垃圾"
      }
    }
  ]
}
```

---

### 2.3 添加清运站可接收的垃圾类型

为指定清运站添加可接收的垃圾类型及价格。

**请求**

```
POST /api/stations/{stationId}/waste-types
Content-Type: application/json
```

**路径参数**

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| stationId | Long | 是 | 清运站ID |

**请求体**

```json
{
  "wasteTypeId": 1,
  "pricePerCubic": 50.00
}
```

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| wasteTypeId | Long | 是 | 垃圾类型ID |
| pricePerCubic | BigDecimal | 是 | 单价（元/立方米） |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "stationId": 1,
    "wasteTypeId": 1,
    "pricePerCubic": 50.00,
    "status": 1
  }
}
```

**错误响应**

```json
{
  "code": 500,
  "message": "该垃圾类型已配置",
  "data": null
}
```

---

### 2.4 更新垃圾类型价格

更新清运站特定垃圾类型的接收价格，并记录历史。

**请求**

```
PUT /api/stations/{stationId}/waste-types/{id}/price
Content-Type: application/json
X-User-Id: 123
```

**路径参数**

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| stationId | Long | 是 | 清运站ID |
| id | Long | 是 | 垃圾类型配置ID |

**请求体**

```json
{
  "newPrice": 55.00,
  "reason": "成本上涨，调整价格"
}
```

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| newPrice | BigDecimal | 是 | 新价格 |
| reason | String | 否 | 调整原因 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "stationId": 1,
    "wasteTypeId": 1,
    "pricePerCubic": 55.00,
    "status": 1
  }
}
```

---

### 2.5 删除清运站的垃圾类型配置

删除清运站可接收的垃圾类型配置。

**请求**

```
DELETE /api/stations/{stationId}/waste-types/{id}
```

**路径参数**

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| stationId | Long | 是 | 清运站ID |
| id | Long | 是 | 垃圾类型配置ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 2.6 获取价格变更历史

获取特定垃圾类型配置的价格变更历史记录。

**请求**

```
GET /api/stations/{stationId}/waste-types/{id}/price-history
```

**路径参数**

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| stationId | Long | 是 | 清运站ID |
| id | Long | 是 | 垃圾类型配置ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 2,
      "stationWasteTypeId": 1,
      "oldPrice": 50.00,
      "newPrice": 55.00,
      "changedBy": 123,
      "changeReason": "成本上涨，调整价格",
      "changedAt": "2025-01-01T10:00:00"
    },
    {
      "id": 1,
      "stationWasteTypeId": 1,
      "oldPrice": null,
      "newPrice": 50.00,
      "changedBy": 123,
      "changeReason": "初始配置",
      "changedAt": "2024-12-01T08:00:00"
    }
  ]
}
```

---

## 3. 费用计算接口

### 3.1 计算订单费用

根据垃圾类型、数量和位置计算订单费用。

**请求**

```
POST /api/orders/calculate-fee
Content-Type: application/json
```

**请求体**

```json
{
  "wasteType": "CONSTRUCTION",
  "estVolume": 5.0,
  "pickupLat": 39.9042,
  "pickupLon": 116.4074,
  "forkliftRequired": true,
  "containerCount": 1
}
```

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| wasteType | String | 是 | 垃圾类型代码 |
| estVolume | BigDecimal | 是 | 预估方量（立方米） |
| pickupLat | BigDecimal | 是 | 取货地点纬度 |
| pickupLon | BigDecimal | 是 | 取货地点经度 |
| forkliftRequired | Boolean | 否 | 是否需要叉车服务 |
| containerCount | Integer | 否 | 收集箱数量 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "stationId": 1,
    "stationName": "朝阳区清运站",
    "distance": 12.5,
    "transportFee": 335.00,
    "disposalFee": 250.00,
    "handlingFee": 150.00,
    "totalFee": 735.00,
    "breakdown": {
      "transportFee": 335.00,
      "disposalFee": 250.00,
      "handlingFee": 150.00,
      "totalFee": 735.00
    }
  }
}
```

**错误响应**

```json
{
  "code": 500,
  "message": "未找到可接收该垃圾类型的清运站",
  "data": null
}
```

---

### 3.2 确认订单费用

确认订单费用计算结果。

**请求**

```
POST /api/orders/{orderId}/confirm-fee
Content-Type: application/json
X-User-Id: 123
```

**路径参数**

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| orderId | Long | 是 | 订单ID |

**请求体**

```json
{
  "confirmedFee": 735.00,
  "adjustmentReason": "客户协商价格"
}
```

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| confirmedFee | BigDecimal | 否 | 确认费用（可调整） |
| adjustmentReason | String | 否 | 调整原因 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderId": 100,
    "transportFee": 335.00,
    "disposalFee": 250.00,
    "handlingFee": 150.00,
    "totalFee": 735.00,
    "isConfirmed": true,
    "confirmedAt": "2025-01-01T12:00:00",
    "confirmedBy": 123,
    "manualAdjustment": 0.00
  }
}
```

---

### 3.3 获取订单费用明细

获取订单的费用明细信息。

**请求**

```
GET /api/orders/{orderId}/fee-detail
```

**路径参数**

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderId": 100,
    "transportFee": 335.00,
    "disposalFee": 250.00,
    "handlingFee": 150.00,
    "totalFee": 735.00,
    "distanceKm": 12.5,
    "estimatedVolume": 5.0,
    "wasteTypePrice": 50.00,
    "forkliftRequired": true,
    "containerCount": 1,
    "isConfirmed": false
  }
}
```

---

## 4. 清运站匹配接口

### 4.1 查找最近的清运站

根据垃圾类型和位置查找最近的清运站。

**请求**

```
GET /api/stations/match?wasteType=CONSTRUCTION&lat=39.9042&lon=116.4074&limit=5
```

**查询参数**

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| wasteType | String | 是 | 垃圾类型代码 |
| lat | BigDecimal | 是 | 纬度 |
| lon | BigDecimal | 是 | 经度 |
| limit | Integer | 否 | 返回数量限制（默认5） |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "stationId": 1,
      "stationName": "朝阳区清运站",
      "stationAddress": "北京市朝阳区xxx路xx号",
      "distance": 5.2,
      "wasteTypePrice": 50.00
    },
    {
      "stationId": 2,
      "stationName": "海淀区清运站",
      "stationAddress": "北京市海淀区xxx路xx号",
      "distance": 12.8,
      "wasteTypePrice": 45.00
    }
  ]
}
```

---

## 5. 费用计算规则

### 5.1 运输费用

```
运输费用 = 起步价 + (距离 - 起步公里数) × 单价
```

**默认配置**

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| transport_base_price | 200 | 起步价（元） |
| transport_base_km | 5 | 起步包含公里数 |
| transport_price_per_km | 18 | 超出起步里程单价（元/公里） |

**计算示例**

- 距离 3km: 费用 = 200 元
- 距离 7km: 费用 = 200 + (7-5) × 18 = 236 元
- 距离 15km: 费用 = 200 + (15-5) × 18 = 380 元

---

### 5.2 消纳费用

```
消纳费用 = 垃圾类型单价 × 预估方量
```

**计算示例**

- 建筑垃圾单价 50元/方，预估 5方: 费用 = 50 × 5 = 250 元

---

### 5.3 搬运费用

```
搬运费用 = (叉车服务 × 100元) + (收集箱数量 × 50元)
```

**默认配置**

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| forklift_fee | 100 | 叉车服务费（元/次） |
| container_fee | 50 | 收集箱服务费（元/个） |

**计算示例**

- 需要叉车 + 1个收集箱: 费用 = 100 + 1 × 50 = 150 元

---

### 5.4 总费用

```
总费用 = 运输费用 + 消纳费用 + 搬运费用
```

---

## 6. 错误码

| 错误码 | 描述 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 7. 示例场景

### 7.1 创建订单并计算费用

**步骤1: 获取垃圾类型列表**

```bash
curl -X GET http://localhost:8080/api/stations/waste-types
```

**步骤2: 计算费用**

```bash
curl -X POST http://localhost:8080/api/orders/calculate-fee \
  -H "Content-Type: application/json" \
  -d '{
    "wasteType": "CONSTRUCTION",
    "estVolume": 5.0,
    "pickupLat": 39.9042,
    "pickupLon": 116.4074,
    "forkliftRequired": true,
    "containerCount": 1
  }'
```

**步骤3: 确认费用**

```bash
curl -X POST http://localhost:8080/api/orders/100/confirm-fee \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 123" \
  -d '{
    "confirmedFee": 735.00,
    "adjustmentReason": "客户协商价格"
  }'
```
