import api from './axios'

// 注文を作成する
export const createOrder = (data) => {
  return api.post('/orders', data)
}

// 自分の注文一覧を取得する
export const getMyOrders = () => {
  return api.get('/orders')
}

// 注文詳細を取得する
export const getOrderDetail = (orderId) => {
  return api.get(`/orders/${orderId}`)
}

// 注文を支払済みにする（テスト用の疑似決済）
export const payOrder = (orderId) => {
  return api.post(`/orders/${orderId}/pay`)
}
