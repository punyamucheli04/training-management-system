import ResourcePage from '../components/ResourcePage.jsx';
import { departmentsApi } from '../lib/api.js';
import { formatNumber } from '../lib/format.js';

export default function DepartmentsPage() {
  const fields = [
    {
      name: 'Dept_ID', label: 'Department ID', type: 'number', required: true, min: 1, max: 99999,
      integer: true, readOnlyOnEdit: true, hint: 'A number such as 106',
    },
    { name: 'Dept_Name', label: 'Department name', required: true, maxLength: 50 },
    { name: 'HOD_Name', label: 'Head of department', maxLength: 50, full: true },
  ];

  const columns = [
    { key: 'Dept_ID', label: 'ID', width: '64px' },
    { key: 'Dept_Name', label: 'Department', render: (r) => <strong className="cell-strong">{r.Dept_Name}</strong> },
    { key: 'HOD_Name', label: 'Head of department' },
    { key: 'Student_Count', label: 'Students', align: 'right' },
    { key: 'Avg_CGPA', label: 'Average CGPA', align: 'right', render: (r) => formatNumber(r.Avg_CGPA) },
  ];

  return (
    <ResourcePage
      title="Departments"
      description="Student count and average CGPA are computed with LEFT JOIN, GROUP BY, COUNT and AVG."
      entityName="department"
      api={departmentsApi}
      idKey="Dept_ID"
      columns={columns}
      fields={fields}
      searchMode="client"
      clientSearchKeys={['Dept_ID', 'Dept_Name', 'HOD_Name']}
      searchPlaceholder="Search departments"
      deleteMessage={(r) => `Delete department ${r.Dept_Name} (ID ${r.Dept_ID})? A department that still has students cannot be deleted.`}
    />
  );
}
