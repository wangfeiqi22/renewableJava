<template>
  <div class="fleet-dispatch-container">
    <!-- Header -->
    <header class="page-header">
      <div class="header-left" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">调度中心</div>
      <div class="header-right"></div>
    </header>

    <!-- Real-time Stats -->
    <div class="stats-banner">
      <div class="stat-item">
        <div class="stat-icon blue">
          <el-icon><Van /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.availableDrivers }}</div>
          <div class="stat-label">空闲司机</div>
        </div>
      </div>

      <div class="stat-item">
        <div class="stat-icon orange">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingOrders }}</div>
          <div class="stat-label">待指派订单</div>
        </div>
      </div>

      <div class="stat-item">
        <div class="stat-icon green">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.inProgressOrders }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="dispatch-tabs">
      <el-tab-pane label="待调度" name="pending">
        <template #label>
          <span class="tab-label">
            待调度
            <el-badge :value="stats.pendingOrders" :max="99" class="tab-badge" />
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="进行中" name="in_progress" />
      <el-tab-pane label="全部" name="all" />
    </el-tabs>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索订单号/地址"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch" :loading="loading">
        搜索
      </el-button>
    </div>

    <!-- Dispatch List -->
    <div class="dispatch-list" v-loading="loading">
      <div v-if="!loading && orders.length === 0" class="empty-state">
        <el-empty description="暂无待调度订单" />
      </div>

      <div
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        :class="{ 'in-progress': order.status !== 'PENDING' }"
      >
        <div class="order-header">
          <div class="order-id">#{{ order.id }}</div>
          <el-tag :type="getStatusType(order.status)" size="small">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>

        <div class="order-content">
          <div class="address-row">
            <div class="address-item pickup">
              <div class="address-label">
                <el-icon><Top /></el-icon>
                取货
              </div>
              <div class="address-text">{{ order.pickupAddress || '未设置' }}</div>
            </div>
            <div class="address-line"></div>
            <div class="address-item delivery">
              <div class="address-label">
                <el-icon><Bottom /></el-icon>
                送货
              </div>
              <div class="address-text">{{ order.deliveryAddress || '未设置' }}</div>
            </div>
          </div>

          <div class="order-meta">
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatTime(order.createdAt) }}
            </span>
            <span class="meta-item" v-if="order.weight">
              <el-icon><Box /></el-icon>
              {{ order.weight }}kg
            </span>
            <span class="meta-item" v-if="order.wasteType">
              <el-icon><Collection /></el-icon>
              {{ getWasteTypeText(order.wasteType) }}
            </span>
          </div>

          <div class="driver-info" v-if="order.driverName">
            <el-icon><Van /></el-icon>
            <span>{{ order.driverName }}</span>
            <span class="driver-phone">{{ order.driverPhone }}</span>
          </div>
        </div>

        <div class="order-actions">
          <div class="price-info">
            <span class="price-label">预估</span>
            <span class="price-value">¥ {{ (order.estimatedPrice || 0).toFixed(0) }}</span>
          </div>

          <div class="action-buttons">
            <el-button
              v-if="order.status === 'PENDING'"
              type="primary"
              size="small"
              @click="handleAssign(order)"
            >
              指派司机
            </el-button>
            <el-button
              v-if="order.status === 'PENDING'"
              type="warning"
              size="small"
              plain
              @click="handleToPool(order)"
            >
              加入抢单池
            </el-button>
            <el-button
              v-if="['ASSIGNED', 'IN_PROGRESS'].includes(order.status)"
              type="info"
              size="small"
              @click="handleReassign(order)"
            >
              重新指派
            </el-button>
            <el-button
              v-if="['ASSIGNED', 'IN_PROGRESS'].includes(order.status)"
              type="success"
              size="small"
              @click="handleTrack(order)"
            >
              位置追踪
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination-wrapper" v-if="totalElements > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalElements"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- Assign Driver Dialog -->
    <el-dialog
      v-model="assignDialogVisible"
      title="指派司机"
      width="90%"
    >
      <div class="assign-form">
        <p class="assign-tip">请为订单 #{{ selectedOrder?.id }} 选择司机</p>

        <el-form-item label="选择司机">
          <el-select
            v-model="selectedDriverId"
            placeholder="请选择可用司机"
            filterable
            style="width: 100%"
          >
            <el-option-group label="可用司机">
              <el-option
                v-for="driver in availableDrivers"
                :key="driver.id"
                :label="`${driver.name} - ${driver.vehicleNumber}`"
                :value="driver.id"
              >
                <div class="driver-option">
                  <span>{{ driver.name }}</span>
                  <el-tag size="small" type="success">空闲</el-tag>
                  <span class="driver-vehicle">{{ driver.vehicleNumber }}</span>
                </div>
              </el-option>
            </el-option-group>
            <el-option-group label="忙碌司机">
              <el-option
                v-for="driver in busyDrivers"
                :key="driver.id"
                :label="`${driver.name} - ${driver.vehicleNumber}`"
                :value="driver.id"
                disabled
              >
                <div class="driver-option">
                  <span>{{ driver.name }}</span>
                  <el-tag size="small" type="warning">忙碌</el-tag>
                  <span class="driver-vehicle">{{ driver.vehicleNumber }}</span>
                </div>
              </el-option>
            </el-option-group>
          </el-select>
        </el-form-item>

        <div v-if="selectedDriver" class="selected-driver-card">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="司机姓名">
              {{ selectedDriver.name }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ selectedDriver.phone }}
            </el-descriptions-item>
            <el-descriptions-item label="车牌号">
              {{ selectedDriver.vehicleNumber }}
            </el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag size="small" :type="selectedDriver.status === 'AVAILABLE' ? 'success' : 'warning'">
                {{ selectedDriver.status === 'AVAILABLE' ? '可用' : '忙碌' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="今日完成">
              {{ selectedDriver.completedOrders || 0 }} 单
            </el-descriptions-item>
            <el-descriptions-item label="驾照类型">
              {{ selectedDriver.licenseType || 'C1' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign" :loading="submitting" :disabled="!selectedDriverId">
          确认指派
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Search,
  Van,
  Document,
  Clock,
  Box,
  Collection,
  Top,
  Bottom
} from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()

// State
const loading = ref(false)
const submitting = ref(false)
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const searchKeyword = ref('')
const activeTab = ref('pending')

// Stats
const stats = ref({
  availableDrivers: 0,
  pendingOrders: 0,
  inProgressOrders: 0
})

// Drivers
const availableDrivers = ref([])
const busyDrivers = ref([])

// Dialogs
const assignDialogVisible = ref(false)
const selectedOrder = ref(null)
const selectedDriverId = ref(null)

// Timer for auto-refresh
let refreshTimer = null

// Lifecycle
onMounted(() => {
  checkAuth()
  loadOrders()
  loadStats()
  loadDrivers()

  // Auto refresh every 30 seconds
  refreshTimer = setInterval(() => {
    loadOrders()
    loadStats()
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

const checkAuth = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (!user.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!['fleet_owner', 'fleet_dispatcher'].includes(user.role)) {
    ElMessage.warning('该页面仅限车队运营用户访问')
    router.push('/home')
  }
}

// API Calls
const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: 'createdAt',
      direction: 'desc'
    }

    if (activeTab.value === 'pending') {
      params.status = 'PENDING'
    } else if (activeTab.value === 'in_progress') {
      params.statusIn = 'ASSIGNED,IN_PROGRESS'
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const response = await api.get('/fleet/orders', { params })
    if (response.data.code === 200) {
      orders.value = response.data.data.content || []
      totalElements.value = response.data.data.totalElements || 0
    }
  } catch (error) {
    console.error('Failed to load orders:', error)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    // Get driver stats
    const driverResponse = await api.get('/fleet/drivers', {
      params: { page: 0, size: 100 }
    })
    if (driverResponse.data.code === 200) {
      const drivers = driverResponse.data.data.content || []
      stats.value.availableDrivers = drivers.filter(d => d.status === 'AVAILABLE').length
    }

    // Get order stats
    const orderResponse = await api.get('/fleet/orders', {
      params: { page: 0, size: 1, status: 'PENDING' }
    })
    if (orderResponse.data.code === 200) {
      stats.value.pendingOrders = orderResponse.data.data.totalElements || 0
    }

    const inProgressResponse = await api.get('/fleet/orders', {
      params: { page: 0, size: 1, statusIn: 'ASSIGNED,IN_PROGRESS' }
    })
    if (inProgressResponse.data.code === 200) {
      stats.value.inProgressOrders = inProgressResponse.data.data.totalElements || 0
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

const loadDrivers = async () => {
  try {
    const response = await api.get('/fleet/drivers', {
      params: { page: 0, size: 100 }
    })
    if (response.data.code === 200) {
      const drivers = response.data.data.content || []
      availableDrivers.value = drivers.filter(d => d.status === 'AVAILABLE')
      busyDrivers.value = drivers.filter(d => d.status === 'BUSY')
    }
  } catch (error) {
    console.error('Failed to load drivers:', error)
  }
}

const assignOrder = async () => {
  if (!selectedDriverId.value) {
    ElMessage.warning('请选择司机')
    return
  }

  submitting.value = true
  try {
    const response = await api.post(
      `/fleet/orders/${selectedOrder.value.id}/assign?driverId=${selectedDriverId.value}`
    )
    if (response.data.code === 200) {
      ElMessage.success('指派成功')
      assignDialogVisible.value = false
      selectedDriverId.value = null
      selectedOrder.value = null
      loadOrders()
      loadStats()
      loadDrivers()
    } else {
      ElMessage.error(response.data.message || '指派失败')
    }
  } catch (error) {
    console.error('Failed to assign order:', error)
    ElMessage.error(error.response?.data?.message || '指派失败')
  } finally {
    submitting.value = false
  }
}

const addToPool = async (order) => {
  try {
    const response = await api.post(`/fleet/grab-pool/add/${order.id}`)
    if (response.data.code === 200) {
      ElMessage.success('已加入抢单池')
      loadOrders()
    } else {
      ElMessage.error(response.data.message || '添加失败')
    }
  } catch (error) {
    console.error('Failed to add to pool:', error)
    ElMessage.error('添加失败')
  }
}

// Event Handlers
const handleTabChange = () => {
  currentPage.value = 1
  loadOrders()
}

const handleSearch = () => {
  currentPage.value = 1
  loadOrders()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadOrders()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadOrders()
}

const handleAssign = (order) => {
  selectedOrder.value = order
  selectedDriverId.value = null
  assignDialogVisible.value = true
}

const handleToPool = (order) => {
  ElMessageBox.confirm(
    `确定将订单 #${order.id} 加入抢单池吗？`,
    '加入抢单池',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    addToPool(order)
  }).catch(() => {})
}

const handleReassign = (order) => {
  selectedOrder.value = order
  selectedDriverId.value = null
  assignDialogVisible.value = true
}

const handleTrack = (order) => {
  ElMessage.info(`正在追踪订单 #${order.id} 的位置...`)
  router.push(`/fleet/order-track/${order.id}`)
}

const confirmAssign = () => {
  assignOrder()
}

// Computed
const selectedDriver = computed(() => {
  if (!selectedDriverId.value) return null
  return [...availableDrivers.value, ...busyDrivers.value].find(d => d.id === selectedDriverId.value)
})

// Helpers
const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'ASSIGNED': 'primary',
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待指派',
    'ASSIGNED': '已指派',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const getWasteTypeText = (type) => {
  const map = {
    'HOUSEHOLD': '生活垃圾',
    'CONSTRUCTION': '建筑垃圾',
    'MEDICAL': '医疗垃圾',
    'INDUSTRIAL': '工业垃圾',
    'ELECTRONIC': '电子垃圾',
    'OTHER': '其他'
  }
  return map[type] || type
}
</script>

<style scoped>
.fleet-dispatch-container {
  min-height: 100vh;
  background-color: #f5f7f9;
  padding-bottom: 80px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 56px;
  background-color: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-left,
.header-right {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #333;
  cursor: pointer;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.stats-banner {
  display: flex;
  padding: 16px;
  gap: 12px;
  background: #fff;
}

.stat-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.green {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 11px;
  color: #999;
}

.dispatch-tabs {
  background: #fff;
  padding: 0 16px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tab-badge {
  margin-left: 4px;
}

.filter-bar {
  padding: 12px 16px;
  background: #fff;
  display: flex;
  gap: 8px;
}

.search-input {
  flex: 1;
}

.dispatch-list {
  padding: 0 16px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.order-card.in-progress {
  border-left: 3px solid #409eff;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-id {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  font-family: monospace;
}

.order-content {
  margin-bottom: 12px;
}

.address-row {
  position: relative;
  padding-left: 20px;
  margin-bottom: 12px;
}

.address-row::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, #409eff 0%, #67c23a 100%);
}

.address-item {
  margin-bottom: 8px;
}

.address-item:last-child {
  margin-bottom: 0;
}

.address-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #999;
  margin-bottom: 2px;
}

.address-text {
  font-size: 13px;
  color: #333;
}

.order-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.driver-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  background: #f0f9ff;
  border-radius: 6px;
  font-size: 13px;
  color: #409eff;
}

.driver-phone {
  color: #666;
  font-size: 12px;
}

.order-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.price-info {
  display: flex;
  flex-direction: column;
}

.price-label {
  font-size: 11px;
  color: #999;
}

.price-value {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  padding: 16px;
  display: flex;
  justify-content: center;
  background: #fff;
}

.empty-state {
  padding: 40px 0;
}

.assign-form {
  padding: 16px 0;
}

.assign-tip {
  margin-bottom: 16px;
  color: #666;
}

.selected-driver-card {
  margin-top: 16px;
}

.driver-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.driver-vehicle {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

@media (min-width: 768px) {
  .fleet-dispatch-container {
    max-width: 480px;
    margin: 0 auto;
  }
}
</style>
