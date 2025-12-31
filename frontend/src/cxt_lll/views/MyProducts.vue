<template>
  <div class="my-products-container">
    <div class="my-products-wrapper">
      <div class="header-section">
        <h2 class="page-title">我的商品</h2>
        <el-button type="primary" class="publish-button" @click="router.push('/publish')">
          <el-icon><Plus /></el-icon>
          发布商品
        </el-button>
      </div>
      
      <el-card class="products-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">商品管理</span>
          </div>
        </template>

        <el-table :data="productList" v-loading="loading" stripe class="products-table">
          <el-table-column label="商品图片" width="100">
            <template #default="{ row }">
              <div class="product-image-wrapper">
                <img
                  :src="getProductImage(row.images)"
                  class="product-image"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="商品名称" min-width="200" />
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">
              <span class="price">¥{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览量" width="100" />
          <el-table-column prop="favoriteCount" label="收藏数" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'ON_SALE'" type="success">在售</el-tag>
              <el-tag v-else-if="row.status === 'SOLD'" type="info">已售出</el-tag>
              <el-tag v-else type="warning">已下架</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  size="small"
                  @click="handleView(row.id)"
                >
                  查看
                </el-button>
                <el-button
                  v-if="row.status === 'ON_SALE'"
                  size="small"
                  type="warning"
                  @click="handleOffShelf(row.id)"
                >
                  下架
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  @click="handleDelete(row.id)"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="total > 0"
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadProducts"
          class="products-pagination"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyProducts, deleteProduct, offShelfProduct } from '../api/product'

const router = useRouter()
const loading = ref(false)
const productList = ref([])
const total = ref(0)

const pagination = reactive({
  current: 1,
  size: 10
})

// 加载我的商品
const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getMyProducts(pagination)
    productList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取商品图片
const getProductImage = (images) => {
  if (!images) return '/placeholder.png'
  try {
    const imageArray = JSON.parse(images)
    return imageArray[0] || '/placeholder.png'
  } catch {
    return images || '/placeholder.png'
  }
}

// 查看商品
const handleView = (id) => {
  router.push(`/product/${id}`)
}

// 下架商品
const handleOffShelf = (id) => {
  ElMessageBox.confirm('确定要下架该商品吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await offShelfProduct(id)
      ElMessage.success('下架成功')
      loadProducts()
    } catch (error) {
      console.error('下架失败:', error)
    }
  }).catch(() => {})
}

// 删除商品
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该商品吗？删除后无法恢复', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteProduct(id)
      ElMessage.success('删除成功')
      loadProducts()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
/* 容器样式 */
.my-products-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, rgba(232, 240, 247, 0.6) 100%);
  position: relative;
  padding: 24px;
}

.my-products-container::before {
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

.my-products-wrapper {
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

.publish-button {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  border: none;
  border-radius: var(--radius-md);
  padding: 12px 24px;
  font-weight: 500;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.publish-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.3);
}

/* 卡片样式 */
.products-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(255, 255, 255, 0.5);
  overflow: hidden;
}

.products-card :deep(.el-card__header) {
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

/* 表格样式 */
.products-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.products-table :deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, var(--bg-secondary) 0%, rgba(232, 240, 247, 0.3) 100%);
}

.products-table :deep(.el-table__header th) {
  background: transparent;
  color: var(--text-primary);
  font-weight: 600;
  border-bottom: 1px solid var(--border-light);
}

.products-table :deep(.el-table__body td) {
  border-bottom: 1px solid var(--border-light);
  transition: all var(--transition-normal);
}

.products-table :deep(.el-table__body tr:hover > td) {
  background: rgba(64, 158, 255, 0.05);
}

/* 商品图片样式 */
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

/* 价格样式 */
.price {
  color: var(--primary-color);
  font-weight: 600;
  font-size: 16px;
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
.products-pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.products-pagination :deep(.el-pager li) {
  border-radius: var(--radius-sm);
  margin: 0 2px;
  transition: all var(--transition-normal);
}

.products-pagination :deep(.el-pager li:hover) {
  color: var(--primary-color);
}

.products-pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  border-color: var(--primary-color);
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-products-container {
    padding: 16px;
  }
  
  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .page-title {
    font-size: 24px;
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
