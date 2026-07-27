-- Supabase Database Schema for MIS Parent App

-- 1. Enable RLS
-- ALL tables should have RLS enabled eventually.

-- 2. Parents Table
CREATE TABLE IF NOT EXISTS parents (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT NOT NULL,
    profile_image_url TEXT NOT NULL DEFAULT '',
    background_image_url TEXT NOT NULL DEFAULT '',
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Students Table
CREATE TABLE IF NOT EXISTS students (
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

-- 4. Junction Table for Parents and Students
CREATE TABLE IF NOT EXISTS parent_students (
    parent_id INTEGER NOT NULL REFERENCES parents(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    PRIMARY KEY(parent_id, student_id)
);

-- 5. Academic Performance
CREATE TABLE IF NOT EXISTS academic_performance (
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

-- 6. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    type TEXT NOT NULL,
    time TEXT NOT NULL,
    category TEXT NOT NULL,
    is_new BOOLEAN NOT NULL DEFAULT TRUE,
    image_url TEXT NOT NULL DEFAULT '',
    is_positive BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Feedback Table
CREATE TABLE IF NOT EXISTS parent_app_feedback (
    id SERIAL PRIMARY KEY,
    user_email TEXT,
    feedback_type TEXT NOT NULL,
    message TEXT NOT NULL,
    app_version TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Class Schedules
CREATE TABLE IF NOT EXISTS class_schedules (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject TEXT NOT NULL,
    room TEXT NOT NULL,
    instructor TEXT NOT NULL,
    day TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL
);

-- 9. Study Load Subjects
CREATE TABLE IF NOT EXISTS study_load_subjects (
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
    date_enrolled TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0
);

-- 10. Attendance Subjects
CREATE TABLE IF NOT EXISTS attendance_subjects (
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
CREATE TABLE IF NOT EXISTS academic_grades (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject_name TEXT NOT NULL,
    units INTEGER NOT NULL,
    grade DOUBLE PRECISION NOT NULL,
    instructor TEXT NOT NULL,
    remarks TEXT NOT NULL DEFAULT '',
    term TEXT NOT NULL
);

-- 12. Calendar Events
CREATE TABLE IF NOT EXISTS calendar_events (
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

-- Row Level Security (RLS) Examples
-- To truly secure this, you should use Supabase Auth uid()
-- ALTER TABLE parents ENABLE ROW LEVEL SECURITY;
-- CREATE POLICY "Parents can view their own data" ON parents FOR SELECT USING (auth.uid() = id::text::uuid);
