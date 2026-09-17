<template>
  <main class="page-wrap narrow">
    <h1>会員情報</h1>
    <el-skeleton v-if="loading" :rows="6" animated />
    <el-result v-else-if="error" icon="error" title="会員情報を取得できません" :sub-title="error">
      <template #extra><el-button @click="load">再読み込み</el-button></template>
    </el-result>
    <el-card v-else-if="member">
      <p>会員番号：{{ member.id }} ／ {{ member.role === 'ADMIN' ? '管理者' : '一般会員' }}</p>
      <p>登録日時：{{ dateTime(member.createdAt) }}</p>
      <MemberForm :member="member" :busy="saving" @submit="save" />
    </el-card>
  </main>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import MemberForm from '@/components/MemberForm.vue'
import { updateMember } from '@/api/member'
import { loadSession } from '@/stores/session'
import { dateTime, errorMessage } from '@/utils/display'
const member = ref(null)
const loading = ref(true)
const saving = ref(false)
const error = ref('')
async function load() {
  loading.value = true
  error.value = ''
  try { member.value = await loadSession(true) }
  catch (e) { error.value = errorMessage(e) }
  finally { loading.value = false }
}
async function save(data) {
  if (saving.value) return
  saving.value = true
  try {
    await updateMember(member.value.id, data)
    ElMessage.success('会員情報を保存しました。')
    await load()
  } catch (e) { ElMessage.error(errorMessage(e)) }
  finally { saving.value = false }
}
onMounted(load)
</script>
