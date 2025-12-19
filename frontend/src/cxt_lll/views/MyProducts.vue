<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的商品</span>
          <el-button type="primary" @click="router.push('/publish')">
            <el-icon><Plus /></el-icon>
            发布商品
          </el-button>
        </div>
      </template>

      <el-table :data="productList" v-loading="loading" stripe>
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <img
              :src="getProductImage(row.images)"
              style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px"
            />
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
        style="margin-top: 20px; justify-content: center; display: flex"
      />
    </el-card>
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
