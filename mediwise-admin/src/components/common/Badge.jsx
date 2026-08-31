import React from 'react'

const STATUS_CONFIG = {
  // Doctor Verification
  VERIFIED:  { label: 'Verified',  cls: 'badge--success' },
  PENDING:   { label: 'Pending Approval', cls: 'badge--warning' },
  REJECTED:  { label: 'Rejected',  cls: 'badge--danger'  },
  SUSPENDED: { label: 'Suspended', cls: 'badge--gray'    },

  // Appointments
  CONFIRMED: { label: 'Confirmed', cls: 'badge--success' },
  IN_PROGRESS: { label: 'In Progress', cls: 'badge--blue' },
  COMPLETED: { label: 'Completed', cls: 'badge--teal'    },
  CANCELLED: { label: 'Cancelled', cls: 'badge--danger'  },

  // User status
  ACTIVE:    { label: 'Active',    cls: 'badge--success' },
  INACTIVE:  { label: 'Inactive',  cls: 'badge--gray'    },
  true:      { label: 'Active',    cls: 'badge--success' },
  false:     { label: 'Suspended', cls: 'badge--danger'  },
}

export default function Badge({ status }) {
  const cfg = STATUS_CONFIG[status] || { label: status || 'Unknown', cls: 'badge--gray' }
  return (
    <span className={`badge ${cfg.cls}`}>
      <span className="badge-dot" />
      <span className="badge-text">{cfg.label}</span>
    </span>
  )
}
