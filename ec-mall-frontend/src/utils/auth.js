export function hasValidToken(token, now = Date.now()) {
  try {
    if (typeof token !== 'string' || token.split('.').length !== 3) return false
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const { exp } = JSON.parse(atob(payload.padEnd(Math.ceil(payload.length / 4) * 4, '=')))
    return Number.isFinite(exp) && exp * 1000 > now
  } catch { return false }
}
export function localRedirect(value, fallback = '/products/search') {
  return typeof value === 'string' && /^\/(?!\/)/.test(value)
    && !/[\\\x00-\x20]/.test(value) && !/^\/(login|register|connection-error)([/?#]|$)/.test(value)
    ? value : fallback
}
