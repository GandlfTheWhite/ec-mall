<template>
  <main class="page-wrap">
    <el-button text @click="router.push('/products/search')">← 商品一覧に戻る</el-button>
    <el-skeleton v-if="loading" :rows="8" animated />
    <el-result v-else-if="error" icon="error" title="商品を読み込めません" :sub-title="error"><template #extra><el-button @click="load">再読み込み</el-button></template></el-result>
    <el-card v-else-if="product" class="detail" shadow="never">
      <el-image :src="product.imageUrl" class="image" fit="contain" :preview-src-list="product.imageUrl ? [product.imageUrl] : []">
        <template #error><div class="no-image">商品画像はありません</div></template>
      </el-image>
      <section>
        <el-tag v-if="product.category">{{ product.category }}</el-tag>
        <h1>{{ product.name }}</h1>
        <p class="price">¥{{ money(product.price) }}</p>
        <p class="description">{{ product.description || '商品説明はありません' }}</p>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="商品番号">{{ product.id }}</el-descriptions-item>
          <el-descriptions-item label="在庫">{{ product.stock > 0 ? product.stock + '点' : '在庫切れ' }}</el-descriptions-item>
          <el-descriptions-item label="販売状況">{{ product.status === 1 ? '販売中' : '販売終了' }}</el-descriptions-item>
        </el-descriptions>
        <div class="actions buy">
          <el-input-number v-model="quantity" :min="1" :max="Math.max(product.stock || 1, 1)" :precision="0" :disabled="!available || adding" aria-label="購入数量" />
          <el-button type="primary" :disabled="!available" :loading="adding" @click="add">カートに入れる</el-button>
        </div>
      </section>
    </el-card>
  </main>
</template>
<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getProductById } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useResource } from '@/composables/useResource'
import { money, errorMessage } from '@/utils/display'
const route = useRoute()
const router = useRouter()
const { data: product, loading, error, load } = useResource(() => getProductById(route.params.id))
const quantity = ref(1)
const adding = ref(false)
const available = computed(() => product.value?.status === 1 && product.value.stock > 0)
async function add() {
  if (adding.value || !available.value) return
  if (!Number.isInteger(quantity.value) || quantity.value < 1 || quantity.value > product.value.stock) {
    return ElMessage.warning('数量を確認してください。')
  }
  adding.value = true
  try { await addToCart(product.value.id, quantity.value); ElMessage.success('カートに追加しました。') }
  catch (e) { ElMessage.error(errorMessage(e)) }
  finally { adding.value = false }
}
watch(() => route.params.id, () => { quantity.value = 1; load() }, { immediate: true })
</script>
<style scoped>
.detail { margin-top: 16px; }
.detail :deep(.el-card__body) { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 36px; }
.image { width: 100%; height: 380px; background: #f4f4f5; }
.no-image { height: 100%; display: grid; place-items: center; color: #909399; }
.price { font-size: 30px; font-weight: 700; color: #cc4747; }
.description { white-space: pre-wrap; overflow-wrap: anywhere; line-height: 1.8; }
.buy { margin-top: 20px; }
@media (max-width: 700px) { .detail :deep(.el-card__body) { grid-template-columns: 1fr; } }
</style>
