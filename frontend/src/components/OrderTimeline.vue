<template>
  <div class="order-timeline">
    <el-timeline>
      <el-timeline-item
        v-for="(activity, index) in activities"
        :key="index"
        :timestamp="formatTime(activity.createdAt)"
        :type="activity.type"
        :color="activity.color"
        :hollow="activity.hollow"
        placement="top"
      >
        <el-card class="timeline-card">
          <div class="timeline-header">
            <span class="action-name">{{ activity.actionName }}</span>
            <el-tag size="small" :type="getStatusType(activity.newStatus)">{{ getStatusText(activity.newStatus) }}</el-tag>
          </div>
          <div class="timeline-content">
            <p class="operator">操作人: {{ activity.operatorName || '系统' }}</p>
            <p class="remark" v-if="activity.remark">{{ activity.remark }}</p>
            
            <!-- Photo Gallery -->
            <div v-if="activity.photos && activity.photos.length > 0" class="photo-gallery">
              <el-image 
                v-for="(photo, pIndex) in activity.photos"
                :key="pIndex"
                :src="photo.fileUrl"
                :preview-src-list="activity.photos.map(p => p.fileUrl)"
                class="log-photo"
                fit="cover"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Picture } from '@element-plus/icons-vue'

const props = defineProps({
  logs: {
    type: Array,
    default: () => []
  },
  // Photos mapped by log id or time? Or pass all photos and we filter?
  // Let's assume parent passes photos array
  photos: {
    type: Array,
    default: () => []
  }
})

const getActionText = (action) => {
  if (!action) return '未知操作'
  // Handle space-separated or other formats by normalizing
  // Example: "Self Create" -> "SELF_CREATE"
  const normalized = action.toUpperCase().replace(/\s+/g, '_')
  
  const map = {
    'CREATE': '创建订单',
    'SELF_CREATE': '自主建单',
    'ASSIGN': '分配司机',
    'GRAB': '司机抢单',
    'ACCEPT': '接单',
    'LOAD': '开始装车',
    'DEPART': '开始运输',
    'ARRIVE': '到达站点',
    'ENTER': '确认进站',
    'UNLOAD': '卸货完成',
    'COMPLETE': '订单完成',
    'CANCEL': '取消订单'
  }
  return map[normalized] || action
}

const activities = computed(() => {
  return props.logs.map(log => {
    // Filter photos relevant to this log
    let matchedPhotos = []
    
    // Map status to nodeType
    let targetNode = ''
    if (log.newStatus === 30) targetNode = 'load'
    else if (log.newStatus === 50) targetNode = 'unload' // Or 'arrive' based on button logic in DriverTaskView
    else if (log.newStatus === 40) targetNode = 'arrive' // DriverTaskView uses 'arrive' at status 40
    else if (log.newStatus === 20) targetNode = 'create'
    
    if (targetNode) {
        matchedPhotos = (props.photos || []).filter(p => p.nodeType === targetNode)
    }
    
    return {
      ...log,
      actionName: getActionText(log.actionName), // Transform here
      type: getStatusType(log.newStatus),
      color: getStatusColor(log.newStatus),
      hollow: false,
      photos: matchedPhotos
    }
  })
})


const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString()
}

const getStatusText = (status) => {
  const map = { 10: '待接单', 20: '已接单', 25: '司机接单', 30: '装车中', 40: '运输中', 50: '已到站', 60: '已完成' }
  return map[status] || `状态 ${status}`
}

const getStatusType = (status) => {
  if (status === 60) return 'success'
  if (status === 10) return 'info'
  if (status >= 40) return 'warning'
  return 'primary'
}

const getStatusColor = (status) => {
  if (status === 60) return '#67C23A' // Success Green
  if (status === 40) return '#E6A23C' // Warning Orange
  if (status === 30) return '#409EFF' // Primary Blue
  return '#909399' // Info Grey
}
</script>

<style scoped>
.timeline-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.action-name {
  font-weight: 600;
  font-size: 14px;
}

.operator {
  font-size: 12px;
  color: #666;
  margin: 0;
}

.remark {
  font-size: 13px;
  color: #333;
  margin: 4px 0 0 0;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
}

.photo-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.log-photo {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #eee;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}
</style>
