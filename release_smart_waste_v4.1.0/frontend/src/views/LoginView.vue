<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>智慧垃圾清运系统 - 登录</span>
        </div>
      </template>
      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" color="#00C851" style="width: 100%;">登录</el-button>
        </el-form-item>
        <div style="text-align: center;">
            <router-link to="/register">没有账号？去注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    const res = await api.post('/auth/login', loginForm)
    if (res.status === 200) {
      ElMessage.success('登录成功')
      localStorage.setItem('user', JSON.stringify(res.data.user))
      localStorage.setItem('token', res.data.token)
      router.push('/order/create')
    }
  } catch (error) {
    // Error handled by interceptor
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #e8f5e9;
}
.login-card {
  width: 400px;
}
.card-header {
  text-align: center;
  font-weight: bold;
  color: #00C851;
}
</style>
