<template>
  <div>
    <el-card>
      <template #header>
        <span>我的消息</span>
      </template>

      <div v-if="conversations.length === 0" style="text-align: center; padding: 40px; color: #909399">
        暂无消息
      </div>

      <el-list v-else>
        <el-list-item
          v-for="partner in conversations"
          :key="partner.id"
          class="conversation-item"
          @click="openChat(partner)"
        >
          <el-avatar :size="50" :src="partner.avatar || undefined">
            {{ partner.nickname?.charAt(0) }}
          </el-avatar>
          <div class="conversation-info">
            <div class="conversation-name">{{ partner.nickname }}</div>
            <div class="conversation-preview">点击查看聊天记录</div>
          </div>
          <el-icon class="conversation-arrow"><ArrowRight /></el-icon>
        </el-list-item>
      </el-list>
    </el-card>

    <!-- 聊天对话框 -->
    <el-dialog
      v-model="chatDialogVisible"
      :title="`与 ${currentPartner?.nickname} 的聊天`"
      width="600px"
    >
      <div class="chat-container">
        <div class="chat-messages" ref="chatMessagesRef">
          <div
            v-for="msg in chatMessages"
            :key="msg.id"
            :class="['chat-message', msg.senderId === userStore.userInfo?.id ? 'mine' : 'other']"
          >
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
        </div>
        <div class="chat-input">
          <el-input
            v-model="chatMessage"
            placeholder="输入消息..."
            @keyup.enter="sendMessage"
          >
            <template #append>
              <el-button @click="sendMessage" type="primary">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { getConversations, getChatHistory, sendMessage as sendMsg, markAsRead } from '../api/message'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const conversations = ref([])
const chatDialogVisible = ref(false)
const currentPartner = ref(null)
const chatMessages = ref([])
const chatMessage = ref('')
const chatMessagesRef = ref(null)

// 加载会话列表
const loadConversations = async () => {
  try {
    const res = await getConversations()
    conversations.value = res.data
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 打开聊天窗口
const openChat = async (partner) => {
  currentPartner.value = partner
  chatDialogVisible.value = true
  await loadChatHistory()
  // 标记消息为已读
  await markAsRead(partner.id)
}

// 加载聊天历史
const loadChatHistory = async () => {
  try {
    const res = await getChatHistory(currentPartner.value.id)
    chatMessages.value = res.data
    // 滚动到底部
    setTimeout(() => {
      if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      }
    }, 100)
  } catch (error) {
    console.error('加载聊天历史失败:', error)
  }
}

// 发送消息
const sendMessage = async () => {
  if (!chatMessage.value.trim()) return

  try {
    const res = await sendMsg({
      receiverId: currentPartner.value.id,
      content: chatMessage.value,
      productId: null
    })

    chatMessages.value.push(res.data)
    chatMessage.value = ''

    // 滚动到底部
    setTimeout(() => {
      if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      }
    }, 100)
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败')
  }
}

// 格式化时间
const formatTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadConversations()
})
</script>

<style scoped>
.conversation-item {
  display: flex;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-info {
  flex: 1;
  margin-left: 16px;
}

.conversation-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.conversation-preview {
  font-size: 14px;
  color: #909399;
}

.conversation-arrow {
  color: #c0c4cc;
  font-size: 18px;
}

/* 聊天组件样式 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.chat-message {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}

.chat-message.mine {
  align-items: flex-end;
}

.chat-message.other {
  align-items: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  word-wrap: break-word;
}

.chat-message.mine .message-content {
  background-color: #409eff;
  color: white;
}

.chat-message.other .message-content {
  background-color: white;
  color: #303133;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.chat-input {
  margin-top: auto;
}
</style>
