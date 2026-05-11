<template>
  <div class="profile-container">
    <!-- Header -->
    <header class="profile-header">
      <div class="header-left" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">个人中心</div>
      <div class="header-right">
        <el-icon @click="router.push('/settings')"><Setting /></el-icon>
      </div>
    </header>

    <div class="profile-content" v-if="user.id">
      <!-- User Basic Info -->
      <div class="user-card">
        <div class="user-avatar-wrapper">
          <el-avatar :size="64" :src="user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        </div>
        <div class="user-info">
          <div class="user-name">{{ user.username || 'Eco用户' }}</div>
          <div class="user-id">ID: {{ user.id }}</div>
        </div>
        <div class="user-role-tag">
          {{ getRoleName(user.role) }}
        </div>
      </div>

      <!-- Account Info Module -->
      <div class="section-title">账户信息</div>
      <div class="info-module">
        <div class="info-row">
          <div class="info-label"><el-icon><Phone /></el-icon> 手机号</div>
          <div class="info-value">{{ maskPhone(user.phone) }}</div>
        </div>
        <div class="info-row">
          <div class="info-label"><el-icon><Message /></el-icon> 邮箱</div>
          <div class="info-value">{{ user.email || '未绑定' }} <el-icon class="arrow"><ArrowRight /></el-icon></div>
        </div>
        <div class="info-row">
          <div class="info-label"><el-icon><UserFilled /></el-icon> 实名认证</div>
          <div class="info-value">
            <span class="status-tag success" v-if="user.idNumber">已认证</span>
            <span class="status-tag warning" v-else>未认证</span>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- Functional Menu -->
      <div class="section-title">功能服务</div>
      <div class="menu-grid">
        <div class="menu-item" @click="router.push('/order/history')">
          <div class="menu-icon-box blue">
            <el-icon><Document /></el-icon>
          </div>
          <div class="menu-name">我的订单</div>
        </div>
        <div class="menu-item" @click="router.push('/address')">
          <div class="menu-icon-box green">
            <el-icon><Location /></el-icon>
          </div>
          <div class="menu-name">收货地址</div>
        </div>
        <div class="menu-item" @click="router.push('/coupons')">
          <div class="menu-icon-box orange">
            <el-icon><Ticket /></el-icon>
          </div>
          <div class="menu-name">优惠券</div>
        </div>
        <div class="menu-item" @click="router.push('/settings')">
          <div class="menu-icon-box gray">
            <el-icon><Setting /></el-icon>
          </div>
          <div class="menu-name">系统设置</div>
        </div>
      </div>

      <div class="logout-wrapper">
        <el-button type="danger" plain class="logout-btn" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
    
    <div v-else class="loading-wrapper">
      <el-skeleton :rows="5" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Setting, Phone, Message, UserFilled, ArrowRight, Document, Location, Ticket } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const user = ref({})

onMounted(() => {
  const localUser = localStorage.getItem('user')
  if (!localUser) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  user.value = JSON.parse(localUser)
  
  // Extra security check for role
  if (['admin', 'property', 'station', 'fleet', 'street'].includes(user.value.role)) {
    ElMessage.warning('该页面仅限个人用户访问')
    router.replace('/home')
  }
})

const getRoleName = (role) => {
  const map = {
    'individual': '个人用户',
    'vip': 'VIP客户',
    'driver': '认证司机'
  }
  return map[role] || '普通用户'
}

const maskPhone = (phone) => {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.profile-container {
  --color-primary: #0dc8a3;
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
  font-size: 20px;
  color: #333;
  cursor: pointer;
}
.header-right {
  justify-content: flex-end;
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
  background: linear-gradient(135deg, #0dc8a3 0%, #00a884 100%);
  border-radius: 16px;
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 8px 20px rgba(13, 200, 163, 0.2);
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

/* Info Module */
.info-module {
  background: #fff;
  border-radius: 12px;
  padding: 0 16px;
  margin-bottom: 24px;
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
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}
.arrow {
  color: #ccc;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.status-tag.success {
  background: #e8f5e9;
  color: #0dc8a3;
}
.status-tag.warning {
  background: #fff3e0;
  color: #ff9800;
}

/* Menu Grid */
.menu-grid {
  background: #fff;
  border-radius: 12px;
  padding: 20px 16px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  margin-bottom: 32px;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.menu-icon-box {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.menu-icon-box.blue { background: #e3f2fd; color: #2196f3; }
.menu-icon-box.green { background: #e8f5e9; color: #4caf50; }
.menu-icon-box.orange { background: #fff3e0; color: #ff9800; }
.menu-icon-box.gray { background: #f5f5f5; color: #9e9e9e; }

.menu-name {
  font-size: 12px;
  color: #333;
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
