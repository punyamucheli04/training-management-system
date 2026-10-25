import { useState } from 'react';
import DataTable from '../components/DataTable.jsx';
import PageHeader from '../components/PageHeader.jsx';
import { reportsApi } from '../lib/api.js';
import { useFetch } from '../lib/useFetch.js';

/** Shows each SQL demonstration query, the SQL text, and its live result. */
export default function ReportsPage() {
  const reports = useFetch(() => reportsApi.list(), []);
  const [selected, setSelected] = useState(null);
  const key = selected || reports.data?.[0]?.key || null;
  const result = useFetch(() => (key ? reportsApi.run(key) : Promise.resolve(null)), [key]);

  const rows = result.data ? result.data.rows : [];
  const columns = rows.length > 0
    ? Object.keys(rows[0]).map((col) => ({
        key: col,
        label: col.replace(/_/g, ' '),
        align: typeof rows[0][col] === 'number' ? 'right' : undefined,
        render: (r) => (r[col] === null ? <span className="muted">NULL</span> : String(r[col])),
      }))
    : [{ key: 'none', label: 'Result' }];

  return (
    <>
      <PageHeader
        title="SQL reports"
        description="Queries that demonstrate JOIN, GROUP BY, HAVING, aggregate functions, subqueries, a stored function and a stored procedure."
      />
      {reports.error && <div className="alert alert-error">{reports.error}</div>}

      <div className="reports">
        <nav className="report-list" aria-label="Reports">
          {(reports.data || []).map((r) => (
            <button
              key={r.key}
              type="button"
              className={r.key === key ? 'active' : ''}
              aria-current={r.key === key ? 'true' : undefined}
              onClick={() => setSelected(r.key)}
            >
              <span className="report-name">{r.title}</span>
              <span className="report-tech">{r.technique}</span>
            </button>
          ))}
        </nav>

        <section className="report-body">
          {result.data && (
            <>
              <h2 className="section-title">{result.data.title}</h2>
              <p className="section-desc">{result.data.description}</p>
              <pre className="sql"><code>{result.data.sql}</code></pre>
            </>
          )}
          <DataTable
            columns={columns}
            rows={rows}
            rowKey={(r) => JSON.stringify(r)}
            loading={result.loading}
            error={result.error}
            emptyText="The query returned no rows."
          />
        </section>
      </div>
    </>
  );
}
