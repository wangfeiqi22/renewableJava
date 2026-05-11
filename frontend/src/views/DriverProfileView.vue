<template>
  <div class="driver-profile-container" :class="{ 'dark-mode': isDarkMode }">
    <!-- Header -->
    <AppHeader v-bind="headerProps" />

    <!-- Top Section with Gradient -->
    <div class="top-gradient-section" :class="isFleetDriver ? 'fleet-bg' : 'personal-bg'">
      <div class="center-title-row">
        <div class="title-icon-box">
          <el-icon><Van /></el-icon>
        </div>
        <div class="title-text-box">
          <h1 class="page-title">{{ t('workCenter') }}</h1>
          <p class="driver-type-badge">{{ isFleetDriver ? t('fleetDriverDesc') : t('personalDriverDesc') }}</p>
        </div>
        <div class="refresh-btn" @click="refreshData" :class="{ 'is-refreshing': refreshingStats }">
          <el-icon><Refresh /></el-icon>
        </div>
      </div>
      
      <!-- Business Data Board -->
      <div class="stats-row">
        <div class="stat-box">
          <div class="stat-val">
            <span class="currency">¥</span>
            <span class="animated-number">{{ formatMoney(stats.monthIncome || 0) }}</span>
          </div>
          <div class="stat-lbl">{{ t('monthIncome') }}</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-box">
          <div class="stat-val">
            <span class="animated-number">{{ stats.todayOrders || 0 }}</span>
            <span class="unit"> {{ t('orderUnit') }}</span>
          </div>
          <div class="stat-lbl">{{ t('todayOrders') }}</div>
        </div>
      </div>
    </div>

    <!-- Overlapping Notice Card -->
    <div class="notice-wrapper">
      <div class="notice-card" :class="isFleetDriver ? 'fleet-notice' : 'personal-notice'">
        <div class="notice-icon"><el-icon><Avatar /></el-icon></div>
        <div class="notice-content">
          <template v-if="isFleetDriver">
            {{ t('fleetNotice') }}
          </template>
          <template v-else>
            {{ t('individualTip') }}
          </template>
        </div>
      </div>
    </div>

    <!-- Fleet Driver Exclusive Area (Add Grab Hall Entry) -->
    <div v-if="isFleetDriver" class="personal-exclusive-area">
      <div class="all-orders-section">
        <div class="tool-card white-card" @click="goToGrabHall">
          <div class="tool-icon-box" style="background: var(--notice-bg-fleet); color: var(--notice-text-fleet);">
            <el-icon><Van /></el-icon>
          </div>
          <div class="tool-content">
            <div class="tool-title">进入抢单大厅</div>
            <div class="tool-desc">查看最新可抢订单</div>
          </div>
          <div class="right-badge">
            <el-icon class="arrow-icon gray-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
      <div class="all-orders-section">
        <div class="tool-card white-card" @click="goToAllOrders">
          <div class="tool-icon-box gray">
            <el-icon><Document /></el-icon>
          </div>
          <div class="tool-content">
            <div class="tool-title">查看我的订单</div>
            <div class="tool-desc">管理进行中及已完成订单</div>
          </div>
          <div class="right-badge">
            <el-icon class="arrow-icon gray-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- Personal Driver Exclusive Area -->
    <div v-if="!isFleetDriver" class="personal-exclusive-area">
      <!-- 1. Ongoing Orders -->
      <div class="ongoing-orders-section">
        <div class="section-header">
          <h3 class="section-title">当前进行中订单</h3>
        </div>
        
        <div v-if="loadingOngoing" class="loading-skeleton">
          <el-skeleton :rows="3" animated />
        </div>
        <template v-else>
          <div v-if="ongoingOrders.length > 0" class="ongoing-list">
            <div 
              v-for="order in ongoingOrders" 
              :key="order.id" 
              class="ongoing-card" 
              @click="goToOrderDetail(order.id)"
            >
              <div class="card-header">
                <span class="order-no">{{ order.orderNo }}</span>
                <span class="status-tag" :class="'status-' + order.status">
                  {{ getStatusText(order.status) }}
                </span>
              </div>
              <div class="route-info">
                <div class="route-point start">{{ order.startAddress }}</div>
                <div class="route-arrow"><el-icon><ArrowRight /></el-icon></div>
                <div class="route-point end">{{ order.endAddress }}</div>
              </div>
              <div class="order-meta">
                <span class="meta-tag">{{ order.wasteType }}</span>
                <span class="meta-time">{{ order.plannedTime }}</span>
              </div>
              <div class="progress-section">
                <div class="progress-labels">
                  <span>已完成 {{ order.completedDistance || 0 }}km</span>
                  <span>总里程 {{ order.totalDistance || 0 }}km</span>
                </div>
                <el-progress 
                  :percentage="calcProgress(order)" 
                  :show-text="false" 
                  color="var(--personal-gradient)"
                ></el-progress>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-empty :description="t('noOngoingOrder')" :image-size="80">
              <template #image>
                <div class="empty-img-placeholder"></div>
              </template>
              <el-button type="primary" plain round @click="goToGrabHall">去抢单大厅</el-button>
            </el-empty>
          </div>
        </template>
      </div>

      <!-- 2. All Orders Entry -->
      <div class="all-orders-section">
        <div class="tool-card white-card" @click="goToAllOrders">
          <div class="tool-icon-box gray">
            <el-icon><Document /></el-icon>
          </div>
          <div class="tool-content">
            <div class="tool-title">查看我的全部订单</div>
            <div class="tool-desc">近30天已完成订单总量</div>
          </div>
          <div class="right-badge">
            <div class="badge-count">{{ stats.last30DaysCompleted }}单</div>
            <el-icon class="arrow-icon gray-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. Floating Create Order Button -->
    <div v-if="!isFleetDriver" class="create-order-fab" @click="goToCreateOrder">
      <div class="fab-content">
        <el-icon class="plus-icon"><Plus /></el-icon>
        <span class="fab-text">{{ t('createOrder') }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Van, Bell, Setting, Avatar, SwitchButton, List, Guide, Money, Tickets, Service, Lock, ArrowRight, Moon, Sunny, Loading, Plus, Document, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AppHeader from '../components/AppHeader.vue'
import api from '../api'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')

// Driver Type Identifier (Fleet Driver if fleetId exists, else Personal Driver)
const isFleetDriver = computed(() => !!user.fleetId)

const isDarkMode = ref(false)
const isOnline = ref(true)
const loadingStatus = ref(false)
const serviceScore = ref(4.9)
const currentLang = ref('zh')

// --- i18n Mock ---
const messages = {
  zh: {
    driverApp: '司机端',
    workCenter: '司机工作中心',
    fleetDriverDesc: '车队司机·持证运营',
    personalDriverDesc: '个人司机·自由接单',
    monthIncome: '本月收入',
    todayOrders: '今日订单',
    orderUnit: '单',
    fleetNotice: '您是车队司机，持有建筑垃圾处置证，归属车队管理。可在公海抢单或接收车队派单，完成后获得收益。接单后系统会智能推荐最近的处置场所。',
    personalNotice: '您是个人司机，自由安排接单时间。可通过抢单大厅获取附近订单，多劳多得。请保持良好的服务态度以提升评分。',
    personalInfo: '个人信息',
    driverMaster: '司机师傅',
    certified: '已认证',
    score: '分',
    onlineTime: '在线时长',
    creditLevel: '信用等级',
    completionRate: '完成率',
    quickEntrances: '快捷业务',
    startTakingOrders: '开始接单',
    stopTakingOrders: '收车休息',
    orderManagement: '订单管理',
    routePlanning: '路线规划',
    paymentCode: '收款码',
    invoiceApply: '发票申请',
    customerService: '客服中心',
    messageCenter: '消息中心',
    accountSecurity: '账号与安全',
    logout: '退出登录',
    statusChanged: '状态已切换',
    individualTip: '您是个人司机，自由安排接单时间。可通过抢单大厅获取附近订单，多劳多得。请保持良好的服务态度以提升评分。',
    noOngoingOrder: '暂无进行中订单',
    createOrder: '+ 创建清运单'
  },
  en: {
    driverApp: 'Driver App',
    workCenter: 'Driver Work Center',
    fleetDriverDesc: 'Fleet Driver · Certified',
    personalDriverDesc: 'Personal Driver · Freelance',
    monthIncome: 'Monthly Income',
    todayOrders: 'Today Orders',
    orderUnit: 'Orders',
    fleetNotice: 'You are a fleet driver with a construction waste disposal certificate. You can grab orders in the public pool or receive fleet dispatches. The system will recommend the nearest disposal site.',
    personalNotice: 'You are a personal driver. Free to schedule your time. Grab orders from the hall to earn more. Maintain good service to improve your score.',
    personalInfo: 'Personal Info',
    driverMaster: 'Driver',
    certified: 'Certified',
    score: 'Pts',
    onlineTime: 'Online Time',
    creditLevel: 'Credit Level',
    completionRate: 'Completion Rate',
    quickEntrances: 'Quick Actions',
    startTakingOrders: 'Start Taking Orders',
    stopTakingOrders: 'Stop Taking Orders',
    orderManagement: 'Order Management',
    routePlanning: 'Route Planning',
    paymentCode: 'Payment Code',
    invoiceApply: 'Invoice Apply',
    customerService: 'Customer Service',
    messageCenter: 'Message Center',
    accountSecurity: 'Account Security',
    logout: 'Logout',
    statusChanged: 'Status Changed',
    individualTip: 'You are a personal driver. Free to schedule your time. Grab orders from the hall to earn more. Maintain good service to improve your score.',
    noOngoingOrder: 'No ongoing orders',
    createOrder: '+ Create Order'
  }
}

const t = (key) => messages[currentLang.value][key] || key

const toggleLang = () => {
  currentLang.value = currentLang.value === 'zh' ? 'en' : 'zh'
}

// --- Theme ---
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  // Note: Using scoped CSS variables for dark mode instead of global class for isolation,
  // but adding a class to the container to trigger the CSS variables switch.
}

// --- Interactions ---
const toggleStatus = () => {
  if (loadingStatus.value) return
  loadingStatus.value = true
  setTimeout(() => {
    isOnline.value = !isOnline.value
    loadingStatus.value = false
    ElMessage.success(t('statusChanged') + ': ' + (isOnline.value ? t('startTakingOrders') : t('stopTakingOrders')))
  }, 300) // Mock API latency < 500ms
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const headerProps = computed(() => ({
  title: 'EcoClear',
  subtitle: t('driverApp'),
  icon: 'Van'
}))

// Security / Role check on mount
onMounted(() => {
  if (user.role !== 'driver' && user.role !== 'admin') {
    ElMessage.warning('该页面仅限司机访问')
    router.replace('/home')
    return
  }
  // Always fetch stats for both types to display income and order count
  fetchStats()
  
  if (!isFleetDriver.value) {
    fetchOngoingOrders()
  }
})

// --- Personal Driver State & Logic ---
const ongoingOrders = ref([])
const loadingOngoing = ref(false)
const stats = ref({ last30DaysCompleted: 0, monthIncome: 0, todayOrders: 0 })
const refreshingStats = ref(false)

const formatMoney = (val) => {
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const createModalVisible = ref(false)
const createStep = ref(0)
const availableVehicles = ref([])
const locating = ref(false)
const submittingOrder = ref(false)

const createForm = ref({
  vehicleId: '',
  wasteType: '',
  weight: 1,
  photos: [],
  startAddress: '',
  lat: null,
  lng: null
})

const fetchOngoingOrders = async () => {
  loadingOngoing.value = true
  try {
    const res = await api.get(`/orders/user/${user.id}`)
    let orders = res.data || []
    orders = orders.filter(o => o.status >= 20 && o.status < 60)
    orders.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    ongoingOrders.value = orders.slice(0, 3)
  } catch (error) {
    console.error('Failed to fetch ongoing orders', error)
  } finally {
    loadingOngoing.value = false
  }
}

const fetchStats = async () => {
  try {
    const res = await api.get(`/orders/user/${user.id}`)
    const orders = res.data || []
    
    const now = new Date()
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1).getTime()
    const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
    
    let monthIncome = 0
    let todayOrdersCount = 0
    let last30DaysCompletedCount = 0
    
    const thirtyDaysAgo = now.getTime() - (30 * 24 * 60 * 60 * 1000)

    orders.forEach(o => {
      const orderTime = new Date(o.createdAt).getTime()
      
      // Calculate last 30 days completed
      if (o.status === 60 && orderTime >= thirtyDaysAgo) {
        last30DaysCompletedCount++
      }

      // Calculate today's completed orders
      if (o.status === 60 && orderTime >= startOfDay) {
        todayOrdersCount++
      }

      // Calculate month income (only for completed orders this month)
      if (o.status === 60 && orderTime >= startOfMonth) {
        // Assume income is stored in 'amount' or 'commission'
        // Using 'amount' for demo, adapt based on actual backend data structure
        const amount = parseFloat(o.amount || 0)
        monthIncome += amount
      }
    })

    stats.value = {
      monthIncome: monthIncome,
      todayOrders: todayOrdersCount,
      last30DaysCompleted: last30DaysCompletedCount
    }
  } catch (error) {
    console.error('Failed to fetch stats', error)
    stats.value = { monthIncome: 0, todayOrders: 0, last30DaysCompleted: 0 }
  }
}

const refreshData = async () => {
  if (refreshingStats.value) return
  refreshingStats.value = true
  try {
    await Promise.all([
      fetchOngoingOrders(),
      fetchStats()
    ])
    ElMessage.success('数据已更新')
  } catch (error) {
    ElMessage.error('刷新失败，请稍后重试')
  } finally {
    refreshingStats.value = false
  }
}

const fetchVehicles = async () => {
  try {
    const res = await api.get('/driver/vehicles')
    availableVehicles.value = res.data || []
  } catch (error) {
    availableVehicles.value = [
      { id: 'v1', plateNumber: '京A·88888', isValid: true },
      { id: 'v2', plateNumber: '京A·66666', isValid: false }
    ]
  }
}

const calcProgress = (order) => {
  if (!order.totalDistance) return 0
  return Math.min(100, Math.round(((order.completedDistance || 0) / order.totalDistance) * 100))
}

const getStatusText = (status) => {
  const map = {
    10: '待接单',
    20: '待接单',
    25: '已接单',
    30: '装车中',
    40: '运输中',
    45: '已到站',
    50: '已到站',
    60: '已完成'
  }
  return map[status] || '未知'
}

const goToOrderDetail = (id) => {
  router.push(`/order/detail/${id}`)
}

const goToGrabHall = () => {
  if (isFleetDriver.value) {
    router.push({ path: '/driver/tasks', query: { tab: 'pool' } })
  } else {
    router.push({ path: '/driver/tasks', query: { tab: 'create' } })
  }
}

const goToAllOrders = () => {
  router.push({ path: '/driver/orders', query: { tab: 'all' } })
}

const goToCreateOrder = () => {
  router.push('/order/create')
}
</script>

<style scoped>
/* --- Design Tokens & CSS Variables --- */
.driver-profile-container {
  /* Light Mode Variables */
  --bg-color: #f5f7f9;
  --card-bg: #ffffff;
  --text-main: #333333;
  --text-sub: #999999;
  --border-color: #f0f0f0;
  --header-bg: #ffffff;
  
  --fleet-gradient: linear-gradient(135deg, #1a73e8 0%, #00e5ff 100%);
  --personal-gradient: linear-gradient(135deg, #0dc8a3 0%, #07a685 100%);
  --notice-bg-fleet: #e8f0fe;
  --notice-text-fleet: #1a73e8;
  --notice-bg-personal: #e6f9f5;
  --notice-text-personal: #0dc8a3;

  min-height: 100vh;
  background-color: var(--bg-color);
  color: var(--text-main);
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue", Helvetica, Arial, sans-serif;
  overflow-x: hidden;
}

/* Dark Mode Overrides */
.driver-profile-container.dark-mode {
  --bg-color: #121212;
  --card-bg: #1e1e1e;
  --text-main: #e0e0e0;
  --text-sub: #888888;
  --border-color: #333333;
  --header-bg: #1e1e1e;
  
  --fleet-gradient: linear-gradient(135deg, #134b99 0%, #008899 100%);
  --personal-gradient: linear-gradient(135deg, #09876e 0%, #05705a 100%);
  --notice-bg-fleet: rgba(26, 115, 232, 0.15);
  --notice-text-fleet: #66a3ff;
  --notice-bg-personal: rgba(13, 200, 163, 0.15);
  --notice-text-personal: #4de6c8;
}

/* --- Top Gradient Section --- */
.top-gradient-section {
  padding: 24px 20px 60px;
  color: #fff;
  border-bottom-left-radius: 24px;
  border-bottom-right-radius: 24px;
}
.fleet-bg {
  background: var(--fleet-gradient);
}
.personal-bg {
  background: var(--personal-gradient);
}

.center-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}
.title-icon-box {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.title-text-box {
  display: flex;
  flex-direction: column;
}
.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
}
.driver-type-badge {
  margin: 4px 0 0;
  font-size: 12px;
  opacity: 0.9;
}

.stats-row {
  display: flex;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 16px 0;
  backdrop-filter: blur(10px);
}
.stat-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.stat-divider {
  width: 1px;
  background: rgba(255, 255, 255, 0.3);
}
.stat-val {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}
.stat-lbl {
  font-size: 12px;
  opacity: 0.9;
  display: flex;
  align-items: center;
  gap: 4px;
}
.trend {
  background: rgba(255, 255, 255, 0.25);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
}

/* --- Notice Card --- */
.notice-wrapper {
  padding: 0 16px;
  margin-top: -30px;
  position: relative;
  z-index: 5;
}
.notice-card {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}
.notice-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.fleet-notice .notice-icon {
  background: var(--notice-bg-fleet);
  color: var(--notice-text-fleet);
}
.personal-notice .notice-icon {
  background: var(--notice-bg-personal);
  color: var(--notice-text-personal);
}
.notice-content {
  font-size: 13px;
  color: var(--text-sub);
  line-height: 1.5;
}

/* --- Personal Exclusive Area --- */
.personal-exclusive-area {
  padding: 0 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-header {
  margin-bottom: 12px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-main);
  margin: 0;
}

.ongoing-card {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  cursor: pointer;
  border: 1px solid var(--border-color);
  transition: transform 0.2s, box-shadow 0.2s;
}
.ongoing-card:active {
  transform: scale(0.98);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-no {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
}
.status-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}
.status-10 { background: rgba(26, 115, 232, 0.1); color: #1a73e8; }
.status-20, .status-25 { background: rgba(255, 152, 0, 0.1); color: #ff9800; }
.status-30, .status-40 { background: rgba(26, 115, 232, 0.1); color: #1a73e8; }
.status-45, .status-50 { background: rgba(13, 200, 163, 0.1); color: #0dc8a3; }
.status-60 { background: rgba(144, 147, 153, 0.1); color: #909399; }

.route-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-main);
}
.route-arrow {
  color: var(--text-sub);
}

.order-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-sub);
  margin-bottom: 16px;
}

.progress-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed var(--border-color);
}
.progress-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-sub);
  margin-bottom: 6px;
}

.empty-state {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 32px 0;
  border: 1px dashed var(--border-color);
}
.empty-img-placeholder {
  width: 80px;
  height: 80px;
  background: url('data:image/svg+xml;utf8,<svg viewBox="0 0 24 24" fill="none" stroke="%23ccc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>') no-repeat center;
  background-size: contain;
}

/* Tool Card */
.tool-card {
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  cursor: pointer;
}
.white-card {
  background: var(--card-bg);
}
.tool-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.tool-icon-box.gray {
  background: var(--bg-color);
  color: var(--text-sub);
}
.tool-content {
  flex: 1;
}
.tool-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
  color: var(--text-main);
}
.tool-desc {
  font-size: 12px;
  color: var(--text-sub);
}
.right-badge {
  display: flex;
  align-items: center;
  gap: 8px;
}
.badge-count {
  background: var(--personal-gradient);
  color: white;
  border-radius: 12px;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: bold;
}
.gray-arrow {
  color: var(--text-sub);
  font-size: 20px;
}

/* Floating Action Button */
.create-order-fab {
  position: fixed;
  bottom: 80px;
  right: 24px;
  z-index: 99;
  background: var(--personal-gradient);
  color: white;
  border-radius: 28px;
  padding: 0 20px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(13, 200, 163, 0.4);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.create-order-fab:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(13, 200, 163, 0.5);
}
.create-order-fab:active {
  transform: translateY(1px);
}
.fab-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}
.plus-icon {
  font-size: 20px;
}

/* Modal Styles */

/* --- Main Content --- */
.main-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: var(--card-bg);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
}

/* Profile Card */
.profile-card {
  padding: 16px;
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.profile-info {
  flex: 1;
}
.profile-info .name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-main);
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.cert-tag {
  border-radius: 12px;
}
.score-wrapper {
  transform: scale(0.9);
  transform-origin: left center;
}

.profile-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-color);
  border-radius: 8px;
  padding: 12px;
}
.p-stat {
  flex: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.p-val {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-main);
}
.p-val.text-primary {
  color: #1a73e8;
}
.p-lbl {
  font-size: 12px;
  color: var(--text-sub);
}
.p-divider {
  width: 1px;
  height: 24px;
  background: var(--border-color);
}

/* Quick Entrances */
.section-title-row {
  padding: 8px 4px 0;
}
.section-title-row h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-main);
}

.grid-card {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  padding: 16px 0;
}
.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 0;
  cursor: pointer;
  transition: opacity 0.2s;
}
.grid-item:active {
  opacity: 0.7;
}
.grid-item.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.grid-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}
.blue-bg { background: #1a73e8; }
.gray-bg { background: #9e9e9e; }
.green-bg { background: #0dc8a3; }
.orange-bg { background: #ff9800; }
.purple-bg { background: #9c27b0; }
.cyan-bg { background: #00bcd4; }
.teal-bg { background: #009688; }
.grid-text {
  font-size: 12px;
  color: var(--text-main);
}

/* List Card */
.list-card {
  padding: 0 16px;
}
.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: opacity 0.2s;
}
.list-item:last-child {
  border-bottom: none;
}
.list-item:active {
  opacity: 0.7;
}
.list-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.li-icon-wrap {
  font-size: 18px;
}
.blue-text { color: #1a73e8; }
.green-text { color: #0dc8a3; }
.text-danger { color: #f56c6c; }
.li-text {
  font-size: 15px;
  color: var(--text-main);
}
.text-danger .li-text {
  color: #f56c6c;
}
.list-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.arrow-right {
  color: var(--text-sub);
}

/* Responsive adjustments */
@media (min-width: 768px) {
  .driver-profile-container {
    max-width: 480px;
    margin: 0 auto;
    border-left: 1px solid var(--border-color);
    border-right: 1px solid var(--border-color);
    box-shadow: 0 0 30px rgba(0,0,0,0.05);
  }
  .create-order-fab {
    right: calc(50% - 240px + 24px); /* Align with max-width container */
  }
}
</style>
