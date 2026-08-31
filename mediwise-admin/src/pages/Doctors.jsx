import { useEffect, useState } from 'react'
import { getDoctors } from '../services/doctorService'
import DataTable from '../components/common/DataTable'
import Badge from '../components/common/Badge'
import ErrorState from '../components/common/ErrorState'
import { StarIcon, SearchIcon } from '../components/common/Icons'

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
      const params = { size: 50 }
      if (search) params.search = search
      if (specialty) params.specialty = specialty
      const res = await getDoctors(params)
      setDoctors(res?.data?.content || [])
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load doctor directory.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchDoctors() }, [search, specialty])

  const columns = [
    {
      key: 'fullName',
      label: 'Doctor Name',
      render: (r) => (
        <div>
          <div style={{ fontWeight: 600 }}>{r.fullName}</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{r.email || `ID: ${r.id?.slice(0, 8)}...`}</div>
        </div>
      ),
    },
    { key: 'specialty',       label: 'Specialization' },
    {
      key: 'licenseNumber',
      label: 'License No.',
      render: (r) => <code style={{ fontSize: '0.75rem', background: '#f8fafc', padding: '2px 6px', borderRadius: '4px' }}>{r.licenseNumber || '—'}</code>,
    },
    { key: 'experienceYears', label: 'Experience', render: (r) => r.experienceYears != null ? `${r.experienceYears} yrs` : '—' },
    { key: 'consultationFee', label: 'Fee',        render: (r) => r.consultationFee != null ? `₹${r.consultationFee}` : '—' },
    {
      key: 'avgRating',
      label: 'Rating',
      render: (r) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <StarIcon size={14} className="text-warning" />
          <span style={{ fontWeight: 600 }}>{r.avgRating ? Number(r.avgRating).toFixed(1) : '—'}</span>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>({r.totalReviews || 0})</span>
        </div>
      ),
    },
    { key: 'verified',        label: 'Status',     render: (r) => <Badge status={r.verified ? 'VERIFIED' : (r.available === false ? 'REJECTED' : 'PENDING')} /> },
    {
      key: 'available',
      label: 'Availability',
      render: (r) => (
        <span className={r.available ? 'badge badge--success-light' : 'badge badge--gray-light'}>
          {r.available ? 'Available' : 'Unavailable'}
        </span>
      ),
    },
  ]

  if (error) return <ErrorState message={error} onRetry={fetchDoctors} />

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Doctor Directory</h1>
          <p className="page-subtitle">Master repository of verified and registered medical practitioners</p>
        </div>
        <button className="btn btn-outline btn-sm" onClick={fetchDoctors}>
          Refresh Directory
        </button>
      </div>

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <SearchIcon size={16} className="search-icon" />
          <input
            id="doctor-search"
            type="text"
            className="form-input search-input"
            placeholder="Search doctor by name or license..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <input
          id="doctor-specialty"
          type="text"
          className="form-input"
          placeholder="Filter by specialty (e.g. Cardiology)..."
          value={specialty}
          onChange={(e) => setSpecialty(e.target.value)}
          style={{ maxWidth: '280px' }}
        />
      </div>

      <div className="card">
        <DataTable
          columns={columns}
          data={doctors}
          loading={loading}
          emptyMessage="No doctors matching the selected criteria."
        />
      </div>
    </div>
  )
}
