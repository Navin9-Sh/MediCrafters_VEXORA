import { useEffect, useState } from 'react'
import { getDoctors } from '../services/doctorService'
import DataTable from '../components/common/DataTable'
import Badge from '../components/common/Badge'
import ErrorState from '../components/common/ErrorState'

export default function Doctors() {
  const [doctors, setDoctors] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')
  const [search, setSearch]   = useState('')
  const [specialty, setSpecialty] = useState('')

  async function fetchDoctors() {
    setLoading(true)
    setError('')
    try {
      const res = await getDoctors({ search, specialty, size: 50 })
      setDoctors(res?.data?.content || [])
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load doctors.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchDoctors() }, [search, specialty])

  const columns = [
    { key: 'fullName',        label: 'Name' },
    { key: 'specialty',       label: 'Specialization' },
    { key: 'experienceYears', label: 'Experience', render: (r) => r.experienceYears ? `${r.experienceYears} yrs` : '—' },
    { key: 'consultationFee', label: 'Fee',        render: (r) => r.consultationFee ? `₹${r.consultationFee}` : '—' },
    { key: 'avgRating',       label: 'Rating',     render: (r) => r.avgRating ? `⭐ ${Number(r.avgRating).toFixed(1)}` : '—' },
    { key: 'verified',        label: 'Status',     render: (r) => <Badge status={r.verified ? 'VERIFIED' : 'PENDING'} /> },
    { key: 'available',       label: 'Available',  render: (r) => <span className={r.available ? 'text-success' : 'text-muted'}>{ r.available ? 'Yes' : 'No'}</span> },
  ]

  if (error) return <ErrorState message={error} onRetry={fetchDoctors} />

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Doctors</h1>
        <p className="page-subtitle">Manage all registered doctors on the platform</p>
      </div>

      <div className="filter-bar">
        <input
          id="doctor-search"
          type="text"
          className="form-input"
          placeholder="Search by name..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{ maxWidth: '260px' }}
        />
        <input
          id="doctor-specialty"
          type="text"
          className="form-input"
          placeholder="Filter by specialization..."
          value={specialty}
          onChange={(e) => setSpecialty(e.target.value)}
          style={{ maxWidth: '220px' }}
        />
      </div>

      <div className="card">
        <DataTable
          columns={columns}
          data={doctors}
          loading={loading}
          emptyMessage="No doctors found."
        />
      </div>
    </div>
  )
}
