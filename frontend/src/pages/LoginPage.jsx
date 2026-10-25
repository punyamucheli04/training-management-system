import { useState } from 'react';
import { authApi } from '../lib/api.js';

/** Shown when nobody is logged in. Checks the username/password against the users table. */
export default function LoginPage({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    if (!username.trim() || !password) {
      setError('Enter your username and password.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const user = await authApi.login(username.trim(), password);
      onLogin(user);
    } catch (err) {
      setError(err.message);
      setPassword('');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login-screen">
      <header className="login-hero">
        <p className="login-kicker">Training &amp; Placement Cell</p>
        <h1 className="login-headline">
          <span>Placement</span>
          <span>Training</span>
          <span>Management</span>
          <span>System</span>
        </h1>
        <p className="login-tagline">
          Students, trainers, sessions and attendance, kept in one MySQL database.
        </p>
      </header>

      <main className="login-side">
      <div className="login-box">
        <h2 className="login-title">Log in</h2>
        <p className="login-desc">Use your account from the users table.</p>

        {error && <div className="alert alert-error" role="alert">{error}</div>}

        <form className="login-form" onSubmit={submit} noValidate>
          <div className="field">
            <label htmlFor="login-username">Username</label>
            <input
              id="login-username"
              autoComplete="username"
              autoFocus
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </div>
          <div className="field">
            <label htmlFor="login-password">Password</label>
            <input
              id="login-password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
            {busy ? 'Logging in…' : 'Log in'}
          </button>
        </form>
      </div>
      </main>
    </div>
  );
}
