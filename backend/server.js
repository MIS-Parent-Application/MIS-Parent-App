const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const nodemailer = require('nodemailer');
const { Pool } = require('pg');

const app = express();
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// Railway PostgreSQL Connection Pool Configuration. PostgreSQL is the single
// backend datastore for parent, student, academic, payment, chat, and feedback data.
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: process.env.DATABASE_SSL === 'false'
    ? false
    : process.env.DATABASE_URL
      ? { rejectUnauthorized: false }
      : false
});

const PORT = process.env.PORT || 3000;
const UPLOAD_DIR = path.join(__dirname, 'data', 'uploads');
const OTP_TTL_MINUTES = Number(process.env.OTP_TTL_MINUTES || 15);
const OTP_MAX_ATTEMPTS = Number(process.env.OTP_MAX_ATTEMPTS || 5);
const OTP_RESEND_COOLDOWN_SECONDS = Number(process.env.OTP_RESEND_COOLDOWN_SECONDS || 60);
const OTP_SECRET = process.env.OTP_SECRET || 'mis-parent-app-dev-otp-secret';

// System Target Constants
const APP_VERSION_CODE = Number(process.env.APP_VERSION_CODE || 2);
const APP_VERSION_NAME = process.env.APP_VERSION_NAME || '1.0.1';
const APP_APK_URL = process.env.APP_APK_URL || 'https://github.com/semi-naan/MIS-Parent-Application/releases';
const APP_RELEASE_NOTES = process.env.APP_RELEASE_NOTES || 'Optimizations for Parent Feature modules.';

// Your version endpoint should look like this
app.get('/api/app/version', (req, res) => {
    try {
        res.status(200).json({
            latestVersionCode: APP_VERSION_CODE,
            latestVersionName: APP_VERSION_NAME,
            remarks: APP_RELEASE_NOTES, // This fixes the UPDATE_REMARKS crash!
            downloadUrl: APP_APK_URL    // This matches your constant name!
        });
    } catch (error) {
        console.error("Endpoint error:", error.message);
        res.status(500).send("Internal Server Error");
    }
});
fs.mkdirSync(UPLOAD_DIR, { recursive: true });
app.use('/media/uploads', express.static(UPLOAD_DIR));

const twoFACodes = {};

const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.EMAIL_USER,
    pass: getEmailPassword(),
  },
});

// Helper for async routes
function asyncHandler(handler) {
    return (req, res, next) => Promise.resolve(handler(req, res, next)).catch(next);
}

// ==========================================
// NEW SYSTEM PRODUCTION ENDPOINTS
// ==========================================

// Server Health Validation check Route
app.get('/api/health', (req, res) => {
    res.json({ status: "healthy", database: "postgres" });
});

// Submit Feedback into Postgres Database with Local Philippine Timestamp
app.post('/api/feedback', async (req, res) => {
    const { userEmail, feedbackType, message, deviceInfo, appVersion } = req.body;

    if (!feedbackType || !message) {
        return res.status(400).json({ error: "Feedback type and message are required fields." });
    }

    try {
        // 1. Explicitly generate the current Philippine Standard Time string
        const phTimestamp = new Date().toLocaleString("en-US", { timeZone: "Asia/Manila" });

        // 2. Pass the timestamp directly into the INSERT statement ($5)
        const queryText = `
            INSERT INTO parent_app_feedback (user_email, feedback_type, message, app_version, created_at)
            VALUES ($1, $2, $3, $4, $5) RETURNING id;
        `;

        const values = [
            userEmail,
            feedbackType,
            message,
            deviceInfo || appVersion || APP_VERSION_NAME,
            phTimestamp
        ];

        const result = await pool.query(queryText, values);

        res.status(201).json({
            success: true,
            message: "Feedback submitted successfully!",
            feedbackId: result.rows[0].id
        });
    } catch (err) {
        console.error('Postgres feedback submission crash:', err.message);
        res.status(500).json({ error: "Failed to store user feedback in production database." });
    }
});

// ==========================================
// AUTHENTICATION ENDPOINTS
// ==========================================

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

    const parent = await get('SELECT * FROM parents WHERE id = ?', [currentOtp.parent_id]);
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

app.get('/api/parent/dashboard', asyncHandler(async (req, res) => {
    const parentId = Number(req.query.parentId || 1);
    const dashboard = await buildDashboard(parentId);
    if (!dashboard) {
        return res.status(404).json({ error: 'Parent not found' });
    }
    res.json(dashboard);
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
        urgent: Boolean(item.is_new),
        imageUrl: item.image_url || null
    })));
}));

app.get('/api/student/:id/attendance', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const rows = await all(
        'SELECT * FROM attendance_subjects WHERE student_id = ? ORDER BY id',
        [studentId]
    );
    res.json(rows.map(mapAttendance));
}));

app.get('/api/student/:id/grades', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const rows = await all(
        'SELECT * FROM academic_grades WHERE student_id = ? ORDER BY id',
        [studentId]
    );
    res.json(rows.map(mapGrade));
}));

app.get('/api/student/:id/academic-performance', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const rows = await all(
        'SELECT * FROM academic_performance WHERE student_id = ? ORDER BY id',
        [studentId]
    );
    res.json(rows.map(mapAcademicPerformance));
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
    const parentId = Number(req.body?.parentId || 1);
    const enabled = Boolean(req.body?.twoFactorEnabled);
    const updated = await get(
        `UPDATE parents
         SET two_factor_enabled = ?
         WHERE id = ?
         RETURNING id, email, phone, two_factor_enabled`,
        [enabled ? 1 : 0, parentId]
    );
    if (!updated) {
        return res.status(404).json({ error: 'Parent not found' });
    }
    res.json({
        parentId: updated.id,
        email: updated.email,
        phone: updated.phone,
        twoFactorEnabled: Boolean(updated.two_factor_enabled)
    });
}));

app.patch('/api/parent/profile', asyncHandler(async (req, res) => {
    const parentId = Number(req.body?.parentId || 1);
    const parent = await get('SELECT * FROM parents WHERE id = ?', [parentId]);
    if (!parent) {
        return res.status(404).json({ error: 'Parent not found' });
    }

    let profileImageUrl = req.body?.profileImageUrl;
    if (typeof req.body?.profileImageData === 'string') {
        if (req.body.profileImageData.trim() === '') {
            profileImageUrl = '';
        } else {
            profileImageUrl = saveBase64Image(
                req.body.profileImageData,
                req.body.profileImageMimeType,
                `parent-${parentId}`
            );
        }
    }

    const updated = await get(
        `UPDATE parents
         SET email = COALESCE(?, email),
             phone = COALESCE(?, phone),
             profile_image_url = COALESCE(?, profile_image_url),
             background_image_url = COALESCE(?, background_image_url)
         WHERE id = ?
         RETURNING *`,
        [
            nullableString(req.body?.email),
            nullableString(req.body?.phone),
            profileImageUrl === undefined ? null : profileImageUrl,
            profileImageUrl === undefined ? null : profileImageUrl,
            parentId
        ]
    );
    res.json(await getParent(updated.id));
}));

app.get('/api/student/:id/studyload', asyncHandler(async (req, res) => {
    const student = await getStudent(Number(req.params.id), true);
    if (!student) {
        return res.status(404).json({ error: 'Student not found' });
    }
    res.json(student.studyLoad);
}));

app.patch('/api/student/:id/photos', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const student = await get('SELECT * FROM students WHERE id = ?', [studentId]);
    if (!student) {
        return res.status(404).json({ error: 'Student not found' });
    }

    let profileImageUrl = req.body?.profileImageUrl;
    if (typeof req.body?.profileImageData === 'string' && req.body.profileImageData.trim() !== '') {
        profileImageUrl = saveBase64Image(
            req.body.profileImageData,
            req.body.profileImageMimeType,
            `student-${studentId}`
        );
    }

    const backgroundImageUrl = req.body?.backgroundImageUrl ?? profileImageUrl;
    await run(
        `UPDATE students
         SET profile_image_url = COALESCE(?, profile_image_url),
             background_image_url = COALESCE(?, background_image_url)
         WHERE id = ?`,
        [
            profileImageUrl === undefined ? null : profileImageUrl,
            backgroundImageUrl === undefined ? null : backgroundImageUrl,
            studentId
        ]
    );
    const updated = await getStudent(studentId);
    res.json(updated);
}));

app.get('/api/student/:id/payments', asyncHandler(async (req, res) => {
    const rows = await all(
        'SELECT * FROM payment_records WHERE student_id = ? ORDER BY id DESC',
        [Number(req.params.id)]
    );
    res.json(rows.map(mapPayment));
}));

app.post('/api/student/:id/payments', asyncHandler(async (req, res) => {
    const studentId = Number(req.params.id);
    const created = await get(
        `INSERT INTO payment_records
         (student_id, invoice_number, purchased_item, payment_option, paid_date, total_amount, pdf_breakdown, status)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?)
         RETURNING *`,
        [
            studentId,
            req.body?.invoiceNumber,
            req.body?.purchasedItem,
            req.body?.paymentOption,
            req.body?.paidDate,
            Number(req.body?.totalAmount || 0),
            req.body?.pdfBreakdown || '',
            req.body?.status || 'Paid'
        ]
    );
    res.status(201).json(mapPayment(created));
}));

app.get('/api/faculty', asyncHandler(async (req, res) => {
    const rows = await all('SELECT * FROM faculty_contacts ORDER BY name');
    res.json(rows.map(mapFaculty));
}));

app.post('/api/auth/parent-login', asyncHandler(async (req, res) => {
    const parentName = String(req.body?.parentName || '').trim();
    const parent = await get(
        `SELECT * FROM parents
         WHERE LOWER(name) = LOWER(?)
         ORDER BY id
         LIMIT 1`,
        [parentName]
    ) || await get('SELECT * FROM parents WHERE id = ?', [1]);

    res.json({
        status: 'success',
        token: `parent-chat-token-${parent.id}`,
        parent_data: {
            userId: `parent_${parent.id}`,
            parentName: parent.name
        }
    });
}));

app.get('/api/chat/history/:facultyId', asyncHandler(async (req, res) => {
    const facultyId = req.params.facultyId;
    const parentId = String(req.query.parentId || 'parent_1');
    const rows = await all(
        `SELECT * FROM chat_messages
         WHERE (sender_id = ? AND receiver_id = ?)
            OR (sender_id = ? AND receiver_id = ?)
         ORDER BY created_at ASC, id ASC`,
        [parentId, facultyId, facultyId, parentId]
    );
    res.json(rows.map(mapChatMessage));
}));

app.post('/api/chat/send', asyncHandler(async (req, res) => {
    const created = await get(
        `INSERT INTO chat_messages (sender_id, receiver_id, message, created_at)
         VALUES (?, ?, ?, ?)
         RETURNING *`,
        [
            req.body?.sender_id || 'parent_1',
            req.body?.receiver_id,
            req.body?.message,
            new Date().toISOString()
        ]
    );
    res.status(201).json(mapChatMessage(created));
}));

// ==========================================
// SECURITY & 2FA ENDPOINTS
// ==========================================

app.post('/api/2fa/send', async (req, res) => {
  const { userId, email } = req.body;
  if (!userId || !email) return res.status(400).json({ message: 'userId and email are required' });

  const code = crypto.randomInt(100000, 999999).toString();
  const expiresAt = Date.now() + 10 * 60 * 1000;
  twoFACodes[userId] = { code, expiresAt };

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

app.post('/api/2fa/verify', (req, res) => {
  const { userId, code } = req.body;
  const record = twoFACodes[userId];

  if (!record) return res.status(400).json({ message: 'No 2FA code found. Request a new one.' });
  if (Date.now() > record.expiresAt) {
    delete twoFACodes[userId];
    return res.status(400).json({ message: 'Code has expired. Request a new one.' });
  }
  if (record.code !== code) return res.status(400).json({ message: 'Invalid code.' });

  delete twoFACodes[userId];
  res.json({ message: '2FA verified successfully' });
});

app.post('/api/2fa/toggle', (req, res) => {
  const { userId, enable } = req.body;
  res.json({ message: `2FA ${enable ? 'enabled' : 'disabled'} successfully` });
});

// ==========================================
// POSTGRES PROMISE WRAPPERS & CORE SCHEMAS
// ==========================================

function toPostgresSql(sql) {
    let index = 0;
    return sql
        .replace(/\?/g, () => `$${++index}`)
        .replace(/REAL/gi, 'DOUBLE PRECISION');
}

function normalizeRow(row) {
    if (!row) return row;
    if (Object.prototype.hasOwnProperty.call(row, 'count')) {
        row.count = Number(row.count);
    }
    return row;
}

async function run(sql, params = []) {
    return pool.query(toPostgresSql(sql), params);
}

async function get(sql, params = []) {
    const result = await pool.query(toPostgresSql(sql), params);
    return normalizeRow(result.rows[0]);
}

async function all(sql, params = []) {
    const result = await pool.query(toPostgresSql(sql), params);
    return result.rows.map(normalizeRow);
}

function toPostgresColumnDefinition(columnDefinition) {
    return columnDefinition
        .replace(/INTEGER/gi, 'INTEGER')
        .replace(/REAL/gi, 'DOUBLE PRECISION');
}

async function ensureColumn(tableName, columnName, columnDefinition) {
    const existing = await get(
        `SELECT column_name
         FROM information_schema.columns
         WHERE table_schema = 'public' AND table_name = ? AND column_name = ?`,
        [tableName, columnName]
    );
    if (!existing) {
        await run(`ALTER TABLE ${tableName} ADD COLUMN ${columnName} ${toPostgresColumnDefinition(columnDefinition)}`);
    }
}

async function initDatabase() {
    if (!process.env.DATABASE_URL) {
        throw new Error('DATABASE_URL is required. The backend now uses PostgreSQL for all app data.');
    }

    try {
        await pool.query(`
            CREATE TABLE IF NOT EXISTS parent_app_feedback (
                id SERIAL PRIMARY KEY,
                user_email TEXT,
                feedback_type TEXT NOT NULL,
                message TEXT NOT NULL,
                app_version TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `);
        console.log('Postgres feedback table verified.');
    } catch (err) {
        console.error('Failed to initialize Postgres table:', err.message);
    }

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
            id SERIAL PRIMARY KEY,
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
            created_at TEXT NOT NULL DEFAULT (now()::text),
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
            gpa DOUBLE PRECISION NOT NULL,
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
            id SERIAL PRIMARY KEY,
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
            id SERIAL PRIMARY KEY,
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
            id SERIAL PRIMARY KEY,
            student_id INTEGER NOT NULL,
            subject_name TEXT NOT NULL,
            units INTEGER NOT NULL,
            grade DOUBLE PRECISION NOT NULL,
            instructor TEXT NOT NULL,
            remarks TEXT NOT NULL DEFAULT '',
            term TEXT NOT NULL,
            FOREIGN KEY(student_id) REFERENCES students(id)
        )
    `);
    await run(`
        CREATE TABLE IF NOT EXISTS academic_performance (
            id SERIAL PRIMARY KEY,
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
            id SERIAL PRIMARY KEY,
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
            id SERIAL PRIMARY KEY,
            student_id INTEGER NOT NULL,
            invoice_number TEXT NOT NULL UNIQUE,
            purchased_item TEXT NOT NULL,
            payment_option TEXT NOT NULL,
            paid_date TEXT NOT NULL,
            total_amount DOUBLE PRECISION NOT NULL,
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
            id SERIAL PRIMARY KEY,
            sender_id TEXT NOT NULL,
            receiver_id TEXT NOT NULL,
            message TEXT NOT NULL,
            created_at TEXT NOT NULL DEFAULT (now()::text)
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
            [101, 'high_score', 'High score in exam', 'IT 312 - Mobile Development', 'Prof. Reyes', 'Nathaniel earned one of the highest scores in the Mobile Development practical exam.', 'Android functional screen setup context details.', 'Explanation validation and alignment mechanics.', 'event1.jpg', '47/50', 'Completed', '2026-05-18', '2026-05-18', '4hrs ago', 1],
            [101, 'low_score', 'Low score in assignment', 'IT 326 - Database Systems', 'Dr. Maria Santos', 'Nathaniel needs improvement in database normalization workflows.', 'The output missed relational rules schemas completely.', 'Full verification analysis structures.', 'event2.jpg', '18/30', 'Needs Review', '2026-05-17', '2026-05-17', '1 day ago', 0],
            [102, 'high_score', 'Excellent quiz result', 'MATH 214 - Discrete Math', 'Ms. Aquino', 'Sofia showed exceptional logic skills in the set theory quiz.', 'Sofia solved all proofs correctly and provided detailed logic steps.', 'Logical consistency and proof accuracy.', 'event1.jpg', '20/20', 'Completed', '2026-05-19', '2026-05-19', '2hrs ago', 1],
            [102, 'missing_output', 'Missing laboratory exercise', 'CS 218 - OOP', 'Engr. Villanueva', 'Sofia has not yet submitted Laboratory #3.', 'The exercise covers inheritance and polymorphism.', 'Functionality and code documentation.', 'event3.jpg', null, 'Pending', '2026-05-15', '2026-05-20', '3 days ago', 0]
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
            [101, '#CDA-0001', 'Student Activity Contribution', 'GCash', '05-15-26 | 9:14 AM', 750.00, 'Activity fee breakdown structures.', 'Paid'],
            [102, '#CDA-0003', 'PE Uniform Fee', 'Pending', '05-01-26 | 8:00 AM', 400.00, 'PE balancing statement logs.', 'Pending']
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
            ['2023-00154', 'parent_1', 'Good afternoon, Mrs. Santerna. Nathaniel submitted his activity today.', '2026-05-18T09:23:00Z']
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

// FIXED: Clean implementation execution sequence for normalizeOfficialData()
async function normalizeOfficialData() {
    await run(
        'UPDATE parents SET email = ?, phone = ? WHERE id = ?',
        ['julianamaelloveras@gmail.com', '09082105876', 1]
    );
    console.log("Database parameters normalized perfectly.");
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
        totalAmount: Number(row.total_amount),
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

function nullableString(value) {
    if (value === undefined || value === null) return null;
    return String(value);
}

function saveBase64Image(base64Data, mimeType, prefix) {
    const extension = String(mimeType || '').includes('png') ? 'png' : 'jpg';
    const fileName = `${prefix}-${Date.now()}.${extension}`;
    const filePath = path.join(UPLOAD_DIR, fileName);
    fs.writeFileSync(filePath, Buffer.from(base64Data, 'base64'));
    return `/media/uploads/${fileName}`;
}

// ==========================================
// CORE DATA ACCESS FUNCTIONS
// ==========================================

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

async function getStudent(studentId, includeStudyLoad = true) {
    const row = await get('SELECT * FROM students WHERE id = ?', [studentId]);
    if (!row) return null;

    const schedules = await all(
        'SELECT * FROM class_schedules WHERE student_id = ? ORDER BY day, start_time',
        [studentId]
    );
    const studyLoad = includeStudyLoad ? await all(
        'SELECT * FROM study_load_subjects WHERE student_id = ? ORDER BY sort_order, id',
        [studentId]
    ) : [];

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
        profileImageUrl: row.profile_image_url || '',
        backgroundImageUrl: row.background_image_url || '',
        schedules: schedules.map(s => ({
            subject: s.subject,
            room: s.room,
            instructor: s.instructor,
            day: s.day,
            startTime: s.start_time,
            endTime: s.end_time
        })),
        studyLoad: studyLoad.map(l => ({
            scheduleNumber: l.schedule_number,
            courseNumber: l.course_number,
            code: l.code,
            title: l.title,
            units: l.units,
            instructor: l.instructor,
            schedule: l.schedule,
            time: l.time,
            days: l.days,
            room: l.room,
            remarks: l.remarks,
            semester: l.semester,
            schoolYear: l.school_year,
            dateEnrolled: l.date_enrolled
        }))
    };
}

async function buildDashboard(parentId = 1) {
    const parent = await getParent(parentId);
    if (!parent) return null;

    const children = [];
    for (const childId of parent.children) {
        const child = await getStudent(childId);
        if (child) {
            // Calculate Performance Percentage for Quick Stats
            const attendanceVal = parseFloat(child.attendance.replace('%', '')) || 0;

            // Normalized GPA (1.0 is 100%, 3.0 is 75%, 5.0 is 0%)
            let gpaNormalized;
            if (child.gpa <= 3.0) {
                gpaNormalized = 100 - (child.gpa - 1.0) * 12.5; // 1.0 -> 100, 3.0 -> 75
            } else {
                gpaNormalized = 75 - (child.gpa - 3.0) * 37.5;  // 3.0 -> 75, 5.0 -> 0
            }

            // Task Score (based on positive performance records)
            const performanceRows = await all(
                'SELECT is_positive FROM academic_performance WHERE student_id = ?',
                [childId]
            );
            const positiveCount = performanceRows.filter(r => Boolean(r.is_positive)).length;
            const taskScore = performanceRows.length > 0 ? (positiveCount / performanceRows.length) * 100 : 100;

            // Final Weighted Performance Score
            child.performancePercentage = Math.round((gpaNormalized * 0.6) + (attendanceVal * 0.3) + (taskScore * 0.1));

            children.push(child);
        }
    }

    const totalUnread = await get('SELECT COUNT(*) AS count FROM notifications WHERE is_new = 1');
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

// ==========================================
// OTP & SECURITY HELPERS
// ==========================================

function hashOtp(code) {
    return crypto.createHash('sha256').update(`${code}:${OTP_SECRET}`).digest('hex');
}

function getEmailPassword() {
    return String(process.env.EMAIL_APP_PASSWORD || process.env.EMAIL_PASS || '')
        .replace(/\s+/g, '')
        .trim();
}

async function sendLoginOtpEmail(parent, code) {
    if (!process.env.EMAIL_USER || !getEmailPassword()) {
        throw new Error('Email OTP is not configured. Set EMAIL_USER and EMAIL_APP_PASSWORD or EMAIL_PASS.');
    }
    await transporter.sendMail({
        from: process.env.EMAIL_FROM || `Colegio De Alicia <${process.env.EMAIL_USER}>`,
        to: parent.email,
        subject: 'Colegio De Alicia Parent App Verification Code',
        text: `Your Colegio De Alicia Parent App verification code is ${code}. It expires in ${OTP_TTL_MINUTES} minutes.`,
        html: `<p>Your Colegio De Alicia Parent App verification code is:</p><h2>${code}</h2><p>This code expires in ${OTP_TTL_MINUTES} minutes.</p>`
    });
}

async function issueLoginOtp(parent) {
    const code = String(crypto.randomInt(100000, 999999));
    const otpId = crypto.randomBytes(24).toString('hex');
    const expiresAt = new Date(Date.now() + OTP_TTL_MINUTES * 60 * 1000).toISOString();

    await run(
        `INSERT INTO login_otps (id, parent_id, code_hash, expires_at)
         VALUES (?, ?, ?, ?)`,
        [otpId, parent.id, hashOtp(code), expiresAt]
    );

    await sendLoginOtpEmail(parent, code);
    console.log(`OTP email sent to ${parent.email.replace(/(..)(.*)(@.*)/, '$1***$3')}`);

    return {
        otpToken: otpId,
        expiresAt,
        email: parent.email.replace(/(..)(.*)(@.*)/, '$1***$3'),
        retryAfterSeconds: OTP_RESEND_COOLDOWN_SECONDS
    };
}

// Core Execution Thread Bootstrap initialization
initDatabase()
  .then(() => {
    app.listen(PORT, () => {
      console.log(`MIS Backend running securely on port: ${PORT}`);
    });
  })
  .catch((err) => {
    console.error("Critical engine initialization crash:", err);
  });
