<template>
  <div class="order-create-page">
    <div class="page-header">
      <h2>我要预约清运</h2>
    </div>

    <el-steps :active="currentStep" align-center class="order-steps">
      <el-step title="填写信息" />
      <el-step title="智能匹配" />
      <el-step title="费用确认" />
      <el-step title="完成预约" />
    </el-steps>

    <div class="step-content">
      <!-- Step 1: 填写信息 -->
      <el-card v-if="currentStep === 0" class="step-card">
        <template #header>
          <span>填写清运信息</span>
        </template>

        <el-form :model="orderForm" label-width="120px" class="order-form">
          <el-form-item label="垃圾类型" required>
            <el-select v-model="orderForm.wasteType" placeholder="请选择垃圾类型" size="large">
              <el-option
                v-for="wt in wasteTypes"
                :key="wt.code"
                :label="wt.name"
                :value="wt.code"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="预估方量" required>
            <el-input-number
              v-model="orderForm.estVolume"
              :min="0.1"
              :precision="1"
              :step="0.5"
              size="large"
            />
            <span class="unit">立方米</span>
          </el-form-item>

          <el-form-item label="取货地址" required>
            <el-input
              v-model="orderForm.pickupAddress"
              placeholder="请输入详细地址"
              size="large"
            />
          </el-form-item>

          <el-form-item label="联系电话" required>
            <el-input
              v-model="orderForm.contactPhone"
              placeholder="请输入联系电话"
              size="large"
            />
          </el-form-item>

          <el-form-item label="附加服务">
            <el-checkbox v-model="orderForm.forkliftRequired">需要叉车服务 (+¥100)</el-checkbox>
            <el-checkbox v-model="orderForm.containerRequired">
              需要收集箱 (+¥{{ orderForm.containerCount * 50 }})
            </el-checkbox>
          </el-form-item>

          <el-form-item v-if="orderForm.containerRequired" label="收集箱数量">
            <el-input-number
              v-model="orderForm.containerCount"
              :min="1"
              :max="10"
              size="default"
            />
          </el-form-item>

          <el-form-item label="备注">
            <el-input
              v-model="orderForm.remark"
              type="textarea"
              :rows="3"
              placeholder="补充说明"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" @click="nextStep">
              下一步：智能匹配
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- Step 2: 智能匹配 -->
      <el-card v-if="currentStep === 1" class="step-card">
        <template #header>
          <span>智能匹配清运站</span>
        </template>

        <div class="matching-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="垃圾类型">{{ selectedWasteTypeName }}</el-descriptions-item>
            <el-descriptions-item label="预估方量">{{ orderForm.estVolume }} 立方米</el-descriptions-item>
            <el-descriptions-item label="取货地址" :span="2">{{ orderForm.pickupAddress }}</el-descriptions-item>
          </el-descriptions>

          <div class="matching-result" v-if="matchedStation">
            <el-alert
              title="已为您匹配到最近的清运站"
              type="success"
              :closable="false"
              show-icon
            />

            <el-card class="station-card" shadow="hover">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="清运站名称">
                  <strong>{{ matchedStation.stationName }}</strong>
                </el-descriptions-item>
                <el-descriptions-item label="距离">
                  {{ matchedStation.distance }} 公里
                </el-descriptions-item>
                <el-descriptions-item label="地址">
                  {{ matchedStation.stationAddress }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </div>

          <div class="loading-state" v-else-if="isMatching">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>正在匹配最近的清运站...</span>
          </div>

          <div class="error-state" v-else-if="matchingError">
            <el-alert
              :title="matchingError"
              type="error"
              :closable="false"
            />
          </div>

          <div class="action-buttons">
            <el-button size="large" @click="prevStep">上一步</el-button>
            <el-button
              type="primary"
              size="large"
              :disabled="!matchedStation || isMatching"
              @click="calculateFee"
            >
              下一步：查看费用
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- Step 3: 费用确认 -->
      <el-card v-if="currentStep === 2" class="step-card">
        <template #header>
          <span>费用明细</span>
        </template>

        <div class="fee-breakdown" v-if="feeResult">
          <el-descriptions title="费用计算明细" :column="1" border class="fee-descriptions">
            <el-descriptions-item label="清运站">
              {{ feeResult.stationName }}
            </el-descriptions-item>
            <el-descriptions-item label="运输距离">
              {{ feeResult.distance }} 公里
            </el-descriptions-item>
            <el-descriptions-item label="预估方量">
              {{ orderForm.estVolume }} 立方米
            </el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">费用明细</el-divider>

          <div class="fee-items">
            <div class="fee-item">
              <span class="fee-label">运输费用</span>
              <span class="fee-value">¥{{ feeResult.transportFee }}</span>
            </div>
            <div class="fee-item">
              <span class="fee-label">消纳费用</span>
              <span class="fee-value">¥{{ feeResult.disposalFee }}</span>
            </div>
            <div class="fee-item">
              <span class="fee-label">搬运费用</span>
              <span class="fee-value">¥{{ feeResult.handlingFee }}</span>
            </div>
            <el-divider />
            <div class="fee-item total">
              <span class="fee-label">总费用</span>
              <span class="fee-value total-price">¥{{ feeResult.totalFee }}</span>
            </div>
          </div>

          <el-alert
            title="费用说明"
            type="info"
            :closable="false"
            show-icon
            class="fee-notice"
          >
            <template #default>
              <ul class="fee-explanation">
                <li>运输费用 = 起步价¥200 + (距离-5km) × ¥18/km</li>
                <li>消纳费用 = 垃圾类型单价 × 预估方量</li>
                <li>搬运费用 = 叉车服务¥100 + 收集箱¥50/个</li>
              </ul>
            </template>
          </el-alert>

          <div class="action-buttons">
            <el-button size="large" @click="prevStep">上一步</el-button>
            <el-button type="primary" size="large" @click="confirmOrder">
              确认预约
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- Step 4: 完成 -->
      <el-card v-if="currentStep === 3" class="step-card">
        <template #header>
          <span>预约成功</span>
        </template>

        <div class="success-content">
          <el-result
            icon="success"
            title="预约成功"
            sub-title="请保持电话畅通，清运站将尽快与您联系"
          >
            <template #extra>
              <el-button type="primary" @click="goToOrderList">查看订单</el-button>
              <el-button @click="resetForm">继续预约</el-button>
            </template>
          </el-result>

          <el-card class="order-summary" shadow="never">
            <el-descriptions title="订单信息" :column="2" border>
              <el-descriptions-item label="订单号">{{ createdOrderNo }}</el-descriptions-item>
              <el-descriptions-item label="预约时间">{{ createdTime }}</el-descriptions-item>
              <el-descriptions-item label="清运站">{{ feeResult?.stationName }}</el-descriptions-item>
              <el-descriptions-item label="预计费用">
                <span class="total-price">¥{{ feeResult?.totalFee }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const api = window.axios || {}

const currentStep = ref(0)
const wasteTypes = ref([])

const orderForm = reactive({
  wasteType: '',
  estVolume: 1,
  pickupAddress: '',
  contactPhone: '',
  forkliftRequired: false,
  containerRequired: false,
  containerCount: 1,
  remark: '',
  pickupLat: null,
  pickupLon: null
})

const matchedStation = ref(null)
const isMatching = ref(false)
const matchingError = ref('')
const feeResult = ref(null)
const isCalculating = ref(false)

const createdOrderNo = ref('')
const createdTime = ref('')

onMounted(async () => {
  await loadWasteTypes()
})

const loadWasteTypes = async () => {
  try {
    const res = await api.get('/api/stations/waste-types')
    wasteTypes.value = res.data.data || []
  } catch (error) {
    console.error('加载垃圾类型失败', error)
  }
}

const selectedWasteTypeName = computed(() => {
  const wt = wasteTypes.value.find(w => w.code === orderForm.wasteType)
  return wt ? wt.name : orderForm.wasteType
})

const nextStep = async () => {
  if (!validateStep1()) return
  currentStep.value = 1
  await matchStation()
}

const validateStep1 = () => {
  if (!orderForm.wasteType) {
    ElMessage.warning('请选择垃圾类型')
    return false
  }
  if (!orderForm.estVolume || orderForm.estVolume <= 0) {
    ElMessage.warning('请输入预估方量')
    return false
  }
  if (!orderForm.pickupAddress) {
    ElMessage.warning('请输入取货地址')
    return false
  }
  if (!orderForm.contactPhone) {
    ElMessage.warning('请输入联系电话')
    return false
  }
  return true
}

const matchStation = async () => {
  isMatching.value = true
  matchingError.value = ''

  try {
    const res = await api.get('/api/stations/match', {
      params: {
        wasteType: orderForm.wasteType,
        lat: orderForm.pickupLat || 39.9042,
        lon: orderForm.pickupLon || 116.4074,
        limit: 1
      }
    })

    const stations = res.data.data || []
    if (stations.length > 0) {
      matchedStation.value = stations[0]
    } else {
      matchingError.value = '未找到可接收该垃圾类型的清运站'
    }
  } catch (error) {
    matchingError.value = '匹配失败：' + (error.message || '未知错误')
  } finally {
    isMatching.value = false
  }
}

const calculateFee = async () => {
  if (!matchedStation.value) return

  isCalculating.value = true

  try {
    const res = await api.post('/api/orders/calculate-fee', {
      wasteType: orderForm.wasteType,
      estVolume: orderForm.estVolume,
      pickupLat: orderForm.pickupLat || 39.9042,
      pickupLon: orderForm.pickupLon || 116.4074,
      forkliftRequired: orderForm.forkliftRequired,
      containerCount: orderForm.containerRequired ? orderForm.containerCount : 0
    })

    feeResult.value = res.data.data
    currentStep.value = 2
  } catch (error) {
    ElMessage.error('费用计算失败：' + (error.message || '未知错误'))
  } finally {
    isCalculating.value = false
  }
}

const confirmOrder = async () => {
  try {
    const res = await api.post('/api/orders', {
      wasteType: orderForm.wasteType,
      estVolume: orderForm.estVolume,
      pickupAddress: orderForm.pickupAddress,
      contactPhone: orderForm.contactPhone,
      remark: orderForm.remark,
      matchedStationId: matchedStation.value.stationId
    })

    createdOrderNo.value = res.data.data?.orderNo || 'ORD' + Date.now()
    createdTime.value = new Date().toLocaleString()
    currentStep.value = 3
    ElMessage.success('预约成功')
  } catch (error) {
    ElMessage.error('预约失败：' + (error.message || '未知错误'))
  }
}

const prevStep = () => {
  currentStep.value--
}

const goToOrderList = () => {
  router.push('/orders')
}

const resetForm = () => {
  Object.assign(orderForm, {
    wasteType: '',
    estVolume: 1,
    pickupAddress: '',
    contactPhone: '',
    forkliftRequired: false,
    containerRequired: false,
    containerCount: 1,
    remark: '',
    pickupLat: null,
    pickupLon: null
  })
  matchedStation.value = null
  feeResult.value = null
  currentStep.value = 0
}
</script>

<style scoped>
.order-create-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 30px 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.order-steps {
  margin-bottom: 40px;
}

.step-content {
  margin-top: 20px;
}

.step-card {
  margin-bottom: 20px;
}

.order-form {
  max-width: 600px;
  margin: 0 auto;
}

.unit {
  margin-left: 10px;
  color: #909399;
}

.matching-info {
  max-width: 700px;
  margin: 0 auto;
}

.matching-result {
  margin-top: 20px;
}

.station-card {
  margin-top: 20px;
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 40px 0;
  color: #909399;
}

.loading-state .el-icon {
  font-size: 32px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
}

.fee-breakdown {
  max-width: 600px;
  margin: 0 auto;
}

.fee-descriptions {
  margin-bottom: 20px;
}

.fee-items {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  font-size: 16px;
}

.fee-label {
  color: #606266;
}

.fee-value {
  color: #303133;
  font-weight: 500;
}

.fee-item.total {
  font-size: 18px;
  padding-top: 15px;
}

.total-price {
  color: #f56c6c;
  font-weight: 700;
  font-size: 20px;
}

.fee-notice {
  margin-top: 20px;
}

.fee-explanation {
  margin: 0;
  padding-left: 20px;
  font-size: 14px;
  color: #606266;
}

.success-content {
  text-align: center;
}

.order-summary {
  margin-top: 30px;
  text-align: left;
}
</style>
