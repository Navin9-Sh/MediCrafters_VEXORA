import api from './api'

// ─── Admin-only endpoints (MOCK — Replace when backend /api/v1/admin/users is ready) ───

const MOCK_PATIENTS = [
  { id: '1', fullName: 'Rohan Sharma', email: 'rohan@example.com', phone: '9876543210', role: 'PATIENT', active: true, createdAt: '2026-01-10T08:00:00Z', appointmentCount: 3 },
  { id: '2', fullName: 'Priya Patel',  email: 'priya@example.com', phone: '9123456780', role: 'PATIENT', active: true, createdAt: '2026-02-14T08:00:00Z', appointmentCount: 1 },
  { id: '3', fullName: 'Arjun Mehta',  email: 'arjun@example.com', phone: '9012345678', role: 'PATIENT', active: false, createdAt: '2026-03-20T08:00:00Z', appointmentCount: 0 },
]

/**
 * Get all patients (admin view)
 *
 * MOCK — Requires backend: GET /api/v1/admin/users?role=PATIENT
 */
export async function getPatients() {
  // TODO: return api.get('/api/v1/admin/users', { params: { role: 'PATIENT' } })
  return { data: MOCK_PATIENTS }
}

/**
 * Get patient by ID
 *
 * MOCK — Requires backend: GET /api/v1/admin/users/{id}
 */
export async function getPatientById(id) {
  // TODO: return api.get(`/api/v1/admin/users/${id}`)
  const patient = MOCK_PATIENTS.find((p) => p.id === id)
  return { data: patient }
}

/**
 * Suspend a patient account
 *
 * MOCK — Requires backend: PATCH /api/v1/admin/users/{id}/suspend
 */
export async function suspendPatient(id) {
  // TODO: return api.patch(`/api/v1/admin/users/${id}/suspend`)
  console.warn('[MOCK] suspendPatient called for', id)
  return { success: true, message: 'Patient suspended (mock)' }
}
