import { useAuth } from '../../context/AuthContext'
import { useNavigate } from 'react-router-dom'

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
        <h2 className="admin-header-title">Admin Portal</h2>
      </div>
      <div className="admin-header-right">
        <div className="admin-header-user">
          <div className="admin-header-avatar">
            {user?.fullName?.[0]?.toUpperCase() || 'A'}
          </div>
          <div className="admin-header-user-info">
            <span className="admin-header-user-name">{user?.fullName || 'Admin'}</span>
            <span className="admin-header-user-role">Administrator</span>
          </div>
        </div>
        <button
          className="btn btn-outline btn-sm"
          onClick={handleLogout}
          id="logout-btn"
        >
          Logout
        </button>
      </div>
    </header>
  )
}
