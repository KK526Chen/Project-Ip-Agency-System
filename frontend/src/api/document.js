import request from '../utils/request'
export const listDocuments = (params) => request.get('/documents', { params })
export const uploadDocument = (data) => request.post('/documents/upload', data)
export const downloadDocument = (id) => request.get(`/documents/${id}/download`, { responseType: 'blob' })
export const reviewDocument = (data) => request.post('/reviews', data)
export const confirmOcr = (id, data) => request.post(`/documents/${id}/ocr`, data)
