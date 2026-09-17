export const money = (value) => value == null || !Number.isFinite(Number(value))
  ? '--' : Number(value).toLocaleString('ja-JP', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
export const dateTime = (value) => {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '--' : date.toLocaleString('ja-JP')
}
export const orderStatuses = [
  { value: 0, label: '未払い', type: 'warning' },
  { value: 1, label: '支払い済み', type: 'success' },
  { value: 2, label: '発送済み', type: 'primary' },
  { value: 3, label: '完了', type: 'success' },
  { value: 4, label: 'キャンセル', type: 'info' },
]
export const orderStatus = (value) => orderStatuses.find((item) => item.value === value)
  || { label: '不明', type: 'info' }
export const nextOrderStatuses = (status) => ({ 0: [1, 4], 1: [2, 4], 2: [3] })[status] || []
export const isCancelled = (error) => error === 'cancel' || error === 'close'
export const errorMessage = (error, fallback = '処理に失敗しました。再度お試しください。') => {
  const status = error.response?.status
  if (status === 401) return 'ログインの有効期限が切れました。再度ログインしてください。'
  if (status === 403) return 'この操作を行う権限がありません。'
  if (!error.response) return 'サーバーに接続できません。接続状況をご確認ください。'
  const message = error.response.data?.message
  return typeof message === 'string' && /[ぁ-んァ-ヶ]/u.test(message) ? message
    : status === 409 ? '入力内容が既存の情報と重複しているか、状態が変更されています。' : fallback
}
export const confirmOptions = { confirmButtonText: '実行する', cancelButtonText: 'キャンセル', type: 'warning' }
