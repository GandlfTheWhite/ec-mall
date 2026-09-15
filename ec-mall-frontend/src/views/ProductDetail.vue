<template>
  <main class="product-detail">
    <el-button text class="back-button" @click="router.back()">← 商品一覧に戻る</el-button>
    <el-skeleton v-if="loading" :rows="8" animated />
    <el-result v-else-if="errorMessage" icon="error" title="商品の読み込みに失敗しました" :sub-title="errorMessage">
      <template #extra><el-button type="primary" @click="loadProduct">再読み込み</el-button></template>
    </el-result>
    <el-card v-else-if="product" class="detail-card" shadow="never">
      <section class="image-panel">
        <el-image class="product-image" :src="product.imageUrl" fit="contain" :preview-src-list="product.imageUrl ? [product.imageUrl] : []">
          <template #error><div class="image-placeholder">商品画像はありません</div></template>
        </el-image>
      </section>
      <section class="info-panel">
        <el-tag v-if="product.category" type="info">{{ product.category }}</el-tag>
        <h1>{{ product.name }}</h1>
        <p class="price">¥{{ formattedPrice }}</p>
        <p class="description">{{ product.description || '商品説明はありません' }}</p>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="商品番号">{{ product.id }}</el-descriptions-item>
          <el-descriptions-item label="在庫"><span :class="product.stock > 0 ? 'in-stock' : 'out-of-stock'">{{ product.stock > 0 ? `在庫あり（${product.stock} 点）` : '在庫切れ' }}</span></el-descriptions-item>
          <el-descriptions-item label="販売状況">{{ product.status === 1 ? '販売中' : '販売終了' }}</el-descriptions-item>
        </el-descriptions>
        <el-button type="primary" size="large" :disabled="product.stock <= 0 || product.status !== 1" @click="addToCart">カートに入れる</el-button>
      </section>
    </el-card>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductById } from '@/api/product'
import { addToCart as addProductToCart } from '@/api/cart'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const formattedPrice = computed(() => {
  const price = Number(product.value?.price)
  return Number.isFinite(price) ? price.toFixed(2) : '--'
})
const loadProduct = async () => {
  loading.value = true
  errorMessage.value = ''
  product.value = null
  try { product.value = (await getProductById(route.params.id)).data }
  catch (error) { errorMessage.value = error.response?.status === 404 ? '商品が見つからないか、削除されています。' : '商品情報を取得できません。時間をおいて再度お試しください。' }
  finally { loading.value = false }
}
const addToCart = async () => {
  try {
    await addProductToCart(product.value.id, 1)
    ElMessage.success('カートに追加しました')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'カートへの追加に失敗しました')
  }
}

watch(() => route.params.id, loadProduct)
onMounted(loadProduct)
</script>

<style scoped>
.product-detail { max-width: 1080px; margin: 0 auto; padding: 24px; }.back-button { margin-bottom: 16px; }
.detail-card :deep(.el-card__body) { display: grid; grid-template-columns: minmax(280px, 1fr) minmax(320px, 1fr); gap: 40px; }.image-panel { min-height: 360px; background: #f7f8fa; display: flex; align-items: center; justify-content: center; }.product-image { width: 100%; height: 360px; }.image-placeholder { color: #909399; }.info-panel { display: flex; flex-direction: column; align-items: flex-start; gap: 18px; }h1 { margin: 0; font-size: 28px; }.price { margin: 0; color: #f56c6c; font-size: 30px; font-weight: 700; }.description { margin: 0; white-space: pre-wrap; color: #606266; line-height: 1.7; }.in-stock { color: #67c23a; }.out-of-stock { color: #f56c6c; }@media (max-width: 720px) { .product-detail { padding: 16px; }.detail-card :deep(.el-card__body) { grid-template-columns: 1fr; gap: 24px; } }
</style>
