<template>
  <div class="publish-container">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="text-large font-600 mr-3">发布商品</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 20px">
      <el-form
        ref="formRef"
        :model="productForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="商品标题" prop="title">
          <el-input
            v-model="productForm.title"
            placeholder="请输入商品标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品价格" prop="price">
          <el-input-number
            v-model="productForm.price"
            :min="0.01"
            :precision="2"
            :step="0.1"
            placeholder="请输入价格"
          />
          <span style="margin-left: 10px; color: #909399">元</span>
        </el-form-item>

        <el-form-item label="原价">
          <el-input-number
            v-model="productForm.originalPrice"
            :min="0.01"
            :precision="2"
            :step="0.1"
            placeholder="可选，用于展示折扣"
          />
          <span style="margin-left: 10px; color: #909399">元</span>
        </el-form-item>

        <el-form-item label="商品数量" prop="quantity">
          <el-input-number
            v-model="productForm.quantity"
            :min="1"
            :step="1"
            placeholder="请输入库存数量"
          />
          <span style="margin-left: 10px; color: #909399">件</span>
        </el-form-item>

        <el-form-item label="所在地">
          <el-input
            v-model="productForm.location"
            placeholder="例如：南京市、仙林校区"
          />
        </el-form-item>

        <el-form-item label="联系方式">
          <el-input
            v-model="productForm.contact"
            placeholder="QQ、微信或手机号"
          />
        </el-form-item>

        <el-form-item label="商品图片" prop="imageList">
          <el-upload
            v-model:file-list="fileList"
            action="/api/upload/image"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            :headers="uploadHeaders"
            :limit="5"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="margin-top: 8px; color: #909399; font-size: 12px">
            最多上传5张图片，支持 jpg、png、gif 格式
          </div>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述商品的情况、成色、使用时长等信息"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            发布商品
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createProduct } from '../api/product'
import { getCategoryList } from '../api/category'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const categories = ref([])
const fileList = ref([])
const imageUrls = ref([])

const productForm = reactive({
  title: '',
  categoryId: null,
  price: null,
  originalPrice: null,
  quantity: 1,
  location: '',
  contact: '',
  imageList: [],
  description: ''
})

const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  quantity: [
    { required: true, message: '请输入商品数量', trigger: 'blur' }
  ],
  imageList: [
    {
      validator: (rule, value, callback) => {
        if (imageUrls.value.length === 0) {
          callback(new Error('请上传至少一张商品图片'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' }
  ]
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 文件上传前的钩子
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 上传成功回调
const handleUploadSuccess = (response, file, files) => {
  if (response.code === 200) {
    imageUrls.value.push(response.data)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
    // 移除上传失败的文件
    const index = files.findIndex(f => f.uid === file.uid)
    if (index > -1) {
      files.splice(index, 1)
    }
  }
}

// 移除图片
const handleRemove = (file, files) => {
  // 从imageUrls中移除对应的URL
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index > -1 && index < imageUrls.value.length) {
    imageUrls.value.splice(index, 1)
  }
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const data = {
          ...productForm,
          images: JSON.stringify(imageUrls.value)
        }
        delete data.imageList

        await createProduct(data)
        ElMessage.success('发布成功')
        router.push('/my/products')
      } catch (error) {
        console.error('发布失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
</style>
