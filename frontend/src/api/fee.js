import request from '../utils/request'

export const getFees = (params) => request.get('/fees', { params })
export const createFee = (data) => request.post('/fees', data)
export const updateFee = (id, data) => request.put(`/fees/${id}`, data)
export const deleteFee = (id) => request.delete(`/fees/${id}`)
