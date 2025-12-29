import request from '../utils/request'

// 创建订单
export const createOrder = (data) => {
  return request({
    url: '/orders/create',
    method: 'post',
    data
  })
}

// 获取我购买的订单
export const getMyBuyOrders = (params) => {
  return request({
    url: '/orders/my-buy',
    method: 'get',
    params
  })
}

// 获取我卖出的订单
export const getMySellOrders = (params) => {
  return request({
    url: '/orders/my-sell',
    method: 'get',
    params
  })
}

// 更新订单状态
export const updateOrderStatus = (id, status) => {
  return request({
    url: `/orders/update-status/${id}`,
    method: 'put',
    params: { status }
  })
}

// ========== 添加以下函数 ==========

// 支付订单
export const payOrder = (id) => {
  return updateOrderStatus(id, 'PAID')
}

// 发货
export const shipOrder = (id) => {
  return updateOrderStatus(id, 'SHIPPED')
}

// 确认收货
export const receiveOrder = (id) => {
  return updateOrderStatus(id, 'RECEIVED')
}

// 取消订单
export const cancelOrder = (id) => {
  return updateOrderStatus(id, 'CANCELLED')
}

// 完成订单
export const completeOrder = (id) => {
  return updateOrderStatus(id, 'COMPLETED')
}

// 卖家确认订单
export const confirmOrder = (id) => {
  return updateOrderStatus(id, 'CONFIRMED')
}