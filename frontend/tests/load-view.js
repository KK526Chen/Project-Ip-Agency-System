import { readFile } from 'node:fs/promises'
import { SourceTextModule, SyntheticModule, createContext } from 'node:vm'
import { compileScript, parse } from '@vue/compiler-sfc'
import * as vue from 'vue'
import * as enums from '../src/utils/enums.js'

// Execute the actual SFC setup with real Vue reactivity and replace only external I/O.
export async function loadView(file, route, overrides = {}) {
  const mounted = []; const unmounted = []; const messages = []
  const modules = {
    vue: { ...vue, onMounted: fn => mounted.push(fn), onUnmounted: fn => unmounted.push(fn) },
    'vue-router': { useRoute: () => route, useRouter: () => ({ back() {}, push() {} }) },
    'element-plus': { ElMessage: { success: text => messages.push(text), error: text => messages.push(text) }, ElMessageBox: { confirm: async () => {} } },
    '../../utils/enums': enums,
    '../../utils/request': { messageOf: error => error.response?.data?.message || error.message },
    '../../utils/session': { session: { user: { role: 'CLIENT' } } },
    '../../api/public': { publicList: async () => ({ data: { list: [], total: 0 } }), publicDetail: async () => ({ data: {} }) },
    '../../api/admin': { listAdmin: async () => ({ data: { list: [], total: 0 } }), createAdmin: async () => {}, updateAdmin: async () => {}, deleteAdmin: async () => {} },
    '../../api/client': { getClientProfile: async () => ({ data: {} }), saveClientProfile: async () => {}, listContacts: async () => ({ data: { list: [], total: 0 } }), createContact: async () => {}, updateContact: async () => {}, deleteContact: async () => {} },
    '../../api/auth': { getProfile: async () => ({ data: {} }), changePassword: async () => {} },
    '../../api/profile': { getAgentProfile: async () => ({ data: {} }), saveAgentProfile: async () => {} },
    '@element-plus/icons-vue': { ArrowLeft: {}, ArrowRight: {}, Medal: {}, Files: {}, Timer: {} },
  }
  for (const [name, exports] of Object.entries(overrides)) modules[name] = { ...modules[name], ...exports }
  const { descriptor } = parse(await readFile(new URL(`../src/views/${file}`, import.meta.url), 'utf8'))
  const context = createContext({ console })
  const module = new SourceTextModule(compileScript(descriptor, { id: file }).content, { context })
  await module.link(name => {
    const exports = name.endsWith('.vue') ? { default: {} } : modules[name]
    if (!exports) throw new Error(`Missing dependency: ${name}`)
    return new SyntheticModule(Object.keys(exports), function () {
      for (const [key, value] of Object.entries(exports)) this.setExport(key, value)
    }, { context })
  })
  await module.evaluate()
  const scope = vue.effectScope()
  const bindings = scope.run(() => module.namespace.default.setup({}, { expose() {} }))
  await Promise.all(mounted.map(fn => fn()))
  return { ...bindings, messages, stop: () => { unmounted.forEach(fn => fn()); scope.stop() } }
}

export const flush = async () => {
  await vue.nextTick()
  await new Promise(resolve => setImmediate(resolve))
}
