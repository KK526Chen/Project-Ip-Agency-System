import request from '../utils/request'

export const getClients = (params) => request.get('/clients', { params })
export const getClientSelector = (params) => request.get('/clients/selector', { params })
export const getClient = (id) => request.get(`/clients/${id}`)
export const createClient = (data) => request.post('/clients', data)
export const updateClient = (id, data) => request.put(`/clients/${id}`, data)
export const deleteClient = (id) => request.delete(`/clients/${id}`)
