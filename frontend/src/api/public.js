import request from '../utils/request'
export const publicList = (resource, params) => request.get(`/public/${resource}`, { params })
export const publicDetail = (resource, id) => request.get(`/public/${resource}/${id}`)
