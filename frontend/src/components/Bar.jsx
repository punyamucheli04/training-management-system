/** Thin horizontal percentage bar with the value next to it. */
export default function Bar({ value }) {
  if (value === null || value === undefined) return <span className="muted">Not marked yet</span>;
  const pct = Math.max(0, Math.min(100, Number(value)));
  const tone = pct >= 75 ? 'good' : pct >= 50 ? 'fair' : 'low';
  return (
    <span className="bar">
      <span className="bar-track"><span className={`bar-fill bar-${tone}`} style={{ width: `${pct}%` }} /></span>
      <span className="bar-value">{pct.toFixed(1)}%</span>
    </span>
  );
}
