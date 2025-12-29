<template>
  <el-badge 
    :value="totalUnreadCount" 
    :hidden="totalUnreadCount === 0"
    :max="99"
    class="message-badge"
  >
    <el-icon :size="20">
      <ChatDotRound />
    </el-icon>
  </el-badge>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import { useMessageStore } from '../store/message'

const messageStore = useMessageStore()

// 获取未读消息总数
const totalUnreadCount = computed(() => messageStore.totalUnreadCount)

let pollingTimer = null

// 定期轮询未读消息数
const startPolling = () => {
  // 立即获取一次
  messageStore.fetchUnreadCount()
  
  // 每15秒轮询一次
  pollingTimer = setInterval(() => {
    messageStore.fetchUnreadCount()
  }, 15000)
}

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

onMounted(() => {
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.message-badge {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
}

.message-badge :deep(.el-badge__content) {
  border: 2px solid #fff;
}
</style>