const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const sqlite3 = require('sqlite3').verbose();

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;
const DB_PATH = process.env.DATABASE_PATH || path.join(__dirname, 'data', 'mis_parent_app.db');

fs.mkdirSync(path.dirname(DB_PATH), { recursive: true });
const db = new sqlite3.Database(DB_PATH);

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

async function initDatabase() {
    await run('PRAGMA foreign_keys = ON');

    await run(`
        CREATE TABLE IF NOT EXISTS parents (
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL,
            email TEXT NOT NULL,
            phone TEXT NOT NULL
        )
    `);
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
            pending_payments INTEGER NOT NULL DEFAULT 0
        )
    `);
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
            is_new INTEGER NOT NULL DEFAULT 0
        )
    `);
    
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
}

async function seedDatabase() {
    await run(
        'INSERT INTO parents (id, name, email, phone) VALUES (?, ?, ?, ?)',
        [1, 'Jordan McClure', 'jordan.mcclure@email.com', '+63 917 555 0198']
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
        [101, 'IT 312 - Mobile Development', 'Lab 402', 'Prof. Santos', 'Monday', '08:00', '09:30'],
        [101, 'IT 326 - Database Systems', 'Room 301', 'Engr. Reyes', 'Monday', '10:00', '11:30'],
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
        [101, '02543', 'IT 312', 'IT 312', 'Mobile Development', 3, 'Prof. Santos', 'Mon 08:00 - 09:30', '08:00 - 09:30 AM', 'MON', 'Lab 402', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 1],
        [101, '33506', 'IT 326', 'IT 326', 'Database Systems', 3, 'Engr. Reyes', 'Mon 10:00 - 11:30', '10:00 - 11:30 AM', 'MON', 'Room 301', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 2],
        [101, '33514', 'GE 108', 'GE 108', 'Ethics', 3, 'Ms. Dela Cruz', 'Tue 13:00 - 14:30', '01:00 - 02:30 PM', 'TUE', 'Room 204', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 3],
        [101, '33522', 'IT 318', 'IT 318', 'Web Systems', 3, 'Prof. Garcia', 'Wed 09:00 - 10:30', '09:00 - 10:30 AM', 'WED', 'Lab 407', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 4],
        [101, '33530', 'IT 330', 'IT 330', 'Capstone 1', 3, 'Dr. Lim', 'Fri 15:00 - 17:00', '03:00 - 05:00 PM', 'FRI', 'Room 305', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 5],
        [102, '34501', 'CS 210', 'CS 210', 'Data Structures', 3, 'Prof. Molina', 'Mon 09:00 - 10:30', '09:00 - 10:30 AM', 'MON', 'Lab 201', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 1],
        [102, '34509', 'MATH 214', 'MATH 214', 'Discrete Mathematics', 3, 'Ms. Aquino', 'Tue 10:00 - 11:30', '10:00 - 11:30 AM', 'TUE', 'Room 112', '', '2nd Sem.', 'S.Y. 2025-2026', '01/29/26', 2],
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
        [1, 101, 'Nathaniel has a Mobile Development laboratory activity due today.', 'Reminders', '1hr ago', 'academic', 1],
        [2, 101, 'Database Systems quiz score has been posted.', 'Student', '4hrs ago', 'academic', 1],
        [3, 102, "Sofia's PE uniform fee is still pending.", 'Reminders', 'Yesterday', 'financial', 1],
        [4, null, 'College assembly will be held on May 24 at the auditorium.', 'College', 'Yesterday', 'college', 0],
        [5, null, 'Emergency drill schedule has been moved to next week.', 'Emergency', '2 days ago', 'school-wide', 0]
    ];
    for (const notification of notifications) {
        await run(
            'INSERT INTO notifications (id, student_id, text, type, time, category, is_new) VALUES (?, ?, ?, ?, ?, ?, ?)',
            notification
        );
    }

    // FIX: Appended event[X].jpg files sequentially to each dummy calendar entry array
    const events = [
        [1, 101, 'Mobile Development Practical Exam', 'Exam', '2026-05-15', '08:00 AM', 'Hands-on Android Compose assessment in Lab 402.', 'Academic', 'event1.jpg'],
        [2, 101, 'Capstone Consultation', 'Academic', '2026-05-17', '03:00 PM', 'Project progress check with Dr. Lim.', 'Reminder', 'event2.jpg'],
        [3, 102, 'Data Structures Long Quiz', 'Exam', '2026-05-16', '09:00 AM', 'Trees, graphs, and sorting algorithms.', 'Academic', 'event3.jpg'],
        [4, null, 'College Assembly', 'College', '2026-05-24', '10:00 AM', 'Required assembly for all CCS students.', 'School-wide', 'event1.jpg'],
        [5, null, 'Parent-Teacher Consultation Day', 'School-wide', '2026-05-30', '01:00 PM', 'Parents may meet instructors by appointment.', 'Reminder', 'event2.jpg']
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
            ['2018-00088', 'Dr. Maria Santos', 'College of Computer Studies', 'santos@colegiodealicia.edu.ph', 'Database Systems'],
            ['2020-00412', 'Ms. Dela Cruz', 'General Education', 'delacruz@colegiodealicia.edu.ph', 'Ethics'],
            ['2021-00642', 'Prof. Garcia', 'College of Computer Studies', 'garcia@colegiodealicia.edu.ph', 'Web Systems']
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
            ['2023-00154', 'parent_1', 'Good afternoon. Nathaniel submitted his laboratory activity today.', '2026-05-18T09:23:00Z'],
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
        schedules,
        studyLoad
    };
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
        children: children.map(item => item.student_id)
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

    const children = [];
    for (const childId of parent.children) {
        const child = await getStudent(childId);
        if (child) children.push(child);
    }

    const unread = await get('SELECT COUNT(*) AS count FROM notifications WHERE is_new = 1');
    const upcomingEvents = await all(
        'SELECT title, date FROM calendar_events ORDER BY date LIMIT 3'
    );

    return {
        parent,
        children,
        unreadAnnouncements: unread.count,
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

    res.json({
        token: `parent-token-${account.parent_id}`,
        parent,
        dashboard
    });
}));

app.post('/api/auth/parent-login', asyncHandler(async (req, res) => {
    const { parentName } = req.body || {};
    const parent = await get(
        'SELECT * FROM parents WHERE LOWER(name) = LOWER(?)',
        [String(parentName || '').trim()]
    );

    if (!parent) {
        return res.status(401).json({ status: 'error', error: 'Parent not found' });
    }

    res.json({
        status: 'success',
        token: `parent-token-${parent.id}`,
        parent_data: {
            userId: `parent_${parent.id}`,
            parentName: parent.name
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

app.get('/api/student/:id/profile', asyncHandler(async (req, res) => {
    const student = await getStudent(Number(req.params.id));
    if (!student) {
        return res.status(404).json({ error: 'Student not found' });
    }
    res.json(student);
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
        isNew: Boolean(item.is_new)
    })));
}));

// FIX: Updated the map payload mapping step to include imageUrl field property output
app.get('/api/calendar', asyncHandler(async (req, res) => {
    const studentId = req.query.studentId ? Number(req.query.studentId) : null;
    const rows = await all(
        `SELECT * FROM calendar_events
         WHERE student_id IS NULL OR student_id = ?
         ORDER BY date, time`,
        [studentId]
    );
    res.json(rows.map(item => ({
        id: item.id,
        studentId: item.student_id,
        title: item.title,
        category: item.category,
        date: item.date,
        time: item.time,
        description: item.description,
        status: item.status,
        imageUrl: item.image_url
    })));
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
            console.log('  POST /api/auth/login');
            console.log('  GET /api/parent/dashboard');
            console.log('  GET /api/notifications?studentId=101');
            console.log('  GET /api/calendar?studentId=101');
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