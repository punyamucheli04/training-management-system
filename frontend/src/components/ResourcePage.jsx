import { useMemo, useState } from 'react';
import { useDebounced, useFetch } from '../lib/useFetch.js';
import ConfirmDialog from './ConfirmDialog.jsx';
import DataTable from './DataTable.jsx';
import Modal from './Modal.jsx';
import PageHeader from './PageHeader.jsx';
import RecordForm, { toFormValues } from './RecordForm.jsx';
import { useToast } from './Toast.jsx';

/**
 * Reusable list + add / edit / delete page used by Students, Trainers,
 * Departments, Trainings and Sessions.
 *
 * Every action calls the REST API and then reloads the table, so the
 * browser never has to be refreshed manually.
 */
export default function ResourcePage({
  title,
  description,
  entityName,           // "student"
  api,                  // { list, create, update, remove }
  idKey,                // "Student_ID"
  columns,
  fields,
  validate,
  searchMode = 'server', // 'server' sends ?search=..., 'client' filters loaded rows
  searchPlaceholder = 'Search…',
  clientSearchKeys = [],
  listParams = {},
  filters = null,       // extra controls shown next to the search box
  deleteMessage,
  rowActions,           // (row) => extra buttons
  formTitle,
}) {
  const notify = useToast();
  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounced(search);
  const [editing, setEditing] = useState(null); // null = closed, {} = new, row = edit
  const [deleting, setDeleting] = useState(null);

  const serverSearch = searchMode === 'server' ? debouncedSearch : '';
  const paramsKey = JSON.stringify(listParams);
  const { data, error, loading, reload } = useFetch(
    () => api.list({ ...listParams, search: serverSearch }),
    [serverSearch, paramsKey]
  );

  const rows = useMemo(() => {
    const list = data || [];
    if (searchMode !== 'client' || !search.trim()) return list;
    const q = search.trim().toLowerCase();
    return list.filter((row) => clientSearchKeys.some((k) => String(row[k] ?? '').toLowerCase().includes(q)));
  }, [data, search, searchMode, clientSearchKeys]);

  const isEdit = editing && editing[idKey] !== undefined;
  const capitalised = entityName.charAt(0).toUpperCase() + entityName.slice(1);

  async function save(payload) {
    if (isEdit) {
      await api.update(editing[idKey], payload);
      notify(`${capitalised} updated.`);
    } else {
      await api.create(payload);
      notify(`${capitalised} added.`);
    }
    setEditing(null);
    reload();
  }

  async function remove() {
    await api.remove(deleting[idKey]);
    notify(`${capitalised} deleted.`);
    setDeleting(null);
    reload();
  }

  const tableColumns = [
    ...columns,
    {
      key: '__actions',
      label: 'Actions',
      width: '1%',
      render: (row) => (
        <div className="row-actions">
          {rowActions && rowActions(row)}
          <button type="button" className="link-btn" onClick={() => setEditing(row)}>Edit</button>
          <button type="button" className="link-btn link-danger" onClick={() => setDeleting(row)}>Delete</button>
        </div>
      ),
    },
  ];

  return (
    <>
      <PageHeader title={title} description={description}>
        <button type="button" className="btn btn-primary" onClick={() => setEditing({})}>
          Add {entityName}
        </button>
      </PageHeader>

      <div className="toolbar">
        <input
          type="search"
          className="search"
          placeholder={searchPlaceholder}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          aria-label={`Search ${entityName}s`}
        />
        {filters}
        <span className="count">
          {loading ? 'Loading…' : `${rows.length} ${rows.length === 1 ? 'record' : 'records'}`}
        </span>
      </div>

      <DataTable
        columns={tableColumns}
        rows={rows}
        rowKey={(r) => r[idKey]}
        loading={loading}
        error={error}
        emptyText={search ? `No ${entityName}s match “${search}”.` : `No ${entityName}s yet. Use “Add ${entityName}” to create one.`}
      />

      <Modal
        open={editing !== null}
        title={isEdit ? `Edit ${entityName} ${editing[idKey]}` : formTitle || `Add ${entityName}`}
        onClose={() => setEditing(null)}
        wide
      >
        {editing !== null && (
          <RecordForm
            key={isEdit ? editing[idKey] : 'new'}
            fields={fields}
            initialValues={toFormValues(fields, isEdit ? editing : null)}
            isEdit={isEdit}
            validate={validate}
            onSubmit={save}
            onCancel={() => setEditing(null)}
            submitLabel={isEdit ? 'Save changes' : `Add ${entityName}`}
          />
        )}
      </Modal>

      <ConfirmDialog
        open={deleting !== null}
        title={`Delete ${entityName}`}
        message={deleting ? deleteMessage(deleting) : ''}
        onConfirm={remove}
        onClose={() => setDeleting(null)}
      />
    </>
  );
}
