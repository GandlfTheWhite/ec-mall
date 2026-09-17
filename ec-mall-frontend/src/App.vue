<template>
  <el-container class="app-shell">
    <el-header v-if="showNavigation" class="app-header">
      <button class="brand" type="button" @click="router.push('/products/search')">EC-Mall</button>
      <nav class="navigation" aria-label="メインナビゲーション">
        <el-button text :type="active('/products') ? 'primary' : 'default'" @click="router.push('/products/search')">商品一覧</el-button>
        <el-button text :type="active('/cart') ? 'primary' : 'default'" @click="router.push('/cart')">カート</el-button>
        <el-button text :type="active('/orders') ? 'primary' : 'default'" @click="router.push('/orders')">注文履歴</el-button>
      </nav>
      <el-button plain type="danger" @click="logout">ログアウト</el-button>
    </el-header>
    <el-main class="page"><router-view /></el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const showNavigation = computed(() => route.meta.requiresAuth === true)
const active = (prefix) => route.path.startsWith(prefix)
const logout = () => {
  localStorage.removeItem('token')
  ElMessage.success('ログアウトしました')
  router.replace('/login')
}
</script>

<style>
html, body, #app { min-height: 100%; margin: 0; }
.app-shell { min-height: 100vh; background: #f6f8fc; }
.app-header { display: flex; align-items: center; gap: 24px; height: 64px; padding: 0 32px; background: #fff; border-bottom: 1px solid #e4e7ed; }
.brand { border: 0; padding: 0; background: transparent; color: #303133; cursor: pointer; font-size: 20px; font-weight: 700; }
.navigation { display: flex; flex: 1; gap: 4px; }.page { padding: 0; }
@media (max-width: 600px) { .app-header { gap: 4px; padding: 0 12px; }.brand { font-size: 16px; }.navigation .el-button { padding: 8px; } }
</style>
