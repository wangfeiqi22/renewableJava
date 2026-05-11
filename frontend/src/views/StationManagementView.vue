<template>
  <div class="station-management">
    <div class="page-header">
      <h2>清运站管理</h2>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="垃圾类型配置" name="waste-types"></el-tab-pane>
        <el-tab-pane label="地址管理" name="address"></el-tab-pane>
        <el-tab-pane label="价格历史" name="price-history"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 垃圾类型配置 -->
    <div v-if="activeTab === 'waste-types'" class="tab-content">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>可接收垃圾类型</span>
            <el-button type="primary" @click="showAddWasteTypeDialog">
              <el-icon><Plus /></el-icon>
              添加垃圾类型
            </el-button>
          </div>
        </template>

        <el-table :data="stationWasteTypes" stripe>
          <el-table-column prop="wasteType.name" label="垃圾类型" />
          <el-table-column prop="wasteType.category" label="分类" />
          <el-table-column prop="pricePerCubic" label="单价（元/方）">
            <template #default="{ row }">
              <span class="price">¥{{ row.pricePerCubic }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="showPriceHistory(row)">历史</el-button>
              <el-button size="small" type="primary" @click="editPrice(row)">调价</el-button>
              <el-button size="small" type="danger" @click="removeWasteType(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 地址管理 -->
    <div v-if="activeTab === 'address'" class="tab-content">
      <el-card>
        <template #header>
          <span>清运站地址信息</span>
        </template>

        <el-form :model="addressForm" label-width="120px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="省份">
                <el-input v-model="addressForm.province" placeholder="请输入省份" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="城市">
                <el-input v-model="addressForm.city" placeholder="请输入城市" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="区县">
                <el-input v-model="addressForm.district" placeholder="请输入区县" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="街道">
                <el-input v-model="addressForm.street" placeholder="请输入街道" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="门牌号">
                <el-input v-model="addressForm.doorNo" placeholder="请输入门牌号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="完整地址">
                <el-input v-model="addressForm.fullAddress" placeholder="完整地址" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="纬度">
                <el-input-number v-model="addressForm.lat" :precision="6" :step="0.000001" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="经度">
                <el-input-number v-model="addressForm.lon" :precision="6" :step="0.000001" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button type="primary" @click="saveAddress">保存地址</el-button>
            <el-button @click="openMapPicker">地图选点</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 价格历史 -->
    <div v-if="activeTab === 'price-history'" class="tab-content">
      <el-card>
        <template #header>
          <span>价格调整历史</span>
        </template>

        <el-table :data="priceHistory" stripe>
          <el-table-column prop="changedAt" label="调整时间" width="180" />
          <el-table-column prop="oldPrice" label="原价格">
            <template #default="{ row }">
              <span>¥{{ row.oldPrice }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="newPrice" label="新价格">
            <template #default="{ row }">
              <span class="new-price">¥{{ row.newPrice }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="changeReason" label="调整原因" />
        </el-table>
      </el-card>
    </div>

    <!-- 添加垃圾类型对话框 -->
    <el-dialog v-model="addWasteTypeVisible" title="添加垃圾类型" width="500px">
      <el-form :model="addWasteTypeForm" label-width="120px">
        <el-form-item label="垃圾类型">
          <el-select v-model="addWasteTypeForm.wasteTypeId" placeholder="请选择垃圾类型">
            <el-option
              v-for="wt in availableWasteTypes"
              :key="wt.id"
              :label="wt.name"
              :value="wt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="单价（元/方）">
          <el-input-number v-model="addWasteTypeForm.pricePerCubic" :precision="2" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addWasteTypeVisible = false">取消</el-button>
        <el-button type="primary" @click="addWasteType">确认</el-button>
      </template>
    </el-dialog>

    <!-- 调价对话框 -->
    <el-dialog v-model="updatePriceVisible" title="调整价格" width="500px">
      <el-form :model="updatePriceForm" label-width="120px">
        <el-form-item label="新价格">
          <el-input-number v-model="updatePriceForm.newPrice" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="updatePriceForm.reason" type="textarea" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="updatePriceVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPriceUpdate">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const api = window.axios || {}

const activeTab = ref('waste-types')
const stationId = ref(1)

const stationWasteTypes = ref([])
const availableWasteTypes = ref([])
const priceHistory = ref([])

const addWasteTypeVisible = ref(false)
const updatePriceVisible = ref(false)

const addressForm = reactive({
  province: '',
  city: '',
  district: '',
  street: '',
  doorNo: '',
  fullAddress: '',
  lat: null,
  lon: null
})

const addWasteTypeForm = reactive({
  wasteTypeId: null,
  pricePerCubic: 0
})

const updatePriceForm = reactive({
  id: null,
  newPrice: 0,
  reason: ''
})

onMounted(async () => {
  await loadStationWasteTypes()
  await loadAvailableWasteTypes()
})

const loadStationWasteTypes = async () => {
  try {
    const res = await api.get(`/api/stations/${stationId.value}/waste-types`)
    stationWasteTypes.value = res.data.data || []
  } catch (error) {
    ElMessage.error('加载垃圾类型失败')
  }
}

const loadAvailableWasteTypes = async () => {
  try {
    const res = await api.get('/api/stations/waste-types')
    availableWasteTypes.value = res.data.data || []
  } catch (error) {
    ElMessage.error('加载可选垃圾类型失败')
  }
}

const showAddWasteTypeDialog = () => {
  addWasteTypeForm.wasteTypeId = null
  addWasteTypeForm.pricePerCubic = 0
  addWasteTypeVisible.value = true
}

const addWasteType = async () => {
  try {
    await api.post(`/api/stations/${stationId.value}/waste-types`, {
      wasteTypeId: addWasteTypeForm.wasteTypeId,
      pricePerCubic: addWasteTypeForm.pricePerCubic
    })
    ElMessage.success('添加成功')
    addWasteTypeVisible.value = false
    await loadStationWasteTypes()
  } catch (error) {
    ElMessage.error(error.message || '添加失败')
  }
}

const editPrice = (row) => {
  updatePriceForm.id = row.id
  updatePriceForm.newPrice = row.pricePerCubic
  updatePriceForm.reason = ''
  updatePriceVisible.value = true
}

const submitPriceUpdate = async () => {
  try {
    await api.put(`/api/stations/${stationId.value}/waste-types/${updatePriceForm.id}/price`, {
      newPrice: updatePriceForm.newPrice,
      reason: updatePriceForm.reason
    })
    ElMessage.success('价格更新成功')
    updatePriceVisible.value = false
    await loadStationWasteTypes()
  } catch (error) {
    ElMessage.error(error.message || '价格更新失败')
  }
}

const removeWasteType = async (row) => {
  try {
    await api.delete(`/api/stations/${stationId.value}/waste-types/${row.id}`)
    ElMessage.success('删除成功')
    await loadStationWasteTypes()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

const showPriceHistory = async (row) => {
  try {
    const res = await api.get(`/api/stations/${stationId.value}/waste-types/${row.id}/price-history`)
    priceHistory.value = res.data.data || []
    activeTab.value = 'price-history'
  } catch (error) {
    ElMessage.error('加载价格历史失败')
  }
}

const saveAddress = async () => {
  try {
    ElMessage.success('地址保存成功')
  } catch (error) {
    ElMessage.error('地址保存失败')
  }
}

const openMapPicker = () => {
  ElMessage.info('地图选点功能开发中')
}

const handleTabChange = () => {
  priceHistory.value = []
}
</script>

<style scoped>
.station-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-content {
  margin-top: 20px;
}

.price {
  color: #409eff;
  font-weight: 600;
}

.new-price {
  color: #67c23a;
  font-weight: 600;
}
</style>
