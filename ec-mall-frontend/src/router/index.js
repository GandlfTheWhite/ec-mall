import { createRouter, createMemoryHistory, createWebHistory } from 'vue-router'
import { hasValidToken, localRedirect } from '@/utils/auth'
import { clearSession, loadSession } from '@/stores/session'

export const routes = [
  { path: '/', redirect: '/products/search' },
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/register', component: () => import('@/views/Register.vue') },
  { path: '/products', redirect: '/products/search' },
  { path: '/products/search', component: () => import('@/views/ProductList.vue'), meta: { requiresAuth: true } },
  { path: '/products/:id', component: () => import('@/views/ProductDetail.vue'), meta: { requiresAuth: true } },
  { path: '/cart', component: () => import('@/views/Cart.vue'), meta: { requiresAuth: true } },
  { path: '/checkout', component: () => import('@/views/Checkout.vue'), meta: { requiresAuth: true } },
  { path: '/orders/create', redirect: '/checkout' },
  { path: '/orders', component: () => import('@/views/OrderList.vue'), meta: { requiresAuth: true } },
  { path: '/orders/:id', component: () => import('@/views/OrderDetail.vue'), meta: { requiresAuth: true } },
  { path: '/payment/:id', redirect: (to) => `/orders/${to.params.id}` },
  { path: '/profile', component: () => import('@/views/Profile.vue'), meta: { requiresAuth: true } },
  { path: '/admin', redirect: '/admin/products' },
  { path: '/admin/products', component: () => import('@/views/admin/Products.vue'), meta: { requiresAuth: true, admin: true } },
  { path: '/admin/members', component: () => import('@/views/admin/Members.vue'), meta: { requiresAuth: true, admin: true } },
  { path: '/admin/orders', component: () => import('@/views/admin/Orders.vue'), meta: { requiresAuth: true, admin: true } },
  { path: '/forbidden', component: () => import('@/views/StatusPage.vue'), meta: { status: 'forbidden' } },
  { path: '/connection-error', component: () => import('@/views/StatusPage.vue'), meta: { status: 'connection' } },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/StatusPage.vue'), meta: { status: 'missing' } },
]

export async function guard(to) {
  const token = localStorage.getItem('token')
  if (!hasValidToken(token)) {
    if (token) clearSession()
    if (to.meta.requiresAuth) return { path: '/login', query: { redirect: to.fullPath } }
    return
  }
  if (to.path === '/login' || to.path === '/register') return localRedirect(to.query.redirect)
  if (!to.meta.requiresAuth) return
  try {
    const member = await loadSession(Boolean(to.meta.admin))
    if (!member) return '/login'
    if (to.meta.admin && member.role !== 'ADMIN') return '/forbidden'
  } catch (error) {
    if (error.response?.status === 401 || error.response?.status === 404) {
      clearSession()
      return '/login'
    }
    return { path: '/connection-error', query: { redirect: to.fullPath } }
  }
}
const router = createRouter({ history: import.meta.env.SSR ? createMemoryHistory() : createWebHistory(), routes, scrollBehavior: () => ({ top: 0 }) })
router.beforeEach(guard)
export default router
