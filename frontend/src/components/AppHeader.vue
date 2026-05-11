<template>
  <header class="mobile-header">
    <div class="header-left">
      <div class="brand-logo">
        <div class="logo-icon">
          <el-icon v-if="icon === 'Van'"><Van /></el-icon>
          <el-icon v-else><Menu /></el-icon>
        </div>
        <div class="brand-text">
          <div class="brand-name">{{ title }}</div>
          <div class="brand-sub">{{ subtitle }}</div>
        </div>
      </div>
    </div>
    <div class="header-center">
      <div class="nav-item">
        <el-icon></el-icon> 
      </div>
      <div class="nav-item">
        <el-icon></el-icon> 
      </div>
    </div>
    <div class="header-right">
      <div class="ai-chat-btn" @click="handleAiChatClick" title="智能问答" aria-label="智能问答" role="button" tabindex="0">
        <svg class="ai-chat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M11 20A7 7 0 0 1 4 13V4h9a7 7 0 0 1 7 7v9h-9Z"></path>
        </svg>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Menu, Van } from '@element-plus/icons-vue'

defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    required: true
  },
  icon: {
    type: String,
    required: true
  }
})

const router = useRouter()

const handleAiChatClick = () => {
  try {
    router.push('/chat')
  } catch (error) {
    console.error('Navigation error on AI chat click:', error)
  }
}
</script>

<style>
:root {
  --header-height: 56px;
  --header-bg: #fff;
  --header-text-main: #333;
  --header-text-sub: #999;
  --header-shadow-ai: 0 4px 10px rgba(13, 200, 163, 0.2);
  --header-shadow-ai-hover: 0 6px 14px rgba(13, 200, 163, 0.3);
  --header-transition: all 0.3s ease;
  --header-ai-gradient: linear-gradient(135deg, #0dc8a3 0%, #07a685 100%);
  --header-icon-size: 28px;
  --header-ai-btn-size: 36px;
}
</style>

<style scoped>
.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: var(--header-height);
  background-color: var(--header-bg);
  flex-shrink: 0;
  z-index: 10;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  width: var(--header-icon-size);
  height: var(--header-icon-size);
  background: var(--color-primary, #0dc8a3);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.brand-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--header-text-main);
  line-height: 1.2;
}

.brand-sub {
  font-size: 10px;
  color: var(--header-text-sub);
}

.header-center {
  display: flex;
  gap: 16px;
  font-size: 14px;
  font-weight: 500;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-primary, #0dc8a3);
}

.nav-item:last-child {
  color: #e6a23c;
}

.header-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.ai-chat-btn {
  width: var(--header-ai-btn-size);
  height: var(--header-ai-btn-size);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: var(--header-ai-gradient);
  color: white;
  box-shadow: var(--header-shadow-ai);
  transition: var(--header-transition);
}

.ai-chat-btn:hover {
  transform: scale(1.05);
  box-shadow: var(--header-shadow-ai-hover);
}

.ai-chat-icon {
  width: 20px;
  height: 20px;
}
</style>
