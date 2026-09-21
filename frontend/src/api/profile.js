import request from '../utils/request'
export const getAgentProfile = () => request.get('/agent/profile')
export const saveAgentProfile = (data) => request.put('/agent/profile', data)
