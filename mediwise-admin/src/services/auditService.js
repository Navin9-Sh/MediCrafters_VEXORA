// ─── Audit Log Service (MOCK — Replace when backend is ready) ────────────────

const MOCK_LOGS = [
  { id: '1', actorId: 'admin-001', actorRole: 'ADMIN', action: 'DOCTOR_VERIFIED',     resourceType: 'Doctor',      resourceId: 'doc-1', outcome: 'SUCCESS', timestamp: '2026-08-19T10:30:00Z' },
  { id: '2', actorId: 'admin-001', actorRole: 'ADMIN', action: 'USER_SUSPENDED',      resourceType: 'User',        resourceId: 'usr-2', outcome: 'SUCCESS', timestamp: '2026-08-19T11:15:00Z' },
  { id: '3', actorId: 'admin-001', actorRole: 'ADMIN', action: 'APPOINTMENT_ASSIGNED',resourceType: 'Appointment', resourceId: 'apt-3', outcome: 'SUCCESS', timestamp: '2026-08-19T14:00:00Z' },
  { id: '4', actorId: 'admin-001', actorRole: 'ADMIN', action: 'DOCTOR_REJECTED',     resourceType: 'Doctor',      resourceId: 'doc-4', outcome: 'SUCCESS', timestamp: '2026-08-20T09:00:00Z' },
]

/**
 * Get audit logs (admin only)
 *
 * MOCK — Requires backend: GET /api/v1/admin/audit-logs
 * Query params: page, size, actorId, action, from, to
 */
export async function getAuditLogs() {
  // TODO: return api.get('/api/v1/admin/audit-logs')
  return { data: MOCK_LOGS }
}
