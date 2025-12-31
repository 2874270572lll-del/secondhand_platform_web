<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <h2 class="logo">二手市场</h2>
      </div>
      
      <div class="header-center">
        <el-menu 
          :default-active="activeMenu" 
          mode="horizontal" 
          @select="handleMenuSelect"
          :ellipsis="false"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/products">商品</el-menu-item>
          <el-menu-item index="/publish">发布</el-menu-item>
        </el-menu>
      </div>

      <div class="header-right">
        <!-- 消息通知图标 - 这里使用我们的MessageNotification组件 -->
        <div class="message-icon" @click="goToMessages">
          <MessageNotification />
        </div>

        <!-- 用户菜单 -->
        <el-dropdown v-if="userStore.userInfo" @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" :src="userStore.userInfo.avatar || undefined">
              {{ userStore.userInfo.nickname?.charAt(0) }}
            </el-avatar>
            <span class="username">{{ userStore.userInfo.nickname }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 未登录状态 -->
        <div v-else class="auth-buttons">
          <el-button @click="goToLogin" text>登录</el-button>
          <el-button @click="goToRegister" type="primary">注册</el-button>
        </div>
      </div>
    </el-header>

    <!-- 主内容区域 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'
import MessageNotification from '../components/MessageNotification.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 菜单选择
const handleMenuSelect = (index) => {
  router.push(index)
}

// 跳转到消息页面
const goToMessages = () => {
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/messages')
}

// 下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}

// 跳转到登录
const goToLogin = () => {
  router.push('/login')
}

// 跳转到注册
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-secondary);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.95) 100%);
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-md);
  height: 70px;
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all var(--transition-normal);
  border-bottom: 1px solid var(--border-light);
}

/* 滚动时导航栏效果 */
.header:hover {
  box-shadow: var(--shadow-lg);
  background: linear-gradient(135deg, rgba(255, 255, 255, 1) 0%, rgba(255, 255, 255, 0.98) 100%);
}

/* 添加微妙的渐变效果 */
.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0) 0%, rgba(64, 158, 255, 0.2) 50%, rgba(255, 255, 255, 0) 100%);
  pointer-events: none;
}

.header-left {
  flex-shrink: 0;
}

.logo {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary-color) 0%, #6a82fb 50%, #fcb045 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.05em;
  transition: all var(--transition-normal);
  position: relative;
  cursor: pointer;
}

.logo:hover {
  transform: scale(1.08);
  filter: drop-shadow(0 4px 8px rgba(64, 158, 255, 0.3));
}

.logo::before {
  content: '';
  position: absolute;
  left: -10px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, var(--primary-color) 0%, #6a82fb 100%);
  border-radius: 2px;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.logo:hover::before {
  opacity: 1;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  margin: 0 40px;
}

.header-center .el-menu {
  border-bottom: none;
  background-color: transparent;
}

/* 菜单样式覆盖 */
.header-center :deep(.el-menu-item) {
  color: var(--text-regular);
  font-weight: 500;
  font-size: 16px;
  height: 70px;
  line-height: 70px;
  padding: 0 26px;
  transition: all var(--transition-fast);
  position: relative;
  border-radius: 8px;
  margin: 0 4px;
}

.header-center :deep(.el-menu-item:hover) {
  color: var(--primary-color);
  background-color: var(--bg-secondary);
  transform: translateY(-2px);
}

.header-center :deep(.el-menu-item.is-active) {
  color: var(--primary-color);
  background-color: var(--bg-primary);
  border-bottom: none;
  box-shadow: inset 0 -2px 0 var(--primary-color);
}

.header-center :deep(.el-menu-item.is-active)::after {
  content: '';
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  width: 6px;
  height: 6px;
  background-color: var(--primary-color);
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.5);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.message-icon {
  cursor: pointer;
  padding: 10px;
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  color: var(--text-regular);
  position: relative;
}

.message-icon:hover {
  background-color: var(--bg-secondary);
  color: var(--primary-color);
  transform: translateY(-2px);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
  background-color: var(--bg-primary);
  border: 1px solid var(--border-light);
}

.user-info:hover {
  background-color: var(--bg-secondary);
  border-color: var(--primary-light);
  box-shadow: var(--shadow-sm);
  transform: translateY(-2px);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 用户头像样式 */
.user-info :deep(.el-avatar) {
  transition: all var(--transition-fast);
  border: 2px solid var(--bg-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-info:hover :deep(.el-avatar) {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.auth-buttons {
  display: flex;
  gap: 12px;
}

/* 登录注册按钮样式 */
.auth-buttons :deep(.el-button) {
  border-radius: var(--radius-lg);
  font-weight: 500;
  padding: 8px 20px;
  transition: all var(--transition-fast);
}

.auth-buttons :deep(.el-button--text) {
  color: var(--text-regular);
}

.auth-buttons :deep(.el-button--text:hover) {
  color: var(--primary-color);
  background-color: var(--bg-secondary);
}

.auth-buttons :deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  border: none;
  box-shadow: var(--shadow-sm);
}

.auth-buttons :deep(.el-button--primary:hover) {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.main-content {
  flex: 1;
  padding: 30px;
  background-color: var(--bg-secondary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 0 20px;
  }
  
  .header-center {
    margin: 0 20px;
  }
  
  .header-center :deep(.el-menu-item) {
    padding: 0 16px;
  }
  
  .username {
    display: none;
  }
  
  .main-content {
    padding: 20px;
  }
}
</style>