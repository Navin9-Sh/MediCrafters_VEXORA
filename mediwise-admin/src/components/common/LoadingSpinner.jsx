export default function LoadingSpinner({ fullScreen }) {
  if (fullScreen) {
    return (
      <div className="loading-fullscreen">
        <div className="spinner spinner--lg" />
        <p>Loading MediWise Admin...</p>
      </div>
    )
  }
  return (
    <div className="loading-inline">
      <div className="spinner" />
    </div>
  )
}
