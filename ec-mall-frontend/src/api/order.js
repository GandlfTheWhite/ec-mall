import api from './axios'

export const createOrder = (data) => api.post('/orders', data)
export const getMyOrders = () => api.get('/orders')
export const getOrderDetail = (id) => api.get(`/orders/${id}`)
export const payOrder = (id) => api.post(`/orders/${id}/pay`)
