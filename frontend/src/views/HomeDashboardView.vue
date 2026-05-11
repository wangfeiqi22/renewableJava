<template>
  <div class="mobile-dashboard">
    <!-- Top Navigation (Sticky) -->
    <AppHeader v-bind="headerProps" />

    <!-- Main Content Area -->
    <div class="mobile-content">
      
      <!-- Top Banner Area (Green) -->
      <div class="top-banner">
        <div class="banner-title">
          <div class="banner-icon"><el-icon><DataBoard /></el-icon></div>
          <div class="banner-text">
            <h2>用户工作台</h2>
            <p>一键预约，轻松清运</p>
          </div>
        </div>
        
        <div class="stats-cards">
          <div class="stat-card" @click="router.push('/order/history?tab=thisMonth')" style="cursor: pointer;">
            <div class="stat-num">{{ stats.completedThisMonth }}单</div>
            <div class="stat-label">本月订单 <span class="tag">+1</span></div>
          </div>
          <div class="stat-card" @click="router.push('/order/history?tab=pending')" style="cursor: pointer;">
            <div class="stat-num">{{ stats.pending }}单</div>
            <div class="stat-label">待处理</div>
          </div>
        </div>
      </div>

      <div class="dashboard-body">
        <!-- AI Tip Card -->
        <div class="info-card ai-tip-card">
          <div class="icon-box green">
            <el-icon><Service /></el-icon>
          </div>
          <div class="text-content">
            拍照下单预约清运，智能识别垃圾类型，实时查看订单状态。
          </div>
        </div>

        <!-- Success Card (Conditional) -->
        <div class="info-card success-card" v-if="newOrderNo">
          <div class="success-header">
            <div class="icon-box orange">
              <el-icon><Select /></el-icon>
            </div>
            <div class="text-content">
              <div class="success-title">提交成功！</div>
              <div class="success-desc">预约单已提交，等待平台确认后进入公海池</div>
            </div>
          </div>
          <div class="success-details">
            <div class="detail-row">
              <span class="label">预约单号</span>
              <span class="value">{{ newOrderNo }}</span>
            </div>
            <div class="detail-row">
              <span class="label">状态</span>
              <span class="value"><span class="status-tag">待平台确认</span></span>
            </div>
          </div>
          <div class="success-action">
            <el-button plain class="btn-outline-orange" @click="goToNewOrderDetail">查看预约单详情</el-button>
          </div>
        </div>

        <!-- Quick Tools -->
        <div class="quick-tools">
          <div class="section-title">快捷工具</div>
          
          <div class="tool-card blue-gradient" @click="router.push('/order/create')">
            <div class="tool-icon-box">
              <el-icon><Camera /></el-icon>
            </div>
            <div class="tool-content">
              <div class="tool-title">预约清运</div>
              <div class="tool-desc">拍照智能识别，一键下单</div>
            </div>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>

          <div class="tool-card white-card" @click="router.push('/order/history')">
            <div class="tool-icon-box gray">
              <el-icon><Document /></el-icon>
            </div>
            <div class="tool-content">
              <div class="tool-title">我的订单</div>
              <div class="tool-desc">订单状态·清运记录·电子联单</div>
            </div>
            <el-icon class="arrow-icon gray-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Menu, Camera, Shop, Setting, DataBoard, Service, Select, ArrowRight, Document } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'

import api from '../api'

const router = useRouter()
const route = useRoute()
const user = JSON.parse(localStorage.getItem('user') || '{}')

const headerProps = {
  title: 'EcoClear',
  subtitle: '用户端',
  icon: 'Menu'
}

const newOrderNo = ref('')
const newOrderId = ref('')
const stats = reactive({
  completedThisMonth: 0,
  pending: 0
})

const fetchStats = async () => {
  if (!user || !user.id) return
  try {
    const res = await api.get(`/orders/user/${user.id}`)
    const orders = res.data || []
    
    const now = new Date()
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1).getTime()
    
    stats.completedThisMonth = orders.filter(o => o.status === 60 && new Date(o.createdAt).getTime() >= startOfMonth).length
    stats.pending = orders.filter(o => o.status >= 10 && o.status < 60 && o.status !== 90).length
  } catch (error) {
    console.error('Fetch stats failed:', error)
  }
}

const goToNewOrderDetail = () => {
  if (newOrderId.value) {
    router.push(`/order/detail/${newOrderId.value}`)
  }
}

onMounted(() => {
  if (route.query.newOrderNo) {
    newOrderNo.value = route.query.newOrderNo
  }
  if (route.query.newOrderId) {
    newOrderId.value = route.query.newOrderId
  }
  fetchStats()
})
</script>

<style scoped>
.mobile-dashboard {
  --color-primary: #0dc8a3;
  --color-bg: #f5f7f9;
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  background-color: var(--color-bg);
  overflow-y: auto;
  font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue", Helvetica, Arial, sans-serif;
}

/* Header */
/* Main Content */
.mobile-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* Top Banner */
.top-banner {
  background: linear-gradient(135deg, #0dc8a3 0%, #00a884 100%);
  padding: 24px 20px 40px;
  color: #fff;
}

.banner-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.banner-icon {
  width: 40px;
  height: 40px;
  background: rgba(255,255,255,0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.banner-text h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}
.banner-text p {
  margin: 4px 0 0;
  font-size: 12px;
  opacity: 0.9;
}

.stats-cards {
  display: flex;
  gap: 12px;
}
.stat-card {
  flex: 1;
  background: rgba(255,255,255,0.15);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}
.stat-num {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 4px;
}
.stat-label {
  font-size: 12px;
  opacity: 0.9;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.tag {
  background: rgba(255,255,255,0.3);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
}

/* Dashboard Body */
.dashboard-body {
  padding: 0 16px 24px;
  margin-top: -20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.ai-tip-card {
  background: #f4faf8;
}

.icon-box {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.icon-box.green {
  background: #0dc8a3;
  color: #fff;
}
.icon-box.orange {
  background: #ff9800;
  color: #fff;
  border-radius: 50%;
}

.text-content {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  padding-top: 4px;
}

/* Success Card */
.success-card {
  background: #fffcf5;
  border: 1px solid #ffeedb;
  flex-direction: column;
}
.success-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}
.success-title {
  font-size: 16px;
  font-weight: 600;
  color: #d35400;
  margin-bottom: 4px;
}
.success-desc {
  font-size: 12px;
  color: #e67e22;
}

.success-details {
  width: 100%;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}
.detail-row .label {
  color: #666;
}
.detail-row .value {
  color: #333;
  font-weight: 500;
}
.status-tag {
  background: #fff4e5;
  color: #ff9800;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.success-action {
  width: 100%;
}
.btn-outline-orange {
  width: 100%;
  border-color: #ffb74d;
  color: #e65100;
}

/* Quick Tools */
.quick-tools {
  margin-top: 8px;
}
.section-title {
  font-size: 14px;
  color: #999;
  margin-bottom: 12px;
  padding-left: 4px;
}

.tool-card {
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  cursor: pointer;
}
.blue-gradient {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}
.white-card {
  background: #fff;
}

.tool-icon-box {
  width: 48px;
  height: 48px;
  background: rgba(255,255,255,0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.tool-icon-box.gray {
  background: #f5f5f5;
  color: #999;
}

.tool-content {
  flex: 1;
}
.tool-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}
.white-card .tool-title {
  color: #333;
}
.tool-desc {
  font-size: 12px;
  opacity: 0.9;
}
.white-card .tool-desc {
  color: #999;
}

.arrow-icon {
  font-size: 20px;
  opacity: 0.8;
}
.gray-arrow {
  color: #ccc;
}

@media (min-width: 768px) {
  .mobile-dashboard {
    max-width: 480px;
    margin: 0 auto;
    border-left: 1px solid #eee;
    border-right: 1px solid #eee;
    box-shadow: 0 0 30px rgba(0,0,0,0.05);
  }
}
</style>