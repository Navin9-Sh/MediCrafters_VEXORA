import api from './api'

export async function loginWithEmailPassword(email, password) {
  const response = await api.post('/api/v1/auth/login', {
    emailOrPhone: email,
    password,
  })
  return response.data
}

export async function getMe() {
  const response = await api.get('/api/v1/auth/me')
  return response.data
}

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
