const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;

const mockParent = {
    id: 1,
    name: "Jordan McClure",
    email: "jordan.mcclure@email.com",
    phone: "+63 917 555 0198",
    children: [101, 102]
};

const schedules = {
    101: [
        { subject: "IT 312 - Mobile Development", room: "Lab 402", instructor: "Prof. Santos", day: "Monday", startTime: "08:00", endTime: "09:30" },
        { subject: "IT 326 - Database Systems", room: "Room 301", instructor: "Engr. Reyes", day: "Monday", startTime: "10:00", endTime: "11:30" },
        { subject: "GE 108 - Ethics", room: "Room 204", instructor: "Ms. Dela Cruz", day: "Tuesday", startTime: "13:00", endTime: "14:30" },
        { subject: "IT 318 - Web Systems", room: "Lab 407", instructor: "Prof. Garcia", day: "Wednesday", startTime: "09:00", endTime: "10:30" },
        { subject: "IT 330 - Capstone 1", room: "Room 305", instructor: "Dr. Lim", day: "Friday", startTime: "15:00", endTime: "17:00" }
    ],
    102: [
        { subject: "CS 210 - Data Structures", room: "Lab 201", instructor: "Prof. Molina", day: "Monday", startTime: "09:00", endTime: "10:30" },
        { subject: "MATH 214 - Discrete Math", room: "Room 112", instructor: "Ms. Aquino", day: "Tuesday", startTime: "10:00", endTime: "11:30" },
        { subject: "CS 218 - Object-Oriented Programming", room: "Lab 203", instructor: "Engr. Villanueva", day: "Thursday", startTime: "13:30", endTime: "15:00" },
        { subject: "PE 204 - Team Sports", room: "Gym", instructor: "Coach Ramos", day: "Friday", startTime: "08:00", endTime: "10:00" }
    ]
};

const studyLoads = {
    101: [
        { code: "IT 312", title: "Mobile Development", units: 3, instructor: "Prof. Santos", schedule: "Mon 08:00 - 09:30", room: "Lab 402" },
        { code: "IT 326", title: "Database Systems", units: 3, instructor: "Engr. Reyes", schedule: "Mon 10:00 - 11:30", room: "Room 301" },
        { code: "GE 108", title: "Ethics", units: 3, instructor: "Ms. Dela Cruz", schedule: "Tue 13:00 - 14:30", room: "Room 204" },
        { code: "IT 318", title: "Web Systems", units: 3, instructor: "Prof. Garcia", schedule: "Wed 09:00 - 10:30", room: "Lab 407" },
        { code: "IT 330", title: "Capstone 1", units: 3, instructor: "Dr. Lim", schedule: "Fri 15:00 - 17:00", room: "Room 305" }
    ],
    102: [
        { code: "CS 210", title: "Data Structures", units: 3, instructor: "Prof. Molina", schedule: "Mon 09:00 - 10:30", room: "Lab 201" },
        { code: "MATH 214", title: "Discrete Mathematics", units: 3, instructor: "Ms. Aquino", schedule: "Tue 10:00 - 11:30", room: "Room 112" },
        { code: "CS 218", title: "Object-Oriented Programming", units: 3, instructor: "Engr. Villanueva", schedule: "Thu 13:30 - 15:00", room: "Lab 203" },
        { code: "PE 204", title: "Team Sports", units: 2, instructor: "Coach Ramos", schedule: "Fri 08:00 - 10:00", room: "Gym" }
    ]
};

const mockStudents = {
    101: {
        id: 101,
        name: "Nathaniel B. McClure",
        rollNumber: "123456789",
        grade: "3rd Year",
        section: "BSIT 3-A",
        program: "Bachelor of Science in Information Technology",
        course: "BSIT - 3rd year",
        year: "A.Y. 2025-2026",
        classTeacher: "Prof. Santos",
        attendance: "94%",
        gpa: 1.5,
        pendingPayments: 0,
        schedules: schedules[101],
        studyLoad: studyLoads[101]
    },
    102: {
        id: 102,
        name: "Sofia B. McClure",
        rollNumber: "987654321",
        grade: "2nd Year",
        section: "BSCS 2-B",
        program: "Bachelor of Science in Computer Science",
        course: "BSCS - 2nd year",
        year: "A.Y. 2025-2026",
        classTeacher: "Prof. Molina",
        attendance: "97%",
        gpa: 1.3,
        pendingPayments: 400,
        schedules: schedules[102],
        studyLoad: studyLoads[102]
    }
};

const notifications = [
    { id: 1, studentId: 101, text: "Nathaniel has a Mobile Development laboratory activity due today.", type: "Reminders", time: "1hr ago", category: "academic", isNew: true },
    { id: 2, studentId: 101, text: "Database Systems quiz score has been posted.", type: "Student", time: "4hrs ago", category: "academic", isNew: true },
    { id: 3, studentId: 102, text: "Sofia's PE uniform fee is still pending.", type: "Reminders", time: "Yesterday", category: "financial", isNew: true },
    { id: 4, studentId: null, text: "College assembly will be held on May 24 at the auditorium.", type: "College", time: "Yesterday", category: "college", isNew: false },
    { id: 5, studentId: null, text: "Emergency drill schedule has been moved to next week.", type: "Emergency", time: "2 days ago", category: "school-wide", isNew: false }
];

const calendarEvents = [
    { id: 1, studentId: 101, title: "Mobile Development Practical Exam", category: "Exam", date: "2026-05-15", time: "08:00 AM", description: "Hands-on Android Compose assessment in Lab 402.", status: "Academic" },
    { id: 2, studentId: 101, title: "Capstone Consultation", category: "Academic", date: "2026-05-17", time: "03:00 PM", description: "Project progress check with Dr. Lim.", status: "Reminder" },
    { id: 3, studentId: 102, title: "Data Structures Long Quiz", category: "Exam", date: "2026-05-16", time: "09:00 AM", description: "Trees, graphs, and sorting algorithms.", status: "Academic" },
    { id: 4, studentId: null, title: "College Assembly", category: "College", date: "2026-05-24", time: "10:00 AM", description: "Required assembly for all CCS students.", status: "School-wide" },
    { id: 5, studentId: null, title: "Parent-Teacher Consultation Day", category: "School-wide", date: "2026-05-30", time: "01:00 PM", description: "Parents may meet instructors by appointment.", status: "Reminder" }
];

app.get('/api/health', (req, res) => {
    res.json({ status: "OK", timestamp: new Date().toISOString() });
});

app.get('/api/parent/dashboard', (req, res) => {
    res.json({
        parent: mockParent,
        children: mockParent.children.map(id => mockStudents[id]),
        unreadAnnouncements: notifications.filter(item => item.isNew).length,
        upcomingEvents: calendarEvents.slice(0, 3).map(event => `${event.title} - ${event.date}`)
    });
});

app.get('/api/student/:id/profile', (req, res) => {
    const student = mockStudents[Number(req.params.id)];
    if (!student) {
        return res.status(404).json({ error: "Student not found" });
    }
    res.json(student);
});

app.get('/api/student/:id/studyload', (req, res) => {
    const studentId = Number(req.params.id);
    if (!mockStudents[studentId]) {
        return res.status(404).json({ error: "Student not found" });
    }
    res.json(studyLoads[studentId] || []);
});

app.get('/api/notifications', (req, res) => {
    const studentId = req.query.studentId ? Number(req.query.studentId) : null;
    const result = notifications.filter(item => item.studentId === null || item.studentId === studentId);
    res.json(result);
});

app.get('/api/calendar', (req, res) => {
    const studentId = req.query.studentId ? Number(req.query.studentId) : null;
    const result = calendarEvents.filter(item => item.studentId === null || item.studentId === studentId);
    res.json(result);
});

app.get('/api/announcements', (req, res) => {
    res.json(notifications.map(item => ({
        id: item.id,
        title: item.type,
        content: item.text,
        category: item.category,
        urgent: item.isNew
    })));
});

app.listen(PORT, () => {
    console.log(`MIS Backend running on http://localhost:${PORT}`);
    console.log('Available endpoints:');
    console.log('  GET /api/health');
    console.log('  GET /api/parent/dashboard');
    console.log('  GET /api/notifications?studentId=101');
    console.log('  GET /api/calendar?studentId=101');
    console.log('  GET /api/student/:id/studyload');
});
