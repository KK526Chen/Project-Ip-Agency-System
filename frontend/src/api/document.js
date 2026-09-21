import request from '../utils/request'

export const getDocuments = (params) => request.get('/documents', { params })
export const uploadDocument = (data) => request.post('/documents/upload', data, { headers: { 'Content-Type': 'multipart/form-data' } })
export const deleteDocument = (id) => request.delete(`/documents/${id}`)
export const downloadDocument = (id) => request.get(`/documents/${id}/download`, { responseType: 'blob' })
