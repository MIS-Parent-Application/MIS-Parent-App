<div align="center">
  <img src="app/src/main/res/drawable/school_logo.png" width="200px" alt="Colegio de Alicia Logo"/>

  <h1>MIS Parent App - Full Stack Documentation</h1>

  <p>
    <a href="https://github.com/MIS-Parent-Application/MIS-Parent-App/actions/workflows/android.yml"><img src="https://github.com/MIS-Parent-Application/MIS-Parent-App/actions/workflows/android.yml/badge.svg" alt="Android Debug APK Build"/></a>
    <a href="https://railway.app/project/f7d6a086-3937-4fd3-bcfe-2162c3a63d7c?environmentId=3132c984-86de-492b-b247-f35e4200b009"><img src="https://img.shields.io/website?label=Railway%20Deployment&url=https%3A%2F%2Fmis-parent-app-production.up.railway.app%2Fapi%2Fapp%2Fversion&style=flat&color=131415" alt="Railway Deployment Status"/></a>
  </p>

  <p>
    <a href="http://www.colegiodealicia.com/"><img src="https://img.shields.io/badge/Website-colegiodealicia.com-0066CC?style=for-the-badge&logo=google-chrome&logoColor=white" alt="Website"/></a>
    <a href="https://www.facebook.com/profile.php?id=61574573837221"><img src="https://img.shields.io/badge/Facebook-Official_Page-1877F2?style=for-the-badge&logo=facebook&logoColor=white" alt="Facebook"/></a>
    <a href="https://github.com/MIS-Parent-Application/MIS-Parent-App/releases/download/v1.0.1/app-release.apk"><img src="https://img.shields.io/badge/Download-MIS_Parent_App_APK-38B02D?style=for-the-badge&logo=android&logoColor=white" alt="Download MIS Parent App APK"/></a>
    <a href="https://docs.google.com/document/d/12nX_p62XZBdJY3RFJh4MCzTqaZNwWht3/edit"><img src="https://img.shields.io/badge/Documentation-Google_Docs-EA4335?style=for-the-badge&logo=google-docs&logoColor=white" alt="Documentation"/></a>
    <a href="https://www.figma.com/design/UamtqxiDJKrvsQHnHENeFs/Parent-App?node-id=0-1&t=8DO0y4AMdGC7fp55-1"><img src="https://img.shields.io/badge/UI_Design-Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white" alt="Figma Design"/></a>
    <a href="https://drive.google.com/drive/folders/1lZW4yZSBHv25OFWngDQWTb3M-rbwzze3?usp=sharing"><img src="https://img.shields.io/badge/Project_Drive-Storage-FFBA08?style=for-the-badge&logo=googledrive&logoColor=white" alt="Google Drive Folder"/></a>
  </p>
</div>

## 📌 Overview
The **MIS Parent App** is a specialized mobile Management Information System designed to bridge the communication gap between educational institutions and parents. It allows parents to monitor their child's academic progress, attendance, and school activities in real-time through a secure, modern interface.

---

## 🏗️ System Architecture
The project follows a **Client-Server architecture** with a modern mobile frontend and a multi-database backend.

### 1. Frontend (Mobile)
- **Framework:** Kotlin Jetpack Compose (Declarative UI)
- **Local Persistence:** Room Database (SQLite) for offline caching and session management.
- **Networking:** Retrofit 2 with OkHttp for RESTful communication.
- **Navigation:** Type-safe Compose Navigation.
- **Image Loading:** Custom RemoteImage implementation with initials fallback logic.

### 2. Backend (API)
- **Runtime:** Node.js (Express framework)
- **Primary Database (SQLite):** Stores student records, schedules, grades, and core application data.
- **Production Database (PostgreSQL):** Hosted on Railway; specifically handles user feedback and high-durability production logs.
- **Email System:** Nodemailer (Gmail SMTP) for Two-Factor Authentication (2FA) codes.

---

## 🛠️ Technical Stack

### Mobile Frontend
- **Language:** Kotlin
- **UI Architecture:** MVVM (Model-View-ViewModel)
- **Concurrency:** Kotlin Coroutines & Flow
- **Dependency Injection:** Manual ViewModel Factories
- **Build System:** Gradle (Kotlin DSL)

### Backend Services
- **Language:** JavaScript (Node.js)
- **Database Drivers:** `sqlite3` for local/core data, `pg` for production PostgreSQL.
- **Security:** `crypto` for SHA-256 OTP hashing and session verification.
- **Middleware:** `cors`, `express.json` (10mb limit for profile image uploads).

---

## 📡 API Documentation

### Authentication (`/api/auth`)
| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/login` | POST | Authenticates user; returns 2FA requirement status or dashboard data. |
| `/verify-otp` | POST | Verifies the 6-digit email code and establishes a session. |
| `/resend-otp` | POST | Invalidates old OTP and issues a fresh one via email. |

### Parent & Student Data (`/api/parent`, `/api/student`)
| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/parent/dashboard` | GET | Comprehensive data pull (parent info + list of all children). |
| `/student/:id/attendance` | GET | Detailed subject-by-subject attendance breakdown. |
| `/student/:id/grades` | GET | Official grades including Term (Prelim/Midterm) and Remarks. |
| `/student/:id/academic-performance` | GET | Performance alerts (Missing outputs, High/Low scores). |
| `/parent/profile` | PATCH | Updates email, phone, or profile image (supports Base64 upload). |

### Communications & Feedback
| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/announcements` | GET | Fetches school-wide and college-specific notifications. |
| `/feedback` | POST | **(Postgres)** Submits user feedback directly to the production database. |
| `/chat/history/:facultyId` | GET | Retrieves conversation history between a parent and a specific teacher. |
| `/chat/send` | POST | Sends a new chat message to a faculty member. |

---

## 🗄️ Database Schemas

### SQLite (Core Data)
- `parents`: Basic info, contact details, and 2FA settings.
- `students`: Academic profiles linked to parent accounts.
- `academic_grades`: Official scores with term and instructor details.
- `class_schedules`: Time, room, and subject mapping for daily tracking.
- `notifications`: Alerts that populate "Recent Activities" and "Announcements".

### PostgreSQL (Production Logs)
- `parent_app_feedback`: Stores `user_email`, `feedback_type`, `message`, and `app_version` with automatic timestamps.

---

## 🚀 Deployment & Installation

### Android (Mobile)
1. **Download:** [Get the latest APK](https://github.com/MIS-Parent-Application/MIS-Parent-App/releases/download/v1.0.1/app-release.apk)
2. **Installation:** Allow "Unknown Sources" in Android Settings.
3. **Sign-in username:** jordan 
4. **Sign-in password:** parent123
5. **Debug Auto-Login:** The app includes a debug bypass for development (`dev_tester` account).

### Backend (Railway)
1. **Repository:** Connect the `backend/` directory to a Railway project.
2. **Environment Variables:**
   - `DATABASE_URL`: Your PostgreSQL connection string.
   - `EMAIL_USER` / `EMAIL_PASS`: SMTP credentials for 2FA.
   - `PORT`: (Default 3000).
3. **Automatic Schema:** On startup, the backend verifies and creates necessary PostgreSQL tables automatically.

---

## ✨ Key Features & UX Enhancements
- **Smart Performance Stats:** Dashboard calculates a weighted performance score (60% GPA, 30% Attendance, 10% Task records).
- **Marquee UI:** Long student names and subject titles automatically pan (right-to-left) to maintain card symmetry.
- **Data Safety:** Parents can toggle 2FA and clear local data directly from the Settings module.
- **Integrated Feedback:** Direct line to app developers with Bug/Feature Request categorization.

---

## 🎨 Contributors
<table style="border-collapse: collapse; border: none;">
  <tr>
    <td style="text-align:center; border: none;">
      <a href="https://github.com/CarlXT">
        <img src="https://res.cloudinary.com/dxolxqfc6/image/fetch/w_200,h_200,c_fill,g_face,r_max,f_auto/https://github.com/CarlXT.png" width="100px;" alt="CarlXT"/>
        <br />
        <sub><b>Carl Sagario</b></sub>
        <br />
        <sub><i>🧑🏻‍💻🎨📄</i></sub>
      </a>
    </td>
    <td style="text-align:center; border: none;">
      <a href="https://github.com/Fallen032">
        <img src="https://res.cloudinary.com/dxolxqfc6/image/fetch/w_200,h_200,c_fill,g_face,r_max,f_auto/https://github.com/Fallen032.png" width="100px;" alt="Fallen032"/>
        <br />
        <sub><b>Serge Keneth Lim</b></sub>
        <br />
        <sub><i>🧑🏻‍💻</i></sub>
      </a>
    </td>
  <td style="text-align:center; border: none;">
      <a href="https://github.com/semi-naan">
        <img src="https://res.cloudinary.com/dxolxqfc6/image/fetch/w_200,h_200,c_fill,g_face,r_max,f_auto/https://github.com/semi-naan.png" width="100px;" alt="semi-naan"/>
        <br />
        <sub><b>Keenan Semine</b></sub>
        <br />
        <sub><i>🧑🏻‍💻</i></sub>
      </a>
    </td>
  <td style="text-align:center; border: none;">
      <a href="https://github.com/Nathzy">
        <img src="https://res.cloudinary.com/dxolxqfc6/image/fetch/w_200,h_200,c_fill,g_face,r_max,f_auto/https://github.com/Namkee.png" width="100px;" alt="Namkee"/>
        <br />
        <sub><b>Jethro Nathaniel Cabahug</b></sub>
        <br />
        <sub><i>🧑🏻‍💻📄</i></sub>
      </a>
    </td>
  <td style="text-align:center; border: none;">
      <a href="https://github.com/Axiala01">
        <img src="https://res.cloudinary.com/dxolxqfc6/image/fetch/w_200,h_200,c_fill,g_face,r_max,f_auto/https://github.com/Axiala01.png" width="100px;" alt="Axiala01"/>
        <br />
        <sub><b>Juliana Mae Lloveras</b></sub>
        <br />
        <sub><i>🧑🏻‍💻</i></sub>
      </a>
    </td>
  <td style="text-align:center; border: none;">
      <a href="https://github.com/vicben2">
        <img src="https://res.cloudinary.com/dxolxqfc6/image/fetch/w_200,h_200,c_fill,g_face,r_max,f_auto/https://github.com/vicben2.png" width="100px;" alt="vicben2"/>
        <br />
        <sub><b>Zeth Brandinno</b></sub>
        <br />
        <sub><i>🧑🏻‍💻</i></sub>
      </a>
    </td>
  <td style="text-align:center; border: none;">
      <a href="https://github.com/b1nsz">
        <img src="https://res.cloudinary.com/dxolxqfc6/image/fetch/w_200,h_200,c_fill,g_face,r_max,f_auto/https://github.com/b1nsz.png" width="100px;" alt="b1nsz"/>
        <br />
        <sub><b>Vince Cherry Betache</b></sub>
        <br />
        <sub><i>🧑🏻‍💻</i></sub>
      </a>
    </td>
  </tr>
</table>

---
<div align="center">
  <p>© 2026 Colegio de Alicia. All Rights Reserved.</p>
</div>

---
*Documentation Version: 1.1.0*  
*Last Updated: May 2026*
