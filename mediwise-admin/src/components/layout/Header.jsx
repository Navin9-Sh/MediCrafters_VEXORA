import { useAuth } from '../../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import { LogOutIcon } from '../common/Icons'

export default function Header() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <header className="admin-header">
      <div className="admin-header-left">
        <h2 className="admin-header-title">Admin Management Portal</h2>
      </div>
      <div className="admin-header-right">
        <div className="admin-header-user">
          <div className="admin-header-avatar">
            {user?.fullName?.[0]?.toUpperCase() || 'A'}
          </div>
          <div className="admin-header-user-info">
            <span className="admin-header-user-name">{user?.fullName || 'Administrator'}</span>
            <span className="admin-header-user-role">System Admin</span>
          </div>
        </div>
        <button
          className="btn btn-outline btn-sm logout-btn"
          onClick={handleLogout}
          id="logout-btn"
        >
          <LogOutIcon size={16} />
          <span>Logout</span>
        </button>
      </div>
    </header>
  )
}
