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
  background: linear-gradient(135deg, #f0f4f8 0%, #e6f2ff 100%);
  position: relative;
}

/* 添加背景装饰 */
.home-container::before {
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

.home-container::after {
  content: '';
  position: absolute;
  bottom: -20%;
  left: -10%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(108, 92, 231, 0.06) 0%, rgba(108, 92, 231, 0) 70%);
  border-radius: 50%;
  z-index: 0;
}

.header {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.95) 100%);
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-md);
  padding: 0;
  height: 70px;
  line-height: 70px;
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid var(--border-light);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 20;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary-color) 0%, #6a82fb 50%, #fcb045 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.logo:hover {
  transform: scale(1.05);
  filter: drop-shadow(0 4px 8px rgba(64, 158, 255, 0.3));
}

.logo .el-icon {
  margin-right: 10px;
  font-size: 26px;
  background: linear-gradient(135deg, var(--primary-color) 0%, #6a82fb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 搜索框样式 */
.search-input {
  width: 400px;
  transition: all var(--transition-normal);
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 30px;
  border: 1px solid var(--border-light);
  background-color: var(--bg-primary);
  box-shadow: var(--shadow-sm);
  height: 45px;
  transition: all var(--transition-normal);
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
  width: 420px;
}

.search-input :deep(.el-input__inner) {
  height: 45px;
  line-height: 45px;
  font-size: 15px;
}

.search-input :deep(.el-input-group__append) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  border: none;
  border-radius: 30px;
  padding: 0;
}

.search-input :deep(.el-input-group__append .el-button) {
  border-radius: 30px;
  height: 45px;
  width: 45px;
  padding: 0;
  color: white;
  background: transparent;
  border: none;
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
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 20px 20px;
  margin-bottom: 30px;
  text-align: center;
  border-bottom: 1px solid var(--border-lighter);
  position: relative;
  z-index: 1;
}

/* 分类按钮样式 */
.category-nav :deep(.el-radio-button__inner) {
  border: 1px solid var(--border-light);
  background-color: var(--bg-primary);
  color: var(--text-regular);
  font-size: 15px;
  padding: 10px 20px;
  border-radius: 25px;
  transition: all var(--transition-normal);
  margin: 0 5px;
  box-shadow: var(--shadow-sm);
}

.category-nav :deep(.el-radio-button__inner:hover) {
  color: var(--primary-color);
  border-color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.category-nav :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  border-color: var(--primary-color);
  color: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transform: translateY(-2px);
}

.product-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
  position: relative;
  z-index: 1;
}

.product-card {
  margin-bottom: 24px;
  cursor: pointer;
  transition: all var(--transition-normal);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-light);
  background-color: var(--bg-primary);
  position: relative;
  box-shadow: var(--shadow-sm);
}

.product-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(64, 158, 255, 0.15);
  border-color: var(--primary-light);
}

.product-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0) 0%, rgba(64, 158, 255, 0.05) 100%);
  opacity: 0;
  transition: opacity var(--transition-normal);
  pointer-events: none;
  z-index: 1;
}

.product-card:hover::before {
  opacity: 1;
}

.product-image {
  width: 100%;
  height: 220px;
  object-fit: cover;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  transition: all var(--transition-normal);
  border-bottom: 1px solid var(--border-light);
  position: relative;
  z-index: 0;
}

.product-card:hover .product-image {
  transform: scale(1.05);
  filter: brightness(1.05);
}

/* 商品卡片内容容器 */
.product-card :deep(.el-card__body) {
  padding: 16px;
}

.product-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  height: 48px;
  transition: all var(--transition-fast);
  position: relative;
  z-index: 2;
}

.product-card:hover .product-title {
  color: var(--primary-color);
}

.product-price {
  margin-bottom: 12px;
  display: flex;
  align-items: baseline;
  position: relative;
  z-index: 2;
}

.price {
  color: var(--danger-color);
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.5px;
  line-height: 1.2;
  position: relative;
  padding-left: 4px;
}

.price::before {
  content: '¥';
  position: absolute;
  left: -8px;
  font-size: 16px;
  color: var(--danger-color);
  font-weight: 600;
}

.original-price {
  color: var(--text-secondary);
  font-size: 13px;
  text-decoration: line-through;
  margin-left: 12px;
  position: relative;
}

.original-price::after {
  content: '原价';
  position: absolute;
  top: -18px;
  left: 0;
  font-size: 10px;
  color: var(--text-secondary);
  background-color: var(--bg-secondary);
  padding: 0 4px;
  border-radius: 2px;
}

.product-stock {
  margin-bottom: 12px;
  color: var(--text-regular);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
  z-index: 2;
  padding: 4px 8px;
  background-color: rgba(64, 158, 255, 0.05);
  border-radius: 6px;
  width: fit-content;
  transition: all var(--transition-fast);
}

.product-card:hover .product-stock {
  background-color: rgba(64, 158, 255, 0.1);
}

.product-stock .el-icon {
  color: var(--primary-color);
  font-size: 14px;
}

.product-info {
  display: flex;
  justify-content: space-between;
  color: var(--text-secondary);
  font-size: 12px;
  padding-top: 10px;
  border-top: 1px solid var(--border-lighter);
  position: relative;
  z-index: 2;
}

.product-info span {
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all var(--transition-fast);
  padding: 2px 6px;
  border-radius: 4px;
}

.product-card:hover .product-info span {
  color: var(--primary-color);
  background-color: rgba(64, 158, 255, 0.05);
}

.product-info .el-icon {
  font-size: 13px;
}
</style>