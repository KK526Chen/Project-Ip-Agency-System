import axios from 'axios'
import { ElMessage } from 'element-plus'
const request = axios.create({ baseURL: '/api', timeout: 20000 })
request.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
request.interceptors.response.use(
  (response) => response.config.responseType === 'blob' ? response : response.data,
  async (error) => {
    const data = error.response?.data
    if (data instanceof Blob) {
      try { error.response.data = JSON.parse(await data.text()) } catch { /* keep blob */ }
    }
    const status = error.response?.status
    if (status === 401) {
      sessionStorage.removeItem('token'); sessionStorage.removeItem('user')
      if (window.location.pathname !== '/login') window.location.assign(`/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`)
    } else if (status === 403) ElMessage.error(error.response?.data?.message || '无权执行此操作')
    return Promise.reject(error)
  },
)
export const messageOf = (error, fallback = '操作失败') => {
  const data = error.response?.data
  if (data && typeof data === 'object' && typeof data.message === 'string') return data.message
  return error.message || fallback
}
export default request
