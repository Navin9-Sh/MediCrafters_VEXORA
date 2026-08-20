import { NavLink } from 'react-router-dom'
import { ROUTES } from '../../utils/constants'

const NAV_ITEMS = [
  { label: 'Dashboard',       path: ROUTES.DASHBOARD,         icon: '📊' },
  { label: 'Doctors',         path: ROUTES.DOCTORS,           icon: '👨‍⚕️' },
  { label: 'Verification',    path: ROUTES.DOCTOR_VERIFICATION,icon: '✅' },
  { label: 'Patients',        path: ROUTES.PATIENTS,          icon: '🧑‍🤝‍🧑' },
  { label: 'Appointments',    path: ROUTES.APPOINTMENTS,      icon: '📅' },
  { label: 'Assign Doctor',   path: ROUTES.ASSIGN_DOCTOR,     icon: '🔗' },
  { label: 'Audit Logs',      path: ROUTES.AUDIT_LOGS,        icon: '📋' },
]

export default function Sidebar() {
  return (
    <aside className="sidebar">
      {/* Brand */}
      <div className="sidebar-brand">
        <span className="sidebar-brand-icon">🏥</span>
        <div>
          <div className="sidebar-brand-name">MediWise</div>
          <div className="sidebar-brand-sub">Admin Panel</div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="sidebar-nav">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end={item.path === '/'}
            className={({ isActive }) =>
              `sidebar-nav-item${isActive ? ' sidebar-nav-item--active' : ''}`
            }
          >
            <span className="sidebar-nav-icon">{item.icon}</span>
            <span className="sidebar-nav-label">{item.label}</span>
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="sidebar-footer">
        <span className="sidebar-footer-text">v1.0.0 · MediWise</span>
      </div>
    </aside>
  )
}
