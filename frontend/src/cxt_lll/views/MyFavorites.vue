<template>
  <div>
    <el-card>
      <template #header>
        <span>我的收藏</span>
      </template>

      <el-row :gutter="20" v-loading="loading">
        <el-col
          v-for="product in productList"
          :key="product.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card
            class="product-card"
            :body-style="{ padding: '0' }"
            shadow="hover"
          >
            <img
              :src="getProductImage(product.images)"
              class="product-image"
              alt="商品图片"
              @click="goToDetail(product.id)"
            />
            <div style="padding: 14px">
              <div class="product-title" @click="goToDetail(product.id)">
                {{ product.title }}
              </div>
              <div class="product-price">
                <span class="price">¥{{ product.price }}</span>
                <span v-if="product.originalPrice" class="original-price">
                  ¥{{ product.originalPrice }}
                </span>
              </div>
              <div class="product-info">
                <el-tag v-if="product.status === 'ON_SALE'" type="success" size="small">
                  在售
                </el-tag>
                <el-tag v-else type="info" size="small">已售出</el-tag>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="handleRemove(product.id)"
                >
                  取消收藏
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && productList.length === 0" description="暂无收藏" />

      <el-pagination
        v-if="total > 0"
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadFavorites"
        style="margin-top: 20px; justify-content: center; display: flex"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyFavorites, removeFavorite } from '../api/favorite'

const router = useRouter()
const loading = ref(false)
const productList = ref([])
const total = ref(0)

const pagination = reactive({
  current: 1,
  size: 12
})

// 加载收藏列表
const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getMyFavorites(pagination)
    productList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载收藏列表失败:', error)
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

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

// 取消收藏
const handleRemove = (productId) => {
  ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await removeFavorite(productId)
      ElMessage.success('取消收藏成功')
      loadFavorites()
    } catch (error) {
      console.error('取消收藏失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.product-card {
  margin-bottom: 20px;
  cursor: pointer;
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  background-color: #f5f5f5;
}

.product-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.product-title:hover {
  color: #409eff;
}

.product-price {
  margin-bottom: 8px;
}

.price {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: bold;
}

.original-price {
  color: #999;
  font-size: 14px;
  text-decoration: line-through;
  margin-left: 8px;
}

.product-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
