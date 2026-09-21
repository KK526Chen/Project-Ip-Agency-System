import request from '../utils/request'

export const getOverview = () => request.get('/overview')
export const getMyTasks = (params) => request.get('/overview/my-tasks', { params })
export const getUpcomingDeadlines = (params) => request.get('/overview/deadlines', { params })
