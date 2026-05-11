<template>
  <div class="chat-widget-container" :class="{ 'no-header': hideHeader }">
    <!-- Header -->
    <header v-if="!hideHeader" class="eco-header">
      <div class="header-left">
        <div class="logo-box">
          <svg class="logo-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M11 20A7 7 0 0 1 4 13V4h9a7 7 0 0 1 7 7v9h-9Z"></path>
            <path d="M11 20v-9"></path>
          </svg>
        </div>
        <div class="brand-text">
          <div class="brand-title">EcoClear</div>
          <div class="brand-subtitle">建筑废弃物全生态管理平台</div>
        </div>
      </div>
      <div class="header-right">
        <div class="user-avatar-btn" @click="handleAvatarClick" title="个人中心">
          <el-avatar :size="32" :src="user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        </div>
      </div>
    </header>

    <!-- Messages Area -->
    <div class="messages-container" ref="messagesContainer">
      <!-- Empty State / Placeholder (1:1 Reference Image) -->
      <div v-if="messages.length === 0" class="eco-placeholder">
        <div class="brain-animation-container">
          <div class="ring ring-outer">
            <div class="dot top-dot"></div>
            <div class="dot bottom-dot"></div>
          </div>
          <div class="ring ring-inner"></div>
          <div class="brain-core">
            <svg class="brain-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9.5 2c-1.4 0-2.6.9-3 2.2-.8.2-1.5.9-1.5 1.8 0 .8.5 1.5 1.2 1.8-.4.7-.4 1.5-.1 2.2.4.9 1.4 1.5 2.4 1.5.2 0 .4 0 .6-.1.6 1.4 2 2.6 3.9 2.6s3.3-1.2 3.9-2.6c.2.1.4.1.6.1 1 0 2-.6 2.4-1.5.3-.7.3-1.5-.1-2.2.7-.3 1.2-1 1.2-1.8 0-.9-.7-1.6-1.5-1.8-.4-1.3-1.6-2.2-3-2.2-1 0-1.9.6-2.5 1.4-.6-.8-1.5-1.4-2.5-1.4Z"/>
              <path d="M12 2v19"/>
              <path d="M12 7h3"/>
              <path d="M12 12h3"/>
              <path d="M12 17h3"/>
              <path d="M9 7h3"/>
              <path d="M9 12h3"/>
              <path d="M9 17h3"/>
            </svg>
          </div>
        </div>
        <h2 class="greeting-title">您好，请问有什么可以帮您？</h2>
        <p class="greeting-sub">说出您的需求，智能为您匹配服务</p>
      </div>

      <!-- Chat Messages -->
      <template v-else>
        <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
          <!-- Timestamp -->
          <div v-if="shouldShowTime(index)" class="message-time">
            {{ formatTime(msg.createdAt) }}
          </div>
          
          <div class="message-row" :class="msg.senderType">
            <!-- AI Avatar -->
            <div v-if="msg.senderType === 'ai'" class="avatar-box ai-avatar">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M11 20A7 7 0 0 1 4 13V4h9a7 7 0 0 1 7 7v9h-9Z"></path>
              </svg>
            </div>
            
            <div class="message-bubble">
              {{ msg.content }}
              
              <!-- Action Hint for Routing -->
              <div v-if="msg.actionPayload" class="action-hint">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>正在为您跳转...</span>
              </div>
              
              <!-- Quick Replies -->
              <div v-if="msg.senderType === 'ai' && index === messages.length - 1 && quickQuestions.length > 0" class="quick-replies">
                <div 
                  v-for="q in quickQuestions" 
                  :key="q" 
                  class="quick-reply-btn"
                  @click="setInput(q)"
                >
                  {{ q }}
                </div>
              </div>
            </div>

            <!-- User Avatar -->
            <div v-if="msg.senderType === 'user'" class="avatar-box user-avatar">
              我
            </div>
          </div>
        </div>

        <!-- Typing Indicator -->
        <div v-if="loading" class="message-wrapper">
          <div class="message-row ai">
            <div class="avatar-box ai-avatar">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M11 20A7 7 0 0 1 4 13V4h9a7 7 0 0 1 7 7v9h-9Z"></path>
              </svg>
            </div>
            <div class="message-bubble typing-bubble">
              <div class="typing-dots">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Input Section -->
    <div class="eco-input-section">
      <!-- Voice Status Indicator -->
      <div class="voice-status-bar" :class="{ active: isListening }">
        <div class="voice-waves">
          <span></span><span></span><span></span><span></span><span></span>
        </div>
        <span>正在聆听，请说话...</span>
      </div>

      <!-- Input Card -->
      <div class="input-card">
        <div class="mic-icon" :class="{ active: isListening }" @click="toggleListen">
          <el-icon><Microphone /></el-icon>
        </div>
        <input 
          type="text" 
          v-model="inputMessage" 
          class="eco-input" 
          placeholder="说出或输入您的需求..." 
          @keyup.enter="sendMessage"
          :disabled="loading"
        />
        <div class="send-btn" :class="{ active: inputMessage.trim() }" @click="sendMessage">
          <el-icon><Position /></el-icon>
        </div>
      </div>
      
      <!-- Hints -->
      <div class="input-hints" v-if="messages.length === 0">
        例如："我要清运装修垃圾"、"我是司机想入驻"、"我要采购石子"
      </div>
    </div>

    <!-- Footer -->
    <div class="eco-footer" v-if="messages.length === 0">
      © 2024 EcoClear · 建筑废弃物全生态管理平台
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import api from '../api'
import { useRouter } from 'vue-router'
import { Position, Microphone, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  hideHeader: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const sessionId = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)

const voiceOutputEnabled = ref(true)
const isListening = ref(false)
let recognition = null

const quickQuestions = [
  '我要预约清运',
  '查看我的车队',
  '积分商城在哪',
  '查询知识库'
]

// --- Utils ---
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

const shouldShowTime = (index) => {
  if (index === 0) return true
  if (!messages.value[index].createdAt || !messages.value[index - 1].createdAt) return false
  const current = new Date(messages.value[index].createdAt)
  const prev = new Date(messages.value[index - 1].createdAt)
  return (current - prev) > 5 * 60 * 1000 // > 5 minutes
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// --- Routing & Tracking Logic ---
const handleAvatarClick = () => {
  // Click Tracking Event
  console.log('Track Event: Avatar Clicked', {
    timestamp: new Date().toISOString(),
    userId: user.id || 'guest',
    userType: user.role || 'guest',
    platform: navigator.userAgent
  })

  if (!user || !user.role) {
    router.push('/login')
    return
  }
  
  // t1 账号特殊处理：直接进入 HomeDashboardView (即 /home)
  if (user.username === 't1') {
    router.push('/home')
    return
  }

  const role = user.role
  if (role === 'admin') {
    router.push('/admin/personal')
  } else if (role === 'vip') {
    router.push('/vip/dashboard')
  } else if (role === 'driver') {
    router.push('/profile/driver')
  } else if (role === 'station') {
    router.push('/station/work')
  } else if (['fleet_owner', 'fleet_dispatcher'].includes(role)) {
    router.push('/fleet/profile')
  } else if (['property', 'street'].includes(role)) {
    router.push('/profile/enterprise')
  } else {
    router.push('/home')
  }
}

// --- Voice Recognition ---
const initSpeechRecognition = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (SpeechRecognition) {
    recognition = new SpeechRecognition()
    recognition.lang = 'zh-CN'
    recognition.continuous = false
    recognition.interimResults = true

    recognition.onstart = () => {
      isListening.value = true
    }

    recognition.onresult = (event) => {
      let interimTranscript = ''
      let finalTranscript = ''

      for (let i = event.resultIndex; i < event.results.length; ++i) {
        if (event.results[i].isFinal) {
          finalTranscript += event.results[i][0].transcript
        } else {
          interimTranscript += event.results[i][0].transcript
        }
      }
      
      if (finalTranscript) {
        inputMessage.value = finalTranscript
        sendMessage()
      } else {
        inputMessage.value = interimTranscript
      }
    }

    recognition.onerror = (event) => {
      console.error('Speech recognition error', event.error)
      isListening.value = false
      if (event.error !== 'no-speech') {
        ElMessage.warning('语音识别失败，请重试或使用文字输入')
      }
    }

    recognition.onend = () => {
      isListening.value = false
    }
  } else {
    console.warn('当前浏览器不支持 Web Speech API')
  }
}

const toggleListen = () => {
  if (!recognition) {
    ElMessage.error('当前浏览器不支持语音输入，请使用Chrome等现代浏览器')
    return
  }
  
  if (isListening.value) {
    recognition.stop()
  } else {
    inputMessage.value = ''
    recognition.start()
  }
}

const speakText = (text) => {
  if (!voiceOutputEnabled.value || !window.speechSynthesis) return
  window.speechSynthesis.cancel()
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'zh-CN'
  utterance.rate = 1.0
  utterance.pitch = 1.0
  window.speechSynthesis.speak(utterance)
}

// --- Chat Logic ---
const setInput = (q) => {
  inputMessage.value = q
  sendMessage()
}

const initSession = async () => {
  if (!user.id) return
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
    // Initial greeting is visually embedded in the placeholder, no need to push to array.
  } catch (error) {
    console.error('Failed to create session', error)
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
  if (!inputMessage.value.trim() || loading.value) return
  
  const content = inputMessage.value
  inputMessage.value = ''
  
  if (isListening.value && recognition) {
    recognition.stop()
  }

  messages.value.push({ 
    senderType: 'user', 
    content: content, 
    createdAt: new Date().toISOString() 
  })
  scrollToBottom()
  loading.value = true

  try {
    const res = await api.post('/ai/chat', {
      sessionId: sessionId.value,
      content: content
    })
    
    setTimeout(() => {
      const aiMsg = res.data
      messages.value.push(aiMsg)
      loading.value = false
      scrollToBottom()
      
      speakText(aiMsg.content)

      if (aiMsg.actionPayload) {
        setTimeout(() => {
          router.push(aiMsg.actionPayload)
        }, 1500)
      }
    }, 600)
    
  } catch (error) {
    loading.value = false
    messages.value.push({ 
      senderType: 'ai', 
      content: '网络开小差了，请稍后再试。',
      createdAt: new Date().toISOString()
    })
    scrollToBottom()
  }
}

onMounted(() => {
  initSession()
  initSpeechRecognition()
})

onUnmounted(() => {
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
  }
  if (recognition && isListening.value) {
    recognition.stop()
  }
})
</script>

<style scoped>
.chat-widget-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: linear-gradient(180deg, #f0fcfa 0%, #f7fdfd 100%);
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", Helvetica, Arial, sans-serif;
  position: relative;
  overflow: hidden;
}

/* --- Header --- */
.eco-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  cursor: pointer;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-box {
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #0dc8a3 0%, #07a685 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 6px 16px rgba(13, 200, 163, 0.3);
}

.logo-svg {
  width: 22px;
  height: 22px;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f294d;
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 12px;
  color: #8c9eb5;
  margin-top: 2px;
}

.user-avatar-btn {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.user-avatar-btn:hover {
  border-color: #0dc8a3;
  transform: scale(1.05);
}

.version-text {
  font-size: 12px;
  color: #aeb9c7;
  font-weight: 500;
}

/* --- Messages Container --- */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

/* --- Placeholder / Empty State --- */
.eco-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-bottom: 5vh;
}

.brain-animation-container {
  position: relative;
  width: 160px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(28, 196, 162, 0.15);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.ring-outer {
  width: 150px;
  height: 150px;
  animation: spin 20s linear infinite;
}

.ring-inner {
  width: 110px;
  height: 110px;
  border-color: rgba(28, 196, 162, 0.25);
  animation: spin-reverse 15s linear infinite;
}

.dot {
  position: absolute;
  width: 8px;
  height: 8px;
  background-color: #1cc4a2;
  border-radius: 50%;
  left: 50%;
  transform: translateX(-50%);
}

.top-dot {
  top: -4px;
}

.bottom-dot {
  bottom: -4px;
}

.brain-core {
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #2fd4b2 0%, #17a98a 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 12px 24px rgba(23, 169, 138, 0.25);
  z-index: 2;
  position: relative;
}

.brain-svg {
  width: 32px;
  height: 32px;
}

@keyframes spin {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes spin-reverse {
  from { transform: translate(-50%, -50%) rotate(360deg); }
  to { transform: translate(-50%, -50%) rotate(0deg); }
}

.greeting-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f294d;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
  text-align: center;
}

.greeting-sub {
  font-size: 13px;
  color: #6c7b8e;
  margin: 0;
  text-align: center;
}

/* --- Chat Messages --- */
.message-wrapper {
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
}

.message-time {
  text-align: center;
  font-size: 12px;
  color: #aeb9c7;
  margin-bottom: 16px;
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

.avatar-box {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: bold;
}

.ai-avatar {
  background: linear-gradient(135deg, #0dc8a3 0%, #07a685 100%);
  color: white;
  box-shadow: 0 4px 10px rgba(13, 200, 163, 0.2);
}

.ai-avatar svg {
  width: 20px;
  height: 20px;
}

.user-avatar {
  background-color: #f0f4f8;
  color: #0f294d;
  border: 1px solid #e1e8f0;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 8px; /* 8dp standard */
  line-height: 1.6;
  font-size: 16px; /* 16px standard */
  word-wrap: break-word;
  position: relative;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.user .message-bubble {
  background-color: #0dc8a3;
  color: white;
  border-top-right-radius: 4px;
}

.ai .message-bubble {
  background-color: white;
  color: #1a2b3c;
  border-top-left-radius: 4px;
}

.action-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #0dc8a3;
}

.quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.quick-reply-btn {
  padding: 8px 16px;
  background-color: #f0fcfa;
  color: #0dc8a3;
  border: 1px solid #bdf1e6;
  border-radius: 8px; /* 8dp standard */
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease-out;
  min-height: 44px; /* WCAG touch target */
  display: flex;
  align-items: center;
}

.quick-reply-btn:hover {
  background-color: #0dc8a3;
  color: white;
}

/* Typing Indicator */
.typing-bubble {
  min-width: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.typing-dots {
  display: flex;
  gap: 4px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  background-color: #aeb9c7;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-dots span:nth-child(1) { animation-delay: -0.32s; }
.typing-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* --- Input Section --- */
.eco-input-section {
  padding: 0 24px 48px; /* Increased bottom padding to prevent overlap with footer */
  display: flex;
  flex-direction: column;
  align-items: center;
}

.voice-status-bar {
  height: 0;
  overflow: hidden;
  opacity: 0;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #0dc8a3;
  font-size: 14px;
  margin-bottom: 0;
}

.voice-status-bar.active {
  height: 30px;
  opacity: 1;
  margin-bottom: 12px;
}

.voice-waves {
  display: flex;
  align-items: center;
  gap: 3px;
  height: 16px;
}

.voice-waves span {
  width: 3px;
  height: 4px;
  background-color: #0dc8a3;
  border-radius: 3px;
  animation: wave 1s ease-in-out infinite;
}

.voice-waves span:nth-child(2) { animation-delay: 0.1s; }
.voice-waves span:nth-child(3) { animation-delay: 0.2s; }
.voice-waves span:nth-child(4) { animation-delay: 0.3s; }
.voice-waves span:nth-child(5) { animation-delay: 0.4s; }

.input-card {
  width: 100%;
  max-width: 800px;
  background: white;
  border-radius: 12px; /* 8dp standard, slightly larger for container */
  padding: 8px 12px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  gap: 12px;
  transition: box-shadow 0.2s ease-out;
}

.input-card:focus-within {
  box-shadow: 0 8px 24px rgba(13, 200, 163, 0.1);
}

.mic-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aeb9c7;
  font-size: 20px;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s;
}

.mic-icon:hover {
  background-color: #f5f7fa;
  color: #0f294d;
}

.mic-icon.active {
  color: #0dc8a3;
  background-color: #f0fcfa;
}

.eco-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 16px; /* WCAG standard for mobile input to prevent zoom */
  color: #1a2b3c;
  background: transparent;
  min-height: 44px; /* WCAG touch target size */
}

.eco-input::placeholder {
  color: #aeb9c7;
}

.send-btn {
  width: 44px;
  height: 44px; /* WCAG touch target size */
  border-radius: 8px; /* 8dp standard */
  background-color: #a5e8d9;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: not-allowed;
  transition: all 0.2s ease-out;
}

.send-btn.active {
  background-color: #0dc8a3; 
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(13, 200, 163, 0.3);
}

.send-btn.active:hover {
  background-color: #0ba88a;
}

.input-hints {
  margin-top: 16px;
  font-size: 12px;
  color: #aeb9c7;
  text-align: center;
}

/* --- Footer --- */
.eco-footer {
  text-align: center;
  font-size: 12px;
  color: #aeb9c7;
  padding: 16px 0 20px; /* Adjust padding instead of absolute positioning */
  width: 100%;
  background: transparent;
}
</style>
