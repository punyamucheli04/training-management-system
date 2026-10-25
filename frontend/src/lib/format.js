// Display helpers. Dates arrive from the API as "YYYY-MM-DD", times as "HH:MM:SS".

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

export function formatDate(value) {
  if (!value) return '—';
  const [y, m, d] = String(value).slice(0, 10).split('-').map(Number);
  if (!y || !m || !d) return String(value);
  return `${d} ${MONTHS[m - 1]} ${y}`;
}

export function formatTime(value) {
  if (!value) return '—';
  return String(value).slice(0, 5);
}

export function formatDateTime(value) {
  if (!value) return '—';
  const date = new Date(String(value).replace(' ', 'T'));
  if (Number.isNaN(date.getTime())) return String(value);
  const hh = String(date.getHours()).padStart(2, '0');
  const mm = String(date.getMinutes()).padStart(2, '0');
  return `${date.getDate()} ${MONTHS[date.getMonth()]} ${date.getFullYear()}, ${hh}:${mm}`;
}

export function formatPct(value) {
  if (value === null || value === undefined || value === '') return '—';
  return `${Number(value).toFixed(1)}%`;
}

export function formatNumber(value, digits = 2) {
  if (value === null || value === undefined || value === '') return '—';
  return Number(value).toFixed(digits);
}

export function sessionLabel(s) {
  return `${s.Session_ID} – ${s.Tr_Name}, ${formatDate(s.Session_Date)} ${formatTime(s.Start_Time)}`;
}

/** Today's date as YYYY-MM-DD in the browser's own time zone. */
export function todayISO() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
