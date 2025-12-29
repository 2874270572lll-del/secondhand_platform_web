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
  background-color: #f5f7fa;
}

.side-menu {
  height: 100vh;
  border-right: solid 1px #e6e6e6;
}

.header {
  background: #fff;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.el-main {
  padding: 20px;
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
</style>