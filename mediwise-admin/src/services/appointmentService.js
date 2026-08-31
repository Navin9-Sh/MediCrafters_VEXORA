import api from './api'

export async function getAppointments(params = {}) {
  const response = await api.get('/api/v1/admin/appointments', { params })
  return response.data
}

export async function getAppointmentById(id) {
  const response = await api.get(`/api/v1/appointments/${id}`)
  return response.data
}
