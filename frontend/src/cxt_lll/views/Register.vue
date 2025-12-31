<template>
  <div class="register-container">
    <!-- 头部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/home')">
          <el-icon><Shop /></el-icon>
          <span>校园二手交易</span>
        </div>
        <el-button @click="router.push('/login')">
          已有账号，去登录
        </el-button>
      </div>
    </el-header>

    <!-- 注册表单 -->
    <div class="register-content">
      <el-card class="register-card">
        <template #header>
          <div class="card-header">
            <h2>注册账号</h2>
            <p>加入校园二手交易平台，开始交易</p>
          </div>
        </template>

        <el-form :model="registerForm" :rules="rules" ref="formRef" label-width="0">
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="nickname">
            <el-input
              v-model="registerForm.nickname"
              placeholder="请输入昵称（可选）"
              size="large"
              prefix-icon="Avatar"
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱（可选）"
              size="large"
              prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              style="width: 100%"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>

          <div class="footer-links">
            <span>已有账号？</span>
            <router-link to="/login">立即登录</router-link>
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
import { register } from '../api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: registerForm.username,
          password: registerForm.password,
          nickname: registerForm.nickname || registerForm.username,
          email: registerForm.email
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('注册失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.register-container::before {
  content: '';
  position: absolute;
  top: -10%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

.register-container::after {
  content: '';
  position: absolute;
  bottom: -15%;
  left: -15%;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
  animation: float 8s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 0;
  height: 64px;
  line-height: 64px;
  position: relative;
  z-index: 10;
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
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.logo:hover {
  transform: scale(1.05);
}

.logo .el-icon {
  margin-right: 10px;
  font-size: 26px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 已有账号按钮 */
.header-content :deep(.el-button) {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-md);
  padding: 8px 20px;
  font-weight: 500;
  color: var(--text-regular);
  transition: all var(--transition-fast);
}

.header-content :deep(.el-button):hover {
  background: var(--bg-primary);
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-2px);
}

.register-content {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
  position: relative;
  z-index: 10;
}

.register-card {
  width: 450px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.card-header {
  text-align: center;
  padding: 0;
}

.card-header h2 {
  margin-bottom: 8px;
  color: var(--text-primary);
  font-size: 26px;
  font-weight: 700;
}

.card-header p {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
  margin-bottom: 8px;
}

/* 表单样式 */
.register-card :deep(.el-card__body) {
  padding: 30px;
}

.register-card :deep(.el-form-item) {
  margin-bottom: 20px;
}

/* 输入框样式 */
.register-card :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  background-color: var(--bg-primary);
  transition: all var(--transition-normal);
  height: 50px;
  align-items: center;
}

.register-card :deep(.el-input__wrapper:hover) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

.register-card :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

.register-card :deep(.el-input__inner) {
  font-size: 15px;
  color: var(--text-primary);
  height: 50px;
  line-height: 50px;
}

.register-card :deep(.el-input__prefix-inner) {
  color: var(--text-secondary);
  font-size: 18px;
}

/* 注册按钮 */
.register-card :deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  border: none;
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-md);
  padding: 12px 0;
  font-size: 16px;
  font-weight: 600;
  height: 50px;
  transition: all var(--transition-normal);
  margin-top: 10px;
}

.register-card :deep(.el-button--primary:hover) {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.register-card :deep(.el-button--primary.is-loading) {
  transform: none;
  box-shadow: var(--shadow-sm);
}

.footer-links {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.footer-links a {
  color: var(--primary-color);
  font-weight: 500;
  transition: all var(--transition-fast);
  text-decoration: none;
}

.footer-links a:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 576px) {
  .register-card {
    width: 100%;
    max-width: 400px;
  }
  
  .register-card :deep(.el-card__body) {
    padding: 24px;
  }
  
  .card-header h2 {
    font-size: 22px;
  }
}
</style>
