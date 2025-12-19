<template>
  <div>
    <el-card>
      <template #header>
        <span>我的订单</span>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
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
                <el-tag v-if="row.status === 'PENDING'" type="warning">待确认</el-tag>
                <el-tag v-else-if="row.status === 'CONFIRMED'" type="success">已确认</el-tag>
                <el-tag v-else-if="row.status === 'COMPLETED'" type="info">已完成</el-tag>
                <el-tag v-else type="danger">已取消</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'PENDING'"
                  size="small"
                  type="danger"
                  @click="handleCancel(row.id)"
                >
                  取消订单
                </el-button>
                <el-button
                  v-if="row.status === 'CONFIRMED'"
                  size="small"
                  type="success"
                  @click="handleComplete(row.id)"
                >
                  确认完成
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

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
                <el-tag v-if="row.status === 'PENDING'" type="warning">待确认</el-tag>
                <el-tag v-else-if="row.status === 'CONFIRMED'" type="success">已确认</el-tag>
                <el-tag v-else-if="row.status === 'COMPLETED'" type="info">已完成</el-tag>
                <el-tag v-else type="danger">已取消</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'PENDING'"
                  size="small"
                  type="primary"
                  @click="handleConfirm(row.id)"
                >
                  确认订单
                </el-button>
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
import { getMyBuyOrders, getMySellOrders, updateOrderStatus } from '../api/order'

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

// 取消订单
const handleCancel = (id) => {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await updateOrderStatus(id, 'CANCELLED')
      ElMessage.success('订单已取消')
      loadOrders()
    } catch (error) {
      console.error('取消订单失败:', error)
    }
  }).catch(() => {})
}

// 确认订单
const handleConfirm = (id) => {
  ElMessageBox.confirm('确认该订单吗？', '提示', {
    type: 'info'
  }).then(async () => {
    try {
      await updateOrderStatus(id, 'CONFIRMED')
      ElMessage.success('订单已确认')
      loadOrders()
    } catch (error) {
      console.error('确认订单失败:', error)
    }
  }).catch(() => {})
}

// 完成订单
const handleComplete = (id) => {
  ElMessageBox.confirm('确认已完成交易吗？', '提示', {
    type: 'success'
  }).then(async () => {
    try {
      await updateOrderStatus(id, 'COMPLETED')
      ElMessage.success('订单已完成')
      loadOrders()
    } catch (error) {
      console.error('完成订单失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
</style>
