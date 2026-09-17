import api from './axios'
export const login = (data) => api.post('/auth/login', data, { publicRequest: true })
export const register = (data) => api.post('/members', data, { publicRequest: true })
export const getCurrentMember = () => api.get('/auth/me')
export const getMember = (id) => api.get(`/members/${id}`)
export const updateMember = (id, data) => api.put(`/members/${id}`, data)
export const deleteMember = (id) => api.delete(`/members/${id}`)
