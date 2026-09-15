<template>
  <main class="order-list">
    <div class="page-header">
      <div>
        <h2>注文履歴</h2>
        <p>これまでの注文を確認できます。</p>
      </div>
      <el-button @click="router.push('/products/search')">商品一覧へ戻る</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="orders.length === 0" description="注文履歴はありません" />
    <el-table v-else :data="orders" stripe>
      <el-table-column prop="orderNo" label="注文番号" min-width="180" />
      <el-table-column label="注文日時" min-width="170">
        <template #default="{ row }">{{ formatDate(row.orderDate || row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="合計金額" width="140">
        <template #default="{ row }">¥{{ formatPrice(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="注文状況" width="130">
        <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="130">
        <template #default="{ row }"><el-button link type="primary" @click="router.push(`/orders/${row.id}`)">詳細を見る</el-button></template>
      </el-table-column>
    </el-table>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMyOrders } from '@/api/order'

const router = useRouter()
const orders = ref([])
const loading = ref(false)
const statusLabels = ['未払い', '支払い済み', '発送済み', '完了', 'キャンセル']
const statusTypes = ['warning', 'success', 'primary', 'success', 'info']

const formatPrice = (value) => {
  const amount = Number(value)
  return Number.isFinite(amount) ? amount.toFixed(2) : '--'
}
const formatDate = (value) => value ? new Date(value).toLocaleString('ja-JP') : '--'
const statusLabel = (status) => statusLabels[status] || '不明'
const statusType = (status) => statusTypes[status] || 'info'

const fetchOrders = async () => {
  loading.value = true
  try {
    orders.value = (await getMyOrders()).data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '注文履歴の取得に失敗しました')
  } finally {
    loading.value = false
  }
}

onMounted(fetchOrders)
</script>

<style scoped>
.order-list { max-width: 1080px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
h2 { margin: 0; }.page-header p { margin: 8px 0 0; color: #909399; }
</style>
