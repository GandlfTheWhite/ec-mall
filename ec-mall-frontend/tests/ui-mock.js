export const messages = []
export const ElMessage = Object.assign((message) => messages.push(message), Object.fromEntries(
  ['success', 'error', 'warning', 'info'].map((type) => [type, (message) => messages.push({ type, message })]),
))
export const ElMessageBox = { confirm: async () => true }
