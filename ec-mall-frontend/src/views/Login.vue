<template>
  <main class="login-page"><el-card class="login-card"><h1>EC-Mall</h1><p>ログインしてお買い物を始めましょう。</p>
    <el-form :model="form" @submit.prevent="login"><el-form-item label="メールアドレス"><el-input v-model="form.email" autocomplete="email" /></el-form-item><el-form-item label="パスワード"><el-input v-model="form.password" type="password" show-password autocomplete="current-password" /></el-form-item><el-button native-type="submit" type="primary" :loading="loading" class="submit">ログイン</el-button></el-form>
  </el-card></main>
</template>
<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
const router = useRouter(); const loading = ref(false); const form = ref({ email: '', password: '' })
const login = async () => { loading.value = true; try { const { data } = await axios.post('/api/auth/login', form.value); localStorage.setItem('token', data.token); ElMessage.success('ログインしました'); router.replace('/products/search') } catch (error) { ElMessage.error(error.response?.data?.message || 'ログインに失敗しました') } finally { loading.value = false } }
</script>
<style scoped>.login-page { min-height: 100vh; display: grid; place-items: center; background: #f6f8fc; }.login-card { width: min(400px, calc(100vw - 32px)); }.login-card h1 { margin: 0; }.login-card p { color: #606266; }.submit { width: 100%; }</style>
