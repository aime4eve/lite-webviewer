import client from './client'

export const deviceApi = {
  getDeviceList(params) {
    return client.get('/devices', { params })
  },

  getDeviceDetail(id) {
    return client.get(`/devices/${id}`)
  },

  addDevice(data) {
    return client.post('/devices/register', data)
  },

  updateDevice(id, data) {
    return client.put(`/devices/${id}`, data)
  },

  deleteDevice(id) {
    return client.delete(`/devices/${id}`)
  },

  getEnvironmentData(id) {
    return client.get(`/devices/${id}/environment`)
  },

  getRiskPrediction(id, hours = 3) {
    return client.get(`/devices/${id}/risk-prediction`, { params: { hours } })
  },

  getAutoMoldStrategy(id) {
    return client.get(`/devices/${id}/auto-mold-strategy`)
  },

  updateAutoMoldStrategy(id, data) {
    return client.put(`/devices/${id}/auto-mold-strategy`, data)
  },

  getLinkageMapping(id) {
    return client.get(`/devices/${id}/linkage-mapping`)
  },

  updateLinkageMapping(id, data) {
    return client.put(`/devices/${id}/linkage-mapping`, data)
  },

  submitFeedback(id, data) {
    return client.post(`/devices/${id}/feedback`, data)
  },

  // Provisioning
  getProvisioningInfo() {
    return client.get('/provisioning/info')
  },
  exchangeKeys(data) {
    return client.post('/provisioning/exchange-keys', data)
  },

  // Work Orders
  createWorkOrder(data) {
    return client.post('/work-orders', data)
  },
  assignWorkOrder(id, assignee) {
    return client.post(`/work-orders/${id}/assign`, null, { params: { assignee } })
  },
  getAssignedWorkOrders(assignee) {
    return client.get('/work-orders/assigned', { params: { assignee } })
  },
  batchAssignWorkOrders(data) {
    return client.post('/work-orders/batch-assign', data)
  },

  // Diagnostics
  getDeviceLogs(deviceId) {
    return client.get('/diagnostics/logs', { params: { deviceId } })
  },
  sendDiagnosticCommand(deviceId, command) {
    return client.post('/diagnostics/command', command, { 
      params: { deviceId },
      headers: { 'Content-Type': 'text/plain' }
    })
  },

  // Alarms
  getAlarms(deviceId) {
    return client.get('/alarms', { params: { deviceId } })
  },
  confirmAlarm(id) {
    return client.post(`/alarms/${id}/confirm`)
  },
  clearAlarm(id) {
    return client.post(`/alarms/${id}/clear`)
  }
}
