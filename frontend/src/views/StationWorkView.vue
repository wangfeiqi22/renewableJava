<template>
  <div class="station-work-container">
    <el-header class="header">
      <div class="header-left">
        <el-icon class="logo-icon"><Van /></el-icon>
        <span class="title">清运站工作台</span>
      </div>
      <div class="header-right">
        <span class="user-info" v-if="user">{{ user.username }}</span>
        <el-button type="danger" link @click="handleLogout">退出</el-button>
      </div>
    </el-header>

    <el-main>
      <el-row :gutter="20">
        <!-- Incoming Vehicles Column -->
        <el-col :span="12">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <span>车辆入场 / 待处理</span>
                <el-button type="primary" link icon="Refresh" @click="fetchIncoming">刷新</el-button>
              </div>
            </template>
            <div v-if="incomingOrders.length === 0" class="empty-text">暂无车辆到达</div>
            <div v-for="order in incomingOrders" :key="order.id" class="order-item">
              <div class="order-info" @click="openDetail(order)">
                <div class="plate-no">订单 #{{ order.orderNo.substring(0,8) }}</div>
                <div class="sub-info">{{ getWasteTypeName(order.wasteType) }} | 预估 {{ order.estWeight }}kg</div>
              </div>
              <div class="order-actions">
                <el-button 
                  v-if="order.status === 40" 
                  type="primary" 
                  size="small" 
                  @click="confirmArrival(order.id)"
                >
                  确认到站
                </el-button>
                <el-button 
                  v-if="order.status === 50" 
                  type="success" 
                  size="small" 
                  @click="openWeighIn(order)"
                >
                  称重入库
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- History Column -->
        <el-col :span="12">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <span>今日已完成</span>
              </div>
            </template>
            <el-table
              :data="historyOrders"
              style="width: 100%"
              size="small"
              highlight-current-row
              @row-click="openDetail"
            >
              <el-table-column prop="orderNo" label="订单号" width="120" show-overflow-tooltip />
              <el-table-column label="类型" width="80">
                <template #default="scope">
                  {{ getWasteTypeName(scope.row.wasteType) }}
                </template>
              </el-table-column>
              <el-table-column prop="updatedAt" label="完成时间" />
              <el-table-column label="状态">
                <template #default>
                  <el-tag type="success" size="small">已入库</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <!-- Weigh In Dialog -->
      <el-dialog v-model="weighDialogVisible" title="车辆称重" width="400px">
        <el-form :model="weighForm">
          <el-form-item label="实际重量 (kg)">
            <el-input-number v-model="weighForm.weight" :precision="2" :step="10" style="width: 100%" />
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
    </el-main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Van, Refresh } from '@element-plus/icons-vue'
import OrderTimeline from '../components/OrderTimeline.vue'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const incomingOrders = ref([])
const historyOrders = ref([])
const weighDialogVisible = ref(false)
const currentOrder = ref(null)
const weighForm = reactive({ weight: 0 })
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentLogs = ref([])
const currentPhotos = ref([])

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
  try {
    const res = await api.get('/station/ops/incoming')
    incomingOrders.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const fetchHistory = async () => {
  try {
    const res = await api.get('/station/ops/history')
    historyOrders.value = res.data
  } catch (error) {
    console.error(error)
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
})
</script>

<style scoped>
.station-work-container {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.header {
  background: white;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #409EFF;
}

.logo-icon {
  font-size: 24px;
}

.title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.order-item {
  background: #f9fafc;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  cursor: pointer;
}

.plate-no {
  font-weight: bold;
  font-size: 16px;
  color: #333;
}

.sub-info {
  font-size: 13px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 20px;
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
</style>