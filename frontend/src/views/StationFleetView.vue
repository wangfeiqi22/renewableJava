<template>
  <div class="admin-container">
    <!-- Top Navbar -->
    <header class="admin-header">
      <div class="header-left">
        <div class="logo">
          <span class="icon">📊</span>
          <span class="text">监管驾驶舱</span>
        </div>
        <nav class="nav-links">
          <router-link to="/admin/dashboard" class="nav-item">概览</router-link>
          <router-link to="/admin/sf" class="nav-item active">站点运力</router-link>
          <router-link to="/admin/kb" class="nav-item">知识库</router-link>
        </nav>
      </div>
      <div class="header-right">
        <div class="action-btn">🔔</div>
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-profile">
            <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <span class="name">管理员</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="user">返回用户端</el-dropdown-item>
              <el-dropdown-item divided command="logout" style="color: var(--color-danger)">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="admin-main container">
      <div class="card content-card">
        <el-tabs v-model="activeTab" class="admin-tabs">
          <!-- Stations Tab -->
          <el-tab-pane label="站点管理" name="stations">
            <div class="table-header">
              <h3>站点列表</h3>
              <el-button type="primary" icon="Plus" @click="openStationDialog()">新增站点</el-button>
            </div>
            <el-table :data="stations" style="width: 100%" stripe :header-cell-style="{background:'#f5f7fa',color:'#606266'}">
              <el-table-column prop="name" label="名称" min-width="120" />
              <el-table-column prop="type" label="类型" width="100">
                  <template #default="scope">
                      <el-tag :type="scope.row.type === 1 ? 'success' : 'warning'" effect="light">
                        {{ scope.row.type === 1 ? '清运站' : '中转站' }}
                      </el-tag>
                  </template>
              </el-table-column>
              <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
              <el-table-column label="坐标" width="180">
                  <template #default="scope">
                      <el-icon><Location /></el-icon> {{ scope.row.lat }}, {{ scope.row.lon }}
                  </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default>
                  <el-button link type="primary" size="small">编辑</el-button>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- Fleets Tab -->
          <el-tab-pane label="车队管理" name="fleets">
            <div class="table-header">
              <h3>车队列表</h3>
              <el-button type="primary" icon="Plus" @click="openFleetDialog()">新增车队</el-button>
            </div>
            <el-table :data="fleets" style="width: 100%" stripe :header-cell-style="{background:'#f5f7fa',color:'#606266'}">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="车队名称" min-width="150">
                <template #default="scope">
                  <span style="font-weight: 500">{{ scope.row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column label="车辆数" width="120">
                  <template #default="scope">
                      <el-button size="small" type="primary" plain round @click="viewVehicles(scope.row.id)">
                        查看车辆 <el-icon class="el-icon--right"><Van /></el-icon>
                      </el-button>
                  </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default>
                  <el-button link type="primary" size="small">编辑</el-button>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- Station Dialog -->
      <el-dialog v-model="stationDialogVisible" title="新增站点" width="500px">
        <el-form :model="stationForm" label-width="80px">
          <el-form-item label="名称">
            <el-input v-model="stationForm.name" placeholder="请输入站点名称"></el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="stationForm.type" style="width: 100%">
                <el-option label="清运站" :value="1"></el-option>
                <el-option label="中转站" :value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="stationForm.address" placeholder="请输入详细地址"></el-input>
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="纬度">
                <el-input-number v-model="stationForm.lat" :precision="6" style="width: 100%"></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="经度">
                <el-input-number v-model="stationForm.lon" :precision="6" style="width: 100%"></el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="stationDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitStation">保存</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- Fleet Dialog -->
      <el-dialog v-model="fleetDialogVisible" title="新增车队" width="400px">
        <el-form :model="fleetForm" label-width="80px">
          <el-form-item label="名称">
            <el-input v-model="fleetForm.name" placeholder="请输入车队名称"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="fleetDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitFleet">保存</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- Vehicles Dialog -->
      <el-dialog v-model="vehicleDialogVisible" title="车辆列表" width="800px">
         <div class="table-header">
            <h4>当前车队车辆</h4>
            <el-button type="primary" size="small" icon="Plus" @click="openVehicleForm()">新增车辆</el-button>
         </div>
         <el-table :data="vehicles" style="width: 100%; margin-top: 10px;" border>
            <el-table-column prop="plateNo" label="车牌号" />
            <el-table-column prop="type" label="车型" />
            <el-table-column prop="loadCapacity" label="载重(吨)" />
         </el-table>

         <div v-if="showVehicleForm" class="vehicle-form-container">
            <h4>新增车辆</h4>
            <el-form :inline="true" :model="vehicleForm" class="vehicle-form">
                <el-form-item label="车牌">
                    <el-input v-model="vehicleForm.plateNo" placeholder="如: 京A88888"></el-input>
                </el-form-item>
                <el-form-item label="车型">
                    <el-input v-model="vehicleForm.type" placeholder="如: 厢式货车"></el-input>
                </el-form-item>
                <el-form-item label="载重">
                    <el-input-number v-model="vehicleForm.loadCapacity" :precision="1" :step="0.5"></el-input-number>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="submitVehicle">添加</el-button>
                    <el-button @click="showVehicleForm = false">取消</el-button>
                </el-form-item>
            </el-form>
         </div>
      </el-dialog>

    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Van, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('stations')
const stations = ref([])
const fleets = ref([])
const vehicles = ref([])

const stationDialogVisible = ref(false)
const fleetDialogVisible = ref(false)
const vehicleDialogVisible = ref(false)
const showVehicleForm = ref(false)
const currentFleetId = ref(null)

const stationForm = reactive({ name: '', type: 1, address: '', lat: 39.9, lon: 116.4 })
const fleetForm = reactive({ name: '' })
const vehicleForm = reactive({ plateNo: '', type: '', loadCapacity: 5.0 })

const fetchStations = async () => {
    try {
        const res = await api.get('/stations?type=0') 
        stations.value = res.data
    } catch (e) {
        console.error(e)
    }
}

const fetchFleets = async () => {
    try {
        const res = await api.get('/fleets')
        fleets.value = res.data
    } catch (e) { console.error(e) }
}

const viewVehicles = async (fleetId) => {
    currentFleetId.value = fleetId
    try {
        const res = await api.get(`/fleets/${fleetId}/vehicles`)
        vehicles.value = res.data
        vehicleDialogVisible.value = true
        showVehicleForm.value = false
    } catch (e) { console.error(e) }
}

const openStationDialog = () => { stationDialogVisible.value = true }
const openFleetDialog = () => { fleetDialogVisible.value = true }
const openVehicleForm = () => { showVehicleForm.value = true }

const submitStation = async () => {
    try {
        await api.post('/stations', stationForm)
        ElMessage.success('保存成功')
        stationDialogVisible.value = false
        fetchStations()
    } catch (e) { ElMessage.error('失败') }
}

const submitFleet = async () => {
    try {
        await api.post('/fleets', fleetForm)
        ElMessage.success('保存成功')
        fleetDialogVisible.value = false
        fetchFleets()
    } catch (e) { ElMessage.error('失败') }
}

const submitVehicle = async () => {
    try {
        await api.post('/fleets/vehicles', { ...vehicleForm, fleetId: currentFleetId.value })
        ElMessage.success('保存成功')
        showVehicleForm.value = false
        viewVehicles(currentFleetId.value)
    } catch (e) { ElMessage.error('失败') }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  } else if (command === 'user') {
    router.push('/order/create')
  }
}

onMounted(() => {
    fetchStations()
    fetchFleets()
})
</script>

<style scoped>
.admin-container {
  background-color: #f5f7fa;
  min-height: 100vh;
}

.admin-header {
  background: white;
  height: 64px;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-item {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  padding: 8px 0;
  position: relative;
  transition: color 0.3s;
}

.nav-item:hover, .nav-item.active {
  color: var(--color-primary);
  font-weight: 500;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--color-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-btn {
  font-size: 20px;
  cursor: pointer;
  color: #909399;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.admin-main {
  padding: 24px;
}

.content-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  min-height: 600px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.vehicle-form-container {
  margin-top: 20px;
  padding: 20px;
  background: #f9fafc;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.vehicle-form-container h4 {
  margin-top: 0;
  margin-bottom: 15px;
}
</style>
