/** Small coloured label for attendance / session status. */
export default function StatusBadge({ value }) {
  if (!value) return <span className="muted">—</span>;
  const key = String(value).toLowerCase();
  return <span className={`badge badge-${key}`}>{value}</span>;
}
