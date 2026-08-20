import api from './api'

/**
 * Get all appointments (admin view, all users)
 *
 * Backend endpoint: GET /api/v1/appointments
 * Query params: status, page, size
 *
 * NOTE: The existing /api/v1/appointments filters by the logged-in user.
 * For admin "all appointments" view, a new endpoint may be needed:
 * GET /api/v1/admin/appointments
 * Until then, this uses the existing endpoint.
 */
export async function getAppointments(params = {}) {
  const response = await api.get('/api/v1/appointments', { params })
  return response.data
}

/**
 * Get appointment by ID
 *
 * Backend endpoint: GET /api/v1/appointments/{id}
 */
export async function getAppointmentById(id) {
  const response = await api.get(`/api/v1/appointments/${id}`)
  return response.data
}

/**
 * Assign/reassign a doctor to an appointment
 *
 * MOCK — Requires backend: POST /api/v1/admin/appointments/{id}/assign
 * Request: { doctorId: string }
 */
export async function assignDoctor(appointmentId, doctorId) {
  // TODO: return api.post(`/api/v1/admin/appointments/${appointmentId}/assign`, { doctorId })
  console.warn('[MOCK] assignDoctor called:', appointmentId, doctorId)
  return { success: true, message: 'Doctor assigned (mock)' }
}
