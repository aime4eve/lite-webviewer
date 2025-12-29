import client from './client'

export const alarmApi = {
  getAlarmList(params) {
    return client.get('/alarms', { params })
  },

  getAlarmDetail(id) {
    return client.get(`/alarms/${id}`)
  },

  handleAlarm(id, data) {
    return client.put(`/alarms/${id}/handle`, data)
  },

  getAlarmStatistics(params) {
    return client.get('/alarms/statistics', { params })
  },

  getOperatorAlarmList(params) {
    return client.get('/operator/alarms', { params })
  },

  handleOperatorAlarm(id, data) {
    return client.put(`/operator/alarms/${id}/handle`, data)
  }
}

export const workOrderApi = {
  getWorkOrderList(params) {
    return client.get('/work-orders', { params })
  },

  getWorkOrderDetail(id) {
    return client.get(`/work-orders/${id}`)
  },

  createWorkOrder(data) {
    return client.post('/work-orders', data)
  },

  updateWorkOrder(id, data) {
    return client.put(`/work-orders/${id}`, data)
  },

  confirmReceipt(id) {
    return client.put(`/work-orders/${id}/confirm-receipt`)
  }
}

export const diagnoseApi = {
  startDiagnose(deviceId) {
    return client.post(`/operator/devices/${deviceId}/diagnose`)
  },

  getDiagnoseResult(diagnoseId) {
    return client.get(`/operator/diagnose/${diagnoseId}`)
  }
}
