import request from '../utils/request'

/**
 * 发送消息
 */
export const sendMessage = (data) => {
  return request({
    url: '/messages/send',
    method: 'post',
    data
  })
}

/**
 * 获取聊天历史
 */
export const getChatHistory = (partnerId) => {
  return request({
    url: `/messages/history/${partnerId}`,
    method: 'get'
  })
}

/**
 * 获取聊天会话列表
 */
export const getConversations = () => {
  return request({
    url: '/messages/conversations',
    method: 'get'
  })
}

/**
 * 获取未读消息数
 */
export const getUnreadCount = () => {
  return request({
    url: '/messages/unread-count',
    method: 'get'
  })
}

/**
 * 标记消息为已读
 */
export const markAsRead = (partnerId) => {
  return request({
    url: `/messages/mark-read/${partnerId}`,
    method: 'put'
  })
}
