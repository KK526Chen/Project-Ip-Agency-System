import { reactive } from 'vue'
const readUser = () => { try { return JSON.parse(sessionStorage.getItem('user') || 'null') } catch { return null } }
export const session = reactive({ token: sessionStorage.getItem('token') || '', user: readUser() })
export const setSession = (token, user) => { session.token = token; session.user = user; sessionStorage.setItem('token', token); sessionStorage.setItem('user', JSON.stringify(user)) }
export const clearSession = () => { session.token = ''; session.user = null; sessionStorage.removeItem('token'); sessionStorage.removeItem('user') }
export const homeFor = (role = session.user?.role) => role ? `/${role.toLowerCase()}/dashboard` : '/'
