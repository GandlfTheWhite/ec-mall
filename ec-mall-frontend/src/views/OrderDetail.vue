<template>
  <main class="page-wrap">
    <el-button text @click="router.push('/orders')">← 注文履歴に戻る</el-button>
    <el-skeleton v-if="loading" :rows="10" animated />
    <el-result v-else-if="error" icon="error" title="注文を読み込めません" :sub-title="error"><template #extra><el-button @click="load">再読み込み</el-button></template></el-result>
    <template v-else-if="order">
      <div class="page-heading">
        <div><h1>注文詳細</h1><p>注文番号：{{ order.orderNo }}</p><p>注文日時：{{ dateTime(order.orderDate || order.createdAt) }}</p></div>
        <div class="actions"><el-tag :type="orderStatus(order.status).type">{{ orderStatus(order.status).label }}</el-tag><el-button :disabled="paying" @click="load">更新</el-button></div>
      </div>
      <el-alert v-if="order.status === 0" title="テスト決済です。実際の請求は発生しません。未払いの注文は作成から15分を過ぎると、定期処理で自動キャンセルされます。" type="info" :closable="false" show-icon />
      <el-card class="section-card" shadow="never">
        <template #header>配送先情報</template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="受取人">{{ order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="電話番号">{{ order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="配送先住所">{{ order.shippingAddress }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
      <el-card class="section-card" shadow="never">
        <template #header>注文商品</template>
        <el-table :data="order.items || []">
          <el-table-column prop="productName" label="商品名" min-width="180" />
          <el-table-column label="単価" width="140"><template #default="{ row }">¥{{ money(row.price) }}</template></el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小計" width="140"><template #default="{ row }">¥{{ money(row.price * row.quantity) }}</template></el-table-column>
        </el-table>
        <p class="total">合計金額：¥{{ money(order.totalAmount) }}</p>
        <el-button v-if="order.status === 0" type="primary" :loading="paying" @click="pay">テスト決済を行う</el-button>
      </el-card>
    </template>
  </main>
</template>
<script setup>
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder } from '@/api/order'
import { useResource } from '@/composables/useResource'
import { money, dateTime, orderStatus, errorMessage, isCancelled, confirmOptions } from '@/utils/display'
const route = useRoute()
const router = useRouter()
const { data: order, loading, error, load } = useResource(() => getOrderDetail(route.params.id))
const paying = ref(false)
async function pay() {
  if (paying.value || order.value?.status !== 0) return
  const id = order.value.id
  paying.value = true
  try {
    await ElMessageBox.confirm('テスト決済を実行しますか？実際の請求は発生しません。', '支払いの確認', confirmOptions)
    const { data } = await payOrder(id)
    // 決済APIの応答には明細が含まれないため、取得済みの明細を維持する。
    if (order.value?.id === id) order.value = { ...order.value, ...data, items: data.items ?? order.value.items }
    ElMessage.success('支払いが完了しました。')
  } catch (e) { if (!isCancelled(e)) { ElMessage.error(errorMessage(e)); await load() } }
  finally { paying.value = false }
}
watch(() => route.params.id, load, { immediate: true })
</script>
<style scoped>.section-card { margin-top: 20px; }</style>
