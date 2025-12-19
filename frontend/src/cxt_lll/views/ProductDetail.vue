<template>
  <div class="product-detail-container">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="text-large font-600 mr-3">商品详情</span>
      </template>
    </el-page-header>

    <el-card v-loading="loading" style="margin-top: 20px">
      <el-row :gutter="40">
        <!-- 商品图片 -->
        <el-col :span="12">
          <el-carousel height="400px" v-if="product.images && getImageArray(product.images).length > 0">
            <el-carousel-item v-for="(image, index) in getImageArray(product.images)" :key="index">
              <img :src="image" style="width: 100%; height: 100%; object-fit: contain" />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="empty-image">
            <el-icon :size="100" color="#dcdfe6"><Picture /></el-icon>
          </div>
        </el-col>

        <!-- 商品信息 -->
        <el-col :span="12">
          <h2>{{ product.title }}</h2>

          <div class="price-box">
            <span class="price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="original-price">
              原价: ¥{{ product.originalPrice }}
            </span>
          </div>

          <el-descriptions :column="1" border style="margin-top: 20px">
            <el-descriptions-item label="分类">
              {{ product.categoryName || '未分类' }}
            </el-descriptions-item>
            <el-descriptions-item label="卖家">
              {{ product.sellerName }}
            </el-descriptions-item>
            <el-descriptions-item label="所在地">
              {{ product.location || '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="联系方式">
              {{ product.contact || '请下单后联系' }}
            </el-descriptions-item>
            <el-descriptions-item label="浏览量">
              {{ product.viewCount }}
            </el-descriptions-item>
            <el-descriptions-item label="收藏数">
              {{ product.favoriteCount }}
            </el-descriptions-item>
            <el-descriptions-item label="发布时间">
              {{ formatDate(product.createdAt) }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="description-box">
            <h3>商品描述</h3>
            <p>{{ product.description || '暂无描述' }}</p>
          </div>

          <div class="action-buttons">
            <el-button
              v-if="product.status === 'ON_SALE' && !isMine"
              type="primary"
              size="large"
              :icon="ShoppingCart"
              @click="handleBuy"
              :disabled="!userStore.isLoggedIn()"
            >
              立即购买
            </el-button>

            <el-button
              v-if="!isMine"
              size="large"
              @click="handleChat"
              :disabled="!userStore.isLoggedIn()"
            >
              联系卖家
            </el-button>

            <el-button
              v-if="!isMine"
              size="large"
              :icon="product.isFavorite ? StarFilled : Star"
              @click="handleFavorite"
              :disabled="!userStore.isLoggedIn()"
            >
              {{ product.isFavorite ? '已收藏' : '收藏' }}
            </el-button>

            <el-tag v-if="product.status === 'SOLD'" type="info" size="large">已售出</el-tag>
            <el-tag v-if="product.status === 'OFF_SHELF'" type="warning" size="large">已下架</el-tag>
            <el-tag v-if="isMine" type="success" size="large">我的商品</el-tag>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 下单对话框 -->
    <el-dialog v-model="buyDialogVisible" title="确认下单" width="400px">
      <el-form :model="orderForm" label-width="80px">
        <el-form-item label="商品名称">
          <span>{{ product.title }}</span>
        </el-form-item>
        <el-form-item label="商品价格">
          <span class="price">¥{{ product.price }}</span>
        </el-form-item>
        <el-form-item label="买家留言">
          <el-input
            v-model="orderForm.buyerMessage"
            type="textarea"
            :rows="3"
            placeholder="给卖家留言（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBuy" :loading="buyLoading">
          确认下单
        </el-button>
      </template>
    </el-dialog>

    <!-- 聊天对话框 -->
    <el-dialog v-model="chatDialogVisible" :title="`与 ${product.sellerName} 聊天`" width="500px">
      <div class="chat-container">
        <div class="chat-messages" ref="chatMessagesRef">
          <div
            v-for="(msg, index) in chatMessages"
            :key="index"
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Star, StarFilled } from '@element-plus/icons-vue'
import { getProductDetail } from '../api/product'
import { createOrder } from '../api/order'
import { addFavorite, removeFavorite } from '../api/favorite'
import { getChatHistory, sendMessage as sendMsg } from '../api/message'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const product = ref({})
const buyDialogVisible = ref(false)
const buyLoading = ref(false)
const orderForm = ref({
  buyerMessage: ''
})

// 聊天相关
const chatDialogVisible = ref(false)
const chatMessages = ref([])
const chatMessage = ref('')
const chatMessagesRef = ref(null)

const isMine = computed(() => {
  return userStore.userInfo?.id === product.value.sellerId
})

// 获取图片数组
const getImageArray = (images) => {
  if (!images) return []
  try {
    return JSON.parse(images)
  } catch {
    return [images]
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 加载商品详情
const loadProduct = async () => {
  loading.value = true
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
  } catch (error) {
    console.error('加载商品详情失败:', error)
    ElMessage.error('商品不存在')
    router.back()
  } finally {
    loading.value = false
  }
}

// 处理购买
const handleBuy = () => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  buyDialogVisible.value = true
}

// 确认下单
const confirmBuy = async () => {
  buyLoading.value = true
  try {
    await createOrder({
      productId: product.value.id,
      buyerMessage: orderForm.value.buyerMessage
    })
    ElMessage.success('下单成功')
    buyDialogVisible.value = false
    router.push('/my/orders')
  } catch (error) {
    console.error('下单失败:', error)
  } finally {
    buyLoading.value = false
  }
}

// 处理收藏
const handleFavorite = async () => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    if (product.value.isFavorite) {
      await removeFavorite(product.value.id)
      ElMessage.success('取消收藏成功')
      product.value.isFavorite = false
      product.value.favoriteCount--
    } else {
      await addFavorite(product.value.id)
      ElMessage.success('收藏成功')
      product.value.isFavorite = true
      product.value.favoriteCount++
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

// 打开聊天窗口
const handleChat = () => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  chatDialogVisible.value = true
  loadChatHistory()
}

// 加载聊天历史
const loadChatHistory = async () => {
  try {
    const res = await getChatHistory(product.value.sellerId)
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
      receiverId: product.value.sellerId,
      productId: product.value.id,
      content: chatMessage.value
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
  loadProduct()
})
</script>

<style scoped>
.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.empty-image {
  width: 100%;
  height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.price-box {
  margin-top: 20px;
  padding: 20px;
  background-color: #fff3f3;
  border-radius: 8px;
}

.price {
  color: #ff4d4f;
  font-size: 32px;
  font-weight: bold;
}

.original-price {
  color: #999;
  font-size: 18px;
  text-decoration: line-through;
  margin-left: 16px;
}

.description-box {
  margin-top: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.description-box h3 {
  margin-bottom: 12px;
  color: #303133;
}

.description-box p {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  gap: 12px;
}

/* 聊天组件样式 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 400px;
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
