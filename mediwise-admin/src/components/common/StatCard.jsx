import React from 'react'

export default function StatCard({ title, value, icon, color = 'blue', subtitle, badge }) {
  return (
    <div className={`stat-card stat-card--${color}`}>
      <div className="stat-card-header">
        <div className={`stat-card-icon stat-card-icon--${color}`}>
          {icon}
        </div>
        {badge && <span className="stat-card-badge">{badge}</span>}
      </div>
      <div className="stat-card-body">
        <div className="stat-card-value">{value ?? '—'}</div>
        <div className="stat-card-title">{title}</div>
        {subtitle && <div className="stat-card-subtitle">{subtitle}</div>}
      </div>
    </div>
  )
}
