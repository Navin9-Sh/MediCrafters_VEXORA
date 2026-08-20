import { useEffect, useState } from 'react'

export function useToast() {
  const [toasts, setToasts] = useState([])

  const show = (message, type = 'success') => {
    const id = Date.now()
    setToasts((prev) => [...prev, { id, message, type }])
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 4000)
  }

  const dismiss = (id) => setToasts((prev) => prev.filter((t) => t.id !== id))

  return { toasts, show, dismiss }
}

export default function ToastContainer({ toasts, dismiss }) {
  return (
    <div className="toast-container">
      {toasts.map((t) => (
        <div key={t.id} className={`toast toast--${t.type}`}>
          <span>{t.message}</span>
          <button className="toast-close" onClick={() => dismiss(t.id)}>✕</button>
        </div>
      ))}
    </div>
  )
}
