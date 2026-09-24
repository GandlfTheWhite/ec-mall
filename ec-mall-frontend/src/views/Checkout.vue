<template>
  <main class="page-wrap narrow">
    <h1>注文内容の確認</h1>
    <el-skeleton v-if="loading" :rows="8" animated />
    <el-result v-else-if="error" icon="error" title="カートを取得できません" :sub-title="error"><template #extra><el-button @click="load">再読み込み</el-button></template></el-result>
    <el-empty v-else-if="!items.length && !createdOrder" description="カートは空です"><el-button @click="router.push('/products/search')">商品一覧へ</el-button></el-empty>
    <el-result v-else-if="createdOrder" icon="success" title="注文を作成しました"><template #extra><el-button type="primary" @click="router.replace('/orders/' + createdOrder)">注文詳細へ</el-button></template></el-result>
    <template v-else>
      <el-table :data="items">
        <el-table-column prop="productName" label="商品名" min-width="150" />
        <el-table-column label="単価" width="130"><template #default="{ row }">¥{{ money(row.priceAtAdd) }}</template></el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小計" width="130"><template #default="{ row }">¥{{ money(row.priceAtAdd * row.quantity) }}</template></el-table-column>
      </el-table>
      <p class="total">合計金額：<strong>¥{{ money(cart.totalPrice) }}</strong></p>
      <el-card shadow="never">
        <template #header>配送先情報</template>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="submitting" @submit.prevent="submit">
          <el-form-item label="受取人氏名" prop="receiverName"><el-input v-model.trim="form.receiverName" autocomplete="shipping name" /></el-form-item>
          <el-form-item label="電話番号" prop="receiverPhone"><el-input v-model.trim="form.receiverPhone" type="tel" autocomplete="shipping tel" /></el-form-item>
          <el-form-item label="配送先住所" prop="shippingAddress"><el-input v-model.trim="form.shippingAddress" type="textarea" :rows="3" autocomplete="shipping street-address" /></el-form-item>
          <el-button @click="router.push('/cart')">カートへ戻る</el-button>
          <el-button type="primary" native-type="submit" :loading="submitting" :disabled="!items.length">注文を確定する</el-button>
        </el-form>
      </el-card>
    </template>
  </main>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCart, cartChanged } from '@/api/cart'
import { createOrder } from '@/api/order'
import { useResource } from '@/composables/useResource'
import { money, errorMessage } from '@/utils/display'
const router = useRouter()
const { data: cart, loading, error, load } = useResource(getCart)
const items = computed(() => cart.value?.items || [])
const formRef = ref()
const submitting = ref(false)
const createdOrder = ref(null)
const form = reactive({ receiverName: '', receiverPhone: '', shippingAddress: '' })
const rules = Object.fromEntries(Object.keys(form).map((key) => [key, [{ required: true, whitespace: true, message: '入力してください。', trigger: 'blur' }]]))
async function submit() {
  if (submitting.value || loading.value || error.value || !items.value.length || createdOrder.value) return
  submitting.value = true
  if (!await formRef.value.validate().catch(() => false)) { submitting.value = false; return }
  try {
    const { data } = await createOrder({ ...form })
    createdOrder.value = data.id
    cartChanged()
    ElMessage.success('注文を作成しました。')
  } catch (e) { ElMessage.error(errorMessage(e)); return }
  finally { submitting.value = false }
  await router.replace('/orders/' + createdOrder.value)
}
onMounted(load)
</script>
