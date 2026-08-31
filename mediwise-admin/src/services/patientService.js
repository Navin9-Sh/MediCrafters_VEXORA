import api from './api'

export async function getPatients(params = {}) {
  const response = await api.get('/api/v1/admin/users', {
    params: { role: 'PATIENT', ...params },
  })
  return response.data
}

export async function getPatientById(id) {
  const response = await api.get(`/api/v1/admin/users/${id}`)
  return response.data
}

export async function togglePatientStatus(id, active) {
  const response = await api.patch(`/api/v1/admin/users/${id}/status`, { active })
  return response.data
}
