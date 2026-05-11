<template>
  <div class="fleet-grab-pool-container">
    <!-- Header -->
    <header class="page-header">
      <div class="header-left" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">抢单池</div>
      <div class="header-right">
        <el-button type="primary" size="small" @click="showRules = true">
          规则设置
        </el-button>
      </div>
    </header>

    <!-- Stats Banner -->
    <div class="stats-banner">
      <div class="stat-item">
        <div class="stat-icon purple">
          <el-icon><Box /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalOrders }}</div>
          <div class="stat-label">池中订单</div>
        </div>
      </div>

      <div class="stat-item">
        <div class="stat-icon green">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.activeOrders }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>

      <div class="stat-item">
        <div class="stat-icon orange">
          <el-icon><Lightning /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ grabRule?.maxOrdersPerDay || 0 }}</div>
          <div class="stat-label">今日已抢</div>
        </div>
      </div>
    </div>

    <!-- Info Card -->
    <div class="info-card">
      <div class="info-header">
        <el-icon><InfoFilled /></el-icon>
        <span>抢单规则</span>
      </div>
      <ul class="rule-list">
        <li>司机必须为可用状态才能抢单</li>
        <li>每个司机每天最多抢 <strong>{{ grabRule?.maxOrdersPerDay || 3 }}</strong> 单</li>
        <li>同一司机抢单间隔至少 <strong>{{ grabRule?.minIntervalMinutes || 30 }}</strong> 分钟</li>
        <li>超时未接单将自动退回抢单池</li>
      </ul>
    </div>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="pool-tabs">
      <el-tab-pane label="可抢订单" name="available">
        <template #label>
          <span class="tab-label">
            可抢订单
            <el-badge :value="stats.availableOrders" :max="99" class="tab-badge" />
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="进行中" name="in_progress" />
      <el-tab-pane label="已完成" name="completed" />
      <el-tab-pane label="已超时" name="expired" />
    </el-tabs>

    <!-- Search -->
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
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- Order List -->
    <div class="order-list" v-loading="loading">
      <div v-if="!loading && orders.length === 0" class="empty-state">
        <el-empty :description="emptyText" />
      </div>

      <div
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        :class="{ 'expiring': isExpiring(order) }"
      >
        <div class="order-header">
          <div class="order-id">#{{ order.id }}</div>
          <el-tag :type="getStatusType(order.grabStatus)" size="small">
            {{ getGrabStatusText(order.grabStatus) }}
          </el-tag>
        </div>

        <div class="order-content">
          <div class="address-row">
            <div class="address-item">
              <div class="address-icon pickup">
                <el-icon><Top /></el-icon>
              </div>
              <div class="address-text">{{ order.pickupAddress || '未设置' }}</div>
            </div>
            <div class="address-arrow">
              <el-icon><DArrowRight /></el-icon>
            </div>
            <div class="address-item">
              <div class="address-icon delivery">
                <el-icon><Bottom /></el-icon>
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
            <span class="meta-item" v-if="order.estimatedPrice">
              <el-icon><Money /></el-icon>
              ¥{{ order.estimatedPrice }}
            </span>
          </div>

          <div class="grab-info" v-if="order.grabbedBy">
            <el-icon><User /></el-icon>
            <span>{{ order.grabbedByDriver }}</span>
            <span class="grab-time">{{ formatTime(order.grabbedAt) }}</span>
          </div>

          <div class="time-remaining" v-if="order.grabStatus === 'AVAILABLE' && order.expiresAt">
            <el-progress
              :percentage="getRemainingPercentage(order)"
              :status="getProgressStatus(order)"
              :stroke-width="6"
            />
            <div class="time-text">
              剩余 {{ getRemainingTime(order) }}
            </div>
          </div>
        </div>

        <div class="order-actions">
          <el-button
            v-if="order.grabStatus === 'AVAILABLE'"
            type="primary"
            size="small"
            @click="handleGrab(order)"
          >
            <el-icon><Lightning /></el-icon>
            立即抢单
          </el-button>
          <el-button
            v-if="['GRABBED', 'IN_PROGRESS'].includes(order.grabStatus)"
            type="success"
            size="small"
            @click="handleViewDetail(order)"
          >
            查看详情
          </el-button>
          <el-button
            v-if="['GRABBED'].includes(order.grabStatus)"
            type="warning"
            size="small"
            @click="handleCancel(order)"
          >
            取消
          </el-button>
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

    <!-- Grab Rule Dialog -->
    <el-dialog
      v-model="showRules"
      title="抢单规则设置"
      width="90%"
    >
      <el-form :model="grabRule" label-width="120px">
        <el-form-item label="每日抢单上限">
          <el-input-number
            v-model="grabRule.maxOrdersPerDay"
            :min="1"
            :max="10"
          />
          <span class="form-tip">每个司机每天最多抢单数量</span>
        </el-form-item>

        <el-form-item label="抢单间隔">
          <el-input-number
            v-model="grabRule.minIntervalMinutes"
            :min="5"
            :max="120"
            :step="5"
          />
          <span class="form-tip">同一司机两次抢单的最小间隔（分钟）</span>
        </el-form-item>

        <el-form-item label="订单有效期">
          <el-input-number
            v-model="grabRule.orderValidMinutes"
            :min="5"
            :max="60"
          />
          <span class="form-tip">订单在抢单池中的有效期（分钟）</span>
        </el-form-item>

        <el-form-item label="启用状态">
          <el-switch v-model="grabRule.enabled" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showRules = false">取消</el-button>
        <el-button type="primary" @click="saveRules" :loading="submitting">
          保存设置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Search,
  Box,
  Clock,
  Lightning,
  InfoFilled,
  Top,
  Bottom,
  DArrowRight,
  Money,
  User
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
const activeTab = ref('available')
const showRules = ref(false)

// Stats
const stats = ref({
  totalOrders: 0,
  availableOrders: 0,
  activeOrders: 0
})

// Grab Rule
const grabRule = ref({
  maxOrdersPerDay: 3,
  minIntervalMinutes: 30,
  orderValidMinutes: 15,
  enabled: true
})

// Lifecycle
onMounted(() => {
  checkAuth()
  loadOrders()
  loadStats()
  loadGrabRule()

  // Auto refresh
  setInterval(() => {
    if (activeTab.value === 'available') {
      loadOrders()
      loadStats()
    }
  }, 10000)
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
      size: pageSize.value
    }

    if (activeTab.value === 'available') {
      params.status = 'AVAILABLE'
    } else if (activeTab.value === 'in_progress') {
      params.status = 'GRABBED'
    } else if (activeTab.value === 'completed') {
      params.status = 'COMPLETED'
    } else if (activeTab.value === 'expired') {
      params.status = 'EXPIRED'
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const response = await api.get('/fleet/grab-pool', { params })
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
    const response = await api.get('/fleet/grab-pool/stats')
    if (response.data.code === 200) {
      stats.value = response.data.data
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

const loadGrabRule = async () => {
  try {
    const response = await api.get('/fleet/grab-pool/rule')
    if (response.data.code === 200) {
      grabRule.value = response.data.data
    }
  } catch (error) {
    console.error('Failed to load grab rule:', error)
  }
}

const saveRules = async () => {
  submitting.value = true
  try {
    const response = await api.put('/fleet/grab-pool/rule', grabRule.value)
    if (response.data.code === 200) {
      ElMessage.success('规则保存成功')
      showRules.value = false
    } else {
      ElMessage.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('Failed to save rules:', error)
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
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

const handleGrab = (order) => {
  ElMessage.info('司机端可进行抢单操作')
}

const handleViewDetail = (order) => {
  router.push(`/fleet/grab-pool/order/${order.id}`)
}

const handleCancel = (order) => {
  ElMessageBox.confirm(
    `确定要取消订单 #${order.id} 的抢单吗？`,
    '取消抢单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    cancelGrab(order)
  }).catch(() => {})
}

const cancelGrab = async (order) => {
  try {
    const response = await api.post(`/fleet/grab-pool/cancel/${order.id}`)
    if (response.data.code === 200) {
      ElMessage.success('已取消抢单，订单退回抢单池')
      loadOrders()
      loadStats()
    } else {
      ElMessage.error(response.data.message || '取消失败')
    }
  } catch (error) {
    console.error('Failed to cancel grab:', error)
    ElMessage.error('取消失败')
  }
}

// Computed
const emptyText = computed(() => {
  const texts = {
    available: '暂无可抢订单',
    in_progress: '暂无进行中的订单',
    completed: '暂无已完成的订单',
    expired: '暂无超时订单'
  }
  return texts[activeTab.value] || '暂无订单'
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
    'AVAILABLE': 'success',
    'GRABBED': 'primary',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'info',
    'EXPIRED': 'danger'
  }
  return map[status] || 'info'
}

const getGrabStatusText = (status) => {
  const map = {
    'AVAILABLE': '可抢',
    'GRABBED': '已抢',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'EXPIRED': '已超时'
  }
  return map[status] || status
}

const isExpiring = (order) => {
  if (!order.expiresAt || order.grabStatus !== 'AVAILABLE') return false
  const remaining = new Date(order.expiresAt) - new Date()
  return remaining > 0 && remaining < 300000 // 5 minutes
}

const getRemainingTime = (order) => {
  if (!order.expiresAt) return ''
  const remaining = new Date(order.expiresAt) - new Date()
  if (remaining <= 0) return '已过期'

  const minutes = Math.floor(remaining / 60000)
  const seconds = Math.floor((remaining % 60000) / 1000)
  return `${minutes}分${seconds}秒`
}

const getRemainingPercentage = (order) => {
  if (!order.expiresAt) return 100
  const total = grabRule.value.orderValidMinutes * 60000
  const remaining = new Date(order.expiresAt) - new Date()
  return Math.max(0, Math.min(100, (remaining / total) * 100))
}

const getProgressStatus = (order) => {
  const percentage = getRemainingPercentage(order)
  if (percentage < 20) return 'exception'
  if (percentage < 50) return 'warning'
  return 'success'
}
</script>

<style scoped>
.fleet-grab-pool-container {
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
  min-width: 40px;
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

.stat-icon.purple {
  background: linear-gradient(135deg, #a855f7 0%, #7c3aed 100%);
}

.stat-icon.green {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
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

.info-card {
  margin: 12px 16px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 8px;
  border-left: 4px solid #f59e0b;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #92400e;
  margin-bottom: 8px;
}

.rule-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #78350f;
}

.rule-list li {
  margin-bottom: 4px;
}

.pool-tabs {
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

.order-list {
  padding: 0 16px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  transition: all 0.3s;
}

.order-card.expiring {
  border: 2px solid #f59e0b;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(245, 158, 11, 0.4);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(245, 158, 11, 0);
  }
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
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.address-item {
  flex: 1;
}

.address-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  margin-bottom: 4px;
}

.address-icon.pickup {
  background: #409eff;
}

.address-icon.delivery {
  background: #67c23a;
}

.address-text {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.address-arrow {
  color: #c0c4cc;
  font-size: 16px;
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

.grab-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  background: #f0f9ff;
  border-radius: 6px;
  font-size: 13px;
  color: #409eff;
  margin-top: 8px;
}

.grab-time {
  color: #999;
  font-size: 12px;
  margin-left: auto;
}

.time-remaining {
  margin-top: 8px;
}

.time-text {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.order-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
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

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}

@media (min-width: 768px) {
  .fleet-grab-pool-container {
    max-width: 480px;
    margin: 0 auto;
  }
}
</style>
