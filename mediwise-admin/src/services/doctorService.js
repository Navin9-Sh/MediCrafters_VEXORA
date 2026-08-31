import api from './api'

export async function getDoctors(params = {}) {
  const response = await api.get('/api/v1/admin/doctors', { params })
  return response.data
}

export async function getPendingDoctors(params = {}) {
  const response = await api.get('/api/v1/admin/doctors', {
    params: { verified: false, ...params },
  })
  return response.data
}

export async function getDoctorById(id) {
  const response = await api.get(`/api/v1/doctors/${id}`)
  return response.data
}

export async function verifyDoctor(id) {
  const response = await api.patch(`/api/v1/admin/doctors/${id}/verify`)
  return response.data
}

export async function rejectDoctor(id, reason) {
  const response = await api.patch(`/api/v1/admin/doctors/${id}/reject`, { reason })
  return response.data
}

export async function updateUserStatus(userId, active) {
  const response = await api.patch(`/api/v1/admin/users/${userId}/status`, { active })
  return response.data
}
