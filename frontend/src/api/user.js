import request from '../utils/request'

export const getUsers = (params) => request.get('/users', { params })
export const getUserSelector = (params) => request.get('/users/selector', { params })
export const createUser = (data) => request.post('/users', data)
export const updateUser = (id, data) => request.put(`/users/${id}`, data)
export const updateUserStatus = (id, data) => request.put(`/users/${id}/status`, data)
