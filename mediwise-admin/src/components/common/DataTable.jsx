export default function DataTable({ columns, data, loading, emptyMessage = 'No records found.' }) {
  if (loading) {
    return (
      <div className="table-loading">
        <div className="spinner" />
        <span>Loading...</span>
      </div>
    )
  }

  if (!data || data.length === 0) {
    return (
      <div className="table-empty">
        <span className="table-empty-icon">📭</span>
        <p>{emptyMessage}</p>
      </div>
    )
  }

  return (
    <div className="table-wrapper">
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((col) => (
              <th key={col.key}>{col.label}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row, i) => (
            <tr key={row.id || i}>
              {columns.map((col) => (
                <td key={col.key}>
                  {col.render ? col.render(row) : row[col.key] ?? '—'}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
