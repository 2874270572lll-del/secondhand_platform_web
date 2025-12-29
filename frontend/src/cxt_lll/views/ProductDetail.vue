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
          <el-carousel 
            height="400px" 
            v-if="hasImages"
            :autoplay="imagesArray.length > 1"
          >
            <el-carousel-item v-for="(image, index) in imagesArray" :key="index">
              <img 
                :src="image" 
                :alt="`${product.title}-图片${index + 1}`"
                style="width: 100%; height: 100%; object-fit: contain" 
              />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="empty-image">
            <el-icon :size="100" color="#dcdfe6"><Picture /></el-icon>
            <p>暂无图片</p>
          </div>
        </el-col>

        <!-- 商品信息 -->
        <el-col :span="12">
          <h2 class="product-title">{{ product.title }}</h2>

          <div class="price-box">
            <span class="price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="original-price">
              原价: ¥{{ product.originalPrice }}
            </span>
            <el-tag v-if="product.discount" type="danger" size="small" class="discount-tag">
              {{ product.discount }}折
            </el-tag>
          </div>

          <el-descriptions :column="1" border style="margin-top: 20px">
            <el-descriptions-item label="分类">
              {{ product.categoryName || '未分类' }}
            </el-descriptions-item>
            <el-descriptions-item label="卖家">
              <el-link type="primary" :underline="false" @click="viewSeller">
                {{ product.sellerName }}
              </el-link>
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
            <el-descriptions-item label="库存" v-if="product.quantity !== undefined">
              {{ product.quantity }} 件
            </el-descriptions-item>
          </el-descriptions>

          <div class="description-box">
            <h3>商品描述</h3>
            <p v-if="product.description" class="description-text">{{ product.description }}</p>
            <p v-else class="no-description">暂无描述</p>
          </div>

          <div class="status-tags">
            <el-tag v-if="product.status === 'SOLD'" type="info" size="large">已售出</el-tag>
            <el-tag v-else-if="product.status === 'OFF_SHELF'" type="warning" size="large">已下架</el-tag>
            <el-tag v-else-if="isSoldOut" type="danger" size="large">已售罄</el-tag>
            <el-tag v-if="isMine" type="success" size="large">我的商品</el-tag>
          </div>

          <div class="action-buttons">
            <el-button
              v-if="canBuy"
              type="primary"
              size="large"
              :icon="ShoppingCart"
              @click="handleBuy"
              :loading="buyLoading"
            >
              立即购买
            </el-button>

            <el-button
              v-if="!isMine && product.status === 'ON_SALE'"
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
              :disabled="!userStore.isLoggedIn() || favoriteLoading"
              :loading="favoriteLoading"
            >
              {{ product.isFavorite ? '已收藏' : '收藏' }}
            </el-button>

            <el-button
              v-if="isMine"
              type="info"
              size="large"
              @click="handleEdit"
            >
              编辑商品
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 下单对话框 -->
    <el-dialog 
      v-model="buyDialogVisible" 
      title="确认下单" 
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form :model="orderForm" label-width="90px">
        <el-form-item label="商品名称">
          <span>{{ product.title }}</span>
        </el-form-item>
        <el-form-item label="单价">
          <span class="price">¥{{ product.price }}</span>
        </el-form-item>
        <el-form-item label="购买数量">
          <el-input-number
            v-model="orderForm.quantity"
            :min="1"
            :max="product.quantity || 1"
            controls-position="right"
            @change="validateQuantity"
            :disabled="product.quantity <= 0"
          />
          <span style="margin-left: 10px; color: #666;">
            库存: {{ product.quantity || 0 }} 件
          </span>
          <el-alert
            v-if="product.quantity <= 0"
            title="商品已售罄"
            type="error"
            :closable="false"
            style="margin-top: 10px;"
          />
        </el-form-item>
        <el-form-item label="总金额">
          <span class="total-price">¥{{ calculateTotal() }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buyDialogVisible = false" :disabled="buyLoading">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmBuy" 
          :loading="buyLoading"
          :disabled="product.quantity <= 0"
        >
          确认下单 (¥{{ calculateTotal() }})
        </el-button>
      </template>
    </el-dialog>

    <!-- 聊天对话框 -->
    <el-dialog 
      v-model="chatDialogVisible" 
      :title="`与 ${product.sellerName} 聊天`" 
      width="500px"
      @closed="resetChat"
    >
      <div class="chat-container">
        <div class="chat-messages" ref="chatMessagesRef">
          <div v-if="chatMessages.length === 0" class="empty-chat">
            <el-icon :size="40" color="#c0c4cc"><ChatLineRound /></el-icon>
            <p>还没有消息，开始聊天吧</p>
          </div>
          <div
            v-for="(msg, index) in chatMessages"
            :key="msg.id || index"
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
            :disabled="sendingMessage"
            clearable
          >
            <template #append>
              <el-button 
                @click="sendMessage" 
                type="primary"
                :disabled="!chatMessage.trim() || sendingMessage"
                :loading="sendingMessage"
              >
                发送
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ShoppingCart, 
  Star, 
  StarFilled, 
  Picture,
  ChatLineRound,
  Edit  // 添加 Edit 图标（只导入一次）
} from '@element-plus/icons-vue'
import { getProductDetail } from '../api/product'
import { createOrder } from '../api/order'
import { addFavorite, removeFavorite } from '../api/favorite'
import { getChatHistory, sendMessage as sendMsg } from '../api/message'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const product = ref({})
const buyDialogVisible = ref(false)
const buyLoading = ref(false)
const favoriteLoading = ref(false)
const orderForm = ref({
  quantity: 1,
  buyerMessage: ''
})

// 聊天相关
const chatDialogVisible = ref(false)
const chatMessages = ref([])
const chatMessage = ref('')
const chatMessagesRef = ref(null)
const sendingMessage = ref(false)

// 计算属性
const isMine = computed(() => {
  return userStore.userInfo?.id === product.value.sellerId
})

const imagesArray = computed(() => {
  return getImageArray(product.value.images)
})

const hasImages = computed(() => {
  return imagesArray.value.length > 0
})

const isSoldOut = computed(() => {
  return product.value.quantity !== undefined && product.value.quantity <= 0
})

const canBuy = computed(() => {
  return !isMine.value && 
         product.value.status === 'ON_SALE' && 
         !isSoldOut.value
})

// 监听库存变化
watch(
  () => product.value.quantity,
  (newQuantity) => {
    if (orderForm.value.quantity > newQuantity) {
      orderForm.value.quantity = newQuantity || 1
    }
  }
)

// 获取图片数组
const getImageArray = (images) => {
  if (!images) return []
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : [parsed]
  } catch {
    return typeof images === 'string' ? [images] : []
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载商品详情
const loadProduct = async () => {
  loading.value = true
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
  } catch (error) {
    console.error('加载商品详情失败:', error)
    ElMessage.error('商品不存在或已被删除')
    router.back()
  } finally {
    loading.value = false
  }
}

// 验证购买数量
const validateQuantity = () => {
  const maxQuantity = product.value.quantity || 0
  if (orderForm.value.quantity > maxQuantity) {
    orderForm.value.quantity = maxQuantity
    ElMessage.warning(`最大可购买数量为 ${maxQuantity} 件`)
  }
  if (orderForm.value.quantity < 1) {
    orderForm.value.quantity = 1
  }
}

// 计算总金额
const calculateTotal = () => {
  if (!product.value.price || !orderForm.value.quantity) return '0.00'
  return (product.value.price * orderForm.value.quantity).toFixed(2)
}

// 处理购买
const handleBuy = () => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (isSoldOut.value) {
    ElMessage.error('商品已售罄')
    return
  }
  
  // 重置数量
  orderForm.value.quantity = Math.min(1, product.value.quantity || 1)
  buyDialogVisible.value = true
}

// 确认下单
const confirmBuy = async () => {
  if (orderForm.value.quantity > product.value.quantity) {
    ElMessage.error('购买数量超过库存')
    return
  }
  
  buyLoading.value = true
  try {
    await createOrder({
      productId: product.value.id,
      quantity: orderForm.value.quantity,
      buyerMessage: orderForm.value.buyerMessage
    })
    
    ElMessage.success('下单成功！')
    buyDialogVisible.value = false
    
    // 刷新商品详情
    await loadProduct()
    
    // 询问用户是否跳转到订单页面
    ElMessageBox.confirm(
      '下单成功！是否跳转到我的订单页面？',
      '提示',
      {
        confirmButtonText: '去订单',
        cancelButtonText: '留在此页',
        type: 'success'
      }
    ).then(() => {
      router.push('/my/orders')
    })
  } catch (error) {
    console.error('下单失败:', error)
    ElMessage.error(error.response?.data?.message || '下单失败，请重试')
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

  favoriteLoading.value = true
  try {
    if (product.value.isFavorite) {
      await removeFavorite(product.value.id)
      ElMessage.success('取消收藏成功')
      product.value.isFavorite = false
      product.value.favoriteCount = Math.max(0, product.value.favoriteCount - 1)
    } else {
      await addFavorite(product.value.id)
      ElMessage.success('收藏成功')
      product.value.isFavorite = true
      product.value.favoriteCount++
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

// 查看卖家信息
const viewSeller = () => {
  if (product.value.sellerId) {
    router.push(`/user/${product.value.sellerId}`)
  }
}

// 编辑商品
const handleEdit = () => {
  router.push(`/product/edit/${product.value.id}`)
}

// 打开聊天窗口
const handleChat = async () => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (isMine.value) {
    ElMessage.warning('不能与自己聊天')
    return
  }
  
  chatDialogVisible.value = true
  await loadChatHistory()
}

// 加载聊天历史
const loadChatHistory = async () => {
  try {
    const res = await getChatHistory(product.value.sellerId)
    chatMessages.value = res.data || []
    
    // 滚动到底部
    await nextTick()
    scrollChatToBottom()
  } catch (error) {
    console.error('加载聊天历史失败:', error)
    ElMessage.error('加载聊天记录失败')
  }
}

// 滚动聊天到底部
const scrollChatToBottom = () => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

// 发送消息
const sendMessage = async () => {
  const content = chatMessage.value.trim()
  if (!content || sendingMessage.value) return

  sendingMessage.value = true
  try {
    const res = await sendMsg({
      receiverId: product.value.sellerId,
      productId: product.value.id,
      content: content
    })

    // 添加新消息到列表
    chatMessages.value.push(res.data)
    chatMessage.value = ''
    
    // 滚动到底部
    await nextTick()
    scrollChatToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    sendingMessage.value = false
  }
}

// 重置聊天
const resetChat = () => {
  chatMessages.value = []
  chatMessage.value = ''
  sendingMessage.value = false
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
  
  // 如果是今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }
  
  // 如果是昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })}`
  }
  
  return date.toLocaleDateString('zh-CN', {
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
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.empty-image p {
  margin-top: 12px;
  color: #909399;
}

.product-title {
  color: #303133;
  margin-bottom: 16px;
  font-size: 24px;
  line-height: 1.4;
}

.price-box {
  margin-top: 20px;
  padding: 20px;
  background-color: #fff3f3;
  border-radius: 8px;
  position: relative;
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

.discount-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.description-box {
  margin-top: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.description-box h3 {
  margin-bottom: 16px;
  color: #303133;
  font-size: 18px;
}

.description-text {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.no-description {
  color: #c0c4cc;
  font-style: italic;
}

.status-tags {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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
  min-height: 200px;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
}

.empty-chat p {
  margin-top: 12px;
  color: #909399;
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
  line-height: 1.5;
}

.chat-message.mine .message-content {
  background-color: #409eff;
  color: white;
}

.chat-message.other .message-content {
  background-color: white;
  color: #303133;
  border: 1px solid #e4e7ed;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.chat-input {
  margin-top: auto;
}

.total-price {
  color: #ff4d4f;
  font-size: 24px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .product-detail-container {
    padding: 10px;
  }
  
  .el-col {
    width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>