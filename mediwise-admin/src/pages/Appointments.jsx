import { useEffect, useState } from 'react'
import { getAppointments } from '../services/appointmentService'
import DataTable from '../components/common/DataTable'
import Badge from '../components/common/Badge'
import ErrorState from '../components/common/ErrorState'
import { APPOINTMENT_STATUSES } from '../utils/constants'

const STATUS_OPTIONS = ['', ...Object.values(APPOINTMENT_STATUSES)]

export default function Appointments() {
  const [appointments, setAppointments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')
  const [status, setStatus]   = useState('')

  async function fetchAppointments() {
    setLoading(true)
    setError('')
    try {
      const params = { size: 50 }
      if (status) params.status = status
      const res = await getAppointments(params)
      setAppointments(res?.data?.content || [])
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load appointments.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchAppointments() }, [status])

  const columns = [
    { key: 'id',            label: 'Reference ID', render: (r) => <code style={{ fontSize: '0.75rem', background: '#f8fafc', padding: '2px 6px', borderRadius: '4px' }}>{r.id?.slice(0, 8)}...</code> },
    { key: 'patientName',   label: 'Patient', render: (r) => <span style={{ fontWeight: 600 }}>{r.patientName || (r.patientId ? `Patient #${r.patientId.slice(0, 6)}` : '—')}</span> },
    { key: 'doctorName',    label: 'Assigned Doctor', render: (r) => r.doctorName ? `Dr. ${r.doctorName}` : (r.doctorId ? `Dr. #${r.doctorId.slice(0, 6)}` : 'Unassigned') },
    { key: 'scheduledDate', label: 'Consultation Date', render: (r) => r.scheduledDate || r.slotDate || '—' },
    { key: 'scheduledTime', label: 'Time Slot', render: (r) => r.scheduledTime || r.slotStartTime || '—' },
    {
      key: 'type',
      label: 'Channel',
      render: (r) => (
        <span className="badge badge--blue-light">
          {r.type || 'VIDEO_CALL'}
        </span>
      ),
    },
    { key: 'status',        label: 'Status', render: (r) => <Badge status={r.status} /> },
  ]

  if (error) return <ErrorState message={error} onRetry={fetchAppointments} />

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Appointment Oversight</h1>
          <p className="page-subtitle">Monitor patient consultation bookings, schedules, and fulfillment statuses across the platform</p>
        </div>
        <button className="btn btn-outline btn-sm" onClick={fetchAppointments}>
          Refresh Appointments
        </button>
      </div>

      <div className="filter-bar">
        <select
          id="appointment-status-filter"
          className="form-input"
          style={{ maxWidth: '240px' }}
          value={status}
          onChange={(e) => setStatus(e.target.value)}
        >
          {STATUS_OPTIONS.map((s) => (
            <option key={s} value={s}>{s ? `Status: ${s}` : 'All Consultation Statuses'}</option>
          ))}
        </select>
      </div>

      <div className="card">
        <DataTable
          columns={columns}
          data={appointments}
          loading={loading}
          emptyMessage="No consultation bookings found for the selected filter."
        />
      </div>
    </div>
  )
}
