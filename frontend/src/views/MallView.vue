<template>
  <div class="mall-container">
    <div class="app-header">
      <div class="header-content">
        <h1 class="header-title">再生资源商城</h1>
        <div class="user-profile" v-if="user">
          <span class="username">{{ user.username }}</span>
          <el-button size="small" type="danger" plain round @click="handleLogout" class="logout-btn">退出</el-button>
        </div>
      </div>
    </div>

    <div class="main-content">
      <el-tabs v-model="activeTab" class="custom-tabs" stretch>
        <el-tab-pane label="商品列表" name="products">
          <div class="filter-bar">
            <el-select v-model="selectedStation" placeholder="选择清运站" clearable @change="fetchProducts">
              <el-option v-for="station in stations" :key="station.id" :label="station.name" :value="station.id" />
            </el-select>
            <el-button type="primary" icon="Search" @click="fetchProducts">查询</el-button>
          </div>

          <div v-if="products.length === 0" class="empty-state">
            <el-empty description="暂无商品" />
          </div>
          
          <div v-else class="product-grid">
            <el-card v-for="product in products" :key="product.id" class="product-card" :body-style="{ padding: '0px' }">
              <div class="product-image">
                <el-icon :size="60" color="#909399"><Box /></el-icon>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <div class="product-price">
                  <span class="currency">¥</span>
                  <span class="amount">{{ product.price }}</span>
                  <span class="unit">/ {{ product.unit }}</span>
                </div>
                <div class="product-stock">库存: {{ product.stock || 0 }} {{ product.unit }}</div>
                <div class="product-station">
                  <el-icon><Location /></el-icon> {{ getStationName(product.stationId) }}
                </div>
              </div>
              <div class="product-actions">
                <el-button type="primary" block @click="addToCart(product)">加入购物车</el-button>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <el-tab-pane label="购物车" name="cart">
          <div v-if="cart.length === 0" class="empty-state">
            <el-empty description="购物车为空" />
          </div>
          <div v-else>
            <el-table :data="cart" style="width: 100%">
              <el-table-column prop="name" label="商品名称" />
              <el-table-column prop="price" label="单价" width="120" />
              <el-table-column label="数量" width="180">
                <template #default="scope">
                  <el-input-number v-model="scope.row.quantity" :min="1" :max="scope.row.stock" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="小计" width="120">
                <template #default="scope">
                  ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button type="danger" icon="Delete" circle size="small" @click="removeFromCart(scope.$index)" />
                </template>
              </el-table-column>
            </el-table>
            <div class="cart-footer">
              <div class="total-price">
                总计: <span class="highlight">¥{{ cartTotal }}</span>
              </div>
              <el-button type="primary" size="large" @click="checkout">提交订单</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Box, Location, Search, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const activeTab = ref('products')
const products = ref([])
const stations = ref([])
const selectedStation = ref('')
const cart = ref([])

const cartTotal = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
})

const fetchStations = async () => {
  try {
    const res = await api.get('/stations')
    stations.value = res.data.filter(s => s.type === 1) // Only clearance stations sell products
  } catch (error) {
    console.error(error)
  }
}

const fetchProducts = async () => {
  try {
    let url = '/mall/products'
    if (selectedStation.value) {
      url = `/mall/station/${selectedStation.value}/products`
    }
    const res = await api.get(url)
    products.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const getStationName = (id) => {
  const station = stations.value.find(s => s.id === id)
  return station ? station.name : '未知站点'
}

const addToCart = (product) => {
  const existing = cart.value.find(item => item.id === product.id)
  if (existing) {
    existing.quantity++
  } else {
    cart.value.push({ ...product, quantity: 1 })
  }
  ElMessage.success('已加入购物车')
}

const removeFromCart = (index) => {
  cart.value.splice(index, 1)
}

const checkout = () => {
  ElMessage.success('订单提交成功 (演示)')
  cart.value = []
  activeTab.value = 'products'
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  fetchStations()
  fetchProducts()
})
</script>

<style scoped>
.mall-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.app-header {
  background: linear-gradient(to right, #409EFF, #79bbff);
  color: white;
  padding: 15px 20px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
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

.main-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 10px;
}

.filter-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.product-card {
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  height: 150px;
  background-color: #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;
}

.product-info {
  padding: 15px;
}

.product-name {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  color: #f56c6c;
  margin-bottom: 5px;
}

.currency {
  font-size: 14px;
}

.amount {
  font-size: 20px;
  font-weight: bold;
}

.unit {
  font-size: 12px;
  color: #909399;
}

.product-stock, .product-station {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.product-actions {
  padding: 10px 15px;
  border-top: 1px solid #ebeef5;
}

.cart-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: white;
  border-radius: 4px;
}

.total-price {
  font-size: 16px;
}

.highlight {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}
</style>
