<template>
  <main class="page-wrap">
    <div class="page-heading"><h1>注文管理</h1><el-button :loading="loading" @click="load">更新</el-button></div>
    <el-alert class="error-alert" type="info" :closable="false" title="支払いはモック処理です。キャンセル時は在庫を戻しますが、実際の返金処理は行いません。" />
    <el-alert v-if="error" class="error-alert" type="error" :title="error" :closable="false" />
    <el-table v-loading="loading" :data="orders" row-key="id" empty-text="注文はありません">
      <el-table-column type="expand"><template #default="{ row }">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="受取人">{{ row.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="電話番号">{{ row.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="配送先住所">{{ row.shippingAddress }}</el-descriptions-item>
        </el-descriptions>
      </template></el-table-column>
      <el-table-column prop="orderNo" label="注文番号" min-width="210" />
      <el-table-column prop="memberId" label="会員番号" width="100" />
      <el-table-column label="注文日時" min-width="170"><template #default="{ row }">{{ dateTime(row.orderDate || row.createdAt) }}</template></el-table-column>
      <el-table-column label="合計金額" width="140"><template #default="{ row }">¥{{ money(row.totalAmount) }}</template></el-table-column>
      <el-table-column label="状況" width="120"><template #default="{ row }"><el-tag :type="orderStatus(row.status).type">{{ orderStatus(row.status).label }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" :disabled="!nextOrderStatuses(row.status).length" @click="open(row)">状況変更</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="editing" title="注文状況の変更" width="460px" :show-close="!saving" :close-on-click-modal="!saving" :close-on-press-escape="!saving">
      <p>注文番号：{{ selected?.orderNo }}</p>
      <el-select v-model="status" :disabled="saving" aria-label="注文状況">
        <el-option v-for="item in orderStatuses" :key="item.value" :value="item.value" :label="item.label" :disabled="!nextOrderStatuses(selected?.status).includes(item.value)" />
      </el-select>
      <template #footer><el-button type="primary" :loading="saving" @click="save">変更する</el-button></template>
    </el-dialog>
  </main>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllOrders, updateOrderStatus } from '@/api/admin'
import { money, dateTime, orderStatus, orderStatuses, nextOrderStatuses, errorMessage, isCancelled, confirmOptions } from '@/utils/display'
const orders = ref([])
const loading = ref(true)
const error = ref('')
const editing = ref(false)
const selected = ref(null)
const status = ref(0)
const saving = ref(false)
async function load() {
  loading.value = true; error.value = ''
  try { orders.value = (await getAllOrders()).data }
  catch (e) { error.value = errorMessage(e) }
  finally { loading.value = false }
}
function open(row) { selected.value = row; status.value = nextOrderStatuses(row.status)[0]; editing.value = true }
async function save() {
  if (saving.value) return
  saving.value = true
  try {
    await ElMessageBox.confirm('注文状況を「' + orderStatus(status.value).label + '」に変更しますか？', '確認', confirmOptions)
    await updateOrderStatus(selected.value.id, status.value)
    editing.value = false
    ElMessage.success('注文状況を変更しました。')
    await load()
  } catch (e) { if (!isCancelled(e)) ElMessage.error(errorMessage(e)) }
  finally { saving.value = false }
}
onMounted(load)
</script>
