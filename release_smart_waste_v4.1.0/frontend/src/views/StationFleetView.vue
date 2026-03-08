<template>
  <div class="sf-container">
    <el-header class="header">
      <div class="title">站点与运力管理</div>
      <div>
        <el-button type="info" size="small" @click="router.push('/admin/dashboard')">返回驾驶舱</el-button>
      </div>
    </el-header>

    <el-main>
      <el-tabs v-model="activeTab">
        <!-- Stations Tab -->
        <el-tab-pane label="站点管理" name="stations">
          <div class="actions">
            <el-button type="primary" @click="openStationDialog()">新增站点</el-button>
          </div>
          <el-table :data="stations" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="type" label="类型">
                <template #default="scope">
                    <el-tag :type="scope.row.type === 1 ? 'success' : 'warning'">{{ scope.row.type === 1 ? '清运站' : '中转站' }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="address" label="地址" />
            <el-table-column label="坐标">
                <template #default="scope">
                    {{ scope.row.lat }}, {{ scope.row.lon }}
                </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Fleets Tab -->
        <el-tab-pane label="车队管理" name="fleets">
          <div class="actions">
            <el-button type="primary" @click="openFleetDialog()">新增车队</el-button>
          </div>
          <el-table :data="fleets" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="车队名称" />
            <el-table-column label="车辆数" width="100">
                <template #default="scope">
                    <el-button size="small" link @click="viewVehicles(scope.row.id)">查看车辆</el-button>
                </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <!-- Station Dialog -->
      <el-dialog v-model="stationDialogVisible" title="新增站点">
        <el-form :model="stationForm" label-width="80px">
          <el-form-item label="名称">
            <el-input v-model="stationForm.name"></el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="stationForm.type">
                <el-option label="清运站" :value="1"></el-option>
                <el-option label="中转站" :value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="stationForm.address"></el-input>
          </el-form-item>
          <el-form-item label="纬度">
            <el-input-number v-model="stationForm.lat" :precision="6"></el-input-number>
          </el-form-item>
          <el-form-item label="经度">
            <el-input-number v-model="stationForm.lon" :precision="6"></el-input-number>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="stationDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitStation">保存</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- Fleet Dialog -->
      <el-dialog v-model="fleetDialogVisible" title="新增车队">
        <el-form :model="fleetForm" label-width="80px">
          <el-form-item label="名称">
            <el-input v-model="fleetForm.name"></el-input>
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
      <el-dialog v-model="vehicleDialogVisible" title="车辆列表" width="70%">
         <div class="actions">
            <el-button type="primary" size="small" @click="openVehicleForm()">新增车辆</el-button>
         </div>
         <el-table :data="vehicles" style="width: 100%; margin-top: 10px;">
            <el-table-column prop="plateNo" label="车牌号" />
            <el-table-column prop="type" label="车型" />
            <el-table-column prop="loadCapacity" label="载重(吨)" />
         </el-table>

         <div v-if="showVehicleForm" style="margin-top: 20px; border-top: 1px solid #eee; padding-top: 10px;">
            <h4>新增车辆</h4>
            <el-form :inline="true" :model="vehicleForm">
                <el-form-item label="车牌">
                    <el-input v-model="vehicleForm.plateNo"></el-input>
                </el-form-item>
                <el-form-item label="车型">
                    <el-input v-model="vehicleForm.type"></el-input>
                </el-form-item>
                <el-form-item label="载重">
                    <el-input-number v-model="vehicleForm.loadCapacity" :precision="1"></el-input-number>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="submitVehicle">添加</el-button>
                </el-form-item>
            </el-form>
         </div>
      </el-dialog>

    </el-main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

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
    // Need to implement GET /api/stations in Backend Controller if not exists
    // Actually I implemented FleetController and FleetService, check StationController?
    // I implemented StationController in Turn 2.
    try {
        const res = await api.get('/stations?type=0') // 0 for all?
        stations.value = res.data
    } catch (e) {
        // Mock if fails
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

onMounted(() => {
    fetchStations()
    fetchFleets()
})
</script>

<style scoped>
.sf-container {
  background-color: #f0f2f5;
  min-height: 100vh;
}
.header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.title { font-size: 18px; font-weight: bold; }
.actions { background: white; padding: 15px; border-radius: 4px; }
</style>
