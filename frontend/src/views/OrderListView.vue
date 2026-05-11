<template>
  <div class="order-list-container">
    <!-- Header -->
    <header class="eco-header">
      <div class="header-left" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">我的订单</div>
      <div class="header-right">
        <el-button link type="primary" @click="exportToExcel">导出</el-button>
      </div>
    </header>

    <!-- Filters & Search -->
    <div class="filter-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索订单号/地址"
        prefix-icon="Search"
        clearable
        class="search-input"
        @input="handleSearch"
      />
      <div class="tabs-wrapper">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="eco-tabs">
          <el-tab-pane label="全部" name="all"></el-tab-pane>
          <el-tab-pane label="待处理" name="pending"></el-tab-pane>
          <el-tab-pane label="进行中" name="processing"></el-tab-pane>
          <el-tab-pane label="本月完成" name="thisMonth"></el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- Main Content Area -->
    <div class="list-content">
      <!-- Loading Skeleton -->
      <div v-if="loading" class="skeleton-wrapper">
        <el-skeleton animated :rows="5" v-for="i in 3" :key="i" class="skeleton-card" />
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredOrders.length === 0" class="empty-wrapper">
        <el-empty description="暂无相关清运订单" />
      </div>

      <!-- Order List -->
      <div v-else class="order-cards">
        <div v-for="order in paginatedOrders" :key="order.id" class="order-card" @click="goToDetail(order.id)">
          <!-- Card Header -->
          <div class="card-header">
            <el-tag :type="getStatusType(order.status)" effect="light" class="status-tag" round>
              {{ getStatusText(order.status) }}
            </el-tag>
            <div class="price" v-if="order.amount">¥{{ order.amount }}</div>
            <div class="price" v-else>待结算</div>
          </div>
          
          <div class="order-no">{{ order.orderNo }}</div>

          <!-- Progress Bar (Visual Mock based on reference) -->
          <div class="progress-section" v-if="order.status >= 30 && order.status < 60">
            <div class="progress-header">
              <span class="progress-title">清运进度</span>
              <span class="progress-value">{{ order.estVolume ? (order.estVolume / 2).toFixed(1) : 0 }}m³ / {{ order.estVolume || 0 }}m³</span>
            </div>
            <div class="progress-bar-bg">
              <div class="progress-bar-fill" :style="{ width: '50%' }"></div>
            </div>
            <div class="progress-footer">
              <span class="progress-detail">已清运 1 车</span>
              <span class="progress-detail">剩余 {{ order.estVolume ? (order.estVolume / 2).toFixed(1) : 0 }}m³</span>
            </div>
          </div>

          <!-- Order Info -->
          <div class="info-list">
            <div class="info-item">
              <el-icon class="info-icon orange"><OfficeBuilding /></el-icon>
              <div class="info-content">
                <div class="info-label">排放单位</div>
                <div class="info-value">{{ order.pickupAddress ? order.pickupAddress.split('市')[0] + '项目' : '环保清运项目' }}</div>
              </div>
            </div>
            
            <div class="info-item">
              <el-icon class="info-icon red"><Location /></el-icon>
              <div class="info-content">
                <div class="info-value">{{ order.pickupAddress }}</div>
              </div>
            </div>

            <div class="info-item" v-if="order.stationId">
              <el-icon class="info-icon blue"><Van /></el-icon>
              <div class="info-content">
                <div class="info-label">处置单位</div>
                <div class="info-value">浦东第一再生资源场</div>
              </div>
            </div>

            <div class="info-item">
              <el-icon class="info-icon gray"><Box /></el-icon>
              <div class="info-content">
                <div class="info-value">{{ getWasteTypeName(order.wasteType) }} · {{ order.estWeight }} kg</div>
              </div>
            </div>

            <div class="info-item">
              <el-icon class="info-icon gray"><Clock /></el-icon>
              <div class="info-content">
                <div class="info-value">{{ formatDate(order.createdAt) }}</div>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="card-actions">
            <el-button link type="info" class="detail-btn">点击查看详情 <el-icon><ArrowRight /></el-icon></el-button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="filteredOrders.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="filteredOrders.length"
          layout="prev, pager, next"
          background
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Search, OfficeBuilding, Location, Van, Box, Clock, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

const allOrders = ref([])
const loading = ref(true)
const searchQuery = ref('')
const activeTab = ref('all')

const currentPage = ref(1)
const pageSize = ref(20)

onMounted(async () => {
  if (route.query.tab) {
    activeTab.value = route.query.tab
  }
  await fetchOrders()
})

const fetchOrders = async () => {
  if (!user.value.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loading.value = true
  try {
    const res = await api.get(`/orders/user/${user.value.id}`)
    // 默认按创建时间倒序
    allOrders.value = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  } catch (error) {
    console.error('Failed to fetch orders:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 过滤逻辑
const filteredOrders = computed(() => {
  let list = allOrders.value

  // 状态筛选
  if (activeTab.value === 'pending') {
    list = list.filter(o => o.status < 60 && o.status !== 90)
  } else if (activeTab.value === 'processing') {
    list = list.filter(o => o.status >= 20 && o.status < 60)
  } else if (activeTab.value === 'thisMonth') {
    const startOfMonth = new Date(new Date().getFullYear(), new Date().getMonth(), 1).getTime()
    list = list.filter(o => o.status === 60 && new Date(o.createdAt).getTime() >= startOfMonth)
  }

  // 搜索关键字
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(o => 
      (o.orderNo && o.orderNo.toLowerCase().includes(q)) || 
      (o.pickupAddress && o.pickupAddress.toLowerCase().includes(q))
    )
  }

  return list
})

// 分页逻辑
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

const handleSearch = () => {
  currentPage.value = 1
}

const handleTabChange = () => {
  currentPage.value = 1
}

const handlePageChange = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const exportToExcel = () => {
  // Mock Excel Export
  ElMessage.success('订单报表已开始导出')
  setTimeout(() => {
    ElMessage.success('导出完成')
  }, 1000)
}

const goToDetail = (id) => {
  router.push(`/order/detail/${id}`)
}

// Utils
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const getWasteTypeName = (type) => {
  const map = {
    'household': '生活垃圾',
    'construction': '建筑垃圾',
    'bulky': '大件垃圾',
    'kitchen': '厨余垃圾',
    'recyclable': '可回收物',
    'hazardous': '有害垃圾',
    'wet': '湿垃圾',
    'dry': '干垃圾'
  }
  return map[type] || '其他垃圾'
}

const getStatusType = (status) => {
  if (status === 10) return 'warning'
  if (status === 60) return 'success'
  if (status === 90) return 'info'
  return 'primary'
}

const getStatusText = (status) => {
  const map = {
    10: '待平台确认',
    20: '已派车',
    25: '司机接单',
    30: '装车中',
    40: '运输中',
    50: '到达站点',
    60: '已完成',
    90: '已取消'
  }
  return map[status] || '未知状态'
}
</script>

<style scoped>
.order-list-container {
  min-height: 100vh;
  background-color: #f5f7f9;
  display: flex;
  flex-direction: column;
}

.eco-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 56px;
  background-color: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  font-size: 20px;
  color: #333;
  cursor: pointer;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.header-right {
  width: 40px;
  display: flex;
  justify-content: flex-end;
}

.filter-section {
  background: #fff;
  padding: 12px 16px 0;
  position: sticky;
  top: 56px;
  z-index: 90;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.search-input {
  margin-bottom: 12px;
}

/* Custom Tabs to match mobile UI */
.tabs-wrapper {
  margin: 0 -16px;
  padding: 0 16px;
}
:deep(.eco-tabs .el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #f0f0f0;
}
:deep(.eco-tabs .el-tabs__active-bar) {
  background-color: #0dc8a3;
}
:deep(.eco-tabs .el-tabs__item.is-active) {
  color: #0dc8a3;
  font-weight: 600;
}
:deep(.eco-tabs .el-tabs__item) {
  color: #666;
}

.list-content {
  padding: 16px;
  flex: 1;
}

.skeleton-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-card {
  background: #fff;
  padding: 16px;
  border-radius: 12px;
}

.empty-wrapper {
  padding: 60px 0;
  display: flex;
  justify-content: center;
}

.order-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Order Card Styling */
.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.status-tag {
  border-radius: 12px;
  padding: 2px 10px;
  font-weight: 500;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #0dc8a3;
}

.order-no {
  font-size: 12px;
  color: #999;
  margin-bottom: 16px;
  font-family: monospace;
}

/* Progress Section */
.progress-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #333;
  margin-bottom: 8px;
}

.progress-bar-bg {
  height: 6px;
  background: #eef0f2;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-bar-fill {
  height: 100%;
  background: #0dc8a3;
  border-radius: 3px;
}

.progress-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

/* Info List */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.info-icon {
  margin-top: 2px;
  font-size: 14px;
}

.info-icon.orange { color: #ff9800; }
.info-icon.red { color: #f44336; }
.info-icon.blue { color: #2196f3; }
.info-icon.gray { color: #999; }

.info-content {
  flex: 1;
}

.info-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.info-value {
  font-size: 13px;
  color: #333;
  line-height: 1.4;
}

/* Actions */
.card-actions {
  border-top: 1px dashed #f0f0f0;
  padding-top: 12px;
  display: flex;
  justify-content: center;
}

.detail-btn {
  font-size: 13px;
  color: #666;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  padding-bottom: 20px;
}
</style>
