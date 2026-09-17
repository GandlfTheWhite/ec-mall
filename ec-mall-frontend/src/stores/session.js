import { reactive } from 'vue'
import { getCurrentMember } from '@/api/member'

export const session = reactive({ member: null })
let loadedToken = null
let pending = null

export function clearSession() {
  loadedToken = null
  pending = null
  session.member = null
  localStorage.removeItem('token')
  window.dispatchEvent(new Event('session-cleared'))
}
export async function loadSession(force = false) {
  const token = localStorage.getItem('token')
  if (!token) { session.member = null; return null }
  if (!force && loadedToken === token && session.member) return session.member
  if (pending?.token === token) return pending.promise
  const promise = getCurrentMember().then(({ data }) => {
    if (localStorage.getItem('token') !== token) return null
    session.member = data
    loadedToken = token
    return data
  }).finally(() => { if (pending?.promise === promise) pending = null })
  pending = { token, promise }
  return promise
}
