import assert from 'node:assert/strict'
import { test, before, after } from 'node:test'
import { fileURLToPath } from 'node:url'
import { mkdtemp, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { basename, dirname, join, resolve } from 'node:path'
import { createServer } from 'vite'
import vuePlugin from '@vitejs/plugin-vue'
import { createRenderer, createSSRApp, h, nextTick, reactive } from 'vue'
import { createRouter, createMemoryHistory } from 'vue-router'
import { renderToString } from '@vue/server-renderer'
import { hasValidToken, localRedirect } from '../src/utils/auth.js'

const storage = new Map()
globalThis.localStorage = {
  getItem: (key) => storage.get(key) ?? null,
  setItem: (key, value) => storage.set(key, String(value)),
  removeItem: (key) => storage.delete(key),
}
globalThis.window = new EventTarget()
window.location = { pathname: '/login', assign() {} }
const root = fileURLToPath(new URL('../', import.meta.url))
const validToken = 'header.' + Buffer.from(JSON.stringify({ exp: 4102444800 })).toString('base64url') + '.signature'
let server, api, memberApi, session, guard, cacheDir
let handler
const renderer = createRenderer({
  createElement: (tag) => ({ tag, children: [] }),
  createText: (text) => ({ text }),
  createComment: (text) => ({ text }),
  insert: (node, parent) => parent.children.push(node),
  remove() {}, setText() {}, setElementText() {}, patchProp() {},
  parentNode() { return null }, nextSibling() { return null },
})
const settle = async () => { for (let i = 0; i < 5; i++) { await new Promise(setImmediate); await nextTick() } }
const moduleAt = (path) => server.ssrLoadModule('/src/' + path)
async function memoryRouter(path = '/') {
  const router = createRouter({ history: createMemoryHistory(), routes: [
    { path: '/:pathMatch(.*)*', component: { render: () => null } },
  ] })
  await router.push(path)
  return router
}
async function mountView(path, url = '/', props = {}) {
  const component = (await moduleAt(path)).default
  const router = await memoryRouter(url)
  let bindings
  const app = renderer.createApp({
    setup(_, context) {
      bindings = component.setup(props, context)
      return () => null
    },
  })
  app.use(router)
  app.provide(Symbol.for('v-scx'), {})
  app.mount({ children: [] })
  await settle()
  return { bindings, router, unmount: () => app.unmount() }
}
before(async () => {
  cacheDir = await mkdtemp(join(tmpdir(), 'ec-mall-vite-test-'))
  server = await createServer({
    root, cacheDir, configFile: false, plugins: [vuePlugin()],
    resolve: { alias: {
      '@': fileURLToPath(new URL('../src', import.meta.url)),
      'element-plus': fileURLToPath(new URL('./ui-mock.js', import.meta.url)),
    } },
    server: { middlewareMode: true }, appType: 'custom', optimizeDeps: { noDiscovery: true, include: [] },
  })
  api = (await moduleAt('api/axios.js')).default
  api.defaults.adapter = async (config) => {
    const result = await handler(config)
    return { status: 200, statusText: 'OK', headers: {}, config, data: result }
  }
  memberApi = await moduleAt('api/member.js')
  session = await moduleAt('stores/session.js')
  guard = (await moduleAt('router/index.js')).guard
})
after(async () => {
  await server?.close()
  if (cacheDir) {
    assert.equal(dirname(resolve(cacheDir)), resolve(tmpdir()))
    assert.ok(basename(cacheDir).startsWith('ec-mall-vite-test-'))
    await rm(cacheDir, { recursive: true, force: true })
  }
})

test('JWT expiry and internal redirect validation', () => {
  assert.equal(hasValidToken(validToken), true)
  const expired = 'h.' + Buffer.from(JSON.stringify({ exp: 1 })).toString('base64url') + '.s'
  assert.equal(hasValidToken(expired), false)
  assert.equal(hasValidToken('broken'), false)
  assert.equal(localRedirect('//example.com'), '/products/search')
  assert.equal(localRedirect('/\\example.com'), '/products/search')
  assert.equal(localRedirect('/orders/7'), '/orders/7')
})

test('public registration sends no stale bearer token', async () => {
  localStorage.setItem('token', validToken)
  let sent
  handler = (config) => { sent = config; return { id: 10 } }
  await memberApi.register({ name: 'テスト', email: 'test@example.com', age: 20, password: 'pass1234' })
  assert.equal(sent.url, '/members')
  assert.equal(sent.headers.Authorization, undefined)
})

test('admin guard checks server role; ordinary users cannot enter management', async () => {
  session.clearSession()
  localStorage.setItem('token', validToken)
  handler = () => ({ id: 1, role: 'USER', name: '会員' })
  assert.equal(await guard({ path: '/admin/products', fullPath: '/admin/products', meta: { requiresAuth: true, admin: true }, query: {} }), '/forbidden')
  handler = () => ({ id: 1, role: 'ADMIN', name: '管理者' })
  assert.equal(await guard({ path: '/admin/products', fullPath: '/admin/products', meta: { requiresAuth: true, admin: true }, query: {} }), undefined)
  localStorage.removeItem('token')
  assert.equal((await guard({ path: '/cart', fullPath: '/cart', meta: { requiresAuth: true }, query: {} })).path, '/login')
})

test('detail initial render is safe before API response', async () => {
  handler = () => new Promise(() => {})
  for (const view of ['ProductDetail', 'OrderDetail']) {
    const component = (await moduleAt('views/' + view + '.vue')).default
    const app = createSSRApp(component)
    app.use(await memoryRouter(view === 'ProductDetail' ? '/products/1' : '/orders/1'))
    app.component('ElButton', { render: () => h('button') })
    app.component('ElSkeleton', { render: () => h('div', '読み込み中') })
    app.config.warnHandler = () => {}
    const html = await renderToString(app)
    assert.ok(html.includes('読み込み中'))
  }
})

test('catalog forwards all search criteria and adds selected product without navigation', async () => {
  const calls = []
  handler = (config) => { calls.push(config); return config.url === '/products/search' ? { content: [], totalElements: 0 } : null }
  const view = await mountView('views/ProductList.vue', '/products/search')
  const b = view.bindings
  Object.assign(b.filters, { keyword: '本', category: '書籍', minPrice: 100, maxPrice: 500 })
  b.search()
  await settle()
  assert.deepEqual(calls.filter((c) => c.url === '/products/search').at(-1).params,
    { keyword: '本', category: '書籍', minPrice: 100, maxPrice: 500, page: 1, size: 10 })
  await b.add({ id: 7, status: 1, stock: 2 })
  assert.deepEqual(JSON.parse(calls.at(-1).data), { productId: 7, quantity: 1 })
  assert.equal(view.router.currentRoute.value.path, '/products/search')
  view.unmount()
})

test('search and page size changes explicitly reload from page one', async () => {
  const calls = []
  handler = (config) => { calls.push(config); return { content: [], totalElements: 100 } }
  const view = await mountView('views/ProductList.vue', '/products/search')
  const b = view.bindings
  b.changePage(3)
  await settle()
  assert.equal(calls.at(-1).params.page, 3)
  b.filters.keyword = '新しい条件'
  b.search()
  await settle()
  assert.equal(calls.at(-1).params.page, 1)
  assert.equal(calls.at(-1).params.keyword, '新しい条件')
  b.changePage(2)
  b.resize(20)
  await settle()
  assert.equal(calls.at(-1).params.page, 1)
  assert.equal(calls.at(-1).params.size, 20)
  view.unmount()
})

test('member form retains input during saving and admin dialog resets only on open', async () => {
  handler = () => []
  const admin = await mountView('views/admin/Members.vue', '/admin/members')
  const b = admin.bindings
  await b.open()
  const firstVersion = b.formVersion.value
  const props = reactive({ member: b.emptyMember, creating: true, busy: false })
  const form = await mountView('components/MemberForm.vue', '/', props)
  form.bindings.form.name = '入力中の氏名'
  props.busy = true
  b.saving.value = true
  await settle()
  assert.equal(form.bindings.form.name, '入力中の氏名')
  assert.equal(b.formVersion.value, firstVersion)
  b.saving.value = false
  await b.open()
  assert.equal(b.formVersion.value, firstVersion + 1)
  form.unmount()
  admin.unmount()
})

test('product image uploads before saving and its URL is stored with the product', async () => {
  const calls = []
  handler = (config) => {
    calls.push(config)
    if (config.url === '/products') return config.method === 'get' ? [] : { id: 5 }
    if (config.url === '/admin/product-images') return { imageUrl: 'https://media.example.com/products/photo.png' }
    return null
  }
  const view = await mountView('views/admin/Products.vue', '/admin/products')
  await view.bindings.open()
  view.bindings.formRef.value = { validate: async () => true, clearValidate() {} }
  const file = new Blob(['image bytes'], { type: 'image/png' })
  view.bindings.selectImage({ target: { files: [file], value: 'photo.png' } })
  view.bindings.form.name = '画像付き商品'
  await view.bindings.save()
  const uploadIndex = calls.findIndex((c) => c.url === '/admin/product-images')
  const createIndex = calls.findIndex((c) => c.url === '/products' && c.method === 'post')
  assert.ok(uploadIndex >= 0 && createIndex > uploadIndex)
  assert.equal(calls[uploadIndex].data.get('file').type, 'image/png')
  assert.equal(JSON.parse(calls[createIndex].data).imageUrl, 'https://media.example.com/products/photo.png')
  view.unmount()
})

test('catalog ignores a delayed response from an older search', async () => {
  let firstResponse
  handler = (config) => config.params.keyword === '新着'
    ? { content: [{ id: 2, name: '新着商品' }], totalElements: 1 }
    : new Promise((resolve) => { firstResponse = resolve })
  const view = await mountView('views/ProductList.vue', '/products/search')
  view.bindings.filters.keyword = '新着'
  view.bindings.search()
  await settle()
  firstResponse({ content: [{ id: 1, name: '古い検索結果' }], totalElements: 1 })
  await settle()
  assert.equal(view.bindings.products.value[0].id, 2)
  view.unmount()
})

test('failed checkout load cannot create an order; valid submit only creates once', async () => {
  let creates = 0
  handler = (config) => {
    if (config.url === '/cart') throw { response: { status: 500, data: {} } }
    creates++
  }
  let view = await mountView('views/Checkout.vue', '/checkout')
  await view.bindings.submit()
  assert.equal(creates, 0)
  view.unmount()
  handler = (config) => {
    if (config.url === '/cart') return { items: [{ productId: 1, quantity: 1, priceAtAdd: 100 }], totalPrice: 100 }
    creates++
    return { id: 8 }
  }
  view = await mountView('views/Checkout.vue', '/checkout')
  view.bindings.formRef.value = { validate: async () => true }
  Object.assign(view.bindings.form, { receiverName: '山田', receiverPhone: '09012345678', shippingAddress: '東京都' })
  await Promise.all([view.bindings.submit(), view.bindings.submit()])
  assert.equal(creates, 1)
  assert.equal(view.router.currentRoute.value.path, '/orders/8')
  view.unmount()
})

test('payment response without items keeps the existing detail lines', async () => {
  const items = [{ productId: 1, productName: '本', quantity: 1, price: 100 }]
  handler = (config) => config.url.endsWith('/pay')
    ? { id: 8, status: 1, items: null }
    : { id: 8, status: 0, items, totalAmount: 100 }
  const view = await mountView('views/OrderDetail.vue', '/orders/8')
  await view.bindings.pay()
  assert.equal(view.bindings.order.value.status, 1)
  assert.deepEqual(JSON.parse(JSON.stringify(view.bindings.order.value.items)), items)
  view.unmount()
})

test('profile save omits unchanged password and renders backend member identity', async () => {
  let payload
  const member = { id: 1, name: '山田', email: 'user@example.com', age: 20, role: 'USER' }
  handler = (config) => { if (config.method === 'put') payload = JSON.parse(config.data); return member }
  localStorage.setItem('token', validToken)
  const view = await mountView('views/Profile.vue', '/profile')
  await view.bindings.save({ name: '山田太郎', email: member.email, age: 21 })
  assert.equal(payload.password, undefined)
  assert.equal(payload.name, '山田太郎')
  view.unmount()
})

test('administrator mutations use the backend parameter contracts', async () => {
  const calls = []
  handler = (config) => { calls.push(config); return [] }
  const admin = await moduleAt('api/admin.js')
  await admin.updateProductStock(4, 10)
  await admin.updateProductStatus(4, 0)
  await admin.updateMemberRole(3, 'ADMIN')
  await admin.updateOrderStatus(8, 2)
  assert.deepEqual(calls.map((c) => [c.method, c.url, c.params]), [
    ['put', '/admin/products/4/stock', { stock: 10 }],
    ['put', '/admin/products/4/status', { status: 0 }],
    ['put', '/admin/members/3/role', { role: 'ADMIN' }],
    ['put', '/admin/orders/8/status', { status: 2 }],
  ])
})
