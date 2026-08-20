import { useEffect, useState } from 'react'
import { getPatients, suspendPatient } from '../services/patientService'
import DataTable from '../components/common/DataTable'
import Badge from '../components/common/Badge'
import Modal from '../components/common/Modal'
import ToastContainer, { useToast } from '../components/common/Toast'
import ErrorState from '../components/common/ErrorState'

export default function Patients() {
  const [patients, setPatients] = useState([])
  const [loading, setLoading]   = useState(true)
  const [error, setError]       = useState('')
  const [selected, setSelected] = useState(null)
  const [suspendLoading, setSuspendLoading] = useState(false)
  const { toasts, show: showToast, dismiss } = useToast()

  async function fetchPatients() {
    setLoading(true)
    setError('')
    try {
      const res = await getPatients()
      setPatients(res?.data || [])
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load patients.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchPatients() }, [])

  async function handleSuspend() {
    if (!selected) return
    setSuspendLoading(true)
    try {
      await suspendPatient(selected.id)
      showToast(`${selected.fullName} has been suspended.`, 'success')
      setSelected(null)
      fetchPatients()
    } catch (err) {
      showToast(err?.response?.data?.message || 'Suspend failed.', 'error')
    } finally {
      setSuspendLoading(false)
    }
  }

  const columns = [
    { key: 'fullName',    label: 'Name' },
    { key: 'email',       label: 'Email' },
    { key: 'phone',       label: 'Phone' },
    { key: 'createdAt',   label: 'Registered', render: (r) => r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—' },
    { key: 'active',      label: 'Status',     render: (r) => <Badge status={r.active} /> },
    { key: 'appointmentCount', label: 'Appointments' },
    {
      key: 'actions',
      label: 'Actions',
      render: (r) => (
        <button
          className="btn btn-danger btn-sm"
          onClick={() => setSelected(r)}
          disabled={!r.active}
          id={`suspend-patient-${r.id}`}
        >
          Suspend
        </button>
      ),
    },
  ]

  if (error) return <ErrorState message={error} onRetry={fetchPatients} />

  return (
    <div className="page">
      <ToastContainer toasts={toasts} dismiss={dismiss} />

      <div className="page-header">
        <h1 className="page-title">Patients</h1>
        <p className="page-subtitle">Manage patient accounts on the platform</p>
      </div>

      <div className="card">
        <DataTable
          columns={columns}
          data={patients}
          loading={loading}
          emptyMessage="No patients found."
        />
      </div>

      <Modal
        isOpen={!!selected}
        title="Suspend Patient"
        onClose={() => setSelected(null)}
        onConfirm={handleSuspend}
        confirmText="Suspend"
        confirmVariant="danger"
        loading={suspendLoading}
      >
        <p>Are you sure you want to suspend <strong>{selected?.fullName}</strong>?</p>
        <p className="text-muted">They will lose access to the MediWise app.</p>
      </Modal>
    </div>
  )
}
