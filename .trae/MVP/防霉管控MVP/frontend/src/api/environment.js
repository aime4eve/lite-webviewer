import client from './client'

export const environmentApi = {
  getRealtimeEnvironment(deviceId) {
    return client.get(`/devices/${deviceId}/environment`)
  },

  getLatestEnvironmentData(deviceId) {
    return client.get(`/devices/${deviceId}/environment/latest`)
  },

  getEnvironmentHistory(deviceId, params) {
    return client.get(`/devices/${deviceId}/environment/history`, { params })
  },

  getRiskPrediction(deviceId, hours = 3) {
    return client.get(`/devices/${deviceId}/risk-prediction`, { params: { hours } })
  },

  submitPredictionFeedback(data) {
    return client.post('/prediction-feedback', data)
  }
}
