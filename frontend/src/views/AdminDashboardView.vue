<template>
  <div class="admin-container">
    <!-- Top Navbar -->
    <header class="admin-header">
      <div class="header-left">
        <div class="logo">
          <span class="icon">📊</span>
          <span class="text">监管驾驶舱</span>
        </div>
        <nav class="nav-links">
          <router-link to="/admin/dashboard" class="nav-item active">概览</router-link>
          <router-link to="/admin/sf" class="nav-item">站点运力</router-link>
          <router-link to="/admin/kb" class="nav-item">知识库</router-link>
        </nav>
      </div>
      <div class="header-right">
        <div class="action-btn">🔔</div>
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-profile">
            <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <span class="name">管理员</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="user">返回用户端</el-dropdown-item>
              <el-dropdown-item divided command="logout" style="color: var(--color-danger)">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="admin-main container">
      <!-- KPI Cards -->
      <div class="kpi-grid">
        <div class="kpi-card total">
          <div class="kpi-icon">📦</div>
          <div class="kpi-content">
            <div class="label">总订单量</div>
            <div class="value">{{ stats.totalOrders }}</div>
            <div class="trend up">↑ 12.5%</div>
          </div>
        </div>
        <div class="kpi-card pending">
          <div class="kpi-icon">⏳</div>
          <div class="kpi-content">
            <div class="label">待处理</div>
            <div class="value">{{ stats.pendingOrders }}</div>
            <div class="trend down">↓ 5.2%</div>
          </div>
        </div>
        <div class="kpi-card success">
          <div class="kpi-icon">✅</div>
          <div class="kpi-content">
            <div class="label">已完成</div>
            <div class="value">{{ stats.completedOrders }}</div>
            <div class="trend up">↑ 8.1%</div>
          </div>
        </div>
        <div class="kpi-card rate">
          <div class="kpi-icon">📈</div>
          <div class="kpi-content">
            <div class="label">完单率</div>
            <div class="value">{{ stats.completionRate ? stats.completionRate.toFixed(1) : 0 }}%</div>
            <div class="trend steady">- 0.0%</div>
          </div>
        </div>
      </div>

      <!-- Charts Section -->
      <div class="charts-grid">
        <div class="card chart-card">
          <div class="card-header">
            <h3>清运趋势分析</h3>
            <div class="actions">
              <el-radio-group v-model="trendPeriod" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div ref="trendChartRef" class="chart-body"></div>
        </div>

        <div class="card chart-card">
          <div class="card-header">
            <h3>垃圾类型占比</h3>
          </div>
          <div ref="pieChartRef" class="chart-body"></div>
        </div>
      </div>

      <!-- Live Map (Placeholder) -->
      <div class="card map-card">
        <div class="card-header">
          <h3>实时监控地图</h3>
          <el-tag type="success" size="small" effect="dark">Live</el-tag>
        </div>
        <div class="map-placeholder">
          <div class="pulse-ring"></div>
          <el-icon :size="48"><MapLocation /></el-icon>
          <p>地图服务连接中...</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref, nextTick } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { MapLocation } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()
const stats = reactive({
  totalOrders: 0,
  pendingOrders: 0,
  completedOrders: 0,
  completionRate: 0
})
const trendChartRef = ref(null)
const pieChartRef = ref(null)
const trendPeriod = ref('week')

const fetchStats = async () => {
  try {
    const res = await api.get('/admin/dashboard/stats')
    Object.assign(stats, res.data)
    initCharts()
  } catch (error) {
    console.error('Failed to fetch dashboard stats')
  }
}

const initCharts = () => {
  if (trendChartRef.value) {
    const trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
        axisLine: { show: false },
        axisTick: { show: false }
      },
      yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
      series: [{
        name: '清运量',
        type: 'line',
        smooth: true,
        data: [120, 200, 150, 80, 70, 110, 130],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 200, 83, 0.5)' },
            { offset: 1, color: 'rgba(0, 200, 83, 0.01)' }
          ])
        },
        itemStyle: { color: '#00C853' }
      }]
    })
  }

  if (pieChartRef.value) {
    const pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '5%', left: 'center' },
      series: [{
        name: '垃圾类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 20, fontWeight: 'bold' }
        },
        data: [
          { value: 1048, name: '生活垃圾' },
          { value: 735, name: '建筑垃圾' },
          { value: 580, name: '厨余垃圾' },
          { value: 484, name: '大件垃圾' }
        ]
      }]
    })
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  } else if (command === 'user') {
    router.push('/order/create')
  }
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', () => {
    echarts.getInstanceByDom(trendChartRef.value)?.resize()
    echarts.getInstanceByDom(pieChartRef.value)?.resize()
  })
})
</script>

<style scoped>
.admin-container {
  background-color: #f5f7fa;
  min-height: 100vh;
}

.admin-header {
  background: white;
  height: 64px;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-item {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  padding: 8px 0;
  position: relative;
  transition: color 0.3s;
}

.nav-item:hover, .nav-item.active {
  color: var(--color-primary);
  font-weight: 500;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--color-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-btn {
  font-size: 20px;
  cursor: pointer;
  color: #909399;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.admin-main {
  padding: 24px;
}

/* KPI Grid */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.kpi-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: transform 0.3s;
}

.kpi-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.kpi-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  background-color: #f0f9eb;
}

.kpi-card.pending .kpi-icon { background-color: #fff7e6; }
.kpi-card.success .kpi-icon { background-color: #f0f9eb; }
.kpi-card.rate .kpi-icon { background-color: #ecf5ff; }

.kpi-content {
  flex: 1;
}

.kpi-content .label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.kpi-content .value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.trend {
  font-size: 12px;
  margin-top: 4px;
}

.trend.up { color: #67c23a; }
.trend.down { color: #f56c6c; }
.trend.steady { color: #909399; }

/* Charts */
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.chart-body {
  height: 300px;
  width: 100%;
}

/* Map */
.map-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.map-placeholder {
  height: 400px;
  background: #f4f4f5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
  position: relative;
}

.pulse-ring {
  position: absolute;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 4px solid var(--color-primary);
  opacity: 0;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.5); opacity: 0; }
  50% { opacity: 0.5; }
  100% { transform: scale(1.5); opacity: 0; }
}
</style>
