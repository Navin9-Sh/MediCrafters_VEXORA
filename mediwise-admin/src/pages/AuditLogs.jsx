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
      setError(err?.response?.data?.message || 'Failed to load audit logs.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchLogs() }, [])

  const columns = [
    { key: 'timestamp',    label: 'Time',     render: (r) => r.timestamp ? new Date(r.timestamp).toLocaleString() : '—' },
    { key: 'actorId',      label: 'Actor ID', render: (r) => r.actorId?.slice(0, 12) + '...' },
    { key: 'actorRole',    label: 'Role' },
    { key: 'action',       label: 'Action' },
    { key: 'resourceType', label: 'Resource' },
    { key: 'resourceId',   label: 'Resource ID' },
    { key: 'outcome',      label: 'Outcome',  render: (r) => (
      <span className={r.outcome === 'SUCCESS' ? 'text-success' : 'text-danger'}>
        {r.outcome}
      </span>
    )},
  ]

  if (error) return <ErrorState message={error} onRetry={fetchLogs} />

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Audit Logs</h1>
        <p className="page-subtitle">
          System activity log — 90 day retention
          <span className="badge badge--warning" style={{ marginLeft: '0.75rem' }}>MOCK DATA</span>
        </p>
      </div>

      <div className="card">
        <DataTable
          columns={columns}
          data={logs}
          loading={loading}
          emptyMessage="No audit logs found."
        />
      </div>
    </div>
  )
}
