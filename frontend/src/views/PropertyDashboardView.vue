<template>
  <div class="property-container">
    <el-header class="header">
      <div class="header-left">
        <div class="logo">
          <el-icon><OfficeBuilding /></el-icon>
          <span class="text">物业管理端</span>
        </div>
      </div>
      <div class="header-right">
        <span class="username" v-if="user">{{ user.username }}</span>
        <el-button type="danger" link @click="handleLogout">退出</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <!-- Stats Cards -->
      <div class="stats-grid">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>总清运量</span>
              <el-tag type="success">本月</el-tag>
            </div>
          </template>
          <div class="stat-value">{{ stats.totalWeight }} kg</div>
        </el-card>
        
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>预估费用</span>
              <el-tag type="warning">待结算</el-tag>
            </div>
          </template>
          <div class="stat-value">¥ {{ stats.totalCost }}</div>
        </el-card>

        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>进行中任务</span>
              <el-tag>实时</el-tag>
            </div>
          </template>
          <div class="stat-value">{{ stats.pendingOrders }}</div>
        </el-card>
      </div>

      <!-- Actions -->
      <div class="action-bar">
        <el-button type="primary" size="large" icon="Plus" @click="router.push('/order/create')">发起清运申请</el-button>
        <el-button type="success" size="large" icon="Calendar">预约定期清运 (订阅)</el-button>
        <el-button type="info" size="large" icon="Document">下载月度报表</el-button>
      </div>

      <!-- Recent Orders Table -->
      <el-card class="table-card">
        <template #header>
          <div class="clearfix">
            <span>最近记录</span>
          </div>
        </template>
        <el-table :data="orders" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="wasteType" label="类型" width="120" />
          <el-table-column prop="estWeight" label="重量(kg)" width="120" />
          <el-table-column prop="pickupAddress" label="地址" />
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="时间" width="180" />
        </el-table>
      </el-card>
    </el-main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { OfficeBuilding, Plus, Calendar, Document } from '@element-plus/icons-vue'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const stats = reactive({
  totalWeight: 0,
  totalCost: 0,
  pendingOrders: 0,
  totalOrders: 0
})
const orders = ref([])

const fetchStats = async () => {
  try {
    const res = await api.get(`/property/dashboard/stats?userId=${user.id}`)
    Object.assign(stats, res.data)
  } catch (error) {
    console.error(error)
  }
}

const fetchOrders = async () => {
  try {
    const res = await api.get(`/orders/user/${user.id}`)
    orders.value = res.data.slice(0, 5) // Last 5
  } catch (error) {
    console.error(error)
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const getStatusText = (status) => {
  const map = { 0: '待接单', 10: '已接单', 20: '已出发', 30: '装车中', 40: '运输中', 50: '已到站', 60: '已完成' }
  return map[status] || status
}

const getStatusType = (status) => {
  if (status === 60) return 'success'
  if (status === 0) return 'info'
  return 'primary'
}

onMounted(() => {
  if (user.role !== 'property') {
    // Optional: redirect if not property? 
    // For demo, we allow access or user might have navigated manually
  }
  fetchStats()
  fetchOrders()
})
</script>

<style scoped>
.property-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
  color: #00C853;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-top: 10px;
}

.action-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.table-card {
  background: white;
}
</style>