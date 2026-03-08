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
          <el-form-item label="角色">
            <el-select v-model="registerForm.role" placeholder="请选择您的角色" class="input-field" style="width: 100%">
              <el-option label="个人/VIP客户" value="vip"></el-option>
              <el-option label="物业" value="property"></el-option>
              <el-option label="街道办" value="street"></el-option>
              <el-option label="清运站" value="station"></el-option>
              <el-option label="车队" value="fleet"></el-option>
              <el-option label="司机" value="driver"></el-option>
            </el-select>
          </el-form-item>
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
import { reactive, ref } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const registerForm = reactive({
  username: '',
  passwordHash: '',
  phone: '',
  role: 'individual'
})

const handleRegister = async () => {
  if (!registerForm.username || !registerForm.passwordHash || !registerForm.phone) {
    ElMessage.warning('请填写所有必填项')
    return
  }

  loading.value = true
  try {
    const res = await api.post('/auth/register', registerForm)
    if (res.status === 200) {
      ElMessage.success('注册成功！部分角色需要等待管理员审核。')
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
