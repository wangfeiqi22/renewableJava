# 车队订单创建功能测试文档

## 📋 功能概述

车队订单创建功能允许车队运营商直接创建订单并指定司机，复用了普通用户创建清运单的表单字段和业务逻辑。

## 🛠 技术实现

### 前端
- **页面**: [FleetOrderCreateView.vue](file:///Users/ripple/renewableJava/frontend/src/views/FleetOrderCreateView.vue)
- **路由**: `/fleet/orders/create`

### 后端
- **API端点**: `POST /api/orders/fleet-create`
- **服务类**: [OrderService.java](file:///Users/ripple/renewableJava/backend/src/main/java/com/renewable/ai/service/OrderService.java#L152-L184)
- **控制器**: [OrderController.java](file:///Users/ripple/renewableJava/backend/src/main/java/com/renewable/ai/controller/OrderController.java#L24-59)

## ✅ 功能特点

1. **司机选择控件**
   - 仅显示当前车队运营商旗下的司机
   - 支持关键字搜索
   - 支持分页加载
   - 已冻结司机禁用显示

2. **表单字段**
   - 复用普通用户创建清运单的所有字段
   - 垃圾类型、预估方量、取货地址
   - 堆放情况、附加服务、备注信息

3. **业务规则**
   - 未选择司机时阻断提交
   - 重复提交检测（1分钟内同一地址）
   - 订单自动指派给司机（状态20）

## 🧪 测试用例

### 后端单元测试

**文件**: [OrderServiceFleetTest.java](file:///Users/ripple/renewableJava/backend/src/test/java/com/renewable/ai/service/OrderServiceFleetTest.java)

**测试场景**:

1. **testCreateFleetOrder_Success**
   - 正常创建车队订单
   - 验证订单号生成
   - 验证司机ID和车队ID绑定
   - 验证订单状态为20（已指派）

2. **testCreateFleetOrder_NoDriver**
   - 未选择司机时抛出异常
   - 错误消息："司机ID不能为空"

3. **testCreateFleetOrder_DuplicateSubmission**
   - 1分钟内重复提交同一地址
   - 错误消息："重复提交：您刚刚已在该地址创建了相同清运单"

4. **testCreateFleetOrder_WithAllFields**
   - 创建包含所有字段的订单
   - 验证所有字段正确保存

5. **testCreateFleetOrder_StatusFlow**
   - 验证订单状态流转
   - 20 (Assigned) → 25 (Accepted) → 30 (Loading)

**运行方式**:

```bash
cd backend
./mvnw test -Dtest=OrderServiceFleetTest
```

### 后端控制器测试

**文件**: [OrderControllerFleetCreateTest.java](file:///Users/ripple/renewableJava/backend/src/test/java/com/renewable/ai/controller/OrderControllerFleetCreateTest.java)

**测试场景**:

1. **testCreateFleetOrder_Success**
   - 验证API成功创建订单
   - 返回订单号和状态

2. **testCreateFleetOrder_NoDriver**
   - 未传司机ID时返回400错误
   - 错误消息："请选择司机"

3. **testCreateFleetOrder_ValidationErrors**
   - 缺少必填字段时返回400错误

**运行方式**:

```bash
cd backend
./mvnw test -Dtest=OrderControllerFleetCreateTest
```

### 端到端测试

**文件**: [test_fleet_order_e2e.py](file:///Users/ripple/renewableJava/test_fleet_order_e2e.py)

**测试场景**:

1. **test_fleet_order_creation**
   - 登录为车队所有者
   - 导航到订单创建页面
   - 验证司机选择控件存在
   - 测试未选择司机时的验证
   - 选择司机
   - 填写表单并提交
   - 验证订单创建成功
   - 验证跳转到订单列表

2. **test_fleet_order_no_driver**
   - 测试未选择司机时阻断提交
   - 验证错误提示

3. **test_fleet_order_frozen_driver**
   - 验证已冻结司机在列表中被禁用

**运行方式**:

```bash
# 确保前端和后端服务正在运行
# 前端: http://localhost:5173
# 后端: http://localhost:8080

cd renewableJava
python3 test_fleet_order_e2e.py
```

## 🔍 异常场景测试

### 1. 未选择司机
**预期行为**: 提交按钮被禁用，显示错误提示"请选择司机"

**测试方法**:
```python
# 填写地址但不选择司机
page.fill('input[placeholder="请输入取货地址"]', '测试地址')
page.locator('.btn-submit').click()

# 验证错误提示
error_text = page.locator('.error-text').first.inner_text()
assert error_text == '请选择司机'
```

### 2. 司机不属于当前运营商
**预期行为**: 司机列表仅显示当前车队司机，无法选择其他车队司机

**实现机制**:
```java
// DriverController.java
@GetMapping("/fleet/drivers")
public ResponseEntity<Page<Driver>> getFleetDrivers(...) {
    // 从当前用户获取 fleetId
    Long fleetId = getCurrentUserFleetId();
    // 仅查询该车队的司机
    return driverRepository.findByFleetId(fleetId, pageable);
}
```

### 3. 司机已被冻结
**预期行为**: 司机在选择列表中被禁用（is-disabled），无法选择

**实现机制**:
```vue
<!-- FleetOrderCreateView.vue -->
<el-option
  :value="driver.id"
  :disabled="driver.status === 'frozen'"
>
  ...
</el-option>
```

**前端验证**:
```javascript
if (selectedDriver.status === 'frozen') {
    ElMessage.error('该司机已被冻结，无法指派')
    return
}
```

## 📊 数据模型

### Order 实体字段

| 字段 | 类型 | 说明 |
|------|------|------|
| orderNo | String | 订单号（自动生成） |
| driverId | Long | 司机ID |
| fleetId | Long | 车队ID |
| pickupAddress | String | 取货地址 |
| wasteType | String | 垃圾类型 |
| estWeight | BigDecimal | 预估方量 |
| placementStatus | String | 堆放情况 |
| wasteDesc | String | 备注信息 |
| status | Integer | 订单状态（20=已指派） |
| sourceType | String | 来源类型（FLEET_CREATE） |

### API 请求格式

```json
POST /api/orders/fleet-create
{
  "pickupAddress": "测试取货地址",
  "wasteType": "recyclable",
  "estWeight": 5.5,
  "placementStatus": "已堆放到位",
  "wasteDesc": "测试备注",
  "driverId": 1,
  "fleetId": 100
}
```

### API 响应格式

```json
{
  "id": 1,
  "orderNo": "QY2024010100001",
  "driverId": 1,
  "fleetId": 100,
  "pickupAddress": "测试取货地址",
  "wasteType": "recyclable",
  "estWeight": 5.5,
  "placementStatus": "已堆放到位",
  "wasteDesc": "测试备注",
  "status": 20,
  "sourceType": "FLEET_CREATE",
  "createdAt": "2024-01-01T10:00:00"
}
```

## 🚀 部署检查清单

- [ ] 后端服务已启动
- [ ] 前端服务已启动
- [ ] 数据库已初始化
- [ ] 测试数据已准备（车队、司机）
- [ ] 后端单元测试通过
- [ ] 后端控制器测试通过
- [ ] 端到端测试通过
- [ ] 司机列表加载正常
- [ ] 表单验证生效
- [ ] 订单提交成功
- [ ] 订单状态正确（20）
- [ ] 跳转到订单列表成功

## 📝 注意事项

1. **司机列表权限**: 仅显示当前车队运营商的司机
2. **重复提交防护**: 1分钟内同一地址仅能提交一次
3. **订单状态**: 创建后直接进入"已指派"状态（20）
4. **日志记录**: 所有状态变更都会记录到 OrderLog 表

## 🎯 下一步优化

1. 添加司机空闲状态实时显示
2. 实现订单创建后的推送通知
3. 添加订单创建模板功能
4. 实现批量订单创建
5. 添加订单优先级设置
