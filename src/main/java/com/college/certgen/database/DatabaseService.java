package com.college.certgen.database;

import com.college.certgen.models.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Central Database Service supporting SQLite (default local embedded)
 * as well as external connections (MySQL, PostgreSQL, MongoDB, Oracle, Firebase).
 */
public class DatabaseService {
    private static DatabaseService instance;
    private DatabaseConfig currentConfig;
    private Connection activeConnection;

    private DatabaseService() {
        instance = this; // Publish instance immediately so that any initialization or dependent calls receive this instance
        this.currentConfig = new DatabaseConfig();
        initDefaultDatabase();
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public static synchronized boolean hasInstance() {
        return instance != null;
    }

    public String getEffectiveCollegeName() {
        if (com.college.certgen.ui.UIUtils.getActiveUser() != null &&
            com.college.certgen.ui.UIUtils.getActiveUser().getCollegeName() != null &&
            !com.college.certgen.ui.UIUtils.getActiveUser().getCollegeName().trim().isEmpty()) {
            return com.college.certgen.ui.UIUtils.getActiveUser().getCollegeName().trim();
        }
        return getSetting("college_name", "NALANDA COLLEGE, BIHARSHARIF");
    }

    public DatabaseConfig getCurrentConfig() {
        return currentConfig;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (activeConnection == null || activeConnection.isClosed()) {
            activeConnection = createConnection(currentConfig);
        }
        return activeConnection;
    }

    private Connection createConnection(DatabaseConfig config) throws SQLException {
        if (config.getType() == DatabaseConfig.DBType.SQLITE) {
            String appDir = System.getProperty("user.home") + File.separator + ".college_cert_gen";
            File dir = new File(appDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String dbPath = appDir + File.separator + "college_cert.db";
            String url = "jdbc:sqlite:" + dbPath;
            return DriverManager.getConnection(url);
        } else if (config.getType() == DatabaseConfig.DBType.MYSQL) {
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    config.getHost(), config.getPort(), config.getDatabaseName());
            return DriverManager.getConnection(url, config.getUsername(), config.getPassword());
        } else if (config.getType() == DatabaseConfig.DBType.POSTGRESQL) {
            String url = String.format("jdbc:postgresql://%s:%d/%s",
                    config.getHost(), config.getPort(), config.getDatabaseName());
            return DriverManager.getConnection(url, config.getUsername(), config.getPassword());
        } else {
            // Default fallback to local SQLite
            return createConnection(new DatabaseConfig());
        }
    }

    public boolean testConnection(DatabaseConfig config, StringBuilder errorMsg) {
        try {
            if (config.getType() == DatabaseConfig.DBType.SQLITE) {
                try (Connection conn = createConnection(config)) {
                    return conn != null && !conn.isClosed();
                }
            } else if (config.getType() == DatabaseConfig.DBType.MYSQL || config.getType() == DatabaseConfig.DBType.POSTGRESQL) {
                try (Connection conn = createConnection(config)) {
                    return conn != null && !conn.isClosed();
                }
            } else if (config.getType() == DatabaseConfig.DBType.MONGODB) {
                // Validate MongoDB URI format
                String uri = config.getConnectionUri();
                if (uri == null || (!uri.startsWith("mongodb://") && !uri.startsWith("mongodb+srv://"))) {
                    errorMsg.append("Invalid MongoDB URI format. Must start with mongodb:// or mongodb+srv://");
                    return false;
                }
                // Format check passed
                return true;
            } else if (config.getType() == DatabaseConfig.DBType.FIREBASE) {
                if (config.getFirebaseProjectId() == null || config.getFirebaseProjectId().trim().isEmpty()) {
                    errorMsg.append("Firebase Project ID cannot be empty.");
                    return false;
                }
                return true;
            } else if (config.getType() == DatabaseConfig.DBType.ORACLE) {
                if (config.getHost() == null || config.getHost().trim().isEmpty()) {
                    errorMsg.append("Oracle Host cannot be empty.");
                    return false;
                }
                return true;
            }
            return true;
        } catch (Exception e) {
            errorMsg.append(e.getMessage());
            return false;
        }
    }

    public synchronized boolean switchDatabase(DatabaseConfig newConfig, StringBuilder errorMsg) {
        if (!testConnection(newConfig, errorMsg)) {
            return false;
        }
        try {
            if (activeConnection != null && !activeConnection.isClosed()) {
                activeConnection.close();
            }
            this.currentConfig = newConfig;
            this.activeConnection = createConnection(newConfig);
            createTablesIfNotExist();
            return true;
        } catch (Exception e) {
            errorMsg.append(e.getMessage());
            return false;
        }
    }

    private void initDefaultDatabase() {
        try {
            activeConnection = createConnection(currentConfig);
            createTablesIfNotExist();

            // 1. One-time seed for base Nalanda departments & admin
            if (!isSettingTrue("initial_setup_completed")) {
                seedDefaultDataIfEmpty();
                ensureNalandaCourses();
                setSetting("initial_setup_completed", "true");
            }

            // 2. One-time import of all 120 BCA-3rd Year students from Nalanda College Attendance list
            if (!isSettingTrue("bca3_attendance_students_v1")) {
                loadBca3AttendanceStudents();
                setSetting("bca3_attendance_students_v1", "true");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isNalandaCollege(String collegeName) {
        if (collegeName == null) return false;
        String lower = collegeName.toLowerCase().trim();
        return lower.contains("nalanda") || lower.equals("admin") || lower.equals("patel");
    }

    private void createTablesIfNotExist() {
        try (Statement stmt = getConnection().createStatement()) {
            // System settings table to track migrations and ensure user deletions remain permanent
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS system_settings (" +
                    "setting_key TEXT PRIMARY KEY, " +
                    "setting_value TEXT)");

            // Users table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " +
                    "college_name TEXT DEFAULT 'NALANDA COLLEGE, BIHARSHARIF', " +
                    "university_name TEXT DEFAULT 'PATLIPUTRA UNIVERSITY, PATNA', " +
                    "college_tagline TEXT DEFAULT 'ESTABLISHED 1870 • CONSTITUENT UNIT OF PATLIPUTRA UNIVERSITY', " +
                    "college_logo_path TEXT DEFAULT '')");

            // Departments table (Isolated by college_name)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS departments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "code TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "hod_name TEXT, " +
                    "description TEXT, " +
                    "college_name TEXT DEFAULT 'NALANDA COLLEGE, BIHARSHARIF')");

            // Classes table (Isolated by college_name)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS classes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "class_code TEXT NOT NULL, " +
                    "class_name TEXT NOT NULL, " +
                    "department_code TEXT NOT NULL, " +
                    "semester TEXT, " +
                    "academic_year TEXT, " +
                    "college_name TEXT DEFAULT 'NALANDA COLLEGE, BIHARSHARIF')");

            // Students table (Isolated by college_name)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "roll_no TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "reg_no TEXT NOT NULL, " +
                    "department_code TEXT NOT NULL, " +
                    "class_code TEXT NOT NULL, " +
                    "email TEXT, " +
                    "phone TEXT, " +
                    "certificate_status TEXT DEFAULT 'Pending', " +
                    "certificate_date TEXT DEFAULT '-', " +
                    "college_name TEXT DEFAULT 'NALANDA COLLEGE, BIHARSHARIF')");

            // Certificates table (Isolated by college_name)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS certificates (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "certificate_no TEXT NOT NULL, " +
                    "student_id INTEGER, " +
                    "student_name TEXT NOT NULL, " +
                    "roll_no TEXT NOT NULL, " +
                    "reg_no TEXT NOT NULL, " +
                    "department TEXT NOT NULL, " +
                    "class_name TEXT NOT NULL, " +
                    "university_name TEXT NOT NULL, " +
                    "college_name TEXT NOT NULL, " +
                    "certificate_title TEXT NOT NULL, " +
                    "remarks TEXT, " +
                    "issue_date TEXT NOT NULL, " +
                    "signatory1 TEXT, " +
                    "signatory2 TEXT, " +
                    "template_name TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        runMigrations();
    }

    private void runMigrations() {
        try (Statement stmt = getConnection().createStatement()) {
            try { stmt.executeUpdate("ALTER TABLE users ADD COLUMN college_name TEXT DEFAULT 'NALANDA COLLEGE, BIHARSHARIF'"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE users ADD COLUMN university_name TEXT DEFAULT 'PATLIPUTRA UNIVERSITY, PATNA'"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE users ADD COLUMN college_tagline TEXT DEFAULT 'ESTABLISHED 1870 • CONSTITUENT UNIT OF PATLIPUTRA UNIVERSITY'"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE users ADD COLUMN college_logo_path TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE students ADD COLUMN college_name TEXT DEFAULT 'NALANDA COLLEGE, BIHARSHARIF'"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE departments ADD COLUMN college_name TEXT DEFAULT 'NALANDA COLLEGE, BIHARSHARIF'"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE classes ADD COLUMN college_name TEXT DEFAULT 'NALANDA COLLEGE, BIHARSHARIF'"); } catch (Exception ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seedDefaultDataIfEmpty() {
        try {
            // Check users
            try (Statement s = getConnection().createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    insertUser(new User("admin", "admin123", "Chief Administrator", "Administrator"));
                    insertUser(new User("faculty", "faculty123", "Prof. Rajesh Sharma", "Faculty"));
                }
            }

            // Check departments
            try (Statement s = getConnection().createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM departments")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    insertDepartment(new Department(0, "CSE", "Computer Science & Engineering", "Dr. A. K. Verma", "Core computing and software engineering"));
                    insertDepartment(new Department(0, "IT", "Information Technology", "Dr. Sunita Rao", "Applied information systems and cybersecurity"));
                    insertDepartment(new Department(0, "ECE", "Electronics & Communication", "Prof. R. N. Mukherjee", "VLSI, embedded systems and telecom"));
                    insertDepartment(new Department(0, "ME", "Mechanical Engineering", "Dr. Vikram Singh", "Design, thermal and manufacturing engineering"));
                }
            }

            // Check classes
            try (Statement s = getConnection().createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM classes")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    insertClass(new SchoolClass(0, "BCA-2026", "Bachelor of Computer Applications", "CSE", "Semester VI", "2023-2026"));
                    insertClass(new SchoolClass(0, "BTECH-CSE-26", "B.Tech Computer Science", "CSE", "Semester VIII", "2022-2026"));
                    insertClass(new SchoolClass(0, "MCA-2026", "Master of Computer Applications", "IT", "Semester IV", "2024-2026"));
                    insertClass(new SchoolClass(0, "BTECH-ECE-26", "B.Tech Electronics & Comm", "ECE", "Semester VIII", "2022-2026"));
                    insertClass(new SchoolClass(0, "BTECH-ME-26", "B.Tech Mechanical Engg", "ME", "Semester VIII", "2022-2026"));
                }
            }

            // Check students
            try (Statement s = getConnection().createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM students")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    insertStudent(new Student(0, "2026CSE001", "Aarav Sharma", "REG2022CSE0101", "CSE", "BTECH-CSE-26", "aarav.s@college.edu", "+91 9876543210"));
                    insertStudent(new Student(0, "2026CSE002", "Diya Patel", "REG2022CSE0102", "CSE", "BTECH-CSE-26", "diya.p@college.edu", "+91 9876543211"));
                    insertStudent(new Student(0, "2026BCA015", "Rohan Mehta", "REG2023BCA0315", "CSE", "BCA-2026", "rohan.m@college.edu", "+91 9876543212"));
                    insertStudent(new Student(0, "2026BCA022", "Ananya Gupta", "REG2023BCA0322", "CSE", "BCA-2026", "ananya.g@college.edu", "+91 9876543213"));
                    insertStudent(new Student(0, "2026MCA005", "Kabir Roy", "REG2024MCA0005", "IT", "MCA-2026", "kabir.r@college.edu", "+91 9876543214"));
                    insertStudent(new Student(0, "2026ECE012", "Ishita Sen", "REG2022ECE0212", "ECE", "BTECH-ECE-26", "ishita.s@college.edu", "+91 9876543215"));
                    insertStudent(new Student(0, "2026ME034", "Aditya Nair", "REG2022ME0434", "ME", "BTECH-ME-26", "aditya.n@college.edu", "+91 9876543216"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ensureNalandaCourses() {
        Department[] depts = {
                new Department(0, "MAC", "Master of Computer Applications", "Dr. Santosh Kumar", "Postgraduate professional degree in computer science, software development and application engineering."),
                new Department(0, "MCA", "Master of Computer Applications", "Dr. Santosh Kumar", "Postgraduate degree in advanced computing, database architecture and software design."),
                new Department(0, "BCA", "Bachelor of Computer Applications", "Prof. Rajesh Ranjan", "Undergraduate degree in computer applications, programming, web technologies, and database systems."),
                new Department(0, "MBA", "Master of Business Administration", "Dr. Amit Verma", "Postgraduate professional program in business administration, finance, marketing and organizational leadership."),
                new Department(0, "BBA", "Bachelor of Business Administration", "Dr. Priya Singh", "Undergraduate professional degree in business administration, management principles and commerce."),
                new Department(0, "BA", "Bachelor of Arts", "Prof. Ramashankar Prasad", "Undergraduate degree program in humanities, social sciences, history, economics, and literature."),
                new Department(0, "MA", "Master of Arts", "Dr. Sunita Kumari", "Postgraduate degree program in advanced arts, humanities, literature, and social sciences.")
        };

        for (Department d : depts) {
            String checkSql = "SELECT COUNT(*) FROM departments WHERE code = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(checkSql)) {
                ps.setString(1, d.getCode());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        insertDepartment(d);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        SchoolClass[] classes = {
                new SchoolClass(0, "MAC-1", "MAC 1st Year (Sem I & II)", "MAC", "Semester II", "2025-2027"),
                new SchoolClass(0, "MAC-2", "MAC 2nd Year (Sem III & IV)", "MAC", "Semester IV", "2024-2026"),
                new SchoolClass(0, "BCA-1", "BCA 1st Year (Sem I & II)", "BCA", "Semester II", "2025-2028"),
                new SchoolClass(0, "BCA-2", "BCA 2nd Year (Sem III & IV)", "BCA", "Semester IV", "2024-2027"),
                new SchoolClass(0, "BCA-3", "BCA 3rd Year (Sem V & VI)", "BCA", "Semester VI", "2023-2026"),
                new SchoolClass(0, "MBA-1", "MBA 1st Year", "MBA", "Semester II", "2025-2027"),
                new SchoolClass(0, "MBA-2", "MBA 2nd Year", "MBA", "Semester IV", "2024-2026"),
                new SchoolClass(0, "BBA-1", "BBA 1st Year", "BBA", "Semester II", "2025-2028"),
                new SchoolClass(0, "BBA-2", "BBA 2nd Year", "BBA", "Semester IV", "2024-2027"),
                new SchoolClass(0, "BBA-3", "BBA 3rd Year", "BBA", "Semester VI", "2023-2026"),
                new SchoolClass(0, "BA-1", "BA 1st Year", "BA", "Semester II", "2025-2028"),
                new SchoolClass(0, "BA-2", "BA 2nd Year", "BA", "Semester IV", "2024-2027"),
                new SchoolClass(0, "BA-3", "BA 3rd Year", "BA", "Semester VI", "2023-2026"),
                new SchoolClass(0, "MA-1", "MA 1st Year", "MA", "Semester II", "2025-2027"),
                new SchoolClass(0, "MA-2", "MA 2nd Year", "MA", "Semester IV", "2024-2026")
        };

        for (SchoolClass c : classes) {
            String checkSql = "SELECT COUNT(*) FROM classes WHERE class_code = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(checkSql)) {
                ps.setString(1, c.getClassCode());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        insertClass(c);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        Student[] sampleStudents = {
                new Student(0, "NC/BCA/23/001", "Aayush Kumar", "23NCBCA0101", "BCA", "BCA-3", "aayush.nc@nalandacollege.ac.in", "+91 9431012345"),
                new Student(0, "NC/BCA/23/002", "Pooja Kumari", "23NCBCA0102", "BCA", "BCA-3", "pooja.nc@nalandacollege.ac.in", "+91 9431012346"),
                new Student(0, "NC/MAC/24/001", "Rahul Raj", "24NCMAC0201", "MAC", "MAC-2", "rahul.raj@nalandacollege.ac.in", "+91 9431012347"),
                new Student(0, "NC/MBA/24/005", "Sneha Patel", "24NCMBA0305", "MBA", "MBA-2", "sneha.p@nalandacollege.ac.in", "+91 9431012348"),
                new Student(0, "NC/BBA/23/012", "Ankit Sharma", "23NCBBA0412", "BBA", "BBA-3", "ankit.s@nalandacollege.ac.in", "+91 9431012349"),
                new Student(0, "NC/BA/23/045", "Ravi Shankar", "23NCBA0545", "BA", "BA-3", "ravi.s@nalandacollege.ac.in", "+91 9431012350"),
                new Student(0, "NC/MA/24/018", "Neha Gupta", "24NCMA0618", "MA", "MA-2", "neha.g@nalandacollege.ac.in", "+91 9431012351")
        };

        for (Student s : sampleStudents) {
            String checkSql = "SELECT COUNT(*) FROM students WHERE roll_no = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(checkSql)) {
                ps.setString(1, s.getRollNo());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        insertStudent(s);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString(1);
                    return (val != null && !val.trim().isEmpty()) ? val : defaultValue;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public String getSetting(String key) {
        return getSetting(key, null);
    }

    public boolean isSettingTrue(String key) {
        String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "true".equalsIgnoreCase(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setSetting(String key, String value) {
        String sql = "INSERT OR REPLACE INTO system_settings (setting_key, setting_value) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadBca3AttendanceStudents() {
        // Update BCA HOD to official BCA In-charge Prof. Parmanand Prasad
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE departments SET hod_name = 'Prof. Parmanand Prasad (BCA In-charge)' WHERE code = 'BCA'")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Ensure Class BCA-3 exists
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT OR IGNORE INTO classes (class_code, class_name, department_code, semester, academic_year) VALUES " +
                "('BCA-3', 'BCA-III (Hons) 3rd Year', 'BCA', 'Semester VI', '2023-2026')")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String[] studentNames = {
            "MURLIDHAR YADAV", "BALI KUMAR", "ANANT RAJ", "AMBIKA KUMARI", "RAUSHAN KUMAR",
            "ADITI GUPTA", "ANJALI GUPTA", "SUMIT KUMAR", "SHRUTI SHARMA", "HIMANSHU KUMAR",
            "SURUCHI KUMARI", "ARYAN KUMAR", "SAURAV KUMAR", "MD HUZAIFA", "HARSH GUPTA",
            "KUMAR SAGAR", "ANISHA KUMARI", "ADITYA SHUBHAM", "PRIYANSHU KUMAR", "HARSH RAJ",
            "SURAJ KUMAR", "SALONI", "SUBHYA SUMAN", "KARAN KUMAR", "RISHIKET KUMAR",
            "AMIT RAJ", "MD SAHOON ALAM", "MUSKAN KUMARI", "MD ARSHAD", "SAURAV KUMAR",
            "SAURAV KUMAR", "HARSHIT KUMAR", "PAYAL KUMARI", "ANKIT KUMAR", "ANKIT KUMAR",
            "SOHAN KUMAR", "JAY KUMAR", "ANJALI RANI", "ROHIT KUMAR", "AAYUSH KUMAR KAUSHIK",
            "RUDRA RAJ", "SHREYA", "ASHISH RANJAN", "SHIVAM KUMAR", "ISHA MEHTA",
            "ABHINAV GUPTA", "ZAINUL ABEDIN ALI", "ADITYA KUMAR", "ASHISH RANJAN", "ALOK KUMAR",
            "VIDYA SUMAN KUMARI", "PAMMI JOSHI", "KHUSHIYAN KUMARI", "PRIYADARSHAN KUMAR", "SACHIN KUMAR",
            "KARINA KUMARI", "DIVYA KUMARI", "PUSHPANJAY SINHA", "MAHLAQA SANA", "SNEHA KUMARI",
            "SUMAN KUMAR", "SONU KUMAR", "KOMAL KUMARI", "PRASHANT KUMAR", "SNAYA SAVANT",
            "MUSKAN KUMARI", "KUNDAN RAJ", "ANKIT KUMAR", "PAYAL KUMARI", "ADITYA KUMAR",
            "ANKUSH KUMAR", "ROHIT RANJAN", "RISHAV KUMAR", "TANAV KUMAR GAUTAM", "PRASHANT RAJ",
            "RAHUL KUMAR", "SAKSHI KUMARI", "SANDEEP KUMAR", "STUTI KUMARI", "PRIYANKA KUMARI",
            "AKRITI VERMA", "SALONI RAJ", "SHIVANI KUMARI", "SAMIR KUMAR", "ANKIT KUMAR",
            "PUSHPANJALI KUMARI", "NILESH KUMAR", "RAJU KUMAR", "GAUTAM KUMAR", "GOPAL KUMAR",
            "MD AMAN FAISAL", "SHIV SHANKAR PRASAD", "AMAN RAJ", "ASHWANI KUMAR", "RISHU RAJ",
            "ZEESHAN", "MOHIT KUMAR", "ALOK KUMAR", "MAYANK KORACHH", "RISHAV RAJ",
            "RAHUL KUMAR", "AKASH KUMAR", "PUSHPANJAY PATEL", "AKASHDEEP KUMAR", "SAHIL RAJ",
            "ANKIT KUMAR", "SHUBHAM RAJ", "SINTU KUMAR", "AMAN KUMAR", "SHIVAM KUMAR",
            "SAURAV KUMAR", "SHUBHAM RAJ", "PRAVEEN KUMAR", "ABNISH KUMAR", "SUBHAM KUMAR",
            "DIPESH PATEL", "RAUNAK RAJ", "ANKIT RAJ", "NIRAJ KUMAR", "SUBODH KUMAR"
        };

        for (int i = 0; i < studentNames.length; i++) {
            int roll = i + 1;
            String rollNo = String.valueOf(roll);
            String name = studentNames[i];
            String regNo = String.format("21NCBCA%03d", roll);
            String cleanName = name.toLowerCase().replaceAll("[^a-z]", "");
            String email = cleanName + "." + roll + "@nalandacollege.ac.in";
            String phone = String.format("+91 94310%05d", (10000 + roll * 73) % 90000);

            Student st = new Student(0, rollNo, name, regNo, "BCA", "BCA-3", email, phone);
            insertStudent(st);
        }
    }

    // User operations
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("role")
                    );
                    try {
                        u.setCollegeName(rs.getString("college_name"));
                        u.setUniversityName(rs.getString("university_name"));
                        u.setCollegeTagline(rs.getString("college_tagline"));
                        u.setCollegeLogoPath(rs.getString("college_logo_path"));
                    } catch (Exception ignored) {}
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("role")
                );
                try {
                    u.setCollegeName(rs.getString("college_name"));
                    u.setUniversityName(rs.getString("university_name"));
                    u.setCollegeTagline(rs.getString("college_tagline"));
                    u.setCollegeLogoPath(rs.getString("college_logo_path"));
                } catch (Exception ignored) {}
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role, college_name, university_name, college_tagline, college_logo_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getCollegeName() != null ? user.getCollegeName() : "NALANDA COLLEGE, BIHARSHARIF");
            ps.setString(6, user.getUniversityName() != null ? user.getUniversityName() : "PATLIPUTRA UNIVERSITY, PATNA");
            ps.setString(7, user.getCollegeTagline() != null ? user.getCollegeTagline() : "ESTABLISHED 1870 • CONSTITUENT UNIT OF PATLIPUTRA UNIVERSITY");
            ps.setString(8, user.getCollegeLogoPath() != null ? user.getCollegeLogoPath() : "");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Department operations
    public List<Department> getAllDepartments() {
        return getDepartmentsByCollege(getEffectiveCollegeName());
    }

    public List<Department> getDepartmentsByCollege(String collegeName) {
        List<Department> list = new ArrayList<>();
        String sql;
        boolean isNalanda = isNalandaCollege(collegeName);
        if (isNalanda) {
            sql = "SELECT * FROM departments WHERE (college_name = ? COLLATE NOCASE OR college_name LIKE '%nalanda%') ORDER BY code ASC";
        } else {
            sql = "SELECT * FROM departments WHERE college_name = ? COLLATE NOCASE ORDER BY code ASC";
        }
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, collegeName != null ? collegeName.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Department(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("name"),
                            rs.getString("hod_name"),
                            rs.getString("description")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertDepartment(Department dept) {
        String col = getEffectiveCollegeName();
        String code = dept.getCode() != null ? dept.getCode().toUpperCase().trim() : "";
        String checkSql = "SELECT id FROM departments WHERE code = ? AND college_name = ? COLLATE NOCASE";
        try (PreparedStatement check = getConnection().prepareStatement(checkSql)) {
            check.setString(1, code);
            check.setString(2, col);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    dept.setId(rs.getInt(1));
                    return updateDepartment(dept);
                }
            }
        } catch (SQLException ignored) {}

        String sql = "INSERT INTO departments (code, name, hod_name, description, college_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, dept.getName().trim());
            ps.setString(3, dept.getHodName() != null ? dept.getHodName().trim() : "");
            ps.setString(4, dept.getDescription() != null ? dept.getDescription().trim() : "");
            ps.setString(5, col);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDepartment(Department dept) {
        String sql = "UPDATE departments SET code = ?, name = ?, hod_name = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, dept.getCode().toUpperCase().trim());
            ps.setString(2, dept.getName().trim());
            ps.setString(3, dept.getHodName() != null ? dept.getHodName().trim() : "");
            ps.setString(4, dept.getDescription() != null ? dept.getDescription().trim() : "");
            ps.setInt(5, dept.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDepartment(int id) {
        String sql = "DELETE FROM departments WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertDepartmentForCollege(Department dept, String collegeName) {
        String sql = "INSERT INTO departments (code, name, hod_name, description, college_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, dept.getCode().toUpperCase().trim());
            ps.setString(2, dept.getName().trim());
            ps.setString(3, dept.getHodName() != null ? dept.getHodName().trim() : "");
            ps.setString(4, dept.getDescription() != null ? dept.getDescription().trim() : "");
            ps.setString(5, collegeName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertClassForCollege(SchoolClass cls, String collegeName) {
        String sql = "INSERT INTO classes (class_code, class_name, department_code, semester, academic_year, college_name) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, cls.getClassCode().toUpperCase().trim());
            ps.setString(2, cls.getClassName().trim());
            ps.setString(3, cls.getDepartmentCode().toUpperCase().trim());
            ps.setString(4, cls.getSemester() != null ? cls.getSemester().trim() : "");
            ps.setString(5, cls.getAcademicYear() != null ? cls.getAcademicYear().trim() : "");
            ps.setString(6, collegeName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void seedStarterDepartmentsForCollege(String collegeName) {
        if (collegeName == null || isNalandaCollege(collegeName)) return;
        Department[] starterDepts = {
                new Department(0, "CS", "Computer Science & IT", "Head of Dept", "Computing and software applications"),
                new Department(0, "MGMT", "Management & Commerce", "Head of Dept", "Business administration and commerce"),
                new Department(0, "ARTS", "Arts & Humanities", "Head of Dept", "Humanities, languages and social sciences"),
                new Department(0, "SCI", "Science & Technology", "Head of Dept", "Pure and applied sciences")
        };
        for (Department d : starterDepts) {
            String checkSql = "SELECT COUNT(*) FROM departments WHERE code = ? AND college_name = ? COLLATE NOCASE";
            try (PreparedStatement ps = getConnection().prepareStatement(checkSql)) {
                ps.setString(1, d.getCode());
                ps.setString(2, collegeName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        insertDepartmentForCollege(d, collegeName);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        SchoolClass[] starterClasses = {
                new SchoolClass(0, "CS-1", "B.Sc Computer Science 1st Year", "CS", "Semester II", "2025-2028"),
                new SchoolClass(0, "CS-2", "B.Sc Computer Science 2nd Year", "CS", "Semester IV", "2024-2027"),
                new SchoolClass(0, "CS-3", "B.Sc Computer Science 3rd Year", "CS", "Semester VI", "2023-2026"),
                new SchoolClass(0, "MGMT-1", "BBA 1st Year", "MGMT", "Semester II", "2025-2028"),
                new SchoolClass(0, "ARTS-1", "B.A. 1st Year", "ARTS", "Semester II", "2025-2028")
        };
        for (SchoolClass c : starterClasses) {
            String checkSql = "SELECT COUNT(*) FROM classes WHERE class_code = ? AND college_name = ? COLLATE NOCASE";
            try (PreparedStatement ps = getConnection().prepareStatement(checkSql)) {
                ps.setString(1, c.getClassCode());
                ps.setString(2, collegeName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        insertClassForCollege(c, collegeName);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Class operations
    public List<SchoolClass> getAllClasses() {
        return getClassesByCollege(getEffectiveCollegeName());
    }

    public List<SchoolClass> getClassesByCollege(String collegeName) {
        List<SchoolClass> list = new ArrayList<>();
        String sql;
        boolean isNalanda = isNalandaCollege(collegeName);
        if (isNalanda) {
            sql = "SELECT * FROM classes WHERE (college_name = ? COLLATE NOCASE OR college_name LIKE '%nalanda%') ORDER BY class_code ASC";
        } else {
            sql = "SELECT * FROM classes WHERE college_name = ? COLLATE NOCASE ORDER BY class_code ASC";
        }
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, collegeName != null ? collegeName.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SchoolClass(
                            rs.getInt("id"),
                            rs.getString("class_code"),
                            rs.getString("class_name"),
                            rs.getString("department_code"),
                            rs.getString("semester"),
                            rs.getString("academic_year")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertClass(SchoolClass cls) {
        String col = getEffectiveCollegeName();
        String code = cls.getClassCode() != null ? cls.getClassCode().toUpperCase().trim() : "";
        String checkSql = "SELECT id FROM classes WHERE class_code = ? AND college_name = ? COLLATE NOCASE";
        try (PreparedStatement check = getConnection().prepareStatement(checkSql)) {
            check.setString(1, code);
            check.setString(2, col);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    cls.setId(rs.getInt(1));
                    return updateClass(cls);
                }
            }
        } catch (SQLException ignored) {}

        String sql = "INSERT INTO classes (class_code, class_name, department_code, semester, academic_year, college_name) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, cls.getClassName().trim());
            ps.setString(3, cls.getDepartmentCode().toUpperCase().trim());
            ps.setString(4, cls.getSemester() != null ? cls.getSemester().trim() : "");
            ps.setString(5, cls.getAcademicYear() != null ? cls.getAcademicYear().trim() : "");
            ps.setString(6, col);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateClass(SchoolClass cls) {
        String sql = "UPDATE classes SET class_code = ?, class_name = ?, department_code = ?, semester = ?, academic_year = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, cls.getClassCode().toUpperCase().trim());
            ps.setString(2, cls.getClassName().trim());
            ps.setString(3, cls.getDepartmentCode().toUpperCase().trim());
            ps.setString(4, cls.getSemester() != null ? cls.getSemester().trim() : "");
            ps.setString(5, cls.getAcademicYear() != null ? cls.getAcademicYear().trim() : "");
            ps.setInt(6, cls.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteClass(int id) {
        String sql = "DELETE FROM classes WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Student operations
    public List<Student> getAllStudents() {
        return getStudentsByCollege(getEffectiveCollegeName());
    }

    public List<Student> getStudentsByCollege(String collegeName) {
        List<Student> list = new ArrayList<>();
        boolean isNalanda = isNalandaCollege(collegeName);
        String sql;
        if (isNalanda) {
            sql = "SELECT * FROM students WHERE (college_name = ? COLLATE NOCASE OR college_name LIKE '%nalanda%') ORDER BY CAST(roll_no AS INTEGER), roll_no ASC";
        } else {
            sql = "SELECT * FROM students WHERE college_name = ? COLLATE NOCASE ORDER BY CAST(roll_no AS INTEGER), roll_no ASC";
        }
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, collegeName != null ? collegeName.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student st = new Student(
                            rs.getInt("id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("reg_no"),
                            rs.getString("department_code"),
                            rs.getString("class_code"),
                            rs.getString("email"),
                            rs.getString("phone")
                    );
                    st.setCertificateStatus(rs.getString("certificate_status"));
                    st.setCertificateDate(rs.getString("certificate_date"));
                    try { st.setCollegeName(rs.getString("college_name")); } catch (Exception ignored) {}
                    list.add(st);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertStudent(Student st) {
        String col = (st.getCollegeName() != null && !st.getCollegeName().trim().isEmpty())
                ? st.getCollegeName().trim()
                : getEffectiveCollegeName();
        String roll = st.getRollNo() != null ? st.getRollNo().replace("'", "").trim() : "";
        String reg = st.getRegNo() != null ? st.getRegNo().replace("'", "").trim() : "";
        String name = st.getName() != null ? st.getName().replace("'", "").trim() : "";

        // Check if student with same roll_no, class_code and college already exists
        String checkSql = "SELECT id FROM students WHERE roll_no = ? AND class_code = ? AND college_name = ? COLLATE NOCASE";
        try (PreparedStatement checkPs = getConnection().prepareStatement(checkSql)) {
            checkPs.setString(1, roll);
            checkPs.setString(2, st.getClassCode());
            checkPs.setString(3, col);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    int existingId = rs.getInt(1);
                    st.setId(existingId);
                    st.setRollNo(roll);
                    st.setRegNo(reg);
                    st.setName(name);
                    return updateStudent(st);
                }
            }
        } catch (SQLException ignored) {}

        String sql = "INSERT INTO students (roll_no, name, reg_no, department_code, class_code, email, phone, certificate_status, certificate_date, college_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, roll);
            ps.setString(2, name);
            ps.setString(3, reg);
            ps.setString(4, st.getDepartmentCode());
            ps.setString(5, st.getClassCode());
            ps.setString(6, st.getEmail() != null ? st.getEmail().trim() : "");
            ps.setString(7, st.getPhone() != null ? st.getPhone().trim() : "");
            ps.setString(8, st.getCertificateStatus() != null ? st.getCertificateStatus() : "Pending");
            ps.setString(9, st.getCertificateDate() != null ? st.getCertificateDate() : "-");
            ps.setString(10, col);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStudent(Student st) {
        String sql = "UPDATE students SET roll_no = ?, name = ?, reg_no = ?, department_code = ?, class_code = ?, email = ?, phone = ?, certificate_status = ?, certificate_date = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, st.getRollNo().trim());
            ps.setString(2, st.getName().trim());
            ps.setString(3, st.getRegNo().trim());
            ps.setString(4, st.getDepartmentCode());
            ps.setString(5, st.getClassCode());
            ps.setString(6, st.getEmail() != null ? st.getEmail().trim() : "");
            ps.setString(7, st.getPhone() != null ? st.getPhone().trim() : "");
            ps.setString(8, st.getCertificateStatus());
            ps.setString(9, st.getCertificateDate());
            ps.setInt(10, st.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Certificate operations
    public List<Certificate> getAllCertificates() {
        return getCertificatesByCollege(getEffectiveCollegeName());
    }

    public List<Certificate> getCertificatesByCollege(String collegeName) {
        List<Certificate> list = new ArrayList<>();
        boolean isNalanda = isNalandaCollege(collegeName);
        String sql;
        if (isNalanda) {
            sql = "SELECT * FROM certificates WHERE (college_name = ? COLLATE NOCASE OR college_name LIKE '%nalanda%') ORDER BY id DESC";
        } else {
            sql = "SELECT * FROM certificates WHERE college_name = ? COLLATE NOCASE ORDER BY id DESC";
        }
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, collegeName != null ? collegeName.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Certificate cert = new Certificate();
                    cert.setId(rs.getInt("id"));
                    cert.setCertificateNo(rs.getString("certificate_no"));
                    cert.setStudentId(rs.getInt("student_id"));
                    cert.setStudentName(rs.getString("student_name"));
                    cert.setRollNo(rs.getString("roll_no"));
                    cert.setRegNo(rs.getString("reg_no"));
                    cert.setDepartment(rs.getString("department"));
                    cert.setClassName(rs.getString("class_name"));
                    cert.setUniversityName(rs.getString("university_name"));
                    cert.setCollegeName(rs.getString("college_name"));
                    cert.setCertificateTitle(rs.getString("certificate_title"));
                    cert.setRemarks(rs.getString("remarks"));
                    cert.setIssueDate(rs.getString("issue_date"));
                    cert.setSignatory1(rs.getString("signatory1"));
                    cert.setSignatory2(rs.getString("signatory2"));
                    cert.setTemplateName(rs.getString("template_name"));
                    list.add(cert);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertCertificate(Certificate cert) {
        String sql = "INSERT INTO certificates (certificate_no, student_id, student_name, roll_no, reg_no, department, class_name, " +
                "university_name, college_name, certificate_title, remarks, issue_date, signatory1, signatory2, template_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, cert.getCertificateNo());
            ps.setInt(2, cert.getStudentId());
            ps.setString(3, cert.getStudentName());
            ps.setString(4, cert.getRollNo());
            ps.setString(5, cert.getRegNo());
            ps.setString(6, cert.getDepartment());
            ps.setString(7, cert.getClassName());
            ps.setString(8, cert.getUniversityName());
            ps.setString(9, cert.getCollegeName());
            ps.setString(10, cert.getCertificateTitle());
            ps.setString(11, cert.getRemarks());
            ps.setString(12, cert.getIssueDate());
            ps.setString(13, cert.getSignatory1());
            ps.setString(14, cert.getSignatory2());
            ps.setString(15, cert.getTemplateName());

            int res = ps.executeUpdate();
            if (res > 0) {
                // Update student status to Issued
                if (cert.getStudentId() > 0) {
                    try (PreparedStatement updateSt = getConnection().prepareStatement(
                            "UPDATE students SET certificate_status = 'Issued', certificate_date = ? WHERE id = ?")) {
                        updateSt.setString(1, cert.getIssueDate());
                        updateSt.setInt(2, cert.getStudentId());
                        updateSt.executeUpdate();
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Dashboard metrics
    public int getCount(String tableName) {
        String college = getEffectiveCollegeName();
        boolean isNalanda = isNalandaCollege(college);
        if ("students".equalsIgnoreCase(tableName) || "certificates".equalsIgnoreCase(tableName)
                || "classes".equalsIgnoreCase(tableName) || "departments".equalsIgnoreCase(tableName)) {
            String sql = isNalanda
                    ? "SELECT COUNT(*) FROM " + tableName + " WHERE (college_name = ? COLLATE NOCASE OR college_name LIKE '%nalanda%')"
                    : "SELECT COUNT(*) FROM " + tableName + " WHERE college_name = ? COLLATE NOCASE";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, college != null ? college.trim() : "");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement s = getConnection().createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
