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
    { key: 'id',           label: 'ID',      render: (r) => r.id?.slice(0, 8) + '...' },
    { key: 'patientId',    label: 'Patient', render: (r) => r.patientId?.slice(0, 8) + '...' },
    { key: 'doctorId',     label: 'Doctor',  render: (r) => r.doctorId?.slice(0, 8) + '...'  },
    { key: 'slotDate',     label: 'Date' },
    { key: 'slotStartTime',label: 'Time' },
    { key: 'type',         label: 'Type'   },
    { key: 'status',       label: 'Status', render: (r) => <Badge status={r.status} /> },
  ]

  if (error) return <ErrorState message={error} onRetry={fetchAppointments} />

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Appointments</h1>
        <p className="page-subtitle">View and manage all patient appointments</p>
      </div>

      <div className="filter-bar">
        <select
          id="appointment-status-filter"
          className="form-input"
          style={{ maxWidth: '200px' }}
          value={status}
          onChange={(e) => setStatus(e.target.value)}
        >
          {STATUS_OPTIONS.map((s) => (
            <option key={s} value={s}>{s || 'All Statuses'}</option>
          ))}
        </select>
      </div>

      <div className="card">
        <DataTable
          columns={columns}
          data={appointments}
          loading={loading}
          emptyMessage="No appointments found."
        />
      </div>
    </div>
  )
}
