<template>
  <div>
    <el-card>
      <template #header>
        <span>我的订单</span>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 买家视角 -->
        <el-tab-pane label="我购买的" name="buy">
          <el-table :data="orderList" v-loading="loading" stripe>
            <el-table-column label="商品" min-width="300">
              <template #default="{ row }">
                <div style="display: flex; align-items: center">
                  <img
                    :src="row.productImage || '/placeholder.png'"
                    style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px; margin-right: 12px"
                  />
                  <div>
                    <div style="font-weight: 500">{{ row.productTitle }}</div>
                    <div style="color: #909399; font-size: 12px; margin-top: 4px">
                      订单号: {{ row.orderNo }}
                    </div>
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
                <el-tag v-if="row.status === 'PENDING'" type="warning">待支付</el-tag>
                <el-tag v-else-if="row.status === 'PAID'" type="primary">已支付</el-tag>
                <el-tag v-else-if="row.status === 'CONFIRMED'" type="info">已确认</el-tag>
                <el-tag v-else-if="row.status === 'SHIPPED'" type="success">已发货</el-tag>
                <el-tag v-else-if="row.status === 'RECEIVED'" type="success">已收货</el-tag>
                <el-tag v-else-if="row.status === 'COMPLETED'" type="info">已完成</el-tag>
                <el-tag v-else-if="row.status === 'REFUNDING'" type="warning">退款中</el-tag>
                <el-tag v-else-if="row.status === 'REFUNDED'" type="info">已退款</el-tag>
                <el-tag v-else type="danger">已取消</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <!-- 买家操作按钮组 -->
                <div v-if="activeTab === 'buy'">
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
          <el-table :data="orderList" v-loading="loading" stripe>
            <el-table-column label="商品" min-width="300">
              <template #default="{ row }">
                <div style="display: flex; align-items: center">
                  <img
                    :src="row.productImage || '/placeholder.png'"
                    style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px; margin-right: 12px"
                  />
                  <div>
                    <div style="font-weight: 500">{{ row.productTitle }}</div>
                    <div style="color: #909399; font-size: 12px; margin-top: 4px">
                      订单号: {{ row.orderNo }}
                    </div>
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
                <el-tag v-if="row.status === 'PENDING'" type="warning">待支付</el-tag>
                <el-tag v-else-if="row.status === 'PAID'" type="primary">已支付</el-tag>
                <el-tag v-else-if="row.status === 'CONFIRMED'" type="info">已确认</el-tag>
                <el-tag v-else-if="row.status === 'SHIPPED'" type="success">已发货</el-tag>
                <el-tag v-else-if="row.status === 'RECEIVED'" type="success">已收货</el-tag>
                <el-tag v-else-if="row.status === 'COMPLETED'" type="info">已完成</el-tag>
                <el-tag v-else-if="row.status === 'REFUNDING'" type="warning">退款中</el-tag>
                <el-tag v-else-if="row.status === 'REFUNDED'" type="info">已退款</el-tag>
                <el-tag v-else type="danger">已取消</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <!-- 卖家操作按钮组 -->
                <div v-if="activeTab === 'sell'">
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
        style="margin-top: 20px; justify-content: center; display: flex"
      />
    </el-card>
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
.price {
  color: #ff6b6b;
  font-weight: bold;
}
</style>