import api from './axios'

export const getProducts = (params) => api.get('/products/search', { params })
export const getProductById = (id) => api.get(`/products/${id}`)
