<template>
  <div class="eco-order-cards">
    <!-- 垃圾分类卡片 -->
    <div class="info-card">
      <div class="card-header">
        <el-icon class="header-icon"><Delete /></el-icon>
        <span class="header-title">垃圾分类</span>
      </div>
      <div class="waste-type-grid">
        <div 
          v-for="type in wasteTypes" 
          :key="type.value"
          class="type-card"
          :class="{ active: selectedWasteType === type.value }"
          @click="selectWasteType(type.value)"
        >
          <div class="type-icon">{{ type.icon }}</div>
          <span class="type-name">{{ type.label }}</span>
        </div>
      </div>
    </div>

    <!-- 清运地址卡片 -->
    <div class="info-card">
      <div class="card-header">
        <el-icon class="header-icon"><LocationInformation /></el-icon>
        <span class="header-title">清运地址</span>
      </div>
      
      <div class="address-box">
        <div class="address-content">
          <div class="main-address">{{ mainAddress || '上海市浦东新区张江高科技园区' }}</div>
          <div class="sub-address">已自动定位</div>
        </div>
        <div class="edit-btn" @click="$emit('edit-address')">
          <el-icon><EditPen /></el-icon> 修改
        </div>
      </div>

      <div class="detail-input-box">
        <input 
          type="text" 
          class="detail-input" 
          v-model="detailAddress" 
          placeholder="详细地址（如：XX小区X栋X单元）"
          @input="$emit('update:detailAddress', $event.target.value)"
        />
      </div>
    </div>

    <!-- 垃圾方数卡片 -->
    <div class="info-card">
      <div class="card-header">
        <el-icon class="header-icon volume-icon"><Box /></el-icon>
        <span class="header-title">垃圾方数</span>
      </div>

      <!-- 方数增减器 -->
      <div class="volume-stepper">
        <button class="step-btn" @click="decreaseVolume" :disabled="volume <= 0">
          <el-icon><Minus /></el-icon>
        </button>
        <div class="volume-display">
          <input 
            type="number" 
            class="volume-input" 
            v-model.number="volume" 
            @input="handleVolumeInput"
            @blur="handleVolumeBlur"
            min="0"
            step="0.1"
          />
          <span class="volume-unit">方</span>
        </div>
        <button class="step-btn" @click="increaseVolume">
          <el-icon><Plus /></el-icon>
        </button>
      </div>

      <!-- 车次预估信息框 -->
      <div class="estimate-box">
        <div class="estimate-header">
          <div class="estimate-title">
            <el-icon><Van /></el-icon> 车次预估
          </div>
          <div class="truck-standard">标准6方车</div>
        </div>

        <div class="estimate-data-grid">
          <div class="data-item">
            <div class="data-value blue">{{ requiredTrucks }}</div>
            <div class="data-label">需车次</div>
          </div>
          <div class="data-item">
            <div class="data-value">{{ fullTrucks }}</div>
            <div class="data-label">满车</div>
          </div>
          <div class="data-item">
            <div class="data-value red">{{ remainingVolume }}</div>
            <div class="data-label">末车方数</div>
          </div>
        </div>

        <!-- 进度条可视化 -->
        <div class="progress-bar-container">
          <div class="progress-segments">
            <div 
              v-for="i in fullTrucks" 
              :key="'full-'+i" 
              class="segment full-segment"
            ></div>
            <div 
              v-if="remainingVolume > 0" 
              class="segment partial-segment"
              :style="{ width: (remainingVolume / 6 * 100) + '%' }"
            ></div>
            <div 
              v-if="remainingVolume > 0"
              class="segment empty-segment"
              :style="{ width: ((6 - remainingVolume) / 6 * 100) + '%' }"
            ></div>
          </div>
        </div>
        
        <div class="estimate-footer-text">
          <span v-if="isCalculating" style="color: #999;"><el-icon class="is-loading"><Loading /></el-icon> 计算中...</span>
          <span v-else>{{ truckSuggestion }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { LocationInformation, Box, EditPen, Minus, Plus, Van, Delete, Loading } from '@element-plus/icons-vue'
import api from '../api'

const props = defineProps({
  mainAddress: {
    type: String,
    default: '上海市浦东新区张江高科技园区'
  },
  modelValue: { // 对应 detailAddress
    type: String,
    default: ''
  },
  volumeValue: {
    type: Number,
    default: 14
  },
  wasteType: {
    type: String,
    default: 'household'
  }
})

const emit = defineEmits(['update:modelValue', 'update:volumeValue', 'update:wasteType', 'edit-address'])

const detailAddress = ref(props.modelValue)
const volume = ref(props.volumeValue)
const selectedWasteType = ref(props.wasteType)

const requiredTrucks = ref(0)
const fullTrucks = ref(0)
const remainingVolume = ref(0)
const truckSuggestion = ref('')
const isCalculating = ref(false)
let debounceTimer = null

const wasteTypes = [
  { label: '生活垃圾', value: 'household', icon: '🏠' },
  { label: '建筑垃圾', value: 'construction', icon: '🧱' },
  { label: '大件垃圾', value: 'bulky', icon: '🛋️' },
  { label: '厨余垃圾', value: 'kitchen', icon: '🍲' }
]

const selectWasteType = (val) => {
  selectedWasteType.value = val
  emit('update:wasteType', val)
}

const fetchTruckEstimation = async (val) => {
  if (val === '' || val < 0) return
  isCalculating.value = true
  try {
    const res = await api.get(`/orders/estimate-trucks?volume=${val}`)
    requiredTrucks.value = res.data.requiredTrucks
    fullTrucks.value = res.data.fullTrucks
    remainingVolume.value = res.data.remainingVolume
    truckSuggestion.value = res.data.suggestion
  } catch (error) {
    console.error('Failed to estimate trucks:', error)
  } finally {
    isCalculating.value = false
  }
}

watch(() => props.modelValue, (newVal) => { detailAddress.value = newVal })
watch(() => props.volumeValue, (newVal) => { 
  volume.value = newVal 
  fetchTruckEstimation(newVal)
})
watch(() => props.wasteType, (newVal) => { selectedWasteType.value = newVal })

const handleVolumeInput = () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    if (volume.value >= 0) {
      emit('update:volumeValue', volume.value)
      fetchTruckEstimation(volume.value)
    }
  }, 500)
}

const handleVolumeBlur = () => {
  if (volume.value < 0 || volume.value === '') {
    volume.value = 0
  }
  emit('update:volumeValue', volume.value)
  fetchTruckEstimation(volume.value)
}

const decreaseVolume = () => {
  if (volume.value > 0) {
    volume.value = Math.max(0, volume.value - 1)
    emit('update:volumeValue', volume.value)
    fetchTruckEstimation(volume.value)
  }
}

const increaseVolume = () => {
  volume.value++
  emit('update:volumeValue', volume.value)
  fetchTruckEstimation(volume.value)
}

onMounted(() => {
  fetchTruckEstimation(volume.value)
})
</script>

<style scoped>
.eco-order-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 基础卡片样式 */
.info-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  padding: 20px;
  border: 1px solid #f0f2f5;
}

/* 头部样式 */
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}

.header-icon {
  font-size: 18px;
  color: #0dc8a3; /* 品牌绿 */
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #333333;
}

/* --- 垃圾分类区块 --- */
.waste-type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.type-card {
  border: 1px solid #eef0f2;
  border-radius: 8px;
  padding: 12px 4px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.type-card:hover {
  border-color: #0dc8a3;
}

.type-card.active {
  border-color: #0dc8a3;
  background-color: #f0fdf4; /* Very light green */
}

.type-icon {
  font-size: 20px;
  margin-bottom: 4px;
  line-height: 1;
}

.type-name {
  font-size: 12px;
  color: #333333;
}
.type-card.active .type-name {
  color: #0dc8a3;
  font-weight: 500;
}

/* --- 清运地址区块 --- */
.address-box {
  background-color: #f7f8fa;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.main-address {
  font-size: 14px;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.sub-address {
  font-size: 12px;
  color: #999999;
}

.edit-btn {
  font-size: 13px;
  color: #666666;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 4px 8px;
}
.edit-btn:hover {
  color: #0dc8a3;
}

.detail-input-box {
  width: 100%;
}

.detail-input {
  width: 100%;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 14px;
  color: #333;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.detail-input::placeholder {
  color: #cccccc;
}
.detail-input:focus {
  border-color: #0dc8a3;
}

/* --- 垃圾方数区块 --- */
.volume-stepper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 10px;
}

.step-btn {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  border: 1px solid #eef0f2;
  background: #ffffff;
  color: #666;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}
.step-btn:hover:not(:disabled) {
  border-color: #0dc8a3;
  color: #0dc8a3;
}
.step-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f9f9f9;
}

.volume-display {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.volume-input {
  width: 80px;
  text-align: center;
  font-size: 40px;
  font-weight: bold;
  color: #0dc8a3;
  line-height: 1;
  border: none;
  background: transparent;
  outline: none;
  padding: 0;
  margin: 0;
  -moz-appearance: textfield;
}

.volume-input::-webkit-outer-spin-button,
.volume-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.volume-input:focus {
  border-bottom: 2px solid #0dc8a3;
}

.volume-unit {
  font-size: 14px;
  color: #666666;
  font-weight: normal;
}

/* --- 车次预估区块 --- */
.estimate-box {
  background-color: #f4f8ff; /* 极淡的蓝色背景 */
  border-radius: 12px;
  padding: 16px;
}

.estimate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.estimate-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4078f2;
  font-weight: 500;
}

.truck-standard {
  font-size: 12px;
  color: #666666;
  background: #e8f0fe;
  padding: 2px 8px;
  border-radius: 4px;
}

.estimate-data-grid {
  display: flex;
  background: #ffffff;
  border-radius: 8px;
  padding: 12px 0;
  margin-bottom: 16px;
}

.data-item {
  flex: 1;
  text-align: center;
  border-right: 1px solid #f0f0f0;
}
.data-item:last-child {
  border-right: none;
}

.data-value {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 4px;
  color: #333333;
}
.data-value.blue {
  color: #4078f2;
}
.data-value.red {
  color: #ff4d4f;
}

.data-label {
  font-size: 12px;
  color: #999999;
}

/* 进度条 */
.progress-bar-container {
  margin-bottom: 12px;
}

.progress-segments {
  display: flex;
  gap: 4px;
  height: 6px;
  border-radius: 3px;
  overflow: hidden;
}

.segment {
  height: 100%;
}
.full-segment {
  flex: 1;
  background-color: #4078f2;
  border-radius: 3px;
}
.partial-segment {
  background-color: #4078f2;
  border-radius: 3px 0 0 3px;
}
.empty-segment {
  background-color: #dbe6fe;
  border-radius: 0 3px 3px 0;
}

.estimate-footer-text {
  text-align: center;
  font-size: 12px;
  color: #666666;
}
</style>
