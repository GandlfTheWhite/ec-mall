<template>
  <main class="page-wrap">
    <h1>商品一覧</h1>
    <el-card class="filters" shadow="never">
      <el-form :model="filters" label-position="top" @submit.prevent="search">
        <div class="filter-grid">
          <el-form-item label="キーワード"><el-input v-model.trim="filters.keyword" clearable placeholder="商品名" /></el-form-item>
          <el-form-item label="カテゴリ"><el-input v-model.trim="filters.category" clearable placeholder="カテゴリ名（完全一致）" /></el-form-item>
          <el-form-item label="最低価格"><el-input-number v-model="filters.minPrice" :min="0" :precision="2" :controls="false" /></el-form-item>
          <el-form-item label="最高価格"><el-input-number v-model="filters.maxPrice" :min="0" :precision="2" :controls="false" /></el-form-item>
        </div>
        <el-button type="primary" native-type="submit">検索</el-button><el-button @click="reset">条件をリセット</el-button>
      </el-form>
    </el-card>
    <el-alert v-if="error" class="error-alert" type="error" :title="error" :closable="false"><el-button text @click="load">再読み込み</el-button></el-alert>
    <el-row v-loading="loading" :gutter="20" class="product-grid">
      <el-col v-for="product in products" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="product-card" shadow="hover">
          <router-link :to="'/products/' + product.id" class="product-link">
            <el-image :src="product.imageUrl" class="product-image" fit="cover"><template #error><div class="no-image">画像はありません</div></template></el-image>
            <h2>{{ product.name }}</h2>
          </router-link>
          <p class="muted">{{ product.category || 'カテゴリ未設定' }}</p>
          <p class="price">¥{{ money(product.price) }}</p>
          <el-button type="primary" :disabled="product.status !== 1 || product.stock < 1" :loading="adding.has(product.id)" @click="add(product)">
            {{ product.status !== 1 ? '販売終了' : product.stock < 1 ? '在庫切れ' : 'カートに入れる' }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && !error && !products.length" description="条件に合う商品が見つかりません" />
    <el-pagination v-if="total > 0" :current-page="page" :page-size="size" :page-sizes="[10, 20, 50]"
      :total="total" layout="total, sizes, prev, pager, next" class="pager" @update:current-page="changePage" @update:page-size="resize" />
  </main>
</template>
<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProducts } from '@/api/product'
import { addToCart } from '@/api/cart'
import { errorMessage, money } from '@/utils/display'
const emptyFilters = () => ({ keyword: '', category: '', minPrice: undefined, maxPrice: undefined })
const filters = reactive(emptyFilters())
let applied = emptyFilters()
const products = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(true)
const error = ref('')
const adding = ref(new Set())
let request = 0
async function load() {
  const current = ++request
  loading.value = true; error.value = ''
  try {
    const { data } = await getProducts({ ...applied, page: page.value, size: size.value })
    if (current !== request) return
    products.value = data.content || []
    total.value = data.totalElements || 0
  } catch (e) { if (current === request) error.value = errorMessage(e) }
  finally { if (current === request) loading.value = false }
}
function search() {
  if (filters.minPrice != null && filters.maxPrice != null && filters.minPrice > filters.maxPrice) {
    return ElMessage.warning('最低価格は最高価格以下で入力してください。')
  }
  applied = { ...filters }
  page.value = 1
  load()
}
function changePage(value) {
  if (page.value === value) return
  page.value = value
  load()
}
function resize(value) { size.value = value; page.value = 1; load() }
function reset() { Object.assign(filters, emptyFilters()); search() }
async function add(product) {
  if (adding.value.has(product.id) || product.status !== 1 || product.stock < 1) return
  adding.value.add(product.id)
  try { await addToCart(product.id, 1); ElMessage.success('カートに追加しました。') }
  catch (e) { ElMessage.error(errorMessage(e)) }
  finally { adding.value.delete(product.id) }
}
onMounted(load)
onUnmounted(() => { request++ })
</script>
<style scoped>
.filters { margin-bottom: 24px; }
.filter-grid { display: grid; grid-template-columns: 2fr 1.5fr 1fr 1fr; gap: 16px; }
.filter-grid .el-input-number { width: 100%; }
.product-grid { min-height: 120px; row-gap: 20px; }
.product-card { height: 100%; }
.product-link { text-decoration: none; color: inherit; }
.product-link h2 { font-size: 18px; overflow-wrap: anywhere; }
.product-image { width: 100%; height: 180px; background: #f4f4f5; }
.no-image { height: 100%; display: grid; place-items: center; color: #909399; }
.price { color: #cc4747; font-size: 20px; font-weight: 700; }
.pager { margin-top: 24px; flex-wrap: wrap; gap: 8px; }
@media (max-width: 700px) { .filter-grid { grid-template-columns: 1fr 1fr; } }
</style>
