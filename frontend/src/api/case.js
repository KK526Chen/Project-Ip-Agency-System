import request from '../utils/request'

export const getCases = (params) => request.get('/cases', { params })
export const getCase = (id) => request.get(`/cases/${id}`)
export const createCase = (data) => request.post('/cases', data)
export const updateCase = (id, data) => request.put(`/cases/${id}`, data)
export const deleteCase = (id) => request.delete(`/cases/${id}`)
export const getCaseMembers = (caseId) => request.get(`/cases/${caseId}/members`)
export const addCaseMember = (caseId, data) => request.post(`/cases/${caseId}/members`, data)
export const removeCaseMember = (caseId, id) => request.delete(`/cases/${caseId}/members/${id}`)
