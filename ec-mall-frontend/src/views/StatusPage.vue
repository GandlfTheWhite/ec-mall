<template>
  <el-result :icon="kind === 'missing' ? 'warning' : 'error'" :title="title" :sub-title="description">
    <template #extra>
      <el-button v-if="kind === 'connection'" type="primary" @click="router.replace(localRedirect(route.query.redirect))">再接続</el-button>
      <el-button @click="router.push('/products/search')">商品一覧へ</el-button>
      <el-button @click="logout">ログイン画面へ</el-button>
    </template>
  </el-result>
</template>
<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearSession } from '@/stores/session'
import { localRedirect } from '@/utils/auth'
const route = useRoute()
const router = useRouter()
const kind = computed(() => route.meta.status)
const title = computed(() => ({ forbidden: 'アクセスできません', connection: '接続できません', missing: 'ページが見つかりません' })[kind.value])
const description = computed(() => kind.value === 'forbidden' ? '管理者アカウントでログインしてください。' : kind.value === 'connection' ? '会員情報を取得できませんでした。時間をおいて再度お試しください。' : 'アドレスをご確認ください。')
const logout = () => { clearSession(); router.replace('/login') }
</script>
