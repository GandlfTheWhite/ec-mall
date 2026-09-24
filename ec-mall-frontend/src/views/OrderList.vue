<template>
  <main class="page-wrap">
    <div class="page-heading"><h1>注文履歴</h1><el-button :loading="loading" @click="load">更新</el-button></div>
    <el-skeleton v-if="loading" :rows="6" animated />
    <el-result v-else-if="error" icon="error" title="注文履歴を取得できません" :sub-title="error"><template #extra><el-button @click="load">再読み込み</el-button></template></el-result>
    <el-empty v-else-if="!orders?.length" description="注文履歴はありません" />
    <el-table v-else :data="orders" row-key="id">
      <el-table-column prop="orderNo" label="注文番号" min-width="210" />
      <el-table-column label="注文日時" min-width="170"><template #default="{ row }">{{ dateTime(row.orderDate || row.createdAt) }}</template></el-table-column>
      <el-table-column label="合計金額" width="140"><template #default="{ row }">¥{{ money(row.totalAmount) }}</template></el-table-column>
      <el-table-column label="状況" width="130"><template #default="{ row }"><el-tag :type="orderStatus(row.status).type">{{ orderStatus(row.status).label }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="100"><template #default="{ row }"><el-button link type="primary" @click="router.push('/orders/' + row.id)">詳細</el-button></template></el-table-column>
    </el-table>
  </main>
</template>
<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyOrders } from '@/api/order'
import { useResource } from '@/composables/useResource'
import { money, dateTime, orderStatus } from '@/utils/display'
const router = useRouter()
const { data: orders, loading, error, load } = useResource(getMyOrders)
onMounted(load)
</script>
