import client from './client'

export const getDailyReport = (date) => {
  return client.post(`/reports/generate/daily?date=${date}`)
}

export const exportReportPdf = (date) => {
  return client.get(`/reports/daily/${date}/export/pdf`, {
    responseType: 'blob'
  })
}

export const getDailyReports = () => {
  return client.get('/reports/daily')
}

export const getDashboardOverview = () => {
  return client.get('/reports/dashboard/overview')
}
