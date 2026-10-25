import { useState } from 'react';
import { authApi } from '../lib/api.js';
import Modal from './Modal.jsx';
import { useToast } from './Toast.jsx';

const EMPTY = { current: '', next: '', repeat: '' };

export default function ChangePasswordDialog({ open, onClose }) {
  const notify = useToast();
  const [values, setValues] = useState(EMPTY);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  function close() {
    setValues(EMPTY);
    setError('');
    onClose();
  }

  async function submit(event) {
    event.preventDefault();
    if (!values.current || !values.next) {
      setError('Fill in your current and new password.');
      return;
    }
    if (values.next.length < 6) {
      setError('The new password must have at least 6 characters.');
      return;
    }
    if (values.next !== values.repeat) {
      setError('The two new passwords do not match.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      await authApi.changePassword(values.current, values.next);
      notify('Password changed.');
      close();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const field = (key, label, autoComplete) => (
    <div className="field field-full">
      <label htmlFor={`pw-${key}`}>{label}</label>
      <input
        id={`pw-${key}`}
        type="password"
        autoComplete={autoComplete}
        value={values[key]}
        onChange={(e) => setValues({ ...values, [key]: e.target.value })}
      />
    </div>
  );

  return (
    <Modal open={open} title="Change password" onClose={close}>
      <form className="form" onSubmit={submit} noValidate>
        {error && <div className="alert alert-error">{error}</div>}
        <div className="form-grid">
          {field('current', 'Current password', 'current-password')}
          {field('next', 'New password', 'new-password')}
          {field('repeat', 'Repeat new password', 'new-password')}
        </div>
        <div className="form-actions">
          <button type="button" className="btn" onClick={close}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={busy}>
            {busy ? 'Saving…' : 'Change password'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
