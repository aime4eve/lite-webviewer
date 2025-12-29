import client from './client'

export const getLatestRisk = (deviceId) => {
  return client.get(`/risk/${deviceId}/current`)
}

export const getHealthFingerprint = (deviceId) => {
  return client.get(`/health-fingerprint/${deviceId}`)
}

export const submitFeedback = (data) => {
  return client.post('/prediction-feedback', data)
}

export const getClimateConfig = (zoneCode) => {
  return client.get(`/climate-configs/${zoneCode}`)
}

export const getAllClimateConfigs = () => {
  return client.get('/climate-configs')
}

export const saveClimateConfig = (data) => {
  return client.post('/climate-configs', data)
}

export const deleteClimateConfig = (zoneCode) => {
  return client.delete(`/climate-configs/${zoneCode}`)
}
