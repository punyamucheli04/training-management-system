import ResourcePage from '../components/ResourcePage.jsx';
import { trainersApi } from '../lib/api.js';

export default function TrainersPage() {
  const fields = [
    { name: 'Trainer_Name', label: 'Trainer name', required: true, maxLength: 50, full: true },
    { name: 'Email', label: 'Email', type: 'email', maxLength: 100 },
    { name: 'Expertise', label: 'Expertise', maxLength: 50, placeholder: 'e.g. Python' },
  ];

  const columns = [
    { key: 'Trainer_ID', label: 'ID', width: '56px' },
    { key: 'Trainer_Name', label: 'Name', render: (r) => <strong className="cell-strong">{r.Trainer_Name}</strong> },
    { key: 'Email', label: 'Email' },
    { key: 'Expertise', label: 'Expertise' },
    { key: 'Session_Count', label: 'Sessions', align: 'right' },
  ];

  return (
    <ResourcePage
      title="Trainers"
      description="People who conduct training sessions."
      entityName="trainer"
      api={trainersApi}
      idKey="Trainer_ID"
      columns={columns}
      fields={fields}
      searchPlaceholder="Search by name, email or expertise"
      deleteMessage={(r) => `Delete trainer ${r.Trainer_Name} (ID ${r.Trainer_ID})? A trainer who has sessions cannot be deleted.`}
    />
  );
}
