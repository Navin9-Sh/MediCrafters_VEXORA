import { useEffect, useState } from 'react'
import { getPatients, togglePatientStatus } from '../services/patientService'
import DataTable from '../components/common/DataTable'
import Badge from '../components/common/Badge'
import Modal from '../components/common/Modal'
import ToastContainer, { useToast } from '../components/common/Toast'
import ErrorState from '../components/common/ErrorState'
import { SearchIcon, CheckCircleIcon, XCircleIcon } from '../components/common/Icons'

export default function Patients() {
  const [patients, setPatients] = useState([])
  const [loading, setLoading]   = useState(true)
  const [error, setError]       = useState('')
  const [search, setSearch]     = useState('')
  const [selected, setSelected] = useState(null)
  const [actionType, setActionType] = useState('suspend') // 'suspend' | 'activate'
  const [statusLoading, setStatusLoading] = useState(false)
  const { toasts, show: showToast, dismiss } = useToast()

  async function fetchPatients() {
    setLoading(true)
    setError('')
    try {
      const params = { size: 50 }
      if (search) params.search = search
      const res = await getPatients(params)
      setPatients(res?.data?.content || [])
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load patient records.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchPatients() }, [search])

  async function handleToggleStatus() {
    if (!selected) return
    setStatusLoading(true)
    const newActive = actionType === 'activate'
    try {
      await togglePatientStatus(selected.id, newActive)
      showToast(
        `Patient account for ${selected.fullName || selected.email} ${newActive ? 'reactivated' : 'suspended'}.`,
        newActive ? 'success' : 'warning'
      )
      setSelected(null)
      fetchPatients()
    } catch (err) {
      showToast(err?.response?.data?.message || 'Status update failed.', 'error')
    } finally {
      setStatusLoading(false)
    }
  }

  const columns = [
    {
      key: 'fullName',
      label: 'Patient Name',
      render: (r) => (
        <div>
          <div style={{ fontWeight: 600 }}>{r.fullName || 'Unspecified'}</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>ID: {r.id?.slice(0, 8)}...</div>
        </div>
      ),
    },
    { key: 'email',       label: 'Email Address' },
    { key: 'phone',       label: 'Phone', render: (r) => r.phone || '—' },
    { key: 'createdAt',   label: 'Registration Date', render: (r) => r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—' },
    { key: 'active',      label: 'Account Status', render: (r) => <Badge status={r.active ? 'ACTIVE' : 'SUSPENDED'} /> },
    {
      key: 'actions',
      label: 'Access Control',
      render: (r) => (
        <div className="action-btns">
          {r.active ? (
            <button
              className="btn btn-danger btn-sm"
              onClick={() => { setSelected(r); setActionType('suspend') }}
              id={`suspend-patient-${r.id}`}
            >
              <XCircleIcon size={14} />
              <span>Suspend</span>
            </button>
          ) : (
            <button
              className="btn btn-success btn-sm"
              onClick={() => { setSelected(r); setActionType('activate') }}
              id={`activate-patient-${r.id}`}
            >
              <CheckCircleIcon size={14} />
              <span>Reactivate</span>
            </button>
          )}
        </div>
      ),
    },
  ]

  if (error) return <ErrorState message={error} onRetry={fetchPatients} />

  return (
    <div className="page">
      <ToastContainer toasts={toasts} dismiss={dismiss} />

      <div className="page-header">
        <div>
          <h1 className="page-title">Patient Management</h1>
          <p className="page-subtitle">Inspect registered patient profiles, contact details, and account standing</p>
        </div>
        <button className="btn btn-outline btn-sm" onClick={fetchPatients}>
          Refresh List
        </button>
      </div>

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <SearchIcon size={16} className="search-icon" />
          <input
            id="patient-search"
            type="text"
            className="form-input search-input"
            placeholder="Search patient by name, email or phone..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      <div className="card">
        <DataTable
          columns={columns}
          data={patients}
          loading={loading}
          emptyMessage="No patient accounts registered matching your criteria."
        />
      </div>

      <Modal
        isOpen={!!selected}
        title={actionType === 'suspend' ? 'Suspend Patient Access' : 'Reactivate Patient Access'}
        onClose={() => setSelected(null)}
        onConfirm={handleToggleStatus}
        confirmText={actionType === 'suspend' ? 'Confirm Suspension' : 'Confirm Reactivation'}
        confirmVariant={actionType === 'suspend' ? 'danger' : 'success'}
        loading={statusLoading}
      >
        <p>
          Are you sure you want to {actionType === 'suspend' ? 'suspend' : 'reactivate'} the account for{' '}
          <strong>{selected?.fullName || selected?.email}</strong>?
        </p>
        <p className="text-muted" style={{ marginTop: '0.5rem' }}>
          {actionType === 'suspend'
            ? 'The patient will be prevented from booking appointments and logging into the platform until restored.'
            : 'The patient will immediately be restored full consultation and platform booking privileges.'}
        </p>
      </Modal>
    </div>
  )
}
