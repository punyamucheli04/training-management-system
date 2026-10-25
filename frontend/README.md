# Frontend - React + Vite

User interface of the Training Management System. It only talks to the Spring Boot REST API;
it never connects to MySQL.

## Run

```bash
npm install
npm run dev
```

Open <http://localhost:5173>. Start the backend first (see `../backend`).

## API address

`src/lib/api.js` reads `VITE_API_URL`. Without a `.env` file it uses `http://localhost:8080/api`.
To change it, copy `.env.example` to `.env` and edit the value, then restart `npm run dev`.

## Structure

```text
src/
├── App.jsx            layout, navigation, database connection indicator
├── lib/api.js         every REST call (fetch)
├── lib/useFetch.js    loading / error state + reload after changes
├── lib/format.js      date, time and percentage formatting
├── components/        DataTable, RecordForm (validation), Modal, ConfirmDialog, badges
└── pages/             Dashboard, Students, Trainers, Departments, Trainings,
                       Sessions, Enrollments, Attendance, SQL reports
```

Form field names are the exact database column names (e.g. `Mobile_Number`, `Dept_ID`), so the
JSON sent to the backend matches the Java models and the MySQL columns without any renaming.

`npm run build` creates a production build in `dist/`.
