import request from '../utils/request'
export const listDeadlines = (params) => request.get('/deadlines', { params })
export const createDeadline = (data) => request.post('/deadlines', data)
export const updateDeadline = (id, data) => request.put(`/deadlines/${id}`, data)
export const completeDeadline = (id) => request.post(`/deadlines/${id}/complete`)
