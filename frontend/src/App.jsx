import { useEffect, useState } from 'react';
import ChangePasswordDialog from './components/ChangePasswordDialog.jsx';
import { ToastProvider } from './components/Toast.jsx';
import { AUTH_EXPIRED_EVENT, authApi, dashboardApi } from './lib/api.js';
import { useRoute } from './lib/router.js';
import { useFetch } from './lib/useFetch.js';
import AttendancePage from './pages/AttendancePage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import DepartmentsPage from './pages/DepartmentsPage.jsx';
import EnrollmentsPage from './pages/EnrollmentsPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import ReportsPage from './pages/ReportsPage.jsx';
import SessionsPage from './pages/SessionsPage.jsx';
import StudentsPage from './pages/StudentsPage.jsx';
import TrainersPage from './pages/TrainersPage.jsx';
import TrainingsPage from './pages/TrainingsPage.jsx';

const NAV = [
  { path: '/', label: 'Dashboard', page: DashboardPage },
  { path: '/students', label: 'Students', page: StudentsPage },
  { path: '/trainers', label: 'Trainers', page: TrainersPage },
  { path: '/departments', label: 'Departments', page: DepartmentsPage },
  { path: '/trainings', label: 'Trainings', page: TrainingsPage },
  { path: '/sessions', label: 'Sessions', page: SessionsPage },
  { path: '/enrollments', label: 'Enrollments', page: EnrollmentsPage },
  { path: '/attendance', label: 'Attendance', page: AttendancePage },
  { path: '/reports', label: 'SQL reports', page: ReportsPage },
];

export default function App() {
  // undefined = still checking, null = not logged in, object = logged-in user
  const [user, setUser] = useState(undefined);

  useEffect(() => {
    authApi.me().then(setUser).catch(() => setUser(null));
    const onExpired = () => setUser(null);
    window.addEventListener(AUTH_EXPIRED_EVENT, onExpired);
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, onExpired);
  }, []);

  async function logout() {
    try {
      await authApi.logout();
    } finally {
      setUser(null);
    }
  }

  if (user === undefined) {
    return <div className="login-screen"><p className="muted">Loading…</p></div>;
  }

  return (
    <ToastProvider>
      {user ? <MainApp user={user} onLogout={logout} /> : <LoginPage onLogin={setUser} />}
    </ToastProvider>
  );
}

function MainApp({ user, onLogout }) {
  const { path, params } = useRoute();
  const [changingPassword, setChangingPassword] = useState(false);
  const current = NAV.find((n) => n.path === path) || NAV[0];
  const Page = current.page;
  const health = useFetch(() => dashboardApi.health(), []);

  return (
    <>
      <div className="layout">
        <aside className="sidebar">
          <a className="brand" href="#/">
            <span className="brand-mark" aria-hidden="true">T</span>
            <span className="brand-text">
              <span className="brand-name">Placement Training</span>
              <span className="brand-sub">Management System</span>
            </span>
          </a>
          <nav className="nav" aria-label="Main">
            {NAV.map((n) => (
              <a key={n.path} href={`#${n.path}`} className={n === current ? 'active' : ''} aria-current={n === current ? 'page' : undefined}>
                {n.label}
              </a>
            ))}
          </nav>
          <div className="user-box">
            <span className="user-name">{user.Username}</span>
            {user.Role && <span className="user-role">{user.Role}</span>}
            <div className="user-actions">
              <button type="button" className="link-btn" onClick={() => setChangingPassword(true)}>Change password</button>
              <button type="button" className="link-btn" onClick={onLogout}>Log out</button>
            </div>
          </div>
          <ConnectionStatus health={health} />
        </aside>

        <main className="main">
          <MissingObjectsBanner health={health} />
          {/* key resets page state when the route changes */}
          <Page key={path} params={params} />
        </main>
      </div>
      <ChangePasswordDialog open={changingPassword} onClose={() => setChangingPassword(false)} />
    </>
  );
}

/** Shows whether Spring Boot can reach MySQL through JDBC (GET /api/health). */
function ConnectionStatus({ health }) {
  let text = 'Checking database…';
  let tone = 'wait';
  if (health.error) {
    text = 'Backend not reachable';
    tone = 'down';
  } else if (health.data && !health.data.connected) {
    text = 'MySQL not connected';
    tone = 'down';
  } else if (health.data) {
    text = `MySQL ${health.data.version || ''}`.trim();
    tone = 'up';
  }
  return (
    <div className={`conn conn-${tone}`} title={health.data?.database || health.error || ''}>
      <span className="conn-dot" aria-hidden="true" />
      <span>
        {text}
        {health.data?.database && <span className="conn-db">{health.data.database}</span>}
      </span>
    </div>
  );
}

function MissingObjectsBanner({ health }) {
  if (health.error) {
    return <div className="alert alert-error">{health.error}</div>;
  }
  if (health.data && !health.data.connected) {
    return <div className="alert alert-error">{health.data.message}</div>;
  }
  const missing = health.data?.missingObjects || [];
  if (missing.length === 0) return null;
  return (
    <div className="alert alert-warn">
      Some database objects are not installed yet: {missing.join(', ')}. Run the matching scripts from the
      database folder (README, step 2), then reload this page.
    </div>
  );
}
