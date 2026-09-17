<template>
  <main class="page-wrap">
    <div class="page-heading"><h1>会員管理</h1><el-button type="primary" @click="open()">会員を登録</el-button></div>
    <el-alert v-if="error" class="error-alert" type="error" :title="error" :closable="false"><el-button text @click="load">再読み込み</el-button></el-alert>
    <el-table v-loading="loading" :data="members" row-key="id" empty-text="会員は登録されていません">
      <el-table-column prop="id" label="会員番号" width="100" />
      <el-table-column prop="name" label="氏名" min-width="120" />
      <el-table-column prop="email" label="メールアドレス" min-width="220" />
      <el-table-column prop="age" label="年齢" width="80" />
      <el-table-column label="権限" width="100"><template #default="{ row }">{{ row.role === 'ADMIN' ? '管理者' : '一般会員' }}</template></el-table-column>
      <el-table-column label="登録日時" min-width="170"><template #default="{ row }">{{ dateTime(row.createdAt) }}</template></el-table-column>
      <el-table-column label="操作" min-width="220"><template #default="{ row }">
        <el-button link type="primary" :disabled="busy" @click="open(row)">詳細・編集</el-button>
        <el-button link :disabled="busy" @click="changeRole(row)">権限変更</el-button>
        <el-button link type="danger" :disabled="busy" @click="remove(row)">削除</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="editing" :title="selected ? '会員情報の編集' : '会員登録'" width="560px" :show-close="!saving" :close-on-click-modal="!saving" :close-on-press-escape="!saving">
      <MemberForm :key="formVersion" :member="selected || emptyMember" :creating="!selected" :busy="saving" @submit="save" />
    </el-dialog>
    <el-dialog v-model="roleDialog" title="権限の変更" width="420px" :show-close="!saving" :close-on-click-modal="!saving" :close-on-press-escape="!saving">
      <p>{{ roleMember?.name }}</p>
      <el-select v-model="role" :disabled="saving" aria-label="権限"><el-option label="一般会員" value="USER" /><el-option label="管理者" value="ADMIN" /></el-select>
      <template #footer><el-button type="primary" :loading="saving" @click="saveRole">変更する</el-button></template>
    </el-dialog>
  </main>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import MemberForm from '@/components/MemberForm.vue'
import { getMembers, updateMemberRole } from '@/api/admin'
import { getMember, register, updateMember, deleteMember } from '@/api/member'
import { clearSession, loadSession, session } from '@/stores/session'
import { dateTime, errorMessage, isCancelled, confirmOptions } from '@/utils/display'
const router = useRouter()
const members = ref([])
const loading = ref(true)
const error = ref('')
const editing = ref(false)
const selected = ref(null)
const emptyMember = {}
const formVersion = ref(0)
const busy = ref(false)
const saving = ref(false)
const roleDialog = ref(false)
const roleMember = ref(null)
const role = ref('USER')
async function load() {
  loading.value = true; error.value = ''
  try { members.value = (await getMembers()).data }
  catch (e) { error.value = errorMessage(e) }
  finally { loading.value = false }
}
async function open(row) {
  if (busy.value) return
  busy.value = true
  try {
    selected.value = row ? (await getMember(row.id)).data : null
    formVersion.value++
    editing.value = true
  } catch (e) { ElMessage.error(errorMessage(e)) }
  finally { busy.value = false }
}
async function save(data) {
  if (saving.value) return
  saving.value = true
  try {
    if (selected.value) await updateMember(selected.value.id, data)
    else await register(data)
    editing.value = false
    ElMessage.success('会員情報を保存しました。')
    if (selected.value?.id === session.member?.id) await loadSession(true)
    await load()
  } catch (e) { ElMessage.error(errorMessage(e)) }
  finally { saving.value = false }
}
function changeRole(row) { roleMember.value = row; role.value = row.role; roleDialog.value = true }
async function saveRole() {
  if (saving.value) return
  saving.value = true
  try {
    await ElMessageBox.confirm('会員の権限を変更しますか？', '確認', confirmOptions)
    await updateMemberRole(roleMember.value.id, role.value)
    roleDialog.value = false
    ElMessage.success('権限を変更しました。')
    await loadSession(true)
    if (session.member?.role !== 'ADMIN') return router.replace('/products/search')
    await load()
  } catch (e) { if (!isCancelled(e)) ElMessage.error(errorMessage(e)) }
  finally { saving.value = false }
}
async function remove(row) {
  if (busy.value) return
  busy.value = true
  try {
    await ElMessageBox.confirm('会員「' + row.name + '」を削除しますか？この操作は取り消せません。', '削除の確認', confirmOptions)
    await deleteMember(row.id)
    ElMessage.success('会員を削除しました。')
    if (row.id === session.member?.id) { clearSession(); return router.replace('/login') }
    await load()
  } catch (e) { if (!isCancelled(e)) ElMessage.error(errorMessage(e)) }
  finally { busy.value = false }
}
onMounted(load)
</script>
