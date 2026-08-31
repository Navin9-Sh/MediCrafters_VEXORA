import React from 'react'
import { CheckCircleIcon, AlertCircleIcon, AlertTriangleIcon, XIcon } from './Icons'
import { useToast } from '../../hooks/useToast'

export { useToast }

export default function ToastContainer({ toasts = [], dismiss }) {
  const getIcon = (type) => {
    if (type === 'success') return <CheckCircleIcon size={18} />
    if (type === 'error')   return <AlertCircleIcon size={18} />
    if (type === 'warning') return <AlertTriangleIcon size={18} />
    return <CheckCircleIcon size={18} />
  }

  return (
    <div className="toast-container">
      {toasts.map((t) => (
        <div key={t.id} className={`toast toast--${t.type}`}>
          <div className="toast-icon">
            {getIcon(t.type)}
          </div>
          <span className="toast-message">{t.message}</span>
          <button className="toast-close" onClick={() => dismiss(t.id)} aria-label="Close">
            <XIcon size={14} />
          </button>
        </div>
      ))}
    </div>
  )
}
