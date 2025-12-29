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
              <el-dropdown-item command="messages">我的消息</el-dropdown-item>
              <el-dropdown-item command="settings">设置</el-dropdown-item>
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
    case 'messages':
      router.push('/messages')
      break
    case 'settings':
      router.push('/settings')
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
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  height: 60px;
}

.header-left {
  flex-shrink: 0;
}

.logo {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.header-center .el-menu {
  border-bottom: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
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

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
}

.auth-buttons {
  display: flex;
  gap: 12px;
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
}
</style>