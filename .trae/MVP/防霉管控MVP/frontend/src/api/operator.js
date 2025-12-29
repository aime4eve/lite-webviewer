import client from './client'

export const operatorApi = {
  getDashboardOverview() {
    return client.get('/operator/dashboard/overview')
  },

  getDeviceStatus() {
    return client.get('/operator/dashboard/device-status')
  },

  getAlarmList(params) {
    return client.get('/operator/alarms', { params })
  },

  handleAlarm(id, data) {
    return client.put(`/operator/alarms/${id}/handle`, data)
  },

  getUserList(params) {
    return client.get('/operator/users', { params })
  },

  diagnoseDevice(id) {
    return client.post(`/operator/devices/${id}/diagnose`)
  },

  getDiagnoseResult(diagnoseId) {
    return client.get(`/operator/diagnose/${diagnoseId}`)
  }
}
