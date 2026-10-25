import Bar from '../components/Bar.jsx';
import DataTable from '../components/DataTable.jsx';
import PageHeader from '../components/PageHeader.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { dashboardApi } from '../lib/api.js';
import { formatDate, formatDateTime, formatPct, formatTime } from '../lib/format.js';
import { useFetch } from '../lib/useFetch.js';

/** Every number on this page comes from GET /api/dashboard (SQL aggregates in MySQL). */
export default function DashboardPage() {
  const { data, error, loading } = useFetch(() => dashboardApi.get(), []);
  const t = data ? data.totals : {};

  const counters = [
    { label: 'Students', value: t.Total_Students, href: '#/students' },
    { label: 'Trainers', value: t.Total_Trainers, href: '#/trainers' },
    { label: 'Trainings', value: t.Total_Trainings, href: '#/trainings' },
    { label: 'Sessions', value: t.Total_Sessions, note: t.Upcoming_Sessions !== undefined ? `${t.Upcoming_Sessions} upcoming` : null, href: '#/sessions' },
    { label: 'Enrollments', value: t.Total_Enrollments, href: '#/enrollments' },
    { label: 'Present', value: t.Present_Count, tone: 'present', href: '#/attendance' },
    { label: 'Absent', value: t.Absent_Count, tone: 'absent', href: '#/attendance' },
    { label: 'Attendance rate', value: formatPct(t.Overall_Attendance_Pct), note: t.Pending_Count !== undefined ? `${t.Pending_Count} pending` : null },
  ];

  const upcomingColumns = [
    { key: 'Session_Date', label: 'Date', nowrap: true, render: (r) => formatDate(r.Session_Date) },
    { key: 'Start_Time', label: 'Time', nowrap: true, render: (r) => `${formatTime(r.Start_Time)}–${formatTime(r.End_Time)}` },
    { key: 'Tr_Name', label: 'Training', render: (r) => <strong className="cell-strong">{r.Tr_Name}</strong> },
    { key: 'Trainer_Name', label: 'Trainer' },
    { key: 'Enrolled_Count', label: 'Students', align: 'right' },
  ];

  const recentColumns = [
    { key: 'Student_Name', label: 'Student' },
    { key: 'Tr_Name', label: 'Session', render: (r) => `${r.Session_ID} – ${r.Tr_Name}` },
    { key: 'Attendance_Status', label: 'Attendance', render: (r) => <StatusBadge value={r.Attendance_Status} /> },
    { key: 'Enrolled_At', label: 'Enrolled', nowrap: true, render: (r) => formatDateTime(r.Enrolled_At) },
  ];

  const programColumns = [
    { key: 'Tr_Name', label: 'Training', render: (r) => <strong className="cell-strong">{r.Tr_Name}</strong> },
    { key: 'Tr_Type', label: 'Type' },
    { key: 'Total_Sessions', label: 'Sessions', align: 'right' },
    { key: 'Total_Enrollments', label: 'Enrolled', align: 'right' },
    { key: 'Attendance_Pct', label: 'Attendance', render: (r) => <Bar value={r.Attendance_Pct} /> },
  ];

  const deptColumns = [
    { key: 'Dept_Name', label: 'Department', render: (r) => <strong className="cell-strong">{r.Dept_Name}</strong> },
    { key: 'Enrollments', label: 'Enrollments', align: 'right' },
    { key: 'Present_Count', label: 'Present', align: 'right' },
    { key: 'Absent_Count', label: 'Absent', align: 'right' },
    { key: 'Attendance_Pct', label: 'Attendance', render: (r) => <Bar value={r.Attendance_Pct} /> },
  ];

  return (
    <>
      <PageHeader title="Dashboard" description="Current figures from the training_management_system database." />

      {error && <div className="alert alert-error">{error}</div>}

      <div className="ledger" aria-busy={loading}>
        {counters.map((c) => {
          const content = (
            <>
              <span className={`ledger-value ${c.tone ? `tone-${c.tone}` : ''}`}>{loading ? '…' : c.value ?? '—'}</span>
              <span className="ledger-label">{c.label}</span>
              {c.note && !loading && <span className="ledger-note">{c.note}</span>}
            </>
          );
          return c.href ? (
            <a key={c.label} className="ledger-cell" href={c.href}>{content}</a>
          ) : (
            <div key={c.label} className="ledger-cell">{content}</div>
          );
        })}
      </div>

      <div className="two-col">
        <section>
          <h2 className="section-title">Upcoming sessions</h2>
          <DataTable columns={upcomingColumns} rows={data ? data.upcomingSessions : []} rowKey={(r) => r.Session_ID}
            loading={loading} emptyText="No upcoming sessions are scheduled." />
        </section>
        <section>
          <h2 className="section-title">Recent enrollments</h2>
          <DataTable columns={recentColumns} rows={data ? data.recentEnrollments : []}
            rowKey={(r) => `${r.Session_ID}-${r.Student_ID}`} loading={loading} emptyText="No enrollments yet." />
        </section>
        <section>
          <h2 className="section-title">Training programmes</h2>
          <p className="section-desc">From the stored procedure GetTrainingEnrollmentCount().</p>
          <DataTable columns={programColumns} rows={data ? data.trainingPrograms : []} rowKey={(r) => r.Training_ID}
            loading={loading} emptyText="No training programmes yet." />
        </section>
        <section>
          <h2 className="section-title">Attendance by department</h2>
          <p className="section-desc">Pending sessions are not counted in the percentage.</p>
          <DataTable columns={deptColumns} rows={data ? data.attendanceByDepartment : []} rowKey={(r) => r.Dept_ID}
            loading={loading} emptyText="No attendance has been recorded yet." />
        </section>
      </div>
    </>
  );
}
