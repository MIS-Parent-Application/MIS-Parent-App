# MIS Parent App Backend Documentation

Prepared for: Colegio De Alicia professor / backend maintainer  
Project: MIS Parent App  
Backend runtime: Node.js + Express  
Primary database: PostgreSQL on Railway  
Android client: Kotlin Jetpack Compose using Retrofit

## 1. Purpose Of This Document

This document explains how the MIS Parent App backend works, how the Android app connects to it, and how official school data can be inserted or maintained.

The backend is now designed to use PostgreSQL as the single source of truth for app data. SQLite is no longer used by the backend.

## 2. Backend Location

Backend source folder:

```text
backend/
```

Main server file:

```text
backend/server.js
```

Backend package file:

```text
backend/package.json
```

Start command:

```bash
npm start
```

The start script runs:

```bash
node server.js
```

## 3. Deployment Overview

The backend is deployed online through Railway.

Current production API URL:

```text
https://mis-parent-app-production.up.railway.app/
```

Health endpoint:

```text
GET /api/health
```

Expected response:

```json
{
  "status": "healthy",
  "database": "postgres"
}
```

The backend requires `DATABASE_URL`. If `DATABASE_URL` is missing, the server intentionally stops because the app should not fall back to local storage.

## 4. Environment Variables

Set these in Railway under the backend service variables.

Required:

```text
DATABASE_URL=<Railway PostgreSQL connection string>
```

Recommended:

```text
PORT=3000
EMAIL_USER=<Gmail or school SMTP email>
EMAIL_APP_PASSWORD=<Gmail app password or SMTP password>
EMAIL_PASS=<fallback SMTP password, optional if EMAIL_APP_PASSWORD exists>
EMAIL_FROM=Colegio De Alicia <school-email@example.com>
OTP_SECRET=<long random private text>
OTP_TTL_MINUTES=15
OTP_MAX_ATTEMPTS=5
OTP_RESEND_COOLDOWN_SECONDS=60
APP_VERSION_CODE=2
APP_VERSION_NAME=1.0.1
APP_APK_URL=https://github.com/MIS-Parent-Application/MIS-Parent-App/releases/download/v1.0.1/app-release.apk
APP_RELEASE_NOTES=Latest Colegio De Alicia parent app release.
```

Security reminder:

Do not put actual passwords, Gmail app passwords, database URLs, or OTP secrets in GitHub. Keep them only in Railway variables or local `.env` files that are not committed.

## 5. Android App Connection

The Android app calls the backend using Retrofit.

Important files:

```text
app/src/main/java/com/mis/parentapp/network/RetrofitInstance.kt
app/src/main/java/com/mis/parentapp/network/ApiService.kt
app/src/main/java/com/mis/parentapp/network/Models.kt
```

The backend URL is compiled into the app through:

```text
BuildConfig.API_BASE_URL
```

For APK builds, use:

```powershell
.\gradlew.bat :app:assembleDebug -PPARENT_APP_API_URL=https://mis-parent-app-production.up.railway.app/
```

Why this matters:

If the app is built without `PARENT_APP_API_URL`, the APK may point to a local development URL instead of Railway. On a phone, local URLs will not work unless the phone can reach that local machine.

## 6. Request Flow

Typical login flow:

```text
Parent enters username/password
  -> Android AuthViewModel
  -> UserRepository
  -> Retrofit ApiService.login()
  -> POST /api/auth/login
  -> PostgreSQL parent_accounts table
  -> returns parent profile and dashboard data
```

Typical dashboard flow:

```text
Home / Student screens
  -> GET /api/parent/dashboard
  -> parents, parent_students, students, schedules, study_load_subjects
  -> Android shared student state
  -> Home, Student screen, class schedule, study load
```

Typical academic flow:

```text
Academic screen
  -> GET /api/student/:id/grades
  -> GET /api/student/:id/academic-performance
  -> GET /api/student/:id/attendance
```

Typical events flow:

```text
Home recent/upcoming + Calendar
  -> GET /api/calendar?studentId=<id>
  -> calendar_events table
```

Typical notifications flow:

```text
Notification and announcements screens
  -> GET /api/notifications?studentId=<id>
  -> GET /api/announcements
  -> notifications table
```

## 7. API Endpoints

### App And Health

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/health` | Confirms backend is online and using PostgreSQL. |
| GET | `/api/app/version` | Returns latest APK version, download URL, and release notes. |

### Authentication And OTP

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/auth/login` | Parent login. Returns dashboard directly or OTP requirement. |
| POST | `/api/auth/verify-otp` | Verifies email OTP and completes login. |
| POST | `/api/auth/resend-otp` | Generates and emails a new OTP. |
| POST | `/api/2fa/send` | Legacy/direct 2FA email sender. |
| POST | `/api/2fa/verify` | Legacy/direct 2FA verifier. |
| POST | `/api/2fa/toggle` | Legacy/direct 2FA toggle. |

Main app login should use `/api/auth/login`, `/api/auth/verify-otp`, and `/api/auth/resend-otp`.

### Parent

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/parent/dashboard?parentId=1` | Main parent dashboard payload. |
| GET | `/api/parent/security?parentId=1` | Gets parent email, phone, and 2FA state. |
| PATCH | `/api/parent/security` | Turns 2FA on/off. |
| PATCH | `/api/parent/profile` | Updates email, phone, and profile photo. |

### Student

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/student/:id/studyload` | Official study load rows. |
| PATCH | `/api/student/:id/photos` | Updates student profile/background photo URLs. |
| GET | `/api/student/:id/grades` | Official grades. |
| GET | `/api/student/:id/academic-performance` | Performance records. |
| GET | `/api/student/:id/attendance` | Subject attendance data. |
| GET | `/api/student/:id/payments` | Payment history. |
| POST | `/api/student/:id/payments` | Creates a payment record. |

### Notifications, Calendar, Announcements

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/notifications?studentId=101` | Student-specific and school-wide notifications. |
| GET | `/api/calendar?studentId=101` | Student-specific and school-wide calendar events. |
| GET | `/api/announcements` | Announcement view derived from notifications. |

### Faculty And Chat

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/faculty` | List of faculty contacts. |
| POST | `/api/auth/parent-login` | Parent chat login shape for chat compatibility. |
| GET | `/api/chat/history/:facultyId?parentId=parent_1` | Chat thread history. |
| POST | `/api/chat/send` | Sends/saves a chat message. |

Note:

The app also has a faculty-group chat backend file:

```text
app/src/main/java/com/mis/parentapp/network/FacultyChatApiService.kt
```

That file points to:

```text
https://eldroid-backend-express.onrender.com/
```

If the school wants one unified backend, update the chat client to use the parent app backend routes listed above.

### Feedback

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/feedback` | Saves app feedback from the Me screen. |

## 8. PostgreSQL Tables

The backend creates tables automatically on startup if they do not exist.

### `parents`

Stores parent profiles.

Columns:

```text
id
name
email
phone
profile_image_url
background_image_url
two_factor_enabled
```

### `parent_accounts`

Stores login credentials mapped to parents.

Columns:

```text
id
username
password
parent_id
```

Current warning:

Passwords are currently plain text. Before real production use, replace this with hashed passwords such as bcrypt.

### `login_otps`

Stores OTP tokens used during login.

Columns:

```text
id
parent_id
code_hash
expires_at
attempts
used
created_at
```

### `students`

Stores student profiles.

Columns:

```text
id
name
roll_number
grade
section
program
course
year
class_teacher
attendance
gpa
pending_payments
profile_image_url
background_image_url
```

### `parent_students`

Connects one parent to one or more children.

Columns:

```text
parent_id
student_id
```

This table controls the dynamic student switcher. If a parent has four children, insert four rows for that parent.

### `class_schedules`

Stores class schedules.

Columns:

```text
id
student_id
subject
room
instructor
day
start_time
end_time
```

Recommended time format:

```text
HH:mm
```

Example:

```text
08:00
09:30
```

### `study_load_subjects`

Stores official study load rows.

Columns:

```text
id
student_id
schedule_number
course_number
code
title
units
instructor
schedule
time
days
room
remarks
semester
school_year
date_enrolled
sort_order
```

### `notifications`

Stores notification and announcement style records.

Columns:

```text
id
student_id
text
type
time
category
is_new
image_url
is_positive
```

If `student_id` is null, the notification is school-wide.

### `calendar_events`

Stores school events and student-specific activities.

Columns:

```text
id
student_id
title
category
date
time
description
status
image_url
```

Recommended date format:

```text
YYYY-MM-DD
```

### `academic_grades`

Stores official grades.

Columns:

```text
id
student_id
subject_name
units
grade
instructor
remarks
term
```

### `academic_performance`

Stores academic performance records.

Columns:

```text
id
student_id
type
title
subject
teacher
summary
details
criteria
image_url
score
status
assigned_date
due_date
time_ago
is_positive
```

Suggested `type` values:

```text
high_score
low_score
missing_output
warning
announcement
```

### `attendance_subjects`

Stores subject attendance.

Columns:

```text
id
student_id
subject_name
instructor
present_days
total_days
late_days
absent_days
```

### `payment_records`

Stores payment history and receipt data.

Columns:

```text
id
student_id
invoice_number
purchased_item
payment_option
paid_date
total_amount
pdf_breakdown
status
```

### `faculty_contacts`

Stores faculty list.

Columns:

```text
faculty_id
name
department
email
subject
```

### `chat_messages`

Stores parent-faculty messages.

Columns:

```text
id
sender_id
receiver_id
message
created_at
```

### `parent_app_feedback`

Stores feedback submitted from the app.

Columns:

```text
id
user_email
feedback_type
message
app_version
created_at
```

## 9. How To Insert Official Data

Sir can insert official data directly into PostgreSQL through Railway's PostgreSQL data tab, pgAdmin, DBeaver, TablePlus, or `psql`.

Recommended insertion order:

1. `parents`
2. `parent_accounts`
3. `students`
4. `parent_students`
5. `class_schedules`
6. `study_load_subjects`
7. `academic_grades`
8. `attendance_subjects`
9. `academic_performance`
10. `payment_records`
11. `notifications`
12. `calendar_events`
13. `faculty_contacts`
14. `chat_messages`, only if importing previous messages

### Example: Add A Parent

```sql
INSERT INTO parents
(id, name, email, phone, profile_image_url, background_image_url, two_factor_enabled)
VALUES
(2, 'Maria Santos', 'maria.parent@example.com', '09123456789', '', '', 0);
```

### Example: Add Parent Login

```sql
INSERT INTO parent_accounts
(username, password, parent_id)
VALUES
('maria.parent@example.com', 'temporaryPassword123', 2);
```

### Example: Add A Student

```sql
INSERT INTO students
(id, name, roll_number, grade, section, program, course, year, class_teacher, attendance, gpa, pending_payments, profile_image_url, background_image_url)
VALUES
(201, 'Juan Dela Cruz', '2026-00001', '1st Year', 'BSIT 1-A',
 'Bachelor of Science in Information Technology', 'BSIT - 1st year',
 'A.Y. 2026-2027', 'Prof. Reyes', '96%', 1.7, 0, '', '');
```

### Example: Link Parent To Student

```sql
INSERT INTO parent_students (parent_id, student_id)
VALUES (2, 201);
```

### Example: Add A Class Schedule

```sql
INSERT INTO class_schedules
(student_id, subject, room, instructor, day, start_time, end_time)
VALUES
(201, 'IT 101 - Introduction to Computing', 'Room 201', 'Prof. Reyes', 'Monday', '08:00', '09:30');
```

### Example: Add Study Load Row

```sql
INSERT INTO study_load_subjects
(student_id, schedule_number, course_number, code, title, units, instructor, schedule, time, days, room, remarks, semester, school_year, date_enrolled, sort_order)
VALUES
(201, '10001', 'IT 101', 'IT 101', 'Introduction to Computing', 3,
 'Prof. Reyes', 'Mon 08:00 - 09:30', '08:00 - 09:30 AM', 'MON',
 'Room 201', '', '1st Sem.', 'S.Y. 2026-2027', '06/10/26', 1);
```

### Example: Add Grade

```sql
INSERT INTO academic_grades
(student_id, subject_name, units, grade, instructor, remarks, term)
VALUES
(201, 'IT 101 - Introduction to Computing', 3, 1.5, 'Prof. Reyes', 'Passed', 'Prelim');
```

### Example: Add Attendance

```sql
INSERT INTO attendance_subjects
(student_id, subject_name, instructor, present_days, total_days, late_days, absent_days)
VALUES
(201, 'IT 101 - Introduction to Computing', 'Prof. Reyes', 17, 18, 1, 0);
```

### Example: Add Academic Performance Record

```sql
INSERT INTO academic_performance
(student_id, type, title, subject, teacher, summary, details, criteria, image_url, score, status, assigned_date, due_date, time_ago, is_positive)
VALUES
(201, 'high_score', 'High score in exam', 'IT 101 - Introduction to Computing',
 'Prof. Reyes', 'Juan received one of the highest scores in the first exam.',
 'The exam covered basic computing concepts, number systems, and problem solving.',
 'Correct answers, complete solution steps, and clear explanation.',
 'event1.jpg', '48/50', 'Completed', '2026-06-18', '2026-06-18', '4hrs ago', 1);
```

### Example: Add Calendar Event

```sql
INSERT INTO calendar_events
(id, student_id, title, category, date, time, description, status, image_url)
VALUES
(1001, 201, 'IT 101 Preliminary Exam', 'Exam', '2026-06-20', '08:00 AM',
 'Preliminary exam for IT 101.', 'Academic', 'event1.jpg');
```

### Example: Add Notification

```sql
INSERT INTO notifications
(id, student_id, text, type, time, category, is_new, image_url, is_positive)
VALUES
(1001, 201, 'Juan has an IT 101 exam tomorrow.', 'Reminder', 'Today', 'academic', 1, 'event1.jpg', 1);
```

## 10. Seed Data

The backend contains seed functions:

```text
seedDatabase()
seedOfficialData()
normalizeOfficialData()
```

These create test/demo records when tables are empty. They are useful for demonstrations but should not be treated as the final official data process.

For official school records, insert directly into PostgreSQL or add future admin/import endpoints.

## 11. Recommended Official Data Maintenance

For real school use, the best future improvement is to add admin-only import tools:

```text
POST /api/admin/import/parents
POST /api/admin/import/students
POST /api/admin/import/schedules
POST /api/admin/import/study-loads
POST /api/admin/import/grades
POST /api/admin/import/attendance
POST /api/admin/import/performance
POST /api/admin/import/payments
```

This would allow Sir or school staff to upload CSV/Excel files instead of manually writing SQL.

## 12. Smoke Test Commands

Health:

```powershell
Invoke-RestMethod -Uri "https://mis-parent-app-production.up.railway.app/api/health" -Method Get
```

Login:

```powershell
Invoke-RestMethod `
  -Uri "https://mis-parent-app-production.up.railway.app/api/auth/login" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"username":"jordan","password":"parent123"}'
```

Dashboard:

```powershell
Invoke-RestMethod -Uri "https://mis-parent-app-production.up.railway.app/api/parent/dashboard?parentId=1" -Method Get
```

Student performance:

```powershell
Invoke-RestMethod -Uri "https://mis-parent-app-production.up.railway.app/api/student/101/academic-performance" -Method Get
```

Calendar:

```powershell
Invoke-RestMethod -Uri "https://mis-parent-app-production.up.railway.app/api/calendar?studentId=101" -Method Get
```

Backend syntax check:

```powershell
node --check backend/server.js
```

## 13. Known Production Notes

Before real school-wide deployment:

- Replace plain-text passwords with hashed passwords.
- Add admin/import endpoints for easier data maintenance.
- Confirm whether chat should stay on the faculty group's backend or move fully into this backend.
- Keep Railway PostgreSQL backed up.
- Keep `APP_VERSION_CODE`, `APP_VERSION_NAME`, `APP_APK_URL`, and `APP_RELEASE_NOTES` updated for every release.

