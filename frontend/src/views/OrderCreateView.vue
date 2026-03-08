<template>
  <div class="app-container">
    <!-- Navbar -->
    <nav class="navbar">
      <div class="nav-content container">
        <div class="brand">
          <div class="logo-icon">♻️</div>
          <span class="logo-text">智慧清运</span>
        </div>
        <div class="user-menu" v-if="user">
          <div class="user-profile">
            <el-avatar :size="32" :src="user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            <span class="username">{{ user.username }}</span>
          </div>
          <el-button type="primary" plain size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </nav>

    <main class="main-content container">
      <div class="dashboard-grid">
        <!-- Order Creation Section -->
        <section class="create-order-section">
          <div class="section-header">
            <h2>✨ 立即下单</h2>
            <el-button type="success" size="small" round @click="$router.push('/chat')">
              <el-icon><Service /></el-icon> AI 客服助手
            </el-button>
          </div>
          
          <div class="card order-card">
            <el-form :model="orderForm" label-position="top" class="order-form">
              
              <!-- Waste Type Selection (Visual Cards) -->
              <div class="form-group">
                <label class="form-label">选择垃圾类型</label>
                <div class="ai-upload-box" v-loading="aiAnalyzing">
                  <el-upload
                    class="ai-uploader"
                    action="#"
                    :show-file-list="false"
                    :http-request="handleAiAnalyze"
                  >
                    <el-button type="primary" icon="Camera" plain>拍照智能识别</el-button>
                  </el-upload>
                  <span class="ai-tip">上传照片，AI 自动识别类型和重量</span>
                </div>
                <div class="waste-type-grid">
                  <div 
                    v-for="type in wasteTypes" 
                    :key="type.value"
                    class="type-card"
                    :class="{ active: orderForm.wasteType === type.value }"
                    @click="orderForm.wasteType = type.value"
                  >
                    <div class="type-icon">{{ type.icon }}</div>
                    <span class="type-name">{{ type.label }}</span>
                  </div>
                </div>
              </div>

              <!-- Address & Weight -->
              <div class="form-row">
                <el-form-item label="📍 起运地址" class="flex-1">
                  <el-input v-model="orderForm.pickupAddress" placeholder="点击选择或输入详细地址" class="input-field" @click="showMapPicker = true" readonly />
                </el-form-item>
                <el-form-item label="⚖️ 预估重量 (kg)" style="width: 180px;">
                  <el-input-number v-model="orderForm.estWeight" :precision="2" :step="1" :min="0" class="input-field" />
                </el-form-item>
              </div>

              <el-form-item label="📝 备注说明">
                <el-input 
                  type="textarea" 
                  v-model="orderForm.wasteDesc" 
                  placeholder="描述垃圾的具体情况..." 
                  :rows="3"
                  resize="none"
                  class="input-field"
                />
              </el-form-item>

              <div class="form-actions">
                <el-button 
                  type="primary" 
                  size="large" 
                  class="btn-primary submit-btn" 
                  @click="submitOrder"
                  :loading="submitting"
                >
                  立即预约清运
                </el-button>
              </div>
            </el-form>
          </div>
        </section>

        <!-- History Section -->
        <section class="history-section">
          <div class="section-header">
            <h2>🕒 最近订单</h2>
            <el-button link type="primary">查看全部 ></el-button>
          </div>
          
          <div class="order-list">
            <el-empty v-if="orderHistory.length === 0" description="暂无订单记录" />
            
            <div v-for="order in orderHistory" :key="order.id" class="card order-item-card">
              <div class="order-header">
                <span class="order-no">#{{ order.orderNo }}</span>
                <el-tag :type="getStatusType(order.status)" effect="light" round>
                  {{ getStatusText(order.status) }}
                </el-tag>
              </div>
              <div class="order-body">
                <div class="info-row">
                  <span class="label">类型：</span>
                  <span class="value">{{ getWasteTypeName(order.wasteType) }}</span>
                </div>
                <div class="info-row">
                  <span class="label">地址：</span>
                  <span class="value address">{{ order.pickupAddress }}</span>
                </div>
                <div class="info-row">
                  <span class="label">时间：</span>
                  <span class="value">{{ new Date(order.createdAt).toLocaleString() }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>

    <map-picker 
      v-model:visible="showMapPicker"
      @address-selected="handleAddressSelected"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Service, Camera } from '@element-plus/icons-vue'
import MapPicker from '../components/MapPicker.vue'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const submitting = ref(false)
const aiAnalyzing = ref(false)
const showMapPicker = ref(false)

const wasteTypes = [
  { label: '生活垃圾', value: 'household', icon: '🏠' },
  { label: '建筑垃圾', value: 'construction', icon: '🧱' },
  { label: '大件垃圾', value: 'bulky', icon: '🛋️' },
  { label: '厨余垃圾', value: 'kitchen', icon: '🍲' }
]

const orderForm = reactive({
  pickupAddress: '',
  wasteType: 'household',
  estWeight: 10,
  wasteDesc: '',
  creatorId: user.value.id,
  type: 1
})

const handleAddressSelected = (address) => {
  const { province, city, district, street, detailAddress } = address;
  orderForm.pickupAddress = `${province}.${city}.${district}.${street}${detailAddress}`;
  showMapPicker.value = false;
}

const orderHistory = ref([])

const fetchOrders = async () => {
  if (!user.value.id) return
  try {
    const res = await api.get(`/orders/user/${user.value.id}`)
    orderHistory.value = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 5) // Limit to 5
  } catch (error) {
    console.error(error)
  }
}

const handleAiAnalyze = async (options) => {
  aiAnalyzing.value = true
  
  // Create mock delay to simulate network request
  setTimeout(() => {
    aiAnalyzing.value = false
    
    // Mock AI Response Logic based on random seed
    const types = ['construction', 'household', 'kitchen']
    const randomType = types[Math.floor(Math.random() * types.length)]
    const randomWeight = Math.floor(Math.random() * 50) + 10
    
    ElMessage.success(`AI 识别完成：${getWasteTypeName(randomType)}，预估 ${randomWeight}kg`)
    
    orderForm.wasteType = randomType
    orderForm.estWeight = randomWeight
    orderForm.wasteDesc = `AI 智能识别结果：检测到${getWasteTypeName(randomType)}堆积，包含塑料袋、纸箱等杂物。`
    
  }, 2000)
}

const submitOrder = async () => {
  if (!user.value.id) {
    ElMessage.warning('请先登录')
    return
  }
  submitting.value = true
  try {
    const res = await api.post('/orders', { ...orderForm, creatorId: user.value.id })
    if (res.status === 200) {
      ElMessage.success('🎉 订单提交成功！')
      fetchOrders()
      // Reset form (partial)
      orderForm.wasteDesc = ''
    }
  } catch (error) {
    // Error handled
  } finally {
    submitting.value = false
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const getStatusText = (status) => {
  const map = { 10: '待接单', 20: '已接单', 30: '装车中', 40: '运输中', 50: '已到站', 60: '已完成' }
  return map[status] || '处理中'
}

const getStatusType = (status) => {
  if (status === 60) return 'success'
  if (status === 10) return 'warning'
  return 'primary'
}

const getWasteTypeName = (val) => {
  const type = wasteTypes.find(t => t.value === val)
  return type ? type.label : val
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.app-container {
  background-color: var(--color-background);
  min-height: 100vh;
}

.ai-upload-box {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.ai-tip {
  font-size: 12px;
  color: #909399;
}

.navbar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-sm);
}

.nav-content {
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: -0.5px;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-weight: 500;
  color: var(--color-text-main);
}

.main-content {
  padding-top: 32px;
  padding-bottom: 32px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

@media (max-width: 768px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 {
  font-size: 18px;
  margin: 0;
  color: var(--color-text-main);
}

/* Waste Type Grid */
.waste-type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 8px;
}

.type-card {
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 16px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.type-card:hover {
  border-color: var(--color-primary-hover);
  background-color: #f0fdf4;
}

.type-card.active {
  border-color: var(--color-primary);
  background-color: #e8f5e9;
  box-shadow: 0 4px 12px rgba(0, 200, 83, 0.2);
}

.type-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.type-name {
  font-size: 13px;
  font-weight: 500;
}

.form-row {
  display: flex;
  gap: 16px;
}

.flex-1 { flex: 1; }

.submit-btn {
  width: 100%;
  margin-top: 12px;
  height: 48px;
  font-size: 16px;
  letter-spacing: 1px;
}

/* Order History List */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item-card {
  padding: 16px;
  cursor: pointer;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.order-no {
  font-family: monospace;
  color: var(--color-text-secondary);
  font-weight: 600;
}

.info-row {
  display: flex;
  margin-bottom: 4px;
  font-size: 13px;
}

.info-row .label {
  color: var(--color-text-placeholder);
  width: 48px;
}

.info-row .value {
  color: var(--color-text-main);
}

.address {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
</style>
