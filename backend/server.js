const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const sqlite3 = require('sqlite3').verbose();
const nodemailer = require('nodemailer');

const app = express();
app.use(cors());
app.use(express.json({ limit: '10mb' }));



const PORT = process.env.PORT || 3000;
const DB_PATH = process.env.DATABASE_PATH || path.join(__dirname, 'data', 'mis_parent_app.db');
const UPLOAD_DIR = path.join(__dirname, 'data', 'uploads');
const OTP_TTL_MINUTES = Number(process.env.OTP_TTL_MINUTES || 15);
const OTP_MAX_ATTEMPTS = Number(process.env.OTP_MAX_ATTEMPTS || 5);
const OTP_RESEND_COOLDOWN_SECONDS = Number(process.env.OTP_RESEND_COOLDOWN_SECONDS || 60);
const OTP_SECRET = process.env.OTP_SECRET || 'mis-parent-app-dev-otp-secret';
const APP_VERSION_CODE = Number(process.env.APP_VERSION_CODE || 1);
const APP_VERSION_NAME = process.env.APP_VERSION_NAME || '1.0';
const APP_APK_URL = process.env.APP_APK_URL || '';
const APP_RELEASE_NOTES = process.env.APP_RELEASE_NOTES || 'No update is available yet.';

fs.mkdirSync(path.dirname(DB_PATH), { recursive: true });
fs.mkdirSync(UPLOAD_DIR, { recursive: true });
const db = new sqlite3.Database(DB_PATH);
app.use('/media/uploads', express.static(UPLOAD_DIR));

const twoFACodes = {};

// Email transporter setup
const transporter = nodemailer.createTransport({
  service: 'gmail', // or your SMTP provider
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS,
  },
});

// Route: Send 2FA code to email
app.post('/api/2fa/send', async (req, res) => {
  const { userId, email } = req.body;

  if (!userId || !email) {
    return res.status(400).json({ message: 'userId and email are required' });
  }

  // Generate a 6-digit code
  const code = crypto.randomInt(100000, 999999).toString();
  const expiresAt = Date.now() + 10 * 60 * 1000; // 10 minutes expiry

  // Store it temporarily
  twoFACodes[userId] = { code, expiresAt };

  // Send email
  try {
    await transporter.sendMail({
      from: process.env.EMAIL_USER,
      to: email,
      subject: 'Your 2FA Verification Code',
      text: `Your verification code is: ${code}\n\nThis code expires in 10 minutes.`,
    });
    res.json({ message: '2FA code sent successfully' });
  } catch (err) {
    console.error('Email error:', err);
    res.status(500).json({ message: 'Failed to send email' });
  }
});

// Route: Verify 2FA code
app.post('/api/2fa/verify', (req, res) => {
  const { userId, code } = req.body;

  const record = twoFACodes[userId];

  if (!record) {
    return res.status(400).json({ message: 'No 2FA code found. Request a new one.' });
  }

  if (Date.now() > record.expiresAt) {
    delete twoFACodes[userId];
    return res.status(400).json({ message: 'Code has expired. Request a new one.' });
  }

  if (record.code !== code) {
    return res.status(400).json({ message: 'Invalid code.' });
  }

  // Code is valid — enable/disable 2FA in your DB here
  delete twoFACodes[userId];
  res.json({ message: '2FA verified successfully' });
});

// Route: Toggle 2FA status in DB
app.post('/api/2fa/toggle', (req, res) => {
  const { userId, enable } = req.body;
  // Update your DB here, e.g.:
  // db.run('UPDATE users SET two_factor_enabled = ? WHERE id = ?', [enable ? 1 : 0, userId])
  res.json({ message: `2FA ${enable ? 'enabled' : 'disabled'} successfully` });
});


function run(sql, params = []) {
    return new Promise((resolve, reject) => {
        db.run(sql, params, function onRun(error) {
            if (error) reject(error);
            else resolve(this);
        });
    });
}

function get(sql, params = []) {
    return new Promise((resolve, reject) => {
        db.get(sql, params, (error, row) => {
            if (error) reject(error);
            else resolve(row);
        });
    });
}

function all(sql, params = []) {
    return new Promise((resolve, reject) => {
        db.all(sql, params, (error, rows) => {
            if (error) reject(error);
            else resolve(rows);
        });
    });
}

async function ensureColumn(tableName, columnName, columnDefinition) {
    const columns = await all(`PRAGMA table_info(${tableName})`);
    if (!columns.some(column => column.name === columnName)) {
        await run(`ALTER TABLE ${tableName} ADD COLUMN ${columnName} ${columnDefinition}`);
    }
}

async function initDatabase() {
    await run('PRAGMA foreign_keys = ON');

    await run(`
        CREATE TABLE IF NOT EXISTS parents (
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL,
            email TEXT NOT NULL,
            phone TEXT NOT NULL,
            profile_image_url TEXT NOT NULL DEFAULT '',
            background_image_url TEXT NOT NULL DEFAULT '',
            two_factor_enabled INTEGER NOT NULL DEFAULT 0
        )
    `);
    await ensureColumn('parents', 'profile_image_url', "TEXT NOT NULL DEFAULT ''");
    await ensureColumn('parents', 'background_image_url', "TEXT NOT NULL DEFAULT ''");
    await ensureColumn('parents', 'two_factor_enabled', 'INTEGER NOT NULL DEFAULT 0');
    await run(`
        CREATE TABLE IF NOT EXISTS parent_accounts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL,
            parent_id INTEGER NOT NULL,
            FOREIGN KEY(parent_id) REFERENCES parents(id)
        )
    `);
    await run(`
        CREATE TABLE IF NOT EXISTS login_otps (
            id TEXT PRIMARY KEY,
            parent_id INTEGER NOT NULL,
            code_hash TEXT NOT NULL,
            expires_at TEXT NOT NULL,
            attempts INTEGER NOT NULL DEFAULT 0,
            used INTEGER NOT NULL DEFAULT 0,
            created_at TEXT NOT NULL DEFAULT (datetime('now')),
            FOREIGN KEY(parent_id) REFERENCES parents(id)
        )
    `);
    await run(`
        CREATE TABLE IF NOT EXISTS students (
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL,
            roll_number TEXT NOT NULL,
            grade TEXT NOT NULL,
            section TEXT NOT NULL,
            program TEXT NOT NULL,
            course TEXT NOT NULL,
            year TEXT NOT NULL,
            class_teacher TEXT NOT NULL,
            attendance TEXT NOT NULL,
            gpa REAL NOT NULL,
            pending_payments INTEGER NOT NULL DEFAULT 0,
            profile_image_url TEXT NOT NULL DEFAULT '',
            background_image_url TEXT NOT NULL DEFAULT ''
        )
    `);
    await ensureColumn('students', 'profile_image_url', "TEXT NOT NULL DEFAULT ''");
    await ensureColumn('students', 'background_image_url', "TEXT NOT NULL DEFAULT ''");
    await run(`
        CREATE TABLE IF NOT EXISTS parent_students (
            parent_id INTEGER NOT NULL,
            student_id INTEGER NOT NULL,
            PRIMARY KEY(parent_id, student_id),
            FOREIGN KEY(parent_id) REFERENCES parents(id),
            FOREIGN KEY(student_id) REFERENCES students(id)
        )
    `);
    await run(`
        CREATE TABLE IF NOT EXISTS class_schedules (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            subject TEXT NOT NULL,
            room TEXT NOT NULL,
            instructor TEXT NOT NULL,
            day TEXT NOT NULL,
            start_time TEXT NOT NULL,
            end_time TEXT NOT NULL,
            FOREIGN KEY(student_id) REFERENCES students(id)
        )
    `);
    await run(`
        CREATE TABLE IF NOT EXISTS study_load_subjects (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            schedule_number TEXT NOT NULL,
            course_number TEXT NOT NULL,
            code TEXT NOT NULL,
            title TEXT NOT NULL,
            units INTEGER NOT NULL,
            instructor TEXT NOT NULL,
            schedule TEXT NOT NULL,
            time TEXT NOT NULL,
            days TEXT NOT NULL,
            room TEXT NOT NULL,
            remarks TEXT NOT NULL DEFAULT '',
            semester TEXT NOT NULL,
            school_year TEXT NOT NULL,
            date_enrolled TEXT NOT NULL,
            sort_order INTEGER NOT NULL DEFAULT 0,
            FOREIGN KEY(student_id) REFERENCES students(id)
        )
    `);
    await run(`
        CREATE TABLE IF NOT EXISTS notifications (
            id INTEGER PRIMARY KEY,
            student_id INTEGER,
            text TEXT NOT NULL,
            type TEXT NOT NULL,
            time TEXT NOT NULL,
            category TEXT NOT NULL,
            is_new INTEGER NOT NULL DEFAULT 0,
            image_url TEXT NOT NULL DEFAULT '',
            is_positive INTEGER NOT NULL DEFAULT 1
        )
    `);
    await ensureColumn('notifications', 'image_url', "TEXT NOT NULL DEFAULT ''");
    await ensureColumn('notifications', 'is_positive', "INTEGER NOT NULL DEFAULT 1");

    // FIX: Added image_url TEXT column to handle the mock image asset strings
    await run(`
        CREATE TABLE IF NOT EXISTS calendar_events (
            id INTEGER PRIMARY KEY,
            student_id INTEGER,
            title TEXT NOT NULL,
            category TEXT NOT NULL,
            date TEXT NOT NULL,
            time TEXT NOT NULL,
            description TEXT NOT NULL,
            status TEXT NOT NULL,
            image_url TEXT
        )
    `);
    await ensureColumn('calendar_events', 'image_url', "TEXT");
    await run(`
        CREATE TABLE IF NOT EXISTS academic_grades (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            subject_name TEXT NOT NULL,
            units INTEGER NOT NULL,
            grade REAL NOT NULL,
            instructor TEXT NOT NULL,
            remarks TEXT NOT NULL DEFAULT '',
            term TEXT NOT NULL,
            FOREIGN KEY(student_id) REFERENCES students(id)
        )
    `);
    await run(`
        CREATE TABLE IF NOT EXISTS academic_performance (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            type TEXT NOT NULL,
            title TEXT NOT NULL,
            subject TEXT NOT NULL,
            teacher TEXT NOT NULL,
            summary TEXT NOT NULL,
            details TEXT NOT NULL,
            criteria TEXT NOT NULL,
            image_url TEXT,
            score TEXT,
            status TEXT NOT NULL,
            assigned_date TEXT NOT NULL,
            due_date TEXT NOT NULL,
            time_ago TEXT NOT NULL,
            is_positive INTEGER NOT NULL DEFAULT 1,
            FOREIGN KEY(student_id) REFERENCES students(id)
        )
    `);
    await run(`
        CREATE TABLE IF NOT EXISTS attendance_subjects (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            subject_name TEXT NOT NULL,
            instructor TEXT NOT NULL,
            present_days INTEGER NOT NULL,
            total_days INTEGER NOT NULL,
            late_days INTEGER NOT NULL DEFAULT 0,
            absent_days INTEGER NOT NULL DEFAULT 0,
            FOREIGN KEY(student_id) REFERENCES students(id)
        )
    `);
    await run(`
        CREATE TABLE IF NOT EXISTS payment_records (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            invoice_number TEXT NOT NULL UNIQUE,
            purchased_item TEXT NOT NULL,
            payment_option TEXT NOT NULL,
            paid_date TEXT NOT NULL,
            total_amount REAL NOT NULL,
            pdf_breakdown TEXT NOT NULL DEFAULT '',
            status TEXT NOT NULL DEFAULT 'Paid',
            FOREIGN KEY(student_id) REFERENCES students(id)
        )
    `);
    await run(`
        CREATE TABLE IF NOT EXISTS faculty_contacts (
            faculty_id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            department TEXT NOT NULL,
            email TEXT NOT NULL,
            subject TEXT NOT NULL
        )
    `);
    await run(`
        CREATE TABLE IF NOT EXISTS chat_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            sender_id TEXT NOT NULL,
            receiver_id TEXT NOT NULL,
            message TEXT NOT NULL,
            created_at TEXT NOT NULL DEFAULT (datetime('now'))
        )
    `);

    const parentCount = await get('SELECT COUNT(*) AS count FROM parents');
    if (parentCount.count === 0) {
        await seedDatabase();
    }
    await seedOfficialData();
    await normalizeOfficialData();
}

async function seedDatabase() {
    await run(
        'INSERT INTO parents (id, name, email, phone) VALUES (?, ?, ?, ?)',
        [1, 'Jordan McClure', 'julianamaelloveras@gmail.com', '09082105876']
    );
    await run(
        'INSERT INTO parent_accounts (username, password, parent_id) VALUES (?, ?, ?), (?, ?, ?)',
        ['jordan.mcclure@email.com', 'parent123', 1, 'jordan', 'parent123', 1]
    );

    const students = [
        [101, 'Nathaniel B. McClure', '123456789', '3rd Year', 'BSIT 3-A', 'Bachelor of Science in Information Technology', 'BSIT - 3rd year', 'A.Y. 2025-2026', 'Prof. Santos', '94%', 1.5, 0],
        [102, 'Sofia B. McClure', '987654321', '2nd Year', 'BSCS 2-B', 'Bachelor of Science in Computer Science', 'BSCS - 2nd year', 'A.Y. 2025-2026', 'Prof. Molina', '97%', 1.3, 400]
    ];
    for (const student of students) {
        await run(
            `INSERT INTO students
             (id, name, roll_number, grade, section, program, course, year, class_teacher, attendance, gpa, pending_payments)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            student
        );
        await run('INSERT INTO parent_students (parent_id, student_id) VALUES (?, ?)', [1, student[0]]);
    }

    const schedules = [
        [101, 'IT 312 - Mobile Development', 'Lab 402', 'Prof. Reyes', 'Monday', '08:00', '09:30'],
        [101, 'IT 326 - Database Systems', 'Room 301', 'Dr. Maria Santos', 'Monday', '10:00', '11:30'],
        [101, 'GE 108 - Ethics', 'Room 204', 'Ms. Dela Cruz', 'Tuesday', '13:00', '14:30'],
        [101, 'IT 318 - Web Systems', 'Lab 407', 'Prof. Garcia', 'Wednesday', '09:00', '10:30'],
        [101, 'IT 330 - Capstone 1', 'Room 305', 'Dr. Lim', 'Friday', '15:00', '17:00'],
        [102, 'CS 210 - Data Structures', 'Lab 201', 'Prof. Molina', 'Monday', '09:00', '10:30'],
        [102, 'MATH 214 - Discrete Math', 'Room 112', 'Ms. Aquino', 'Tuesday', '10:00', '11:30'],
        [102, 'CS 218 - Object-Oriented Programming', 'Lab 203', 'Engr. Villanueva', 'Thursday', '13:30', '15:00'],
        [102, 'PE 204 - Team Sports', 'Gym', 'Coach Ramos', 'Friday', '08:00', '10:00']
    ];
    for (const schedule of schedules) {
        await run(
            `INSERT INTO class_schedules
             (student_id, subject, room, instructor, day, start_time, end_time)
             VALUES (?, ?, ?, ?, ?, ?, ?)`,
            schedule
        );
    }

    const studyLoads = [
        [101, '02543', 'IT 312', 'IT 312', 'Mobile Development', 3, 'Prof. Reyes', 'Mon 08:00 - 09:30', '08:00 - 09:30 AM', 'MON', 'Lab 402', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 1],
        [101, '33506', 'IT 326', 'IT 326', 'Database Systems', 3, 'Dr. Maria Santos', 'Mon 10:00 - 11:30', '10:00 - 11:30 AM', 'MON', 'Room 301', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 2],
        [101, '33514', 'GE 108', 'GE 108', 'Ethics', 3, 'Ms. Dela Cruz', 'Tue 13:00 - 14:30', '01:00 - 02:30 PM', 'TUE', 'Room 204', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 3],
        [101, '33522', 'IT 318', 'IT 318', 'Web Systems', 3, 'Prof. Garcia', 'Wed 09:00 - 10:30', '09:00 - 10:30 AM', 'WED', 'Lab 407', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 4],
        [101, '33530', 'IT 330', 'IT 330', 'Capstone 1', 3, 'Dr. Lim', 'Fri 15:00 - 17:00', '03:00 - 05:00 PM', 'FRI', 'Room 305', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 5],
        [102, '34501', 'CS 210', 'CS 210', 'Data Structures', 3, 'Prof. Molina', 'Mon 09:00 - 10:30', '09:00 - 10:30 AM', 'MON', 'Lab 201', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 1],
        [102, '34509', 'MATH 214', 'MATH 214', 'Discrete Math', 3, 'Ms. Aquino', 'Tue 10:00 - 11:30', '10:00 - 11:30 AM', 'TUE', 'Room 112', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 2],
        [102, '34518', 'CS 218', 'CS 218', 'Object-Oriented Programming', 3, 'Engr. Villanueva', 'Thu 13:30 - 15:00', '01:30 - 03:00 PM', 'THU', 'Lab 203', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 3],
        [102, '34525', 'PE 204', 'PE 204', 'Team Sports', 2, 'Coach Ramos', 'Fri 08:00 - 10:00', '08:00 - 10:00 AM', 'FRI', 'Gym', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 4]
    ];
    for (const subject of studyLoads) {
        await run(
            `INSERT INTO study_load_subjects
             (student_id, schedule_number, course_number, code, title, units, instructor, schedule, time, days, room, remarks, semester, school_year, date_enrolled, sort_order)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            subject
        );
    }

    const notifications = [
        [1, 101, 'Nathaniel has a Mobile Development laboratory activity due today.', 'Reminder', '1hr ago', 'academic', 1, 'event1.jpg', 1],
        [2, 101, 'Database Systems quiz score has been posted.', 'Activity', '4hrs ago', 'academic', 1, 'event2.jpg', 1],
        [3, 102, "Sofia's PE uniform fee is still pending.", 'Reminder', 'Yesterday', 'financial', 1, 'event3.jpg', 0],
        [4, null, 'College assembly will be held on May 24 at the auditorium.', 'Event', 'Yesterday', 'college', 0, 'event1.jpg', 1],
        [5, null, 'Emergency drill schedule has been moved to next week.', 'Emergency', '2 days ago', 'school-wide', 0, 'event2.jpg', 1]
    ];
    for (const notification of notifications) {
        await run(
            'INSERT INTO notifications (id, student_id, text, type, time, category, is_new, image_url, is_positive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
            notification
        );
    }

    const events = [
        [1, 101, 'Mobile Development Practical Exam', 'Exam', '2026-05-15', '08:00 AM', 'Hands-on Android Compose assessment in Lab 402.', 'Academic', 'event1.jpg'],
        [2, 101, 'Capstone Consultation', 'Academic', '2026-05-17', '03:00 PM', 'Project progress check with Dr. Lim.', 'Reminder', 'event2.jpg'],
        [3, 102, 'Data Structures Long Quiz', 'Exam', '2026-05-16', '09:00 AM', 'Trees, graphs, and sorting algorithms.', 'Academic', 'event3.jpg'],
        [4, null, 'College Assembly', 'College', '2026-05-24', '10:00 AM', 'Required assembly for all CCS students.', 'School-wide', 'event1.jpg'],
        [5, null, 'Parent-Teacher Consultation Day', 'School-wide', '2026-05-30', '01:00 PM', 'Parents may meet instructors by appointment.', 'Reminder', 'event2.jpg'],
        [6, null, 'Sports Day', 'Sports', '2026-06-20', '08:00 AM', 'Inter-school sports competition.', 'Postponed', 'event1.jpg'],
        [7, null, 'Art Workshop', 'Creative', '2026-05-15', '02:00 PM', 'Hands-on painting and sculpting.', 'Normal', 'event2.jpg'],
        [8, null, 'Music Gala', 'Arts', '2026-07-05', '06:00 PM', 'Evening of classical music.', 'Normal', 'event3.jpg'],
        [9, null, 'Summer Camp', 'General', '2026-08-01', '09:00 AM', 'Week-long outdoor activities.', 'Normal', 'event1.jpg'],
        [10, null, 'PTA Meeting', 'Meeting', '2026-04-15', '01:00 PM', 'Discussion on school curriculum.', 'Normal', 'event2.jpg'],
        [11, null, 'Math Olympiad', 'Academic', '2026-04-10', '09:00 AM', 'Regional math competition winners announced.', 'Normal', 'event3.jpg'],
        [12, null, 'Field Trip', 'Excursion', '2026-04-05', '07:00 AM', 'Visit to the National Museum.', 'Normal', 'event1.jpg'],
        [13, null, 'Career Talk', 'Education', '2026-03-28', '10:00 AM', 'Industry experts sharing insights.', 'Normal', 'event2.jpg'],
        [14, null, 'Spring Fest', 'Social', '2026-03-20', '04:00 PM', 'Celebrating the spring season.', 'Cancelled', 'event3.jpg']
    ];
    for (const event of events) {
        await run(
            'INSERT INTO calendar_events (id, student_id, title, category, date, time, description, status, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
            event
        );
    }
}

async function seedOfficialData() {
    const gradeCount = await get('SELECT COUNT(*) AS count FROM academic_grades');
    if (gradeCount.count === 0) {
        const grades = [
            [101, 'IT 312 - Mobile Development', 3, 1.3, 'Prof. Reyes', 'Passed', 'Prelim'],
            [101, 'IT 326 - Database Systems', 3, 1.5, 'Dr. Maria Santos', 'Passed', 'Prelim'],
            [101, 'GE 108 - Ethics', 3, 1.8, 'Ms. Dela Cruz', 'Passed', 'Prelim'],
            [101, 'IT 318 - Web Systems', 3, 1.4, 'Prof. Garcia', 'Passed', 'Prelim'],
            [102, 'CS 210 - Data Structures', 3, 1.2, 'Prof. Molina', 'Passed', 'Prelim'],
            [102, 'MATH 214 - Discrete Math', 3, 1.6, 'Ms. Aquino', 'Passed', 'Prelim'],
            [102, 'CS 218 - Object-Oriented Programming', 3, 1.4, 'Engr. Villanueva', 'Passed', 'Prelim'],
            [102, 'PE 204 - Team Sports', 2, 1.1, 'Coach Ramos', 'Passed', 'Prelim']
        ];
        for (const grade of grades) {
            await run(
                `INSERT INTO academic_grades
                 (student_id, subject_name, units, grade, instructor, remarks, term)
                 VALUES (?, ?, ?, ?, ?, ?, ?)`,
                grade
            );
        }
    }

    const attendanceCount = await get('SELECT COUNT(*) AS count FROM attendance_subjects');
    if (attendanceCount.count === 0) {
        const attendance = [
            [101, 'IT 312 - Mobile Development', 'Prof. Reyes', 17, 18, 1, 0],
            [101, 'IT 326 - Database Systems', 'Dr. Maria Santos', 16, 18, 1, 1],
            [101, 'GE 108 - Ethics', 'Ms. Dela Cruz', 18, 18, 0, 0],
            [101, 'IT 318 - Web Systems', 'Prof. Garcia', 15, 16, 0, 1],
            [102, 'CS 210 - Data Structures', 'Prof. Molina', 18, 18, 0, 0],
            [102, 'MATH 214 - Discrete Math', 'Ms. Aquino', 17, 18, 1, 0],
            [102, 'CS 218 - Object-Oriented Programming', 'Engr. Villanueva', 16, 17, 0, 1],
            [102, 'PE 204 - Team Sports', 'Coach Ramos', 14, 14, 0, 0]
        ];
        for (const item of attendance) {
            await run(
                `INSERT INTO attendance_subjects
                 (student_id, subject_name, instructor, present_days, total_days, late_days, absent_days)
                 VALUES (?, ?, ?, ?, ?, ?, ?)`,
                item
            );
        }
    }

    const performanceCount = await get('SELECT COUNT(*) AS count FROM academic_performance');
    if (performanceCount.count === 0) {
        const performance = [
            [
                101,
                'high_score',
                'High score in exam',
                'IT 312 - Mobile Development',
                'Prof. Reyes',
                'Nathaniel earned one of the highest scores in the Mobile Development practical exam.',
                'The practical exam required students to build a working Android screen, connect it to sample data, and explain the navigation flow. Nathaniel completed the required UI states, used proper spacing, and demonstrated a clear understanding of Compose layout behavior during the checking period.',
                'Functional screen output, correct navigation, readable code structure, and accurate explanation during checking.',
                'event1.jpg',
                '47/50',
                'Completed',
                '2026-05-18',
                '2026-05-18',
                '4hrs ago',
                1
            ],
            [
                101,
                'low_score',
                'Low score in assignment',
                'IT 326 - Database Systems',
                'Dr. Maria Santos',
                'Nathaniel needs improvement in the recent database normalization assignment.',
                'The submitted output missed several required normalization steps and had incomplete explanations for relationship constraints. The instructor recommends reviewing the feedback and preparing corrections before the next database activity.',
                'Complete normalization steps, correct primary and foreign key identification, and clear written explanation of table relationships.',
                'event2.jpg',
                '18/30',
                'Needs Review',
                '2026-05-17',
                '2026-05-17',
                '1 day ago',
                0
            ],
            [
                101,
                'missing_output',
                'Missing laboratory output',
                'IT 318 - Web Systems',
                'Prof. Garcia',
                'Nathaniel has not yet submitted the latest Web Systems laboratory output.',
                'The missing output covers responsive page layout, form validation, and backend request handling. The parent is advised to remind the student to complete and submit the laboratory file as soon as possible.',
                'Submitted source files, working form validation, responsive layout, and screenshots of successful test cases.',
                'event3.jpg',
                null,
                'Missing',
                '2026-05-16',
                '2026-05-20',
                '2 days ago',
                0
            ],
            [
                102,
                'high_score',
                'Excellent data structures quiz',
                'CS 210 - Data Structures',
                'Prof. Molina',
                'Sofia received a high score in the Data Structures long quiz.',
                'The quiz covered trees, graph traversal, sorting algorithms, and complexity analysis. Sofia showed strong understanding of traversal order and algorithm comparison.',
                'Correct answers, clear problem solving, and accurate complexity analysis.',
                'event1.jpg',
                '44/50',
                'Completed',
                '2026-05-16',
                '2026-05-16',
                '2 days ago',
                1
            ],
            [
                102,
                'missing_output',
                'Missing PE activity documentation',
                'PE 204 - Team Sports',
                'Coach Ramos',
                'Sofia still needs to submit the PE activity documentation.',
                'The required documentation includes activity photos, reflection notes, and attendance confirmation for the team sports session.',
                'Complete activity documentation, reflection notes, and attendance confirmation.',
                'event2.jpg',
                null,
                'Missing',
                '2026-05-15',
                '2026-05-19',
                '3 days ago',
                0
            ]
        ];
        for (const item of performance) {
            await run(
                `INSERT INTO academic_performance
                 (student_id, type, title, subject, teacher, summary, details, criteria, image_url, score, status, assigned_date, due_date, time_ago, is_positive)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                item
            );
        }
    }

    const paymentCount = await get('SELECT COUNT(*) AS count FROM payment_records');
    if (paymentCount.count === 0) {
        const payments = [
            [101, '#CDA-0001', 'Student Activity Contribution', 'GCash', '05-15-26 | 9:14 AM', 750.00, 'Activity fee: PHP 500.00\nLaboratory materials: PHP 250.00', 'Paid'],
            [101, '#CDA-0002', 'Laboratory Fee', 'Cashier', '04-18-26 | 2:35 PM', 1200.00, 'IT laboratory fee: PHP 1200.00', 'Paid'],
            [102, '#CDA-0003', 'PE Uniform Fee', 'Pending', '05-01-26 | 8:00 AM', 400.00, 'PE uniform balance: PHP 400.00', 'Pending']
        ];
        for (const payment of payments) {
            await run(
                `INSERT INTO payment_records
                 (student_id, invoice_number, purchased_item, payment_option, paid_date, total_amount, pdf_breakdown, status)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
                payment
            );
        }
    }

    const facultyCount = await get('SELECT COUNT(*) AS count FROM faculty_contacts');
    if (facultyCount.count === 0) {
        const faculty = [
            ['2023-00154', 'Prof. Reyes', 'College of Computer Studies', 'reyes@colegiodealicia.edu.ph', 'Mobile Development'],
            ['2018-00088', 'Dr. Maria Santos', 'College of Computer Studies', 'santos@colegiodealicia.edu.ph', 'Database Systems']
        ];
        for (const contact of faculty) {
            await run(
                `INSERT INTO faculty_contacts
                 (faculty_id, name, department, email, subject)
                 VALUES (?, ?, ?, ?, ?)`,
                contact
            );
        }
    }

    const chatCount = await get('SELECT COUNT(*) AS count FROM chat_messages');
    if (chatCount.count === 0) {
        const messages = [
            ['2023-00154', 'parent_1', 'Good afternoon, Mrs. Santerna. Nathaniel submitted his laboratory activity today.', '2026-05-18T09:23:00Z'],
            ['parent_1', '2023-00154', 'Thank you, Professor. I will remind him about the next deadline.', '2026-05-18T09:30:00Z'],
            ['2018-00088', 'parent_1', 'Database quiz results are now available in the academic monitor.', '2026-05-18T13:10:00Z']
        ];
        for (const message of messages) {
            await run(
                `INSERT INTO chat_messages
                 (sender_id, receiver_id, message, created_at)
                 VALUES (?, ?, ?, ?)`,
                message
            );
        }
    }
}

async function normalizeOfficialData() {
    await run(
        'UPDATE parents SET email = ?, phone = ? WHERE id = ?',
        ['julianamaelloveras@gmail.com', '09082105876', 1]
    );

    const officialEvents = [
        [1, 101, 'Mobile Development Practical Exam', 'Exam', '2026-05-15', '08:00 AM', 'Hands-on Android Compose assessment in Lab 402.', 'Academic', 'event1.jpg'],
        [2, 101, 'Capstone Consultation', 'Academic', '2026-05-17', '03:00 PM', 'Project progress check with Dr. Lim.', 'Reminder', 'event2.jpg'],
        [3, 102, 'Data Structures Long Quiz', 'Exam', '2026-05-16', '09:00 AM', 'Trees, graphs, and sorting algorithms.', 'Academic', 'event3.jpg'],
        [4, null, 'College Assembly', 'College', '2026-05-24', '10:00 AM', 'Required assembly for all CCS students.', 'School-wide', 'event1.jpg'],
        [5, null, 'Parent-Teacher Consultation Day', 'School-wide', '2026-05-30', '01:00 PM', 'Parents may meet instructors by appointment.', 'Reminder', 'event2.jpg'],
        [6, null, 'Sports Day', 'Sports', '2026-06-20', '08:00 AM', 'Inter-school sports competition.', 'Postponed', 'event1.jpg'],
        [7, null, 'Art Workshop', 'Creative', '2026-05-15', '02:00 PM', 'Hands-on painting and sculpting.', 'Normal', 'event2.jpg'],
        [8, null, 'Music Gala', 'Arts', '2026-07-05', '06:00 PM', 'Evening of classical music.', 'Normal', 'event3.jpg'],
        [9, null, 'Summer Camp', 'General', '2026-08-01', '09:00 AM', 'Week-long outdoor activities.', 'Normal', 'event1.jpg'],
        [10, null, 'PTA Meeting', 'Meeting', '2026-04-15', '01:00 PM', 'Discussion on school curriculum.', 'Normal', 'event2.jpg'],
        [11, null, 'Math Olympiad', 'Academic', '2026-04-10', '09:00 AM', 'Regional math competition winners announced.', 'Normal', 'event3.jpg'],
        [12, null, 'Field Trip', 'Excursion', '2026-04-05', '07:00 AM', 'Visit to the National Museum.', 'Normal', 'event1.jpg'],
        [13, null, 'Career Talk', 'Education', '2026-03-28', '10:00 AM', 'Industry experts sharing insights.', 'Normal', 'event2.jpg'],
        [14, null, 'Spring Fest', 'Social', '2026-03-20', '04:00 PM', 'Celebrating the spring season.', 'Cancelled', 'event3.jpg']
    ];
    for (const event of officialEvents) {
        await run(
            `INSERT INTO calendar_events (id, student_id, title, category, date, time, description, status, image_url)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
             ON CONFLICT(id) DO UPDATE SET
                student_id = excluded.student_id,
                title = excluded.title,
                category = excluded.category,
                date = excluded.date,
                time = excluded.time,
                description = excluded.description,
                status = excluded.status,
                image_url = excluded.image_url`,
            event
        );
    }

    const eventNotifications = officialEvents.map(event => {
        const [id, studentId, title, category, date, time, description, status, imageUrl] = event;
        const notificationType = status === 'Cancelled' || status === 'Postponed' ? 'Reminder' : 'Event';
        const audience = studentId ? 'student' : 'school-wide';
        return [
            100 + id,
            studentId,
            `${title}: ${description}`,
            notificationType,
            date,
            audience === 'student' ? category.toLowerCase() : category,
            id <= 5 ? 1 : 0,
            imageUrl,
            1
        ];
    });
    for (const notification of eventNotifications) {
        await run(
            `INSERT INTO notifications (id, student_id, text, type, time, category, is_new, image_url, is_positive)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
             ON CONFLICT(id) DO UPDATE SET
                student_id = excluded.student_id,
                text = excluded.text,
                type = excluded.type,
                time = excluded.time,
                category = excluded.category,
                is_new = excluded.is_new,
                image_url = excluded.image_url,
                is_positive = excluded.is_positive`,
            notification
        );
    }

    const scheduleInstructorUpdates = [
        ['Prof. Reyes', 101, 'IT 312 - Mobile Development'],
        ['Dr. Maria Santos', 101, 'IT 326 - Database Systems']
    ];
    for (const item of scheduleInstructorUpdates) {
        await run(
            'UPDATE class_schedules SET instructor = ? WHERE student_id = ? AND subject = ?',
            item
        );
    }

    const studyLoadInstructorUpdates = [
        ['Prof. Reyes', 'Mobile Development', 101, 'IT 312'],
        ['Dr. Maria Santos', 'Database Systems', 101, 'IT 326'],
        ['Ms. Aquino', 'Discrete Math', 102, 'MATH 214']
    ];
    for (const item of studyLoadInstructorUpdates) {
        await run(
            'UPDATE study_load_subjects SET instructor = ?, title = ? WHERE student_id = ? AND code = ?',
            item
        );
    }

    const faculty = [
        ['2023-00154', 'Prof. Reyes', 'College of Computer Studies', 'reyes@colegiodealicia.edu.ph', 'Mobile Development'],
        ['2018-00088', 'Dr. Maria Santos', 'College of Computer Studies', 'santos@colegiodealicia.edu.ph', 'Database Systems']
    ];
    for (const contact of faculty) {
        await run(
            `INSERT INTO faculty_contacts (faculty_id, name, department, email, subject)
             VALUES (?, ?, ?, ?, ?)
             ON CONFLICT(faculty_id) DO UPDATE SET
                name = excluded.name,
                department = excluded.department,
                email = excluded.email,
                subject = excluded.subject`,
            contact
        );
    }
    await run(
        `DELETE FROM faculty_contacts
         WHERE faculty_id NOT IN ('2023-00154', '2018-00088')`
    );
}

function mapStudent(row, schedules = [], studyLoad = []) {
    return {
        id: row.id,
        name: row.name,
        rollNumber: row.roll_number,
        grade: row.grade,
        section: row.section,
        program: row.program,
        course: row.course,
        year: row.year,
        classTeacher: row.class_teacher,
        attendance: row.attendance,
        gpa: row.gpa,
        pendingPayments: row.pending_payments,
        notificationCount: row.notificationCount || 0,
        profileImageUrl: row.profile_image_url || '',
        backgroundImageUrl: row.background_image_url || '',
        schedules,
        studyLoad
    };
}

function maskEmail(email) {
    const [name, domain] = String(email || '').split('@');
    if (!name || !domain) return email || '';
    const visible = name.slice(0, Math.min(2, name.length));
    return `${visible}${'*'.repeat(Math.max(2, name.length - visible.length))}@${domain}`;
}

function hashOtp(code) {
    return crypto
        .createHash('sha256')
        .update(`${code}:${OTP_SECRET}`)
        .digest('hex');
}

function createOtpCode() {
    return String(crypto.randomInt(100000, 1000000));
}

function createOtpId() {
    return crypto.randomBytes(24).toString('hex');
}

async function sendEmailOtp(email, code) {
    const user = process.env.EMAIL_USER;
    const pass = process.env.EMAIL_APP_PASSWORD;
    const from = process.env.EMAIL_FROM || `Colegio De Alicia <${user}>`;

    if (!user || !pass) {
        throw new Error('Email OTP is not configured. Set EMAIL_USER and EMAIL_APP_PASSWORD.');
    }

    let nodemailer;
    try {
        nodemailer = require('nodemailer');
    } catch (_error) {
        throw new Error('Email dependency missing. Run npm install in the backend folder.');
    }

    const transporter = nodemailer.createTransport({
        host: process.env.EMAIL_HOST || 'smtp.gmail.com',
        port: Number(process.env.EMAIL_PORT || 465),
        secure: String(process.env.EMAIL_SECURE || 'true') !== 'false',
        auth: { user, pass }
    });

    await transporter.sendMail({
        from,
        to: email,
        subject: 'Colegio De Alicia Parent App verification code',
        text: `Your Colegio De Alicia Parent App verification code is ${code}. It expires in ${OTP_TTL_MINUTES} minutes.`,
        html: `<p>Your Colegio De Alicia Parent App verification code is:</p><h2>${code}</h2><p>This code expires in ${OTP_TTL_MINUTES} minutes.</p>`
    });
}

function assertEmailOtpConfigured() {
    if (!process.env.EMAIL_USER || !process.env.EMAIL_APP_PASSWORD) {
        throw new Error('Email OTP is not configured. Set EMAIL_USER and EMAIL_APP_PASSWORD.');
    }
    try {
        require.resolve('nodemailer');
    } catch (_error) {
        throw new Error('Email dependency missing. Run npm install in the backend folder.');
    }
}

async function issueLoginOtp(parent) {
    assertEmailOtpConfigured();

    const code = createOtpCode();
    const otpId = createOtpId();
    const expiresAt = new Date(Date.now() + OTP_TTL_MINUTES * 60 * 1000).toISOString();
    const createdAt = new Date().toISOString();

    await run(
        `INSERT INTO login_otps (id, parent_id, code_hash, expires_at, created_at)
         VALUES (?, ?, ?, ?, ?)`,
        [otpId, parent.id, hashOtp(code), expiresAt, createdAt]
    );
    sendEmailOtp(parent.email, code).catch(error => {
        console.error(`Failed to send OTP email to parent ${parent.id}:`, error.message);
    });

    return {
        otpToken: otpId,
        expiresAt,
        email: maskEmail(parent.email),
        retryAfterSeconds: OTP_RESEND_COOLDOWN_SECONDS
    };
}

function secondsUntilOtpResend(otp) {
    if (!otp?.created_at) return 0;
    const createdAt = new Date(String(otp.created_at).replace(' ', 'T').replace(/Z?$/, 'Z')).getTime();
    if (!Number.isFinite(createdAt)) return 0;
    const elapsedSeconds = Math.floor((Date.now() - createdAt) / 1000);
    return Math.max(0, OTP_RESEND_COOLDOWN_SECONDS - elapsedSeconds);
}

function mapSchedule(row) {
    return {
        subject: row.subject,
        room: row.room,
        instructor: row.instructor,
        day: row.day,
        startTime: row.start_time,
        endTime: row.end_time
    };
}

function mapStudyLoad(row) {
    return {
        scheduleNumber: row.schedule_number,
        courseNumber: row.course_number,
        code: row.code,
        title: row.title,
        units: row.units,
        instructor: row.instructor,
        schedule: row.schedule,
        time: row.time,
        days: row.days,
        room: row.room,
        remarks: row.remarks,
        semester: row.semester,
        schoolYear: row.school_year,
        dateEnrolled: row.date_enrolled
    };
}

function mapGrade(row) {
    return {
        id: row.id,
        studentId: row.student_id,
        subjectName: row.subject_name,
        units: row.units,
        grade: row.grade,
        instructor: row.instructor,
        remarks: row.remarks,
        term: row.term
    };
}

function mapAcademicPerformance(row) {
    return {
        id: row.id,
        studentId: row.student_id,
        type: row.type,
        title: row.title,
        subject: row.subject,
        teacher: row.teacher,
        summary: row.summary,
        details: row.details,
        criteria: row.criteria,
        imageUrl: row.image_url || null,
        score: row.score || null,
        status: row.status,
        assignedDate: row.assigned_date,
        dueDate: row.due_date,
        timeAgo: row.time_ago,
        isPositive: Boolean(row.is_positive)
    };
}

function mapAttendance(row) {
    return {
        id: row.id,
        studentId: row.student_id,
        subjectName: row.subject_name,
        instructor: row.instructor,
        presentDays: row.present_days,
        totalDays: row.total_days,
        lateDays: row.late_days,
        absentDays: row.absent_days
    };
}

function mapPayment(row) {
    return {
        id: row.id,
        studentId: row.student_id,
        invoiceNumber: row.invoice_number,
        purchasedItem: row.purchased_item,
        paymentOption: row.payment_option,
        paidDate: row.paid_date,
        totalAmount: row.total_amount,
        pdfBreakdown: row.pdf_breakdown,
        status: row.status
    };
}

function mapFaculty(row) {
    return {
        facultyId: row.faculty_id,
        name: row.name,
        department: row.department,
        email: row.email,
        subject: row.subject
    };
}

function mapChatMessage(row) {
    return {
        id: row.id,
        sender_id: row.sender_id,
        receiver_id: row.receiver_id,
        message: row.message,
        created_at: row.created_at
    };
}

function saveUploadedImage({ ownerType, ownerId, imageData, mimeType }) {
    if (!imageData) return null;

    const cleanMime = String(mimeType || 'image/jpeg').toLowerCase();
    const extension = cleanMime.includes('png') ? 'png' : cleanMime.includes('webp') ? 'webp' : 'jpg';
    const cleanData = String(imageData).replace(/^data:image\/[a-zA-Z0-9+.-]+;base64,/, '');
    const buffer = Buffer.from(cleanData, 'base64');

    if (!buffer.length || buffer.length > 6 * 1024 * 1024) {
        throw new Error('Image must be a valid file up to 6MB');
    }

    const fileName = `${ownerType}-${ownerId}-${Date.now()}-${crypto.randomBytes(6).toString('hex')}.${extension}`;
    fs.writeFileSync(path.join(UPLOAD_DIR, fileName), buffer);
    return `/media/uploads/${fileName}`;
}

async function getParent(parentId) {
    const parent = await get('SELECT * FROM parents WHERE id = ?', [parentId]);
    if (!parent) return null;

    const children = await all(
        'SELECT student_id FROM parent_students WHERE parent_id = ? ORDER BY student_id',
        [parentId]
    );

    return {
        id: parent.id,
        name: parent.name,
        email: parent.email,
        phone: parent.phone,
        children: children.map(item => item.student_id),
        profileImageUrl: parent.profile_image_url || '',
        backgroundImageUrl: parent.background_image_url || parent.profile_image_url || ''
    };
}

async function getStudyLoadForStudent(studentId) {
    const rows = await all(
        'SELECT * FROM study_load_subjects WHERE student_id = ? ORDER BY sort_order, id',
        [studentId]
    );
    return rows.map(mapStudyLoad);
}

async function getSchedulesForStudent(studentId) {
    const rows = await all(
        `SELECT * FROM class_schedules
         WHERE student_id = ?
         ORDER BY
            CASE day
                WHEN 'Monday' THEN 1
                WHEN 'Tuesday' THEN 2
                WHEN 'Wednesday' THEN 3
                WHEN 'Thursday' THEN 4
                WHEN 'Friday' THEN 5
                WHEN 'Saturday' THEN 6
                WHEN 'Sunday' THEN 7
                ELSE 8
            END,
            start_time`,
        [studentId]
    );
    return rows.map(mapSchedule);
}

async function getStudent(studentId, includeStudyLoad = true) {
    const row = await get('SELECT * FROM students WHERE id = ?', [studentId]);
    if (!row) return null;

    const schedules = await getSchedulesForStudent(studentId);
    const studyLoad = includeStudyLoad ? await getStudyLoadForStudent(studentId) : [];
    return mapStudent(row, schedules, studyLoad);
}

async function buildDashboard(parentId = 1) {
    const parent = await getParent(parentId);
    if (!parent) return null;

    const schoolUnread = await get('SELECT COUNT(*) AS count FROM notifications WHERE is_new = 1 AND student_id IS NULL');
    const children = [];
    for (const childId of parent.children) {
        const child = await getStudent(childId);
        if (child) {
            // Calculate Performance Percentage
            const attendanceVal = parseFloat(child.attendance.replace('%', '')) || 0;

            // Normalized GPA (3.0 is passing limit at 75%)
            let gpaNormalized;
            if (child.gpa <= 3.0) {
                gpaNormalized = 100 - (child.gpa - 1.0) * 12.5; // 1.0 -> 100, 3.0 -> 75
            } else {
                gpaNormalized = 75 - (child.gpa - 3.0) * 37.5;  // 3.0 -> 75, 5.0 -> 0
            }

            const performanceRecords = await all('SELECT is_positive FROM notifications WHERE student_id = ? AND type IN ("Activity", "Reminder")', [childId]);
            const positiveCount = performanceRecords.filter(r => r.is_positive === 1).length;
            const taskScore = performanceRecords.length > 0 ? (positiveCount / performanceRecords.length) * 100 : 100;

            child.performancePercentage = Math.round((gpaNormalized * 0.6) + (attendanceVal * 0.3) + (taskScore * 0.1));

            const childUnread = await get('SELECT COUNT(*) AS count FROM notifications WHERE is_new = 1 AND student_id = ?', [childId]);
            child.notificationCount = childUnread.count + schoolUnread.count;
            children.push(child);
        }
    }

    const childIds = parent.children.length > 0 ? parent.children.join(',') : '0';
    const totalUnread = await get(`SELECT COUNT(*) AS count FROM notifications WHERE is_new = 1 AND (student_id IS NULL OR student_id IN (${childIds}))`);
    const upcomingEvents = await all(
        'SELECT title, date FROM calendar_events ORDER BY date LIMIT 3'
    );

    return {
        parent,
        children,
        unreadAnnouncements: totalUnread.count,
        upcomingEvents: upcomingEvents.map(event => `${event.title} - ${event.date}`)
    };
}

function asyncHandler(handler) {
    return (req, res, next) => Promise.resolve(handler(req, res, next)).catch(next);
}

app.get('/api/health', asyncHandler(async (req, res) => {
    const parentCount = await get('SELECT COUNT(*) AS count FROM parents');
    res.json({
        status: 'OK',
        database: 'connected',
        parents: parentCount.count,
        timestamp: new Date().toISOString()
    });
}));

app.get('/api/app/version', asyncHandler(async (_req, res) => {
    res.json({
        versionCode: APP_VERSION_CODE,
        versionName: APP_VERSION_NAME,
        apkUrl: APP_APK_URL,
        releaseNotes: APP_RELEASE_NOTES
    });
}));

app.post('/api/auth/login', asyncHandler(async (req, res) => {
    const { username, password } = req.body || {};
    const account = await get(
        `SELECT * FROM parent_accounts
         WHERE LOWER(username) = LOWER(?) AND password = ?`,
        [String(username || '').trim(), password]
    );

    if (!account) {
        return res.status(401).json({ error: 'Invalid username or password' });
    }

    const parent = await getParent(account.parent_id);
    const dashboard = await buildDashboard(account.parent_id);
    const parentRow = await get('SELECT * FROM parents WHERE id = ?', [account.parent_id]);

    if (parentRow?.two_factor_enabled) {
        try {
            const otp = await issueLoginOtp(parentRow);
            return res.json({
                requiresTwoFactor: true,
                otpToken: otp.otpToken,
                email: otp.email,
                expiresAt: otp.expiresAt,
                retryAfterSeconds: otp.retryAfterSeconds,
                parent
            });
        } catch (error) {
            return res.status(503).json({ error: error.message });
        }
    }

    res.json({
        requiresTwoFactor: false,
        token: `parent-token-${account.parent_id}`,
        parent,
        dashboard
    });
}));

app.post('/api/auth/verify-otp', asyncHandler(async (req, res) => {
    const { otpToken, code } = req.body || {};
    const otp = await get('SELECT * FROM login_otps WHERE id = ?', [String(otpToken || '')]);

    if (!otp || otp.used) {
        return res.status(401).json({ error: 'Invalid or expired verification code' });
    }

    if (new Date(otp.expires_at).getTime() < Date.now()) {
        return res.status(401).json({ error: 'Verification code has expired' });
    }

    if (otp.attempts >= OTP_MAX_ATTEMPTS) {
        return res.status(429).json({ error: 'Too many verification attempts' });
    }

    const matches = hashOtp(String(code || '').trim()) === otp.code_hash;
    await run('UPDATE login_otps SET attempts = attempts + 1 WHERE id = ?', [otp.id]);

    if (!matches) {
        return res.status(401).json({ error: 'Invalid verification code' });
    }

    await run('UPDATE login_otps SET used = 1 WHERE id = ?', [otp.id]);

    const parent = await getParent(otp.parent_id);
    const dashboard = await buildDashboard(otp.parent_id);
    res.json({
        requiresTwoFactor: false,
        token: `parent-token-${otp.parent_id}`,
        parent,
        dashboard
    });
}));

app.post('/api/auth/resend-otp', asyncHandler(async (req, res) => {
    const { otpToken } = req.body || {};
    const currentOtp = await get('SELECT * FROM login_otps WHERE id = ?', [String(otpToken || '')]);

    if (!currentOtp || currentOtp.used) {
        return res.status(401).json({ error: 'Invalid or expired verification code' });
    }

    if (new Date(currentOtp.expires_at).getTime() < Date.now()) {
        return res.status(401).json({ error: 'Verification code has expired. Please sign in again.' });
    }

    const retryAfterSeconds = secondsUntilOtpResend(currentOtp);
    if (retryAfterSeconds > 0) {
        return res.status(429).json({
            error: `Please wait ${retryAfterSeconds} seconds before requesting another code`,
            retryAfterSeconds
        });
    }

    const parent = await get('SELECT * FROM parents WHERE id = ?', [currentOtp.parent_id]);
    if (!parent?.two_factor_enabled) {
        return res.status(400).json({ error: 'Two-factor authentication is not enabled' });
    }

    try {
        await run('UPDATE login_otps SET used = 1 WHERE id = ?', [currentOtp.id]);
        const otp = await issueLoginOtp(parent);
        return res.json({
            otpToken: otp.otpToken,
            email: otp.email,
            expiresAt: otp.expiresAt,
            retryAfterSeconds: otp.retryAfterSeconds
        });
    } catch (error) {
        return res.status(503).json({ error: error.message });
    }
}));

app.post('/api/auth/parent-login', asyncHandler(async (req, res) => {
    const { parentName } = req.body || {};
    const requestedName = String(parentName || '').trim();
    const parent = await get(
        'SELECT * FROM parents WHERE LOWER(name) = LOWER(?)',
        [requestedName]
    );

    if (!parent && requestedName.toLowerCase() !== 'mrs. santerna' && requestedName.toLowerCase() !== 'mrs santerna') {
        return res.status(401).json({ status: 'error', error: 'Parent not found' });
    }
    const resolvedParent = parent || {
        id: 1,
        name: 'Mrs. Santerna'
    };

    res.json({
        status: 'success',
        token: `parent-token-${resolvedParent.id}`,
        parent_data: {
            userId: `parent_${resolvedParent.id}`,
            parentName: resolvedParent.name
        }
    });
}));

app.get('/api/parent/dashboard', asyncHandler(async (req, res) => {
    const parentId = Number(req.query.parentId || 1);
    const dashboard = await buildDashboard(parentId);
    if (!dashboard) {
        return res.status(404).json({ error: 'Parent not found' });
    }
    res.json(dashboard);
}));

app.get('/api/parent/security', asyncHandler(async (req, res) => {
    const parentId = Number(req.query.parentId || 1);
    const parent = await get('SELECT id, email, phone, two_factor_enabled FROM parents WHERE id = ?', [parentId]);
    if (!parent) {
        return res.status(404).json({ error: 'Parent not found' });
    }
    res.json({
        parentId: parent.id,
        email: parent.email,
        phone: parent.phone,
        twoFactorEnabled: Boolean(parent.two_factor_enabled)
    });
}));

app.patch('/api/parent/security', asyncHandler(async (req, res) => {
    const parentId = Number(req.body?.parentId || req.query.parentId || 1);
    const enabled = Boolean(req.body?.twoFactorEnabled);
    const parent = await get('SELECT id FROM parents WHERE id = ?', [parentId]);
    if (!parent) {
        return res.status(404).json({ error: 'Parent not found' });
    }

    await run('UPDATE parents SET two_factor_enabled = ? WHERE id = ?', [enabled ? 1 : 0, parentId]);
    const updated = await get('SELECT id, email, phone, two_factor_enabled FROM parents WHERE id = ?', [parentId]);
    res.json({
        parentId: updated.id,
        email: updated.email,
        phone: updated.phone,
        twoFactorEnabled: Boolean(updated.two_factor_enabled)
    });
}));

app.patch('/api/parent/profile', asyncHandler(async (req, res) => {
    const parentId = Number(req.body?.parentId || req.query.parentId || 1);
    const parent = await get('SELECT * FROM parents WHERE id = ?', [parentId]);
    if (!parent) {
        return res.status(404).json({ error: 'Parent not found' });
    }

    let profileImageUrl = req.body?.profileImageUrl;
    if (req.body?.profileImageData) {
        try {
            profileImageUrl = saveUploadedImage({
                ownerType: 'parent',
                ownerId: parentId,
                imageData: req.body.profileImageData,
                mimeType: req.body.profileImageMimeType
            });
        } catch (error) {
            return res.status(400).json({ error: error.message });
        }
    }

    const email = req.body?.email === undefined ? parent.email : String(req.body.email || '').trim();
    const phone = req.body?.phone === undefined ? parent.phone : String(req.body.phone || '').trim();

    await run(
        `UPDATE parents
         SET email = ?,
             phone = ?,
             profile_image_url = COALESCE(?, profile_image_url),
             background_image_url = COALESCE(?, background_image_url)
         WHERE id = ?`,
        [
            email,
            phone,
            profileImageUrl === undefined ? null : String(profileImageUrl || '').trim(),
            profileImageUrl === undefined ? null : String(profileImageUrl || '').trim(),
            parentId
        ]
    );

    res.json(await getParent(parentId));
}));

app.get('/api/student/:id/profile', asyncHandler(async (req, res) => {
    const student = await getStudent(Number(req.params.id));
    if (!student) {
        return res.status(404).json({ error: 'Student not found' });
    }
    res.json(student);
}));

app.patch('/api/student/:id/photos', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const student = await get('SELECT id FROM students WHERE id = ?', [studentId]);
    if (!student) {
        return res.status(404).json({ error: 'Student not found' });
    }

    const profileImageUrl = req.body?.profileImageUrl;
    let backgroundImageUrl = req.body?.backgroundImageUrl;
    let uploadedProfileImageUrl;
    if (req.body?.profileImageData) {
        try {
            uploadedProfileImageUrl = saveUploadedImage({
                ownerType: 'student',
                ownerId: studentId,
                imageData: req.body.profileImageData,
                mimeType: req.body.profileImageMimeType
            });
            backgroundImageUrl = uploadedProfileImageUrl;
        } catch (error) {
            return res.status(400).json({ error: error.message });
        }
    }

    if (profileImageUrl === undefined && backgroundImageUrl === undefined && uploadedProfileImageUrl === undefined) {
        return res.status(400).json({ error: 'Provide profileImageUrl, backgroundImageUrl, or profileImageData.' });
    }

    await run(
        `UPDATE students
         SET profile_image_url = COALESCE(?, profile_image_url),
             background_image_url = COALESCE(?, background_image_url)
         WHERE id = ?`,
        [
            uploadedProfileImageUrl || (profileImageUrl === undefined ? null : String(profileImageUrl || '').trim()),
            backgroundImageUrl === undefined ? null : String(backgroundImageUrl || '').trim(),
            studentId
        ]
    );

    res.json(await getStudent(studentId));
}));

app.get('/api/student/:id/studyload', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const student = await get('SELECT id FROM students WHERE id = ?', [studentId]);
    if (!student) {
        return res.status(404).json({ error: 'Student not found' });
    }
    res.json(await getStudyLoadForStudent(studentId));
}));

app.get('/api/student/:id/grades', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const student = await get('SELECT id FROM students WHERE id = ?', [studentId]);
    if (!student) {
        return res.status(404).json({ error: 'Student not found' });
    }
    const rows = await all(
        'SELECT * FROM academic_grades WHERE student_id = ? ORDER BY id',
        [studentId]
    );
    res.json(rows.map(mapGrade));
}));

app.get('/api/student/:id/academic-performance', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const student = await get('SELECT id FROM students WHERE id = ?', [studentId]);
    if (!student) {
        return res.status(404).json({ error: 'Student not found' });
    }
    const rows = await all(
        'SELECT * FROM academic_performance WHERE student_id = ? ORDER BY id',
        [studentId]
    );
    res.json(rows.map(mapAcademicPerformance));
}));

app.get('/api/student/:id/attendance', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const student = await get('SELECT id FROM students WHERE id = ?', [studentId]);
    if (!student) {
        return res.status(404).json({ error: 'Student not found' });
    }
    const rows = await all(
        'SELECT * FROM attendance_subjects WHERE student_id = ? ORDER BY id',
        [studentId]
    );
    res.json(rows.map(mapAttendance));
}));

app.get('/api/student/:id/payments', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const student = await get('SELECT id FROM students WHERE id = ?', [studentId]);
    if (!student) {
        return res.status(404).json({ error: 'Student not found' });
    }
    const rows = await all(
        'SELECT * FROM payment_records WHERE student_id = ? ORDER BY id DESC',
        [studentId]
    );
    res.json(rows.map(mapPayment));
}));

app.post('/api/student/:id/payments', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const student = await get('SELECT id FROM students WHERE id = ?', [studentId]);
    if (!student) {
        return res.status(404).json({ error: 'Student not found' });
    }

    const invoiceNumber = req.body?.invoiceNumber || `#CDA-${Date.now()}`;
    const purchasedItem = req.body?.purchasedItem || 'Contribution dues';
    const paymentOption = req.body?.paymentOption || 'Unspecified';
    const paidDate = req.body?.paidDate || new Date().toLocaleString('en-US');
    const totalAmount = Number(req.body?.totalAmount || 0);
    const pdfBreakdown = req.body?.pdfBreakdown || '';
    const status = req.body?.status || 'Paid';

    await run(
        `INSERT INTO payment_records
         (student_id, invoice_number, purchased_item, payment_option, paid_date, total_amount, pdf_breakdown, status)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
        [studentId, invoiceNumber, purchasedItem, paymentOption, paidDate, totalAmount, pdfBreakdown, status]
    );

    const saved = await get('SELECT * FROM payment_records WHERE invoice_number = ?', [invoiceNumber]);
    res.status(201).json(mapPayment(saved));
}));

app.get('/api/faculty', asyncHandler(async (req, res) => {
    const rows = await all('SELECT * FROM faculty_contacts ORDER BY name');
    res.json(rows.map(mapFaculty));
}));

app.get('/api/chat/history/:facultyId', asyncHandler(async (req, res) => {
    const facultyId = req.params.facultyId;
    const parentId = String(req.query.parentId || 'parent_1');
    const rows = await all(
        `SELECT * FROM chat_messages
         WHERE (sender_id = ? AND receiver_id = ?)
            OR (sender_id = ? AND receiver_id = ?)
         ORDER BY created_at, id`,
        [facultyId, parentId, parentId, facultyId]
    );
    res.json(rows.map(mapChatMessage));
}));

app.post('/api/chat/send', asyncHandler(async (req, res) => {
    const senderId = String(req.body?.sender_id || 'parent_1');
    const receiverId = String(req.body?.receiver_id || '');
    const message = String(req.body?.message || '').trim();

    if (!receiverId || !message) {
        return res.status(400).json({ error: 'receiver_id and message are required' });
    }

    await run(
        'INSERT INTO chat_messages (sender_id, receiver_id, message, created_at) VALUES (?, ?, ?, ?)',
        [senderId, receiverId, message, new Date().toISOString()]
    );
    const saved = await get('SELECT * FROM chat_messages ORDER BY id DESC LIMIT 1');
    res.status(201).json(mapChatMessage(saved));
}));

app.get('/api/notifications', asyncHandler(async (req, res) => {
    const studentId = req.query.studentId ? Number(req.query.studentId) : null;
    const rows = await all(
        `SELECT * FROM notifications
         WHERE student_id IS NULL OR student_id = ?
         ORDER BY is_new DESC, id DESC`,
        [studentId]
    );
    res.json(rows.map(item => ({
        id: item.id,
        studentId: item.student_id,
        text: item.text,
        type: item.type,
        time: item.time,
        category: item.category,
        isNew: Boolean(item.is_new),
        imageUrl: item.image_url || '',
        isPositive: Boolean(item.is_positive)
    })));
}));

app.get('/api/calendar', asyncHandler(async (req, res) => {
    const studentId = req.query.studentId ? Number(req.query.studentId) : null;
    const rows = await all(
        `SELECT id, title, category, date, time, description, status, image_url
         FROM calendar_events
         WHERE student_id IS NULL OR student_id = ?
         ORDER BY date ASC, time ASC`,
        [studentId]
    );

    const events = rows.map(item => ({
        id: item.id,
        title: item.title,
        category: item.category,
        date: item.date,
        time: item.time || "",
        description: item.description,
        eventType: item.category,
        status: item.status || "Normal",
        imageUrl: item.image_url || "event1.jpg"
    }));

    res.json(events);
}));

app.get('/api/announcements', asyncHandler(async (req, res) => {
    const rows = await all('SELECT * FROM notifications ORDER BY is_new DESC, id DESC');
    res.json(rows.map(item => ({
        id: item.id,
        title: item.type,
        content: item.text,
        category: item.category,
        urgent: Boolean(item.is_new)
    })));
}));

app.use((error, req, res, next) => {
    console.error(error);
    res.status(500).json({ error: 'Server error', detail: error.message });
});

initDatabase()
    .then(() => {
        app.listen(PORT, '0.0.0.0', () => {
            console.log(`MIS Backend running on http://0.0.0.0:${PORT}`);
            console.log(`SQLite database: ${DB_PATH}`);
            console.log(`Phone URL example: http://192.168.1.248:${PORT}`);
            console.log('Available endpoints:');
            console.log('  GET /api/health');
            console.log('  GET /api/app/version');
            console.log('  POST /api/auth/login');
            console.log('  POST /api/auth/verify-otp');
            console.log('  POST /api/auth/resend-otp');
            console.log('  GET /api/parent/dashboard');
            console.log('  GET/PATCH /api/parent/security');
            console.log('  PATCH /api/parent/profile');
            console.log('  GET /api/notifications?studentId=101');
            console.log('  GET /api/calendar?studentId=101');
            console.log('  PATCH /api/student/:id/photos');
            console.log('  GET /api/student/:id/studyload');
            console.log('  GET /api/student/:id/grades');
            console.log('  GET /api/student/:id/attendance');
            console.log('  GET /api/student/:id/payments');
            console.log('  GET /api/faculty');
            console.log('  GET /api/chat/history/:facultyId?parentId=parent_1');
        });
    })
    .catch(error => {
        console.error('Failed to initialize database', error);
        process.exit(1);
    });
