const STATUS_CONFIG = {
  // Doctor
  VERIFIED:  { label: 'Verified',  cls: 'badge--success' },
  PENDING:   { label: 'Pending',   cls: 'badge--warning' },
  REJECTED:  { label: 'Rejected',  cls: 'badge--danger'  },
  SUSPENDED: { label: 'Suspended', cls: 'badge--gray'    },
  // Appointments
  CONFIRMED: { label: 'Confirmed', cls: 'badge--success' },
  COMPLETED: { label: 'Completed', cls: 'badge--blue'    },
  CANCELLED: { label: 'Cancelled', cls: 'badge--danger'  },
  // User active
  true:  { label: 'Active',    cls: 'badge--success' },
  false: { label: 'Suspended', cls: 'badge--danger'  },
}

export default function Badge({ status }) {
  const cfg = STATUS_CONFIG[status] || { label: status, cls: 'badge--gray' }
  return <span className={`badge ${cfg.cls}`}>{cfg.label}</span>
}
