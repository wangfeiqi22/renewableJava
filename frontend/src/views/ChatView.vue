<template>
  <div class="chat-view-container">
    <el-header class="chat-header">
      <div class="header-left" @click="router.push('/order/create')">
        <el-icon class="back-icon"><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <div class="header-title">智能客服助手</div>
      <div class="header-right">
        <el-button link class="header-action">
          <el-icon><MoreFilled /></el-icon>
        </el-button>
      </div>
    </el-header>

    <div class="messages-container" ref="messagesContainer">
      <div v-if="messages.length === 0" class="chat-placeholder">
        <div class="placeholder-icon">🤖</div>
        <p>有什么可以帮您？</p>
        <div class="quick-questions">
          <el-tag 
            v-for="q in quickQuestions" 
            :key="q" 
            class="question-tag" 
            effect="plain" 
            round
            @click="setInput(q)"
          >
            {{ q }}
          </el-tag>
        </div>
      </div>

      <div v-for="(msg, index) in messages" :key="index" class="message-row" :class="msg.senderType">
        <!-- AI Avatar -->
        <el-avatar v-if="msg.senderType === 'ai'" class="avatar ai-avatar" :size="36" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png">AI</el-avatar>
        
        <div class="message-bubble">
          {{ msg.content }}
        </div>

        <!-- User Avatar -->
        <el-avatar v-if="msg.senderType === 'user'" class="avatar user-avatar" :size="36">我</el-avatar>
      </div>
      
      <!-- Typing Indicator -->
      <div v-if="loading" class="message-row ai">
        <el-avatar class="avatar ai-avatar" :size="36">AI</el-avatar>
        <div class="message-bubble typing">
          <span>.</span><span>.</span><span>.</span>
        </div>
      </div>
    </div>

    <div class="input-area">
      <div class="input-wrapper">
        <el-input
          v-model="inputMessage"
          placeholder="请输入您的问题..."
          @keyup.enter="sendMessage"
          :disabled="loading"
          class="chat-input"
        >
          <template #suffix>
            <el-button 
              type="primary" 
              circle 
              @click="sendMessage" 
              :loading="loading" 
              class="send-btn"
              :disabled="!inputMessage.trim()"
            >
              <el-icon><Position /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ArrowLeft, MoreFilled, Position } from '@element-plus/icons-vue'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const sessionId = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)

const quickQuestions = [
  '如何预约回收？',
  '收费标准是什么？',
  '大件垃圾怎么处理？',
  '订单状态查询'
]

const setInput = (q) => {
  inputMessage.value = q
  sendMessage()
}

const initSession = async () => {
  if (!user.id) return
  
  // Try to get latest session or create new
  try {
    const res = await api.get(`/ai/sessions/user/${user.id}`)
    if (res.data && res.data.length > 0) {
      sessionId.value = res.data[0].id
      loadHistory()
    } else {
      createSession()
    }
  } catch (error) {
    createSession()
  }
}

const createSession = async () => {
  try {
    const res = await api.post('/ai/sessions', { userId: user.id })
    sessionId.value = res.data.id
    // Initial Greeting
    messages.value.push({ senderType: 'ai', content: '您好！我是您的智能助手，有什么可以帮您？' })
  } catch (error) {
    console.error('Failed to create session')
  }
}

const loadHistory = async () => {
  if (!sessionId.value) return
  try {
    const res = await api.get(`/ai/history/${sessionId.value}`)
    messages.value = res.data
    scrollToBottom()
  } catch (error) {
    console.error(error)
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const content = inputMessage.value
  inputMessage.value = ''

  // Optimistic update
  messages.value.push({ senderType: 'user', content: content })
  scrollToBottom()
  loading.value = true

  try {
    const res = await api.post('/ai/chat', {
      sessionId: sessionId.value,
      content: content
    })
    
    // Simulate typing delay
    setTimeout(() => {
      messages.value.push(res.data) // AI Response
      loading.value = false
      scrollToBottom()
    }, 600)
    
  } catch (error) {
    loading.value = false
    messages.value.push({ senderType: 'ai', content: '网络开小差了，请稍后再试。' })
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  initSession()
})
</script>

<style scoped>
.chat-view-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f2f4f6;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.chat-header {
  background: white;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  height: 50px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  color: var(--color-primary);
}

.back-icon {
  font-size: 18px;
  margin-right: 4px;
}

.header-title {
  font-weight: 600;
  font-size: 16px;
}

.header-action {
  color: #333;
  font-size: 20px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chat-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-top: 50px;
  color: #999;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 10px;
  background: white;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-top: 20px;
  max-width: 300px;
}

.question-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.question-tag:hover {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 85%;
}

.message-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-row.ai {
  align-self: flex-start;
}

.avatar {
  flex-shrink: 0;
}

.ai-avatar {
  background-color: white;
  border: 1px solid #eee;
  color: var(--color-primary);
}

.user-avatar {
  background-color: var(--color-primary);
  color: white;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.5;
  font-size: 15px;
  word-wrap: break-word;
  position: relative;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.user .message-bubble {
  background-color: var(--color-primary);
  color: white;
  border-top-right-radius: 2px;
}

.ai .message-bubble {
  background-color: white;
  color: #333;
  border-top-left-radius: 2px;
}

.input-area {
  padding: 15px;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.input-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.chat-input :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
  border-radius: 24px;
  padding-left: 20px;
  box-shadow: none;
}

.chat-input :deep(.el-input__wrapper.is-focus) {
  background-color: white;
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.send-btn {
  background-color: var(--color-primary);
  color: white;
  border: none;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: -5px;
}

.send-btn:hover {
  background-color: #00e676;
}

.send-btn.is-disabled {
  background-color: #ccc;
}

/* Typing Animation */
.typing {
  display: flex;
  align-items: center;
  height: 24px;
}

.typing span {
  animation: typing 1.4s infinite ease-in-out both;
  margin: 0 2px;
  display: inline-block;
  background-color: #999;
  width: 4px;
  height: 4px;
  border-radius: 50%;
}

.typing span:nth-child(1) { animation-delay: -0.32s; }
.typing span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
