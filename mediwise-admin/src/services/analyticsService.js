import api from './api'

export async function getAdminStats() {
  const response = await api.get('/api/v1/admin/stats')
  return response.data
}

export async function getDashboardAnalytics() {
  const response = await api.get('/api/v1/analytics/dashboard')
  return response.data
}
