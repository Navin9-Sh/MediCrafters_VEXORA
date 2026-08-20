import axios from 'axios'
import { API_BASE_URL } from '../utils/constants'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// ── Request interceptor: attach JWT token from localStorage ──────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('mediwise_admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ── Response interceptor: handle 401 globally ────────────────────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('mediwise_admin_token')
      localStorage.removeItem('mediwise_admin_user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
