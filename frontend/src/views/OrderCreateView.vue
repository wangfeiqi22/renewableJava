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
            <el-button type="success" size="small" round @click="$router.push('/home')">
              <el-icon><Service /></el-icon> AI 客服助手
            </el-button>
          </div>
          
          <div class="card order-card" style="padding: 0;">
            <!-- Step Indicator -->
            <div class="step-indicator">
              <div class="step" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
                <div class="step-icon">
                  <el-icon v-if="currentStep > 1"><Check /></el-icon>
                  <span v-else>1</span>
                </div>
              </div>
              <div class="step-line" :class="{ active: currentStep >= 2 }"></div>
              <div class="step" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
                <div class="step-icon">
                  <el-icon v-if="currentStep > 2"><Check /></el-icon>
                  <span v-else>2</span>
                </div>
              </div>
              <div class="step-line" :class="{ active: currentStep >= 3 }"></div>
              <div class="step" :class="{ active: currentStep >= 3, completed: currentStep > 3 }">
                <div class="step-icon">
                  <span v-if="currentStep <= 3">3</span>
                </div>
              </div>
            </div>

            <el-form :model="orderForm" label-position="top" class="order-form" style="padding: 20px;">
              
              <!-- Step 1: Basic Info -->
              <div v-show="currentStep === 1">
                <!-- Waste Type Selection (Visual Cards) -->
                <div class="form-group" style="margin-bottom: 15px;">
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
                </div>

                <!-- 引入全新设计的清运地址与方数卡片组件 -->
                <OrderInfoCards 
                  v-model:detailAddress="orderForm.pickupAddress"
                  v-model:volumeValue="orderForm.estWeight"
                  v-model:wasteType="orderForm.wasteType"
                  @edit-address="showMapPicker = true"
                  class="mb-20"
                />

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
              </div>

              <!-- Step 2: Advanced Info -->
              <div v-show="currentStep === 2" class="step2-container">
                <!-- 垃圾堆放情况 -->
                <div class="eco-section">
                  <div class="eco-section-header">
                    <el-icon class="eco-icon"><Location /></el-icon>
                    <span>垃圾堆放情况</span>
                  </div>
                  <div class="eco-section-desc">请选择垃圾当前的堆放状态，我们将为您推荐最佳清运方案</div>
                  
                  <div class="placement-options">
                    <div 
                      v-for="opt in placementOptions" 
                      :key="opt.value"
                      class="placement-card"
                      :class="{ active: orderForm.placementStatus === opt.value }"
                      @click="orderForm.placementStatus = opt.value"
                    >
                      <div class="opt-icon-box">
                        <el-icon v-if="opt.icon === 'check'"><Select /></el-icon>
                        <el-icon v-else-if="opt.icon === 'box'"><Box /></el-icon>
                        <el-icon v-else-if="opt.icon === 'collection'"><Files /></el-icon>
                        <el-icon v-else-if="opt.icon === 'indoor'"><House /></el-icon>
                      </div>
                      <div class="opt-content">
                        <div class="opt-title">{{ opt.label }}</div>
                        <div class="opt-desc">{{ opt.desc }}</div>
                      </div>
                      <div class="opt-check" v-if="orderForm.placementStatus === opt.value">
                        <el-icon><Check /></el-icon>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 收集箱租用 -->
                <div class="eco-section" v-if="orderForm.placementStatus === '需放置收集箱'">
                  <div class="eco-section-header">
                    <el-icon class="eco-icon blue"><Box /></el-icon>
                    <span>收集箱租用</span>
                  </div>
                  
                  <div class="rental-box">
                    <div class="rental-info">
                      <div class="rental-title">租用天数</div>
                      <div class="rental-desc">配送费80元 + 50元/天</div>
                    </div>
                    <div class="rental-stepper">
                      <button type="button" class="step-btn" @click="orderForm.rentalDays > 1 && orderForm.rentalDays--"><el-icon><Minus /></el-icon></button>
                      <span class="step-val">{{ orderForm.rentalDays }}天</span>
                      <button type="button" class="step-btn" @click="orderForm.rentalDays++"><el-icon><Plus /></el-icon></button>
                    </div>
                  </div>
                </div>

                <!-- 附加服务 -->
                <div class="eco-section">
                  <div class="eco-section-header">
                    <el-icon class="eco-icon green"><Setting /></el-icon>
                    <span>附加服务</span>
                  </div>
                  
                  <div class="service-options">
                    <div 
                      v-for="srv in additionalServices" 
                      :key="srv.value"
                      class="service-card"
                      :class="{ active: orderForm.additionalServices.includes(srv.value) }"
                      @click="toggleService(srv.value)"
                    >
                      <div class="srv-icon-box">
                        <el-icon v-if="srv.icon === 'van'"><Van /></el-icon>
                        <el-icon v-else-if="srv.icon === 'forklift'"><Box /></el-icon>
                      </div>
                      <div class="srv-content">
                        <div class="srv-title">{{ srv.label }}</div>
                        <div class="srv-desc">{{ srv.desc }}</div>
                      </div>
                      <div class="srv-check" v-if="orderForm.additionalServices.includes(srv.value)">
                        <el-icon><Check /></el-icon>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Form Actions -->
              <div class="form-actions multi-step-actions">
                <el-button 
                  v-if="currentStep === 2"
                  size="large" 
                  class="btn-prev" 
                  @click="currentStep = 1"
                >
                  上一步
                </el-button>
                <el-button 
                  v-if="currentStep === 1"
                  type="primary" 
                  size="large" 
                  class="btn-primary submit-btn flex-1" 
                  @click="validateStep1"
                >
                  下一步 >
                </el-button>
                <el-button 
                  v-if="currentStep === 2"
                  type="primary" 
                  size="large" 
                  class="btn-primary submit-btn flex-1" 
                  @click="submitOrder"
                  :loading="submitting"
                >
                  提交预约
                </el-button>
              </div>
            </el-form>
          </div>
        </section>

        <!-- History Section -->
        <section class="history-section">
          <div class="section-header">
            <h2>🕒 最近订单</h2>
            <el-button link type="primary" @click="router.push('/order/history')">查看全部 ></el-button>
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

    <el-dialog v-model="showAiResultDialog" title="智能识别结果确认" width="400px">
      <div class="ai-report">
        <p><strong>识别物品：</strong> {{ aiResult.itemName }}</p>
        <p><strong>推荐分类：</strong> {{ getWasteTypeName(aiResult.category) }} (置信度: {{ aiResult.confidence }}%)</p>
        <p><strong>处理建议：</strong> {{ aiResult.advice }}</p>
        <p><strong>环保提示：</strong> {{ aiResult.environmentalTips }}</p>
        <p><em>(实验组标识：{{ aiResult.abTestGroup }})</em></p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="openFeedbackDialog">识别有误，我要纠错</el-button>
          <el-button type="primary" @click="confirmAiResult">确认分类</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="showFeedbackDialog" title="模型纠错反馈" width="400px">
      <el-form :model="feedbackForm">
        <el-form-item label="实际分类">
          <el-select v-model="feedbackForm.userCorrectedType" placeholder="请选择正确的分类">
            <el-option v-for="type in wasteTypes" :key="type.value" :label="type.label" :value="type.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showFeedbackDialog = false">取消</el-button>
          <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Service, Camera, Check, Location, Select, Box, Files, House, Setting, Minus, Plus, Van } from '@element-plus/icons-vue'
import MapPicker from '../components/MapPicker.vue'
import OrderInfoCards from '../components/OrderInfoCards.vue'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const submitting = ref(false)
const aiAnalyzing = ref(false)
const showMapPicker = ref(false)

const currentStep = ref(1)

const showAiResultDialog = ref(false)
const showFeedbackDialog = ref(false)
const aiResult = reactive({
  itemName: '',
  category: '',
  confidence: 0,
  advice: '',
  environmentalTips: '',
  abTestGroup: ''
})
const feedbackForm = reactive({
  originalImageUrl: 'mock_url.jpg',
  predictedType: '',
  userCorrectedType: ''
})

const wasteTypes = [
  { label: '可回收物', value: 'recyclable', icon: '♻️' },
  { label: '有害垃圾', value: 'hazardous', icon: '☠️' },
  { label: '湿垃圾', value: 'wet', icon: '🍲' },
  { label: '干垃圾', value: 'dry', icon: '🗑️' }
]

const placementOptions = [
  { label: '已堆放到位', value: '已堆放到位', desc: '垃圾已堆放在可即时清运点，可直接装车清运', icon: 'check' },
  { label: '散落堆放', value: '散落堆放', desc: '垃圾分散堆放或未集中，需要先集中再清运', icon: 'collection' },
  { label: '需放置收集箱', value: '需放置收集箱', desc: '垃圾需要收集箱暂存，等待清运', icon: 'box' },
  { label: '室内/楼上', value: '室内/楼上', desc: '垃圾在室内或楼上，需要人工搬运', icon: 'indoor' }
]

const additionalServices = [
  { label: '人工搬运', value: '人工搬运', desc: '¥80/小时', icon: 'van' },
  { label: '叉车服务', value: '叉车服务', desc: '¥150/车', icon: 'forklift' }
]

const orderForm = reactive({
  pickupAddress: '',
  wasteType: 'recyclable',
  estWeight: 10,
  wasteDesc: '',
  creatorId: user.value.id,
  type: 1,
  // New step 2 fields
  placementStatus: '',
  rentalDays: 3,
  additionalServices: []
})

const toggleService = (val) => {
  const index = orderForm.additionalServices.indexOf(val)
  if (index === -1) {
    orderForm.additionalServices.push(val)
  } else {
    orderForm.additionalServices.splice(index, 1)
  }
}

const validateStep1 = () => {
  if (!orderForm.pickupAddress) {
    ElMessage.warning('请选择或填写清运地址')
    return
  }
  if (!orderForm.estWeight || orderForm.estWeight <= 0) {
    ElMessage.warning('请输入有效的垃圾方数')
    return
  }
  currentStep.value = 2
}

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
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    formData.append('userId', user.value.id || 0)
    
    const res = await api.post('/ai/recognize-waste', formData)
    const data = res.data
    Object.assign(aiResult, data)
    
    if (data.confidence >= 85) {
      ElMessage.success(`AI 高置信度识别完成 (${data.confidence}%)`)
      orderForm.wasteType = data.category
      orderForm.wasteDesc = `AI 识别结果：${data.itemName}，建议：${data.advice}`
    } else {
      ElMessage.warning(`AI 置信度较低 (${data.confidence}%)，请人工核对`)
      showAiResultDialog.value = true
    }
  } catch (error) {
    ElMessage.error('AI 识别失败')
  } finally {
    aiAnalyzing.value = false
  }
}

const confirmAiResult = () => {
  orderForm.wasteType = aiResult.category
  orderForm.wasteDesc = `AI 识别结果：${aiResult.itemName}，建议：${aiResult.advice}`
  showAiResultDialog.value = false
}

const openFeedbackDialog = () => {
  feedbackForm.predictedType = aiResult.category
  feedbackForm.userCorrectedType = ''
  showFeedbackDialog.value = true
}

const submitFeedback = async () => {
  if (!feedbackForm.userCorrectedType) {
    ElMessage.warning('请选择正确的分类')
    return
  }
  try {
    await api.post('/ai/feedback', {
      originalImageUrl: feedbackForm.originalImageUrl,
      predictedType: feedbackForm.predictedType,
      userCorrectedType: feedbackForm.userCorrectedType,
      confidence: aiResult.confidence
    })
    ElMessage.success('感谢您的反馈！模型已记录该次纠错。')
    
    orderForm.wasteType = feedbackForm.userCorrectedType
    orderForm.wasteDesc = `人工修正 AI 识别结果为：${getWasteTypeName(feedbackForm.userCorrectedType)}`
    
    showFeedbackDialog.value = false
    showAiResultDialog.value = false
  } catch (e) {
    ElMessage.error('反馈提交失败')
  }
}

const submitOrder = async () => {
  if (!user.value.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (!orderForm.placementStatus) {
    ElMessage.warning('请选择垃圾堆放情况')
    return
  }

  submitting.value = true
  try {
    const payload = {
      ...orderForm,
      additionalServices: orderForm.additionalServices.join(',')
    }
    const res = await api.post('/orders', payload)
    if (res.status === 200 || res.status === 201) {
      ElMessage.success('🎉 订单提交成功！')
      // redirect to home and pass success param
      router.push({ path: '/home', query: { newOrderNo: res.data.orderNo, newOrderId: res.data.id } })
    }
  } catch (error) {
    console.error('Submit failed', error)
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

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 40px;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

.step-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #f5f5f5;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  transition: all 0.3s;
}

.step.active .step-icon {
  background-color: #fff;
  border: 2px solid #0dc8a3;
  color: #0dc8a3;
}

.step.completed .step-icon {
  background-color: #0dc8a3;
  color: #fff;
  border: 2px solid #0dc8a3;
}

.step-line {
  flex: 1;
  height: 2px;
  background-color: #f0f0f0;
  margin: 0 10px;
  transition: all 0.3s;
}

.step-line.active {
  background-color: #0dc8a3;
}

.eco-section {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  padding: 20px;
  border: 1px solid #f0f2f5;
  margin-bottom: 20px;
}

.eco-section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #333333;
}

.eco-icon {
  font-size: 18px;
  color: #0dc8a3;
}
.eco-icon.blue { color: #4078f2; }
.eco-icon.green { color: #0dc8a3; }

.eco-section-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 16px;
}

.placement-options, .service-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.placement-card, .service-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.placement-card:hover, .service-card:hover {
  border-color: #0dc8a3;
}

.placement-card.active, .service-card.active {
  border-color: #4078f2;
  background: #f4f8ff;
}
.service-card.active {
  border-color: #0dc8a3;
  background: #f0fdf4;
}

.opt-icon-box, .srv-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f7f8fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: #666;
  font-size: 20px;
}
.placement-card.active .opt-icon-box {
  background: #4078f2;
  color: #fff;
}

.opt-content, .srv-content {
  flex: 1;
}

.opt-title, .srv-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.opt-desc, .srv-desc {
  font-size: 12px;
  color: #999;
}

.opt-check, .srv-check {
  color: #4078f2;
  font-size: 18px;
}
.srv-check {
  color: #0dc8a3;
}

.rental-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rental-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.rental-desc {
  font-size: 12px;
  color: #999;
}

.rental-stepper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.step-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid #eef0f2;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #666;
}
.step-btn:hover {
  border-color: #0dc8a3;
  color: #0dc8a3;
}

.step-val {
  font-size: 14px;
  font-weight: 500;
}

.multi-step-actions {
  display: flex;
  gap: 16px;
}
.btn-prev {
  flex: 1;
  border-color: #eef0f2;
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
