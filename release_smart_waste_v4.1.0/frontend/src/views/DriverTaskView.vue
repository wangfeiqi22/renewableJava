<template>
  <div class="driver-task-container">
    <el-header class="header">
      <div class="title">司机作业台</div>
      <div class="user-info" v-if="user">
        {{ user.username }} ({{ user.role }})
        <el-button size="small" type="danger" link style="color: white;" @click="handleLogout">退出</el-button>
      </div>
    </el-header>

    <el-main>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待处理任务" name="pending">
          <div v-for="order in tasks" :key="order.id" class="task-card">
            <el-card shadow="hover">
              <div class="task-header">
                <span class="order-no">#{{ order.orderNo.substring(0, 8) }}</span>
                <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
              </div>
              <div class="task-body">
                <p><strong>地址：</strong>{{ order.pickupAddress }}</p>
                <p><strong>类型：</strong>{{ order.wasteType }}</p>
                <p><strong>预估：</strong>{{ order.estWeight }} kg</p>
              </div>
              <div class="task-actions">
                <el-button v-if="order.status === 20" type="primary" size="small" @click="updateStatus(order.id, 30)">开始装车</el-button>
                <el-button v-if="order.status === 30" type="success" size="small" @click="openUpload(order, 'load')">上传装车照</el-button>
                <el-button v-if="order.status === 30" type="primary" size="small" @click="updateStatus(order.id, 40)">开始运输</el-button>
                <el-button v-if="order.status === 40" type="warning" size="small" @click="updateStatus(order.id, 50)">到达站点</el-button>
                <el-button v-if="order.status === 50" type="success" size="small" @click="openUpload(order, 'unload')">上传卸货照</el-button>
                <el-button v-if="order.status === 50" type="primary" size="small" @click="updateStatus(order.id, 60)">完成订单</el-button>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane label="历史任务" name="history">
          <el-empty description="暂无历史记录" />
        </el-tab-pane>
      </el-tabs>

      <!-- Upload Dialog -->
      <el-dialog v-model="uploadVisible" title="上传作业照片" width="90%">
        <el-upload
          class="upload-demo"
          action="#"
          :http-request="handleUpload"
          :file-list="fileList"
          list-type="picture"
        >
          <el-button type="primary">点击拍照/上传</el-button>
          <template #tip>
            <div class="el-upload__tip">
              请上传{{ uploadNodeType === 'load' ? '装车' : '卸货' }}现场照片，需包含车辆与货物
            </div>
          </template>
        </el-upload>
      </el-dialog>
    </el-main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const activeTab = ref('pending')
const tasks = ref([])
const uploadVisible = ref(false)
const currentOrder = ref(null)
const uploadNodeType = ref('')
const fileList = ref([])

// Mock tasks for demo if no real backend data
const fetchTasks = async () => {
  try {
    // In real app: api.get(`/orders/driver/${user.id}`)
    // For demo, we fetch all user orders or mock
    const res = await api.get(`/orders/user/${user.id}`)
    tasks.value = res.data.filter(o => o.status >= 20 && o.status < 60)
  } catch (error) {
    console.error(error)
  }
}

const updateStatus = async (orderId, status) => {
  try {
    await api.put(`/orders/${orderId}/status?status=${status}`)
    ElMessage.success('状态更新成功')
    fetchTasks()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const openUpload = (order, type) => {
  currentOrder.value = order
  uploadNodeType.value = type
  fileList.value = []
  uploadVisible.value = true
}

const handleUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  formData.append('orderId', currentOrder.value.id)
  formData.append('nodeType', uploadNodeType.value)

  try {
    await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success('上传成功')
    uploadVisible.value = false
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const getStatusText = (status) => {
  const map = { 20: '待装车', 30: '装车中', 40: '运输中', 50: '已到站' }
  return map[status] || status
}

const getStatusType = (status) => {
  return status === 30 ? 'warning' : 'primary'
}

onMounted(() => {
  fetchTasks()
})
</script>

<style scoped>
.driver-task-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}
.header {
  background-color: #409EFF; /* Blue for Driver App distinction */
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
}
.task-card {
  margin-bottom: 15px;
}
.task-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}
.order-no {
  font-weight: bold;
}
.task-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
