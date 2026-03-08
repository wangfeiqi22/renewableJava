<template>
  <div class="order-create-container">
    <el-header class="header">
      <div class="logo">智慧垃圾清运</div>
      <div class="user-info" v-if="user">
        欢迎, {{ user.username }}
        <el-button size="small" type="danger" link @click="handleLogout">退出</el-button>
      </div>
      <el-button size="small" circle color="white" style="color: #00C851; font-weight: bold;" @click="$router.push('/chat')">AI</el-button>
    </el-header>

    <el-main>
      <el-card class="order-card">
        <template #header>
          <div class="card-header">
            <span>创建清运单</span>
          </div>
        </template>
        <el-form :model="orderForm" label-width="100px">
          <el-form-item label="起运地址">
            <el-input v-model="orderForm.pickupAddress" placeholder="请输入详细地址"></el-input>
          </el-form-item>
          <el-form-item label="垃圾类型">
            <el-select v-model="orderForm.wasteType" placeholder="请选择垃圾类型">
               <el-option label="生活垃圾" value="household"></el-option>
               <el-option label="建筑垃圾" value="construction"></el-option>
               <el-option label="大件垃圾" value="bulky"></el-option>
               <el-option label="厨余垃圾" value="kitchen"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="预估重量(kg)">
             <el-input-number v-model="orderForm.estWeight" :precision="2" :step="0.1"></el-input-number>
          </el-form-item>
          <el-form-item label="垃圾描述">
            <el-input type="textarea" v-model="orderForm.wasteDesc"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitOrder" color="#00C851">提交订单</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="history-card" style="margin-top: 20px;">
        <template #header>
          <span>历史订单</span>
        </template>
        <el-table :data="orderHistory" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="wasteType" label="类型" width="100" />
          <el-table-column prop="pickupAddress" label="地址" />
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" />
        </el-table>
      </el-card>
    </el-main>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const orderForm = reactive({
  pickupAddress: '',
  wasteType: '',
  estWeight: 0,
  wasteDesc: '',
  creatorId: user.value.id,
  type: 1 // Smart Order
})
const orderHistory = ref([])

const fetchOrders = async () => {
  if (!user.value.id) return
  try {
    const res = await api.get(`/orders/user/${user.value.id}`)
    orderHistory.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const submitOrder = async () => {
  if (!user.value.id) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await api.post('/orders', { ...orderForm, creatorId: user.value.id })
    if (res.status === 200) {
      ElMessage.success('订单提交成功')
      fetchOrders()
    }
  } catch (error) {
    // Error handled by interceptor
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const getStatusText = (status) => {
  const map = { 10: '待接单', 20: '已接单', 30: '装车中', 60: '已完成' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  return status === 60 ? 'success' : status === 10 ? 'warning' : 'primary'
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.header {
  background-color: #00C851;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.logo {
  font-size: 20px;
  font-weight: bold;
}
.order-card, .history-card {
  max-width: 800px;
  margin: 0 auto;
}
</style>
