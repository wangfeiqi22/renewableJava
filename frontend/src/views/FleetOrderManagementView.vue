<template>
  <div class="fleet-order-container">
    <!-- Header -->
    <header class="page-header">
      <div class="header-left" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">订单管理</div>
      <div class="header-right"></div>
    </header>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="order-tabs">
      <el-tab-pane label="全部订单" name="all" />
      <el-tab-pane label="待指派" name="PENDING" />
      <el-tab-pane label="进行中" name="ASSIGNED" />
      <el-tab-pane label="已完成" name="COMPLETED" />
      <el-tab-pane label="已取消" name="CANCELLED" />
    </el-tabs>

    <!-- Search & Filter Bar -->
    <div class="filter-bar">
      <el-input
        v-model="searchDriverName"
        placeholder="搜索司机姓名"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch" :loading="loading">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
    </div>

    <!-- Stats Summary -->
    <div class="stats-summary">
      <div class="stat-item">
        <span class="stat-label">订单总数</span>
        <span class="stat-value">{{ totalElements || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">待指派</span>
        <span class="stat-value pending">{{ pendingCount || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">进行中</span>
        <span class="stat-value processing">{{ inProgressCount || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">已完成</span>
        <span class="stat-value completed">{{ completedCount || 0 }}</span>
      </div>
    </div>

    <!-- Order List -->
    <div class="order-list" v-loading="loading">
      <div v-if="!loading && orders.length === 0" class="empty-state">
        <el-empty description="暂无订单数据" />
      </div>

      <div
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        @click="handleOrderClick(order)"
      >
        <div class="order-header">
          <div class="order-id">订单号: {{ order.id }}</div>
          <el-tag :type="getStatusType(order.status)" size="small">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>

        <div class="order-content">
          <div class="order-info">
            <div class="info-row">
              <el-icon><Location /></el-icon>
              <span>{{ order.pickupAddress || '未设置' }}</span>
            </div>
            <div class="info-row">
              <el-icon><LocationFilled /></el-icon>
              <span>{{ order.deliveryAddress || '未设置' }}</span>
            </div>
            <div class="info-row" v-if="order.driverName">
              <el-icon><Van /></el-icon>
              <span>{{ order.driverName }} ({{ order.driverPhone }})</span>
            </div>
            <div class="info-row" v-if="order.projectName">
              <el-icon><Folder /></el-icon>
              <span>{{ order.projectName }}</span>
            </div>
          </div>

          <div class="order-meta">
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatDate(order.createdAt) }}
            </div>
            <div class="meta-item" v-if="order.weight">
              <el-icon><Box /></el-icon>
              {{ order.weight }}kg
            </div>
          </div>
        </div>

        <div class="order-footer">
          <div class="order-price">
            <span class="price-label">预估费用</span>
            <span class="price-value">¥ {{ (order.estimatedPrice || 0).toFixed(2) }}</span>
          </div>
          <div class="order-actions">
            <el-button
              v-if="order.status === 'PENDING'"
              type="primary"
              size="small"
              @click.stop="handleAssign(order)"
            >
              指派司机
            </el-button>
            <el-button
              v-if="['ASSIGNED', 'IN_PROGRESS'].includes(order.status)"
              type="warning"
              size="small"
              @click.stop="handleReassign(order)"
            >
              重新指派
            </el-button>
            <el-button
              v-if="order.status === 'PENDING'"
              type="danger"
              size="small"
              plain
              @click.stop="handleCancel(order)"
            >
              取消
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination-wrapper" v-if="totalElements > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalElements"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- Add Order Button -->
    <div class="fab-wrapper">
      <el-button type="primary" circle size="large" class="add-btn" @click="handleAddOrder">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>

    <!-- Order Detail Dialog -->
    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="90%"
    >
      <div class="order-detail" v-if="selectedOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单编号">
            {{ selectedOrder.id }}
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(selectedOrder.status)">
              {{ getStatusText(selectedOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="取货地址">
            {{ selectedOrder.pickupAddress || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="送货地址">
            {{ selectedOrder.deliveryAddress || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="垃圾类型">
            {{ getWasteTypeText(selectedOrder.wasteType) }}
          </el-descriptions-item>
          <el-descriptions-item label="预估重量">
            {{ selectedOrder.weight ? `${selectedOrder.weight} kg` : '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="预估费用">
            ¥ {{ (selectedOrder.estimatedPrice || 0).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="指派司机">
            {{ selectedOrder.driverName || '待指派' }}
            <span v-if="selectedOrder.driverPhone">
              ({{ maskPhone(selectedOrder.driverPhone) }})
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="关联项目">
            {{ selectedOrder.projectName || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(selectedOrder.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(selectedOrder.updatedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注">
            {{ selectedOrder.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="selectedOrder?.status === 'PENDING'"
          type="primary"
          @click="handleAssign(selectedOrder)"
        >
          指派司机
        </el-button>
      </template>
    </el-dialog>

    <!-- Create/Edit Order Dialog -->
    <el-dialog
      v-model="orderDialogVisible"
      :title="isEdit ? '编辑订单' : '创建订单'"
      width="90%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
        class="order-form"
      >
        <el-form-item label="取货地址" prop="pickupAddress">
          <el-input v-model="formData.pickupAddress" placeholder="请输入取货地址" />
        </el-form-item>

        <el-form-item label="送货地址" prop="deliveryAddress">
          <el-input v-model="formData.deliveryAddress" placeholder="请输入送货地址" />
        </el-form-item>

        <el-form-item label="垃圾类型" prop="wasteType">
          <el-select v-model="formData.wasteType" placeholder="请选择垃圾类型" style="width: 100%">
            <el-option label="生活垃圾" value="HOUSEHOLD" />
            <el-option label="建筑垃圾" value="CONSTRUCTION" />
            <el-option label="医疗垃圾" value="MEDICAL" />
            <el-option label="工业垃圾" value="INDUSTRIAL" />
            <el-option label="电子垃圾" value="ELECTRONIC" />
            <el-option label="其他垃圾" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="预估重量" prop="weight">
          <el-input-number
            v-model="formData.weight"
            :min="0"
            :precision="2"
            placeholder="kg"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="预估费用" prop="estimatedPrice">
          <el-input-number
            v-model="formData.estimatedPrice"
            :min="0"
            :precision="2"
            placeholder="元"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="关联项目" prop="projectId">
          <el-select
            v-model="formData.projectId"
            placeholder="请选择项目（可选）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Assign Driver Dialog -->
    <el-dialog
      v-model="assignDialogVisible"
      title="指派司机"
      width="90%"
    >
      <div class="assign-form">
        <p class="assign-tip">请选择要指派的司机</p>

        <el-form-item label="选择司机">
          <el-select
            v-model="selectedDriverId"
            placeholder="请选择司机"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="driver in availableDrivers"
              :key="driver.id"
              :label="`${driver.name} - ${driver.phone} (${driver.vehicleNumber})`"
              :value="driver.id"
            />
          </el-select>
        </el-form-item>

        <div v-if="selectedDriver" class="selected-driver-info">
          <el-card>
            <div class="driver-info">
              <div class="info-item">
                <span class="label">司机:</span>
                <span class="value">{{ selectedDriver.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">电话:</span>
                <span class="value">{{ selectedDriver.phone }}</span>
              </div>
              <div class="info-item">
                <span class="label">车牌:</span>
                <span class="value">{{ selectedDriver.vehicleNumber }}</span>
              </div>
              <div class="info-item">
                <span class="label">状态:</span>
                <el-tag size="small" :type="getDriverStatusType(selectedDriver.status)">
                  {{ getDriverStatusText(selectedDriver.status) }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign" :loading="submitting">
          确认指派
        </el-button>
      </template>
    </el-dialog>

    <!-- Batch Assign Dialog -->
    <el-dialog
      v-model="batchAssignDialogVisible"
      title="批量指派"
      width="90%"
    >
      <div class="batch-assign-form">
        <p class="assign-tip">
          已选择 <strong>{{ selectedOrders.length }}</strong> 个待指派订单
        </p>

        <el-form-item label="选择司机">
          <el-select
            v-model="selectedDriverId"
            placeholder="请选择司机"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="driver in availableDrivers"
              :key="driver.id"
              :label="`${driver.name} - ${driver.phone}`"
              :value="driver.id"
            />
          </el-select>
        </el-form-item>

        <div class="selected-orders-list">
          <div class="list-title">待指派订单:</div>
          <el-tag
            v-for="order in selectedOrders"
            :key="order.id"
            closable
            @close="removeOrder(order.id)"
            style="margin: 4px;"
          >
            订单 #{{ order.id }}
          </el-tag>
        </div>
      </div>

      <template #footer>
        <el-button @click="batchAssignDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmBatchAssign"
          :loading="submitting"
          :disabled="selectedOrders.length === 0 || !selectedDriverId"
        >
          确认批量指派
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Search,
  Plus,
  Location,
  LocationFilled,
  Van,
  Folder,
  Clock,
  Box
} from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()

// State
const loading = ref(false)
const submitting = ref(false)
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const searchDriverName = ref('')
const activeTab = ref('all')

// Stats
const pendingCount = ref(0)
const inProgressCount = ref(0)
const completedCount = ref(0)

// Dialogs
const detailVisible = ref(false)
const orderDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const batchAssignDialogVisible = ref(false)
const isEdit = ref(false)
const selectedOrder = ref(null)
const selectedDriverId = ref(null)
const selectedOrders = ref([])
const availableDrivers = ref([])
const projects = ref([])

// Form
const formRef = ref(null)
const formData = ref({
  pickupAddress: '',
  deliveryAddress: '',
  wasteType: '',
  weight: null,
  estimatedPrice: null,
  projectId: null,
  remark: ''
})

const formRules = {
  pickupAddress: [{ required: true, message: '请输入取货地址', trigger: 'blur' }],
  deliveryAddress: [{ required: true, message: '请输入送货地址', trigger: 'blur' }],
  wasteType: [{ required: true, message: '请选择垃圾类型', trigger: 'change' }]
}

// Lifecycle
onMounted(() => {
  checkAuth()
  loadOrders()
  loadDrivers()
  loadProjects()
})

const checkAuth = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (!user.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!['fleet_owner', 'fleet_dispatcher'].includes(user.role)) {
    ElMessage.warning('该页面仅限车队运营用户访问')
    router.push('/home')
  }
}

// API Calls
const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: 'createdAt',
      direction: 'desc'
    }

    if (activeTab.value !== 'all') {
      params.status = activeTab.value
    }
    if (searchDriverName.value) {
      params.driverName = searchDriverName.value
    }

    const response = await api.get('/fleet/orders', { params })
    if (response.data.code === 200) {
      orders.value = response.data.data.content || []
      totalElements.value = response.data.data.totalElements || 0

      // Calculate stats
      pendingCount.value = orders.value.filter(o => o.status === 'PENDING').length
      inProgressCount.value = orders.value.filter(o =>
        ['ASSIGNED', 'IN_PROGRESS'].includes(o.status)
      ).length
      completedCount.value = orders.value.filter(o =>
        ['COMPLETED', 'CANCELLED'].includes(o.status)
      ).length
    }
  } catch (error) {
    console.error('Failed to load orders:', error)
  } finally {
    loading.value = false
  }
}

const loadDrivers = async () => {
  try {
    const response = await api.get('/fleet/drivers', {
      params: { page: 0, size: 100, status: 'AVAILABLE' }
    })
    if (response.data.code === 200) {
      availableDrivers.value = response.data.data.content || []
    }
  } catch (error) {
    console.error('Failed to load drivers:', error)
  }
}

const loadProjects = async () => {
  try {
    const response = await api.get('/fleet/projects', {
      params: { page: 0, size: 100, status: 'ACTIVE' }
    })
    if (response.data.code === 200) {
      projects.value = response.data.data.content || []
    }
  } catch (error) {
    console.error('Failed to load projects:', error)
  }
}

const createOrder = async () => {
  submitting.value = true
  try {
    const response = await api.post('/fleet/orders', formData.value)
    if (response.data.code === 200) {
      ElMessage.success('创建订单成功')
      orderDialogVisible.value = false
      resetForm()
      loadOrders()
    } else {
      ElMessage.error(response.data.message || '创建失败')
    }
  } catch (error) {
    console.error('Failed to create order:', error)
    ElMessage.error(error.response?.data?.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

const updateOrder = async () => {
  submitting.value = true
  try {
    const response = await api.put(`/fleet/orders/${selectedOrder.value.id}`, formData.value)
    if (response.data.code === 200) {
      ElMessage.success('更新订单成功')
      orderDialogVisible.value = false
      resetForm()
      loadOrders()
    } else {
      ElMessage.error(response.data.message || '更新失败')
    }
  } catch (error) {
    console.error('Failed to update order:', error)
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    submitting.value = false
  }
}

const assignOrder = async () => {
  if (!selectedDriverId.value) {
    ElMessage.warning('请选择司机')
    return
  }

  submitting.value = true
  try {
    const response = await api.post(
      `/fleet/orders/${selectedOrder.value.id}/assign?driverId=${selectedDriverId.value}`
    )
    if (response.data.code === 200) {
      ElMessage.success('指派成功')
      assignDialogVisible.value = false
      selectedDriverId.value = null
      loadOrders()
    } else {
      ElMessage.error(response.data.message || '指派失败')
    }
  } catch (error) {
    console.error('Failed to assign order:', error)
    ElMessage.error(error.response?.data?.message || '指派失败')
  } finally {
    submitting.value = false
  }
}

const batchAssignOrders = async () => {
  if (!selectedDriverId.value || selectedOrders.value.length === 0) {
    ElMessage.warning('请选择司机和订单')
    return
  }

  submitting.value = true
  try {
    const response = await api.post('/fleet/orders/batch-assign', {
      orderIds: selectedOrders.value.map(o => o.id),
      driverId: selectedDriverId.value
    })
    if (response.data.code === 200) {
      ElMessage.success(`成功指派 ${selectedOrders.value.length} 个订单`)
      batchAssignDialogVisible.value = false
      selectedOrders.value = []
      selectedDriverId.value = null
      loadOrders()
    } else {
      ElMessage.error(response.data.message || '批量指派失败')
    }
  } catch (error) {
    console.error('Failed to batch assign orders:', error)
    ElMessage.error(error.response?.data?.message || '批量指派失败')
  } finally {
    submitting.value = false
  }
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消订单 #${order.id} 吗？`,
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await api.put(`/fleet/orders/${order.id}/status?status=CANCELLED`)
    if (response.data.code === 200) {
      ElMessage.success('订单已取消')
      loadOrders()
    } else {
      ElMessage.error(response.data.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to cancel order:', error)
      ElMessage.error('取消失败')
    }
  }
}

// Event Handlers
const handleTabChange = () => {
  currentPage.value = 1
  loadOrders()
}

const handleSearch = () => {
  currentPage.value = 1
  loadOrders()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadOrders()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadOrders()
}

const handleOrderClick = (order) => {
  selectedOrder.value = order
  detailVisible.value = true
}

const handleAddOrder = () => {
  isEdit.value = false
  resetForm()
  orderDialogVisible.value = true
}

const handleEdit = (order) => {
  isEdit.value = true
  selectedOrder.value = order
  formData.value = {
    pickupAddress: order.pickupAddress || '',
    deliveryAddress: order.deliveryAddress || '',
    wasteType: order.wasteType || '',
    weight: order.weight || null,
    estimatedPrice: order.estimatedPrice || null,
    projectId: order.projectId || null,
    remark: order.remark || ''
  }
  orderDialogVisible.value = true
}

const handleAssign = (order) => {
  selectedOrder.value = order
  selectedDriverId.value = null
  detailVisible.value = false
  assignDialogVisible.value = true
}

const handleReassign = (order) => {
  selectedOrder.value = order
  selectedDriverId.value = null
  assignDialogVisible.value = true
}

const handleCancel = (order) => {
  cancelOrder(order)
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await updateOrder()
    } else {
      await createOrder()
    }
  } catch (error) {
    console.error('Validation failed:', error)
  }
}

const confirmAssign = () => {
  assignOrder()
}

const confirmBatchAssign = () => {
  batchAssignOrders()
}

const removeOrder = (orderId) => {
  selectedOrders.value = selectedOrders.value.filter(o => o.id !== orderId)
}

// Helpers
const resetForm = () => {
  formData.value = {
    pickupAddress: '',
    deliveryAddress: '',
    wasteType: '',
    weight: null,
    estimatedPrice: null,
    projectId: null,
    remark: ''
  }
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'ASSIGNED': 'primary',
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待指派',
    'ASSIGNED': '已指派',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const getWasteTypeText = (type) => {
  const map = {
    'HOUSEHOLD': '生活垃圾',
    'CONSTRUCTION': '建筑垃圾',
    'MEDICAL': '医疗垃圾',
    'INDUSTRIAL': '工业垃圾',
    'ELECTRONIC': '电子垃圾',
    'OTHER': '其他垃圾'
  }
  return map[type] || type || '未填写'
}

const getDriverStatusType = (status) => {
  const map = {
    'AVAILABLE': 'success',
    'BUSY': 'warning',
    'ON_LEAVE': 'info'
  }
  return map[status] || 'info'
}

const getDriverStatusText = (status) => {
  const map = {
    'AVAILABLE': '可用',
    'BUSY': '忙碌',
    'ON_LEAVE': '请假'
  }
  return map[status] || status
}

const selectedDriver = computed(() => {
  if (!selectedDriverId.value) return null
  return availableDrivers.value.find(d => d.id === selectedDriverId.value)
})
</script>

<style scoped>
.fleet-order-container {
  min-height: 100vh;
  background-color: #f5f7f9;
  padding-bottom: 100px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 56px;
  background-color: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-left,
.header-right {
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

.order-tabs {
  background: #fff;
  padding: 0 16px;
}

.filter-bar {
  padding: 12px 16px;
  background: #fff;
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-input {
  flex: 1;
}

.stats-summary {
  display: flex;
  padding: 16px;
  gap: 12px;
  background: #fff;
  margin-bottom: 12px;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 12px 8px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.stat-value.pending {
  color: #e6a23c;
}

.stat-value.processing {
  color: #409eff;
}

.stat-value.completed {
  color: #67c23a;
}

.order-list {
  padding: 0 16px;
}

.empty-state {
  padding: 40px 0;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-id {
  font-size: 14px;
  color: #666;
  font-family: monospace;
}

.order-content {
  margin-bottom: 12px;
}

.order-info {
  margin-bottom: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
  margin-bottom: 6px;
}

.info-row .el-icon {
  color: #409eff;
  font-size: 16px;
}

.order-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.order-price {
  display: flex;
  flex-direction: column;
}

.price-label {
  font-size: 12px;
  color: #999;
}

.price-value {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  padding: 16px;
  display: flex;
  justify-content: center;
  background: #fff;
}

.fab-wrapper {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 100;
}

.add-btn {
  width: 56px;
  height: 56px;
  font-size: 28px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.order-detail {
  padding: 16px 0;
}

.order-form {
  padding: 16px 0;
}

.assign-form,
.batch-assign-form {
  padding: 16px 0;
}

.assign-tip {
  margin-bottom: 16px;
  color: #666;
}

.selected-driver-info {
  margin-top: 16px;
}

.driver-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  gap: 8px;
  font-size: 14px;
}

.info-item .label {
  color: #999;
}

.info-item .value {
  color: #333;
}

.selected-orders-list {
  margin-top: 16px;
}

.list-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

@media (min-width: 768px) {
  .fleet-order-container {
    max-width: 480px;
    margin: 0 auto;
  }
}
</style>
