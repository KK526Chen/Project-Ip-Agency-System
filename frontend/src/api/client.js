import request from '../utils/request'
export const getClientProfile = () => request.get('/client/profile')
export const saveClientProfile = (data) => request.put('/client/profile', data)
export const listContacts = (params) => request.get('/client/contacts', { params })
export const createContact = (data) => request.post('/client/contacts', data)
export const updateContact = (id, data) => request.put(`/client/contacts/${id}`, data)
export const deleteContact = (id) => request.delete(`/client/contacts/${id}`)
