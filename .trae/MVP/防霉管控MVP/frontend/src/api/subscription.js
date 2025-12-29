import client from './client'

export const getPoints = (userId) => {
  return client.get(`/subscriptions/${userId}/points`)
}

export const getPointsHistory = (userId) => {
  return client.get(`/subscriptions/${userId}/points/history`)
}

export const subscribe = (data) => {
  return client.post('/subscriptions/subscribe', data)
}

export const getActiveSubscription = (userId) => {
  return client.get(`/subscriptions/${userId}/active`)
}
