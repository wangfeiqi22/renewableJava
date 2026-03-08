<template>
  <div class="kb-container">
    <el-header class="header">
      <div class="title">知识库管理</div>
      <div>
          <el-button type="info" size="small" @click="router.push('/admin/dashboard')">返回驾驶舱</el-button>
      </div>
    </el-header>

    <el-main>
      <div class="actions">
        <el-button type="primary" @click="openDialog()">新增问答</el-button>
      </div>

      <el-table :data="kbList" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="question" label="问题" />
        <el-table-column prop="answer" label="回答" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="isActive" label="状态" width="100">
            <template #default="scope">
                <el-tag :type="scope.row.isActive ? 'success' : 'info'">{{ scope.row.isActive ? '启用' : '禁用' }}</el-tag>
            </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Edit Dialog -->
      <el-dialog v-model="dialogVisible" :title="form.id ? '编辑问答' : '新增问答'">
        <el-form :model="form" label-width="80px">
          <el-form-item label="问题">
            <el-input v-model="form.question"></el-input>
          </el-form-item>
          <el-form-item label="回答">
            <el-input type="textarea" v-model="form.answer" rows="4"></el-input>
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="form.category"></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="form.isActive"></el-switch>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">保存</el-button>
          </span>
        </template>
      </el-dialog>
    </el-main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

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

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.kb-container {
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
.title {
  font-size: 18px;
  font-weight: bold;
}
.actions {
  background: white;
  padding: 15px;
  border-radius: 4px;
}
</style>
