<template>
  <div class="station-work-container">
    <!-- App Header -->
    <header class="app-header">
      <div class="header-left">
        <el-icon class="logo-icon"><Van /></el-icon>
        <span class="title">EcoClear</span>
        <span class="subtitle">清运站端</span>
      </div>
      <div class="header-right">
        <span class="user-info" v-if="user">{{ user.username }}</span>
        <el-button type="danger" link @click="handleLogout">退出</el-button>
      </div>
    </header>

    <main class="main-content">
      <!-- Profile/Stats Card -->
      <div class="profile-card">
        <div class="profile-bg-circle"></div>
        <div class="profile-header">
          <div class="avatar-placeholder">
            <el-icon><Van /></el-icon>
          </div>
          <div class="profile-info">
            <h2 class="station-name">清运站工作中心 <el-icon class="refresh-icon" @click="fetchIncoming" :class="{ 'is-loading': loadingIncoming }"><Refresh /></el-icon></h2>
            <div class="station-tags">
              {{ user.username || '清运站' }} · 持证运营
            </div>
          </div>
        </div>
        
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-value">¥0.00</div>
            <div class="stat-label">本月收入</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-value">{{ historyOrders.length }}单</div>
            <div class="stat-label">今日订单</div>
          </div>
        </div>
      </div>

      <!-- Action Tabs -->
      <div class="filter-section">
        <el-tabs v-model="activeTab" class="custom-tabs" stretch>
          <el-tab-pane label="车辆入场" name="incoming"></el-tab-pane>
          <el-tab-pane label="今日已完成" name="history"></el-tab-pane>
          <el-tab-pane label="站点信息" name="profile"></el-tab-pane>
        </el-tabs>
      </div>

      <!-- List Content -->
      <div class="list-content">
        <!-- Incoming Tab -->
        <div v-show="activeTab === 'incoming'">
          <div class="tab-toolbar">
            <el-button type="primary" plain size="small" icon="Refresh" @click="fetchIncoming" :loading="loadingIncoming" round>刷新</el-button>
          </div>
          
          <div v-if="loadingIncoming" class="skeleton-wrapper">
            <el-skeleton :rows="3" animated v-for="i in 3" :key="i" class="mb-4" />
          </div>
          <el-empty v-else-if="incomingOrders.length === 0" description="暂无车辆到达" />
          
          <div v-else class="orders-wrapper">
            <div v-for="order in incomingOrders" :key="order.id" class="order-card">
              <div class="card-header">
                <div class="order-no">
                  <span class="label">订单号</span>
                  <span class="value">{{ order.orderNo.substring(0, 8) }}</span>
                </div>
                <div class="order-status" :class="order.status === 40 ? 'status-primary' : 'status-warning'">
                  {{ order.status === 40 ? '待到站' : '待称重' }}
                </div>
              </div>
              
              <div class="card-body" @click="openDetail(order)">
                <div class="info-row">
                  <div class="info-item full-width">
                    <el-icon class="icon location-icon"><Location /></el-icon>
                    <div class="location-text">
                      <div class="start-addr">{{ order.pickupAddress }}</div>
                      <div class="end-addr">到达本站</div>
                    </div>
                  </div>
                </div>
                <div class="info-row split">
                  <div class="info-item">
                    <el-icon class="icon"><Van /></el-icon>
                    <span>{{ getWasteTypeName(order.wasteType) }}</span>
                  </div>
                </div>
              </div>

              <div class="card-footer">
                <div class="footer-left">
                  <span class="weight-badge">预估 {{ order.estWeight }} kg</span>
                </div>
                <div class="footer-actions">
                  <el-button v-if="order.status === 40" type="primary" round size="small" @click="confirmArrival(order.id)">确认到站</el-button>
                  <el-button v-if="order.status === 50" type="success" round size="small" @click="openWeighIn(order)">称重入库</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- History Tab -->
        <div v-show="activeTab === 'history'">
          <div v-if="loadingHistory" class="skeleton-wrapper">
            <el-skeleton :rows="3" animated v-for="i in 3" :key="i" class="mb-4" />
          </div>
          <el-empty v-else-if="historyOrders.length === 0" description="今日暂无完成订单" />
          
          <div v-else class="orders-wrapper">
            <div v-for="order in historyOrders" :key="order.id" class="order-card history-card" @click="openDetail(order)">
              <div class="card-header">
                <div class="order-no">
                  <span class="label">订单号</span>
                  <span class="value">{{ order.orderNo.substring(0, 8) }}</span>
                </div>
                <div class="order-status status-success">已入库</div>
              </div>
              <div class="card-body">
                <div class="info-row split">
                  <div class="info-item">
                    <el-icon class="icon"><Van /></el-icon>
                    <span>{{ getWasteTypeName(order.wasteType) }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon class="icon"><Calendar /></el-icon>
                    <span>{{ formatDate(order.updatedAt) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Profile Tab -->
        <div v-show="activeTab === 'profile'" class="profile-tab">
          <el-form :model="stationProfile" label-position="top">
            <el-form-item label="站点公告">
              <el-input v-model="stationProfile.announcement" type="textarea" :rows="3" placeholder="如：营业时间、临时停运公告等" />
            </el-form-item>
            <el-form-item label="站点介绍">
              <el-input v-model="stationProfile.description" type="textarea" :rows="4" placeholder="简要介绍本站点的服务范围、处理能力等" />
            </el-form-item>
            <el-form-item label="库存概况">
              <el-input v-model="stationProfile.inventorySummary" type="textarea" :rows="3" placeholder="例如：再生骨料 120 吨、金属压块 30 吨等" />
            </el-form-item>
            <div class="form-actions">
              <el-button type="primary" round class="submit-btn" @click="saveProfile" :loading="profileSaving">保存设置</el-button>
            </div>
          </el-form>
        </div>
      </div>

      <!-- Weigh In Dialog -->
      <el-dialog v-model="weighDialogVisible" title="车辆称重" width="400px">
        <el-form :model="weighForm">
          <el-form-item label="实际立方 (m³)">
            <el-input-number v-model="weighForm.weight" :precision="2" :step="1" :min="0.1" style="width: 100%" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="weighDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitWeighIn">确认入库</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- Order Detail Dialog -->
      <el-dialog
        v-model="detailVisible"
        title="订单详情"
        width="70%"
        destroy-on-close
      >
        <div v-if="currentOrder" v-loading="detailLoading">
          <div class="detail-basic">
            <div class="detail-row">
              <span class="detail-label">订单号：</span>
              <span class="detail-value">{{ currentOrder.orderNo }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">垃圾类型：</span>
              <span class="detail-value">{{ getWasteTypeName(currentOrder.wasteType) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">起运地址：</span>
              <span class="detail-value">{{ currentOrder.pickupAddress }}</span>
            </div>
          </div>

          <order-timeline :logs="currentLogs" :photos="currentPhotos" />
        </div>
      </el-dialog>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Van, Refresh, School, Location, Calendar } from '@element-plus/icons-vue'
import OrderTimeline from '../components/OrderTimeline.vue'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const activeTab = ref('incoming')
const incomingOrders = ref([])
const historyOrders = ref([])
const weighDialogVisible = ref(false)
const currentOrder = ref(null)
const weighForm = reactive({ weight: 0 })
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentLogs = ref([])
const currentPhotos = ref([])

const loadingIncoming = ref(false)
const loadingHistory = ref(false)

const stationProfile = reactive({
  announcement: '',
  description: '',
  inventorySummary: ''
})
const profileSaving = ref(false)

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const getWasteTypeName = (val) => {
  const normalizedVal = val ? val.toString().toUpperCase() : ''

  const map = {
    'HOUSEHOLD': '生活垃圾',
    'CONSTRUCTION': '建筑垃圾',
    'BULKY': '大件垃圾',
    'KITCHEN': '厨余垃圾',
    'RECYCLABLE': '可回收物',
    'HAZARDOUS': '有害垃圾',
    'OTHER': '其他垃圾'
  }

  return map[normalizedVal] || val || '-'
}

const fetchIncoming = async () => {
  loadingIncoming.value = true
  try {
    const res = await api.get('/station/ops/incoming')
    incomingOrders.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loadingIncoming.value = false
  }
}

const fetchHistory = async () => {
  loadingHistory.value = true
  try {
    const res = await api.get('/station/ops/history')
    historyOrders.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loadingHistory.value = false
  }
}

const fetchProfile = async () => {
  try {
    const res = await api.get('/stations/me')
    if (res.data) {
      stationProfile.announcement = res.data.announcement || ''
      stationProfile.description = res.data.description || ''
      stationProfile.inventorySummary = res.data.inventorySummary || ''
    }
  } catch (e) {
    console.error(e)
  }
}

const saveProfile = async () => {
  if (profileSaving.value) return
  profileSaving.value = true
  try {
    await api.put('/stations/me/profile', {
      announcement: stationProfile.announcement,
      description: stationProfile.description,
      inventorySummary: stationProfile.inventorySummary
    })
    ElMessage.success('站点信息已保存')
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    profileSaving.value = false
  }
}

const confirmArrival = async (orderId) => {
  try {
    await api.put(`/station/ops/confirm-arrival/${orderId}`)
    ElMessage.success('已确认车辆到站')
    fetchIncoming()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const openWeighIn = (order) => {
  currentOrder.value = order
  weighForm.weight = order.estWeight // default to est
  weighDialogVisible.value = true
}

const submitWeighIn = async () => {
  try {
    await api.put(`/station/ops/weigh-in/${currentOrder.value.id}?weight=${weighForm.weight}`)
    ElMessage.success('入库完成')
    weighDialogVisible.value = false
    fetchIncoming()
    fetchHistory()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const openDetail = async (order) => {
  if (detailLoading.value) return
  detailLoading.value = true

  try {
    currentOrder.value = order
    currentLogs.value = []
    currentPhotos.value = []

    const [resLogs, resPhotos] = await Promise.all([
      api.get(`/orders/${order.id}/logs`),
      api.get(`/orders/${order.id}/photos`)
    ])

    currentLogs.value = resLogs.data
    currentPhotos.value = resPhotos.data || []
    detailVisible.value = true
  } catch (error) {
    console.error(error)
    ElMessage.error('获取订单详情失败')
  } finally {
    detailLoading.value = false
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  fetchIncoming()
  fetchHistory()
  fetchProfile()
})
</script>

<style scoped>
.station-work-container {
  min-height: 100vh;
  background-color: #f5f7f9;
  display: flex;
  flex-direction: column;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  background-color: #fff;
  padding: 0 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  position: relative;
  padding-left: 36px;
}

.logo-icon {
  font-size: 28px;
  color: #0dc8a3;
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
}

.title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}

.subtitle {
  font-size: 12px;
  color: #999;
  line-height: 1.2;
}

.user-info {
  margin-right: 12px;
  font-size: 14px;
  color: #666;
}

.main-content {
  flex: 1;
  padding: 16px;
  max-width: 480px;
  margin: 0 auto;
  width: 100%;
}

.profile-card {
  background: linear-gradient(135deg, #0fb2f0 0%, #00d2ff 100%);
  border-radius: 16px;
  padding: 24px 20px 30px;
  color: white;
  margin-top: -16px;
  margin-bottom: 20px;
  box-shadow: 0 8px 24px rgba(15, 178, 240, 0.3);
  position: relative;
  overflow: hidden;
  width: calc(100% + 32px);
  margin-left: -16px;
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}

.profile-bg-circle {
  position: absolute;
  top: -80px;
  right: -50px;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0) 70%);
  border-radius: 50%;
  pointer-events: none;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
}

.avatar-placeholder {
  width: 56px;
  height: 56px;
  background: rgba(255,255,255,0.2);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.station-name {
  margin: 0 0 6px 0;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-icon {
  font-size: 16px;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.3s;
}

.refresh-icon:hover {
  opacity: 1;
}

.refresh-icon.is-loading {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  100% { transform: rotate(360deg); }
}

.station-tags {
  font-size: 13px;
  opacity: 0.9;
}

.stats-grid {
  display: flex;
  background: rgba(255,255,255,0.15);
  border-radius: 12px;
  padding: 16px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-divider {
  width: 1px;
  background: rgba(255,255,255,0.3);
  margin: 0 15px;
}

.stat-value {
  font-size: 26px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.9;
}

.filter-section {
  background: #fff;
  border-radius: 12px;
  padding: 0 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
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
}

.tab-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.orders-wrapper {
  display: flex;
  flex-direction: column;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
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
.status-warning { color: #ff9800; }

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

.profile-tab {
  background: white;
  border-radius: 12px;
  padding: 20px;
}

.form-actions {
  margin-top: 30px;
}

.submit-btn {
  width: 100%;
}

.detail-basic {
  margin-bottom: 16px;
}

.detail-row {
  margin-bottom: 6px;
  font-size: 14px;
}

.detail-label {
  color: #909399;
  margin-right: 4px;
}

.detail-value {
  color: #303133;
}

@media (min-width: 768px) {
  .main-content {
    border-left: 1px solid #ebeef5;
    border-right: 1px solid #ebeef5;
    background: #fff;
    min-height: calc(100vh - 56px);
    padding: 16px;
  }
  
  .profile-card {
    width: 100%;
    margin-left: 0;
    margin-top: 0;
    border-radius: 16px;
  }
}
</style>