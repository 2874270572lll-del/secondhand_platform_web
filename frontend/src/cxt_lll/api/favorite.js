import request from '../utils/request'

// 添加收藏
export const addFavorite = (productId) => {
  return request({
    url: `/favorites/add/${productId}`,
    method: 'post'
  })
}

// 取消收藏
export const removeFavorite = (productId) => {
  return request({
    url: `/favorites/remove/${productId}`,
    method: 'delete'
  })
}

// 获取我的收藏列表
export const getMyFavorites = (params) => {
  return request({
    url: '/favorites/my-favorites',
    method: 'get',
    params
  })
}
