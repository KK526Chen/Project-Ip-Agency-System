import assert from 'node:assert/strict'
import test from 'node:test'
import { reactive } from 'vue'
import { flush, loadView } from './load-view.js'

const routeFor = (resource, id = '1') => reactive({ meta: { resource }, params: { id } })
const page = list => ({ data: { list, total: list.length } })
const deferred = () => { let resolve; const promise = new Promise(r => { resolve = r }); return { promise, resolve } }

test('public filters send the category parameter expected by each backend endpoint', async t => {
  for (const [resource, key, category] of [['services', 'serviceType', 'PATENT_APPLICATION'], ['success-cases', 'serviceType', 'TRADEMARK_REGISTRATION'], ['announcements', 'announcementType', 'POLICY']]) {
    let params
    const view = await loadView('public/PublicListView.vue', routeFor(resource), {
      '../../api/public': { publicList: async (_, value) => { params = value; return page([]) } },
    })
    t.after(view.stop)
    view.state.category = category
    await view.load()
    assert.equal(params[key], category)
    assert.equal(params.category, undefined)
  }
})

test('switching public resources clears stale search and results', async t => {
  const route = routeFor('services')
  const view = await loadView('public/PublicListView.vue', route, {
    '../../api/public': { publicList: async resource => page([{ id: 1, resource }]) },
  })
  t.after(view.stop); await flush()
  view.state.keyword = '专利'; view.state.category = 'PATENT_APPLICATION'; view.state.pageNum = 3
  route.meta.resource = 'announcements'; await flush()
  assert.equal(view.state.keyword, '')
  assert.equal(view.state.category, '')
  assert.equal(view.state.pageNum, 1)
  assert.equal(view.state.list[0].resource, 'announcements')
})

test('older public list response cannot overwrite the latest query', async t => {
  let calls = 0; const old = deferred()
  const view = await loadView('public/PublicListView.vue', routeFor('services'), {
    '../../api/public': { publicList: () => ++calls === 1 ? old.promise : Promise.resolve(page([{ id: 2 }])) },
  })
  t.after(view.stop)
  await view.load(); old.resolve(page([{ id: 1 }])); await flush()
  assert.equal(view.state.list[0].id, 2)
})

test('public detail reloads when the same component receives another id or resource', async t => {
  const route = routeFor('services')
  const view = await loadView('public/PublicDetailView.vue', route, {
    '../../api/public': { publicDetail: async (resource, id) => ({ data: { id, resource } }) },
  })
  t.after(view.stop); await flush()
  route.params.id = '2'; await flush()
  assert.equal(view.item.value.id, '2')
  route.meta.resource = 'announcements'; await flush()
  assert.equal(view.item.value.resource, 'announcements')
})

test('missing public detail clears previous content and supplies an error state', async t => {
  const route = routeFor('services')
  const view = await loadView('public/PublicDetailView.vue', route, {
    '../../api/public': { publicDetail: async (_, id) => {
      if (id === '2') throw new Error('内容不存在')
      return { data: { id, serviceName: '旧服务' } }
    } },
  })
  t.after(view.stop); await flush(); route.params.id = '2'; await flush()
  assert.equal(view.item.value.serviceName, undefined)
  assert.equal(view.error?.value, '内容不存在')
  assert.equal(view.loading.value, false)
})

test('contact save validates before calling the backend', async t => {
  let saves = 0
  const view = await loadView('client/ContactView.vue', routeFor('contacts'), {
    '../../api/client': { createContact: async () => { saves++ } },
  })
  t.after(view.stop)
  if (view.formRef) view.formRef.value = { clearValidate() {}, validate: async () => { throw new Error('姓名必填') } }
  await view.save()
  assert.equal(saves, 0)
})

test('double-clicking contact save sends one request and excludes ownership fields', async t => {
  const pending = deferred(); let saves = 0; let payload
  const view = await loadView('client/ContactView.vue', routeFor('contacts'), {
    '../../api/client': { createContact: async data => { saves++; payload = data; await pending.promise } },
  })
  t.after(view.stop)
  if (view.formRef) view.formRef.value = { clearValidate() {}, validate: async () => true }
  view.form.name = '张三'; view.form.clientId = 99
  const first = view.save(); await flush(); const second = view.save(); await flush()
  assert.equal(saves, 1); assert.equal(payload.clientId, undefined)
  pending.resolve(); await Promise.all([first, second])
})

test('deleting the last contact on a page moves to the preceding page', async t => {
  const view = await loadView('client/ContactView.vue', routeFor('contacts'))
  t.after(view.stop)
  Object.assign(view.state, { pageNum: 2, list: [{ id: 7, name: '张三' }], total: 11 })
  await view.remove({ id: 7, name: '张三' })
  assert.equal(view.state.pageNum, 1)
})

test('cancelling contact deletion does not call the backend or reject', async t => {
  let deletes = 0
  const view = await loadView('client/ContactView.vue', routeFor('contacts'), {
    'element-plus': { ElMessageBox: { confirm: async () => { throw 'cancel' } } },
    '../../api/client': { deleteContact: async () => { deletes++ } },
  })
  t.after(view.stop)
  await view.remove({ id: 7, name: '张三' })
  assert.equal(deletes, 0)
})

test('client profile validates and reloads persisted values after save', async t => {
  let saves = 0
  const view = await loadView('profile/ProfileView.vue', routeFor('profile'), {
    '../../api/client': { getClientProfile: async () => ({ data: { clientType: 'INDIVIDUAL', clientName: '客户' } }), saveClientProfile: async () => { saves++ } },
  })
  t.after(view.stop)
  if (view.formRef) view.formRef.value = { clearValidate() {}, validate: async () => { throw new Error('校验失败') } }
  await view.save(); assert.equal(saves, 0)
  view.formRef.value = { clearValidate() {}, validate: async () => true }
  await view.save(); assert.equal(saves, 1); assert.equal(view.form.clientName, '客户')
})

test('switching admin resources resets its search and closes stale edit dialog', async t => {
  const route = routeFor('service-products')
  const view = await loadView('admin/ContentManagerView.vue', route)
  t.after(view.stop)
  view.edit({ id: 1, serviceName: '专利' }); view.state.keyword = '专利'
  route.meta.resource = 'announcements'; await flush()
  assert.equal(view.open.value, false)
  assert.equal(view.state.keyword, '')
})

test('admin content is validated and repeated save submits only once', async t => {
  let saves = 0; const pending = deferred()
  const view = await loadView('admin/ContentManagerView.vue', routeFor('service-products'), {
    '../../api/admin': { createAdmin: async () => { saves++; await pending.promise } },
  })
  t.after(view.stop); view.edit()
  if (view.formRef) view.formRef.value = { clearValidate() {}, validate: async () => { throw new Error('必填项为空') } }
  // Resolve baseline calls so an implementation without validation cannot hang the test.
  pending.resolve(); await view.save(); assert.equal(saves, 0)
  view.formRef.value = { clearValidate() {}, validate: async () => true }
  await Promise.all([view.save(), view.save()]); assert.equal(saves, 1)
})

test('homepage still displays services when announcements cannot be loaded', async t => {
  const view = await loadView('public/HomeView.vue', routeFor('services'), {
    '../../api/public': { publicList: async resource => {
      if (resource === 'announcements') throw new Error('公告加载失败')
      return page([{ id: 1, serviceName: '专利申请' }])
    } },
  })
  t.after(view.stop)
  assert.equal(view.services.value[0].id, 1)
  assert.equal(view.announcements.value.length, 0)
})
