import ResourcePage from '../components/ResourcePage.jsx';
import { trainingsApi } from '../lib/api.js';

export default function TrainingsPage() {
  const fields = [
    { name: 'Tr_Name', label: 'Training name', required: true, maxLength: 50 },
    {
      name: 'Tr_Type', label: 'Type', type: 'select',
      options: [
        { value: 'Technical', label: 'Technical' },
        { value: 'Soft Skill', label: 'Soft Skill' },
        { value: 'Aptitude', label: 'Aptitude' },
        { value: 'Workshop', label: 'Workshop' },
      ],
    },
    { name: 'Duration_Hrs', label: 'Duration (hours)', type: 'number', min: 1, max: 1000, integer: true },
    { name: 'Description', label: 'Description', type: 'textarea', maxLength: 100, full: true, hint: 'Up to 100 characters' },
  ];

  const columns = [
    { key: 'Training_ID', label: 'ID', width: '56px' },
    { key: 'Tr_Name', label: 'Training', render: (r) => <strong className="cell-strong">{r.Tr_Name}</strong> },
    { key: 'Tr_Type', label: 'Type' },
    { key: 'Description', label: 'Description' },
    { key: 'Duration_Hrs', label: 'Duration', align: 'right', render: (r) => (r.Duration_Hrs ? `${r.Duration_Hrs} h` : '—') },
    { key: 'Session_Count', label: 'Sessions', align: 'right' },
    { key: 'Enrollment_Count', label: 'Enrollments', align: 'right' },
  ];

  return (
    <ResourcePage
      title="Trainings"
      description="Training programmes offered to students."
      entityName="training"
      api={trainingsApi}
      idKey="Training_ID"
      columns={columns}
      fields={fields}
      searchPlaceholder="Search by name, type or description"
      deleteMessage={(r) => `Delete training ${r.Tr_Name} (ID ${r.Training_ID})? A training that has sessions cannot be deleted.`}
    />
  );
}
