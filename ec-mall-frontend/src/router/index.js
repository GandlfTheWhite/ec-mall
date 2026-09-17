import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import ProductList from '@/views/ProductList.vue'
import ProductDetail from '@/views/ProductDetail.vue'
import Cart from '@/views/Cart.vue'
import Checkout from '@/views/Checkout.vue'
import OrderList from '@/views/OrderList.vue'
import OrderDetail from '@/views/OrderDetail.vue'

const routes = [
  { path: '/', redirect: '/products/search' },
  { path: '/login', component: Login },
  { path: '/products/search', component: ProductList, meta: { requiresAuth: true } },
  { path: '/products/:id', component: ProductDetail, meta: { requiresAuth: true } },
  { path: '/cart', component: Cart, meta: { requiresAuth: true } },
  { path: '/checkout', component: Checkout, meta: { requiresAuth: true } },
  { path: '/orders', component: OrderList, meta: { requiresAuth: true } },
  { path: '/orders/:id', component: OrderDetail, meta: { requiresAuth: true } },
  { path: '/:pathMatch(.*)*', redirect: '/products/search' }
]

const hasValidToken = (token) => {
  try {
    if (!token) return false
    const payload = token.split('.')[1]
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=')
    const { exp } = JSON.parse(atob(padded))
    return typeof exp === 'number' && exp * 1000 > Date.now()
  } catch { return false }
}

const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const authenticated = hasValidToken(token)
  if (token && !authenticated) localStorage.removeItem('token')
  if (to.meta.requiresAuth && !authenticated) return '/login'
  if (to.path === '/login' && authenticated) return '/products/search'
})

export default router
