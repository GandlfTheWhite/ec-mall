<template>
  <div class="checkout-container">
    <h2>📋 注文内容の確認</h2>

    <!-- 商品リスト -->
    <el-table :data="cart.items" style="width: 100%; margin-bottom: 20px;">
      <el-table-column label="商品名" prop="productName" />
      <el-table-column label="単価" prop="priceAtAdd" width="150">
        <template #default="{ row }">¥{{ row.priceAtAdd }}</template>
      </el-table-column>
      <el-table-column label="数量" prop="quantity" width="100" />
      <el-table-column label="小計" width="180">
        <template #default="{ row }">¥{{ row.priceAtAdd * row.quantity }}</template>
      </el-table-column>
    </el-table>

    <div class="total-amount">
      合計金額： <strong>¥{{ totalPrice }}</strong>
    </div>

    <!-- 配送先入力 -->
    <el-divider>配送先情報</el-divider>
    <el-form :model="orderForm" label-width="120px">
      <el-form-item label="受取人氏名">
        <el-input v-model="orderForm.receiverName" placeholder="例：山田 太郎" />
      </el-form-item>
      <el-form-item label="電話番号">
        <el-input v-model="orderForm.receiverPhone" placeholder="例：090-1234-5678" />
      </el-form-item>
      <el-form-item label="配送先住所">
        <el-input v-model="orderForm.shippingAddress" type="textarea" rows="3" placeholder="例：東京都新宿区..." />
      </el-form-item>
    </el-form>

    <!-- ボタン -->
    <div class="actions">
      <el-button @click="$router.push('/cart')">戻る</el-button>
      <el-button type="primary" :loading="submitting" @click="submitOrder">
        注文を確定する
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCart } from '@/api/cart'
import { createOrder } from '@/api/order'

const router = useRouter()
const submitting = ref(false)
const cart = ref({ items: [] })
const orderForm = ref({
  receiverName: '',
  receiverPhone: '',
  shippingAddress: '',
})

// 合計金額
const totalPrice = computed(() => {
  return cart.value.items.reduce((sum, item) => sum + item.priceAtAdd * item.quantity, 0)
})

// カートデータを取得
const fetchCart = async () => {
  try {
    const res = await getCart()
    cart.value = res.data
    if (cart.value.items.length === 0) {
      ElMessage.warning('カートが空です')
      router.push('/products/search')
    }
  } catch (err) {
    ElMessage.error('カート情報の取得に失敗しました')
  }
}

// 注文確定
const submitOrder = async () => {
  // 簡単なバリデーション（本当はもっと丁寧に）
  if (!orderForm.value.receiverName || !orderForm.value.receiverPhone || !orderForm.value.shippingAddress) {
    ElMessage.warning('配送先情報を全て入力してください')
    return
  }

  submitting.value = true
  try {
    const { data: order } = await createOrder(orderForm.value)
    ElMessage.success('注文が完了しました！')
    // 成功したら注文完了ページへ（仮に /orders 一覧へ飛ばす）
    // router.push(`/orders/${order.id}`)
    router.push(`/payment/${res.data.id}`)
  } catch (err) {
    ElMessage.error('注文に失敗しました：' + (err.response?.data?.message || err.message))
  } finally {
    submitting.value = false
  }
}

onMounted(fetchCart)
</script>

<style scoped>
.checkout-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.total-amount {
  text-align: right;
  font-size: 20px;
  margin-bottom: 20px;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
</style>
