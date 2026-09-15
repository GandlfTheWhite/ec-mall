import axios from './axios'  // 前回作成した axios インスタンス

export const getProducts = (params) => {
  return axios.get('/products/search', { params })
}

export const getProductById = (id) => axios.get(`/products/${id}`)
