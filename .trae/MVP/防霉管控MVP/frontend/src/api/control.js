import client from './client'

export const controlApi = {
  // 获取风险概览
  getRiskOverview() {
    return client.get('/control/risk/overview')
  },
  
  // 获取高风险房间
  getHighRiskRooms(params) {
    return client.get('/control/risk/high-risk-rooms', { params })
  },
  
  // 创建策略
  createStrategy(data) {
    return client.post('/control/strategies', data)
  },
  
  // 更新策略
  updateStrategy(id, data) {
    return client.put(`/control/strategies/${id}`, data)
  },
  
  // 获取策略详情
  getStrategyDetail(id) {
    return client.get(`/control/strategies/${id}`)
  },

  // 获取空间列表
  getSpaceList(params) {
    return client.get('/control/spaces', { params })
  },

  // 创建空间
  createSpace(data) {
    return client.post('/control/spaces', data)
  },

  // 删除空间
  deleteSpace(id) {
    return client.delete(`/control/spaces/${id}`)
  },

  // 关联设备
  associateDevice(spaceId, deviceId) {
    return client.post(`/control/spaces/${spaceId}/devices/${deviceId}`)
  },

  // 移除设备关联
  removeDeviceAssociation(spaceId, deviceId) {
    return client.delete(`/control/spaces/${spaceId}/devices/${deviceId}`)
  },

  // Operational Control
  getActivePlans() {
    return client.get('/plans/active')
  },
  
  sendCommand(data) {
    return client.post('/control/commands/send', data)
  },
  
  getCommandHistory(deviceId) {
    return client.get(`/control/commands/history/${deviceId}`)
  }
}
