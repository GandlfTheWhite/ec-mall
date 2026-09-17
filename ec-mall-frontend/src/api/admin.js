import api from './axios'
export const getMembers = () => api.get('/admin/members')
export const updateMemberRole = (id, role) => api.put(`/admin/members/${id}/role`, null, { params: { role } })
export const getAllOrders = () => api.get('/admin/orders')
export const updateOrderStatus = (id, status) => api.put(`/admin/orders/${id}/status`, null, { params: { status } })
export const updateProductStatus = (id, status) => api.put(`/admin/products/${id}/status`, null, { params: { status } })
export const updateProductStock = (id, stock) => api.put(`/admin/products/${id}/stock`, null, { params: { stock } })
