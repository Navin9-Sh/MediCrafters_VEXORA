import { useEffect } from 'react'
import { XIcon } from './Icons'

export default function Modal({ isOpen, title, children, onClose, onConfirm, confirmText = 'Confirm', confirmVariant = 'danger', loading }) {
  useEffect(() => {
    if (isOpen) document.body.style.overflow = 'hidden'
    else document.body.style.overflow = ''
    return () => { document.body.style.overflow = '' }
  }, [isOpen])

  if (!isOpen) return null

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        <div className="modal-header">
          <h3 className="modal-title">{title}</h3>
          <button className="modal-close" onClick={onClose} aria-label="Close">
            <XIcon size={16} />
          </button>
        </div>
        <div className="modal-body">{children}</div>
        {onConfirm && (
          <div className="modal-footer">
            <button className="btn btn-outline" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button
              className={`btn btn-${confirmVariant}`}
              onClick={onConfirm}
              disabled={loading}
              id="modal-confirm-btn"
            >
              {loading ? 'Processing...' : confirmText}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
