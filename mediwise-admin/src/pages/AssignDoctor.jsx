import { useEffect, useState } from 'react'
import { getAppointments, assignDoctor } from '../services/appointmentService'
import { getDoctors } from '../services/doctorService'
import ToastContainer, { useToast } from '../components/common/Toast'
import ErrorState from '../components/common/ErrorState'
import Badge from '../components/common/Badge'

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
      setDoctors((docRes?.data?.content || []).filter((d) => d.verified && d.available))
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load data.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchData() }, [])

  async function handleAssign(appointmentId) {
    const doctorId = selected[appointmentId]
    if (!doctorId) {
      showToast('Please select a doctor first.', 'error')
      return
    }
    setAssigning(appointmentId)
    try {
      await assignDoctor(appointmentId, doctorId)
      showToast('Doctor assigned successfully! (mock)', 'success')
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
        <h1 className="page-title">Assign Doctor</h1>
        <p className="page-subtitle">Assign or reassign doctors to pending appointments</p>
      </div>

      {loading ? (
        <div className="loading-inline"><div className="spinner" /></div>
      ) : (
        <div className="card">
          {appointments.length === 0 ? (
            <div className="table-empty">
              <span className="table-empty-icon">📭</span>
              <p>No pending appointments to assign.</p>
            </div>
          ) : (
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Appointment ID</th>
                    <th>Patient ID</th>
                    <th>Date</th>
                    <th>Status</th>
                    <th>Assign Doctor</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.map((appt) => (
                    <tr key={appt.id}>
                      <td>{appt.id?.slice(0, 8)}...</td>
                      <td>{appt.patientId?.slice(0, 8)}...</td>
                      <td>{appt.slotDate || '—'}</td>
                      <td><Badge status={appt.status} /></td>
                      <td>
                        <select
                          id={`assign-doctor-${appt.id}`}
                          className="form-input"
                          style={{ minWidth: '200px' }}
                          value={selected[appt.id] || ''}
                          onChange={(e) => setSelected((prev) => ({ ...prev, [appt.id]: e.target.value }))}
                        >
                          <option value="">Select a doctor...</option>
                          {doctors.map((d) => (
                            <option key={d.id} value={d.id}>
                              {d.fullName} — {d.specialty}
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
                          {assigning === appt.id ? 'Assigning...' : 'Assign'}
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
