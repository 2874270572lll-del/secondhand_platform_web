import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '../api/message'

export const useMessageStore = defineStore('message', () => {
  // 总未读消息数
  const totalUnreadCount = ref(0)
  
  // 是否正在加载
  const loading = ref(false)

  /**
   * 获取未读消息总数
   */
  const fetchUnreadCount = async () => {
    try {
      loading.value = true
      const res = await getUnreadCount()
      totalUnreadCount.value = res.data || 0
    } catch (error) {
      console.error('获取未读消息数失败:', error)
      totalUnreadCount.value = 0
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置未读消息数
   */
  const resetUnreadCount = () => {
    totalUnreadCount.value = 0
  }

  /**
   * 减少未读消息数
   */
  const decreaseUnreadCount = (count = 1) => {
    totalUnreadCount.value = Math.max(0, totalUnreadCount.value - count)
  }

  return {
    totalUnreadCount,
    loading,
    fetchUnreadCount,
    resetUnreadCount,
    decreaseUnreadCount
  }
})