<template>
  <el-container class="app-shell">
    <el-header v-if="showNavigation" class="app-header">
      <button class="brand" type="button" @click="router.push('/products/search')">EC-Mall</button>
      <nav class="main-navigation" aria-label="メインナビゲーション">
        <el-button text :type="isActive('/products/search') ? 'primary' : 'default'" @click="router.push('/products/search')">
          商品一覧
        </el-button>
        <el-button text :type="isActive('/cart') ? 'primary' : 'default'" @click="router.push('/cart')">
          カート
        </el-button>
        <el-button text :type="isActive('/orders') ? 'primary' : 'default'" @click="router.push('/orders')">
          注文履歴
        </el-button>
      </nav>
      <el-button plain type="danger" @click="logout">ログアウト</el-button>
    </el-header>

    <el-main :class="{ 'page-content': showNavigation }">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const showNavigation = computed(() => route.meta.requiresAuth === true)
const isActive = (path) => route.path === path

const logout = () => {
  localStorage.removeItem('token')
  ElMessage.success('ログアウトしました')
  router.replace('/login')
}
</script>

<style>
html, body, #app { min-height: 100%; margin: 0; }
.app-shell { min-height: 100vh; background: #f7f8fa; }
.app-header { display: flex; align-items: center; gap: 24px; height: 64px; padding: 0 32px; background: #fff; border-bottom: 1px solid #ebeef5; }
.brand { padding: 0; border: 0; background: none; color: #303133; cursor: pointer; font-size: 20px; font-weight: 700; }
.main-navigation { display: flex; align-items: center; gap: 4px; flex: 1; }
.page-content { padding: 0; }
@media (max-width: 600px) { .app-header { gap: 8px; padding: 0 12px; }.brand { font-size: 16px; }.main-navigation { gap: 0; }.main-navigation .el-button { padding: 8px; } }
</style>
