<template>
  <div class="cart-container">
    <h2>🛒 ショッピングカート</h2>

    <!-- 読み込み中 -->
    <div v-if="loading" class="loading">読み込み中...</div>

    <!-- 空カート -->
    <div v-else-if="cart.items.length === 0" class="empty-cart">
      <p>カートは空です</p>
      <el-button type="primary" @click="$router.push('/products/search')">
        商品一覧へ戻る
      </el-button>
    </div>

    <!-- カート一覧 -->
    <div v-else>
      <el-table :data="cart.items" style="width: 100%">
        <el-table-column label="商品" min-width="250">
          <template #default="{ row }">
            <div class="product-cell">
              <img :src="row.imageUrl" class="product-image" />
              <span>{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="単価" prop="priceAtAdd" width="120">
          <template #default="{ row }">
            ¥{{ row.priceAtAdd }}
          </template>
        </el-table-column>

        <el-table-column label="数量" width="180">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="99"
              size="small"
              @change="updateQuantity(row.productId, row.quantity)"
            />
          </template>
        </el-table-column>

        <el-table-column label="小計" width="150">
          <template #default="{ row }">
            ¥{{ row.priceAtAdd * row.quantity }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="removeItem(row.productId)">
              削除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 合計 + 操作ボタン -->
      <div class="cart-footer">
        <div class="total">
          <span>合計金額：</span>
          <strong>¥{{ totalPrice }}</strong>
        </div>
        <div class="actions">
          <el-button type="danger" plain @click="handleClearCart">
            カートを空にする
          </el-button>
          <el-button type="success" @click="handleCheckout">
            注文に進む
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCart, updateCartItem, removeCartItem, clearCart } from '@/api/cart'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const cart = ref({ items: [] })

// 合計金額
const totalPrice = computed(() => {
  return cart.value.items.reduce((sum, item) => {
    return sum + item.priceAtAdd * item.quantity
  }, 0)
})

// 購入カート取得
const fetchCart = async () => {
  loading.value = true
  try {
    const res = await getCart()
    cart.value = res.data
  } catch (err) {
    ElMessage.error('カート情報の取得に失敗しました')
  } finally {
    loading.value = false
  }
}

// 数量更新
const updateQuantity = async (productId, quantity) => {
  try {
    await updateCartItem(productId, quantity)
    // 更新成功 → 再取得（最新のデータを反映）
    await fetchCart()
  } catch (err) {
    ElMessage.error('数量更新に失敗しました')
    await fetchCart() // ロールバック（サーバー側と整合性を保つ）
  }
}

// 商品削除
const removeItem = async (productId) => {
  try {
    await ElMessageBox.confirm('この商品をカートから削除しますか？', '確認', {
      confirmButtonText: '削除',
      cancelButtonText: 'キャンセル',
      type: 'warning',
    })
    await removeCartItem(productId)
    ElMessage.success('削除しました')
    await fetchCart()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('削除に失敗しました')
    }
  }
}

// カートを空にする
const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm('カートをすべて空にしますか？', '確認', {
      confirmButtonText: '空にする',
      cancelButtonText: 'キャンセル',
      type: 'warning',
    })
    await clearCart()
    ElMessage.success('カートを空にしました')
    await fetchCart()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('カートのクリアに失敗しました')
    }
  }
}

// 注文へ進む
const handleCheckout = () => {
  if (cart.value.items.length === 0) {
    ElMessage.warning('カートが空です')
    return
  }
  router.push('/checkout')
}

onMounted(fetchCart)
</script>

<style scoped>
.cart-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}
.empty-cart {
  text-align: center;
  padding: 80px 0;
}
.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 16px 0;
  border-top: 1px solid #ddd;
}
.total {
  font-size: 18px;
}
.actions {
  display: flex;
  gap: 12px;
}
</style>
