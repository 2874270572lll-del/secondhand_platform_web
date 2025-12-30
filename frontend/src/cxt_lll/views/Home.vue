<template>
  <div class="home-container">
    <!-- 头部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <el-icon><Shop /></el-icon>
          <span>校园二手交易</span>
        </div>

        <el-input
          v-model="keyword"
          placeholder="搜索商品"
          class="search-input"
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button icon="Search" @click="handleSearch" />
          </template>
        </el-input>

        <div class="nav-menu">
          <!-- 未登录状态 -->
          <el-button
            v-if="!userStore.isLoggedIn()"
            type="primary"
            @click="router.push('/login')"
          >
            登录
          </el-button>

          <!-- 已登录状态 -->
          <template v-else>
            <!-- 消息通知图标 -->
            <div class="message-icon" @click="router.push('/my/messages')">
              <MessageNotification />
            </div>

            <!-- 用户下拉菜单 -->
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar || undefined">
                  {{ userStore.userInfo?.nickname?.charAt(0) }}
                </el-avatar>
                <span style="margin-left: 8px">{{ userStore.userInfo?.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="my">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 分类导航 -->
    <div class="category-nav">
      <el-radio-group v-model="selectedCategory" @change="handleCategoryChange">
        <el-radio-button :label="0">全部</el-radio-button>
        <el-radio-button
          v-for="category in categories"
          :key="category.id"
          :label="category.id"
        >
          {{ category.name }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 商品列表 -->
<div class="product-list">
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
        @click="goToDetail(product.id)"
      >
        <img
          :src="getProductImage(product.images)"
          class="product-image"
          alt="商品图片"
        />
        <div style="padding: 14px">
          <div class="product-title">{{ product.title }}</div>
          <div class="product-price">
            <span class="price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="original-price">
              ¥{{ product.originalPrice }}
            </span>
          </div>
          <!-- 添加库存显示 -->
          <div class="product-stock">
            <el-icon><Box /></el-icon>
            库存: {{ product.quantity || 0 }} 件
          </div>
          <div class="product-info">
            <span>
              <el-icon><View /></el-icon>
              {{ product.viewCount }}
            </span>
            <span>
              <el-icon><Star /></el-icon>
              {{ product.favoriteCount }}
            </span>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>

      <!-- 空状态 -->
      <el-empty v-if="!loading && productList.length === 0" description="暂无商品" />

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadProducts"
        @size-change="loadProducts"
        style="margin-top: 20px; justify-content: center; display: flex"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, searchProducts } from '../api/product'
import { getCategoryList } from '../api/category'
import { useUserStore } from '../store/user'
import MessageNotification from '../components/MessageNotification.vue'

const router = useRouter()
const userStore = useUserStore()

const keyword = ref('')
const selectedCategory = ref(0)
const categories = ref([])
const productList = ref([])
const loading = ref(false)
const total = ref(0)

const pagination = reactive({
  current: 1,
  size: 12
})

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载商品列表
const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }

    if (selectedCategory.value > 0) {
      params.categoryId = selectedCategory.value
    }

    const res = keyword.value
      ? await searchProducts({ ...params, keyword: keyword.value })
      : await getProductList(params)

    productList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadProducts()
}

// 切换分类
const handleCategoryChange = () => {
  pagination.current = 1
  loadProducts()
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

// 下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'my':
      router.push('/my/products')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      }).then(() => {
        userStore.logout()
        ElMessage.success('已退出登录')
        loadProducts()
      }).catch(() => {})
      break
  }
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 64px;
  line-height: 64px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.logo .el-icon {
  margin-right: 8px;
  font-size: 24px;
}

.search-input {
  width: 400px;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 16px;
}

.message-icon {
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
}

.message-icon:hover {
  background-color: #f5f7fa;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.category-nav {
  background: #fff;
  padding: 16px 20px;
  margin-bottom: 20px;
  text-align: center;
}

.product-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-4px);
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
}

.product-price {
  margin-bottom: 8px;
}

.price {
  color: #ff4d4f;
  font-size: 20px;
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
  color: #909399;
  font-size: 14px;
}

.product-info span {
  display: flex;
  align-items: center;
}

.product-info .el-icon {
  margin-right: 4px;
}

.product-stock {
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.product-stock .el-icon {
  margin-right: 4px;
  color: #409eff;
}
</style>