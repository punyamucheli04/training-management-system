import { useState } from 'react';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Builds a form from a list of field definitions.
 *
 * Field names are the exact database column / JSON names (e.g. "Dept_ID"),
 * so the object sent to the backend needs no renaming.
 *
 * field: { name, label, type: text|email|number|date|time|select|textarea,
 *          required, maxLength, min, max, step, options, hint, readOnlyOnEdit, full }
 */
export function toFormValues(fields, record) {
  const values = {};
  fields.forEach((f) => {
    let v = record ? record[f.name] : '';
    if (v === null || v === undefined) v = '';
    if (f.type === 'time' && v) v = String(v).slice(0, 5);
    values[f.name] = String(v);
  });
  return values;
}

function toPayload(fields, values) {
  const payload = {};
  fields.forEach((f) => {
    const raw = typeof values[f.name] === 'string' ? values[f.name].trim() : values[f.name];
    if (raw === '' || raw === undefined) {
      payload[f.name] = null;
    } else if (f.type === 'number' || f.numeric) {
      payload[f.name] = Number(raw);
    } else {
      payload[f.name] = raw;
    }
  });
  return payload;
}

function validateFields(fields, values) {
  const errors = {};
  fields.forEach((f) => {
    const v = (values[f.name] ?? '').trim();
    if (f.required && v === '') {
      errors[f.name] = `${f.label} is required.`;
      return;
    }
    if (v === '') return;
    if (f.maxLength && v.length > f.maxLength) errors[f.name] = `At most ${f.maxLength} characters.`;
    if (f.type === 'email' && !EMAIL_PATTERN.test(v)) errors[f.name] = 'Enter a valid email address.';
    if (f.type === 'number') {
      const n = Number(v);
      if (Number.isNaN(n)) errors[f.name] = 'Enter a number.';
      else if (f.min !== undefined && n < f.min) errors[f.name] = `Must be at least ${f.min}.`;
      else if (f.max !== undefined && n > f.max) errors[f.name] = `Must be at most ${f.max}.`;
      else if (f.integer && !Number.isInteger(n)) errors[f.name] = 'Enter a whole number.';
    }
    if (f.pattern && !f.pattern.test(v)) errors[f.name] = f.patternMessage || 'Invalid value.';
  });
  return errors;
}

export default function RecordForm({ fields, initialValues, isEdit, onSubmit, onCancel, validate, submitLabel }) {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [saving, setSaving] = useState(false);

  function change(name, value) {
    setValues((v) => ({ ...v, [name]: value }));
    setErrors((e) => ({ ...e, [name]: undefined }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const fieldErrors = { ...validateFields(fields, values), ...(validate ? validate(values) : {}) };
    Object.keys(fieldErrors).forEach((k) => fieldErrors[k] === undefined && delete fieldErrors[k]);
    setErrors(fieldErrors);
    if (Object.keys(fieldErrors).length > 0) return;

    setSaving(true);
    setServerError('');
    try {
      await onSubmit(toPayload(fields, values));
    } catch (err) {
      setServerError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="form" onSubmit={handleSubmit} noValidate>
      {serverError && <div className="alert alert-error">{serverError}</div>}
      <div className="form-grid">
        {fields.map((f) => {
          const id = `field-${f.name}`;
          const readOnly = isEdit && f.readOnlyOnEdit;
          const common = {
            id,
            name: f.name,
            value: values[f.name] ?? '',
            onChange: (e) => change(f.name, e.target.value),
            'aria-invalid': errors[f.name] ? 'true' : undefined,
            disabled: readOnly,
          };
          return (
            <div key={f.name} className={`field ${f.full ? 'field-full' : ''}`}>
              <label htmlFor={id}>
                {f.label}
                {f.required && <span className="req" aria-hidden="true"> *</span>}
              </label>
              {f.type === 'select' ? (
                <select {...common}>
                  <option value="">{f.placeholder || 'Select…'}</option>
                  {(f.options || []).map((o) => (
                    <option key={o.value} value={String(o.value)}>{o.label}</option>
                  ))}
                </select>
              ) : f.type === 'textarea' ? (
                <textarea {...common} rows={3} maxLength={f.maxLength} />
              ) : (
                <input
                  {...common}
                  type={f.type || 'text'}
                  maxLength={f.maxLength}
                  min={f.min}
                  max={f.max}
                  step={f.step}
                  placeholder={f.placeholder}
                />
              )}
              {errors[f.name] ? (
                <span className="field-error">{errors[f.name]}</span>
              ) : (
                f.hint && <span className="field-hint">{readOnly ? 'Cannot be changed after creation.' : f.hint}</span>
              )}
            </div>
          );
        })}
      </div>
      <div className="form-actions">
        <button type="button" className="btn" onClick={onCancel}>Cancel</button>
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving…' : submitLabel}
        </button>
      </div>
    </form>
  );
}
