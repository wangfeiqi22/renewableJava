<template>
  <div class="chat-page">
    <AiChatWidget />
  </div>
</template>

<script setup>
import AiChatWidget from '../components/AiChatWidget.vue'
</script>

<style scoped>
.chat-page {
  height: 100vh;
  height: 100dvh;
  width: 100vw;
  overflow: hidden;
  background-color: #f0fcfa;
}

@media (min-width: 768px) {
  .chat-page {
    max-width: 480px;
    margin: 0 auto;
    border-left: 1px solid #eee;
    border-right: 1px solid #eee;
    box-shadow: 0 0 30px rgba(0,0,0,0.05);
  }
}
</style>
