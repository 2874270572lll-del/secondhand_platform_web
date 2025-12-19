<template>
  <div>
    <el-card>
      <template #header>
        <span>个人资料</span>
      </template>

      <el-form
        ref="formRef"
        :model="profileForm"
        label-width="100px"
        style="max-width: 600px"
      >
        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="头像">
          <div style="display: flex; align-items: center; gap: 20px">
            <el-avatar :size="80" :src="profileForm.avatar || undefined">
              {{ profileForm.nickname?.charAt(0) }}
            </el-avatar>
            <el-upload
              action="/api/upload/image"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
              accept="image/*"
            >
              <el-button type="primary" size="small">上传头像</el-button>
            </el-upload>
          </div>
          <div style="margin-top: 8px; color: #909399; font-size: 12px">
            支持 jpg、png 格式，大小不超过 2MB
          </div>
        </el-form-item>

        <el-form-item label="注册时间">
          <el-input :value="formatDate(profileForm.createdAt)" disabled />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleUpdate" :loading="loading">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentUser, updateUser } from '../api/user'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const profileForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  createdAt: ''
})

const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

// 上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传成功回调
const handleAvatarSuccess = async (response) => {
  if (response.code === 200) {
    profileForm.avatar = response.data
    ElMessage.success('头像上传成功')
    // 自动保存头像到服务器
    try {
      const res = await updateUser({
        nickname: profileForm.nickname,
        email: profileForm.email,
        phone: profileForm.phone,
        avatar: profileForm.avatar
      })
      userStore.setUserInfo(res.data)
    } catch (error) {
      console.error('保存头像失败:', error)
    }
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 加载用户信息
const loadProfile = async () => {
  try {
    const res = await getCurrentUser()
    Object.assign(profileForm, res.data)
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 更新用户信息
const handleUpdate = async () => {
  loading.value = true
  try {
    const res = await updateUser({
      nickname: profileForm.nickname,
      email: profileForm.email,
      phone: profileForm.phone,
      avatar: profileForm.avatar
    })
    userStore.setUserInfo(res.data)
    ElMessage.success('更新成功')
  } catch (error) {
    console.error('更新失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
</style>
