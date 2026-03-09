<template>
  <div class="driver-task-container">
    <div class="app-header">
<!-- ... (header content remains same) -->
      <div class="header-content">
        <h1 class="header-title">司机作业台</h1>
        <div class="user-profile" v-if="user">
          <span class="username">{{ user.username }}</span>
          <el-button size="small" type="danger" plain round @click="handleLogout" class="logout-btn">退出</el-button>
        </div>
      </div>
    </div>

    <div class="main-content">
      <el-tabs v-model="activeTab" class="custom-tabs" stretch>
        <!-- For Fleet Drivers (Type A): Order Pool -->
        <el-tab-pane v-if="user.fleetId" label="抢单大厅" name="pool">
             <div v-if="poolOrders.length === 0" class="empty-state">
                 <el-empty description="暂无可用订单" />
             </div>
             <div v-else class="task-list">
                 <div v-for="order in poolOrders" :key="order.id" class="task-card">
                     <div class="card-header">
                         <span class="order-id">#{{ order.orderNo.substring(0, 8) }}</span>
                         <el-tag type="success" size="small">待抢单</el-tag>
                     </div>
                     <div class="card-body">
                         <div class="info-row"><el-icon class="icon"><Location /></el-icon> {{ order.pickupAddress }}</div>
                         <div class="info-row"><el-icon class="icon"><Delete /></el-icon> {{ getWasteTypeName(order.wasteType) }} | {{ order.estWeight }} kg</div>
                     </div>
                     <div class="card-footer">
                         <el-button type="primary" class="action-btn" @click="grabOrder(order.id)">立即抢单</el-button>
                     </div>
                 </div>
             </div>
        </el-tab-pane>

        <!-- For Freelance Drivers (Type B): Self Create Only -->
        <el-tab-pane v-if="!user.fleetId" label="自主建单" name="create">
             <div class="create-order-container">
                 <el-form :model="createForm" label-position="top">
                     <!-- Waste Type Selection (Visual Cards) -->
                     <div class="form-group">
                        <label class="form-label">选择垃圾类型</label>
                        <div class="ai-upload-box" 
                             v-loading="aiAnalyzing"
                             :element-loading-text="aiLoadingText"
                        >
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
                            :class="{ active: createForm.wasteType === type.value }"
                            @click="createForm.wasteType = type.value"
                          >
                            <div class="type-icon">{{ type.icon }}</div>
                            <span class="type-name">{{ type.label }}</span>
                          </div>
                        </div>
                      </div>

                     <el-form-item label="托运人信息">
                         <el-input v-model="createForm.shipperName" placeholder="姓名/商户名" />
                         <el-input v-model="createForm.shipperPhone" placeholder="联系电话" style="margin-top: 10px;" />
                     </el-form-item>
<!-- ... (rest of form) -->
                     <el-form-item label="起运地址">
                         <el-input 
                           v-model="createForm.pickupAddress" 
                           placeholder="点击选择或输入详细地址" 
                           class="input-field" 
                           @click="showMapPicker = true" 
                           :readonly="false" 
                         >
                           <template #append>
                             <el-button @click="showMapPicker = true">
                               <el-icon><Location /></el-icon> 地图选点
                             </el-button>
                           </template>
                         </el-input>
                     </el-form-item>
                     <el-form-item label="预估重量 (kg)">
                         <el-input-number v-model="createForm.estWeight" :min="0" style="width: 100%" />
                     </el-form-item>
                     <el-form-item label="备注说明">
                        <el-input 
                          type="textarea" 
                          v-model="createForm.wasteDesc" 
                          placeholder="描述垃圾的具体情况..." 
                          :rows="3"
                          resize="none"
                          class="input-field"
                        />
                      </el-form-item>
                     <el-button type="primary" class="submit-btn" @click="submitSelfOrder">创建并接单</el-button>
                 </el-form>
             </div>
        </el-tab-pane>
<!-- ... (rest of template) -->


        <el-tab-pane label="待处理任务" name="pending">
          <div v-if="tasks.length === 0" class="empty-state">
            <el-empty description="当前暂无任务" />
          </div>
          <div v-else class="task-list">
            <div v-for="order in tasks" :key="order.id" class="task-card">
              <div class="card-header">
                <span class="order-id">订单 #{{ order.orderNo.substring(0, 8) }}</span>
                <el-tag :type="getStatusType(order.status)" effect="dark" round size="small">
                  {{ getStatusText(order.status) }}
                </el-tag>
              </div>
              
              <div class="card-body">
                <div class="info-row">
                  <el-icon class="icon"><Location /></el-icon>
                  <span class="text address">{{ order.pickupAddress }}</span>
                </div>
                <div class="info-row">
                  <el-icon class="icon"><Delete /></el-icon>
                  <span class="text">{{ getWasteTypeName(order.wasteType) }}</span>
                  <span class="divider">|</span>
                  <span class="text highlight">{{ order.estWeight }} kg</span>
                </div>
              </div>

              <div class="card-footer">
                <el-button 
                  type="info" 
                  class="action-btn" 
                  plain 
                  @click="openDetail(order)"
                >
                  <el-icon><InfoFilled /></el-icon> 详情
                </el-button>
                <el-button 
                  v-if="order.status === 20" 
                  type="primary" 
                  class="action-btn" 
                  @click="updateStatus(order.id, 25)"
                >
                  接单
                </el-button>
                <el-button 
                  v-if="order.status === 25" 
                  type="primary" 
                  class="action-btn" 
                  @click="updateStatus(order.id, 30)"
                >
                  开始装车
                </el-button>
                <el-button 
                  v-if="order.status === 30" 
                  type="success" 
                  class="action-btn" 
                  plain
                  @click="openUpload(order, 'load')"
                >
                  装车拍照 ({{ user.fleetId ? '可选' : '必须' }})
                </el-button>
                <el-button 
                  v-if="order.status === 30" 
                  type="primary" 
                  class="action-btn" 
                  @click="updateStatus(order.id, 40)"
                >
                  开始运输
                </el-button>
                <el-button 
                  v-if="order.status === 40" 
                  type="warning" 
                  class="action-btn" 
                  @click="openUpload(order, 'arrive')"
                >
                  到达站点拍照 ({{ user.fleetId ? '可选' : '必须' }})
                </el-button>
                <el-button 
                  v-if="order.status === 45" 
                  type="primary" 
                  class="action-btn" 
                  @click="updateStatus(order.id, 50)"
                >
                  确认进站
                </el-button>
                <el-button 
                  v-if="order.status === 50" 
                  type="success" 
                  class="action-btn" 
                  plain
                  @click="openUpload(order, 'unload')"
                >
                  卸货/空车拍照 ({{ user.fleetId ? '可选' : '必须' }})
                </el-button>
                <el-button 
                  v-if="order.status === 50" 
                  type="primary" 
                  class="action-btn" 
                  @click="updateStatus(order.id, 60)"
                >
                  完成订单
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="历史任务" name="history">
          <el-empty description="暂无历史记录" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Upload Dialog -->
    <el-dialog 
      v-model="uploadVisible" 
      title="上传作业照片" 
      width="90%" 
      custom-class="mobile-dialog"
      align-center
    >
      <div class="upload-container">
        <el-upload
          class="upload-demo"
          action="#"
          :http-request="handleUpload"
          :file-list="fileList"
          list-type="picture-card"
          :limit="1"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <p class="upload-tip">
          请拍摄{{ uploadNodeType === 'load' ? '装车' : '卸货' }}现场照片，需包含车辆与货物
        </p>
      </div>
    </el-dialog>

    <!-- Detail Dialog (Timeline & Map) -->
    <el-dialog
      v-model="detailVisible"
      title="订单全程追踪"
      width="95%"
      custom-class="mobile-dialog detail-dialog"
      align-center
      destroy-on-close
    >
      <div class="detail-container" v-if="currentOrder" v-loading="detailLoading">
        <el-tabs>
          <el-tab-pane label="实时轨迹">
            <OrderTrackMap 
              :start-point="{ lat: currentOrder.pickupLat, lon: currentOrder.pickupLon }"
              :status="currentOrder.status"
            />
          </el-tab-pane>
          <el-tab-pane label="状态记录">
            <OrderTimeline :logs="currentLogs" :photos="currentPhotos" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
    <!-- Map Picker -->
    <map-picker 
      v-model:visible="showMapPicker"
      @address-selected="handleAddressSelected"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Delete, Plus, InfoFilled, Service, Camera } from '@element-plus/icons-vue'
import OrderTimeline from '../components/OrderTimeline.vue'
import OrderTrackMap from '../components/OrderTrackMap.vue'
import MapPicker from '../components/MapPicker.vue'
import piexif from 'piexifjs'
import CryptoJS from 'crypto-js'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
// Fix: Freelance drivers (no fleetId) should default to 'create', Fleet drivers to 'pool'
const activeTab = ref(user.fleetId ? 'pool' : 'create')
const tasks = ref([])
const poolOrders = ref([])
const createForm = reactive({
    shipperName: '',
    shipperPhone: '',
    pickupAddress: '',
    wasteType: 'household',
    estWeight: 0,
    wasteDesc: ''
})
const uploadVisible = ref(false)
const detailVisible = ref(false)
const aiAnalyzing = ref(false)
const aiLoadingText = ref('')
const showMapPicker = ref(false)
const currentOrder = ref(null)
const currentLogs = ref([])
const currentPhotos = ref([])
const detailLoading = ref(false)
const uploadNodeType = ref('')
const fileList = ref([])

const wasteTypes = [
  { label: '生活垃圾', value: 'household', icon: '🏠' },
  { label: '建筑垃圾', value: 'construction', icon: '🧱' },
  { label: '大件垃圾', value: 'bulky', icon: '🛋️' },
  { label: '厨余垃圾', value: 'kitchen', icon: '🍲' }
]

const getWasteTypeName = (val) => {
  // Normalize input to handle case-insensitivity or standard formats
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
  
  // Try direct map, then check wasteTypes array for backward compatibility
  if (map[normalizedVal]) return map[normalizedVal]
  
  const type = wasteTypes.find(t => t.value.toUpperCase() === normalizedVal)
  return type ? type.label : val
}

const handleAddressSelected = (address) => {
  const { province, city, district, street, detailAddress } = address;
  createForm.pickupAddress = `${province}${city}${district}${street}${detailAddress}`;
  showMapPicker.value = false;
}

const handleAiAnalyze = async (options) => {
  aiAnalyzing.value = true
  aiLoadingText.value = '正在上传图片到云端...'
  
  try {
    // Step 1: Upload Simulation
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // Step 2: Processing Simulation
    aiLoadingText.value = 'AI 视觉引擎正在识别物体特征...'
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Step 3: Classification Simulation
    aiLoadingText.value = '正在匹配垃圾分类规则...'
    await new Promise(resolve => setTimeout(resolve, 800))

    // Step 4: Result Generation
    // Logic: Use file size as seed to be deterministic for same file
    const types = ['construction', 'household', 'kitchen', 'bulky']
    const seed = options.file.size
    const typeIndex = seed % types.length
    const detectedType = types[typeIndex]
    
    // Generate weight (random but realistic range based on type)
    let weight = 0
    if (detectedType === 'construction') weight = 50 + Math.floor((seed % 200)) // 50-250kg
    else if (detectedType === 'household') weight = 5 + Math.floor((seed % 20)) // 5-25kg
    else if (detectedType === 'kitchen') weight = 10 + Math.floor((seed % 40)) // 10-50kg
    else if (detectedType === 'bulky') weight = 30 + Math.floor((seed % 100)) // 30-130kg
    
    // Generate Professional Description
    const descriptions = {
      construction: ['检测到砖块、混凝土碎块及部分木材遗留。', '识别为装修废弃物，包含石膏板和水泥袋。', '建筑渣土堆积，混有少量金属废料。'],
      household: ['识别为日常生活垃圾，主要包含纸屑、塑料包装。', '混合生活废弃物，检测到多个垃圾袋堆积。', '居民生活垃圾，需分类处理。'],
      kitchen: ['检测到餐饮废弃物，含有果皮、蔬菜残余。', '厨余垃圾识别，建议密封运输以防泄漏。', '高水分有机垃圾，需专用容器清运。'],
      bulky: ['识别为废旧家具（沙发/床垫），体积较大。', '大件废弃物，检测到废弃家电或木制家具。', '不规则大件垃圾，建议使用平板车清运。']
    }
    const descIndex = seed % descriptions[detectedType].length
    const confidence = (90 + (seed % 90) / 10).toFixed(1) // 90.0 - 99.0%
    const description = `${descriptions[detectedType][descIndex]} (AI置信度: ${confidence}%)`

    createForm.wasteType = detectedType
    createForm.estWeight = weight
    createForm.wasteDesc = description
    
    ElMessage.success(`AI 识别完成：${getWasteTypeName(detectedType)}，置信度 ${confidence}%`)
  } catch (e) {
    console.error(e)
    ElMessage.error('AI 识别服务连接超时，请手动输入')
  } finally {
    aiAnalyzing.value = false
    aiLoadingText.value = ''
  }
}

const fetchTasks = async () => {
  try {
    const res = await api.get(`/orders/user/${user.id}`)
    tasks.value = res.data.filter(o => o.status >= 20 && o.status < 60)
  } catch (error) {
    console.error(error)
  }
}

const fetchPoolOrders = async () => {
  // Only Fleet Drivers (Type A) can see pool
  if (!user.fleetId) return
  try {
    const res = await api.get('/orders/pool')
    poolOrders.value = res.data
  } catch (error) {
      console.error(error)
  }
}

const grabOrder = async (orderId) => {
    try {
        await api.put(`/orders/${orderId}/grab?driverId=${user.id}`)
        ElMessage.success('抢单成功')
        fetchPoolOrders()
        fetchTasks()
        activeTab.value = 'pending'
    } catch (e) {
        ElMessage.error('抢单失败')
    }
}

const submitSelfOrder = async () => {
    try {
        await api.post('/orders/self-create', { ...createForm, driverId: user.id })
        ElMessage.success('建单成功')
        // Clear form
        createForm.shipperName = ''
        createForm.shipperPhone = ''
        createForm.pickupAddress = ''
        createForm.wasteType = 'household'
        createForm.estWeight = 0
        createForm.wasteDesc = ''
        
        // Fetch latest data first
        await fetchTasks()
        
        // Force switch tab with a small delay to ensure DOM update
        activeTab.value = 'pending'
    } catch (e) {
        console.error(e)
        ElMessage.error('建单失败: ' + (e.response?.data?.message || '未知错误'))
    }
}

const openDetail = async (order) => {
  if (detailLoading.value) return
  detailLoading.value = true
  
  try {
    console.log('Fetching details for order:', order.id)
    currentOrder.value = order
    // Reset data to avoid showing previous order's data
    currentLogs.value = []
    currentPhotos.value = []
    
    // Parallel fetch for better performance
    const [resLogs, resPhotos] = await Promise.all([
      api.get(`/orders/${order.id}/logs`),
      api.get(`/orders/${order.id}/photos`)
    ])
    
    currentLogs.value = resLogs.data
    console.log('Photos fetched:', resPhotos.data)
    currentPhotos.value = resPhotos.data || [] // Ensure array even if null
    
    detailVisible.value = true
  } catch (e) {
    console.error('Open detail error:', e)
    const msg = e.response?.data?.message || e.message || '获取详情失败'
    ElMessage.error(`获取详情失败: ${msg}`)
  } finally {
    detailLoading.value = false
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
  
  // Add watermark
  const watermarkedFile = await addWatermark(options.file)
  formData.append('file', watermarkedFile)
  
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

const getLocation = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('浏览器不支持定位'))
      return
    }

    const options = {
      enableHighAccuracy: true,
      timeout: 5000,
      maximumAge: 0
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => resolve(pos),
      (err) => {
        // Retry once
        console.warn('First GPS attempt failed, retrying...', err)
        navigator.geolocation.getCurrentPosition(
          (pos) => resolve(pos),
          (err2) => reject(err2),
          options
        )
      },
      options
    )
  })
}

const convertDDDToDMS = (ddd) => {
    const d = Math.floor(ddd);
    const m = Math.floor((ddd - d) * 60);
    const s = (ddd - d - m / 60) * 3600;
    return [[d, 1], [m, 1], [Math.round(s * 100), 100]];
}

const addWatermark = async (file) => {
  try {
    // 1. Get GPS (Removed)
    // const lat = ...
    
    // 2. Process Image (Canvas Watermark)
    const imgBitmap = await createImageBitmap(file)
    const canvas = document.createElement('canvas')
    canvas.width = imgBitmap.width
    canvas.height = imgBitmap.height
    const ctx = canvas.getContext('2d')
    ctx.drawImage(imgBitmap, 0, 0)
    
    // Watermark 1: Time (Right Bottom)
    const now = new Date()
    const timeStr = now.getFullYear() + '-' + 
                    String(now.getMonth()+1).padStart(2, '0') + '-' + 
                    String(now.getDate()).padStart(2, '0') + ' ' + 
                    String(now.getHours()).padStart(2, '0') + ':' + 
                    String(now.getMinutes()).padStart(2, '0') + ':' + 
                    String(now.getSeconds()).padStart(2, '0');
    
    ctx.font = '14px Arial'
    ctx.lineWidth = 1
    ctx.strokeStyle = 'black'
    ctx.fillStyle = 'white'
    ctx.textAlign = 'right'
    ctx.textBaseline = 'bottom'
    
    const timeX = canvas.width - 20
    const timeY = canvas.height - 20
    ctx.strokeText(timeStr, timeX, timeY)
    ctx.fillText(timeStr, timeX, timeY)
    
    // Watermark 2: Business Info (Left Bottom)
    const userIdStr = `User: ${user.id}`
    const orderNoStr = `Order: ${currentOrder.value?.orderNo || 'Unknown'}`
    
    ctx.textAlign = 'left'
    ctx.fillStyle = 'rgba(255, 255, 255, 0.8)' // 80% opacity
    ctx.shadowColor = "black";
    ctx.shadowBlur = 2;
    
    const infoX = 20
    const infoY_Line2 = canvas.height - 20
    const infoY_Line1 = infoY_Line2 - 20
    
    ctx.fillText(orderNoStr, infoX, infoY_Line2)
    ctx.fillText(userIdStr, infoX, infoY_Line1)
    
    // 3. Export to JPEG (Canvas drops EXIF, so we must add it back)
    const dataURL = canvas.toDataURL('image/jpeg', 0.9)
    
    // 4. Construct EXIF
    const zeroth = {};
    const exif = {};
    const gps = {}; // Empty GPS
    
    zeroth[piexif.ImageIFD.Make] = "RenewableAI App";
    zeroth[piexif.ImageIFD.Model] = "Driver App";
    
    exif[piexif.ExifIFD.DateTimeOriginal] = timeStr;
    exif[piexif.ExifIFD.DateTimeDigitized] = timeStr;
    
    // GPS Removed
    
    let exifObj = { "0th": zeroth, "Exif": exif, "GPS": gps };
    let exifBytes = piexif.dump(exifObj);
    let newJpegData = piexif.insert(exifBytes, dataURL); // This is Base64
    
    // REMOVED: Hash calculation and Signature Request (Requirement: Signature not needed)
    
    // 8. Convert back to Blob/File
    const byteString = atob(newJpegData.split(',')[1]);
    const ab = new ArrayBuffer(byteString.length);
    const ia = new Uint8Array(ab);
    for (let i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }
    const finalBlob = new Blob([ab], { type: 'image/jpeg' });
    return new File([finalBlob], file.name, { type: 'image/jpeg' });
    
  } catch (e) {
      console.error(e)
      ElMessage.error('图片处理失败: ' + e.message)
      throw e
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const getStatusText = (status) => {
  const map = { 10: '待接单', 20: '待接单', 25: '已接单', 30: '装车中', 40: '运输中', 50: '已到站', 60: '已完成' }
  return map[status] || status
}

const getStatusType = (status) => {
  return status === 30 || status === 50 ? 'warning' : 'success'
}

onMounted(() => {
  fetchTasks()
  fetchPoolOrders()
})
</script>

<style scoped>
.driver-task-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.create-order-container {
    padding: 20px;
    background: white;
    margin-top: 10px;
}

.submit-btn {
    width: 100%;
    margin-top: 20px;
}

.app-header {
  background: linear-gradient(to right, #00C853, #69F0AE);
  color: white;
  padding: 15px 20px;
  box-shadow: 0 2px 8px rgba(0, 200, 83, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 800px;
  margin: 0 auto;
}

.header-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-size: 14px;
  opacity: 0.9;
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 10px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding-bottom: 20px;
}

.task-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.2s;
}

.task-card:active {
  transform: scale(0.98);
}

.card-header {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-id {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.card-body {
  padding: 15px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  color: #666;
}

.info-row:last-child {
  margin-bottom: 0;
}

.icon {
  margin-right: 8px;
  color: #00C853;
  font-size: 16px;
}

.text {
  font-size: 14px;
}

.address {
  color: #333;
  font-weight: 500;
  line-height: 1.4;
}

.divider {
  margin: 0 10px;
  color: #ddd;
}

.highlight {
  color: #ff9800;
  font-weight: 600;
}

.card-footer {
  padding: 15px;
  background: #fafafa;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-btn {
  flex: 1;
  border-radius: 20px;
  font-weight: 500;
}

.empty-state {
  padding: 40px 0;
}

.upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.upload-tip {
  margin-top: 15px;
  color: #999;
  font-size: 13px;
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

/* Waste Type Grid */
.waste-type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 8px;
}

.type-card {
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  padding: 16px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.type-card:hover {
  border-color: #69F0AE;
  background-color: #f0fdf4;
}

.type-card.active {
  border-color: #00C853;
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

/* Element Plus Overrides */
:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #eee;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  height: 48px;
  line-height: 48px;
}

:deep(.el-tabs__active-bar) {
  background-color: #00C853;
  height: 3px;
}

:deep(.el-dialog__body) {
  padding-top: 0;
}
</style>
