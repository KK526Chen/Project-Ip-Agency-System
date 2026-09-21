import request from '../utils/request'
export const listSystems = (params) => request.get('/external-sync/systems', { params })
export const listSyncTasks = (params) => request.get('/external-sync/tasks', { params })
export const createSyncTask = (data) => request.post('/external-sync/tasks', data)
export const runSyncTask = (id, retry = false) => request.post(`/external-sync/tasks/${id}/${retry ? 'retry' : 'execute'}`)
