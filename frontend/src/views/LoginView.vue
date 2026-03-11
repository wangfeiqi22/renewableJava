<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-header">
        <h1 class="app-title">智慧垃圾清运</h1>
        <p class="app-subtitle">Smart Waste Management System</p>
      </div>
      
      <div class="card login-card">
        <h2 class="card-title">欢迎登录</h2>
        <el-form :model="loginForm" class="login-form" size="large">
          <el-form-item>
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名" 
              prefix-icon="User"
              class="input-field"
            />
          </el-form-item>
          <el-form-item>
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              prefix-icon="Lock"
              show-password
              class="input-field"
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleLogin" 
              class="btn-primary btn-block" 
              :loading="loading"
            >
              登 录
            </el-button>
          </el-form-item>
          <el-form-item>
             <el-button type="success" plain class="btn-block" @click="router.push('/mall')">进入商城 (无需登录)</el-button>
          </el-form-item>
          <div class="form-footer">
            <a href="/register" class="link" @click.prevent="router.push('/register')">注册新账号</a>
            <span class="divider">|</span>
            <a href="#" class="link">忘记密码?</a>
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
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    const res = await api.post('/auth/login', loginForm)
    if (res.status === 200) {
      ElMessage.success('登录成功')
      localStorage.setItem('user', JSON.stringify(res.data.user))
      localStorage.setItem('token', res.data.token)
      
      // Redirect based on role
      const role = res.data.user.role
      if (role === 'admin') {
        router.push('/admin/dashboard')
      } else if (role === 'driver') {
        router.push('/driver/tasks')
      } else if (role === 'property') {
        router.push('/property/dashboard')
      } else if (role === 'station') {
        router.push('/station/work')
      } else {
        router.push('/order/create')
      }
    }
  } catch (error) {
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
  max-width: 400px;
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

.divider {
  margin: 0 10px;
  color: #ddd;
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
