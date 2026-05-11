<template>
  <div class="order-list-container">
    <!-- Header -->
    <header class="eco-header">
      <div class="header-left" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">我的全部订单</div>
      <div class="header-right"></div>
    </header>

    <!-- Filter Tabs -->
    <div class="filter-section">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange" class="custom-tabs">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="进行中" name="ongoing"></el-tab-pane>
        <el-tab-pane label="已完成" name="completed"></el-tab-pane>
        <el-tab-pane label="已取消" name="cancelled"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- Order List -->
    <div class="list-content" v-loading="loading">
      <div v-if="loading" class="skeleton-wrapper">
        <el-skeleton :rows="4" animated v-for="i in 3" :key="i" class="mb-4" />
      </div>
      
      <el-empty 
        v-else-if="filteredOrders.length === 0" 
        description="暂无相关清运订单" 
        :image-size="120"
      >
        <template #image>
          <div class="empty-icon-placeholder">
            <el-icon><DocumentDelete /></el-icon>
          </div>
        </template>
      </el-empty>

      <div v-else class="orders-wrapper">
        <div 
          v-for="order in paginatedOrders" 
          :key="order.id" 
          class="order-card"
          @click="goToDetail(order.orderNo || order.id)"
        >
          <div class="card-header">
            <div class="order-no">
              <span class="label">订单号</span>
              <span class="value">{{ order.orderNo || order.id }}</span>
            </div>
            <div class="order-status" :class="getStatusClass(order.status)">
              {{ getStatusText(order.status) }}
            </div>
          </div>
          
          <div class="card-body">
            <div class="info-row">
              <div class="info-item full-width">
                <el-icon class="icon location-icon"><Location /></el-icon>
                <div class="location-text">
                  <div class="start-addr">{{ order.startAddress || '客户上车点' }}</div>
                  <div class="end-addr">{{ order.endAddress || '清运处置站' }}</div>
                </div>
              </div>
            </div>
            <div class="info-row split">
              <div class="info-item">
                <el-icon class="icon"><User /></el-icon>
                <span>乘客/联系人：{{ order.contactName || '默认客户' }}</span>
              </div>
              <div class="info-item">
                <el-icon class="icon"><Van /></el-icon>
                <span>类型：{{ getWasteTypeName(order.wasteType) }}</span>
              </div>
            </div>
            <div class="info-row split">
              <div class="info-item">
                <el-icon class="icon"><Money /></el-icon>
                <span class="amount">实收：¥{{ formatMoney(order.amount || 0) }}</span>
              </div>
              <div class="info-item">
                <el-icon class="icon"><Calendar /></el-icon>
                <span>时间：{{ formatDate(order.createdAt) }}</span>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <div class="footer-left">
              <span class="weight-badge" v-if="order.weight">
                {{ order.weight }} 吨
              </span>
            </div>
            <div class="footer-actions">
              <el-button size="small" round @click.stop="goToDetail(order.orderNo || order.id)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="pagination-wrapper" v-if="filteredOrders.length > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="filteredOrders.length"
            layout="prev, pager, next"
            background
            hide-on-single-page
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Location, Calendar, DocumentDelete, User, Van, Money } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const route = useRoute()
const user = JSON.parse(localStorage.getItem('user') || '{}')

const activeTab = ref(route.query.tab || 'all')
const loading = ref(false)
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

const handleBack = () => {
  router.push('/profile/driver')
}

const handleTabChange = () => {
  currentPage.value = 1
  router.replace({ query: { ...route.query, tab: activeTab.value } })
}

const fetchOrders = async () => {
  if (!user || !user.id) {
    ElMessage.error('用户未登录')
    router.replace('/login')
    return
  }
  
  loading.value = true
  try {
    // Requirements stated: 列表接口使用/driver/orders分页查询，需携带司机ID及token
    // But since backend doesn't have /driver/orders, we use /orders/user/${user.id} to fetch all,
    // which is the standard pattern in this app for driver orders.
    const res = await api.get(`/orders/user/${user.id}`)
    orders.value = res.data || []
    orders.value.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  } catch (error) {
    console.error('Fetch driver orders failed:', error)
    ElMessage.error('获取订单列表失败，请重试')
  } finally {
    loading.value = false
  }
}

// 状态枚举映射为司机端语义：已接单、已到达上车点、乘客已上车、已完成、已取消
const getStatusText = (status) => {
  const map = {
    10: '待接单',
    20: '已接单',
    25: '已接单',
    30: '已到达上车点',
    40: '乘客已上车',
    45: '已到达站点',
    50: '待结算',
    60: '已完成',
    90: '已取消'
  }
  return map[status] || '未知状态'
}

const getStatusClass = (status) => {
  if (status === 60) return 'status-success'
  if (status === 90) return 'status-danger'
  if (status >= 20 && status < 60) return 'status-primary'
  return 'status-info'
}

// 格式化金额
const formatMoney = (val) => {
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取垃圾类型名称
const getWasteTypeName = (type) => {
  const map = {
    'domestic': '生活垃圾',
    'construction': '建筑垃圾',
    'industrial': '工业废弃物',
    'hazardous': '有害垃圾'
  }
  return map[type] || type || '常规清运'
}

const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    if (activeTab.value === 'all') return true
    if (activeTab.value === 'ongoing') return order.status >= 20 && order.status < 60
    if (activeTab.value === 'completed') return order.status === 60
    if (activeTab.value === 'cancelled') return order.status === 90
    return true
  })
})

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

const goToDetail = (orderNo) => {
  router.push(`/driver/order/${orderNo}`)
}

onMounted(() => {
  fetchOrders()
})
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
  height: 56px;
  padding: 0 16px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-left, .header-right {
  width: 40px;
  display: flex;
  align-items: center;
}
.header-left {
  font-size: 20px;
  color: #333;
  cursor: pointer;
}
.header-title {
  flex: 1;
  text-align: center;
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.filter-section {
  background: #fff;
  padding: 0 16px;
  position: sticky;
  top: 56px;
  z-index: 99;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #f0f0f0;
}
.custom-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  color: #666;
}
.custom-tabs :deep(.el-tabs__item.is-active) {
  color: #0dc8a3;
  font-weight: 600;
}
.custom-tabs :deep(.el-tabs__active-bar) {
  background-color: #0dc8a3;
  height: 3px;
  border-radius: 3px;
}

.list-content {
  flex: 1;
  padding: 16px;
}

.skeleton-wrapper {
  background: #fff;
  padding: 16px;
  border-radius: 12px;
}

.empty-icon-placeholder {
  font-size: 64px;
  color: #dcdfe6;
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.order-card:active {
  transform: scale(0.98);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}
.order-no {
  display: flex;
  align-items: center;
  gap: 6px;
}
.order-no .label {
  font-size: 12px;
  color: #999;
  background: #f5f7f9;
  padding: 2px 6px;
  border-radius: 4px;
}
.order-no .value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.order-status {
  font-size: 13px;
  font-weight: 500;
}
.status-primary { color: #1a73e8; }
.status-success { color: #0dc8a3; }
.status-danger { color: #f56c6c; }
.status-info { color: #909399; }

.card-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.info-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.info-row.split {
  justify-content: space-between;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}
.info-item.full-width {
  flex: 1;
}
.icon {
  font-size: 15px;
  color: #999;
}
.location-icon {
  color: #0dc8a3;
  margin-top: 2px;
}
.location-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}
.start-addr, .end-addr {
  color: #333;
  font-weight: 500;
  line-height: 1.4;
}
.end-addr {
  color: #666;
  font-weight: normal;
  position: relative;
}
.end-addr::before {
  content: '';
  position: absolute;
  left: -13px;
  top: -8px;
  height: 8px;
  width: 1px;
  border-left: 1px dashed #dcdfe6;
}
.amount {
  color: #f56c6c;
  font-weight: 600;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #f0f0f0;
}
.weight-badge {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(13, 200, 163, 0.1);
  color: #0dc8a3;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  margin-bottom: 24px;
}

@media (min-width: 768px) {
  .order-list-container {
    max-width: 480px;
    margin: 0 auto;
    border-left: 1px solid #ebeef5;
    border-right: 1px solid #ebeef5;
    box-shadow: 0 0 20px rgba(0,0,0,0.05);
  }
}
</style>
