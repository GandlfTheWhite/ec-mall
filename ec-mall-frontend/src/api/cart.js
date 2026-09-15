import api from './axios'

// カート一覧を取得する
export const getCart = () => {
  return api.get('/cart')
}

// カートに商品を追加する
export const addToCart = (productId, quantity) => {
  return api.post('/cart/items', { productId, quantity })
}

// カート内商品の数量を更新する
export const updateCartItem = (productId, quantity) => {
  return api.put(`/cart/items/${productId}?quantity=${quantity}`)
}

// カートから商品を削除する
export const removeCartItem = (productId) => {
  return api.delete(`/cart/items/${productId}`)
}

// カートを空にする
export const clearCart = () => {
  return api.delete('/cart')
}
