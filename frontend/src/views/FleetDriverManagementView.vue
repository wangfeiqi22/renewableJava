<template>
  <div class="fleet-driver-container">
    <!-- Header -->
    <header class="page-header">
      <div class="header-left" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">司机管理</div>
      <div class="header-right"></div>
    </header>

    <!-- Search & Filter Bar -->
    <div class="filter-bar">
      <el-input
        v-model="searchName"
        placeholder="搜索司机姓名"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable class="status-select" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="可用" value="AVAILABLE" />
        <el-option label="忙碌" value="BUSY" />
        <el-option label="请假" value="ON_LEAVE" />
        <el-option label="离职" value="RESIGNED" />
      </el-select>
      <el-button type="primary" @click="handleSearch" :loading="loading">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
    </div>

    <!-- Stats Summary -->
    <div class="stats-summary">
      <div class="stat-item">
        <span class="stat-label">司机总数</span>
        <span class="stat-value">{{ totalElements || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">可用司机</span>
        <span class="stat-value available">{{ availableCount || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">忙碌司机</span>
        <span class="stat-value busy">{{ busyCount || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">请假司机</span>
        <span class="stat-value leave">{{ onLeaveCount || 0 }}</span>
      </div>
    </div>

    <!-- Driver List -->
    <div class="driver-list" v-loading="loading">
      <div v-if="!loading && drivers.length === 0" class="empty-state">
        <el-empty description="暂无司机数据" />
      </div>

      <div
        v-for="driver in drivers"
        :key="driver.id"
        class="driver-card"
        @click="handleDriverClick(driver)"
      >
        <div class="driver-avatar">
          <el-avatar :size="48" :src="driver.avatarUrl">
            {{ driver.name?.charAt(0) || '司' }}
          </el-avatar>
          <span class="status-dot" :class="getStatusClass(driver.status)"></span>
        </div>

        <div class="driver-info">
          <div class="driver-name">{{ driver.name }}</div>
          <div class="driver-phone">{{ maskPhone(driver.phone) }}</div>
          <div class="driver-meta">
            <span class="meta-item">
              <el-icon><Van /></el-icon>
              {{ driver.vehicleNumber || '未绑定车辆' }}
            </span>
            <span class="meta-item">
              <el-icon><Document /></el-icon>
              {{ driver.completedOrders || 0 }} 单
            </span>
          </div>
        </div>

        <div class="driver-actions">
          <el-tag :type="getStatusType(driver.status)" size="small">
            {{ getStatusText(driver.status) }}
          </el-tag>
          <el-dropdown trigger="click" @command="handleCommand($event, driver)" @click.stop>
            <el-button type="primary" link>
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">
                  <el-icon><Edit /></el-icon> 编辑
                </el-dropdown-item>
                <el-dropdown-item command="reset">
                  <el-icon><Refresh /></el-icon> 重置密码
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="driver.status !== 'RESIGNED'"
                  command="resign"
                >
                  <el-icon><SwitchButton /></el-icon> 离职
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="driver.status === 'AVAILABLE'"
                  command="leave"
                >
                  <el-icon><Calendar /></el-icon> 请假
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="driver.status === 'ON_LEAVE'"
                  command="activate"
                >
                  <el-icon><CircleCheck /></el-icon> 激活
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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

    <!-- Add Driver Button -->
    <div class="fab-wrapper">
      <el-button type="primary" circle size="large" class="add-btn" @click="handleAddDriver">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>

    <!-- Add/Edit Driver Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑司机' : '添加司机'"
      width="90%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
        class="driver-form"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入司机姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入手机号"
            :disabled="isEdit"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="车牌号" prop="vehicleNumber">
          <el-input v-model="formData.vehicleNumber" placeholder="请输入车牌号，如: 京A12345" />
        </el-form-item>

        <el-form-item label="车型" prop="vehicleType">
          <el-select v-model="formData.vehicleType" placeholder="请选择车型" style="width: 100%">
            <el-option label="小型货车" value="SMALL_TRUCK" />
            <el-option label="中型货车" value="MEDIUM_TRUCK" />
            <el-option label="大型货车" value="LARGE_TRUCK" />
            <el-option label="电动三轮车" value="ELECTRIC_TRICYCLE" />
          </el-select>
        </el-form-item>

        <el-form-item label="驾照类型" prop="licenseType">
          <el-select v-model="formData.licenseType" placeholder="请选择驾照类型" style="width: 100%">
            <el-option label="C1" value="C1" />
            <el-option label="C2" value="C2" />
            <el-option label="B1" value="B1" />
            <el-option label="B2" value="B2" />
            <el-option label="A1" value="A1" />
            <el-option label="A2" value="A2" />
          </el-select>
        </el-form-item>

        <el-form-item label="紧急联系" prop="emergencyContact">
          <el-input v-model="formData.emergencyContact" placeholder="请输入紧急联系人电话" />
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Driver Detail Dialog -->
    <el-dialog
      v-model="detailVisible"
      title="司机详情"
      width="90%"
    >
      <div class="driver-detail" v-if="selectedDriver">
        <div class="detail-header">
          <el-avatar :size="64" :src="selectedDriver.avatarUrl">
            {{ selectedDriver.name?.charAt(0) || '司' }}
          </el-avatar>
          <div class="detail-info">
            <div class="detail-name">{{ selectedDriver.name }}</div>
            <el-tag :type="getStatusType(selectedDriver.status)" size="small">
              {{ getStatusText(selectedDriver.status) }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="1" border class="detail-descriptions">
          <el-descriptions-item label="手机号">
            {{ selectedDriver.phone }}
          </el-descriptions-item>
          <el-descriptions-item label="车牌号">
            {{ selectedDriver.vehicleNumber || '未绑定' }}
          </el-descriptions-item>
          <el-descriptions-item label="车型">
            {{ getVehicleTypeText(selectedDriver.vehicleType) }}
          </el-descriptions-item>
          <el-descriptions-item label="驾照类型">
            {{ selectedDriver.licenseType || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="紧急联系人">
            {{ selectedDriver.emergencyContact || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="完成订单数">
            {{ selectedDriver.completedOrders || 0 }} 单
          </el-descriptions-item>
          <el-descriptions-item label="累计收入">
            ¥ {{ (selectedDriver.totalIncome || 0).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="加入时间">
            {{ formatDate(selectedDriver.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注">
            {{ selectedDriver.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEditFromDetail">
          编辑信息
        </el-button>
      </template>
    </el-dialog>

    <!-- Change Status Dialog -->
    <el-dialog
      v-model="statusDialogVisible"
      title="修改司机状态"
      width="90%"
    >
      <div class="status-change-form">
        <p>司机：{{ statusChangeDriver?.name }}</p>
        <p>当前状态：{{ getStatusText(statusChangeDriver?.status) }}</p>
        <el-form-item label="新状态" style="margin-top: 16px">
          <el-select v-model="newStatus" placeholder="选择新状态" style="width: 100%">
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="忙碌" value="BUSY" />
            <el-option label="请假" value="ON_LEAVE" />
            <el-option label="离职" value="RESIGNED" />
          </el-select>
        </el-form-item>
      </div>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStatusChange" :loading="submitting">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Search,
  Plus,
  Van,
  Document,
  MoreFilled,
  Edit,
  Refresh,
  Calendar,
  CircleCheck,
  SwitchButton
} from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()

// State
const loading = ref(false)
const submitting = ref(false)
const drivers = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const searchName = ref('')
const filterStatus = ref('')

// Stats
const availableCount = ref(0)
const busyCount = ref(0)
const onLeaveCount = ref(0)

// Dialogs
const dialogVisible = ref(false)
const detailVisible = ref(false)
const statusDialogVisible = ref(false)
const isEdit = ref(false)
const selectedDriver = ref(null)
const statusChangeDriver = ref(null)
const newStatus = ref('')

// Form
const formRef = ref(null)
const formData = ref({
  name: '',
  phone: '',
  vehicleNumber: '',
  vehicleType: '',
  licenseType: '',
  emergencyContact: '',
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入司机姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  vehicleNumber: [{ required: true, message: '请输入车牌号', trigger: 'blur' }]
}

// Lifecycle
onMounted(() => {
  checkAuth()
  loadDrivers()
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
const loadDrivers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: 'createdAt',
      direction: 'desc'
    }
    if (searchName.value) {
      params.name = searchName.value
    }
    if (filterStatus.value) {
      params.status = filterStatus.value
    }

    const response = await api.get('/fleet/drivers', { params })
    if (response.data.code === 200) {
      drivers.value = response.data.data.content || []
      totalElements.value = response.data.data.totalElements || 0

      // Calculate stats
      availableCount.value = drivers.value.filter(d => d.status === 'AVAILABLE').length
      busyCount.value = drivers.value.filter(d => d.status === 'BUSY').length
      onLeaveCount.value = drivers.value.filter(d => d.status === 'ON_LEAVE').length
    }
  } catch (error) {
    console.error('Failed to load drivers:', error)
  } finally {
    loading.value = false
  }
}

const createDriver = async () => {
  submitting.value = true
  try {
    const response = await api.post('/fleet/drivers', formData.value)
    if (response.data.code === 200) {
      ElMessage.success('添加司机成功')
      dialogVisible.value = false
      resetForm()
      loadDrivers()
    } else {
      ElMessage.error(response.data.message || '添加失败')
    }
  } catch (error) {
    console.error('Failed to create driver:', error)
    ElMessage.error(error.response?.data?.message || '添加失败')
  } finally {
    submitting.value = false
  }
}

const updateDriver = async () => {
  submitting.value = true
  try {
    const response = await api.put(`/fleet/drivers/${selectedDriver.value.id}`, formData.value)
    if (response.data.code === 200) {
      ElMessage.success('更新司机信息成功')
      dialogVisible.value = false
      resetForm()
      loadDrivers()
    } else {
      ElMessage.error(response.data.message || '更新失败')
    }
  } catch (error) {
    console.error('Failed to update driver:', error)
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    submitting.value = false
  }
}

const deleteDriver = async (driver) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除司机 ${driver.name} 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await api.delete(`/fleet/drivers/${driver.id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadDrivers()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete driver:', error)
      ElMessage.error('删除失败')
    }
  }
}

const resetDriverPassword = async (driver) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置司机 ${driver.name} 的密码吗？`,
      '重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await api.post(`/fleet/drivers/${driver.id}/reset-password`)
    if (response.data.code === 200) {
      ElMessage.success('密码已重置为: 123456')
    } else {
      ElMessage.error(response.data.message || '重置失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to reset password:', error)
      ElMessage.error('重置失败')
    }
  }
}

const changeDriverStatus = async () => {
  if (!newStatus.value) {
    ElMessage.warning('请选择新状态')
    return
  }

  submitting.value = true
  try {
    const response = await api.put(`/fleet/drivers/${statusChangeDriver.value.id}`, {
      status: newStatus.value
    })
    if (response.data.code === 200) {
      ElMessage.success('状态修改成功')
      statusDialogVisible.value = false
      loadDrivers()
    } else {
      ElMessage.error(response.data.message || '修改失败')
    }
  } catch (error) {
    console.error('Failed to change status:', error)
    ElMessage.error('修改失败')
  } finally {
    submitting.value = false
  }
}

// Event Handlers
const handleSearch = () => {
  currentPage.value = 1
  loadDrivers()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadDrivers()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadDrivers()
}

const handleAddDriver = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleDriverClick = (driver) => {
  selectedDriver.value = driver
  detailVisible.value = true
}

const handleEditFromDetail = () => {
  detailVisible.value = false
  handleEdit(selectedDriver.value)
}

const handleEdit = (driver) => {
  isEdit.value = true
  selectedDriver.value = driver
  formData.value = {
    name: driver.name,
    phone: driver.phone,
    vehicleNumber: driver.vehicleNumber || '',
    vehicleType: driver.vehicleType || '',
    licenseType: driver.licenseType || '',
    emergencyContact: driver.emergencyContact || '',
    remark: driver.remark || ''
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await updateDriver()
    } else {
      await createDriver()
    }
  } catch (error) {
    console.error('Validation failed:', error)
  }
}

const handleCommand = (command, driver) => {
  switch (command) {
    case 'edit':
      handleEdit(driver)
      break
    case 'reset':
      resetDriverPassword(driver)
      break
    case 'resign':
      statusChangeDriver.value = driver
      newStatus.value = 'RESIGNED'
      statusDialogVisible.value = true
      break
    case 'leave':
      statusChangeDriver.value = driver
      newStatus.value = 'ON_LEAVE'
      statusDialogVisible.value = true
      break
    case 'activate':
      statusChangeDriver.value = driver
      newStatus.value = 'AVAILABLE'
      statusDialogVisible.value = true
      break
  }
}

const handleStatusChange = () => {
  changeDriverStatus()
}

// Helpers
const resetForm = () => {
  formData.value = {
    name: '',
    phone: '',
    vehicleNumber: '',
    vehicleType: '',
    licenseType: '',
    emergencyContact: '',
    remark: ''
  }
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const maskPhone = (phone) => {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getStatusClass = (status) => {
  const map = {
    'AVAILABLE': 'available',
    'BUSY': 'busy',
    'ON_LEAVE': 'leave',
    'RESIGNED': 'resigned'
  }
  return map[status] || ''
}

const getStatusType = (status) => {
  const map = {
    'AVAILABLE': 'success',
    'BUSY': 'warning',
    'ON_LEAVE': 'info',
    'RESIGNED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'AVAILABLE': '可用',
    'BUSY': '忙碌',
    'ON_LEAVE': '请假',
    'RESIGNED': '已离职'
  }
  return map[status] || '未知'
}

const getVehicleTypeText = (type) => {
  const map = {
    'SMALL_TRUCK': '小型货车',
    'MEDIUM_TRUCK': '中型货车',
    'LARGE_TRUCK': '大型货车',
    'ELECTRIC_TRICYCLE': '电动三轮车'
  }
  return map[type] || type || '未填写'
}
</script>

<style scoped>
.fleet-driver-container {
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

.status-select {
  width: 120px;
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

.stat-value.available {
  color: #67c23a;
}

.stat-value.busy {
  color: #e6a23c;
}

.stat-value.leave {
  color: #909399;
}

.driver-list {
  padding: 0 16px;
}

.empty-state {
  padding: 40px 0;
}

.driver-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.driver-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.driver-avatar {
  position: relative;
}

.status-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #fff;
}

.status-dot.available {
  background: #67c23a;
}

.status-dot.busy {
  background: #e6a23c;
}

.status-dot.leave {
  background: #909399;
}

.status-dot.resigned {
  background: #f56c6c;
}

.driver-info {
  flex: 1;
  min-width: 0;
}

.driver-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.driver-phone {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}

.driver-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.driver-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
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

.driver-form {
  padding: 16px 0;
}

.driver-detail {
  padding: 16px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-name {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.detail-descriptions {
  margin-top: 16px;
}

.status-change-form {
  padding: 16px 0;
}

.status-change-form p {
  margin: 8px 0;
  color: #666;
}

@media (min-width: 768px) {
  .fleet-driver-container {
    max-width: 480px;
    margin: 0 auto;
  }
}
</style>
