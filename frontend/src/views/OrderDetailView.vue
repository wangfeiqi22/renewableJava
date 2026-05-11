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
            <span class="order-no">订单号: {{ order.orderNo }}</span>
            <el-button class="manifest-btn" size="small" round>
              <el-icon><Document /></el-icon> 查看联单
            </el-button>
          </div>
        </div>
        
        <div class="progress-section" v-if="order.status >= 30 && order.status < 90">
          <div class="progress-header">
            <span class="progress-title">清运进度</span>
            <span class="progress-value">{{ order.estVolume ? (order.estVolume / 2).toFixed(1) : 0 }}m³ / {{ order.estVolume || 0 }}m³</span>
          </div>
          <div class="progress-bar-bg">
            <div class="progress-bar-fill" :style="{ width: '50%' }"></div>
          </div>
          <div class="progress-footer">
            <span class="progress-detail">已清运 1 车</span>
            <span class="progress-detail">剩余 {{ order.estVolume ? (order.estVolume / 2).toFixed(1) : 0 }}m³</span>
          </div>
        </div>
      </div>

      <!-- Shipper Card -->
      <div class="card info-card">
        <div class="info-row">
          <div class="icon-wrap red-bg">
            <el-icon><Location /></el-icon>
          </div>
          <div class="info-main">
            <div class="info-title-wrap">
              <span class="info-title">{{ order.pickupAddress ? order.pickupAddress.split('市')[0] + '项目' : '环保清运项目' }}</span>
              <el-tag size="small" type="info" effect="plain" class="role-tag">排放单位</el-tag>
            </div>
            <div class="info-sub">{{ order.pickupAddress }}</div>
          </div>
        </div>
        <div class="contact-row">
          <div class="contact-item">
            <el-icon><User /></el-icon> {{ getShipperName() }}
          </div>
          <div class="contact-item">
            <el-icon><Phone /></el-icon> {{ getShipperPhone() }}
          </div>
        </div>
        <div class="tags-row">
          <el-tag type="info" class="waste-tag">{{ getWasteTypeName(order.wasteType) }}</el-tag>
          <span class="volume-text">共 {{ order.estVolume || 0 }}m³</span>
        </div>
      </div>

      <!-- Fleet Card -->
      <div class="card info-card" v-if="order.driverId">
        <div class="info-row">
          <div class="icon-wrap blue-bg">
            <el-icon><Van /></el-icon>
          </div>
          <div class="info-main">
            <div class="info-title-wrap">
              <span class="info-title">城投环境物流车队</span>
              <el-tag size="small" type="info" effect="plain" class="role-tag">运输单位</el-tag>
            </div>
            <div class="info-sub">王师傅 · 沪A-88888</div>
          </div>
        </div>
        <div class="contact-row">
          <div class="contact-item">
            <el-icon><Phone /></el-icon> 139****5678
          </div>
        </div>
      </div>

      <!-- Station Card -->
      <div class="card info-card" v-if="order.stationId">
        <div class="info-row">
          <div class="icon-wrap orange-bg">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="info-main">
            <div class="info-title-wrap">
              <span class="info-title">浦东第一再生资源场</span>
              <el-tag size="small" type="info" effect="plain" class="role-tag">处置单位</el-tag>
            </div>
            <div class="info-sub">浦东新区川沙路88号</div>
          </div>
        </div>
      </div>

      <!-- Timeline Card -->
      <div class="card timeline-card">
        <div class="card-header-flex">
          <h3>清运流程</h3>
          <span class="expand-text">展开 <el-icon><ArrowDown /></el-icon></span>
        </div>
        <div class="timeline-container">
          <div class="timeline-item" v-for="(log, index) in orderLogs" :key="index">
            <div class="timeline-line" :class="{'animated-line': index === 0}" v-if="index !== orderLogs.length - 1"></div>
            <div class="timeline-icon" :class="{ 'is-active': index === 0, 'is-exception': log.newStatus === 90 }">
              <el-icon v-if="log.newStatus === 90"><Close /></el-icon>
              <el-icon v-else><Select /></el-icon>
            </div>
            <div class="timeline-content" @click="handleNodeClick(log)">
              <div class="timeline-title-row">
                <div class="timeline-title" :class="{ 'is-active': index === 0, 'is-exception-text': log.newStatus === 90 }">{{ getLogStatusName(log.newStatus) }}</div>
                <div class="timeline-time">{{ formatDate(log.createdAt) }}</div>
              </div>
              <div class="timeline-operator" v-if="log.operatorName || log.operatorId">
                <el-icon><User /></el-icon> 操作人: {{ log.operatorName || '清运人员/系统' }}
              </div>
              <div class="timeline-remark" v-if="log.remark && log.newStatus === 90">
                异常原因: {{ log.remark }}
              </div>
            </div>
          </div>
          
          <!-- Mock upcoming steps if not finished -->
          <div class="timeline-item pending" v-if="order.status < 60 && order.status !== 90">
            <div class="timeline-line" style="background-color: #eee;"></div>
            <div class="timeline-icon empty"></div>
            <div class="timeline-content">
              <div class="timeline-title-row">
                <div class="timeline-title pending-text">清运完成确认</div>
              </div>
            </div>
          </div>
          <div class="timeline-item pending" v-if="order.status < 60 && order.status !== 90">
            <div class="timeline-icon empty"></div>
            <div class="timeline-content">
              <div class="timeline-title-row">
                <div class="timeline-title pending-text">订单结算</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Node Detail Dialog -->
      <el-dialog v-model="nodeDialogVisible" title="节点详细信息" width="90%" style="max-width: 400px; border-radius: 12px;">
        <div v-if="selectedNode" class="node-detail-body">
          <div class="detail-item"><span class="label">节点状态：</span> <el-tag :type="selectedNode.newStatus === 90 ? 'danger' : 'success'" size="small">{{ getLogStatusName(selectedNode.newStatus) }}</el-tag></div>
          <div class="detail-item"><span class="label">操作时间：</span> {{ formatDate(selectedNode.createdAt) }}</div>
          <div class="detail-item"><span class="label">操作人员：</span> {{ selectedNode.operatorName || '系统自动处理' }}</div>
          <div class="detail-item"><span class="label">操作备注：</span> {{ selectedNode.remark || '无' }}</div>
          <div class="detail-item exception-box" v-if="selectedNode.newStatus === 90">
            <span class="label exception-label">异常判定：</span> 
            <span class="exception-text">{{ selectedNode.remark }}</span>
          </div>
        </div>
      </el-dialog>

      <!-- Requirements Section -->
      <div class="requirements-section">
        <div class="req-title">作业要求</div>
        <div class="req-tags">
          <div class="req-tag"><el-icon><Location /></el-icon> 围栏内装车</div>
          <div class="req-tag"><el-icon><Location /></el-icon> 围栏内卸车</div>
          <div class="req-tag"><el-icon><Camera /></el-icon> 需装车照片</div>
          <div class="req-tag"><el-icon><Camera /></el-icon> 需卸车照片</div>
        </div>
      </div>
    </div>

    <!-- Floating Action Button -->
    <div class="fab-btn" v-if="!loading && !error && order">
      <el-icon><Box /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'
import { ArrowLeft, Document, Location, User, Phone, Van, OfficeBuilding, ArrowDown, Select, Camera, Box, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id

const order = ref(null)
const orderLogs = ref([])
const loading = ref(true)
const error = ref(false)

// Node Detail Interaction
const nodeDialogVisible = ref(false)
const selectedNode = ref(null)

const handleNodeClick = (log) => {
  selectedNode.value = log
  nodeDialogVisible.value = true
}

const handleBack = () => {
  router.back()
}

const fetchOrderDetail = async () => {
  loading.value = true
  error.value = false
  try {
    const res = await api.get(`/orders/${orderId}`)
    order.value = res.data

    // Fetch logs
    try {
      const logRes = await api.get(`/orders/${orderId}/logs`)
      orderLogs.value = logRes.data || []
      
      // If no logs, mock creation log
      if (orderLogs.value.length === 0) {
        orderLogs.value = [{
          newStatus: 10,
          createdAt: order.value.createdAt
        }]
      }
    } catch (e) {
      console.warn('Failed to fetch logs, using mock')
      orderLogs.value = [{
        newStatus: 10,
        createdAt: order.value.createdAt
      }]
    }

  } catch (err) {
    console.error('Failed to fetch order detail:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (orderId) {
    fetchOrderDetail()
  } else {
    error.value = true
    loading.value = false
  }
})

const getStatusText = (status) => {
  const map = {
    10: '待平台确认',
    20: '派单成功',
    25: '前往取货点',
    30: '装载中',
    40: '运输中',
    50: '到达站点',
    60: '已完成',
    90: '已取消'
  }
  return map[status] || '未知状态'
}

const getLogStatusName = (status) => {
  const map = {
    10: '订单创建',
    20: '派单成功',
    25: '清运人员接单',
    30: '开始清运作业',
    40: '到达清运地点',
    50: '到达处置站点',
    60: '清运完成确认',
    70: '订单结算',
    90: '异常/已取消'
  }
  return map[status] || '状态流转'
}

const getStatusClass = (status) => {
  if (status === 10) return 'text-warning'
  if (status >= 20 && status < 60) return 'text-primary'
  if (status === 60) return 'text-success'
  if (status === 90) return 'text-info'
  return 'text-primary'
}

const getWasteTypeName = (type) => {
  const map = {
    'household': '生活垃圾',
    'construction': '装修垃圾',
    'bulky': '大件垃圾',
    'kitchen': '厨余垃圾'
  }
  return map[type] || '装修垃圾'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:${String(d.getSeconds()).padStart(2,'0')}`
}

const getShipperName = () => {
  if (order.value && order.value.wasteDesc && order.value.wasteDesc.includes('托运人:')) {
    const match = order.value.wasteDesc.match(/托运人:\s*([^\(]+)/)
    if (match) return match[1].trim()
  }
  return '张先生'
}

const getShipperPhone = () => {
  if (order.value && order.value.wasteDesc && order.value.wasteDesc.includes('托运人:')) {
    const match = order.value.wasteDesc.match(/\(([\d]+)\)/)
    if (match) {
      const p = match[1]
      if (p.length === 11) return p.substring(0,3) + '****' + p.substring(7)
      return p
    }
  }
  return '138****1234'
}
</script>

<style scoped>
.order-detail-container {
  min-height: 100vh;
  background-color: #f5f7f9;
  padding-bottom: 80px;
  position: relative;
}

.eco-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 56px;
  background-color: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  font-size: 20px;
  color: #333;
  cursor: pointer;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.header-right {
  width: 40px;
}

.detail-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.02);
}

/* Status Card */
.status-card {
  background: #fffaf0;
  border: 1px solid #fff0e0;
}

.status-header {
  margin-bottom: 16px;
}

.status-text {
  font-size: 18px;
  font-weight: bold;
  display: block;
  margin-bottom: 8px;
}

.text-warning { color: #ff9800; }
.text-primary { color: #ff6b00; } /* Reference uses orange-ish for "前往取货点" */
.text-success { color: #0dc8a3; }
.text-info { color: #999; }

.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-no {
  font-size: 12px;
  color: #666;
}

.manifest-btn {
  color: #333;
  border-color: #e0e0e0;
}

.progress-section {
  margin-top: 16px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #333;
  margin-bottom: 8px;
}

.progress-bar-bg {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-bar-fill {
  height: 100%;
  background: #0dc8a3;
  border-radius: 4px;
}

.progress-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

/* Info Cards */
.info-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.red-bg { background: #ffebee; color: #f44336; }
.blue-bg { background: #e3f2fd; color: #2196f3; }
.orange-bg { background: #fff3e0; color: #ff9800; }

.info-main {
  flex: 1;
}

.info-title-wrap {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 4px;
}

.info-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.role-tag {
  border-radius: 4px;
}

.info-sub {
  font-size: 12px;
  color: #999;
  line-height: 1.4;
}

.contact-row {
  display: flex;
  gap: 24px;
  padding-left: 44px;
  margin-bottom: 12px;
}

.contact-item {
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tags-row {
  padding-left: 44px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.waste-tag {
  border-radius: 12px;
}

.volume-text {
  font-size: 12px;
  color: #999;
}

/* Timeline */
.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header-flex h3 {
  font-size: 14px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.expand-text {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 2px;
}

.timeline-container {
  padding: 8px 0;
}

.timeline-item {
  display: flex;
  position: relative;
  min-height: 48px;
}

.timeline-line {
  position: absolute;
  left: 9px;
  top: 20px;
  bottom: -4px;
  width: 2px;
  background-color: #bdf2e8;
  transform-origin: top;
}

.animated-line {
  animation: scaleYAnim 0.6s ease-out forwards;
}

@keyframes scaleYAnim {
  from { transform: scaleY(0); }
  to { transform: scaleY(1); }
}

.timeline-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #bdf2e8;
  color: #0dc8a3;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  z-index: 2;
  margin-right: 12px;
}

.timeline-icon.is-active {
  background-color: #0dc8a3;
  color: #fff;
  box-shadow: 0 0 0 3px #bdf2e8;
}

.timeline-icon.is-exception {
  background-color: #f56c6c;
  color: #fff;
  box-shadow: 0 0 0 3px #fde2e2;
}

.timeline-icon.empty {
  background-color: #fff;
  border: 2px solid #ddd;
}

.timeline-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding-bottom: 24px;
  cursor: pointer;
  transition: background 0.2s;
  padding: 8px;
  border-radius: 8px;
  margin-top: -8px;
}

.timeline-content:hover {
  background: #f9f9f9;
}

.timeline-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.timeline-title {
  font-size: 14px;
  color: #333;
}

.timeline-title.is-active {
  font-weight: 600;
}

.timeline-title.is-exception-text {
  color: #f56c6c;
}

.timeline-title.pending-text {
  color: #999;
}

.timeline-time {
  font-size: 12px;
  color: #999;
}

.timeline-operator {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.timeline-remark {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 4px;
  background: #fef0f0;
  padding: 4px 8px;
  border-radius: 4px;
}

/* Node Detail Dialog */
.node-detail-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.detail-item .label {
  color: #999;
  display: inline-block;
  width: 70px;
}

.exception-box {
  background: #fef0f0;
  padding: 12px;
  border-radius: 8px;
}

.exception-label {
  color: #f56c6c !important;
}

.exception-text {
  color: #f56c6c;
  font-weight: 500;
}

/* Requirements */
.requirements-section {
  padding: 8px;
}

.req-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.req-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.req-tag {
  background: #fff;
  border: 1px solid #eee;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* FAB */
.fab-btn {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 56px;
  height: 56px;
  background-color: #0dc8a3;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(13, 200, 163, 0.4);
  cursor: pointer;
  transition: transform 0.2s;
  z-index: 90;
}

.fab-btn:active {
  transform: scale(0.95);
}
</style>
