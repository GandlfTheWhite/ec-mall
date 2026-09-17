<template>
  <main class="auth-page">
    <el-card class="auth-card">
      <h1>会員登録</h1>
      <MemberForm creating :busy="saving" @submit="submit" />
      <p><router-link to="/login">登録済みの方：ログイン</router-link></p>
    </el-card>
  </main>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import MemberForm from '@/components/MemberForm.vue'
import { register } from '@/api/member'
import { errorMessage } from '@/utils/display'
const router = useRouter()
const saving = ref(false)
async function submit(data) {
  if (saving.value) return
  saving.value = true
  try {
    await register(data)
    ElMessage.success('登録が完了しました。ログインしてください。')
    await router.replace('/login')
  } catch (error) { ElMessage.error(errorMessage(error)) }
  finally { saving.value = false }
}
</script>
