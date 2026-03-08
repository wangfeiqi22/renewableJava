<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>智慧垃圾清运系统 - 注册</span>
        </div>
      </template>
      <el-form :model="registerForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.passwordHash" type="password"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="registerForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="registerForm.role" placeholder="请选择角色">
            <el-option label="个人用户" value="individual"></el-option>
            <el-option label="物业" value="property"></el-option>
            <el-option label="司机" value="driver"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" color="#00C851" style="width: 100%;">注册</el-button>
        </el-form-item>
        <div style="text-align: center;">
            <router-link to="/login">已有账号？去登录</router-link>
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
const registerForm = reactive({
  username: '',
  passwordHash: '',
  phone: '',
  role: 'individual'
})

const handleRegister = async () => {
  try {
    const res = await api.post('/auth/register', registerForm)
    if (res.status === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    }
  } catch (error) {
    // Error handled by interceptor
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #e8f5e9;
}
.register-card {
  width: 400px;
}
.card-header {
  text-align: center;
  font-weight: bold;
  color: #00C851;
}
</style>
