<template>
  <div class="eco-data-card">
    <div class="data-header">
      <h3 class="data-title">清运明细</h3>
      <span class="data-tag">智能评估</span>
    </div>
    
    <div class="data-body">
      <!-- 清运地址 -->
      <div class="data-row">
        <div class="icon-wrapper location-icon">
          <el-icon><Location /></el-icon>
        </div>
        <div class="content-wrapper">
          <div class="label-text">清运地址</div>
          <div class="value-text address-value" :title="address">
            {{ address || '尚未识别到详细地址' }}
          </div>
        </div>
      </div>

      <div class="eco-divider"></div>

      <!-- 垃圾方数 -->
      <div class="data-row">
        <div class="icon-wrapper volume-icon">
          <el-icon><Box /></el-icon>
        </div>
        <div class="content-wrapper">
          <div class="label-text">垃圾方数</div>
          <div class="value-text volume-value">
            <span class="number">{{ volume || '0.00' }}</span>
            <span class="unit">方</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Location, Box } from '@element-plus/icons-vue'

defineProps({
  address: {
    type: String,
    default: '上海市朝阳区环保科技产业园A栋' // 模拟OCR占位数据
  },
  volume: {
    type: [Number, String],
    default: '12.5' // 模拟OCR占位数据
  }
})
</script>

<style scoped>
.eco-data-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  padding: 20px 24px;
  width: 100%;
  max-width: 400px; /* 适配移动端/侧边栏 */
  font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue", Helvetica, Arial, sans-serif;
  transition: box-shadow 0.2s ease-out;
}

.eco-data-card:hover {
  box-shadow: 0 8px 24px rgba(13, 200, 163, 0.08);
}

.data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.data-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1b1c;
  margin: 0;
}

.data-tag {
  background-color: #f0fcfa;
  color: #0dc8a3;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid #bdf1e6;
}

.data-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.data-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 8px; /* 8dp 标准 */
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.location-icon {
  background-color: #f0f4f8;
  color: #409EFF;
}

.volume-icon {
  background-color: #f0fcfa;
  color: #0dc8a3;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  min-width: 0;
}

.label-text {
  font-size: 13px;
  color: #8c9299;
  margin-bottom: 4px;
}

.value-text {
  font-size: 16px;
  color: #1a1b1c;
  font-weight: 500;
  line-height: 1.4;
}

.address-value {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
}

.volume-value .number {
  font-size: 24px;
  font-weight: 700;
  color: #0dc8a3;
}

.volume-value .unit {
  font-size: 14px;
  color: #8c9299;
  margin-left: 4px;
  font-weight: normal;
}

.eco-divider {
  height: 1px;
  background-color: #f0f0f0;
  width: 100%;
  margin: 4px 0;
}
</style>
