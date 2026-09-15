import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import ProductList from '../views/ProductList.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Cart from '@/views/Cart.vue'
import Checkout from '@/views/Checkout.vue'
import OrderList from '@/views/OrderList.vue'
import OrderDetail from '@/views/OrderDetail.vue'
import Payment from '@/views/Payment.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  {
    path: '/products/search',
    component: ProductList,
    meta: { requiresAuth: true }
  },
  {
    path: '/products/:id',
    component: ProductDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: Cart,
    meta: { requiresAuth: true }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: Checkout,
    meta: { requiresAuth: true }
  },
  {
    path: '/orders',
    component: OrderList,
    meta: { requiresAuth: true }
  },
  {
    path: '/orders/:id',
    component: OrderDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/payment/:id',   // :id 是订单ID
    name: 'Payment',
    component: Payment,
    meta: { requiresAuth: true }
  }
]

// The client cannot verify a JWT signature; that remains the API's job.
// It can, however, avoid treating an expired or malformed token as a login.
const hasValidToken = (token) => {
  if (!token) return false

  try {
    const payload = token.split('.')[1]
    if (!payload) return false

    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=')
    const { exp } = JSON.parse(atob(padded))

    return typeof exp === 'number' && exp * 1000 > Date.now()
  } catch {
    return false
  }
}

// 1️⃣ ルーターのインスタンスを作成
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 2️⃣ ナビゲーションガードを定義（ルーター作成後に行う）
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const authenticated = hasValidToken(token)

  if (token && !authenticated) {
    localStorage.removeItem('token')
  }

  if (to.meta.requiresAuth && !authenticated) {
    next('/login')
  } else if (to.path === '/login' && authenticated) {
    next('/products/search')
  } else {
    next()
  }
})

// 3️⃣ エクスポート
export default router
