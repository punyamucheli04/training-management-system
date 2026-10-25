import { useState } from 'react';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import DataTable from '../components/DataTable.jsx';
import PageHeader from '../components/PageHeader.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { useToast } from '../components/Toast.jsx';
import { enrollmentsApi, sessionsApi, studentsApi } from '../lib/api.js';
import { formatDate, formatDateTime, formatTime, sessionLabel } from '../lib/format.js';
import { useFetch } from '../lib/useFetch.js';

export default function EnrollmentsPage() {
  const notify = useToast();
  const [filterSession, setFilterSession] = useState('');
  const [form, setForm] = useState({ Session_ID: '', Student_ID: '' });
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const [removing, setRemoving] = useState(null);

  const sessions = useFetch(() => sessionsApi.list(), []);
  const students = useFetch(() => studentsApi.list(), []);
  const enrollments = useFetch(
    () => enrollmentsApi.list({ sessionId: filterSession }),
    [filterSession]
  );

  async function enroll(event) {
    event.preventDefault();
    if (!form.Session_ID || !form.Student_ID) {
      setFormError('Choose both a session and a student.');
      return;
    }
    setSaving(true);
    setFormError('');
    try {
      // POST /api/enrollments -> runs as one JDBC transaction on the server
      const created = await enrollmentsApi.create({
        Session_ID: Number(form.Session_ID),
        Student_ID: Number(form.Student_ID),
      });
      notify(`${created.Student_Name} enrolled in session ${created.Session_ID}.`);
      setForm((f) => ({ ...f, Student_ID: '' }));
      enrollments.reload();
      sessions.reload();
    } catch (err) {
      setFormError(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function remove() {
    await enrollmentsApi.remove(removing.Session_ID, removing.Student_ID);
    notify('Enrollment removed.');
    setRemoving(null);
    enrollments.reload();
    sessions.reload();
  }

  const columns = [
    { key: 'Session_ID', label: 'Session', width: '64px' },
    {
      key: 'Tr_Name', label: 'Training',
      render: (r) => (
        <>
          <strong className="cell-strong">{r.Tr_Name}</strong>
          <div className="cell-sub">{formatDate(r.Session_Date)}, {formatTime(r.Start_Time)}–{formatTime(r.End_Time)}</div>
        </>
      ),
    },
    {
      key: 'Student_Name', label: 'Student',
      render: (r) => (
        <>
          {r.Student_Name} <span className="muted">({r.Student_ID})</span>
          {r.Dept_Name && <div className="cell-sub">{r.Dept_Name}</div>}
        </>
      ),
    },
    { key: 'Attendance_Status', label: 'Attendance', render: (r) => <StatusBadge value={r.Attendance_Status} /> },
    { key: 'Enrolled_At', label: 'Enrolled at', nowrap: true, render: (r) => formatDateTime(r.Enrolled_At) },
    {
      key: '__actions', label: 'Actions', width: '1%',
      render: (r) => (
        <div className="row-actions">
          <button type="button" className="link-btn link-danger" onClick={() => setRemoving(r)}>Remove</button>
        </div>
      ),
    },
  ];

  const sessionOptions = sessions.data || [];
  const rows = enrollments.data || [];

  return (
    <>
      <PageHeader
        title="Enrollments"
        description="Enrolling a student runs one JDBC transaction: checks, insert and history row are committed together or rolled back."
      />

      <section className="panel">
        <h2 className="panel-title">Enroll a student</h2>
        <form className="inline-form" onSubmit={enroll} noValidate>
          <div className="field">
            <label htmlFor="enroll-session">Session <span className="req">*</span></label>
            <select
              id="enroll-session"
              value={form.Session_ID}
              onChange={(e) => setForm({ ...form, Session_ID: e.target.value })}
            >
              <option value="">Select a session…</option>
              {sessionOptions.map((s) => (
                <option key={s.Session_ID} value={s.Session_ID}>
                  {sessionLabel(s)} ({s.Enrolled_Count} enrolled)
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="enroll-student">Student <span className="req">*</span></label>
            <select
              id="enroll-student"
              value={form.Student_ID}
              onChange={(e) => setForm({ ...form, Student_ID: e.target.value })}
            >
              <option value="">Select a student…</option>
              {(students.data || []).map((s) => (
                <option key={s.Student_ID} value={s.Student_ID}>
                  {s.Student_ID} – {s.Name}{s.Dept_Name ? ` (${s.Dept_Name})` : ''}
                </option>
              ))}
            </select>
          </div>
          <div className="field field-button">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Enrolling…' : 'Enroll student'}
            </button>
          </div>
        </form>
        {formError && <div className="alert alert-error">{formError}</div>}
      </section>

      <div className="toolbar">
        <label htmlFor="filter-session" className="toolbar-label">Show</label>
        <select id="filter-session" value={filterSession} onChange={(e) => setFilterSession(e.target.value)}>
          <option value="">All sessions</option>
          {sessionOptions.map((s) => (
            <option key={s.Session_ID} value={s.Session_ID}>{sessionLabel(s)}</option>
          ))}
        </select>
        <span className="count">
          {enrollments.loading ? 'Loading…' : `${rows.length} ${rows.length === 1 ? 'enrollment' : 'enrollments'}`}
        </span>
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        rowKey={(r) => `${r.Session_ID}-${r.Student_ID}`}
        loading={enrollments.loading}
        error={enrollments.error}
        emptyText="No enrollments yet."
      />

      <ConfirmDialog
        open={removing !== null}
        title="Remove enrollment"
        confirmLabel="Remove"
        message={removing ? `Remove ${removing.Student_Name} from session ${removing.Session_ID} (${removing.Tr_Name})?` : ''}
        onConfirm={remove}
        onClose={() => setRemoving(null)}
      />
    </>
  );
}
