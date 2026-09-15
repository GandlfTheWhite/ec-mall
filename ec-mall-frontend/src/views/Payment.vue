<template>
  <div class="payment-container">
    <h2>💳 お支払い</h2>

    <!-- 加载中 -->
    <div v-if="loading" class="loading">読み込み中...</div>

    <!-- エラー -->
    <div v-else-if="error" class="error">
      <el-alert type="error" :title="error" show-icon />
      <el-button @click="$router.push('/orders')">注文一覧へ</el-button>
    </div>

    <!-- 注文情報 -->
    <div v-else>
      <el-card class="order-card">
        <div class="order-header">
          <span class="order-no">注文番号：{{ order.orderNo }}</span>
          <span class="order-status" :class="statusClass">
            {{ statusLabel }}
          </span>
        </div>

        <el-divider />

        <div class="order-items">
          <el-table :data="order.items" style="width: 100%">
            <el-table-column label="商品名" prop="productName" />
            <el-table-column label="単価" prop="price" width="120">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column label="数量" prop="quantity" width="80" />
            <el-table-column label="小計" width="150">
              <template #default="{ row }">¥{{ row.price * row.quantity }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div class="total-amount">
          合計金額： <strong>¥{{ order.totalAmount }}</strong>
        </div>

        <div class="address-info">
          <p><strong>配送先：</strong>{{ order.shippingAddress }}</p>
          <p><strong>受取人：</strong>{{ order.receiverName }}（{{ order.receiverPhone }}）</p>
        </div>
      </el-card>

      <!-- 支払いボタン（ステータスが0の場合のみ表示） -->
      <div class="actions" v-if="order.status === 0">
        <el-button @click="$router.push('/orders')">戻る</el-button>
        <el-button type="success" :loading="paying" @click="handlePay">
          支払いを実行する
        </el-button>
      </div>

      <!-- 既に支払い済み or キャンセル -->
      <div v-else class="actions">
        <el-button type="primary" @click="$router.push('/orders')">注文一覧へ</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, payOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id

const loading = ref(false)
const paying = ref(false)
const error = ref('')
const order = ref(null)

// ステータス表示用
const statusMap = {
  0: '未払い',
  1: '支払済み',
  2: '発送済み',
  3: '完了',
  4: 'キャンセル',
}
const statusClassMap = {
  0: 'status-pending',
  1: 'status-paid',
  2: 'status-shipped',
  3: 'status-completed',
  4: 'status-canceled',
}
const statusLabel = computed(() => statusMap[order.value?.status] || '不明')
const statusClass = computed(() => statusClassMap[order.value?.status] || '')

// 注文詳細を取得
const fetchOrder = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await getOrderDetail(orderId)
    order.value = res.data
  } catch (err) {
    error.value = '注文情報の取得に失敗しました'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 支払い処理
const handlePay = async () => {
  if (order.value.status !== 0) {
    ElMessage.warning('この注文は既に処理済みです')
    return
  }
  paying.value = true
  try {
    await payOrder(orderId)
    ElMessage.success('支払いが完了しました！')
    // 再読み込みして最新ステータスを表示
    await fetchOrder()
  } catch (err) {
    ElMessage.error('支払いに失敗しました：' + (err.response?.data?.message || err.message))
  } finally {
    paying.value = false
  }
}

onMounted(fetchOrder)
</script>

<style scoped>
.payment-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
.loading, .error {
  text-align: center;
  padding: 60px 0;
}
.order-card {
  margin-bottom: 20px;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-no {
  font-size: 18px;
  font-weight: bold;
}
.order-status {
  font-weight: bold;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
}
.status-pending { background: #ff9800; color: #fff; }
.status-paid { background: #4caf50; color: #fff; }
.status-shipped { background: #2196f3; color: #fff; }
.status-completed { background: #9e9e9e; color: #fff; }
.status-canceled { background: #f44336; color: #fff; }
.total-amount {
  text-align: right;
  font-size: 20px;
  margin: 16px 0;
}
.address-info {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  margin-top: 12px;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>