import { useState } from 'react';
import Bar from '../components/Bar.jsx';
import DataTable from '../components/DataTable.jsx';
import Modal from '../components/Modal.jsx';
import ResourcePage from '../components/ResourcePage.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { departmentsApi, studentsApi } from '../lib/api.js';
import { formatDate, formatNumber, formatPct, formatTime, todayISO } from '../lib/format.js';
import { useFetch } from '../lib/useFetch.js';

export default function StudentsPage() {
  const departments = useFetch(() => departmentsApi.list(), []);
  const [historyFor, setHistoryFor] = useState(null);

  const deptOptions = (departments.data || []).map((d) => ({ value: d.Dept_ID, label: `${d.Dept_ID} – ${d.Dept_Name}` }));
  const today = todayISO();

  const fields = [
    { name: 'Name', label: 'Name', required: true, maxLength: 50 },
    {
      name: 'Gender', label: 'Gender', type: 'select',
      options: [{ value: 'Male', label: 'Male' }, { value: 'Female', label: 'Female' }, { value: 'Other', label: 'Other' }],
    },
    { name: 'DOB', label: 'Date of birth', type: 'date', max: today },
    { name: 'Email', label: 'Email', type: 'email', maxLength: 100 },
    {
      name: 'Mobile_Number', label: 'Mobile number', maxLength: 50,
      pattern: /^[0-9+\-() ]{7,20}$/, patternMessage: 'Use 7–20 digits (spaces, +, - allowed).',
    },
    { name: 'CGPA', label: 'CGPA', type: 'number', min: 0, max: 10, step: '0.01', hint: 'Between 0 and 10' },
    { name: 'Dept_ID', label: 'Department', type: 'select', options: deptOptions, numeric: true, full: true },
  ];

  const columns = [
    { key: 'Student_ID', label: 'ID', width: '56px' },
    { key: 'Name', label: 'Name', render: (r) => <strong className="cell-strong">{r.Name}</strong> },
    { key: 'Gender', label: 'Gender' },
    { key: 'Email', label: 'Email' },
    { key: 'Mobile_Number', label: 'Mobile' },
    { key: 'CGPA', label: 'CGPA', align: 'right', render: (r) => formatNumber(r.CGPA) },
    { key: 'Dept_Name', label: 'Department', render: (r) => r.Dept_Name || <span className="muted">—</span> },
    { key: 'Attendance_Pct', label: 'Attendance', render: (r) => <Bar value={r.Attendance_Pct} /> },
  ];

  return (
    <>
      <ResourcePage
        title="Students"
        description="Student records with their department. Attendance % is calculated by the MySQL function calculate_attendance_percentage()."
        entityName="student"
        api={studentsApi}
        idKey="Student_ID"
        columns={columns}
        fields={fields}
        searchPlaceholder="Search by name, email, mobile or department"
        validate={(v) => (v.DOB && v.DOB >= today ? { DOB: 'Date of birth must be in the past.' } : {})}
        deleteMessage={(r) =>
          `Delete ${r.Name} (ID ${r.Student_ID})? Their session enrollments will also be removed. This cannot be undone.`}
        rowActions={(r) => (
          <button type="button" className="link-btn" onClick={() => setHistoryFor(r)}>Attendance</button>
        )}
      />
      <AttendanceHistory student={historyFor} onClose={() => setHistoryFor(null)} />
    </>
  );
}

/** Uses GET /api/students/{id}/attendance, which calls the stored procedure GetStudentAttendance. */
function AttendanceHistory({ student, onClose }) {
  const id = student ? student.Student_ID : null;
  const { data, error, loading } = useFetch(() => (id ? studentsApi.attendance(id) : Promise.resolve(null)), [id]);

  const columns = [
    { key: 'Session_ID', label: 'Session' },
    { key: 'Session_Date', label: 'Date', nowrap: true, render: (r) => formatDate(r.Session_Date) },
    { key: 'Start_Time', label: 'Time', nowrap: true, render: (r) => `${formatTime(r.Start_Time)}–${formatTime(r.End_Time)}` },
    { key: 'Tr_Name', label: 'Training' },
    { key: 'Trainer_Name', label: 'Trainer' },
    { key: 'Attendance_Status', label: 'Attendance', render: (r) => <StatusBadge value={r.Attendance_Status} /> },
  ];

  return (
    <Modal open={student !== null} title={student ? `Attendance of ${student.Name}` : ''} onClose={onClose} wide>
      {data && (
        <p className="modal-note">
          Overall attendance: <strong>{formatPct(data.Attendance_Pct)}</strong>
          <span className="muted"> (from CALL GetStudentAttendance({id}) and calculate_attendance_percentage)</span>
        </p>
      )}
      <DataTable
        columns={columns}
        rows={data ? data.Sessions : []}
        rowKey={(r) => r.Session_ID}
        loading={loading}
        error={error}
        emptyText="This student is not enrolled in any session."
      />
    </Modal>
  );
}
