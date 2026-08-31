import { useEffect, useState } from 'react'
import { getAuditLogs } from '../services/auditService'
import DataTable from '../components/common/DataTable'
import ErrorState from '../components/common/ErrorState'

export default function AuditLogs() {
  const [logs, setLogs]       = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')

  async function fetchLogs() {
    setLoading(true)
    setError('')
    try {
      const res = await getAuditLogs()
      setLogs(res?.data || [])
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load system audit trail.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchLogs() }, [])

  const columns = [
    { key: 'timestamp',    label: 'Timestamp', render: (r) => r.timestamp ? new Date(r.timestamp).toLocaleString() : '—' },
    { key: 'actorId',      label: 'Actor Identifier', render: (r) => <code style={{ fontSize: '0.75rem', background: '#f8fafc', padding: '2px 6px', borderRadius: '4px' }}>{r.actorId?.slice(0, 12)}...</code> },
    { key: 'actorRole',    label: 'Role', render: (r) => <span className="badge badge--gray-light">{r.actorRole || 'SYSTEM'}</span> },
    { key: 'action',       label: 'Clinical Event / Action', render: (r) => <strong style={{ color: 'var(--text)' }}>{r.action}</strong> },
    { key: 'resourceType', label: 'Resource Target', render: (r) => <code>{r.resourceType}</code> },
    {
      key: 'outcome',
      label: 'Status Outcome',
      render: (r) => (
        <span className={r.outcome === 'SUCCESS' ? 'badge badge--success' : 'badge badge--danger'}>
          <span className="badge-dot" />
          <span className="badge-text">{r.outcome}</span>
        </span>
      ),
    },
  ]

  if (error) return <ErrorState message={error} onRetry={fetchLogs} />

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Compliance & Security Audit Trail</h1>
          <p className="page-subtitle">Immutable chronological logging of all clinical, administrative, and security operations</p>
        </div>
        <button className="btn btn-outline btn-sm" onClick={fetchLogs}>
          Refresh Audit Trail
        </button>
      </div>

      <div className="card">
        <DataTable
          columns={columns}
          data={logs}
          loading={loading}
          emptyMessage="No audit logs recorded."
        />
      </div>
    </div>
  )
}
