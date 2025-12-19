import request from '../utils/request'

// 获取商品列表
export const getProductList = (params) => {
  return request({
    url: '/products/list',
    method: 'get',
    params
  })
}

// 搜索商品
export const searchProducts = (params) => {
  return request({
    url: '/products/search',
    method: 'get',
    params
  })
}

// 获取商品详情
export const getProductDetail = (id) => {
  return request({
    url: `/products/detail/${id}`,
    method: 'get'
  })
}

// 发布商品
export const createProduct = (data) => {
  return request({
    url: '/products/create',
    method: 'post',
    data
  })
}

// 更新商品
export const updateProduct = (id, data) => {
  return request({
    url: `/products/update/${id}`,
    method: 'put',
    data
  })
}

// 删除商品
export const deleteProduct = (id) => {
  return request({
    url: `/products/delete/${id}`,
    method: 'delete'
  })
}

// 下架商品
export const offShelfProduct = (id) => {
  return request({
    url: `/products/off-shelf/${id}`,
    method: 'put'
  })
}

// 获取我的商品列表
export const getMyProducts = (params) => {
  return request({
    url: '/products/my-products',
    method: 'get',
    params
  })
}
