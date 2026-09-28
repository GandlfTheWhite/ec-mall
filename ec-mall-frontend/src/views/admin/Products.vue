<template>
  <main class="page-wrap">
    <div class="page-heading"><h1>商品管理</h1><el-button type="primary" @click="open()">商品を登録</el-button></div>
    <el-alert v-if="error" class="error-alert" :title="error" type="error" :closable="false"><el-button text @click="load">再読み込み</el-button></el-alert>
    <el-table v-loading="loading" :data="products" row-key="id" empty-text="商品はありません">
      <el-table-column prop="id" label="番号" width="80" />
      <el-table-column prop="name" label="商品名" min-width="160" />
      <el-table-column prop="category" label="カテゴリ" min-width="100" />
      <el-table-column label="価格" width="120"><template #default="{ row }">¥{{ money(row.price) }}</template></el-table-column>
      <el-table-column prop="stock" label="在庫" width="80" />
      <el-table-column label="公開状態" width="110"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '公開' : '非公開' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" min-width="280">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="busy" @click="open(row)">編集</el-button>
          <el-button link :disabled="busy" @click="openStock(row)">在庫変更</el-button>
          <el-button link :disabled="busy" @click="toggle(row)">{{ row.status === 1 ? '非公開にする' : '公開する' }}</el-button>
          <el-button link type="danger" :disabled="busy" @click="remove(row)">削除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="editing" :title="productId ? '商品を編集' : '商品を登録'" width="600px" :close-on-click-modal="!saving" :close-on-press-escape="!saving" :show-close="!saving">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="saving" @submit.prevent="save">
        <el-form-item label="商品名" prop="name"><el-input v-model.trim="form.name" /></el-form-item>
        <el-form-item label="商品説明"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="価格" prop="price"><el-input-number v-model="form.price" :min="0.01" :precision="2" /></el-form-item>
        <el-form-item label="在庫数" prop="stock"><el-input-number v-model="form.stock" :min="0" :precision="0" /></el-form-item>
        <el-form-item label="カテゴリ"><el-input v-model.trim="form.category" /></el-form-item>
        <el-form-item label="商品画像">
          <input type="file" accept="image/jpeg,image/png,image/webp" :disabled="saving" aria-label="商品画像を選択" @change="selectImage" />
          <p class="image-hint">JPEG・PNG・WebP、5MB以下。保存時にアップロードします。</p>
          <img v-if="imagePreview || form.imageUrl" class="image-preview" :src="imagePreview || form.imageUrl" alt="商品画像のプレビュー" />
        </el-form-item>
        <el-form-item label="画像URL（既存のURLも使用できます）" prop="imageUrl"><el-input v-model.trim="form.imageUrl" placeholder="https://…" /></el-form-item>
        <el-button native-type="submit" type="primary" :loading="saving">保存する</el-button>
      </el-form>
    </el-dialog>
    <el-dialog v-model="stockDialog" title="在庫数の変更" width="420px" :show-close="!saving" :close-on-click-modal="!saving" :close-on-press-escape="!saving">
      <p>{{ stockProduct?.name }}</p>
      <el-input-number v-model="stock" :min="0" :precision="0" :disabled="saving" aria-label="在庫数" />
      <template #footer><el-button type="primary" :loading="saving" @click="saveStock">保存する</el-button></template>
    </el-dialog>
  </main>
</template>
<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllProducts, getProductById, createProduct, updateProduct, deleteProduct } from '@/api/product'
import { updateProductStatus, updateProductStock, uploadProductImage } from '@/api/admin'
import { money, errorMessage, isCancelled, confirmOptions } from '@/utils/display'
const products = ref([])
const loading = ref(true)
const busy = ref(false)
const error = ref('')
const editing = ref(false)
const saving = ref(false)
const productId = ref(null)
const formRef = ref()
const imageFile = ref(null)
const imagePreview = ref('')
const maxImageBytes = 5 * 1024 * 1024
const imageTypes = new Set(['image/jpeg', 'image/png', 'image/webp'])
function clearImageSelection() {
  imageFile.value = null
  if (imagePreview.value) URL.revokeObjectURL(imagePreview.value)
  imagePreview.value = ''
}
function selectImage(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!imageTypes.has(file.type) || file.size === 0 || file.size > maxImageBytes) {
    ElMessage.warning('JPEG・PNG・WebP形式の5MB以下の画像を選択してください。')
    return
  }
  clearImageSelection()
  imageFile.value = file
  imagePreview.value = URL.createObjectURL(file)
}
const blank = () => ({ name: '', description: '', price: 0.01, stock: 0, category: '', imageUrl: '' })
const form = reactive(blank())
const stockDialog = ref(false)
const stockProduct = ref(null)
const stock = ref(0)
const rules = {
  name: [{ required: true, whitespace: true, message: '商品名を入力してください。' }],
  price: [{ required: true, type: 'number', min: 0.01, message: '価格は0.01以上で入力してください。' }],
  stock: [{ required: true, type: 'integer', min: 0, message: '在庫数は0以上の整数で入力してください。' }],
  imageUrl: [{ validator: (_, value, done) => done(!value || /^(https?:\/\/|\/)/.test(value) ? undefined : new Error('画像URLをご確認ください。')), trigger: 'blur' }],
}
async function load() {
  loading.value = true; error.value = ''
  try { products.value = (await getAllProducts()).data }
  catch (e) { error.value = errorMessage(e) }
  finally { loading.value = false }
}
async function open(product) {
  if (busy.value) return
  busy.value = true
  try {
    const data = product ? (await getProductById(product.id)).data : blank()
    clearImageSelection()
    productId.value = product?.id || null
    Object.assign(form, blank(), Object.fromEntries(Object.keys(blank()).map((key) => [key, data[key] ?? blank()[key]])))
    editing.value = true
    await nextTick()
    formRef.value?.clearValidate()
  } catch (e) { ElMessage.error(errorMessage(e)) }
  finally { busy.value = false }
}
async function save() {
  if (saving.value) return
  saving.value = true
  if (!await formRef.value.validate().catch(() => false)) { saving.value = false; return }
  try {
    if (imageFile.value) {
      const { data } = await uploadProductImage(imageFile.value)
      form.imageUrl = data.imageUrl
      clearImageSelection()
    }
    if (productId.value) await updateProduct(productId.value, { ...form })
    else await createProduct({ ...form })
    editing.value = false
    clearImageSelection()
    ElMessage.success('商品を保存しました。')
    await load()
  } catch (e) { ElMessage.error(errorMessage(e)) }
  finally { saving.value = false }
}
async function mutate(message, operation) {
  if (busy.value) return
  busy.value = true
  try {
    await ElMessageBox.confirm(message, '確認', confirmOptions)
    await operation()
    ElMessage.success('更新しました。')
    await load()
  } catch (e) { if (!isCancelled(e)) ElMessage.error(errorMessage(e)) }
  finally { busy.value = false }
}
const toggle = (row) => mutate('商品の公開状態を変更しますか？', () => updateProductStatus(row.id, row.status === 1 ? 0 : 1))
const remove = (row) => mutate('商品「' + row.name + '」を削除しますか？', () => deleteProduct(row.id))
function openStock(row) { stockProduct.value = row; stock.value = row.stock; stockDialog.value = true }
async function saveStock() {
  if (saving.value) return
  if (!Number.isInteger(stock.value) || stock.value < 0) return ElMessage.warning('在庫数は0以上の整数で入力してください。')
  saving.value = true
  try {
    await updateProductStock(stockProduct.value.id, stock.value)
    stockDialog.value = false
    ElMessage.success('在庫数を更新しました。')
    await load()
  } catch (e) { ElMessage.error(errorMessage(e)) }
  finally { saving.value = false }
}
onMounted(load)
onBeforeUnmount(clearImageSelection)
</script>
<style scoped>
.image-hint { margin: 4px 0; color: #909399; font-size: 12px; }
.image-preview { display: block; width: 160px; height: 120px; object-fit: contain; margin-top: 8px; background: #f4f4f5; }
</style>
