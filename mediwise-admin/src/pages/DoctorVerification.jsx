import { useEffect, useState } from 'react'
import { getPendingDoctors, verifyDoctor, rejectDoctor } from '../services/doctorService'
import DataTable from '../components/common/DataTable'
import Badge from '../components/common/Badge'
import Modal from '../components/common/Modal'
import ToastContainer, { useToast } from '../components/common/Toast'
import ErrorState from '../components/common/ErrorState'
import { CheckCircleIcon, XCircleIcon, ShieldCheckIcon } from '../components/common/Icons'

export default function DoctorVerification() {
  const [allDoctors, setAllDoctors] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionLoading, setActLoading] = useState(false)
  const [activeTab, setActiveTab] = useState('pending') // 'pending' | 'rejected'
  const [selected, setSelected] = useState(null)
  const [modal, setModal] = useState(null) // 'verify' | 'reject'
  const [rejectReason, setRejectReason] = useState('')
  const { toasts, show: showToast, dismiss } = useToast()

  async function fetchDoctors() {
    setLoading(true)
    setError('')
    try {
      const res = await getPendingDoctors({ size: 100 })
      setAllDoctors(res?.data?.content || [])
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load doctor applications.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchDoctors() }, [])

  const pendingDoctors = allDoctors.filter((d) => !d.verified && d.available !== false)
  const rejectedDoctors = allDoctors.filter((d) => !d.verified && d.available === false)
  const displayDoctors = activeTab === 'pending' ? pendingDoctors : rejectedDoctors

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
      if (modal === 'verify') {
        await verifyDoctor(selected.id)
        showToast(`Dr. ${selected.fullName} has been approved and verified for clinical consultations.`, 'success')
      } else if (modal === 'reject') {
        await rejectDoctor(selected.id, rejectReason || 'Medical credentials could not be verified with council registry.')
        showToast(`Application for Dr. ${selected.fullName} has been marked as Rejected.`, 'info')
      }
      closeModal()
      fetchDoctors()
    } catch (err) {
      showToast(err?.response?.data?.message || 'Action failed.', 'error')
    } finally {
      setActLoading(false)
    }
  }

  const columns = [
    {
      key: 'fullName',
      label: 'Doctor Name',
      render: (r) => (
        <div>
          <div style={{ fontWeight: 600 }}>{r.fullName}</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>ID: {r.id?.slice(0, 8)}...</div>
        </div>
      ),
    },
    { key: 'specialty', label: 'Specialization' },
    {
      key: 'licenseNumber',
      label: 'License / Reg No.',
      render: (r) => <code style={{ fontSize: '0.8rem', background: '#f1f5f9', padding: '2px 6px', borderRadius: '4px' }}>{r.licenseNumber || 'UNSPECIFIED'}</code>,
    },
    { key: 'experienceYears', label: 'Experience', render: (r) => r.experienceYears != null ? `${r.experienceYears} yrs` : '—' },
    { key: 'consultationFee', label: 'Consultation Fee', render: (r) => r.consultationFee != null ? `₹${r.consultationFee}` : '—' },
    {
      key: 'status',
      label: 'Status',
      render: (r) => <Badge status={r.available === false ? 'REJECTED' : 'PENDING'} />,
    },
    {
      key: 'actions',
      label: 'Decision',
      render: (r) => (
        <div className="action-btns">
          {r.available === false ? (
            <button className="btn btn-primary btn-sm" onClick={() => openModal(r, 'verify')} id={`re-verify-${r.id}`}>
              <CheckCircleIcon size={14} />
              <span>Re-evaluate & Approve</span>
            </button>
          ) : (
            <>
              <button className="btn btn-success btn-sm" onClick={() => openModal(r, 'verify')} id={`verify-${r.id}`}>
                <CheckCircleIcon size={14} />
                <span>Approve</span>
              </button>
              <button className="btn btn-danger btn-sm" onClick={() => openModal(r, 'reject')} id={`reject-${r.id}`}>
                <XCircleIcon size={14} />
                <span>Reject</span>
              </button>
            </>
          )}
        </div>
      ),
    },
  ]

  if (error) return <ErrorState message={error} onRetry={fetchDoctors} />

  return (
    <div className="page">
      <ToastContainer toasts={toasts} dismiss={dismiss} />

      <div className="page-header">
        <div>
          <h1 className="page-title">Doctor Verification & Board Governance</h1>
          <p className="page-subtitle">Review medical credentials and grant or reject platform consultation privileges</p>
        </div>
        <button className="btn btn-outline btn-sm" onClick={fetchDoctors}>
          Refresh Applications
        </button>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.75rem', borderBottom: '1px solid var(--border)', paddingBottom: '0.5rem' }}>
        <button
          className={`btn btn-sm ${activeTab === 'pending' ? 'btn-primary' : 'btn-outline'}`}
          onClick={() => setActiveTab('pending')}
          id="tab-pending"
        >
          <ShieldCheckIcon size={14} />
          <span>Pending Verification ({pendingDoctors.length})</span>
        </button>
        <button
          className={`btn btn-sm ${activeTab === 'rejected' ? 'btn-danger' : 'btn-outline'}`}
          onClick={() => setActiveTab('rejected')}
          id="tab-rejected"
        >
          <XCircleIcon size={14} />
          <span>Rejected Applications ({rejectedDoctors.length})</span>
        </button>
      </div>

      <div className="card">
        <DataTable
          columns={columns}
          data={displayDoctors}
          loading={loading}
          emptyMessage={
            activeTab === 'pending'
              ? 'No pending doctor applications awaiting verification. All clear!'
              : 'No rejected doctor applications.'
          }
        />
      </div>

      {/* Approve Modal */}
      <Modal
        isOpen={modal === 'verify'}
        title="Approve Medical Practitioner"
        onClose={closeModal}
        onConfirm={handleConfirm}
        confirmText="Approve Credentials"
        confirmVariant="success"
        loading={actionLoading}
      >
        <p>You are about to authorize <strong>Dr. {selected?.fullName}</strong> ({selected?.specialty}) on MediWise.</p>
        <div style={{ margin: '1rem 0', padding: '0.75rem', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
          <div><strong>License Number:</strong> <code>{selected?.licenseNumber || 'Not provided'}</code></div>
          <div><strong>Stated Experience:</strong> {selected?.experienceYears || 0} years</div>
          <div><strong>Fee:</strong> ₹{selected?.consultationFee || 0}</div>
        </div>
        <p className="text-muted">Once verified, this practitioner will become publicly visible and eligible for consultations.</p>
      </Modal>

      {/* Reject Modal */}
      <Modal
        isOpen={modal === 'reject'}
        title="Reject Doctor Application"
        onClose={closeModal}
        onConfirm={handleConfirm}
        confirmText="Confirm Rejection"
        confirmVariant="danger"
        loading={actionLoading}
      >
        <p>Are you sure you want to reject the application for <strong>Dr. {selected?.fullName}</strong>?</p>
        <div className="form-group" style={{ marginTop: '0.75rem' }}>
          <label className="form-label">Clinical / Verification Reason (Optional)</label>
          <textarea
            className="form-input"
            rows="3"
            placeholder="e.g. License verification mismatch with national medical council..."
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
          />
        </div>
        <p className="text-muted" style={{ marginTop: '0.5rem', fontSize: '0.8rem' }}>
          Their account will be deactivated and moved to the Rejected Applications archive.
        </p>
      </Modal>
    </div>
  )
}
