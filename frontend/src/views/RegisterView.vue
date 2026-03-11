<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-header">
        <h1 class="app-title">智慧垃圾清运</h1>
        <p class="app-subtitle">Smart Waste Management System</p>
      </div>
      
      <div class="card login-card">
        <h2 class="card-title">注册新账号</h2>
        <el-form :model="registerForm" class="login-form" size="large" label-position="top">
          <el-form-item label="用户名">
            <el-input 
              v-model="registerForm.username" 
              placeholder="请输入用户名" 
              prefix-icon="User"
              class="input-field"
            />
          </el-form-item>
          <el-form-item label="密码">
            <el-input 
              v-model="registerForm.passwordHash" 
              type="password" 
              placeholder="请输入密码" 
              prefix-icon="Lock"
              show-password
              class="input-field"
            />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input 
              v-model="registerForm.phone" 
              placeholder="请输入手机号" 
              prefix-icon="Iphone"
              class="input-field"
            />
          </el-form-item>
          <el-form-item label="角色类型">
            <el-select v-model="registerForm.role" placeholder="请选择您的角色" class="input-field" style="width: 100%">
              <el-option label="个人/商户（免审核）" value="vip"></el-option>
              <el-option label="物业公司（需审核）" value="property"></el-option>
              <el-option label="街道办（需审核）" value="street"></el-option>
              <el-option label="清运站管理员（需审核）" value="station"></el-option>
              <el-option label="车队管理员（需审核）" value="fleet"></el-option>
              <el-option label="司机" value="driver"></el-option>
            </el-select>
          </el-form-item>

          <!-- 企业 / 清运站类注册信息 -->
          <template v-if="['property','street','station','fleet'].includes(registerForm.role)">
            <el-alert
              type="info"
              :closable="false"
              show-icon
              description="企业、清运站和车队账号需提交完整资料并等待管理员审核通过后方可登录系统。"
              style="margin-bottom: 12px;"
            />
            <el-form-item label="单位名称">
              <el-input v-model="registerForm.companyName" placeholder="请输入企业/机构名称" />
            </el-form-item>
            <el-form-item label="联系人姓名">
              <el-input v-model="registerForm.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
            <el-form-item label="联系人电话">
              <el-input v-model="registerForm.contactPhone" placeholder="请输入联系人电话" />
            </el-form-item>
            <el-form-item label="单位地址">
              <el-input v-model="registerForm.address" placeholder="请输入详细地址" />
            </el-form-item>
            <el-form-item label="营业执照扫描件">
              <el-upload
                class="upload-block"
                :auto-upload="false"
                :limit="1"
                :file-list="businessLicenseList"
                :on-change="handleBusinessLicenseChange"
                accept=".jpg,.jpeg,.png,.pdf"
              >
                <el-button type="primary" plain>上传营业执照</el-button>
                <div class="upload-tip">支持 JPG/PNG/PDF，清晰无遮挡</div>
              </el-upload>
            </el-form-item>
            <el-form-item label="法人身份证扫描件">
              <el-upload
                class="upload-block"
                :auto-upload="false"
                :limit="1"
                :file-list="companyIdCardList"
                :on-change="handleCompanyIdCardChange"
                accept=".jpg,.jpeg,.png,.pdf"
              >
                <el-button type="primary" plain>上传身份证</el-button>
                <div class="upload-tip">请上传法人身份证正反面扫描件</div>
              </el-upload>
            </el-form-item>
          </template>

          <!-- 司机注册信息 -->
          <template v-if="registerForm.role === 'driver'">
            <el-alert
              type="info"
              :closable="false"
              show-icon
              description="类型A-车队司机需要管理员审核后才能登录；类型B-个人司机注册后可直接使用。"
              style="margin-bottom: 12px;"
            />
            <el-form-item label="司机类型">
              <el-radio-group v-model="registerForm.driverType">
                <el-radio label="A">类型A - 车队绑定司机（需审核）</el-radio>
                <el-radio label="B">类型B - 个人司机（免审核）</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="registerForm.driverType === 'A'" label="所属车队" required>
              <el-select
                v-model="registerForm.fleetId"
                placeholder="请选择要绑定的车队"
                style="width: 100%"
                filterable
                :loading="fleetLoading"
                :teleported="false"
              >
                <el-option
                  v-for="f in fleetOptions"
                  :key="f.id"
                  :label="String(f.name || '')"
                  :value="f.id"
                />
              </el-select>
              <div v-if="registerForm.driverType === 'A' && fleetOptions.length === 0 && !fleetLoading" class="upload-tip">暂无车队，请联系管理员先创建车队</div>
            </el-form-item>
            <el-form-item label="车牌号">
              <el-input v-model="registerForm.vehiclePlate" placeholder="如：京A88888" />
            </el-form-item>
            <el-form-item label="身份证号">
              <el-input v-model="registerForm.idNumber" placeholder="请输入身份证号" />
            </el-form-item>
            <el-form-item label="驾驶证号">
              <el-input v-model="registerForm.driverLicenseNumber" placeholder="请输入驾驶证号" />
            </el-form-item>
            <el-form-item label="身份证扫描件">
              <el-upload
                class="upload-block"
                :auto-upload="false"
                :limit="1"
                :file-list="driverIdCardList"
                :on-change="handleDriverIdCardChange"
                accept=".jpg,.jpeg,.png,.pdf"
              >
                <el-button type="primary" plain>上传身份证</el-button>
                <div class="upload-tip">请上传本人身份证正反面扫描件</div>
              </el-upload>
            </el-form-item>
            <el-form-item label="驾驶证扫描件">
              <el-upload
                class="upload-block"
                :auto-upload="false"
                :limit="1"
                :file-list="driverLicenseList"
                :on-change="handleDriverLicenseChange"
                accept=".jpg,.jpeg,.png,.pdf"
              >
                <el-button type="primary" plain>上传驾驶证</el-button>
                <div class="upload-tip">请上传本人驾驶证扫描件</div>
              </el-upload>
            </el-form-item>
          </template>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleRegister" 
              class="btn-primary btn-block" 
              :loading="loading"
            >
              注 册
            </el-button>
          </el-form-item>
          <div class="form-footer">
            <router-link to="/login" class="link">已有账号？立即登录</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const fleetOptions = ref([])
const fleetLoading = ref(false)
const registerForm = reactive({
  username: '',
  passwordHash: '',
  phone: '',
  role: 'vip',
  // 企业 / 站点信息
  companyName: '',
  address: '',
  contactName: '',
  contactPhone: '',
  // 司机信息
  driverType: 'B', // 默认个人司机，免审核
  fleetId: null,   // 类型A时必选
  vehiclePlate: '',
  idNumber: '',
  driverLicenseNumber: ''
})

// 附件上传状态
const businessLicense = ref(null)
const businessLicenseList = ref([])
const companyIdCard = ref(null)
const companyIdCardList = ref([])
const driverIdCard = ref(null)
const driverIdCardList = ref([])
const driverLicense = ref(null)
const driverLicenseList = ref([])

const handleBusinessLicenseChange = (file, fileList) => {
  businessLicense.value = file.raw
  businessLicenseList.value = fileList
}

const handleCompanyIdCardChange = (file, fileList) => {
  companyIdCard.value = file.raw
  companyIdCardList.value = fileList
}

const handleDriverIdCardChange = (file, fileList) => {
  driverIdCard.value = file.raw
  driverIdCardList.value = fileList
}

const handleDriverLicenseChange = (file, fileList) => {
  driverLicense.value = file.raw
  driverLicenseList.value = fileList
}

const fetchFleetOptions = async () => {
  fleetLoading.value = true
  try {
    // 使用与“站点运力”相同的接口，后端已允许未登录 GET /api/fleets
    const res = await api.get('/fleets')
    const raw = res.data
    // 兼容直接数组或 { content: [] } 等结构，并统一为 { id, name }
    const list = Array.isArray(raw) ? raw : (raw && raw.content) ? raw.content : []
    fleetOptions.value = list.map(f => ({
      id: f.id != null ? Number(f.id) : f.id,
      name: f.name != null ? String(f.name) : ''
    })).filter(f => f.id != null)
  } catch (e) {
    console.error('Fetch fleets failed:', e)
    fleetOptions.value = []
  } finally {
    fleetLoading.value = false
  }
}

onMounted(() => {
  fetchFleetOptions()
})

watch(() => [registerForm.role, registerForm.driverType], ([role, driverType]) => {
  if (role !== 'driver' || driverType !== 'A') {
    registerForm.fleetId = null
  } else {
    // 切换到“类型A-车队司机”时确保有车队数据
    if (fleetOptions.value.length === 0 && !fleetLoading.value) {
      fetchFleetOptions()
    }
  }
}, { immediate: true })

const handleRegister = async () => {
  if (!registerForm.username || !registerForm.passwordHash || !registerForm.phone) {
    ElMessage.warning('请填写用户名、密码和手机号')
    return
  }

  // 简单必填校验（不同角色）
  if (['property','street','station','fleet'].includes(registerForm.role)) {
    if (!registerForm.companyName || !registerForm.contactName || !registerForm.contactPhone) {
      ElMessage.warning('请完整填写单位名称和联系人信息')
      return
    }
    if (!businessLicense.value || !companyIdCard.value) {
      ElMessage.warning('请上传营业执照和法人身份证扫描件')
      return
    }
  }
  if (registerForm.role === 'driver') {
    if (registerForm.driverType === 'A' && !registerForm.fleetId) {
      ElMessage.warning('类型A-车队司机请选择所属车队')
      return
    }
    if (!registerForm.vehiclePlate || !registerForm.idNumber || !registerForm.driverLicenseNumber) {
      ElMessage.warning('请完整填写司机车辆和证件信息')
      return
    }
    if (!driverIdCard.value || !driverLicense.value) {
      ElMessage.warning('请上传身份证和驾驶证扫描件')
      return
    }
  }

  loading.value = true
  try {
    // 仅选择需要提交到后端的字段构造 user 对象
    const userPayload = {
      username: registerForm.username,
      passwordHash: registerForm.passwordHash,
      phone: registerForm.phone,
      role: registerForm.role,
      companyName: registerForm.companyName,
      address: registerForm.address,
      contactName: registerForm.contactName,
      contactPhone: registerForm.contactPhone,
      driverType: registerForm.driverType,
      fleetId: registerForm.driverType === 'A' ? registerForm.fleetId : null,
      vehiclePlate: registerForm.vehiclePlate,
      idNumber: registerForm.idNumber,
      driverLicenseNumber: registerForm.driverLicenseNumber
    }

    const formData = new FormData()
    formData.append(
      'user',
      new Blob([JSON.stringify(userPayload)], { type: 'application/json' })
    )

    if (businessLicense.value) {
      formData.append('businessLicense', businessLicense.value)
    }
    if (companyIdCard.value) {
      formData.append('companyIdCard', companyIdCard.value)
    }
    if (driverIdCard.value) {
      formData.append('driverIdCard', driverIdCard.value)
    }
    if (driverLicense.value) {
      formData.append('driverLicense', driverLicense.value)
    }

    const res = await api.post('/auth/register-with-docs', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.status === 200) {
      ElMessage.success('注册成功！部分角色需要等待管理员审核后才能登录。')
      router.push('/login')
    }
  } catch (error) {
    if (error.response && error.response.status === 409) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('注册失败，请稍后重试')
    }
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e0f7fa 0%, #ffffff 100%);
  position: relative;
  overflow: hidden;
}

/* Background decoration */
.login-container::before {
  content: '';
  position: absolute;
  top: -10%;
  right: -5%;
  width: 50%;
  height: 50%;
  background: radial-gradient(circle, rgba(0, 200, 83, 0.1) 0%, rgba(255, 255, 255, 0) 70%);
  border-radius: 50%;
  z-index: 0;
}

.login-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
  padding: 20px;
  animation: slideUp 0.5s ease-out;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.app-title {
  color: var(--color-primary);
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.app-subtitle {
  color: var(--color-text-secondary);
  font-size: 14px;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  padding: 40px 30px;
}

.card-title {
  text-align: center;
  margin-top: 0;
  margin-bottom: 30px;
  font-size: 20px;
  color: var(--color-text-main);
  font-weight: 600;
}

.btn-block {
  width: 100%;
  height: 44px;
  font-size: 16px;
  margin-top: 10px;
  border-radius: var(--radius-md);
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.link {
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: color 0.3s;
}

.link:hover {
  color: var(--color-primary);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
