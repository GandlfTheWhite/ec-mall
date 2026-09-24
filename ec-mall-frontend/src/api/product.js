import api from './axios'
export const getProducts = (params) => api.get('/products/search', { params })
export const getProductById = (id) => api.get(`/products/${id}`)
export const getAllProducts = () => api.get('/products')
export const createProduct = (data) => api.post('/products', data)
export const updateProduct = (id, data) => api.put(`/products/${id}`, data)
export const deleteProduct = (id) => api.delete(`/products/${id}`)
