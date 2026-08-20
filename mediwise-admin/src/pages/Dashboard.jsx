import { useEffect, useState } from 'react'
import api from '../services/api'
import StatCard from '../components/common/StatCard'
import ErrorState from '../components/common/ErrorState'

export default function Dashboard() {
  const [stats, setStats]     = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')

  async function fetchStats() {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/api/v1/analytics/dashboard')
      setStats(res.data?.data)
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load dashboard stats.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchStats() }, [])

  if (error) return <ErrorState message={error} onRetry={fetchStats} />

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Dashboard</h1>
        <p className="page-subtitle">Overview of MediWise clinical operations</p>
      </div>

      {loading ? (
        <div className="stats-grid">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="stat-card stat-card--skeleton" />
          ))}
        </div>
      ) : (
        <>
          <div className="stats-grid">
            <StatCard title="Total Patients"      value={stats?.totalPatients}           icon="👥" color="blue"   />
            <StatCard title="Total Doctors"       value={stats?.totalDoctors}            icon="👨‍⚕️" color="green"  />
            <StatCard title="Total Appointments"  value={stats?.totalAppointments}       icon="📅" color="purple" />
            <StatCard title="Pending"             value={stats?.pendingAppointments}     icon="⏳" color="orange" />
            <StatCard title="Completed"           value={stats?.completedAppointments}   icon="✅" color="teal"   />
            <StatCard
              title="Revenue (Total)"
              value={stats?.totalRevenue != null ? `₹${Number(stats.totalRevenue).toLocaleString()}` : '—'}
              icon="💰" color="gold"
            />
          </div>

          <div className="stats-grid" style={{ marginTop: '1.5rem' }}>
            <StatCard title="Avg. Doctor Rating"  value={stats?.averageRating?.toFixed(1) || '—'} icon="⭐" color="blue"  />
            <StatCard title="Total Reviews"        value={stats?.totalReviews}                       icon="💬" color="green" />
            <StatCard title="Cancelled"            value={stats?.cancelledAppointments}              icon="❌" color="red"   />
          </div>
        </>
      )}
    </div>
  )
}
