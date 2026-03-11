<template>
  <div class="admin-container">
    <header class="admin-header">
      <div class="header-left">
        <div class="logo">
          <span class="icon">📊</span>
          <span class="text">监管驾驶舱</span>
        </div>
        <nav class="nav-links">
          <router-link to="/admin/dashboard" class="nav-item">概览</router-link>
          <router-link to="/admin/sf" class="nav-item">站点运力</router-link>
          <router-link to="/admin/kb" class="nav-item">知识库</router-link>
          <router-link to="/admin/users" class="nav-item active">人员审核</router-link>
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
        <div class="table-header">
          <h3>待审核用户</h3>
          <div style="display:flex; gap: 10px; align-items:center;">
            <el-tag type="info" effect="plain">待审核：{{ pendingUsers.length }}</el-tag>
            <el-button type="primary" @click="fetchPending" :loading="loading">刷新</el-button>
          </div>
        </div>

        <el-table :data="pendingUsers" style="width: 100%" stripe :header-cell-style="{background:'#f5f7fa',color:'#606266'}">
          <el-table-column prop="username" label="用户名" width="140" />
          <el-table-column prop="role" label="角色" width="120">
            <template #default="scope">
              <el-tag size="small" effect="plain">{{ roleText(scope.row.role) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column label="关键信息" min-width="220" show-overflow-tooltip>
            <template #default="scope">
              <div v-if="scope.row.role === 'driver'">
                司机类型：{{ scope.row.driverType || '-' }}｜车牌：{{ scope.row.vehiclePlate || '-' }}
              </div>
              <div v-else>
                单位：{{ scope.row.companyName || '-' }}｜联系人：{{ scope.row.contactName || '-' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="附件" min-width="260">
            <template #default="scope">
              <div class="doc-links">
                <template v-if="isCompanyRole(scope.row.role)">
                  <a v-if="scope.row.companyLicenseUrl" :href="scope.row.companyLicenseUrl" target="_blank" rel="noreferrer">营业执照</a>
                  <span v-else class="muted">营业执照(缺)</span>
                  <span class="sep">|</span>
                  <a v-if="scope.row.companyLegalIdCardUrl" :href="scope.row.companyLegalIdCardUrl" target="_blank" rel="noreferrer">法人身份证</a>
                  <span v-else class="muted">法人身份证(缺)</span>
                </template>
                <template v-else-if="scope.row.role === 'driver'">
                  <a v-if="scope.row.driverIdCardUrl" :href="scope.row.driverIdCardUrl" target="_blank" rel="noreferrer">身份证</a>
                  <span v-else class="muted">身份证(缺)</span>
                  <span class="sep">|</span>
                  <a v-if="scope.row.driverLicenseUrl" :href="scope.row.driverLicenseUrl" target="_blank" rel="noreferrer">驾驶证</a>
                  <span v-else class="muted">驾驶证(缺)</span>
                </template>
                <template v-else>
                  <span class="muted">-</span>
                </template>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="提交时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="success" size="small" @click="approve(scope.row)" :loading="actionLoadingId === scope.row.id">通过</el-button>
              <el-button type="danger" size="small" plain @click="openReject(scope.row)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-dialog v-model="rejectVisible" title="驳回原因" width="520px">
        <el-form>
          <el-form-item label="原因">
            <el-input v-model="rejectRemark" type="textarea" :rows="4" placeholder="请输入驳回原因（会反馈给用户）" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="rejectVisible=false">取消</el-button>
          <el-button type="danger" @click="reject" :loading="actionLoadingId === (currentUser?.id || -1)">确认驳回</el-button>
        </template>
      </el-dialog>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const pendingUsers = ref([])
const actionLoadingId = ref(null)

const rejectVisible = ref(false)
const rejectRemark = ref('')
const currentUser = ref(null)

const isCompanyRole = (role) => ['property', 'street', 'station', 'fleet'].includes(role)

const roleText = (role) => {
  const map = {
    admin: '管理员',
    vip: '个人/商户',
    individual: '个体户',
    property: '物业',
    street: '街道办',
    station: '清运站',
    fleet: '车队',
    driver: '司机'
  }
  return map[role] || role
}

const fetchPending = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const res = await api.get('/admin/users/pending')
    pendingUsers.value = res.data || []
  } catch (e) {
    console.error(e)
    ElMessage.error('获取待审核用户失败')
  } finally {
    loading.value = false
  }
}

const approve = async (user) => {
  actionLoadingId.value = user.id
  try {
    await api.put(`/admin/users/${user.id}/approve`)
    ElMessage.success('已通过审核')
    fetchPending()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  } finally {
    actionLoadingId.value = null
  }
}

const openReject = (user) => {
  currentUser.value = user
  rejectRemark.value = user.auditRemark || ''
  rejectVisible.value = true
}

const reject = async () => {
  if (!currentUser.value) return
  actionLoadingId.value = currentUser.value.id
  try {
    await api.put(`/admin/users/${currentUser.value.id}/reject`, { remark: rejectRemark.value })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    currentUser.value = null
    rejectRemark.value = ''
    fetchPending()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  } finally {
    actionLoadingId.value = null
  }
}

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  } else if (cmd === 'user') {
    router.push('/order/create')
  }
}

onMounted(() => {
  fetchPending()
})
</script>

<style scoped>
.doc-links {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.sep { color: #c0c4cc; }
.muted { color: #909399; }
</style>

