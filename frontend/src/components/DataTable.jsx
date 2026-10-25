/**
 * Simple table.
 * columns: [{ key, label, render?(row), align?: 'right', width?, nowrap? }]
 */
export default function DataTable({ columns, rows, rowKey, loading, error, emptyText = 'No records found.' }) {
  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>
            {columns.map((c) => (
              <th key={c.key} className={c.align === 'right' ? 'num' : undefined} style={c.width ? { width: c.width } : undefined}>
                {c.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {error ? (
            <tr><td colSpan={columns.length} className="table-message error-text">{error}</td></tr>
          ) : loading && (!rows || rows.length === 0) ? (
            <tr><td colSpan={columns.length} className="table-message">Loading…</td></tr>
          ) : !rows || rows.length === 0 ? (
            <tr><td colSpan={columns.length} className="table-message">{emptyText}</td></tr>
          ) : (
            rows.map((row) => (
              <tr key={rowKey(row)}>
                {columns.map((c) => (
                  <td key={c.key} className={[c.align === 'right' ? 'num' : '', c.nowrap ? 'nowrap' : ''].join(' ').trim() || undefined}>
                    {c.render ? c.render(row) : row[c.key] ?? <span className="muted">—</span>}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
