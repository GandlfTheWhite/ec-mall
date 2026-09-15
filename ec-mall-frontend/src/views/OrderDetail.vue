<template>
  <main class="order-detail">
    <el-button text class="back-button" @click="router.push('/orders')">← 注文履歴に戻る</el-button>

    <el-skeleton v-if="loading" :rows="10" animated />
    <el-result v-else-if="errorMessage" icon="error" title="注文情報の取得に失敗しました" :sub-title="errorMessage">
      <template #extra><el-button type="primary" @click="loadOrder">再読み込み</el-button></template>
    </el-result>

    <template v-else-if="order">
      <div class="page-header">
        <div>
          <h2>注文詳細</h2>
          <p>注文番号：{{ order.orderNo }}</p>
        </div>
        <div class="header-actions">
          <el-tag :type="statusType">{{ statusLabel }}</el-tag>
          <el-button v-if="order.status === 0" type="primary" :loading="paying" @click="pay">
            支払いを完了する
          </el-button>
        </div>
      </div>

      <el-alert v-if="order.status === 0" title="これはテスト用の疑似決済です。実際の請求は発生しません。" type="info" :closable="false" show-icon />

      <el-card class="section-card" shadow="never">
        <template #header><strong>配送先情報</strong></template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="受取人">{{ order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="電話番号">{{ order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="配送先住所">{{ order.shippingAddress }}</el-descriptions-item>
          <el-descriptions-item label="注文日時">{{ formatDate(order.orderDate || order.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="section-card" shadow="never">
        <template #header><strong>注文商品</strong></template>
        <el-table :data="order.items || []">
          <el-table-column prop="productName" label="商品名" min-width="220" />
          <el-table-column label="単価" width="140"><template #default="{ row }">¥{{ formatPrice(row.price) }}</template></el-table-column>
          <el-table-column prop="quantity" label="数量" width="110" />
          <el-table-column label="小計" width="150"><template #default="{ row }">¥{{ formatPrice(row.price * row.quantity) }}</template></el-table-column>
        </el-table>
        <p class="total">合計金額：<strong>¥{{ formatPrice(order.totalAmount) }}</strong></p>
      </el-card>
    </template>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(false)
const paying = ref(false)
const errorMessage = ref('')
const statusLabels = ['未払い', '支払い済み', '発送済み', '完了', 'キャンセル']
const statusTypes = ['warning', 'success', 'primary', 'success', 'info']
const statusLabel = computed(() => statusLabels[order.value?.status] || '不明')
const statusType = computed(() => statusTypes[order.value?.status] || 'info')

const formatPrice = (value) => {
  const amount = Number(value)
  return Number.isFinite(amount) ? amount.toFixed(2) : '--'
}
const formatDate = (value) => value ? new Date(value).toLocaleString('ja-JP') : '--'

const loadOrder = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    order.value = (await getOrderDetail(route.params.id)).data
  } catch (error) {
    errorMessage.value = error.response?.status === 404 ? '注文が見つかりません。' : '時間をおいて再度お試しください。'
  } finally {
    loading.value = false
  }
}

const pay = async () => {
  try {
    await ElMessageBox.confirm('テスト用の疑似決済を実行しますか？', '支払いの確認', {
      confirmButtonText: '支払う',
      cancelButtonText: 'キャンセル',
      type: 'warning'
    })
    paying.value = true
    order.value = (await payOrder(order.value.id)).data
    ElMessage.success('支払いが完了しました')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '支払い処理に失敗しました')
    }
  } finally {
    paying.value = false
  }
}

watch(() => route.params.id, loadOrder)
onMounted(loadOrder)
</script>

<style scoped>
.order-detail { max-width: 1080px; margin: 0 auto; padding: 24px; }.back-button { margin-bottom: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }.page-header h2 { margin: 0; }.page-header p { margin: 8px 0 0; color: #606266; }.header-actions { display: flex; align-items: center; gap: 12px; }
.section-card { margin-top: 20px; }.total { text-align: right; font-size: 18px; }.total strong { color: #f56c6c; font-size: 24px; }
</style>
