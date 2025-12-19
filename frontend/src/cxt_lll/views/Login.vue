<template>
  <div class="login-container">
    <!-- 头部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/home')">
          <el-icon><Shop /></el-icon>
          <span>校园二手交易</span>
        </div>
        <el-button type="primary" @click="router.push('/register')">
          立即注册
        </el-button>
      </div>
    </el-header>

    <!-- 登录表单 -->
    <div class="login-content">
      <el-card class="login-card">
        <template #header>
          <div class="card-header">
            <h2>欢迎登录</h2>
            <p>使用您的账号登录校园二手交易平台</p>
          </div>
        </template>

        <el-form :model="loginForm" :rules="rules" ref="formRef" label-width="0">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              style="width: 100%"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>

          <div class="footer-links">
            <span>还没有账号？</span>
            <router-link to="/register">立即注册</router-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Shop } from '@element-plus/icons-vue'
import { login } from '../api/user'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        userStore.setToken(res.data.token)
        userStore.setUserInfo(res.data.user)
        ElMessage.success('登录成功')
        router.push('/home')
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
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
  cursor: pointer;
}

.logo .el-icon {
  margin-right: 8px;
  font-size: 24px;
}

.login-content {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 80px 20px;
}

.login-card {
  width: 450px;
  border-radius: 12px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin-bottom: 8px;
  color: #303133;
  font-size: 24px;
}

.card-header p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.footer-links {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #606266;
}

.footer-links a {
  color: #409eff;
  margin-left: 8px;
}

.footer-links a:hover {
  text-decoration: underline;
}
</style>
