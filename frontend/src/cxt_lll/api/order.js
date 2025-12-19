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
