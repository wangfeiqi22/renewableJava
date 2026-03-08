<template>
  <div class="chat-view-container">
    <el-header class="header">
      <div class="header-left" @click="router.push('/order/create')">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <div class="title">智能客服</div>
      <div class="header-right"></div>
    </el-header>

    <div class="messages-container" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index">
        <!-- User Message -->
        <div class="message-wrapper user" v-if="msg.senderType === 'user'">
          <div class="message-bubble user-bubble">{{ msg.content }}</div>
          <el-avatar class="avatar">我</el-avatar>
        </div>

        <!-- AI Message -->
        <div class="message-wrapper ai" v-else>
          <el-avatar class="avatar ai-avatar">AI</el-avatar>
          <div class="message-bubble ai-bubble">{{ msg.content }}</div>
        </div>
      </div>
      
      <!-- Typing Indicator -->
      <div v-if="loading" class="message-wrapper ai">
        <el-avatar class="avatar ai-avatar">AI</el-avatar>
        <div class="message-bubble ai-bubble typing">
          <span>.</span><span>.</span><span>.</span>
        </div>
      </div>
    </div>

    <div class="input-area">
      <el-input
        v-model="inputMessage"
        placeholder="请输入您的问题..."
        @keyup.enter="sendMessage"
        :disabled="loading"
      >
        <template #append>
          <el-button @click="sendMessage" :loading="loading" class="send-btn">发送</el-button>
        </template>
      </el-input>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const sessionId = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)

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
  background-color: #f5f7fa;
}

.header {
  background-color: #00C851;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  height: 50px;
}
.header-left {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
}
.title {
  font-weight: bold;
  font-size: 16px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 85%;
}
.message-wrapper.user {
  align-self: flex-end;
  flex-direction: row;
}
.message-wrapper.ai {
  align-self: flex-start;
}

.avatar {
  background-color: #ddd;
  color: #555;
}
.ai-avatar {
  background-color: #00C851;
  color: white;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  line-height: 1.5;
  font-size: 14px;
  word-wrap: break-word;
}
.user-bubble {
  background-color: #c8e6c9; /* Light Green */
  color: #2e7d32;
  border-bottom-right-radius: 2px;
}
.ai-bubble {
  background-color: white;
  color: #333;
  border-bottom-left-radius: 2px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.input-area {
  padding: 15px;
  background: white;
  border-top: 1px solid #eee;
}
.send-btn {
  background-color: #00C851;
  color: white;
  border: none;
}
.send-btn:hover {
  background-color: #00e676;
}

/* Typing Animation */
.typing span {
  animation: typing 1.4s infinite ease-in-out both;
  margin: 0 2px;
  display: inline-block;
}
.typing span:nth-child(1) { animation-delay: -0.32s; }
.typing span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
