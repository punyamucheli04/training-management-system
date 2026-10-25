import { useMemo, useState } from 'react';
import Bar from '../components/Bar.jsx';
import DataTable from '../components/DataTable.jsx';
import PageHeader from '../components/PageHeader.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { useToast } from '../components/Toast.jsx';
import { attendanceApi, enrollmentsApi, sessionsApi } from '../lib/api.js';
import { formatDate, formatDateTime, formatTime, sessionLabel } from '../lib/format.js';
import { navigate } from '../lib/router.js';
import { useFetch } from '../lib/useFetch.js';

const STATUSES = ['Present', 'Absent', 'Pending'];

export default function AttendancePage({ params }) {
  const notify = useToast();
  const [busyRow, setBusyRow] = useState(null);
  const [bulkBusy, setBulkBusy] = useState(false);

  const sessions = useFetch(() => sessionsApi.list(), []);
  const byStudent = useFetch(() => attendanceApi.byStudent(), []);
  const history = useFetch(() => attendanceApi.history(), []);

  // Selected session: from the URL (#/attendance?session=401) or the most recent held session
  const sessionId = useMemo(() => {
    if (params.session) return Number(params.session);
    const list = sessions.data || [];
    const held = list.find((s) => s.Session_Status !== 'Upcoming');
    return held ? held.Session_ID : list[0]?.Session_ID ?? null;
  }, [params.session, sessions.data]);

  const session = (sessions.data || []).find((s) => s.Session_ID === sessionId) || null;

  const register = useFetch(
    () => (sessionId ? enrollmentsApi.list({ sessionId }) : Promise.resolve([])),
    [sessionId]
  );
  const summary = useFetch(
    () => (sessionId ? sessionsApi.attendanceSummary(sessionId) : Promise.resolve(null)),
    [sessionId]
  );

  function refreshAll() {
    register.reload();
    summary.reload();
    byStudent.reload();
    history.reload();
    sessions.reload();
  }

  async function mark(row, status) {
    if (row.Attendance_Status === status) return;
    setBusyRow(row.Student_ID);
    try {
      // PUT /api/enrollments/{sessionId}/{studentId}/attendance
      await enrollmentsApi.updateAttendance(row.Session_ID, row.Student_ID, status);
      notify(`${row.Student_Name} marked ${status}.`);
      refreshAll();
    } catch (err) {
      notify(err.message, 'error');
    } finally {
      setBusyRow(null);
    }
  }

  async function markAllPresent() {
    const rows = register.data || [];
    if (rows.length === 0) return;
    setBulkBusy(true);
    try {
      // One request, one database transaction: all rows are saved or none.
      await sessionsApi.saveAttendance(
        sessionId,
        rows.map((r) => ({ Student_ID: r.Student_ID, Attendance_Status: 'Present' }))
      );
      notify(`All ${rows.length} students marked Present.`);
      refreshAll();
    } catch (err) {
      notify(err.message, 'error');
    } finally {
      setBulkBusy(false);
    }
  }

  const registerColumns = [
    { key: 'Student_ID', label: 'ID', width: '56px' },
    { key: 'Student_Name', label: 'Student', render: (r) => <strong className="cell-strong">{r.Student_Name}</strong> },
    { key: 'Dept_Name', label: 'Department' },
    { key: 'Attendance_Status', label: 'Current', render: (r) => <StatusBadge value={r.Attendance_Status} /> },
    {
      key: '__mark', label: 'Mark as', width: '1%',
      render: (r) => (
        <div className="segmented segmented-small" role="group" aria-label={`Attendance for ${r.Student_Name}`}>
          {STATUSES.map((s) => (
            <button
              key={s}
              type="button"
              className={r.Attendance_Status === s ? `active active-${s.toLowerCase()}` : ''}
              aria-pressed={r.Attendance_Status === s}
              disabled={busyRow === r.Student_ID}
              onClick={() => mark(r, s)}
            >
              {s}
            </button>
          ))}
        </div>
      ),
    },
  ];

  const studentColumns = [
    { key: 'Student_ID', label: 'ID', width: '56px' },
    { key: 'Name', label: 'Student', render: (r) => <strong className="cell-strong">{r.Name}</strong> },
    { key: 'Dept_Name', label: 'Department' },
    { key: 'Enrolled_Sessions', label: 'Sessions', align: 'right' },
    { key: 'Present_Count', label: 'Present', align: 'right' },
    { key: 'Absent_Count', label: 'Absent', align: 'right' },
    { key: 'Attendance_Pct', label: 'Attendance', render: (r) => <Bar value={r.Attendance_Pct} /> },
  ];

  const historyColumns = [
    { key: 'Changed_At', label: 'When', nowrap: true, render: (r) => formatDateTime(r.Changed_At) },
    { key: 'Student_Name', label: 'Student', render: (r) => r.Student_Name || `ID ${r.Student_ID}` },
    { key: 'Session_ID', label: 'Session', render: (r) => `${r.Session_ID}${r.Tr_Name ? ` – ${r.Tr_Name}` : ''}` },
    {
      key: 'New_Status', label: 'Change',
      render: (r) =>
        r.Old_Status ? (
          <span className="change"><StatusBadge value={r.Old_Status} /> <span aria-label="changed to">→</span> <StatusBadge value={r.New_Status} /></span>
        ) : (
          <span className="change">Enrolled as <StatusBadge value={r.New_Status} /></span>
        ),
    },
    { key: 'Changed_By', label: 'By', render: (r) => <span className="muted">{r.Changed_By}</span> },
  ];

  return (
    <>
      <PageHeader
        title="Attendance"
        description="Mark attendance session by session. MySQL triggers validate every change and record it in the attendance history."
      />

      <section className="panel">
        <div className="register-head">
          <div className="field">
            <label htmlFor="register-session">Session</label>
            <select
              id="register-session"
              value={sessionId ?? ''}
              onChange={(e) => navigate(`/attendance?session=${e.target.value}`)}
            >
              {(sessions.data || []).map((s) => (
                <option key={s.Session_ID} value={s.Session_ID}>{sessionLabel(s)}</option>
              ))}
            </select>
          </div>
          {summary.data && (
            <dl className="summary">
              <div><dt>Present</dt><dd>{summary.data.Present_Count}</dd></div>
              <div><dt>Absent</dt><dd>{summary.data.Absent_Count}</dd></div>
              <div><dt>Pending</dt><dd>{summary.data.Pending_Count}</dd></div>
            </dl>
          )}
          <button
            type="button"
            className="btn"
            onClick={markAllPresent}
            disabled={bulkBusy || !register.data || register.data.length === 0}
          >
            {bulkBusy ? 'Saving…' : 'Mark all present'}
          </button>
        </div>

        {session && (
          <p className="register-meta">
            {session.Tr_Name} with {session.Trainer_Name}, {formatDate(session.Session_Date)},{' '}
            {formatTime(session.Start_Time)}–{formatTime(session.End_Time)} <StatusBadge value={session.Session_Status} />
            {session.Session_Status === 'Upcoming' && (
              <span className="note">
                This session has not taken place yet. The database trigger will refuse Present or Absent until its date.
              </span>
            )}
          </p>
        )}

        <DataTable
          columns={registerColumns}
          rows={register.data || []}
          rowKey={(r) => r.Student_ID}
          loading={register.loading || sessions.loading}
          error={register.error || sessions.error}
          emptyText={sessionId ? 'No students are enrolled in this session. Enroll them on the Enrollments page.' : 'No sessions yet.'}
        />
      </section>

      <div className="stack">
        <section>
          <h2 className="section-title">Attendance by student</h2>
          <p className="section-desc">Present ÷ (Present + Absent), calculated in SQL with GROUP BY and SUM(CASE …).</p>
          <DataTable
            columns={studentColumns}
            rows={byStudent.data || []}
            rowKey={(r) => r.Student_ID}
            loading={byStudent.loading}
            error={byStudent.error}
          />
        </section>
        <section>
          <h2 className="section-title">Recent attendance changes</h2>
          <p className="section-desc">Written to attendance_audit by the trigger trg_enrollment_after_update.</p>
          <DataTable
            columns={historyColumns}
            rows={history.data || []}
            rowKey={(r) => r.Audit_ID}
            loading={history.loading}
            error={history.error}
            emptyText="No changes recorded yet."
          />
        </section>
      </div>
    </>
  );
}
