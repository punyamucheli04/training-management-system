// ---------------------------------------------------------------------
// All communication with the Spring Boot REST API goes through this file.
// The React app never talks to MySQL directly:
//
//   React  --fetch (HTTP + JSON)-->  Spring Boot  --JDBC-->  MySQL
//
// The API address comes from the VITE_API_URL environment variable
// (frontend/.env). If it is not set, the default backend address is used.
// ---------------------------------------------------------------------

export const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api').replace(/\/+$/, '');

// Fired when the backend answers 401 (not logged in / session expired).
export const AUTH_EXPIRED_EVENT = 'auth:expired';

async function request(method, path, body) {
  // credentials: 'include' sends the login session cookie (JSESSIONID) to the backend
  const options = { method, headers: {}, credentials: 'include' };
  if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }

  let response;
  try {
    response = await fetch(API_URL + path, options);
  } catch {
    throw new Error(`Cannot reach the backend at ${API_URL}. Make sure Spring Boot is running.`);
  }

  if (response.status === 204) return null;

  const text = await response.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = null;
  }

  if (response.status === 401 && !path.startsWith('/auth/')) {
    window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
  }

  if (!response.ok) {
    // The backend always sends { status, error, message } for errors.
    throw new Error((data && data.message) || `Request failed with status ${response.status}.`);
  }
  return data;
}

function query(params) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.append(key, value);
  });
  const text = search.toString();
  return text ? `?${text}` : '';
}

const crud = (base) => ({
  list: (params = {}) => request('GET', `${base}${query(params)}`),
  get: (id) => request('GET', `${base}/${id}`),
  create: (data) => request('POST', base, data),
  update: (id, data) => request('PUT', `${base}/${id}`, data),
  remove: (id) => request('DELETE', `${base}/${id}`),
});

export const authApi = {
  login: (Username, Password) => request('POST', '/auth/login', { Username, Password }),
  logout: () => request('POST', '/auth/logout'),
  me: () => request('GET', '/auth/me'),
  changePassword: (Current_Password, New_Password) =>
    request('PUT', '/auth/password', { Current_Password, New_Password }),
};

export const studentsApi = {
  ...crud('/students'),
  attendance: (id) => request('GET', `/students/${id}/attendance`),
};

export const trainersApi = crud('/trainers');
export const departmentsApi = crud('/departments');
export const trainingsApi = crud('/trainings');

export const sessionsApi = {
  ...crud('/sessions'),
  attendanceSummary: (id) => request('GET', `/sessions/${id}/attendance-summary`),
  saveAttendance: (id, items) => request('PUT', `/sessions/${id}/attendance`, items),
};

export const enrollmentsApi = {
  list: (params = {}) => request('GET', `/enrollments${query(params)}`),
  create: (data) => request('POST', '/enrollments', data),
  remove: (sessionId, studentId) => request('DELETE', `/enrollments/${sessionId}/${studentId}`),
  updateAttendance: (sessionId, studentId, status) =>
    request('PUT', `/enrollments/${sessionId}/${studentId}/attendance`, { Attendance_Status: status }),
};

export const attendanceApi = {
  byStudent: () => request('GET', '/attendance/students'),
  history: () => request('GET', '/attendance/history'),
};

export const dashboardApi = {
  get: () => request('GET', '/dashboard'),
  health: () => request('GET', '/health'),
};

export const reportsApi = {
  list: () => request('GET', '/reports'),
  run: (key) => request('GET', `/reports/${key}`),
};
