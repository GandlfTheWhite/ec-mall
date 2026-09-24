<template>
  <main class="page-wrap">
    <h1>カート</h1>
    <el-skeleton v-if="loading" :rows="6" animated />
    <el-result v-else-if="error" icon="error" title="カートを取得できません" :sub-title="error"><template #extra><el-button @click="load">再読み込み</el-button></template></el-result>
    <el-empty v-else-if="!items.length" description="カートは空です"><el-button type="primary" @click="router.push('/products/search')">商品一覧へ</el-button></el-empty>
    <template v-else>
      <el-table :data="items" row-key="productId">
        <el-table-column label="商品" min-width="200"><template #default="{ row }"><router-link :to="'/products/' + row.productId">{{ row.productName }}</router-link></template></el-table-column>
        <el-table-column label="単価" width="150"><template #default="{ row }">¥{{ money(row.priceAtAdd) }}<small v-if="Number(row.priceAtAdd) !== Number(row.currentPrice)" class="muted">現在の価格：¥{{ money(row.currentPrice) }}</small></template></el-table-column>
        <el-table-column label="数量" width="190"><template #default="{ row }"><el-input-number :model-value="row.quantity" :min="1" :precision="0" :disabled="busy" @change="(value) => update(row, value)" /></template></el-table-column>
        <el-table-column label="小計" width="140"><template #default="{ row }">¥{{ money(row.priceAtAdd * row.quantity) }}</template></el-table-column>
        <el-table-column label="操作" width="90"><template #default="{ row }"><el-button link type="danger" :disabled="busy" @click="remove(row)">削除</el-button></template></el-table-column>
      </el-table>
      <div class="page-heading footer">
        <strong>合計金額：¥{{ money(cart.totalPrice) }}</strong>
        <div class="actions"><el-button :disabled="busy" @click="clear">カートを空にする</el-button><el-button type="primary" :disabled="busy" @click="router.push('/checkout')">注文へ進む</el-button></div>
      </div>
    </template>
  </main>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCart, updateCartItem, removeCartItem, clearCart } from '@/api/cart'
import { useResource } from '@/composables/useResource'
import { money, errorMessage, isCancelled, confirmOptions } from '@/utils/display'
const router = useRouter()
const { data: cart, loading, error, load } = useResource(getCart)
const items = computed(() => cart.value?.items || [])
const busy = ref(false)
async function mutate(operation, confirmation) {
  if (busy.value) return
  busy.value = true
  try {
    if (confirmation) await ElMessageBox.confirm(confirmation, '確認', confirmOptions)
    await operation()
    await load()
  } catch (e) { if (!isCancelled(e)) { ElMessage.error(errorMessage(e)); await load() } }
  finally { busy.value = false }
}
function update(row, quantity) {
  if (quantity === row.quantity) return
  if (!Number.isInteger(quantity) || quantity < 1) { ElMessage.warning('数量は1以上の整数で入力してください。'); return load() }
  return mutate(() => updateCartItem(row.productId, quantity))
}
const remove = (row) => mutate(() => removeCartItem(row.productId), 'この商品をカートから削除しますか？')
const clear = () => mutate(clearCart, 'カート内の商品をすべて削除しますか？')
onMounted(load)
</script>
<style scoped>.footer { margin-top: 24px; } small { display: block; margin-top: 6px; }</style>
