import request from '../utils/request'

export const getDeadlines = (params) => request.get('/deadlines', { params })
export const createDeadline = (data) => request.post('/deadlines', data)
export const updateDeadline = (id, data) => request.put(`/deadlines/${id}`, data)
export const deleteDeadline = (id) => request.delete(`/deadlines/${id}`)
