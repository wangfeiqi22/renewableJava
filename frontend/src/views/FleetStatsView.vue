<template>
  <div class="fleet-stats-container">
    <!-- Header -->
    <header class="page-header">
      <div class="header-left" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">数据统计</div>
      <div class="header-right">
        <el-button type="primary" size="small" @click="handleExport">
          导出报表
        </el-button>
      </div>
    </header>

    <!-- Time Range Selector -->
    <div class="time-selector">
      <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
        <el-radio-button label="today">今日</el-radio-button>
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
        <el-radio-button label="year">本年</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Overview Stats -->
    <div class="overview-section">
      <div class="section-title">运营概览</div>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon blue">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overviewStats.totalOrders }}</div>
            <div class="stat-label">订单总数</div>
            <div class="stat-trend" :class="overviewStats.ordersTrend >= 0 ? 'up' : 'down'">
              {{ overviewStats.ordersTrend >= 0 ? '↑' : '↓' }}
              {{ Math.abs(overviewStats.ordersTrend) }}%
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon green">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overviewStats.completedOrders }}</div>
            <div class="stat-label">已完成</div>
            <div class="stat-trend" :class="overviewStats.completionTrend >= 0 ? 'up' : 'down'">
              {{ overviewStats.completionTrend >= 0 ? '↑' : '↓' }}
              {{ Math.abs(overviewStats.completionTrend) }}%
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon orange">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ formatNumber(overviewStats.totalRevenue) }}</div>
            <div class="stat-label">总收入</div>
            <div class="stat-trend" :class="overviewStats.revenueTrend >= 0 ? 'up' : 'down'">
              {{ overviewStats.revenueTrend >= 0 ? '↑' : '↓' }}
              {{ Math.abs(overviewStats.revenueTrend) }}%
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon purple">
            <el-icon><Van /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overviewStats.activeDrivers }}</div>
            <div class="stat-label">活跃司机</div>
            <div class="stat-trend" :class="overviewStats.driversTrend >= 0 ? 'up' : 'down'">
              {{ overviewStats.driversTrend >= 0 ? '↑' : '↓' }}
              {{ Math.abs(overviewStats.driversTrend) }}%
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Order Statistics -->
    <div class="stats-section">
      <div class="section-title">订单统计</div>
      <div class="stats-detail">
        <div class="detail-item">
          <div class="detail-label">待处理订单</div>
          <div class="detail-value warning">{{ orderStats.pendingOrders }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">进行中订单</div>
          <div class="detail-value primary">{{ orderStats.inProgressOrders }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">已完成订单</div>
          <div class="detail-value success">{{ orderStats.completedOrders }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">已取消订单</div>
          <div class="detail-value info">{{ orderStats.cancelledOrders }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">完成率</div>
          <div class="detail-value">{{ orderStats.completionRate }}%</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">平均处理时长</div>
          <div class="detail-value">{{ orderStats.avgProcessingTime }}分钟</div>
        </div>
      </div>
    </div>

    <!-- Revenue Statistics -->
    <div class="stats-section">
      <div class="section-title">收入统计</div>
      <div class="stats-detail">
        <div class="detail-item">
          <div class="detail-label">总收入</div>
          <div class="detail-value primary">¥{{ formatNumber(revenueStats.totalRevenue) }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">日均收入</div>
          <div class="detail-value">¥{{ formatNumber(revenueStats.avgDailyRevenue) }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">项目收入</div>
          <div class="detail-value success">¥{{ formatNumber(revenueStats.projectRevenue) }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">抢单收入</div>
          <div class="detail-value">¥{{ formatNumber(revenueStats.grabRevenue) }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">平均客单价</div>
          <div class="detail-value">¥{{ formatNumber(revenueStats.avgOrderValue) }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">最高单笔</div>
          <div class="detail-value">¥{{ formatNumber(revenueStats.maxOrderValue) }}</div>
        </div>
      </div>
    </div>

    <!-- Driver Rankings -->
    <div class="stats-section">
      <div class="section-title">司机排行榜</div>
      <div class="ranking-list">
        <div
          v-for="(driver, index) in driverRankings"
          :key="driver.id"
          class="ranking-item"
        >
          <div class="rank-badge" :class="'rank-' + (index + 1)">
            {{ index + 1 }}
          </div>
          <div class="driver-info">
            <div class="driver-name">{{ driver.name }}</div>
            <div class="driver-stats">
              <span>{{ driver.completedOrders }} 单</span>
              <span>¥{{ formatNumber(driver.revenue) }}</span>
            </div>
          </div>
          <div class="driver-rating">
            <el-icon><Star /></el-icon>
            {{ driver.rating || '5.0' }}
          </div>
        </div>
      </div>
    </div>

    <!-- Order Distribution -->
    <div class="stats-section">
      <div class="section-title">订单分布</div>
      <div class="distribution-list">
        <div
          v-for="item in orderDistribution"
          :key="item.type"
          class="distribution-item"
        >
          <div class="distribution-label">
            <span class="type-name">{{ getWasteTypeName(item.type) }}</span>
            <span class="type-count">{{ item.count }} 单</span>
          </div>
          <el-progress
            :percentage="item.percentage"
            :stroke-width="8"
            :color="getProgressColor(item.percentage)"
          />
        </div>
      </div>
    </div>

    <!-- Recent Activity -->
    <div class="stats-section">
      <div class="section-title">近期动态</div>
      <div class="activity-list">
        <div
          v-for="activity in recentActivities"
          :key="activity.id"
          class="activity-item"
        >
          <div class="activity-icon" :class="activity.type">
            <el-icon>
              <component :is="getActivityIcon(activity.type)" />
            </el-icon>
          </div>
          <div class="activity-content">
            <div class="activity-text">{{ activity.description }}</div>
            <div class="activity-time">{{ formatTime(activity.createdAt) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Document,
  CircleCheck,
  Money,
  Van,
  Star,
  Plus,
  Check,
  Clock,
  Warning
} from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()

// State
const timeRange = ref('week')
const loading = ref(false)

// Stats Data
const overviewStats = ref({
  totalOrders: 0,
  completedOrders: 0,
  totalRevenue: 0,
  activeDrivers: 0,
  ordersTrend: 0,
  completionTrend: 0,
  revenueTrend: 0,
  driversTrend: 0
})

const orderStats = ref({
  pendingOrders: 0,
  inProgressOrders: 0,
  completedOrders: 0,
  cancelledOrders: 0,
  completionRate: 0,
  avgProcessingTime: 0
})

const revenueStats = ref({
  totalRevenue: 0,
  avgDailyRevenue: 0,
  projectRevenue: 0,
  grabRevenue: 0,
  avgOrderValue: 0,
  maxOrderValue: 0
})

const driverRankings = ref([])
const orderDistribution = ref([])
const recentActivities = ref([])

// Lifecycle
onMounted(() => {
  checkAuth()
  loadAllStats()
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
const loadAllStats = async () => {
  loading.value = true
  try {
    const response = await api.get('/fleet/stats', {
      params: { timeRange: timeRange.value }
    })
    if (response.data.code === 200) {
      const data = response.data.data
      overviewStats.value = data.overview || {}
      orderStats.value = data.orders || {}
      revenueStats.value = data.revenue || {}
      driverRankings.value = data.driverRankings || []
      orderDistribution.value = data.orderDistribution || []
      recentActivities.value = data.recentActivities || []
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
    loadMockData()
  } finally {
    loading.value = false
  }
}

const loadMockData = () => {
  overviewStats.value = {
    totalOrders: 156,
    completedOrders: 142,
    totalRevenue: 45680,
    activeDrivers: 8,
    ordersTrend: 12.5,
    completionTrend: 8.3,
    revenueTrend: 15.2,
    driversTrend: 0
  }

  orderStats.value = {
    pendingOrders: 5,
    inProgressOrders: 9,
    completedOrders: 142,
    cancelledOrders: 8,
    completionRate: 91,
    avgProcessingTime: 45
  }

  revenueStats.value = {
    totalRevenue: 45680,
    avgDailyRevenue: 6525,
    projectRevenue: 32000,
    grabRevenue: 13680,
    avgOrderValue: 293,
    maxOrderValue: 1500
  }

  driverRankings.value = [
    { id: 1, name: '李师傅', completedOrders: 45, revenue: 13500, rating: 4.9 },
    { id: 2, name: '王师傅', completedOrders: 38, revenue: 11200, rating: 4.8 },
    { id: 3, name: '张师傅', completedOrders: 32, revenue: 9600, rating: 4.7 },
    { id: 4, name: '赵师傅', completedOrders: 27, revenue: 8100, rating: 4.8 },
    { id: 5, name: '刘师傅', completedOrders: 22, revenue: 6600, rating: 4.6 }
  ]

  orderDistribution.value = [
    { type: 'HOUSEHOLD', count: 68, percentage: 43.6 },
    { type: 'CONSTRUCTION', count: 45, percentage: 28.8 },
    { type: 'INDUSTRIAL', count: 28, percentage: 17.9 },
    { type: 'MEDICAL', count: 10, percentage: 6.4 },
    { type: 'OTHER', count: 5, percentage: 3.2 }
  ]

  recentActivities.value = [
    { id: 1, type: 'order_complete', description: '订单 #1234 已完成', createdAt: new Date() },
    { id: 2, type: 'driver_grab', description: '李师傅抢单成功', createdAt: new Date(Date.now() - 300000) },
    { id: 3, type: 'order_create', description: '新订单 #1235 已创建', createdAt: new Date(Date.now() - 600000) },
    { id: 4, type: 'driver_assign', description: '订单 #1233 已指派给王师傅', createdAt: new Date(Date.now() - 900000) }
  ]
}

// Event Handlers
const handleTimeRangeChange = () => {
  loadAllStats()
}

const handleExport = () => {
  ElMessage.success('报表导出功能开发中')
}

// Helpers
const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

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

const getWasteTypeName = (type) => {
  const map = {
    'HOUSEHOLD': '生活垃圾',
    'CONSTRUCTION': '建筑垃圾',
    'INDUSTRIAL': '工业垃圾',
    'MEDICAL': '医疗垃圾',
    'ELECTRONIC': '电子垃圾',
    'OTHER': '其他垃圾'
  }
  return map[type] || type
}

const getProgressColor = (percentage) => {
  if (percentage >= 40) return '#67c23a'
  if (percentage >= 20) return '#e6a23c'
  return '#909399'
}

const getActivityIcon = (type) => {
  const map = {
    'order_complete': Check,
    'driver_grab': Document,
    'order_create': Plus,
    'driver_assign': Van
  }
  return map[type] || Clock
}
</script>

<style scoped>
.fleet-stats-container {
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

.time-selector {
  padding: 16px;
  background: #fff;
}

.stats-section {
  background: #fff;
  margin: 12px 16px;
  padding: 16px;
  border-radius: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.overview-section {
  padding: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.green {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-icon.purple {
  background: linear-gradient(135deg, #a855f7 0%, #7c3aed 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.stat-trend {
  font-size: 12px;
  margin-top: 4px;
}

.stat-trend.up {
  color: #67c23a;
}

.stat-trend.down {
  color: #f56c6c;
}

.stats-detail {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.detail-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.detail-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.detail-value.primary {
  color: #409eff;
}

.detail-value.success {
  color: #67c23a;
}

.detail-value.warning {
  color: #e6a23c;
}

.detail-value.info {
  color: #909399;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  background: #909399;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffa500 100%);
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #a0a0a0 100%);
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #b87333 100%);
}

.driver-info {
  flex: 1;
}

.driver-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.driver-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.driver-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #f59e0b;
}

.distribution-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.distribution-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.distribution-label {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.type-name {
  color: #333;
}

.type-count {
  color: #999;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #909399;
}

.activity-icon.order_complete {
  background: #67c23a;
}

.activity-icon.driver_grab {
  background: #409eff;
}

.activity-icon.order_create {
  background: #e6a23c;
}

.activity-icon.driver_assign {
  background: #a855f7;
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #999;
}

@media (min-width: 768px) {
  .fleet-stats-container {
    max-width: 480px;
    margin: 0 auto;
  }
}
</style>
