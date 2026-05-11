<template>
  <div class="admin-container">
    <!-- Top Navbar -->
    <header class="admin-header">
      <div class="header-left">
        <div class="logo">
          <svg viewBox="0 0 76 76" fill="none" xmlns="http://www.w3.org/2000/svg" style="width: 24px; height: 24px;"><path d="M37.5274 0L75.0548 65H0L37.5274 0Z" fill="var(--ds-text)"/></svg>
          <span class="text">监管驾驶舱</span>
        </div>
        <nav class="nav-links">
          <router-link to="/admin/dashboard" class="nav-item" active-class="active">概览</router-link>
          <router-link to="/admin/sf" class="nav-item" active-class="active">站点运力</router-link>
          <router-link to="/admin/kb" class="nav-item" active-class="active">知识库</router-link>
          <router-link to="/admin/users" class="nav-item" active-class="active">人员审核</router-link>
        </nav>
      </div>
      <div class="header-right">
        <div class="action-btn">
          <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" class="css-i6dzq1"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
        </div>
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-profile">
            <el-avatar :size="30" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" style="background:transparent;" />
          </div>
          <template #dropdown>
            <el-dropdown-menu class="vercel-dropdown">
              <el-dropdown-item command="user">返回用户端</el-dropdown-item>
              <el-dropdown-item divided command="logout" style="color: #e00;">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="admin-main container">
      <!-- KPI Cards -->
      <div class="kpi-grid">
        <div class="kpi-card total">
          <div class="kpi-icon"><el-icon><DataLine /></el-icon></div>
          <div class="kpi-content">
            <div class="label">总订单量</div>
            <div class="value">{{ stats.totalOrders }}</div>
            <div class="trend up">↑ 12.5%</div>
          </div>
        </div>
        <div class="kpi-card pending">
          <div class="kpi-icon"><el-icon><Timer /></el-icon></div>
          <div class="kpi-content">
            <div class="label">待处理</div>
            <div class="value">{{ stats.pendingOrders }}</div>
            <div class="trend down">↓ 5.2%</div>
          </div>
        </div>
        <div class="kpi-card success">
          <div class="kpi-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="kpi-content">
            <div class="label">已完成</div>
            <div class="value">{{ stats.completedOrders }}</div>
            <div class="trend up">↑ 8.1%</div>
          </div>
        </div>
        <div class="kpi-card rate">
          <div class="kpi-icon"><el-icon><TrendCharts /></el-icon></div>
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
              <el-radio-group v-model="trendPeriod" size="small" class="vercel-radio">
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
          <div ref="pieChartRef" class="chart-body" style="height: 400px;"></div>
        </div>
        
        <!-- User Growth Trend -->
        <div class="card chart-card">
          <div class="card-header">
            <h3>运力增长分析</h3>
          </div>
          <div ref="userChartRef" class="chart-body" style="height: 400px;"></div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref, nextTick } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { MapLocation, DataLine, Timer, CircleCheck, TrendCharts } from '@element-plus/icons-vue'
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
const userChartRef = ref(null)
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
  if (userChartRef.value) {
    const userChart = echarts.init(userChartRef.value)
    userChart.setOption({
      tooltip: { 
        trigger: 'axis',
        backgroundColor: '#ffffff',
        borderColor: 'rgba(0,0,0,0.08)',
        textStyle: { color: '#171717', fontFamily: 'inherit' },
        extraCssText: 'box-shadow: 0px 4px 8px rgba(0,0,0,0.04), 0px 0px 0px 1px rgba(0,0,0,0.08); border-radius: 8px;'
      },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '5%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['一月', '二月', '三月', '四月', '五月', '六月'],
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#666666', fontFamily: 'inherit', margin: 16 }
      },
      yAxis: { 
        type: 'value', 
        splitLine: { lineStyle: { type: 'dashed', color: '#ebebeb' } },
        axisLabel: { color: '#666666', fontFamily: 'inherit' }
      },
      series: [{
        name: '新增司机',
        type: 'bar',
        barWidth: '30%',
        itemStyle: {
          color: '#171717',
          borderRadius: [4, 4, 0, 0]
        },
        data: [120, 150, 180, 220, 260, 310]
      }]
    })
  }
  if (trendChartRef.value) {
    const trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { 
        trigger: 'axis',
        backgroundColor: '#ffffff',
        borderColor: 'rgba(0,0,0,0.08)',
        textStyle: { color: '#171717', fontFamily: 'inherit' },
        extraCssText: 'box-shadow: 0px 4px 8px rgba(0,0,0,0.04), 0px 0px 0px 1px rgba(0,0,0,0.08); border-radius: 8px;'
      },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '5%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#666666', fontFamily: 'inherit', margin: 16 }
      },
      yAxis: { 
        type: 'value', 
        splitLine: { lineStyle: { type: 'dashed', color: '#ebebeb' } },
        axisLabel: { color: '#666666', fontFamily: 'inherit' }
      },
      series: [{
        name: '清运量',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: [120, 200, 150, 80, 70, 110, 130],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(23, 23, 23, 0.08)' },
            { offset: 1, color: 'rgba(23, 23, 23, 0.01)' }
          ])
        },
        itemStyle: { color: '#171717' },
        lineStyle: { width: 2, color: '#171717' }
      }]
    })
  }

  if (pieChartRef.value) {
    const pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { 
        trigger: 'item',
        backgroundColor: '#ffffff',
        borderColor: 'rgba(0,0,0,0.08)',
        textStyle: { color: '#171717', fontFamily: 'inherit' },
        extraCssText: 'box-shadow: 0px 4px 8px rgba(0,0,0,0.04), 0px 0px 0px 1px rgba(0,0,0,0.08); border-radius: 8px;'
      },
      legend: { 
        bottom: '0%', 
        left: 'center',
        icon: 'circle',
        itemWidth: 8,
        itemHeight: 8,
        textStyle: { color: '#666666', fontFamily: 'inherit', fontSize: 13 }
      },
      color: ['#171717', '#666666', '#a3a3a3', '#ebebeb'],
      series: [{
        name: '垃圾类型',
        type: 'pie',
        radius: ['50%', '80%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 18, fontWeight: 600, color: '#171717', fontFamily: 'inherit' }
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
    echarts.getInstanceByDom(userChartRef.value)?.resize()
  })
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Geist+Mono:wght@400;500&display=swap');

/* Vercel Design System Variables */
.admin-container {
  --ds-background: #ffffff;
  --ds-text: #171717;
  --ds-text-secondary: #4d4d4d;
  --ds-text-tertiary: #666666;
  --ds-border: rgba(0, 0, 0, 0.08);
  --ds-shadow-border: 0px 0px 0px 1px var(--ds-border);
  --ds-shadow-card: var(--ds-shadow-border), 0px 2px 2px rgba(0, 0, 0, 0.04), 0px 0px 0px 1px #fafafa inset;
  --ds-focus: hsla(212, 100%, 48%, 1);
  --ds-accent-blue: #0a72ef;
  --ds-accent-blue-bg: #ebf5ff;

  background-color: #fafafa; /* Slight contrast to pure white cards */
  min-height: 100vh;
  color: var(--ds-text);
  font-family: 'Geist', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.admin-header {
  background: var(--ds-background);
  height: 64px;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--ds-border);
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
  font-size: 16px;
  font-weight: 600;
  letter-spacing: -0.32px;
  color: var(--ds-text);
  font-family: 'Geist', -apple-system, sans-serif;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-item {
  color: var(--ds-text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 400;
  padding: 8px 0;
  position: relative;
  transition: color 0.2s ease;
}

.nav-item:hover, .nav-item.active {
  color: var(--ds-text);
  font-weight: 500;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: -21px; /* Align with the bottom of header */
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--ds-text);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-btn {
  font-size: 18px;
  cursor: pointer;
  color: var(--ds-text-tertiary);
  transition: color 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
}
.action-btn:hover {
  color: var(--ds-text);
  background: rgba(0, 0, 0, 0.04);
}

.user-profile {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
  border-radius: 50%;
  transition: box-shadow 0.2s;
  border: 1px solid var(--ds-border);
  width: 32px;
  height: 32px;
}
.user-profile:hover {
  box-shadow: var(--ds-shadow-border);
}

.admin-main {
  padding: 64px 24px 80px;
  max-width: 1400px;
  margin: 0 auto;
}

/* KPI Grid */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.kpi-card {
  background: var(--ds-background);
  border-radius: 8px;
  padding: 24px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  box-shadow: var(--ds-shadow-card);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  position: relative;
  overflow: hidden;
}

.kpi-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: var(--ds-border);
  transition: background 0.2s ease;
}
.kpi-card:hover::before {
  background: var(--ds-text);
}
.kpi-card.total::before { background: var(--ds-text); }
.kpi-card.pending::before { background: #f5a623; }
.kpi-card.success::before { background: var(--ds-accent-blue); }
.kpi-card.rate::before { background: #7928ca; }

.kpi-card:hover {
  box-shadow: var(--ds-shadow-border), 0px 4px 8px rgba(0,0,0,0.04), 0px 0px 0px 1px #fafafa inset;
  transform: translateY(-2px);
}

.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  background-color: var(--ds-background);
  border: 1px solid var(--ds-border);
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  color: var(--ds-text-secondary);
}

.kpi-card.pending .kpi-icon { background-color: #fff0f0; border-color: rgba(255, 0, 0, 0.1); color: #e00; }
.kpi-card.success .kpi-icon { background-color: var(--ds-accent-blue-bg); border-color: rgba(0, 112, 243, 0.1); color: var(--ds-accent-blue); }
.kpi-card.rate .kpi-icon { background-color: #f3e8ff; border-color: rgba(121, 40, 202, 0.1); color: #7928ca; }

.kpi-content {
  flex: 1;
}

.kpi-content .label {
  font-size: 14px;
  font-weight: 500;
  color: var(--ds-text-secondary);
  margin-bottom: 8px;
  text-transform: capitalize;
}

.kpi-content .value {
  font-size: 32px;
  font-weight: 600;
  letter-spacing: -1.28px;
  color: var(--ds-text);
  line-height: 1.2;
  font-family: 'Geist Mono', 'Geist', -apple-system, sans-serif;
}

.trend {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
  margin-top: 12px;
  padding: 4px 8px;
  border-radius: 9999px;
  background: var(--ds-background);
  box-shadow: var(--ds-shadow-border);
  font-family: 'Geist Mono', 'Geist', sans-serif;
}

.trend.up { color: var(--ds-accent-blue); background: var(--ds-accent-blue-bg); box-shadow: none; }
.trend.down { color: #e00; background: #fff0f0; box-shadow: none; }
.trend.steady { color: #7928ca; background: #f3e8ff; box-shadow: none; }

/* Charts */
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.chart-card {
  background: var(--ds-background);
  border-radius: 8px;
  padding: 24px;
  box-shadow: var(--ds-shadow-card);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.card-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: -0.8px;
  color: var(--ds-text);
  font-family: 'Geist', -apple-system, sans-serif;
}

.actions :deep(.el-radio-button__inner) {
  font-size: 13px;
  font-weight: 500;
  box-shadow: none !important;
  border: 1px solid var(--ds-border) !important;
  background: var(--ds-background);
  color: var(--ds-text-secondary);
  border-radius: 6px;
  margin-left: 4px;
  transition: all 0.2s ease;
}
.actions :deep(.el-radio-button:first-child .el-radio-button__inner) {
  margin-left: 0;
}
.actions :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--ds-text);
  color: var(--ds-background);
  border-color: var(--ds-text) !important;
  box-shadow: none !important;
}

.chart-body {
  height: 320px;
  width: 100%;
}

/* Map */
.map-card {
  background: var(--ds-background);
  border-radius: 8px;
  padding: 24px;
  box-shadow: var(--ds-shadow-card);
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
}

.map-placeholder {
  flex: 1;
  min-height: 320px;
  background: #fafafa;
  border-radius: 6px;
  box-shadow: var(--ds-shadow-border) inset;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--ds-text-secondary);
  position: relative;
  font-size: 14px;
}

.status-badge {
  background: var(--ds-accent-blue-bg);
  color: var(--ds-accent-blue);
  padding: 2px 10px;
  border-radius: 9999px;
  font-size: 11px;
  font-weight: 500;
  font-family: 'Geist Mono', sans-serif;
}

.pulse-ring {
  position: absolute;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 1px solid var(--ds-border);
  opacity: 0;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.5); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

@media (max-width: 1024px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-grid { grid-template-columns: 1fr; }
}
@media (max-width: 600px) {
  .kpi-grid { grid-template-columns: 1fr; }
  .admin-main { padding: 24px 16px; }
}
</style>
