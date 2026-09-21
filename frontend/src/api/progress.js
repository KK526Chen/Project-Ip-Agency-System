import request from '../utils/request'

export const getProgress = (params) => request.get('/progress', { params })
export const createProgress = (data) => request.post('/progress', data)
export const deleteProgress = (id) => request.delete(`/progress/${id}`)
