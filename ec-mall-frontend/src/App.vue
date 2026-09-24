<template>
  <el-container class="app-shell">
    <el-header v-if="showNavigation" class="app-header">
      <router-link class="brand" to="/products/search">EC-Mall</router-link>
      <nav class="navigation" aria-label="メインナビゲーション">
        <router-link to="/products/search" :class="{ selected: route.path.startsWith('/products') }">商品一覧</router-link>
        <router-link to="/cart" :class="{ selected: ['/cart', '/checkout'].includes(route.path) }">
          カート<el-badge v-if="cartCount" :value="cartCount" :max="99" class="cart-badge" />
        </router-link>
        <router-link to="/orders" :class="{ selected: route.path.startsWith('/orders') }">注文履歴</router-link>
        <router-link to="/profile">会員情報</router-link>
        <router-link v-if="session.member?.role === 'ADMIN'" to="/admin/products" :class="{ selected: route.path.startsWith('/admin') }">管理画面</router-link>
      </nav>
      <el-button plain type="danger" @click="logout">ログアウト</el-button>
    </el-header>
    <nav v-if="showNavigation && route.meta.admin" class="admin-navigation" aria-label="管理メニュー">
      <router-link to="/admin/products">商品管理</router-link>
      <router-link to="/admin/members">会員管理</router-link>
      <router-link to="/admin/orders">注文管理</router-link>
    </nav>
    <el-main class="page"><router-view /></el-main>
  </el-container>
</template>
<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { session, clearSession } from '@/stores/session'
import { getCart } from '@/api/cart'
const route = useRoute()
const router = useRouter()
const showNavigation = computed(() => route.meta.requiresAuth === true)
const cartCount = ref(0)
let countRequest = 0
async function refreshCart() {
  const id = ++countRequest
  const token = localStorage.getItem('token')
  if (!token) { cartCount.value = 0; return }
  try {
    const { data } = await getCart()
    if (id === countRequest && token === localStorage.getItem('token')) {
      cartCount.value = (data.items || []).reduce((sum, item) => sum + item.quantity, 0)
    }
  } catch { if (id === countRequest) cartCount.value = 0 }
}
function reset() { countRequest++; cartCount.value = 0; session.member = null }
function logout() {
  clearSession()
  ElMessage.success('ログアウトしました。')
  router.replace('/login')
}
watch(showNavigation, (value) => { if (value) refreshCart(); else reset() })
onMounted(() => {
  window.addEventListener('cart-changed', refreshCart)
  window.addEventListener('session-cleared', reset)
  if (showNavigation.value) refreshCart()
})
onUnmounted(() => {
  window.removeEventListener('cart-changed', refreshCart)
  window.removeEventListener('session-cleared', reset)
})
</script>
<style>
* { box-sizing: border-box; }
html, body, #app { min-height: 100%; margin: 0; font-family: system-ui, sans-serif; color: #303133; }
a { color: #3265a8; }
.app-shell { min-height: 100vh; background: #f6f8fc; }
.app-header { display: flex; align-items: center; flex-wrap: wrap; gap: 20px; height: auto; min-height: 64px; padding: 12px 24px; background: white; border-bottom: 1px solid #e4e7ed; }
.brand { text-decoration: none; color: #303133; font-size: 20px; font-weight: 700; }
.navigation { display: flex; flex-wrap: wrap; flex: 1; gap: 20px; align-items: center; }
.navigation a, .admin-navigation a { text-decoration: none; padding: 6px 0; }
.navigation .selected, .navigation .router-link-exact-active, .admin-navigation .router-link-exact-active { font-weight: 700; border-bottom: 2px solid #409eff; }
.admin-navigation { display: flex; flex-wrap: wrap; gap: 24px; padding: 12px 24px; background: #edf2f9; }
.cart-badge { margin-left: 6px; }
.page { padding: 0; }
.page-wrap { max-width: 1200px; margin: 0 auto; padding: 24px; }
.narrow { max-width: 740px; }
.page-heading, .actions { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.page-heading { justify-content: space-between; margin-bottom: 20px; }
.page-heading h1 { margin: 0; font-size: 26px; }
.auth-page { min-height: 100vh; display: grid; place-items: center; padding: 24px; }
.auth-card { width: 100%; max-width: 480px; }
.muted { color: #727984; }
.total { text-align: right; font-size: 20px; }
.error-alert { margin-bottom: 16px; }
.table-scroll { overflow-x: auto; }
@media (max-width: 640px) {
  .page-wrap { padding: 16px; }
  .navigation { flex-basis: 100%; order: 3; gap: 16px; }
  .app-header > .el-button { margin-left: auto; }
  .el-dialog { --el-dialog-width: 94% !important; }
}
</style>
