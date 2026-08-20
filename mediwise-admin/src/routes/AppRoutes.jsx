import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import AdminLayout from '../components/layout/AdminLayout'
import Login from '../pages/Login'
import Dashboard from '../pages/Dashboard'
import Doctors from '../pages/Doctors'
import DoctorVerification from '../pages/DoctorVerification'
import Patients from '../pages/Patients'
import Appointments from '../pages/Appointments'
import AssignDoctor from '../pages/AssignDoctor'
import AuditLogs from '../pages/AuditLogs'
import LoadingSpinner from '../components/common/LoadingSpinner'

/** Redirects to login if not authenticated */
function ProtectedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth()
  if (loading) return <LoadingSpinner fullScreen />
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return children
}

/** Redirects authenticated admins away from login page */
function PublicRoute({ children }) {
  const { isAuthenticated, loading } = useAuth()
  if (loading) return <LoadingSpinner fullScreen />
  if (isAuthenticated) return <Navigate to="/" replace />
  return children
}

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />

      {/* Protected Admin Routes */}
      <Route path="/" element={<ProtectedRoute><AdminLayout /></ProtectedRoute>}>
        <Route index element={<Dashboard />} />
        <Route path="doctors" element={<Doctors />} />
        <Route path="doctors/verification" element={<DoctorVerification />} />
        <Route path="patients" element={<Patients />} />
        <Route path="appointments" element={<Appointments />} />
        <Route path="assign-doctor" element={<AssignDoctor />} />
        <Route path="audit-logs" element={<AuditLogs />} />
      </Route>

      {/* Catch-all */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
