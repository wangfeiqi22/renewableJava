<template>
  <div class="order-detail-container">
    <!-- Header -->
    <header class="eco-header">
      <div class="header-left" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">订单详情</div>
      <div class="header-right"></div>
    </header>

    <!-- Main Content -->
    <div class="detail-content" v-if="loading">
      <el-skeleton animated :rows="10" />
    </div>
    
    <div class="detail-content" v-else-if="error">
      <el-empty description="订单获取失败或不存在">
        <el-button type="primary" @click="fetchOrderDetail">重试</el-button>
      </el-empty>
    </div>

    <div class="detail-content" v-else-if="order">
      <!-- Status Card -->
      <div class="card status-card">
        <div class="status-header">
          <span class="status-text" :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</span>
          <div class="order-info">
            <span class="order-no">订单号: {{ order.orderNo || order.id }}</span>
          </div>
        </div>
      </div>

      <!-- Shipper Card (Passenger/Contact) -->
      <div class="card info-card">
        <div class="info-row">
          <div class="icon-wrap red-bg">
            <el-icon><Location /></el-icon>
          </div>
          <div class="info-main">
            <div class="info-title-wrap">
              <span class="info-title">{{ order.pickupAddress ? order.pickupAddress.split('市')[0] + '项目' : '上车点/起点' }}</span>
              <el-tag size="small" type="info" effect="plain" class="role-tag">起点</el-tag>
            </div>
            <div class="info-sub">{{ order.startAddress || order.pickupAddress || '暂无详细地址' }}</div>
          </div>
        </div>
        <div class="contact-row">
          <div class="contact-item">
            <el-icon><User /></el-icon> {{ order.contactName || '默认联系人' }}
          </div>
          <div class="contact-item phone-link" @click="handleCall(order.contactPhone)">
            <el-icon><Phone /></el-icon> {{ order.contactPhone || '暂无号码' }}
          </div>
        </div>
        <div class="tags-row">
          <el-tag type="info" class="waste-tag">{{ order.wasteType || '常规清运' }}</el-tag>
          <span class="volume-text" v-if="order.weight">重量 {{ order.weight }}吨</span>
        </div>
      </div>

      <!-- Station Card (Destination) -->
      <div class="card info-card">
        <div class="info-row">
          <div class="icon-wrap orange-bg">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="info-main">
            <div class="info-title-wrap">
              <span class="info-title">{{ order.endAddress || '平台分配站点' }}</span>
              <el-tag size="small" type="info" effect="plain" class="role-tag">终点</el-tag>
            </div>
            <div class="info-sub">{{ order.endAddress || '到达后确认' }}</div>
          </div>
        </div>
      </div>

      <!-- Financial Card -->
      <div class="card info-card">
        <div class="info-row">
          <div class="icon-wrap" style="background: rgba(245, 108, 108, 0.1); color: #f56c6c;">
            <el-icon><Money /></el-icon>
          </div>
          <div class="info-main">
            <div class="info-title-wrap">
              <span class="info-title">订单金额</span>
            </div>
            <div class="info-sub" style="color: #f56c6c; font-size: 18px; font-weight: bold; margin-top: 4px;">
              ¥{{ formatMoney(order.amount || 0) }}
            </div>
          </div>
        </div>
        <div class="contact-row" style="margin-top: 12px; border-top: 1px dashed #eee; padding-top: 12px;">
          <div class="contact-item" style="color: #666; font-size: 13px;">
            完成时间: {{ formatDate(order.updatedAt || order.createdAt) }}
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Actions -->
    <div class="bottom-actions" v-if="!loading && !error && order">
      <el-button class="action-btn" icon="Phone" @click="handleCall(order.contactPhone)">联系乘客</el-button>
      <el-button class="action-btn" type="primary" icon="Position" @click="handleNavigate">导航</el-button>
      
      <el-button v-if="order.status === 20 || order.status === 25" type="warning" class="action-btn" @click="handleUpdateStatus(30)">开始装载</el-button>
      <el-button v-if="order.status === 30" type="primary" class="action-btn" @click="handleUpdateStatus(40)">完成装车(运输中)</el-button>
      <el-button v-if="order.status === 40" type="success" class="action-btn" @click="handleUpdateStatus(50)">到达站点</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Location, OfficeBuilding, User, Phone, Van, Select, Close, ArrowDown, Camera, Box, Money, Position, List } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id

const loading = ref(true)
const error = ref(false)
const order = ref(null)

const handleBack = () => {
  router.back()
}

const fetchOrderDetail = async () => {
  loading.value = true
  error.value = false
  try {
    const res = await api.get(`/orders/${orderId}`)
    order.value = res.data
  } catch (err) {
    console.error('Failed to fetch order detail:', err)
    error.value = true
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

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

const formatMoney = (val) => {
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const handleCall = (phone) => {
  if (!phone) {
    ElMessage.warning('暂无联系人电话')
    return
  }
  window.location.href = `tel:${phone}`
}

const handleNavigate = () => {
  ElMessage.success('正在启动导航...')
}

const handleFeeDetails = () => {
  ElMessage.info('费用明细正在加载中...')
}

const handleUpdateStatus = async (newStatus) => {
  try {
    await api.put(`/orders/${order.value.id}/status?status=${newStatus}`)
    ElMessage.success('状态更新成功')
    fetchOrderDetail()
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail-container {
  min-height: 100vh;
  background-color: #f5f7f9;
  display: flex;
  flex-direction: column;
  padding-bottom: 80px; /* Space for bottom actions */
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

.detail-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
}

.status-card {
  padding: 20px 16px;
}
.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.status-text {
  font-size: 20px;
  font-weight: bold;
}
.status-primary { color: #1a73e8; }
.status-success { color: #0dc8a3; }
.status-danger { color: #f56c6c; }
.status-info { color: #909399; }

.order-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}
.order-no {
  font-size: 13px;
  color: #999;
  font-family: monospace;
}

/* Info Cards */
.info-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #fff;
}
.red-bg { background-color: #ff6b6b; }
.blue-bg { background-color: #409eff; }
.orange-bg { background-color: #e6a23c; }

.info-main {
  flex: 1;
}
.info-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.info-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.role-tag {
  border-radius: 12px;
}
.info-sub {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.contact-row {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}
.contact-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #333;
}
.phone-link {
  color: #1a73e8;
  cursor: pointer;
}

.tags-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}
.waste-tag {
  background: #f4f4f5;
  border-color: #e9e9eb;
  color: #909399;
  border-radius: 12px;
}
.volume-text {
  font-size: 13px;
  color: #666;
}

/* Bottom Actions */
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  display: flex;
  gap: 12px;
  z-index: 100;
}
.bottom-actions .action-btn {
  flex: 1;
  height: 44px;
  border-radius: 22px;
  font-size: 15px;
  font-weight: 500;
}

@media (min-width: 768px) {
  .order-detail-container {
    max-width: 480px;
    margin: 0 auto;
    border-left: 1px solid #ebeef5;
    border-right: 1px solid #ebeef5;
    box-shadow: 0 0 20px rgba(0,0,0,0.05);
  }
  .bottom-actions {
    max-width: 480px;
    margin: 0 auto;
    border-left: 1px solid #ebeef5;
    border-right: 1px solid #ebeef5;
  }
}
</style>