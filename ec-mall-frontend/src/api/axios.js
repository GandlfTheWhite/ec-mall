import axios from 'axios'

const api = axios.create({ baseURL: '/api', timeout: 10000 })
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token && !config.publicRequest) config.headers.Authorization = `Bearer ${token}`
  return config
})
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const sentToken = error.config?.headers?.Authorization
    const currentToken = localStorage.getItem('token')
    if (error.response?.status === 401 && !error.config?.publicRequest
        && (!currentToken || sentToken === `Bearer ${currentToken}`)) {
      localStorage.removeItem('token')
      window.dispatchEvent(new Event('session-cleared'))
      if (window.location.pathname !== '/login') window.location.assign('/login')
    }
    return Promise.reject(error)
  },
)
export default api
