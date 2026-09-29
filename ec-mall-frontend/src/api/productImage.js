import api from './axios'

export function uploadProductImage(file, signal) {
  const data = new FormData()
  data.append('file', file)
  // multipart の boundary はブラウザーが設定する。
  return api.post('/admin/product-images', data, { timeout: 45000, signal })
}

export function imageFileError(file) {
  if (!file || file.size === 0) return '空のファイルはアップロードできません。'
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) return 'JPEG・PNG・WebPの画像を選択してください。'
  if (file.size > 5 * 1024 * 1024) return '画像は5 MB以下にしてください。'
  return ''
}
