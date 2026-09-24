import api from './axios'
export function cartChanged() { window.dispatchEvent(new Event('cart-changed')) }
const changed = (response) => { cartChanged(); return response }
export const getCart = () => api.get('/cart')
export const addToCart = (productId, quantity) => api.post('/cart/items', { productId, quantity }).then(changed)
export const updateCartItem = (productId, quantity) => api.put(`/cart/items/${productId}`, null, { params: { quantity } }).then(changed)
export const removeCartItem = (productId) => api.delete(`/cart/items/${productId}`).then(changed)
export const clearCart = () => api.delete('/cart').then(changed)
