<template>
  <div class="my-orders-container">
    <div class="my-orders-wrapper">
      <div class="header-section">
        <h2 class="page-title">我的订单</h2>
      </div>
      
      <el-card class="orders-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">订单管理</span>
          </div>
        </template>

        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="orders-tabs">
          <!-- 买家视角 -->
          <el-tab-pane label="我购买的" name="buy">
            <el-table :data="orderList" v-loading="loading" stripe class="orders-table">
              <el-table-column label="商品" min-width="300">
                <template #default="{ row }">
                  <div class="product-info">
                    <div class="product-image-wrapper">
                      <img
                        :src="row.productImage || '/placeholder.png'"
                        class="product-image"
                      />
                    </div>
                    <div class="product-details">
                      <div class="product-title">{{ row.productTitle }}</div>
                      <div class="order-number">订单号: {{ row.orderNo }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="sellerName" label="卖家" width="120" />
              <el-table-column prop="price" label="价格" width="100">
                <template #default="{ row }">
                  <span class="price">¥{{ row.price }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag 
                    :type="getStatusType(row.status)" 
                    class="status-tag"
                  >
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="下单时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <!-- 买家操作按钮组 -->
                  <div v-if="activeTab === 'buy'" class="action-buttons">
                    <!-- 待支付订单 -->
                    <el-button
                      v-if="row.status === 'PENDING'"
                      size="small"
                      type="primary"
                      @click="handlePay(row.id)"
                    >
                      支付
                    </el-button>
                    <el-button
                      v-if="row.status === 'PENDING'"
                      size="small"
                      type="danger"
                      @click="handleCancel(row.id)"
                    >
                      取消订单
                    </el-button>
                    
                    <!-- 已发货订单 -->
                    <el-button
                      v-if="row.status === 'SHIPPED'"
                      size="small"
                      type="success"
                      @click="handleReceive(row.id)"
                    >
                      确认收货
                    </el-button>
                    
                    <!-- 已收货订单 -->
                    <el-button
                      v-if="row.status === 'RECEIVED'"
                      size="small"
                      type="info"
                      @click="handleComplete(row.id)"
                    >
                      完成订单
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 卖家视角 -->
          <el-tab-pane label="我卖出的" name="sell">
            <el-table :data="orderList" v-loading="loading" stripe class="orders-table">
              <el-table-column label="商品" min-width="300">
                <template #default="{ row }">
                  <div class="product-info">
                    <div class="product-image-wrapper">
                      <img
                        :src="row.productImage || '/placeholder.png'"
                        class="product-image"
                      />
                    </div>
                    <div class="product-details">
                      <div class="product-title">{{ row.productTitle }}</div>
                      <div class="order-number">订单号: {{ row.orderNo }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="buyerName" label="买家" width="120" />
              <el-table-column prop="price" label="价格" width="100">
                <template #default="{ row }">
                  <span class="price">¥{{ row.price }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag 
                    :type="getStatusType(row.status)" 
                    class="status-tag"
                  >
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="下单时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <!-- 卖家操作按钮组 -->
                  <div v-if="activeTab === 'sell'" class="action-buttons">
                    <!-- 已支付订单 -->
                    <el-button
                      v-if="row.status === 'PAID'"
                      size="small"
                      type="primary"
                      @click="handleConfirm(row.id)"
                    >
                      确认订单
                    </el-button>
                    
                    <!-- 已确认订单 -->
                    <el-button
                      v-if="row.status === 'CONFIRMED'"
                      size="small"
                      type="success"
                      @click="handleShip(row.id)"
                    >
                      发货
                    </el-button>
                    
                    <!-- 已收货订单 -->
                    <el-button
                      v-if="row.status === 'RECEIVED'"
                      size="small"
                      type="info"
                      @click="handleComplete(row.id)"
                    >
                      完成订单
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>

        <el-pagination
          v-if="total > 0"
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadOrders"
          class="orders-pagination"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getMyBuyOrders, 
  getMySellOrders, 
  payOrder, 
  shipOrder, 
  receiveOrder, 
  cancelOrder, 
  completeOrder ,
  confirmOrder,
} from '../api/order'

const loading = ref(false)
const activeTab = ref('buy')
const orderList = ref([])
const total = ref(0)

const pagination = reactive({
  current: 1,
  size: 10
})

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const api = activeTab.value === 'buy' ? getMyBuyOrders : getMySellOrders
    const res = await api(pagination)
    orderList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载订单失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换标签页
const handleTabChange = () => {
  pagination.current = 1
  loadOrders()
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'PAID': 'primary',
    'CONFIRMED': 'info',
    'SHIPPED': 'success',
    'RECEIVED': 'success',
    'COMPLETED': 'info',
    'REFUNDING': 'warning',
    'REFUNDED': 'info',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusTextMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'CONFIRMED': '已确认',
    'SHIPPED': '已发货',
    'RECEIVED': '已收货',
    'COMPLETED': '已完成',
    'REFUNDING': '退款中',
    'REFUNDED': '已退款',
    'CANCELLED': '已取消'
  }
  return statusTextMap[status] || '未知状态'
}

// 支付订单
const handlePay = (id) => {
  ElMessageBox.confirm('确认支付订单吗？', '支付确认', {
    type: 'info'
  }).then(async () => {
    try {
      await payOrder(id)
      ElMessage.success('支付成功')
      loadOrders()
    } catch (error) {
      console.error('支付失败:', error)
      ElMessage.error(error.response?.data?.message || '支付失败')
    }
  }).catch(() => {})
}

// 取消订单
const handleCancel = (id) => {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await cancelOrder(id)
      ElMessage.success('订单已取消')
      loadOrders()
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error(error.response?.data?.message || '取消失败')
    }
  }).catch(() => {})
}

// 卖家确认订单
const handleConfirm = (id) => {
  ElMessageBox.confirm('确认该订单吗？', '确认订单', {
    type: 'info'
  }).then(async () => {
    try {
      // 使用正确的 confirmOrder 函数
      await confirmOrder(id) 
      ElMessage.success('订单已确认')
      loadOrders()
    } catch (error) {
      console.error('确认订单失败:', error)
      ElMessage.error(error.response?.data?.message || '确认失败')
    }
  }).catch(() => {})
}

// 卖家发货
const handleShip = (id) => {
  ElMessageBox.confirm('确认已发货吗？', '发货确认', {
    type: 'success'
  }).then(async () => {
    try {
      await shipOrder(id)
      ElMessage.success('发货成功')
      loadOrders()
    } catch (error) {
      console.error('发货失败:', error)
      ElMessage.error(error.response?.data?.message || '发货失败')
    }
  }).catch(() => {})
}

// 买家确认收货
const handleReceive = (id) => {
  ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
    type: 'success'
  }).then(async () => {
    try {
      await receiveOrder(id)
      ElMessage.success('确认收货成功')
      loadOrders()
    } catch (error) {
      console.error('确认收货失败:', error)
      ElMessage.error(error.response?.data?.message || '确认失败')
    }
  }).catch(() => {})
}

// 完成订单
const handleComplete = (id) => {
  ElMessageBox.confirm('确认订单已完成吗？', '订单完成', {
    type: 'info'
  }).then(async () => {
    try {
      await completeOrder(id)
      ElMessage.success('订单已完成')
      loadOrders()
    } catch (error) {
      console.error('完成订单失败:', error)
      ElMessage.error(error.response?.data?.message || '完成失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
/* 容器样式 */
.my-orders-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, rgba(232, 240, 247, 0.6) 100%);
  position: relative;
  padding: 24px;
}

.my-orders-container::before {
  content: '';
  position: absolute;
  top: -20%;
  right: -10%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.08) 0%, rgba(64, 158, 255, 0) 70%);
  border-radius: 50%;
  z-index: 0;
}

.my-orders-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 头部区域 */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 卡片样式 */
.orders-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(255, 255, 255, 0.5);
  overflow: hidden;
}

.orders-card :deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.02) 0%, rgba(64, 158, 255, 0.01) 100%);
  border-bottom: 1px solid var(--border-light);
  padding: 20px 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

/* 标签页样式 */
.orders-tabs :deep(.el-tabs__header) {
  margin: 0 0 24px 0;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, rgba(232, 240, 247, 0.3) 100%);
  border-radius: var(--radius-md);
  padding: 4px;
}

.orders-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.orders-tabs :deep(.el-tabs__item) {
  color: var(--text-regular);
  font-weight: 500;
  border-radius: var(--radius-sm);
  transition: all var(--transition-normal);
  margin: 2px;
}

.orders-tabs :deep(.el-tabs__item.is-active) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.orders-tabs :deep(.el-tabs__item:hover) {
  color: var(--primary-color);
}

/* 表格样式 */
.orders-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.orders-table :deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, var(--bg-secondary) 0%, rgba(232, 240, 247, 0.3) 100%);
}

.orders-table :deep(.el-table__header th) {
  background: transparent;
  color: var(--text-primary);
  font-weight: 600;
  border-bottom: 1px solid var(--border-light);
}

.orders-table :deep(.el-table__body td) {
  border-bottom: 1px solid var(--border-light);
  transition: all var(--transition-normal);
}

.orders-table :deep(.el-table__body tr:hover > td) {
  background: rgba(64, 158, 255, 0.05);
}

/* 商品信息样式 */
.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image-wrapper {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-sm);
  transition: all var(--transition-normal);
}

.product-image-wrapper:hover {
  transform: scale(1.1);
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  transition: all var(--transition-normal);
}

.product-image-wrapper:hover .product-image {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.product-details {
  flex: 1;
}

.product-title {
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: 4px;
}

.order-number {
  color: var(--text-secondary);
  font-size: 12px;
}

/* 价格样式 */
.price {
  color: var(--primary-color);
  font-weight: 600;
  font-size: 16px;
}

/* 状态标签样式 */
.status-tag {
  border-radius: var(--radius-sm);
  font-weight: 500;
  border: none;
  padding: 4px 8px;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  border-radius: var(--radius-sm);
  font-weight: 500;
  transition: all var(--transition-normal);
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 分页样式 */
.orders-pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.orders-pagination :deep(.el-pager li) {
  border-radius: var(--radius-sm);
  margin: 0 2px;
  transition: all var(--transition-normal);
}

.orders-pagination :deep(.el-pager li:hover) {
  color: var(--primary-color);
}

.orders-pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  border-color: var(--primary-color);
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-orders-container {
    padding: 16px;
  }
  
  .header-section {
    justify-content: center;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .product-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
    margin-bottom: 4px;
  }
}
</style>