<template>
  <div class="my-container">
    <el-container>
      <!-- 侧边栏菜单 -->
      <el-aside width="200px">
        <el-menu
          :default-active="activeMenu"
          router
          class="side-menu"
        >
          <el-menu-item index="/my/products">
            <el-icon><Goods /></el-icon>
            <span>我的商品</span>
          </el-menu-item>
          <el-menu-item index="/my/orders">
            <el-icon><ShoppingCart /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="/my/favorites">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="/my/messages" class="message-menu-item">
            <el-icon><ChatDotSquare /></el-icon>
            <span>我的消息</span>
            <!-- 未读消息提示 -->
            <el-badge 
              v-if="messageStore.totalUnreadCount > 0"
              :value="messageStore.totalUnreadCount" 
              :max="99"
              class="menu-badge"
            />
          </el-menu-item>
          <el-menu-item index="/my/profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区域 -->
      <el-container>
        <el-header class="header">
          <el-page-header @back="router.push('/home')">
            <template #content>
              <span class="text-large font-600">个人中心</span>
            </template>
          </el-page-header>
        </el-header>

        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessageStore } from '../store/message'

const route = useRoute()
const router = useRouter()
const messageStore = useMessageStore()

const activeMenu = computed(() => route.path)

// 监听路由变化，当进入消息页面时更新未读数量
watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/my/messages') {
      // 可以在这里调用接口更新已读状态
      // 比如：messageStore.fetchUnreadCount()
    }
  }
)

// 组件挂载时获取未读消息数
onMounted(() => {
  messageStore.fetchUnreadCount()
})
</script>

<style scoped>
.my-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, rgba(232, 240, 247, 0.6) 100%);
  position: relative;
  display: flex;
  flex-direction: column;
}

/* 添加背景装饰 */
.my-container::before {
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

.my-container::after {
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

/* 侧边栏样式 */
:deep(.el-aside) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.95) 100%);
  backdrop-filter: blur(12px);
  box-shadow: 4px 0 12px rgba(0, 0, 0, 0.08);
  z-index: 10;
  transition: all var(--transition-normal);
  border-right: 1px solid var(--border-light);
}

.side-menu {
  height: 100%;
  border-right: none;
  background: transparent;
  padding: 24px 0;
}

/* 侧边栏菜单样式 */
.side-menu :deep(.el-menu-item) {
  color: var(--text-regular);
  font-weight: 500;
  font-size: 16px;
  height: 56px;
  line-height: 56px;
  margin: 6px 16px;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
  border: 1px solid transparent;
}

.side-menu :deep(.el-menu-item:hover) {
  color: var(--primary-color);
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.08) 0%, rgba(64, 158, 255, 0.04) 100%);
  border-color: rgba(64, 158, 255, 0.2);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.side-menu :deep(.el-menu-item.is-active) {
  color: var(--primary-color);
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.12) 0%, rgba(64, 158, 255, 0.06) 100%);
  border-color: rgba(64, 158, 255, 0.3);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.side-menu :deep(.el-menu-item .el-icon) {
  font-size: 20px;
  margin-right: 14px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.side-menu :deep(.el-menu-item:hover .el-icon),
.side-menu :deep(.el-menu-item.is-active .el-icon) {
  color: var(--primary-color);
  transform: scale(1.1);
}

/* 主容器样式 */
:deep(.el-container) {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 右侧内容容器 */
:deep(.el-container:nth-child(2)) {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

/* 头部样式 */
.header {
  background: var(--bg-primary);
  padding: 20px 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-bottom: 1px solid var(--border-light);
  transition: all var(--transition-normal);
}

.header :deep(.el-page-header__content) {
  color: var(--text-primary);
  font-size: 22px;
  font-weight: 700;
}

.header :deep(.el-page-header__back) {
  color: var(--text-regular);
  transition: all var(--transition-fast);
}

.header :deep(.el-page-header__back:hover) {
  color: var(--primary-color);
  transform: translateX(-4px);
}

/* 主内容区域 */
:deep(.el-main) {
  padding: 30px;
  background-color: var(--bg-secondary);
  overflow-y: auto;
  flex: 1;
}

/* 主内容卡片样式 */
:deep(.el-main > div) {
  background: rgba(var(--bg-primary-rgb, 255, 255, 255), 0.95);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 24px;
  transition: all var(--transition-normal);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

:deep(.el-main > div:hover) {
  box-shadow: 0 20px 40px rgba(64, 158, 255, 0.15);
}

/* 消息菜单项样式 */
.message-menu-item {
  position: relative;
}

.menu-badge {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
}

/* 消息徽章样式 */
.side-menu :deep(.el-badge) {
  background-color: var(--danger-color);
  font-size: 12px;
  font-weight: 600;
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  padding: 0 6px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.el-aside) {
    width: 70px !important;
  }
  
  .side-menu :deep(.el-menu-item span) {
    display: none;
  }
  
  .side-menu :deep(.el-menu-item .el-icon) {
    margin-right: 0;
  }
  
  .side-menu :deep(.el-menu-item) {
    justify-content: center;
    padding: 0;
  }
  
  .header {
    padding: 16px 20px;
  }
  
  :deep(.el-main) {
    padding: 20px;
  }
  
  :deep(.el-main > div) {
    padding: 16px;
  }
}
</style>