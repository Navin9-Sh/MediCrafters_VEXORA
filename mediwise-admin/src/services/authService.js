import api from './api'

/**
 * Login with email + password (Admin Panel uses direct auth, no Firebase)
 *
 * Backend endpoint: POST /api/v1/auth/login
 * Request:  { emailOrPhone: string, password: string }
 * Response: { success, data: { accessToken, refreshToken, user } }
 */
export async function loginWithEmailPassword(email, password) {
  const response = await api.post('/api/v1/auth/login', {
    emailOrPhone: email,
    password,
  })
  return response.data
}

/**
 * Get the currently authenticated admin user info
 *
 * Backend endpoint: GET /api/v1/auth/me
 */
export async function getMe() {
  const response = await api.get('/api/v1/auth/me')
  return response.data
}

/**
 * Logout — clears local session (backend token blacklist via /logout optional)
 *
 * Backend endpoint: POST /api/v1/auth/logout
 */
export async function logout() {
  try {
    const token = localStorage.getItem('mediwise_admin_token')
    if (token) {
      await api.post('/api/v1/auth/logout', null, {
        headers: { Authorization: `Bearer ${token}` },
      })
    }
  } finally {
    localStorage.removeItem('mediwise_admin_token')
    localStorage.removeItem('mediwise_admin_user')
  }
}
