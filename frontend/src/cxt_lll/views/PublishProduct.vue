<template>
  <div class="publish-container">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="text-large font-600 mr-3">
          {{ isEditMode ? '编辑商品' : '发布商品' }}
        </span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 20px">
      <el-form
        ref="formRef"
        :model="productForm"
        :rules="rules"
        label-width="100px"
        v-loading="loading"
      >
        <!-- 商品标题（编辑模式下禁用） -->
        <el-form-item label="商品标题" prop="title">
          <el-input
            v-model="productForm.title"
            placeholder="请输入商品标题"
            maxlength="100"
            show-word-limit
            :disabled="isEditMode"
          />
          <div v-if="isEditMode" style="color: #909399; font-size: 12px; margin-top: 4px;">
            商品标题不可修改
          </div>
        </el-form-item>

        <!-- 商品分类（编辑模式下禁用） -->
        <el-form-item label="商品分类" prop="categoryId" v-if="!isEditMode">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        
        <!-- 编辑模式下显示分类（只读） -->
        <el-form-item label="商品分类" v-if="isEditMode">
          <el-input
            :value="selectedCategoryName"
            disabled
            placeholder="分类不可修改"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            商品分类不可修改
          </div>
        </el-form-item>

        <!-- 商品价格（可修改） -->
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

        <!-- 原价（编辑模式下禁用） -->
        <el-form-item label="原价" v-if="!isEditMode">
          <el-input-number
            v-model="productForm.originalPrice"
            :min="0.01"
            :precision="2"
            :step="0.1"
            placeholder="可选，用于展示折扣"
          />
          <span style="margin-left: 10px; color: #909399">元</span>
        </el-form-item>
        
        <!-- 编辑模式下显示原价（只读） -->
        <el-form-item label="原价" v-if="isEditMode && productForm.originalPrice">
          <el-input
            :value="productForm.originalPrice"
            disabled
            placeholder="无"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            原价不可修改
          </div>
        </el-form-item>

        <!-- 商品数量（可修改） -->
        <el-form-item label="商品数量" prop="quantity">
          <el-input-number
            v-model="productForm.quantity"
            :min="1"
            :step="1"
            placeholder="请输入库存数量"
          />
          <span style="margin-left: 10px; color: #909399">件</span>
        </el-form-item>

        <!-- 所在地（编辑模式下禁用） -->
        <el-form-item label="所在地" v-if="!isEditMode">
          <el-input
            v-model="productForm.location"
            placeholder="例如：南京市、仙林校区"
          />
        </el-form-item>
        
        <!-- 编辑模式下显示所在地（只读） -->
        <el-form-item label="所在地" v-if="isEditMode">
          <el-input
            :value="productForm.location || '未填写'"
            disabled
            placeholder="未填写"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            所在地不可修改
          </div>
        </el-form-item>

        <!-- 联系方式（可修改） -->
        <el-form-item label="联系方式" prop="contact">
          <el-input
            v-model="productForm.contact"
            placeholder="QQ、微信或手机号"
            maxlength="50"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            买家将通过此方式联系您
          </div>
        </el-form-item>

        <!-- 商品图片（可修改） -->
        <el-form-item label="商品图片" prop="imageList">
          <el-upload
            v-model:file-list="fileList"
            :action="uploadAction"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            :headers="uploadHeaders"
            :limit="5"
            accept="image/*"
            :on-preview="handlePictureCardPreview"
            :on-exceed="handleExceed"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="margin-top: 8px; color: #909399; font-size: 12px">
            最多上传5张图片，支持 jpg、png、gif 格式
          </div>
        </el-form-item>

        <!-- 商品描述（可修改） - 移除了maxlength限制 -->
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述商品的情况、成色、使用时长等信息"
            show-word-limit
          />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            请详细描述您的商品，没有字符数限制
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEditMode ? '保存修改' : '发布商品' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="dialogVisible" title="图片预览" width="50%">
      <img :src="dialogImageUrl" style="width: 100%; max-height: 70vh; object-fit: contain" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createProduct, updateProduct, getProductDetail } from '../api/product'
import { getCategoryList } from '../api/category'

const route = useRoute()
const router = useRouter()

// 判断是否是编辑模式
const isEditMode = computed(() => route.path.includes('/product/edit/') || route.name === 'EditProduct')
const productId = computed(() => route.params.id)

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const categories = ref([])
const fileList = ref([])
const imageUrls = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)

// 商品表单数据
const productForm = reactive({
  title: '',
  categoryId: null,
  price: null,
  originalPrice: null,
  quantity: 1,
  location: '',
  contact: '',
  description: ''
})

// 计算已选择的分类名称
const selectedCategoryName = computed(() => {
  if (!productForm.categoryId) return '未选择'
  const category = categories.value.find(c => c.id === productForm.categoryId)
  return category ? category.name : '未知分类'
})

// 上传相关配置
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

const uploadAction = computed(() => {
  return '/api/upload/image'
})

// 验证规则（根据编辑模式调整）
const rules = computed(() => {
  const baseRules = {
    price: [
      { required: true, message: '请输入商品价格', trigger: 'blur' },
      { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }
    ],
    quantity: [
      { required: true, message: '请输入商品数量', trigger: 'blur' },
      { type: 'number', min: 1, message: '库存必须大于0', trigger: 'blur' }
    ],
    description: [
      { required: true, message: '请输入商品描述', trigger: 'blur' }
      // 移除了长度限制
    ]
  }
  
  // 发布模式下需要标题和分类
  if (!isEditMode.value) {
    baseRules.title = [
      { required: true, message: '请输入商品标题', trigger: 'blur' },
      { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
    ]
    baseRules.categoryId = [
      { required: true, message: '请选择商品分类', trigger: 'change' }
    ]
  } else {
    // 编辑模式下标题不需要必填验证（因为不可修改且已存在）
    baseRules.title = []
  }
  
  return baseRules
})

// 如果是编辑模式，加载商品数据
const loadProduct = async () => {
  if (!isEditMode.value) return
  
  loading.value = true
  try {
    const res = await getProductDetail(productId.value)
    const product = res.data
    
    // 填充所有字段，包括不可修改的字段（用于显示）
    Object.keys(productForm).forEach(key => {
      if (product[key] !== undefined && product[key] !== null) {
        productForm[key] = product[key]
      }
    })
    
    // 处理图片
    if (product.images) {
      try {
        const imagesArray = JSON.parse(product.images)
        if (Array.isArray(imagesArray) && imagesArray.length > 0) {
          imageUrls.value = imagesArray
          fileList.value = imagesArray.map((url, index) => ({
            uid: `existing-${index}`,
            name: `image${index + 1}.jpg`,
            url: url,
            status: 'success'
          }))
        }
      } catch (e) {
        console.error('解析图片失败:', e)
      }
    }
  } catch (error) {
    console.error('加载商品详情失败:', error)
    ElMessage.error('商品不可编辑或已被删除')
    router.back()
  } finally {
    loading.value = false
  }
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
  if (file.url) {
    const index = imageUrls.value.findIndex(url => url === file.url)
    if (index > -1) {
      imageUrls.value.splice(index, 1)
    }
  }
  // 如果是新上传的文件，从文件列表中移除
  if (file.response && file.response.data) {
    const index = imageUrls.value.findIndex(url => url === file.response.data)
    if (index > -1) {
      imageUrls.value.splice(index, 1)
    }
  }
}

// 图片预览
const handlePictureCardPreview = (file) => {
  dialogImageUrl.value = file.url || file.response?.data
  dialogVisible.value = true
}

// 超出限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传5张图片')
}

// 图片验证规则
const validateImages = () => {
  if (imageUrls.value.length === 0) {
    return '请上传至少一张商品图片'
  }
  return true
}

// 准备提交的数据（编辑模式下只提交可修改的字段）
const prepareSubmitData = () => {
  const data = {}
  
  // 可修改的字段
  if (productForm.price !== undefined && productForm.price !== null) {
    data.price = productForm.price
  }
  if (productForm.quantity !== undefined && productForm.quantity !== null) {
    data.quantity = productForm.quantity
  }
  if (productForm.contact !== undefined && productForm.contact !== null) {
    data.contact = productForm.contact
  }
  if (productForm.description !== undefined && productForm.description !== null) {
    data.description = productForm.description
  }
  if (imageUrls.value.length > 0) {
    data.images = JSON.stringify(imageUrls.value)
  }
  
  return data
}

// 提交表单
const handleSubmit = async () => {
  try {
    // 验证表单 - 但跳过不可修改的字段验证
    await formRef.value.validate()
    
    // 验证图片
    const imageValidation = validateImages()
    if (imageValidation !== true) {
      ElMessage.error(imageValidation)
      return
    }
    
    submitting.value = true
    
    if (isEditMode.value) {
      // 编辑模式：只更新可修改的字段
      const updateData = prepareSubmitData()
      console.log('提交的更新数据:', updateData) // 添加调试日志
      
      await updateProduct(productId.value, updateData)
      ElMessage.success('商品修改成功')
    } else {
      // 发布模式：提交所有字段
      const data = {
        ...productForm,
        images: JSON.stringify(imageUrls.value)
      }
      console.log('提交的发布数据:', data) // 添加调试日志
      
      await createProduct(data)
      ElMessage.success('商品发布成功')
    }
    
    // 跳转到我的商品页面
    router.push('/my/products')
  } catch (error) {
    console.error('操作失败:', error)
    
    // 更详细的错误处理
    if (error.response) {
      console.error('响应数据:', error.response.data)
      console.error('响应状态:', error.response.status)
      console.error('响应头:', error.response.headers)
      
      if (error.response.data?.message) {
        ElMessage.error(error.response.data.message)
      } else if (error.response.data) {
        // 尝试解析验证错误
        try {
          const errorMsg = JSON.stringify(error.response.data)
          ElMessage.error(`请求失败: ${errorMsg}`)
        } catch (e) {
          ElMessage.error('请求失败')
        }
      }
    } else if (error.request) {
      console.error('请求数据:', error.request)
      ElMessage.error('网络连接错误')
    } else {
      console.error('错误信息:', error.message)
      ElMessage.error(error.message || (isEditMode.value ? '修改失败' : '发布失败'))
    }
  } finally {
    submitting.value = false
  }
}

// 监听路由变化，如果是编辑模式就加载数据
watch(
  () => route.params.id,
  (newId) => {
    if (newId && isEditMode.value) {
      loadProduct()
    }
  },
  { immediate: true }
)

onMounted(() => {
  loadCategories()
  
  // 如果是编辑模式，加载商品数据
  if (isEditMode.value && productId.value) {
    loadProduct()
  }
})
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
</style>