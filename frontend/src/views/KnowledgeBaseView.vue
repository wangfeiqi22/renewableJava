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
          <router-link to="/admin/sf" class="nav-item">站点运力</router-link>
          <router-link to="/admin/kb" class="nav-item active">知识库</router-link>
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
          <h3>智能客服知识库</h3>
          <el-button type="primary" icon="Plus" @click="openDialog()">新增问答</el-button>
        </div>

        <el-table :data="kbList" style="width: 100%" stripe :header-cell-style="{background:'#f5f7fa',color:'#606266'}">
          <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip>
             <template #default="scope">
                <span style="font-weight: 500; color: #333">{{ scope.row.question }}</span>
             </template>
          </el-table-column>
          <el-table-column prop="answer" label="回答" min-width="300" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120">
             <template #default="scope">
                <el-tag size="small" effect="plain">{{ scope.row.category || '通用' }}</el-tag>
             </template>
          </el-table-column>
          <el-table-column prop="isActive" label="状态" width="100">
              <template #default="scope">
                  <el-tag :type="scope.row.isActive ? 'success' : 'info'" size="small" effect="dark">
                    {{ scope.row.isActive ? '启用' : '禁用' }}
                  </el-tag>
              </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="openDialog(scope.row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Edit Dialog -->
      <el-dialog v-model="dialogVisible" :title="form.id ? '编辑问答' : '新增问答'" width="600px">
        <el-form :model="form" label-width="80px">
          <el-form-item label="问题">
            <el-input v-model="form.question" placeholder="请输入用户可能询问的问题"></el-input>
          </el-form-item>
          <el-form-item label="回答">
            <el-input type="textarea" v-model="form.answer" rows="6" placeholder="请输入AI回复的内容"></el-input>
          </el-form-item>
          <el-row :gutter="20">
             <el-col :span="12">
                <el-form-item label="分类">
                    <el-input v-model="form.category" placeholder="如：业务咨询"></el-input>
                </el-form-item>
             </el-col>
             <el-col :span="12">
                <el-form-item label="状态">
                    <el-switch v-model="form.isActive" active-text="启用" inactive-text="禁用"></el-switch>
                </el-form-item>
             </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">保存</el-button>
          </span>
        </template>
      </el-dialog>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const kbList = ref([])
const dialogVisible = ref(false)
const form = reactive({
  id: null,
  question: '',
  answer: '',
  category: '',
  isActive: true
})

const fetchList = async () => {
  try {
    const res = await api.get('/admin/knowledge-base')
    kbList.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, question: '', answer: '', category: '', isActive: true })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await api.put(`/admin/knowledge-base/${form.id}`, form)
    } else {
      await api.post('/admin/knowledge-base', form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该条目吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/admin/knowledge-base/${id}`)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
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
  fetchList()
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
</style>
