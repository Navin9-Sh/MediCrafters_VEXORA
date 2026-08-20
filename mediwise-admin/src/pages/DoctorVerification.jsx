import { useEffect, useState } from 'react'
import { getDoctors } from '../services/doctorService'
import { verifyDoctor, rejectDoctor, suspendDoctor } from '../services/doctorService'
import DataTable from '../components/common/DataTable'
import Badge from '../components/common/Badge'
import Modal from '../components/common/Modal'
import ToastContainer, { useToast } from '../components/common/Toast'
import ErrorState from '../components/common/ErrorState'

export default function DoctorVerification() {
  const [doctors, setDoctors]         = useState([])
  const [loading, setLoading]         = useState(true)
  const [error, setError]             = useState('')
  const [actionLoading, setActLoading]= useState(false)
  const [selected, setSelected]       = useState(null)
  const [modal, setModal]             = useState(null) // 'verify' | 'reject' | 'suspend'
  const [rejectReason, setRejectReason] = useState('')
  const { toasts, show: showToast, dismiss } = useToast()

  async function fetchDoctors() {
    setLoading(true)
    setError('')
    try {
      const res = await getDoctors({ size: 100 })
      // Filter to only pending/unverified doctors
      setDoctors((res?.data?.content || []).filter((d) => !d.verified))
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load doctors.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchDoctors() }, [])

  function openModal(doctor, type) {
    setSelected(doctor)
    setModal(type)
    setRejectReason('')
  }

  function closeModal() {
    setModal(null)
    setSelected(null)
  }

  async function handleConfirm() {
    if (!selected) return
    setActLoading(true)
    try {
      if (modal === 'verify')  await verifyDoctor(selected.id)
      if (modal === 'reject')  await rejectDoctor(selected.id, rejectReason)
      if (modal === 'suspend') await suspendDoctor(selected.id)
      showToast(`Doctor ${modal}ed successfully.`, 'success')
      closeModal()
      fetchDoctors()
    } catch (err) {
      showToast(err?.response?.data?.message || 'Action failed.', 'error')
    } finally {
      setActLoading(false)
    }
  }

  const columns = [
    { key: 'fullName',  label: 'Name' },
    { key: 'specialty', label: 'Specialization' },
    { key: 'licenseNumber', label: 'License No.' },
    { key: 'experienceYears', label: 'Experience', render: (r) => r.experienceYears ? `${r.experienceYears} yrs` : '—' },
    { key: 'status', label: 'Status', render: () => <Badge status="PENDING" /> },
    {
      key: 'actions',
      label: 'Actions',
      render: (r) => (
        <div className="action-btns">
          <button className="btn btn-success btn-sm" onClick={() => openModal(r, 'verify')}  id={`verify-${r.id}`}>Verify</button>
          <button className="btn btn-danger  btn-sm" onClick={() => openModal(r, 'reject')}  id={`reject-${r.id}`}>Reject</button>
          <button className="btn btn-gray    btn-sm" onClick={() => openModal(r, 'suspend')} id={`suspend-${r.id}`}>Suspend</button>
        </div>
      ),
    },
  ]

  if (error) return <ErrorState message={error} onRetry={fetchDoctors} />

  return (
    <div className="page">
      <ToastContainer toasts={toasts} dismiss={dismiss} />

      <div className="page-header">
        <h1 className="page-title">Doctor Verification</h1>
        <p className="page-subtitle">Review and approve pending doctor registrations</p>
      </div>

      <div className="card">
        <DataTable
          columns={columns}
          data={doctors}
          loading={loading}
          emptyMessage="No pending doctors to verify. All clear! ✅"
        />
      </div>

      {/* Verify Modal */}
      <Modal
        isOpen={modal === 'verify'}
        title="Verify Doctor"
        onClose={closeModal}
        onConfirm={handleConfirm}
        confirmText="Yes, Verify"
        confirmVariant="success"
        loading={actionLoading}
      >
        <p>Are you sure you want to verify <strong>{selected?.fullName}</strong>?</p>
        <p className="text-muted">They will be able to accept patient appointments.</p>
      </Modal>

      {/* Reject Modal */}
      <Modal
        isOpen={modal === 'reject'}
        title="Reject Doctor"
        onClose={closeModal}
        onConfirm={handleConfirm}
        confirmText="Reject"
        confirmVariant="danger"
        loading={actionLoading}
      >
        <p>Provide a reason for rejecting <strong>{selected?.fullName}</strong>:</p>
        <textarea
          id="reject-reason"
          className="form-input"
          rows={3}
          style={{ marginTop: '0.75rem', resize: 'vertical' }}
          placeholder="Enter rejection reason..."
          value={rejectReason}
          onChange={(e) => setRejectReason(e.target.value)}
        />
      </Modal>

      {/* Suspend Modal */}
      <Modal
        isOpen={modal === 'suspend'}
        title="Suspend Doctor"
        onClose={closeModal}
        onConfirm={handleConfirm}
        confirmText="Suspend"
        confirmVariant="danger"
        loading={actionLoading}
      >
        <p>Are you sure you want to suspend <strong>{selected?.fullName}</strong>?</p>
        <p className="text-muted">They will no longer be able to access the platform.</p>
      </Modal>
    </div>
  )
}
