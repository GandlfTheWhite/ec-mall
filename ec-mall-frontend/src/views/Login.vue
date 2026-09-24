<template>
  <main class="auth-page">
    <el-card class="auth-card">
      <h1>EC-Mall</h1>
      <p>ログインしてお買い物を始めましょう。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
        <el-form-item label="メールアドレス" prop="email"><el-input v-model.trim="form.email" type="email" autocomplete="email" /></el-form-item>
        <el-form-item label="パスワード" prop="password"><el-input v-model="form.password" type="password" show-password autocomplete="current-password" /></el-form-item>
        <el-button native-type="submit" type="primary" :loading="loading">ログイン</el-button>
      </el-form>
      <p><router-link to="/register">初めての方はこちら：会員登録</router-link></p>
    </el-card>
  </main>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { login } from '@/api/member'
import { clearSession, loadSession } from '@/stores/session'
import { localRedirect } from '@/utils/auth'
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const formRef = ref()
const form = reactive({ email: '', password: '' })
const rules = {
  email: [{ required: true, type: 'email', message: 'メールアドレスをご確認ください。', trigger: 'blur' }],
  password: [{ required: true, message: 'パスワードを入力してください。', trigger: 'blur' }],
}
async function submit() {
  if (loading.value) return
  loading.value = true
  if (!await formRef.value.validate().catch(() => false)) { loading.value = false; return }
  try {
    const { data } = await login(form)
    clearSession()
    localStorage.setItem('token', data.token)
    // 会員情報の取得に失敗した場合は、ルーターで再接続画面を表示する。
    await loadSession().catch(() => {})
    await router.replace(localRedirect(route.query.redirect))
  } catch (error) {
    ElMessage.error(error.response ? 'メールアドレスまたはパスワードをご確認ください。' : 'サーバーに接続できません。')
  } finally { loading.value = false }
}
</script>
