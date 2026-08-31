import React from 'react'
import { AlertTriangleIcon, RefreshCwIcon } from './Icons'

export default function ErrorState({ message, onRetry }) {
  return (
    <div className="error-state">
      <div className="error-state-icon-container">
        <AlertTriangleIcon size={32} />
      </div>
      <h3 className="error-state-title">Unable to Load Data</h3>
      <p className="error-state-msg">{message || 'An error occurred while fetching information from the clinical server.'}</p>
      {onRetry && (
        <button className="btn btn-primary btn-retry" onClick={onRetry}>
          <RefreshCwIcon size={16} />
          <span>Try Again</span>
        </button>
      )}
    </div>
  )
}
