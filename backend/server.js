const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;

// ========== MOCK DATA (Until sir gives real data) ==========

const mockParent = {
    id: 1,
    name: "Rajesh Kumar",
    email: "rajesh.kumar@email.com",
    phone: "+91 98765 43210",
    children: [101, 102]
};

const mockStudents = {
    101: {
        id: 101,
        name: "Aarav Kumar",
        rollNumber: "2024-001",
        grade: "10th Grade",
        section: "A",
        program: "Science Stream",
        classTeacher: "Mrs. Sharma",
        attendance: "92%",
        gpa: 8.7,
        subjects: ["Mathematics", "Physics", "Chemistry", "English", "Computer Science"]
    },
    102: {
        id: 102,
        name: "Ananya Kumar",
        rollNumber: "2024-045",
        grade: "8th Grade", 
        section: "B",
        program: "General",
        classTeacher: "Mr. Verma",
        attendance: "96%",
        gpa: 9.2,
        subjects: ["Mathematics", "Science", "Social Studies", "English", "Hindi"]
    }
};

const mockAttendance = {
    101: [
        { date: "2024-02-20", status: "present", subject: "Mathematics" },
        { date: "2024-02-19", status: "present", subject: "Physics" },
        { date: "2024-02-18", status: "absent", subject: "Chemistry", reason: "Fever" },
        { date: "2024-02-17", status: "late", subject: "English", reason: "Traffic" },
        { date: "2024-02-16", status: "present", subject: "Computer Science" }
    ],
    102: [
        { date: "2024-02-20", status: "present", subject: "Mathematics" },
        { date: "2024-02-19", status: "present", subject: "Science" },
        { date: "2024-02-18", status: "present", subject: "Social Studies" },
        { date: "2024-02-17", status: "present", subject: "English" },
        { date: "2024-02-16", status: "present", subject: "Hindi" }
    ]
};

const mockAnnouncements = [
    {
        id: 1,
        title: "Annual Day Celebration",
        content: "Annual day will be held on March 15th. All parents are invited.",
        category: "school-wide",
        date: "2024-02-20",
        urgent: false
    },
    {
        id: 2,
        title: "Exam Schedule Released",
        content: "Final exams start from March 1st. Check the detailed schedule.",
        category: "academic",
        date: "2024-02-19",
        urgent: true
    },
    {
        id: 3,
        title: "Fee Payment Reminder",
        content: "Last date for fee payment is February 28th.",
        category: "financial",
        date: "2024-02-18",
        urgent: true
    }
];

// ========== API ROUTES ==========

// Health check
app.get('/api/health', (req, res) => {
    res.json({ status: "OK", timestamp: new Date().toISOString() });
});

// Parent Dashboard
app.get('/api/parent/dashboard', (req, res) => {
    // TODO: Add JWT auth later
    const dashboard = {
        parent: mockParent,
        children: mockParent.children.map(id => ({
            id: id,
            name: mockStudents[id].name,
            grade: mockStudents[id].grade,
            attendance: mockStudents[id].attendance,
            gpa: mockStudents[id].gpa,
            pendingPayments: 1 // Mock
        })),
        unreadAnnouncements: mockAnnouncements.filter(a => a.urgent).length,
        upcomingEvents: ["Parent-Teacher Meeting - March 5", "Annual Day - March 15"]
    };
    res.json(dashboard);
});

// Get specific student profile
app.get('/api/student/:id/profile', (req, res) => {
    const studentId = parseInt(req.params.id);
    const student = mockStudents[studentId];
    
    if (!student) {
        return res.status(404).json({ error: "Student not found" });
    }
    
    res.json(student);
});

// Get student attendance
app.get('/api/student/:id/attendance', (req, res) => {
    const studentId = parseInt(req.params.id);
    const attendance = mockAttendance[studentId] || [];
    
    // Calculate stats
    const total = attendance.length;
    const present = attendance.filter(a => a.status === 'present').length;
    const absent = attendance.filter(a => a.status === 'absent').length;
    const late = attendance.filter(a => a.status === 'late').length;
    
    res.json({
        studentId: studentId,
        records: attendance,
        summary: {
            total,
            present,
            absent,
            late,
            percentage: total > 0 ? Math.round((present/total)*100) : 0
        }
    });
});

// Get all announcements
app.get('/api/announcements', (req, res) => {
    res.json(mockAnnouncements);
});

// Get specific announcement
app.get('/api/announcements/:id', (req, res) => {
    const announcement = mockAnnouncements.find(a => a.id === parseInt(req.params.id));
    if (!announcement) {
        return res.status(404).json({ error: "Announcement not found" });
    }
    res.json(announcement);
});

// ========== START SERVER ==========

app.listen(PORT, () => {
    console.log(`🚀 MIS Backend running on http://localhost:${PORT}`);
    console.log(`📱 Android team can connect using your IP: http://YOUR_IP:${PORT}`);
    console.log('');
    console.log('Available endpoints:');
    console.log('  GET /api/health');
    console.log('  GET /api/parent/dashboard');
    console.log('  GET /api/student/:id/profile');
    console.log('  GET /api/student/:id/attendance');
    console.log('  GET /api/announcements');
});