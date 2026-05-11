<template>
  <div class="project-create-container">
    <!-- Header -->
    <header class="page-header">
      <div class="header-left" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="header-title">新建项目</div>
      <div class="header-right"></div>
    </header>

    <!-- Form Content -->
    <div class="form-content">
      <!-- Project Name -->
      <div class="form-item">
        <label class="form-label">项目名称 <span class="required">*</span></label>
        <el-input
          v-model="formData.projectName"
          placeholder="请输入项目名称"
          class="form-input"
        />
      </div>

      <!-- Customer Name -->
      <div class="form-item">
        <label class="form-label">客户名称 <span class="required">*</span></label>
        <el-input
          v-model="formData.customerName"
          placeholder="请输入客户名称"
          class="form-input"
        />
      </div>

      <!-- Contact Phone -->
      <div class="form-item">
        <label class="form-label">联系电话 <span class="required">*</span></label>
        <el-input
          v-model="formData.contactPhone"
          placeholder="请输入联系电话"
          class="form-input"
        />
      </div>

      <!-- Pickup Address -->
      <div class="form-item">
        <label class="form-label">取货地址 <span class="required">*</span></label>
        <el-input
          v-model="formData.pickupAddress"
          placeholder="请输入取货地址"
          class="form-input"
        >
          <template #suffix>
            <el-icon class="icon"><Location /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- Disposal Site -->
      <div class="form-item">
        <label class="form-label">消纳点 <span class="required">*</span></label>
        <el-input
          v-model="formData.disposalSite"
          placeholder="请输入消纳点"
          class="form-input"
        />
      </div>

      <!-- Waste Type -->
      <div class="form-item">
        <label class="form-label">垃圾类型 <span class="required">*</span></label>
        <el-select
          v-model="formData.wasteType"
          placeholder="请选择垃圾类型"
          class="form-select"
        >
          <el-option label="生活垃圾" value="DOMESTIC" />
          <el-option label="建筑垃圾" value="CONSTRUCTION" />
          <el-option label="工业垃圾" value="INDUSTRIAL" />
          <el-option label="医疗垃圾" value="MEDICAL" />
          <el-option label="电子垃圾" value="ELECTRONIC" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </div>

      <!-- Estimated Volume -->
      <div class="form-item">
        <label class="form-label">预估方量 <span class="required">*</span></label>
        <el-input
          v-model.number="formData.estimatedVolume"
          type="number"
          placeholder="请输入预估方量"
          class="form-input"
        >
          <template #suffix>
            <span class="unit">方</span>
          </template>
        </el-input>
      </div>

      <!-- Estimated Trips -->
      <div class="form-item">
        <label class="form-label">预计车次</label>
        <el-input
          v-model.number="formData.estimatedTrips"
          type="number"
          placeholder="请输入预计车次"
          class="form-input"
        >
          <template #suffix>
            <span class="unit">次</span>
          </template>
        </el-input>
      </div>

      <!-- Quotation Amount -->
      <div class="form-item">
        <label class="form-label">报价金额 <span class="required">*</span></label>
        <el-input
          v-model.number="formData.quotationAmount"
          type="number"
          placeholder="请输入报价金额"
          class="form-input"
        >
          <template #prefix>
            <span class="prefix">¥</span>
          </template>
        </el-input>
      </div>

      <!-- LianDan Requirement -->
      <div class="form-item">
        <label class="form-label">联单需求</label>
        <el-radio-group v-model="formData.lianDanRequirement" class="lian-dan-group">
          <el-radio value="ELECTRONIC">需要电子联单</el-radio>
          <el-radio value="TECHNICAL">仅技术存证</el-radio>
          <el-radio value="NONE">无需联单</el-radio>
        </el-radio-group>
      </div>

      <!-- Remarks -->
      <div class="form-item">
        <label class="form-label">备注</label>
        <el-input
          v-model="formData.remarks"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
          class="form-textarea"
        />
      </div>
    </div>

    <!-- Bottom Bar -->
    <div class="bottom-bar">
      <el-button class="btn-cancel" @click="handleBack">取消</el-button>
      <el-button
        type="primary"
        class="btn-submit"
        @click="handleSubmit"
        :loading="submitting"
      >
        {{ submitting ? '提交中...' : '确认创建' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Location } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const submitting = ref(false)

const formData = ref({
  projectName: '',
  customerName: '',
  contactPhone: '',
  pickupAddress: '',
  disposalSite: '',
  wasteType: '',
  estimatedVolume: null,
  estimatedTrips: null,
  quotationAmount: null,
  lianDanRequirement: 'ELECTRONIC',
  remarks: ''
})

const handleBack = () => {
  router.back()
}

const validateForm = () => {
  if (!formData.value.projectName) {
    ElMessage.warning('请输入项目名称')
    return false
  }
  if (!formData.value.customerName) {
    ElMessage.warning('请输入客户名称')
    return false
  }
  if (!formData.value.contactPhone) {
    ElMessage.warning('请输入联系电话')
    return false
  }
  if (!formData.value.pickupAddress) {
    ElMessage.warning('请输入取货地址')
    return false
  }
  if (!formData.value.disposalSite) {
    ElMessage.warning('请输入消纳点')
    return false
  }
  if (!formData.value.wasteType) {
    ElMessage.warning('请选择垃圾类型')
    return false
  }
  if (!formData.value.estimatedVolume) {
    ElMessage.warning('请输入预估方量')
    return false
  }
  if (!formData.value.quotationAmount) {
    ElMessage.warning('请输入报价金额')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) return

  submitting.value = true
  try {
    const response = await api.post('/fleet/projects', formData.value)
    if (response.data.code === 200) {
      ElMessage.success('项目创建成功')
      router.push('/fleet/projects')
    } else {
      ElMessage.error(response.data.message || '创建失败')
    }
  } catch (error) {
    console.error('Failed to create project:', error)
    ElMessage.error(error.response?.data?.message || '创建失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.project-create-container {
  min-height: 100vh;
  background-color: #f5f7f9;
  padding-bottom: 80px;
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
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
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

.form-content {
  padding: 20px 16px;
}

.form-item {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.required {
  color: #f56c6c;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
}

.form-input :deep(.el-input__wrapper),
.form-select :deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: none;
  border: 1px solid #e8e8e8;
  font-size: 15px;
}

.form-input :deep(.el-input__wrapper:hover),
.form-select :deep(.el-input__wrapper:hover) {
  border-color: #d0d0d0;
}

.form-input :deep(.el-input__wrapper.is-focus),
.form-select :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
}

.form-textarea :deep(.el-textarea__inner) {
  border-radius: 8px;
  padding: 12px 16px;
  border: 1px solid #e8e8e8;
  font-size: 15px;
}

.form-textarea :deep(.el-textarea__inner:focus) {
  border-color: #409eff;
}

.prefix {
  color: #409eff;
  font-weight: 600;
}

.unit {
  color: #999;
  font-size: 14px;
}

.icon {
  color: #409eff;
  font-size: 16px;
}

.lian-dan-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.lian-dan-group :deep(.el-radio) {
  margin-right: 0;
  height: 36px;
  line-height: 36px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 0 16px;
  transition: all 0.2s;
}

.lian-dan-group :deep(.el-radio:hover) {
  border-color: #409eff;
}

.lian-dan-group :deep(.el-radio.is-checked) {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.lian-dan-group :deep(.el-radio__label) {
  font-size: 14px;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -1px 4px rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 12px;
  z-index: 100;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
  border: none;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e8e8e8;
  color: #333;
}

.btn-submit {
  background: #409eff;
  color: #fff;
}

.btn-submit:hover {
  background: #66b1ff;
}

.btn-submit:active {
  background: #3a8ee6;
}

.btn-submit:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}

@media (min-width: 768px) {
  .project-create-container {
    max-width: 480px;
    margin: 0 auto;
  }

  .lian-dan-group {
    flex-direction: row;
  }

  .lian-dan-group :deep(.el-radio) {
    flex: 1;
  }
}
</style>
