import request from '../utils/request'
export const listAdmin = (resource, params) => request.get(`/admin/${resource}`, { params })
export const getAdmin = (resource, id) => request.get(`/admin/${resource}/${id}`)
export const createAdmin = (resource, data) => request.post(`/admin/${resource}`, data)
export const updateAdmin = (resource, id, data) => request.put(`/admin/${resource}/${id}`, data)
export const deleteAdmin = (resource, id) => request.delete(`/admin/${resource}/${id}`)
export const dashboard = (role) => request.get(`/${role.toLowerCase()}/dashboard`)
export const statistics = (role) => request.get(`/${role.toLowerCase()}/${role === 'ADMIN' ? 'statistics' : 'performance'}`)
