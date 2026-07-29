-- MIS Parent App - COMPLETE UNIFIED SCHEMA & SEED SCRIPT
-- Optimized for standard PostgreSQL / Supabase SQL Editor compatibility.

--------------------------------------------------------------------------------
-- STEP 1: CREATE ALL TABLES (IF NOT EXIST)
--------------------------------------------------------------------------------

-- 1. Parents Table
CREATE TABLE IF NOT EXISTS public.parents (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT,
    profile_image_url TEXT NOT NULL DEFAULT '',
    background_image_url TEXT NOT NULL DEFAULT '',
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Students Table
CREATE TABLE IF NOT EXISTS public.students (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    roll_number TEXT NOT NULL UNIQUE,
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
    background_image_url TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Junction Table for Parents and Students
CREATE TABLE IF NOT EXISTS public.parent_students (
    parent_id UUID NOT NULL REFERENCES parents(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    PRIMARY KEY(parent_id, student_id)
);

-- 4. Announcements Table
CREATE TABLE IF NOT EXISTS public.announcements (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    category TEXT NOT NULL,
    urgent BOOLEAN NOT NULL DEFAULT FALSE,
    image_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Class Schedules
CREATE TABLE IF NOT EXISTS public.class_schedules (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject TEXT NOT NULL,
    room TEXT NOT NULL,
    instructor TEXT NOT NULL,
    day TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL
);

-- 6. Study Load Subjects
CREATE TABLE IF NOT EXISTS public.study_load_subjects (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
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
    date_enrolled TEXT NOT NULL
);

-- 7. Calendar Events
CREATE TABLE IF NOT EXISTS public.calendar_events (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    date TEXT NOT NULL,
    time TEXT,
    description TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'Normal',
    image_url TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Academic Performance
CREATE TABLE IF NOT EXISTS public.academic_performance (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
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
    is_positive BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Notifications
CREATE TABLE IF NOT EXISTS public.notifications (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    type TEXT NOT NULL,
    time TEXT NOT NULL,
    category TEXT NOT NULL,
    is_new BOOLEAN NOT NULL DEFAULT TRUE,
    image_url TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. Attendance Subjects
CREATE TABLE IF NOT EXISTS public.attendance_subjects (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject_name TEXT NOT NULL,
    instructor TEXT NOT NULL,
    present_days INTEGER NOT NULL,
    total_days INTEGER NOT NULL,
    late_days INTEGER NOT NULL DEFAULT 0,
    absent_days INTEGER NOT NULL DEFAULT 0
);

-- 11. Academic Grades
CREATE TABLE IF NOT EXISTS public.academic_grades (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject_name TEXT NOT NULL,
    units INTEGER NOT NULL,
    grade DOUBLE PRECISION NOT NULL,
    instructor TEXT NOT NULL,
    remarks TEXT NOT NULL DEFAULT '',
    term TEXT NOT NULL
);

-- 12. App Versions Table
CREATE TABLE IF NOT EXISTS public.app_versions (
    id SERIAL PRIMARY KEY,
    "latestVersionCode" INTEGER NOT NULL,
    "latestVersionName" TEXT NOT NULL,
    "remarks" TEXT,
    "downloadUrl" TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 13. Faculty Contacts Table
CREATE TABLE IF NOT EXISTS public.faculty (
    "facultyId" TEXT PRIMARY KEY,
    "name" TEXT NOT NULL,
    "department" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "subject" TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 14. Payments Table
CREATE TABLE IF NOT EXISTS public.payments (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL,
    "invoiceNumber" TEXT NOT NULL,
    "purchasedItem" TEXT NOT NULL,
    "paymentOption" TEXT NOT NULL,
    "paidDate" TEXT NOT NULL,
    "totalAmount" DOUBLE PRECISION NOT NULL,
    "pdfBreakdown" TEXT,
    "status" TEXT NOT NULL DEFAULT 'Paid',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--------------------------------------------------------------------------------
-- STEP 2: INSERT SAMPLE DATA USING A PROCEDURAL BLOCK
--------------------------------------------------------------------------------

DO $$
DECLARE
    -- !!! CHANGE THIS TO YOUR ACTUAL UUID FROM SUPABASE AUTH !!!
    v_parent_id UUID := '8a214280-bdb5-400b-8dc6-8f26861e67ce';
    v_student1_id INTEGER;
    v_student2_id INTEGER;
BEGIN

    -- 1. Ensure Parent Profile Exists
    INSERT INTO public.parents (id, name, email)
    VALUES (v_parent_id, 'Nathaniel B. McClure', 'parent@example.com')
    ON CONFLICT (id) DO NOTHING;

    -- 2. Insert Students and capture their IDs
    -- Student 1
    INSERT INTO public.students (name, roll_number, grade, section, program, course, year, class_teacher, attendance, gpa, pending_payments)
    VALUES ('Nathaniel McClure Jr.', '2024-00123', 'Grade 10', 'Section A', 'BS Computer Science', 'BSCS', '3rd Year', 'Emily Santerna', '95%', 1.25, 0)
    ON CONFLICT (roll_number) DO UPDATE SET name = EXCLUDED.name
    RETURNING id INTO v_student1_id;

    -- Student 2
    INSERT INTO public.students (name, roll_number, grade, section, program, course, year, class_teacher, attendance, gpa, pending_payments)
    VALUES ('Isabella McClure', '2024-00456', 'Grade 8', 'Section B', 'Basic Education', 'K-12', 'Grade 8', 'Robert Davis', '92%', 1.50, 1200)
    ON CONFLICT (roll_number) DO UPDATE SET name = EXCLUDED.name
    RETURNING id INTO v_student2_id;

    -- 3. Link Students to Parent
    INSERT INTO public.parent_students (parent_id, student_id)
    VALUES (v_parent_id, v_student1_id), (v_parent_id, v_student2_id)
    ON CONFLICT DO NOTHING;

    -- 4. Seed Academic Performance
    INSERT INTO public.academic_performance (student_id, type, title, subject, teacher, summary, details, criteria, score, status, assigned_date, due_date, time_ago, is_positive)
    VALUES
    (v_student1_id, 'QUIZ', 'Data Structures Quiz', 'CS301', 'Emily Santerna', 'Scored 45/50.', 'Binary Trees.', '45/50', '45/50', 'Completed', '2026-07-20', '2026-07-21', '2 days ago', TRUE),
    (v_student2_id, 'ASSIGNMENT', 'Math Homework', 'MATH101', 'Robert Davis', 'Algebra problems.', 'Chapter 2.', 'Pass/Fail', 'Pass', 'Submitted', '2026-07-22', '2026-07-25', 'Just now', TRUE);

    -- 5. Seed Class Schedules
    INSERT INTO public.class_schedules (student_id, subject, room, instructor, day, start_time, end_time)
    VALUES
    (v_student1_id, 'Data Structures', 'Lab 402', 'Emily Santerna', 'Monday', '08:00', '10:00'),
    (v_student1_id, 'Database Systems', 'Lab 305', 'Alan Turing', 'Tuesday', '13:00', '15:00'),
    (v_student2_id, 'Mathematics', 'Room 202', 'Robert Davis', 'Monday', '09:00', '10:30');

    -- 6. Seed Notifications
    INSERT INTO public.notifications (student_id, text, type, "time", category, is_new, is_positive)
    VALUES
    (v_student1_id, 'New grade posted: 1.25', 'GRADE', '10:30 AM', 'Academic', TRUE, TRUE),
    (v_student2_id, 'Marked present in Math', 'ATTENDANCE', '09:00 AM', 'Daily Log', FALSE, TRUE);

    -- 7. Seed Study Load
    INSERT INTO public.study_load_subjects (student_id, schedule_number, course_number, code, title, units, instructor, schedule, "time", days, room, semester, school_year, date_enrolled)
    VALUES
    (v_student1_id, 'S101', 'CS301', 'CS301', 'Data Structures', 3, 'Emily Santerna', 'MW 08:00-10:00', '08:00-10:00', 'Monday,Wednesday', 'Lab 402', '1st Sem.', '2025-2026', '2025-06-01');

    -- 8. Seed Attendance
    INSERT INTO public.attendance_subjects (student_id, subject_name, instructor, present_days, total_days, late_days, absent_days)
    VALUES
    (v_student1_id, 'Data Structures', 'Emily Santerna', 14, 15, 1, 0),
    (v_student2_id, 'Mathematics', 'Robert Davis', 12, 15, 1, 2);

    -- 9. Seed Academic Grades
    INSERT INTO public.academic_grades (student_id, subject_name, units, grade, instructor, term)
    VALUES
    (v_student1_id, 'Data Structures', 3, 1.25, 'Emily Santerna', 'Midterm'),
    (v_student2_id, 'Mathematics', 3, 1.50, 'Robert Davis', 'Prelim');

    -- 10. Seed Payments
    INSERT INTO public.payments (student_id, "invoiceNumber", "purchasedItem", "paymentOption", "paidDate", "totalAmount", "status")
    VALUES
    (v_student1_id, 'INV-2026-001', 'Tuition Fee', 'GCash', '2026-06-15 | 10:30 AM', 5000.00, 'Paid'),
    (v_student2_id, 'INV-2026-008', 'Library Dues', 'Maya', '2026-07-20 | 09:15 AM', 200.00, 'Paid');

END $$;

--------------------------------------------------------------------------------
-- STEP 3: SEED GLOBAL TABLES (No Student Relationship)
--------------------------------------------------------------------------------

-- Seed Announcements
INSERT INTO public.announcements (title, content, category, urgent)
VALUES
('Examination Week', 'Midterm exams start next Monday.', 'Academic', TRUE),
('School Foundation', 'Foundation day on Sept 15.', 'Holiday', FALSE)
ON CONFLICT DO NOTHING;

-- Seed Faculty
INSERT INTO public.faculty ("facultyId", "name", "department", "email", "subject")
VALUES
('fac_001', 'Emily Santerna', 'CS', 'e.santerna@school.edu', 'Data Structures'),
('fac_002', 'Robert Davis', 'GenEd', 'r.davis@school.edu', 'Mathematics')
ON CONFLICT ("facultyId") DO UPDATE SET name = EXCLUDED.name;

-- Seed App Versions
INSERT INTO public.app_versions ("latestVersionCode", "latestVersionName", "remarks")
VALUES (175, '1.1.0', 'Production Release')
ON CONFLICT DO NOTHING;
