import request from '../utils/request'
export const listBills = (path, params) => request.get(path, { params })
export const createBill = (data) => request.post('/admin/bills', data)
export const updateBill = (id, data) => request.put(`/admin/bills/${id}`, data)
export const confirmBill = (id) => request.post(`/client/bills/${id}/confirm`)
export const payBill = (id) => request.post(`/client/bills/${id}/pay`)
export const issueInvoice = (id, data = {}) => request.post(`/admin/bills/${id}/invoice`, data)
export const listInvoices = (path, params) => request.get(path, { params })
