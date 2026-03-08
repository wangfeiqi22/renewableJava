<template>
  <div class="dashboard-container">
    <el-header class="header">
      <div class="title">监管驾驶舱 - 智慧垃圾清运系统</div>
      <div>
        <el-button type="primary" size="small" @click="router.push('/admin/kb')">知识库管理</el-button>
        <el-button type="primary" size="small" @click="router.push('/admin/sf')">站点运力</el-button>
        <el-button type="info" size="small" @click="router.push('/order/create')">返回用户端</el-button>
        <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
      </div>
    </el-header>

    <el-main>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <template #header>总订单量</template>
            <div class="stat-value">{{ stats.totalOrders }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <template #header>待处理</template>
            <div class="stat-value warning">{{ stats.pendingOrders }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <template #header>已完成</template>
            <div class="stat-value success">{{ stats.completedOrders }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <template #header>完单率</template>
            <div class="stat-value primary">{{ stats.completionRate ? stats.completionRate.toFixed(1) : 0 }}%</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>实时监控地图</template>
            <div class="map-placeholder">
              <el-icon :size="50"><MapLocation /></el-icon>
              <p>地图服务加载中...</p>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>清运趋势分析</template>
            <div ref="chartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref, nextTick } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { MapLocation, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()
const stats = reactive({
  totalOrders: 0,
  pendingOrders: 0,
  completedOrders: 0,
  completionRate: 0
})
const chartRef = ref(null)

const fetchStats = async () => {
  try {
    const res = await api.get('/admin/dashboard/stats')
    Object.assign(stats, res.data)
    initChart()
  } catch (error) {
    console.error('Failed to fetch dashboard stats')
  }
}

const initChart = () => {
  if (!chartRef.value) return
  const myChart = echarts.init(chartRef.value)
  const option = {
    title: { text: '近7天清运量趋势' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    },
    yAxis: { type: 'value' },
    series: [
      {
        data: [120, 200, 150, 80, 70, 110, 130],
        type: 'bar',
        itemStyle: { color: '#00C851' }
      }
    ]
  }
  myChart.setOption(option)
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
  background-color: #f0f2f5;
}
.header {
  background-color: #2c3e50;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
}
.warning { color: #e6a23c; }
.success { color: #67c23a; }
.primary { color: #409eff; }

.map-placeholder, .chart-container {
  height: 300px;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
  border-radius: 4px;
}
</style>
