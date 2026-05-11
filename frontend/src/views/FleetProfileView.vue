<template>
  <div class="profile-container">
    <!-- Header -->
    <header class="profile-header">
      <div class="header-left" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">车队运营中心</div>
      <div class="header-right">
        <el-icon @click="handleLogout"><SwitchButton /></el-icon>
      </div>
    </header>

    <div class="profile-content" v-if="user.id">
      <!-- User Basic Info -->
      <div class="user-card" :style="cardStyle">
        <div class="user-avatar-wrapper">
          <el-avatar :size="64" :src="user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        </div>
        <div class="user-info">
          <div class="user-name">{{ user.username || '车队运营' }}</div>
          <div class="user-id">ID: {{ user.id }}</div>
          <div class="company-name" v-if="user.companyName">{{ user.companyName }}</div>
        </div>
        <div class="user-role-tag">
          {{ getRoleName(user.role) }}
        </div>
      </div>

      <!-- Fleet Stats -->
      <div class="section-title">运营概览</div>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value">{{ stats.drivers || 0 }}</div>
          <div class="stat-label">司机数量</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.orders || 0 }}</div>
          <div class="stat-label">订单总数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.pendingOrders || 0 }}</div>
          <div class="stat-label">待处理</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.completedOrders || 0 }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>

      <!-- Fleet Management Menu -->
      <div class="section-title">车队管理</div>
      <div class="menu-section">
        <div class="menu-item" @click="router.push('/fleet/drivers')">
          <div class="menu-icon-box blue">
            <el-icon><Van /></el-icon>
          </div>
          <div class="menu-content">
            <div class="menu-name">司机管理</div>
            <div class="menu-desc">管理车队司机账号</div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="router.push('/fleet/orders')">
          <div class="menu-icon-box green">
            <el-icon><Document /></el-icon>
          </div>
          <div class="menu-content">
            <div class="menu-name">订单管理</div>
            <div class="menu-desc">查看和管理清运订单</div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="router.push('/fleet/dispatch')">
          <div class="menu-icon-box orange">
            <el-icon><Location /></el-icon>
          </div>
          <div class="menu-content">
            <div class="menu-name">调度中心</div>
            <div class="menu-desc">实时监控和调度</div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="router.push('/fleet/grab-pool')">
          <div class="menu-icon-box purple">
            <el-icon><Lightning /></el-icon>
          </div>
          <div class="menu-content">
            <div class="menu-name">抢单池</div>
            <div class="menu-desc">公共订单池管理</div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="router.push('/fleet/projects')">
          <div class="menu-icon-box teal">
            <el-icon><Briefcase /></el-icon>
          </div>
          <div class="menu-content">
            <div class="menu-name">项目管理</div>
            <div class="menu-desc">自营项目管理</div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="router.push('/fleet/stats')">
          <div class="menu-icon-box red">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="menu-content">
            <div class="menu-name">数据统计</div>
            <div class="menu-desc">运营数据报表</div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- Account Info -->
      <div class="section-title">账户信息</div>
      <div class="info-module">
        <div class="info-row">
          <div class="info-label"><el-icon><Phone /></el-icon> 手机号</div>
          <div class="info-value">{{ maskPhone(user.phone) }}</div>
        </div>
        <div class="info-row">
          <div class="info-label"><el-icon><OfficeBuilding /></el-icon> 车队ID</div>
          <div class="info-value">{{ user.fleetId || '未分配' }}</div>
        </div>
        <div class="info-row">
          <div class="info-label"><el-icon><Clock /></el-icon> 注册时间</div>
          <div class="info-value">{{ formatDate(user.createdAt) }}</div>
        </div>
      </div>

      <div class="logout-wrapper">
        <el-button type="danger" plain class="logout-btn" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
    
    <div v-else class="loading-wrapper">
      <el-skeleton :rows="8" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowLeft, ArrowRight, SwitchButton, Van, Document, Location, 
  Lightning, Briefcase, DataAnalysis, Phone, OfficeBuilding, Clock 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const user = ref({})
const stats = ref({})

onMounted(() => {
  const localUser = localStorage.getItem('user')
  if (!localUser) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  user.value = JSON.parse(localUser)
  
  // Only allow fleet roles
  if (!['fleet_owner', 'fleet_dispatcher'].includes(user.value.role)) {
    ElMessage.warning('该页面仅限车队运营用户访问')
    router.replace('/home')
    return
  }
  
  // Load fleet stats
  loadFleetStats()
})

const loadFleetStats = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/api/fleet/stats?fleetId=${user.value.fleetId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (response.ok) {
      const data = await response.json()
      if (data.code === 200) {
        stats.value = data.data || {}
      }
    }
  } catch (error) {
    console.error('Failed to load fleet stats:', error)
  }
}

const getRoleName = (role) => {
  const map = {
    'fleet_owner': '车队所有者',
    'fleet_dispatcher': '车队调度员'
  }
  return map[role] || '车队运营'
}

const maskPhone = (phone) => {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const cardStyle = computed(() => {
  if (user.value.role === 'fleet_owner') {
    return {
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }
  } else {
    return {
      background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
    }
  }
})
</script>

<style scoped>
.profile-container {
  --color-primary: #667eea;
  --color-bg: #f5f7f9;
  background-color: var(--color-bg);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue", Helvetica, Arial, sans-serif;
}

.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 56px;
  background-color: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left, .header-right {
  width: 40px;
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

.profile-content {
  padding: 16px;
  flex: 1;
}

/* User Card */
.user-card {
  border-radius: 16px;
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.2);
  position: relative;
  overflow: hidden;
}

.user-avatar-wrapper {
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  padding: 2px;
}

.user-info {
  flex: 1;
}
.user-name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}
.user-id {
  font-size: 12px;
  opacity: 0.8;
  font-family: monospace;
}
.company-name {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 4px;
}

.user-role-tag {
  position: absolute;
  top: 0;
  right: 0;
  background: rgba(255,255,255,0.2);
  padding: 6px 12px;
  border-radius: 0 16px 0 16px;
  font-size: 12px;
  font-weight: 500;
}

.section-title {
  font-size: 14px;
  color: #999;
  margin-bottom: 12px;
  padding-left: 4px;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 8px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 11px;
  color: #999;
}

/* Menu Section */
.menu-section {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 24px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 12px;
  border-bottom: 1px solid #f5f7f9;
  cursor: pointer;
  transition: background 0.2s;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: #f8f9fa;
}

.menu-icon-box {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.menu-icon-box.blue { background: #e3f2fd; color: #2196f3; }
.menu-icon-box.green { background: #e8f5e9; color: #4caf50; }
.menu-icon-box.orange { background: #fff3e0; color: #ff9800; }
.menu-icon-box.purple { background: #f3e5f5; color: #9c27b0; }
.menu-icon-box.teal { background: #e0f2f1; color: #009688; }
.menu-icon-box.red { background: #ffebee; color: #f44336; }

.menu-content {
  flex: 1;
}
.menu-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}
.menu-desc {
  font-size: 12px;
  color: #999;
}

.arrow {
  color: #ccc;
  font-size: 16px;
}

/* Info Module */
.info-module {
  background: #fff;
  border-radius: 12px;
  padding: 0 16px;
  margin-bottom: 32px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f5f7f9;
}
.info-row:last-child {
  border-bottom: none;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
  font-size: 14px;
}
.info-label .el-icon {
  color: #999;
  font-size: 16px;
}

.info-value {
  color: #666;
  font-size: 14px;
}

/* Logout */
.logout-wrapper {
  padding: 0 16px;
  margin-bottom: 40px;
}
.logout-btn {
  width: 100%;
  height: 44px;
  border-radius: 22px;
  font-size: 15px;
  letter-spacing: 1px;
}

.loading-wrapper {
  padding: 20px;
}

@media (min-width: 768px) {
  .profile-container {
    max-width: 480px;
    margin: 0 auto;
    border-left: 1px solid #eee;
    border-right: 1px solid #eee;
    box-shadow: 0 0 30px rgba(0,0,0,0.05);
  }
}
</style>
