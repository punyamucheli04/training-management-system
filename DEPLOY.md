# Deploying safely

The deployed app is **one container** (see `Dockerfile`): Spring Boot serves the React pages
and the REST API from the same HTTPS address. MySQL runs on a hosted MySQL service.

```text
Browser --HTTPS--> Hosting platform --> Spring Boot (React pages + /api) --JDBC + SSL--> Hosted MySQL
```

Example used below: **Aiven** (free MySQL) + **Render** (free web service built from GitHub).
Any host that can build a Dockerfile and any MySQL 8 service works the same way.

Free plans sleep when idle: the first visit after a quiet period can take about a minute.

---

## What production mode changes

Production mode is switched on by `SPRING_PROFILES_ACTIVE=prod` (set in the Dockerfile) and uses
`backend/src/main/resources/application-prod.properties`. Local development is not affected.

| Protection | How |
|---|---|
| No secrets in the code | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` come only from the host's environment variables |
| Encrypted database connection | `sslMode=REQUIRED` in `DB_URL` |
| Least-privilege database user | the app logs in as `tms_app`, which cannot drop tables or touch other databases |
| Secure login cookie | `Secure` (HTTPS only), `HttpOnly` (no JavaScript access), `SameSite=Lax` |
| Password guessing blocked | 5 wrong passwords lock that username for 15 minutes (`LoginAttemptService`) |
| No CORS needed | pages and API share one address; no other site may call the API |
| Less information leaked | `/api/health` hides the MySQL version; SQL is not logged |
| Container runs as a normal user | `USER appuser` in the Dockerfile |

---

## Steps

### 1. Keep your password out of GitHub
* Put your local MySQL password in `backend/.env` (copy of `.env.example`) and leave
  `application.properties` with `YOUR_PASSWORD`.
* `.gitignore` already excludes `backend/.env`. Check with `git status` before every push.

### 2. Create the hosted MySQL
Create a free MySQL service, then connect with the mysql client using the host, port, user
(`avnadmin` on Aiven) and password shown in its dashboard:

```bash
mysql --host=HOST --port=PORT --user=avnadmin -p --ssl-mode=REQUIRED
```

Run the scripts (sample data contains only made-up people - do not upload real students' data):

```sql
SOURCE database/schema.sql;
SOURCE database/sample_data.sql;
SOURCE database/views.sql;
SOURCE database/functions.sql;
SOURCE database/procedures.sql;
SOURCE database/triggers.sql;
```

### 3. Create the app's own database user

```sql
CREATE USER 'tms_app'@'%' IDENTIFIED BY 'a-long-random-password';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON training_management_system.* TO 'tms_app'@'%';
```

### 4. Create the admin login with a strong password
On your computer, in the `backend` folder:

```bash
java src/main/java/com/trainingmanagement/service/PasswordHasher.java "your-strong-password"
```

Copy the printed hash, then in the hosted MySQL:

```sql
INSERT INTO training_management_system.users (Username, Email, Password_Hash, Role)
VALUES ('admin', 'admin@example.com', 'PASTE-HASH-HERE', 'Admin');
```

Do not use `admin123` online.

### 5. Push the project to a GitHub repository
Private is recommended.

### 6. Create the web service
New web service → connect the GitHub repository → it detects the `Dockerfile`.
Set these environment variables in the host's dashboard:

| Name | Value |
|---|---|
| `DB_URL` | `jdbc:mysql://HOST:PORT/training_management_system?sslMode=REQUIRED` |
| `DB_USERNAME` | `tms_app` |
| `DB_PASSWORD` | the password from step 3 |

The host provides `PORT` and HTTPS automatically.

### 7. Check
Open `https://your-app-address/api/health` → `{"connected":true}`.
Open `https://your-app-address` → log in with the admin from step 4.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| Error 1419 / 1418 when running `functions.sql` or `triggers.sql` | Enable `log_bin_trust_function_creators` in the MySQL service's advanced settings, then run the script again. |
| App log: `Access denied for user 'tms_app'` | Check `DB_USERNAME` / `DB_PASSWORD`, and that step 3 ran. |
| App log: `Communications link failure` | Check host and port in `DB_URL`; the database may be powered off for inactivity - power it on. |
| Login works locally but not online | Make sure you open the `https://` address (the secure cookie is not sent over plain http). |
