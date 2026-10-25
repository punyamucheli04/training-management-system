import { useState } from 'react';
import ResourcePage from '../components/ResourcePage.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { sessionsApi, trainersApi, trainingsApi } from '../lib/api.js';
import { formatDate, formatTime } from '../lib/format.js';
import { navigate } from '../lib/router.js';
import { useFetch } from '../lib/useFetch.js';

const FILTERS = [
  { value: '', label: 'All' },
  { value: 'upcoming', label: 'Upcoming' },
  { value: 'completed', label: 'Completed' },
];

export default function SessionsPage() {
  const trainings = useFetch(() => trainingsApi.list(), []);
  const trainers = useFetch(() => trainersApi.list(), []);
  const [status, setStatus] = useState('');

  const fields = [
    { name: 'Session_Date', label: 'Date', type: 'date', required: true },
    {
      name: 'Training_ID', label: 'Training', type: 'select', required: true, numeric: true,
      options: (trainings.data || []).map((t) => ({ value: t.Training_ID, label: `${t.Training_ID} – ${t.Tr_Name}` })),
    },
    { name: 'Start_Time', label: 'Start time', type: 'time', required: true },
    { name: 'End_Time', label: 'End time', type: 'time', required: true },
    {
      name: 'Trainer_ID', label: 'Trainer', type: 'select', required: true, numeric: true, full: true,
      options: (trainers.data || []).map((t) => ({
        value: t.Trainer_ID,
        label: `${t.Trainer_ID} – ${t.Trainer_Name}${t.Expertise ? ` (${t.Expertise})` : ''}`,
      })),
      hint: 'A trainer cannot have two sessions at overlapping times (checked by a database trigger).',
    },
  ];

  const columns = [
    { key: 'Session_ID', label: 'ID', width: '56px' },
    { key: 'Session_Date', label: 'Date', nowrap: true, render: (r) => formatDate(r.Session_Date) },
    { key: 'Start_Time', label: 'Start', render: (r) => formatTime(r.Start_Time) },
    { key: 'End_Time', label: 'End', render: (r) => formatTime(r.End_Time) },
    { key: 'Tr_Name', label: 'Training', render: (r) => <strong className="cell-strong">{r.Tr_Name}</strong> },
    { key: 'Trainer_Name', label: 'Trainer' },
    { key: 'Enrolled_Count', label: 'Students', align: 'right' },
    { key: 'Session_Status', label: 'Status', render: (r) => <StatusBadge value={r.Session_Status} /> },
  ];

  const filterControl = (
    <div className="segmented" role="group" aria-label="Filter sessions">
      {FILTERS.map((f) => (
        <button
          key={f.value}
          type="button"
          className={status === f.value ? 'active' : ''}
          aria-pressed={status === f.value}
          onClick={() => setStatus(f.value)}
        >
          {f.label}
        </button>
      ))}
    </div>
  );

  return (
    <ResourcePage
      title="Sessions"
      description="Scheduled classes. Training and trainer names come from the view session_details_view."
      entityName="session"
      api={sessionsApi}
      idKey="Session_ID"
      columns={columns}
      fields={fields}
      listParams={{ status }}
      filters={filterControl}
      searchPlaceholder="Search by training or trainer"
      validate={(v) =>
        v.Start_Time && v.End_Time && v.End_Time <= v.Start_Time ? { End_Time: 'End time must be after the start time.' } : {}}
      deleteMessage={(r) =>
        `Delete session ${r.Session_ID} (${r.Tr_Name}, ${formatDate(r.Session_Date)})? Its ${r.Enrolled_Count} enrollment(s) will also be removed.`}
      rowActions={(r) => (
        <button type="button" className="link-btn" onClick={() => navigate(`/attendance?session=${r.Session_ID}`)}>
          Register
        </button>
      )}
    />
  );
}
