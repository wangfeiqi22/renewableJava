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
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  logs: {
    type: Array,
    default: () => []
  }
})

const activities = computed(() => {
  return props.logs.map(log => ({
    ...log,
    type: getStatusType(log.newStatus),
    color: getStatusColor(log.newStatus),
    hollow: false
  }))
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
</style>
