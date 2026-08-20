import api from './api'

/**
 * Get paginated list of all doctors (admin view)
 *
 * Backend endpoint: GET /api/v1/doctors
 * Query params: search, specialty, sortBy, page, size
 */
export async function getDoctors(params = {}) {
  const response = await api.get('/api/v1/doctors', { params })
  return response.data
}

/**
 * Get a single doctor by ID
 *
 * Backend endpoint: GET /api/v1/doctors/{id}
 */
export async function getDoctorById(id) {
  const response = await api.get(`/api/v1/doctors/${id}`)
  return response.data
}

// ─── Admin-only endpoints (MOCK — Replace when backend /api/v1/admin/* is ready) ───

/**
 * Get doctors filtered by verification status
 *
 * MOCK — Requires backend: GET /api/v1/admin/doctors?status=PENDING|VERIFIED|REJECTED|SUSPENDED
 */
export async function getDoctorsByStatus(status) {
  // TODO: Replace with: api.get('/api/v1/admin/doctors', { params: { status } })
  const allDoctors = await getDoctors({ size: 100 })
  const doctors = allDoctors?.data?.content || []
  if (!status) return doctors
  return doctors.filter((d) => {
    if (status === 'VERIFIED') return d.verified === true
    if (status === 'PENDING') return d.verified === false
    return true
  })
}

/**
 * Verify a doctor
 *
 * MOCK — Requires backend: PATCH /api/v1/admin/doctors/{id}/verify
 * Request: {}
 */
export async function verifyDoctor(id) {
  // TODO: return api.patch(`/api/v1/admin/doctors/${id}/verify`)
  console.warn('[MOCK] verifyDoctor called for', id)
  return { success: true, message: 'Doctor verified (mock)' }
}

/**
 * Reject a doctor
 *
 * MOCK — Requires backend: PATCH /api/v1/admin/doctors/{id}/reject
 * Request: { reason: string }
 */
export async function rejectDoctor(id, reason) {
  // TODO: return api.patch(`/api/v1/admin/doctors/${id}/reject`, { reason })
  console.warn('[MOCK] rejectDoctor called for', id, reason)
  return { success: true, message: 'Doctor rejected (mock)' }
}

/**
 * Suspend a doctor
 *
 * MOCK — Requires backend: PATCH /api/v1/admin/doctors/{id}/suspend
 */
export async function suspendDoctor(id) {
  // TODO: return api.patch(`/api/v1/admin/doctors/${id}/suspend`)
  console.warn('[MOCK] suspendDoctor called for', id)
  return { success: true, message: 'Doctor suspended (mock)' }
}
