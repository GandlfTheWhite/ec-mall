<template>
  <div class="product-list">
    <el-input v-model="keyword" placeholder="商品を検索" @input="handleSearch" />
    <el-row :gutter="20">
      <el-col v-for="p in products" :key="p.id" :span="6">
        <el-card class="product-card" shadow="hover" @click="goToDetail(p.id)">
          <img :src="p.imageUrl" class="product-image" />
          <h3>{{ p.name }}</h3>
          <p>¥{{ p.price }}</p>
          <el-button type="primary" @click="addToCart(p.id)">カートに入れる</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      @current-change="fetchProducts"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProducts } from '@/api/product'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

const products = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(import.meta.env.VITE_PAGE_SIZE || 10)
const keyword = ref('')

const fetchProducts = async () => {
  try {
    const res = await getProducts({ keyword: keyword.value, page: page.value, size: size.value })
    console.log('API レスポンス全体:', res)
    console.log('res.data の型:', Array.isArray(res.data) ? '配列' : 'オブジェクト')
    console.log('res.data の中身:', res.data)
    // レスポンス形式に応じて商品一覧を設定する
    if (Array.isArray(res.data)) {
      products.value = res.data
    } else if (res.data.content) {
      products.value = res.data.content
      total.value = res.data.totalElements
    }
  } catch (err) {
    ElMessage.error('商品一覧の取得に失敗しました')
  }
}

const handleSearch = () => {
  page.value = 1   // 検索時は1ページ目に戻す
  fetchProducts()
}

const addToCart = (productId) => {
  // 後で実装
  ElMessage.info('カートに追加（未実装）')
}

const goToDetail = (productId) => {
  router.push(`/products/${productId}`)
}

onMounted(fetchProducts)
</script>

<style scoped>
.product-list { padding: 20px; }
.product-card { margin-bottom: 20px; cursor: pointer; }
.product-image { width: 100%; height: 150px; object-fit: cover; }
</style>
