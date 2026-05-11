<template>
  <div class="fleet-projects-container">
    <!-- Header -->
    <header class="page-header">
      <div class="header-left" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">项目管理</div>
      <div class="header-right"></div>
    </header>

    <!-- Stats Summary -->
    <div class="stats-summary">
      <div class="stat-item">
        <div class="stat-icon teal">
          <el-icon><Folder /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalProjects }}</div>
          <div class="stat-label">项目总数</div>
        </div>
      </div>

      <div class="stat-item">
        <div class="stat-icon green">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.activeProjects }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>

      <div class="stat-item">
        <div class="stat-icon blue">
          <el-icon><Money /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">¥{{ (stats.totalRevenue || 0).toFixed(0) }}</div>
          <div class="stat-label">累计收入</div>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="project-tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="进行中" name="ACTIVE" />
      <el-tab-pane label="已暂停" name="PAUSED" />
      <el-tab-pane label="已结束" name="COMPLETED" />
    </el-tabs>

    <!-- Search & Filter -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索项目名称"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- Project List -->
    <div class="project-list" v-loading="loading">
      <div v-if="!loading && projects.length === 0" class="empty-state">
        <el-empty description="暂无项目数据">
          <el-button type="primary" @click="handleAdd">创建第一个项目</el-button>
        </el-empty>
      </div>

      <div
        v-for="project in projects"
        :key="project.id"
        class="project-card"
        @click="handleViewDetail(project)"
      >
        <div class="project-header">
          <div class="project-name">{{ project.name }}</div>
          <el-tag :type="getStatusType(project.status)" size="small">
            {{ getStatusText(project.status) }}
          </el-tag>
        </div>

        <div class="project-content">
          <div class="project-info">
            <div class="info-row">
              <el-icon><Location /></el-icon>
              <span>{{ project.address || '未设置地址' }}</span>
            </div>
            <div class="info-row">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(project.startDate) }} - {{ project.endDate ? formatDate(project.endDate) : '长期' }}</span>
            </div>
            <div class="info-row">
              <el-icon><User /></el-icon>
              <span>{{ project.contactPerson || '未设置联系人' }}</span>
            </div>
          </div>

          <div class="project-stats">
            <div class="stat-item">
              <div class="stat-value">{{ project.assignedDrivers || 0 }}</div>
              <div class="stat-label">已指派司机</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ project.totalOrders || 0 }}</div>
              <div class="stat-label">总订单数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">¥{{ (project.revenue || 0).toFixed(0) }}</div>
              <div class="stat-label">项目收入</div>
            </div>
          </div>
        </div>

        <div class="project-actions">
          <el-button
            type="primary"
            size="small"
            @click.stop="handleEdit(project)"
          >
            编辑
          </el-button>
          <el-button
            v-if="project.status === 'ACTIVE'"
            type="warning"
            size="small"
            @click.stop="handleChangeStatus(project, 'PAUSED')"
          >
            暂停
          </el-button>
          <el-button
            v-if="project.status === 'PAUSED'"
            type="success"
            size="small"
            @click.stop="handleChangeStatus(project, 'ACTIVE')"
          >
            启用
          </el-button>
          <el-button
            type="danger"
            size="small"
            plain
            @click.stop="handleDelete(project)"
          >
            删除
          </el-button>
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

    <!-- Add/Edit Project Button -->
    <div class="fab-wrapper">
      <el-button type="primary" circle size="large" class="add-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>

    <!-- Add/Edit Project Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑项目' : '创建项目'"
      width="90%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
        class="project-form"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入项目名称" />
        </el-form-item>

        <el-form-item label="项目地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入项目地址" />
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="formData.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="formData.endDate"
            type="date"
            placeholder="选择结束日期（可选）"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="formData.contactPerson" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>

        <el-form-item label="定价模式" prop="pricingModel">
          <el-radio-group v-model="formData.pricingModel">
            <el-radio label="FIXED">固定价格</el-radio>
            <el-radio label="PER_ORDER">按单计价</el-radio>
            <el-radio label="PER_WEIGHT">按重量计价</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="单价" prop="unitPrice" v-if="formData.pricingModel !== 'FIXED'">
          <el-input-number
            v-model="formData.unitPrice"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
          <span class="form-tip">
            {{ formData.pricingModel === 'PER_ORDER' ? '元/单' : '元/kg' }}
          </span>
        </el-form-item>

        <el-form-item label="项目状态" prop="status">
          <el-select v-model="formData.status" placeholder="选择项目状态" style="width: 100%">
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="暂停" value="PAUSED" />
            <el-option label="已结束" value="COMPLETED" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Project Detail Dialog -->
    <el-dialog
      v-model="detailVisible"
      title="项目详情"
      width="90%"
      class="project-detail-dialog"
    >
      <div class="project-detail" v-if="selectedProject">
        <!-- Status Badge -->
        <div class="detail-status">
          <el-tag :type="getStatusType(selectedProject.status)" size="large">
            {{ getStatusText(selectedProject.status) }}
          </el-tag>
        </div>

        <!-- Basic Info Card -->
        <div class="detail-section">
          <div class="detail-card">
            <div class="detail-title">
              <el-icon><Folder /></el-icon>
              <span>基本信息</span>
            </div>
            <div class="detail-content">
              <div class="detail-row">
                <span class="detail-label">项目名称</span>
                <span class="detail-value">{{ selectedProject.name || '未设置' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">状态</span>
                <span class="detail-value">
                  <el-tag :type="getStatusType(selectedProject.status)" size="small">
                    {{ getStatusText(selectedProject.status) }}
                  </el-tag>
                </span>
              </div>
              <div class="detail-row">
                <span class="detail-label">联系人</span>
                <span class="detail-value">{{ selectedProject.contactPerson || '未设置' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">联系电话</span>
                <span class="detail-value">{{ selectedProject.contactPhone || '未设置' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">地址</span>
                <span class="detail-value">{{ selectedProject.address || '未设置' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">开始日期</span>
                <span class="detail-value">{{ selectedProject.startDate || '未设置' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">结束日期</span>
                <span class="detail-value">{{ selectedProject.endDate || '未设置' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Pricing Info Card -->
        <div class="detail-section">
          <div class="detail-card">
            <div class="detail-title">
              <el-icon><Money /></el-icon>
              <span>定价信息</span>
            </div>
            <div class="detail-content">
              <div class="detail-row">
                <span class="detail-label">定价模式</span>
                <span class="detail-value">{{ getPricingModelText(selectedProject.pricingModel) }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">单价</span>
                <span class="detail-value price">
                  {{ formatPrice(selectedProject) }}
                </span>
              </div>
              <div class="detail-row" v-if="selectedProject.description">
                <span class="detail-label">项目描述</span>
                <span class="detail-value">{{ selectedProject.description }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Stats Info Card -->
        <div class="detail-section">
          <div class="detail-card">
            <div class="detail-title">
              <el-icon><DataLine /></el-icon>
              <span>项目统计</span>
            </div>
            <div class="stats-grid">
              <div class="stat-box">
                <div class="stat-icon teal">
                  <el-icon><User /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ selectedProject.assignedDrivers || 0 }}</div>
                  <div class="stat-label">关联司机</div>
                </div>
              </div>
              <div class="stat-box">
                <div class="stat-icon blue">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ selectedProject.totalOrders || 0 }}</div>
                  <div class="stat-label">关联订单</div>
                </div>
              </div>
              <div class="stat-box">
                <div class="stat-icon orange">
                  <el-icon><Money /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">¥{{ (selectedProject.revenue || 0).toFixed(0) }}</div>
                  <div class="stat-label">项目收入</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Driver List -->
        <div class="detail-section" v-if="selectedProject.drivers && selectedProject.drivers.length > 0">
          <div class="detail-card">
            <div class="detail-title">
              <el-icon><Van /></el-icon>
              <span>关联司机列表</span>
            </div>
            <div class="driver-list">
              <div
                v-for="driver in selectedProject.drivers"
                :key="driver.id"
                class="driver-item"
              >
                <el-avatar :size="36" :src="driver.avatarUrl">
                  {{ driver.name?.charAt(0) || '司' }}
                </el-avatar>
                <div class="driver-info">
                  <div class="driver-name">{{ driver.name }}</div>
                  <div class="driver-phone">{{ driver.phone }}</div>
                </div>
                <el-tag size="small" :type="getDriverStatusType(driver.status)">
                  {{ getDriverStatusText(driver.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Orders -->
        <div class="detail-section" v-if="selectedProject.recentOrders && selectedProject.recentOrders.length > 0">
          <div class="detail-card">
            <div class="detail-title">
              <el-icon><Clock /></el-icon>
              <span>近期订单</span>
            </div>
            <div class="order-list">
              <div
                v-for="order in selectedProject.recentOrders"
                :key="order.id"
                class="order-item"
              >
                <div class="order-info">
                  <div class="order-id">#{{ order.id }}</div>
                  <div class="order-address">{{ order.pickupAddress }}</div>
                </div>
                <el-tag size="small" :type="getOrderStatusType(order.status)">
                  {{ getOrderStatusText(order.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">返回列表</el-button>
        <el-button type="primary" @click="handleEditFromDetail">
          编辑项目
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
  Folder,
  CircleCheck,
  Money,
  Location,
  Calendar,
  User,
  Van,
  Clock,
  Document,
  DataLine
} from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()

// State
const loading = ref(false)
const submitting = ref(false)
const projects = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const searchKeyword = ref('')
const activeTab = ref('all')

// Stats
const stats = ref({
  totalProjects: 0,
  activeProjects: 0,
  totalRevenue: 0
})

// Dialogs
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const selectedProject = ref(null)
const formRef = ref(null)

// Form
const formData = ref({
  name: '',
  address: '',
  startDate: '',
  endDate: '',
  contactPerson: '',
  contactPhone: '',
  description: '',
  pricingModel: 'PER_ORDER',
  unitPrice: 0,
  status: 'ACTIVE'
})

const formRules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入项目地址', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

// Lifecycle
onMounted(() => {
  checkAuth()
  loadProjects()
  loadStats()
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
const loadProjects = async () => {
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

    if (searchKeyword.value) {
      params.projectName = searchKeyword.value
    }

    const response = await api.get('/fleet/projects', { params })
    if (response.data.code === 200) {
      projects.value = response.data.data.content || []
      totalElements.value = response.data.data.totalElements || 0
    }
  } catch (error) {
    console.error('Failed to load projects:', error)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await api.get('/fleet/projects/stats')
    if (response.data.code === 200) {
      stats.value = response.data.data
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

const createProject = async () => {
  submitting.value = true
  try {
    const projectData = {
      projectName: formData.value.name,
      address: formData.value.address,
      contractStartDate: formData.value.startDate,
      contractEndDate: formData.value.endDate,
      contactPerson: formData.value.contactPerson,
      contactPhone: formData.value.contactPhone,
      description: formData.value.description,
      unitPrice: formData.value.unitPrice
    }
    
    const response = await api.post('/fleet/projects', projectData)
    if (response.data.code === 200) {
      ElMessage.success('项目创建成功')
      dialogVisible.value = false
      resetForm()
      loadProjects()
      loadStats()
    } else {
      ElMessage.error(response.data.message || '创建失败')
    }
  } catch (error) {
    console.error('Failed to create project:', error)
    ElMessage.error(error.response?.data?.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

const updateProject = async () => {
  submitting.value = true
  try {
    const projectData = {
      projectName: formData.value.name,
      address: formData.value.address,
      contractStartDate: formData.value.startDate,
      contractEndDate: formData.value.endDate,
      contactPerson: formData.value.contactPerson,
      contactPhone: formData.value.contactPhone,
      description: formData.value.description,
      unitPrice: formData.value.unitPrice,
      status: formData.value.status
    }
    
    const response = await api.put(`/fleet/projects/${formData.value.id}`, projectData)
    if (response.data.code === 200) {
      ElMessage.success('项目更新成功')
      dialogVisible.value = false
      resetForm()
      loadProjects()
      loadStats()
    } else {
      ElMessage.error(response.data.message || '更新失败')
    }
  } catch (error) {
    console.error('Failed to update project:', error)
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    submitting.value = false
  }
}

const deleteProject = async (project) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目 "${project.name}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await api.delete(`/fleet/projects/${project.id}`)
    if (response.data.code === 200) {
      ElMessage.success('项目已删除')
      loadProjects()
      loadStats()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete project:', error)
      ElMessage.error('删除失败')
    }
  }
}

const changeProjectStatus = async (project, newStatus) => {
  try {
    const statusText = newStatus === 'ACTIVE' ? '启用' : '暂停'
    await ElMessageBox.confirm(
      `确定要${statusText}项目 "${project.name}" 吗？`,
      '状态变更',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    const response = await api.put(`/fleet/projects/${project.id}`, { status: newStatus })
    if (response.data.code === 200) {
      ElMessage.success(`项目已${statusText}`)
      loadProjects()
      loadStats()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to change status:', error)
      ElMessage.error('操作失败')
    }
  }
}

// Event Handlers
const handleTabChange = () => {
  currentPage.value = 1
  loadProjects()
}

const handleSearch = () => {
  currentPage.value = 1
  loadProjects()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadProjects()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadProjects()
}

const handleAdd = () => {
  router.push('/fleet/projects/create')
}

const handleEdit = (project) => {
  isEdit.value = true
  formData.value = {
    id: project.id,
    name: project.name || project.projectName,
    address: project.address,
    startDate: project.startDate || project.contractStartDate,
    endDate: project.endDate || project.contractEndDate,
    contactPerson: project.contactPerson,
    contactPhone: project.contactPhone,
    description: project.description,
    pricingModel: project.pricingModel || 'PER_ORDER',
    unitPrice: project.unitPrice || 0,
    status: project.status || 'ACTIVE'
  }
  dialogVisible.value = true
}

const handleViewDetail = (project) => {
  selectedProject.value = { ...project }
  detailVisible.value = true
}

const handleEditFromDetail = () => {
  detailVisible.value = false
  handleEdit(selectedProject.value)
}

const handleDelete = (project) => {
  deleteProject(project)
}

const handleChangeStatus = (project, status) => {
  changeProjectStatus(project, status)
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await updateProject()
    } else {
      await createProject()
    }
  } catch (error) {
    console.error('Validation failed:', error)
  }
}

// Helpers
const resetForm = () => {
  formData.value = {
    name: '',
    address: '',
    startDate: '',
    endDate: '',
    contactPerson: '',
    contactPhone: '',
    description: '',
    pricingModel: 'PER_ORDER',
    unitPrice: 0,
    status: 'ACTIVE'
  }
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split('T')[0]
}

const getStatusType = (status) => {
  const map = {
    'ACTIVE': 'success',
    'PAUSED': 'warning',
    'COMPLETED': 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'ACTIVE': '进行中',
    'PAUSED': '已暂停',
    'COMPLETED': '已结束'
  }
  return map[status] || status
}

const getPricingModelText = (model) => {
  const map = {
    'FIXED': '固定价格',
    'PER_ORDER': '按单计价',
    'PER_WEIGHT': '按重量计价'
  }
  return map[model] || model
}

const formatPrice = (project) => {
  if (!project) return '未设置'
  if (project.pricingModel === 'FIXED') {
    return `¥${(project.fixedPrice || 0).toFixed(2)}`
  } else if (project.pricingModel === 'PER_ORDER') {
    return `¥${(project.unitPrice || 0).toFixed(2)}/单`
  } else if (project.pricingModel === 'PER_WEIGHT') {
    return `¥${(project.unitPrice || 0).toFixed(2)}/kg`
  }
  return '未设置'
}

const getDriverStatusType = (status) => {
  const map = {
    'AVAILABLE': 'success',
    'BUSY': 'warning',
    'OFFLINE': 'info'
  }
  return map[status] || 'info'
}

const getDriverStatusText = (status) => {
  const map = {
    'AVAILABLE': '空闲',
    'BUSY': '忙碌',
    'OFFLINE': '离线'
  }
  return map[status] || status
}

const getOrderStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'ASSIGNED': 'primary',
    'IN_PROGRESS': 'info',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return map[status] || 'info'
}

const getOrderStatusText = (status) => {
  const map = {
    'PENDING': '待接单',
    'ASSIGNED': '已指派',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}
</script>

<style scoped>
.fleet-projects-container {
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

.stats-summary {
  display: flex;
  padding: 16px;
  gap: 12px;
  background: #fff;
}

.stat-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
}

.stat-icon.teal {
  background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%);
}

.stat-icon.green {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-icon.blue {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 11px;
  color: #999;
}

.project-tabs {
  background: #fff;
  padding: 0 16px;
}

.filter-bar {
  padding: 12px 16px;
  background: #fff;
  display: flex;
  gap: 8px;
}

.search-input {
  flex: 1;
}

.project-list {
  padding: 0 16px;
}

.project-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.project-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.project-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.project-content {
  margin-bottom: 12px;
}

.project-info {
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}

.info-row .el-icon {
  color: #409eff;
  font-size: 14px;
}

.project-stats {
  display: flex;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.project-stats .stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  background: transparent;
}

.project-stats .stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.project-stats .stat-label {
  font-size: 10px;
  color: #999;
  margin-top: 2px;
}

.project-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
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

.project-form {
  padding: 16px 0;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}

@media (min-width: 768px) {
  .fleet-projects-container {
    max-width: 480px;
    margin: 0 auto;
  }
}

.project-detail-dialog :deep(.el-dialog__body) {
  padding: 16px;
  max-height: 70vh;
  overflow-y: auto;
}

.project-detail {
  padding: 0;
}

.detail-status {
  text-align: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-section {
  margin-bottom: 16px;
}

.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f5f5f5;
}

.detail-title .el-icon {
  color: #409eff;
  font-size: 18px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 6px 0;
}

.detail-row:not(:last-child) {
  border-bottom: 1px dashed #f0f0f0;
}

.detail-label {
  font-size: 13px;
  color: #666;
  min-width: 80px;
  flex-shrink: 0;
}

.detail-value {
  font-size: 13px;
  color: #333;
  text-align: right;
  flex: 1;
  margin-left: 12px;
  word-break: break-word;
}

.detail-value.price {
  font-weight: 600;
  color: #f56c6c;
  font-size: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 8px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-box .stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #fff;
}

.stat-box .stat-icon.orange {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
}

.stat-box .stat-info {
  text-align: center;
}

.stat-box .stat-value {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.stat-box .stat-label {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.driver-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.driver-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.2s;
}

.driver-item:hover {
  background: #f0f0f0;
}

.driver-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.driver-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.driver-phone {
  font-size: 12px;
  color: #999;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.2s;
}

.order-item:hover {
  background: #f0f0f0;
}

.order-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-id {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
}

.order-address {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .stat-box {
    padding: 10px 6px;
  }

  .stat-box .stat-icon {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }

  .stat-box .stat-value {
    font-size: 14px;
  }

  .order-address {
    max-width: 150px;
  }
}
</style>
