import { onUnmounted, ref } from 'vue'
import { errorMessage } from '@/utils/display'

export function useResource(fetcher) {
  const data = ref(null)
  const loading = ref(true)
  const error = ref('')
  let request = 0
  async function load() {
    const current = ++request
    loading.value = true
    error.value = ''
    try {
      const response = await fetcher()
      if (current === request) data.value = response.data
    } catch (e) {
      if (current === request) error.value = errorMessage(e)
    } finally {
      if (current === request) loading.value = false
    }
  }
  onUnmounted(() => { request++ })
  return { data, loading, error, load }
}
