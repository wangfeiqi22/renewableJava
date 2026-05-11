# 司机选择功能测试文档

## 功能概述

在 `FleetOrderCreateView.vue` 组件中添加了司机选择功能，要求只能选择当前登录用户所属的"车队运营商"旗下的司机。

## 实现的功能

### 1. 获取当前用户的车队运营商ID
- 通过 `localStorage.getItem('user')` 获取当前登录用户信息
- 用户信息中包含 `fleetId` 字段，标识用户所属的车队运营商

### 2. 调用API获取该运营商下的司机列表
- **API端点**: `GET /api/fleet/drivers`
- **参数**:
  - `page`: 页码（默认0）
  - `size`: 每页数量（默认20）
  - `name`: 司机姓名（可选，用于搜索）
  - `status`: 司机状态（可选，如'AVAILABLE'）
- **后端实现**: 使用 `SecurityUtil.getCurrentUser().getFleetId()` 确保只返回当前用户车队的司机

### 3. 前端界面司机选择下拉框
- 使用 `el-select` 组件实现
- 支持远程搜索（filterable + remote）
- 显示司机姓名和电话号码
- 状态标签显示（空闲/已冻结）
- 冻结司机不可选（disabled）

### 4. 数据绑定和验证逻辑
- **必填验证**: 必须选择司机才能提交
- **有效性验证**: 选择的司机ID必须存在于司机列表中
- **状态验证**: 不能选择已冻结的司机
- **实时反馈**: 错误信息即时显示

### 5. 订单创建数据关联
- 将选中的司机ID（`driverId`）包含在订单创建请求中
- **API端点**: `POST /api/orders/fleet-create`
- **Payload示例**:
  ```json
  {
    "driverId": 1,
    "pickupAddress": "北京市朝阳区xxx",
    "wasteType": "recyclable",
    "estWeight": 10,
    "placementStatus": "已堆放到位",
    "additionalServices": "人工搬运,叉车服务",
    "wasteDesc": "备注信息"
  }
  ```

### 6. 权限校验
- **前端校验**: 
  - 只能选择列表中的司机（列表由后端根据fleetId过滤）
  - 冻结司机不可选
- **后端校验**:
  - `DriverService.getDriverById()` 验证司机是否属于当前用户车队
  - `DriverService.updateDriver()` 验证更新权限
  - `DriverService.deleteDriver()` 验证删除权限
  - 所有操作使用 `currentUser.getFleetId()` 进行数据隔离

## 测试用例

### 前端测试 (FleetOrderCreateView.test.js)

#### 1. 司机加载测试
- ✅ 测试从API加载司机列表
- ✅ 测试默认过滤可用司机
- ✅ 测试API错误处理

#### 2. 司机搜索测试
- ✅ 测试按姓名搜索司机
- ✅ 测试清空搜索词时重新加载全部司机

#### 3. 司机选择验证测试
- ✅ 测试必须选择司机
- ✅ 测试不能选择冻结的司机
- ✅ 测试选择的司机存在于列表
- ✅ 测试无效司机ID被拒绝

#### 4. 权限安全测试
- ✅ 测试只获取当前用户车队的司机
- ✅ 测试不能选择其他车队的司机
- ✅ 测试选择司机时验证车队归属

#### 5. 订单创建测试
- ✅ 测试订单载荷包含driverId
- ✅ 测试未选择司机时不能创建订单
- ✅ 测试冻结司机不能创建订单

#### 6. 状态显示测试
- ✅ 测试可用司机正确显示
- ✅ 测试冻结司机标记为禁用
- ✅ 测试司机姓名和电话显示

### 后端测试 (DriverControllerSecurityTest.java)

#### 1. 车队隔离测试
- ✅ 测试获取司机列表只返回当前车队司机
- ✅ 测试调度员只能看到自己车队的司机

#### 2. 所有权验证测试
- ✅ 测试可以访问自己车队的司机
- ✅ 测试不能访问其他车队的司机
- ✅ 测试可以更新自己车队的司机
- ✅ 测试不能更新其他车队的司机
- ✅ 测试可以删除自己车队的司机
- ✅ 测试不能删除其他车队的司机

#### 3. 认证测试
- ✅ 测试未认证请求返回401

## 如何运行测试

### 前端测试
```bash
cd /Users/ripple/renewableJava/frontend
npm test -- src/views/FleetOrderCreateView.test.js
```

### 后端测试
```bash
cd /Users/ripple/renewableJava/backend
./mvnw test -Dtest=DriverControllerSecurityTest
```

## 安全特性

### 数据隔离
- 每个车队的司机数据完全隔离
- API使用当前用户的`fleetId`进行数据过滤
- 无法通过修改请求参数访问其他车队数据

### 状态控制
- 冻结司机在UI中显示为禁用
- 前端和后端都验证司机状态
- 订单创建时再次确认司机有效性

### 审计追踪
- 所有操作记录创建人信息（createdBy, createdByName）
- 更新操作记录更新人信息
- 删除操作记录删除人信息

## 实现文件

### 前端
- `/Users/ripple/renewableJava/frontend/src/views/FleetOrderCreateView.vue` - 主要组件
- `/Users/ripple/renewableJava/frontend/src/views/FleetOrderCreateView.test.js` - 单元测试

### 后端
- `/Users/ripple/renewableJava/backend/src/main/java/com/renewable/ai/controller/DriverController.java` - API控制器
- `/Users/ripple/renewableJava/backend/src/main/java/com/renewable/ai/service/DriverService.java` - 业务逻辑
- `/Users/ripple/renewableJava/backend/src/test/java/com/renewable/ai/controller/DriverControllerSecurityTest.java` - 安全测试

## 注意事项

1. **后端安全**: 所有司机操作都通过`currentUser.getFleetId()`进行权限校验，前端无法绕过
2. **状态同步**: 司机状态可能在外系统中被修改，提交订单时会再次验证
3. **错误处理**: API调用失败时会显示错误消息并记录日志
4. **用户体验**: 提供清晰的加载状态、错误提示和成功反馈
