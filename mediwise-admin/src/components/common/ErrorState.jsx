export default function ErrorState({ message, onRetry }) {
  return (
    <div className="error-state">
      <span className="error-state-icon">⚠️</span>
      <h3 className="error-state-title">Something went wrong</h3>
      <p className="error-state-msg">{message || 'Failed to load data. Please try again.'}</p>
      {onRetry && (
        <button className="btn btn-primary" onClick={onRetry}>
          Retry
        </button>
      )}
    </div>
  )
}
