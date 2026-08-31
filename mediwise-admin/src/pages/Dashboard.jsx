import { useEffect, useState } from 'react'
import { getAdminStats } from '../services/analyticsService'
import StatCard from '../components/common/StatCard'
import ErrorState from '../components/common/ErrorState'
import {
  UsersIcon,
  DoctorIcon,
  ShieldCheckIcon,
  CalendarIcon,
  DollarSignIcon,
  ClockIcon,
  CheckCircleIcon,
  XCircleIcon,
  ZapIcon,
} from '../components/common/Icons'

export default function Dashboard() {
  const [stats, setStats]     = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')

  async function fetchStats() {
    setLoading(true)
    setError('')
    try {
      const res = await getAdminStats()
      setStats(res?.data)
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load platform statistics.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchStats() }, [])

  if (error) return <ErrorState message={error} onRetry={fetchStats} />

  const hasPending = stats?.pendingDoctorVerifications > 0

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Executive Dashboard</h1>
          <p className="page-subtitle">Real-time overview of MediWise clinical ecosystem and platform health</p>
        </div>
        <button className="btn btn-outline btn-sm" onClick={fetchStats}>
          Refresh Metrics
        </button>
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
            <StatCard
              title="Registered Patients"
              value={stats?.totalPatients ?? 0}
              icon={<UsersIcon size={22} />}
              color="blue"
              subtitle="Verified patient profiles"
            />
            <StatCard
              title="Registered Doctors"
              value={stats?.totalDoctors ?? 0}
              icon={<DoctorIcon size={22} />}
              color="green"
              subtitle="Active medical practitioners"
            />
            <StatCard
              title="Pending Verifications"
              value={stats?.pendingDoctorVerifications ?? 0}
              icon={<ShieldCheckIcon size={22} />}
              color={hasPending ? "orange" : "teal"}
              badge={hasPending ? "Action Required" : "All Clear"}
              subtitle="Doctors awaiting license review"
            />
            <StatCard
              title="Total Consultations"
              value={stats?.totalAppointments ?? 0}
              icon={<CalendarIcon size={22} />}
              color="purple"
              subtitle="Cumulative appointments"
            />
            <StatCard
              title="Active Platform Accounts"
              value={stats?.activeUsers ?? 0}
              icon={<ZapIcon size={22} />}
              color="teal"
              subtitle="In good standing"
            />
            <StatCard
              title="Gross Revenue"
              value={stats?.totalRevenue != null ? `₹${Number(stats.totalRevenue).toLocaleString()}` : '₹0'}
              icon={<DollarSignIcon size={22} />}
              color="gold"
              subtitle="Settled consultation payments"
            />
          </div>

          <div className="section-divider">
            <h3 className="section-title">Consultation Lifecycle Breakdown</h3>
          </div>

          <div className="stats-grid stats-grid--4">
            <StatCard
              title="Pending Bookings"
              value={stats?.pendingAppointments ?? 0}
              icon={<ClockIcon size={20} />}
              color="orange"
            />
            <StatCard
              title="Confirmed Consultations"
              value={stats?.confirmedAppointments ?? 0}
              icon={<CheckCircleIcon size={20} />}
              color="blue"
            />
            <StatCard
              title="Completed Sessions"
              value={stats?.completedAppointments ?? 0}
              icon={<CheckCircleIcon size={20} />}
              color="teal"
            />
            <StatCard
              title="Cancelled Sessions"
              value={stats?.cancelledAppointments ?? 0}
              icon={<XCircleIcon size={20} />}
              color="red"
            />
          </div>
        </>
      )}
    </div>
  )
}
