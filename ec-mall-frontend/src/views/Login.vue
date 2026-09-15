<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>EC-Mall ログイン</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item label="メールアドレス">
          <el-input v-model="form.email" placeholder="admin@example.com" />
        </el-form-item>
        <el-form-item label="パスワード">
          <el-input v-model="form.password" type="password" placeholder="••••••••" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading">
          ログイン
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = ref({ email: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await axios.post('/api/auth/login', form.value)
    localStorage.setItem('token', res.data.token)
    ElMessage.success('ログイン成功！')
    router.push('/products/search')
  } catch (err) {
    ElMessage.error('ログイン失敗：' + err.response?.data?.message || err.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f0f2f5;
}
.login-card {
  width: 400px;
  padding: 20px;
}
</style>