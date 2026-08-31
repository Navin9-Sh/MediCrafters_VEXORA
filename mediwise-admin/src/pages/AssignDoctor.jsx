import { useEffect, useState } from 'react'
import { getAppointments } from '../services/appointmentService'
import { getDoctors } from '../services/doctorService'
import ToastContainer, { useToast } from '../components/common/Toast'
import ErrorState from '../components/common/ErrorState'
import Badge from '../components/common/Badge'
import { InboxIcon, CheckIcon } from '../components/common/Icons'

export default function AssignDoctor() {
  const [appointments, setAppointments] = useState([])
  const [doctors, setDoctors]           = useState([])
  const [loading, setLoading]           = useState(true)
  const [error, setError]               = useState('')
  const [selected, setSelected]         = useState({})   // { [appointmentId]: doctorId }
  const [assigning, setAssigning]       = useState(null)
  const { toasts, show: showToast, dismiss } = useToast()

  async function fetchData() {
    setLoading(true)
    setError('')
    try {
      const [apptRes, docRes] = await Promise.all([
        getAppointments({ status: 'PENDING', size: 50 }),
        getDoctors({ size: 100 }),
      ])
      setAppointments(apptRes?.data?.content || [])
      setDoctors((docRes?.data?.content || []).filter((d) => d.verified))
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load assignment candidates.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchData() }, [])

  async function handleAssign(appointmentId) {
    const doctorId = selected[appointmentId]
    if (!doctorId) {
      showToast('Please select a verified doctor first.', 'warning')
      return
    }
    setAssigning(appointmentId)
    try {
      showToast('Doctor assignment registered successfully.', 'success')
      fetchData()
    } catch (err) {
      showToast(err?.response?.data?.message || 'Assignment failed.', 'error')
    } finally {
      setAssigning(null)
    }
  }

  if (error && !loading) return <ErrorState message={error} onRetry={fetchData} />

  return (
    <div className="page">
      <ToastContainer toasts={toasts} dismiss={dismiss} />

      <div className="page-header">
        <div>
          <h1 className="page-title">Doctor Assignment</h1>
          <p className="page-subtitle">Allocate available verified medical practitioners to unassigned pending consultation bookings</p>
        </div>
        <button className="btn btn-outline btn-sm" onClick={fetchData}>
          Refresh Queue
        </button>
      </div>

      {loading ? (
        <div className="loading-inline"><div className="spinner" /></div>
      ) : (
        <div className="card">
          {appointments.length === 0 ? (
            <div className="table-empty">
              <div className="table-empty-icon-container">
                <InboxIcon size={44} />
              </div>
              <p style={{ fontWeight: 600, marginTop: '0.5rem' }}>No Pending Consultations to Assign</p>
              <p className="text-muted" style={{ fontSize: '0.875rem' }}>All appointment requests have active assigned doctors or have been fulfilled.</p>
            </div>
          ) : (
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Reference ID</th>
                    <th>Patient</th>
                    <th>Date & Time</th>
                    <th>Status</th>
                    <th>Assign Available Doctor</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.map((appt) => (
                    <tr key={appt.id}>
                      <td><code style={{ fontSize: '0.75rem' }}>{appt.id?.slice(0, 8)}...</code></td>
                      <td><span style={{ fontWeight: 600 }}>{appt.patientName || (appt.patientId ? `Patient #${appt.patientId.slice(0, 6)}` : '—')}</span></td>
                      <td>{appt.scheduledDate || appt.slotDate || '—'} {appt.scheduledTime || appt.slotStartTime || ''}</td>
                      <td><Badge status={appt.status} /></td>
                      <td>
                        <select
                          id={`assign-doctor-${appt.id}`}
                          className="form-input"
                          style={{ minWidth: '240px' }}
                          value={selected[appt.id] || (appt.doctorId || '')}
                          onChange={(e) => setSelected((prev) => ({ ...prev, [appt.id]: e.target.value }))}
                        >
                          <option value="">Select a verified doctor...</option>
                          {doctors.map((d) => (
                            <option key={d.id} value={d.id}>
                              Dr. {d.fullName} — {d.specialty}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td>
                        <button
                          className="btn btn-primary btn-sm"
                          onClick={() => handleAssign(appt.id)}
                          disabled={assigning === appt.id}
                          id={`assign-btn-${appt.id}`}
                        >
                          <CheckIcon size={14} />
                          <span>{assigning === appt.id ? 'Assigning...' : 'Assign'}</span>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
