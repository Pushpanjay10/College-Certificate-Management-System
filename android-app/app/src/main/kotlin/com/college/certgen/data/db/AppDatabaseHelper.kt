package com.college.certgen.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.college.certgen.data.models.*

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "college_certgen_mobile.db"
        const val DATABASE_VERSION = 2

        const val TABLE_USERS = "users"
        const val TABLE_STUDENTS = "students"
        const val TABLE_CERTIFICATES = "certificates"
        const val TABLE_DEPARTMENTS = "departments"
        const val TABLE_CLASSES = "classes"

        @Volatile
        private var instance: AppDatabaseHelper? = null

        fun getInstance(context: Context): AppDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: AppDatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL,
                college_name TEXT NOT NULL,
                university_name TEXT NOT NULL,
                college_tagline TEXT NOT NULL,
                hod_name TEXT NOT NULL,
                principal_name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_DEPARTMENTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT NOT NULL,
                name TEXT NOT NULL,
                hod_name TEXT NOT NULL,
                description TEXT,
                college_name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CLASSES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                class_code TEXT NOT NULL,
                class_name TEXT NOT NULL,
                department_code TEXT NOT NULL,
                semester TEXT,
                academic_year TEXT,
                college_name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_STUDENTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                roll_no TEXT NOT NULL,
                name TEXT NOT NULL,
                reg_no TEXT NOT NULL,
                department_code TEXT NOT NULL,
                class_code TEXT NOT NULL,
                email TEXT,
                phone TEXT,
                college_name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CERTIFICATES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                certificate_no TEXT NOT NULL,
                student_id INTEGER,
                student_name TEXT NOT NULL,
                roll_no TEXT NOT NULL,
                reg_no TEXT NOT NULL,
                department TEXT NOT NULL,
                class_name TEXT NOT NULL,
                university_name TEXT NOT NULL,
                college_name TEXT NOT NULL,
                certificate_title TEXT NOT NULL,
                remarks TEXT,
                issue_date TEXT NOT NULL,
                signatory1 TEXT,
                signatory2 TEXT,
                template_name TEXT DEFAULT 'Academic Certificate',
                pdf_path TEXT
            )
        """.trimIndent())

        seedDefaultData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_CERTIFICATES ADD COLUMN template_name TEXT DEFAULT 'Academic Certificate'")
            } catch (ignored: Exception) {}

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_DEPARTMENTS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    code TEXT NOT NULL,
                    name TEXT NOT NULL,
                    hod_name TEXT NOT NULL,
                    description TEXT,
                    college_name TEXT NOT NULL
                )
            """.trimIndent())

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_CLASSES (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    class_code TEXT NOT NULL,
                    class_name TEXT NOT NULL,
                    department_code TEXT NOT NULL,
                    semester TEXT,
                    academic_year TEXT,
                    college_name TEXT NOT NULL
                )
            """.trimIndent())

            seedDepartmentsAndClasses(db, "NALANDA COLLEGE, BIHARSHARIF")
        }
    }

    private fun seedDefaultData(db: SQLiteDatabase) {
        val nalandaCollege = "NALANDA COLLEGE, BIHARSHARIF"

        // 1. Seed Nalanda College Admin User
        val userCv = ContentValues().apply {
            put("username", "admin")
            put("password", "admin123")
            put("full_name", "Pushpanjay Patel")
            put("role", "Administrator")
            put("college_name", nalandaCollege)
            put("university_name", "PATLIPUTRA UNIVERSITY, PATNA")
            put("college_tagline", "ESTABLISHED 1870 • CONSTITUENT UNIT OF PATLIPUTRA UNIVERSITY")
            put("hod_name", "Prof. Parmanand Prasad (BCA In-charge)")
            put("principal_name", "Prof. (Dr.) Ramakant Prasad (Principal)")
        }
        db.insert(TABLE_USERS, null, userCv)

        // 2. Seed Departments & Classes
        seedDepartmentsAndClasses(db, nalandaCollege)

        // 3. Seed 120 BCA-3rd Year students for Nalanda College
        val studentNames = arrayOf(
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
        )

        for (i in studentNames.indices) {
            val roll = i + 1
            val rollNo = roll.toString()
            val name = studentNames[i]
            val regNo = String.format("21NCBCA%03d", roll)
            val cleanName = name.lowercase().replace(Regex("[^a-z]"), "")
            val email = "$cleanName.$roll@nalandacollege.ac.in"
            val phone = String.format("+91 94310%05d", (10000 + roll * 73) % 90000)

            val studentCv = ContentValues().apply {
                put("roll_no", rollNo)
                put("name", name)
                put("reg_no", regNo)
                put("department_code", "BCA")
                put("class_code", "BCA-3")
                put("email", email)
                put("phone", phone)
                put("college_name", nalandaCollege)
            }
            db.insert(TABLE_STUDENTS, null, studentCv)
        }
    }

    private fun seedDepartmentsAndClasses(db: SQLiteDatabase, collegeName: String) {
        val depts = arrayOf(
            Triple("BCA", "Bachelor of Computer Applications", "Prof. Parmanand Prasad (BCA In-charge)"),
            Triple("MAC", "Master of Applied Computing", "Prof. Rajesh Sharma"),
            Triple("MBA", "Master of Business Administration", "Dr. Ananya Mishra"),
            Triple("BBA", "Bachelor of Business Administration", "Prof. Sanjay Verma"),
            Triple("BA", "Bachelor of Arts", "Prof. Ramashankar Prasad"),
            Triple("MA", "Master of Arts", "Dr. Sunita Kumari")
        )

        for ((code, name, hod) in depts) {
            val cv = ContentValues().apply {
                put("code", code)
                put("name", name)
                put("hod_name", hod)
                put("description", "Department of $name at $collegeName")
                put("college_name", collegeName)
            }
            db.insert(TABLE_DEPARTMENTS, null, cv)
        }

        val classes = arrayOf(
            Triple("BCA-1", "BCA 1st Year (Sem I & II)", "BCA"),
            Triple("BCA-2", "BCA 2nd Year (Sem III & IV)", "BCA"),
            Triple("BCA-3", "BCA 3rd Year (Sem V & VI)", "BCA"),
            Triple("MAC-1", "MAC 1st Year", "MAC"),
            Triple("MAC-2", "MAC 2nd Year", "MAC"),
            Triple("MBA-1", "MBA 1st Year", "MBA"),
            Triple("MBA-2", "MBA 2nd Year", "MBA"),
            Triple("BBA-1", "BBA 1st Year", "BBA"),
            Triple("BBA-2", "BBA 2nd Year", "BBA"),
            Triple("BBA-3", "BBA 3rd Year", "BBA"),
            Triple("BA-1", "BA 1st Year", "BA"),
            Triple("BA-2", "BA 2nd Year", "BA"),
            Triple("BA-3", "BA 3rd Year", "BA"),
            Triple("MA-1", "MA 1st Year", "MA"),
            Triple("MA-2", "MA 2nd Year", "MA")
        )

        for ((code, name, dept) in classes) {
            val cv = ContentValues().apply {
                put("class_code", code)
                put("class_name", name)
                put("department_code", dept)
                put("semester", "Semester Final")
                put("academic_year", "2023-2026")
                put("college_name", collegeName)
            }
            db.insert(TABLE_CLASSES, null, cv)
        }
    }

    // --- User operations ---
    fun authenticate(username: String, pass: String): UserAccount? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE username = ? AND password = ?",
            arrayOf(username, pass)
        )
        return cursor.use {
            if (it.moveToFirst()) {
                UserAccount(
                    id = it.getLong(it.getColumnIndexOrThrow("id")),
                    username = it.getString(it.getColumnIndexOrThrow("username")),
                    password = it.getString(it.getColumnIndexOrThrow("password")),
                    fullName = it.getString(it.getColumnIndexOrThrow("full_name")),
                    role = it.getString(it.getColumnIndexOrThrow("role")),
                    collegeName = it.getString(it.getColumnIndexOrThrow("college_name")),
                    universityName = it.getString(it.getColumnIndexOrThrow("university_name")),
                    collegeTagline = it.getString(it.getColumnIndexOrThrow("college_tagline")),
                    hodName = it.getString(it.getColumnIndexOrThrow("hod_name")),
                    principalName = it.getString(it.getColumnIndexOrThrow("principal_name"))
                )
            } else null
        }
    }

    fun getAllUsers(): List<UserAccount> {
        val list = mutableListOf<UserAccount>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS ORDER BY id ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    UserAccount(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        username = it.getString(it.getColumnIndexOrThrow("username")),
                        password = it.getString(it.getColumnIndexOrThrow("password")),
                        fullName = it.getString(it.getColumnIndexOrThrow("full_name")),
                        role = it.getString(it.getColumnIndexOrThrow("role")),
                        collegeName = it.getString(it.getColumnIndexOrThrow("college_name")),
                        universityName = it.getString(it.getColumnIndexOrThrow("university_name")),
                        collegeTagline = it.getString(it.getColumnIndexOrThrow("college_tagline")),
                        hodName = it.getString(it.getColumnIndexOrThrow("hod_name")),
                        principalName = it.getString(it.getColumnIndexOrThrow("principal_name"))
                    )
                )
            }
        }
        return list
    }

    fun registerUser(user: UserAccount): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("username", user.username)
            put("password", user.password)
            put("full_name", user.fullName)
            put("role", user.role)
            put("college_name", user.collegeName)
            put("university_name", user.universityName)
            put("college_tagline", user.collegeTagline)
            put("hod_name", user.hodName)
            put("principal_name", user.principalName)
        }
        val id = db.insert(TABLE_USERS, null, cv)
        if (id != -1L) {
            seedDepartmentsAndClasses(db, user.collegeName)
            return true
        }
        return false
    }

    // --- Department & Class operations ---
    fun getDepartments(collegeName: String): List<Department> {
        val list = mutableListOf<Department>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_DEPARTMENTS WHERE college_name = ? ORDER BY id ASC",
            arrayOf(collegeName)
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Department(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        code = it.getString(it.getColumnIndexOrThrow("code")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        hodName = it.getString(it.getColumnIndexOrThrow("hod_name")),
                        description = it.getString(it.getColumnIndexOrThrow("description")),
                        collegeName = it.getString(it.getColumnIndexOrThrow("college_name"))
                    )
                )
            }
        }
        return list
    }

    fun getClasses(collegeName: String): List<SchoolClass> {
        val list = mutableListOf<SchoolClass>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CLASSES WHERE college_name = ? ORDER BY id ASC",
            arrayOf(collegeName)
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    SchoolClass(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        classCode = it.getString(it.getColumnIndexOrThrow("class_code")),
                        className = it.getString(it.getColumnIndexOrThrow("class_name")),
                        departmentCode = it.getString(it.getColumnIndexOrThrow("department_code")),
                        semester = it.getString(it.getColumnIndexOrThrow("semester")),
                        academicYear = it.getString(it.getColumnIndexOrThrow("academic_year")),
                        collegeName = it.getString(it.getColumnIndexOrThrow("college_name"))
                    )
                )
            }
        }
        return list
    }

    // --- Student operations ---
    fun getStudents(collegeName: String): List<Student> {
        val list = mutableListOf<Student>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_STUDENTS WHERE college_name = ? ORDER BY id ASC",
            arrayOf(collegeName)
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Student(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        rollNo = it.getString(it.getColumnIndexOrThrow("roll_no")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        regNo = it.getString(it.getColumnIndexOrThrow("reg_no")),
                        departmentCode = it.getString(it.getColumnIndexOrThrow("department_code")),
                        classCode = it.getString(it.getColumnIndexOrThrow("class_code")),
                        email = it.getString(it.getColumnIndexOrThrow("email")),
                        phone = it.getString(it.getColumnIndexOrThrow("phone")),
                        collegeName = it.getString(it.getColumnIndexOrThrow("college_name"))
                    )
                )
            }
        }
        return list
    }

    fun getStudentsByClass(collegeName: String, classCode: String): List<Student> {
        val list = mutableListOf<Student>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_STUDENTS WHERE college_name = ? AND class_code = ? ORDER BY id ASC",
            arrayOf(collegeName, classCode)
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Student(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        rollNo = it.getString(it.getColumnIndexOrThrow("roll_no")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        regNo = it.getString(it.getColumnIndexOrThrow("reg_no")),
                        departmentCode = it.getString(it.getColumnIndexOrThrow("department_code")),
                        classCode = it.getString(it.getColumnIndexOrThrow("class_code")),
                        email = it.getString(it.getColumnIndexOrThrow("email")),
                        phone = it.getString(it.getColumnIndexOrThrow("phone")),
                        collegeName = it.getString(it.getColumnIndexOrThrow("college_name"))
                    )
                )
            }
        }
        return list
    }

    fun addStudent(student: Student): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("roll_no", student.rollNo)
            put("name", student.name)
            put("reg_no", student.regNo)
            put("department_code", student.departmentCode)
            put("class_code", student.classCode)
            put("email", student.email)
            put("phone", student.phone)
            put("college_name", student.collegeName)
        }
        val id = db.insert(TABLE_STUDENTS, null, cv)
        return id != -1L
    }

    fun deleteStudent(id: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_STUDENTS, "id = ?", arrayOf(id.toString())) > 0
    }

    fun isCertificateIssuedForStudent(studentId: Long): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CERTIFICATES WHERE student_id = ?", arrayOf(studentId.toString()))
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) > 0 else false
        }
    }

    // --- Certificate operations ---
    fun getCertificates(collegeName: String): List<CertificateRecord> {
        val list = mutableListOf<CertificateRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CERTIFICATES WHERE college_name = ? ORDER BY id DESC",
            arrayOf(collegeName)
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    CertificateRecord(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        certificateNo = it.getString(it.getColumnIndexOrThrow("certificate_no")),
                        studentId = it.getLong(it.getColumnIndexOrThrow("student_id")),
                        studentName = it.getString(it.getColumnIndexOrThrow("student_name")),
                        rollNo = it.getString(it.getColumnIndexOrThrow("roll_no")),
                        regNo = it.getString(it.getColumnIndexOrThrow("reg_no")),
                        department = it.getString(it.getColumnIndexOrThrow("department")),
                        className = it.getString(it.getColumnIndexOrThrow("class_name")),
                        universityName = it.getString(it.getColumnIndexOrThrow("university_name")),
                        collegeName = it.getString(it.getColumnIndexOrThrow("college_name")),
                        certificateTitle = it.getString(it.getColumnIndexOrThrow("certificate_title")),
                        remarks = it.getString(it.getColumnIndexOrThrow("remarks")),
                        issueDate = it.getString(it.getColumnIndexOrThrow("issue_date")),
                        signatory1 = it.getString(it.getColumnIndexOrThrow("signatory1")),
                        signatory2 = it.getString(it.getColumnIndexOrThrow("signatory2")),
                        templateName = try { it.getString(it.getColumnIndexOrThrow("template_name")) } catch (e: Exception) { "Academic Certificate" },
                        pdfPath = it.getString(it.getColumnIndexOrThrow("pdf_path"))
                    )
                )
            }
        }
        return list
    }

    fun insertCertificate(cert: CertificateRecord): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("certificate_no", cert.certificateNo)
            put("student_id", cert.studentId)
            put("student_name", cert.studentName)
            put("roll_no", cert.rollNo)
            put("reg_no", cert.regNo)
            put("department", cert.department)
            put("class_name", cert.className)
            put("university_name", cert.universityName)
            put("college_name", cert.collegeName)
            put("certificate_title", cert.certificateTitle)
            put("remarks", cert.remarks)
            put("issue_date", cert.issueDate)
            put("signatory1", cert.signatory1)
            put("signatory2", cert.signatory2)
            put("template_name", cert.templateName)
            put("pdf_path", cert.pdfPath)
        }
        return db.insert(TABLE_CERTIFICATES, null, cv)
    }

    fun deleteCertificate(id: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_CERTIFICATES, "id = ?", arrayOf(id.toString())) > 0
    }

    fun verifyCertificate(certNo: String): CertificateRecord? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CERTIFICATES WHERE certificate_no = ?",
            arrayOf(certNo)
        )
        return cursor.use {
            if (it.moveToFirst()) {
                CertificateRecord(
                    id = it.getLong(it.getColumnIndexOrThrow("id")),
                    certificateNo = it.getString(it.getColumnIndexOrThrow("certificate_no")),
                    studentId = it.getLong(it.getColumnIndexOrThrow("student_id")),
                    studentName = it.getString(it.getColumnIndexOrThrow("student_name")),
                    rollNo = it.getString(it.getColumnIndexOrThrow("roll_no")),
                    regNo = it.getString(it.getColumnIndexOrThrow("reg_no")),
                    department = it.getString(it.getColumnIndexOrThrow("department")),
                    className = it.getString(it.getColumnIndexOrThrow("class_name")),
                    universityName = it.getString(it.getColumnIndexOrThrow("university_name")),
                    collegeName = it.getString(it.getColumnIndexOrThrow("college_name")),
                    certificateTitle = it.getString(it.getColumnIndexOrThrow("certificate_title")),
                    remarks = it.getString(it.getColumnIndexOrThrow("remarks")),
                    issueDate = it.getString(it.getColumnIndexOrThrow("issue_date")),
                    signatory1 = it.getString(it.getColumnIndexOrThrow("signatory1")),
                    signatory2 = it.getString(it.getColumnIndexOrThrow("signatory2")),
                    templateName = try { it.getString(it.getColumnIndexOrThrow("template_name")) } catch (e: Exception) { "Academic Certificate" },
                    pdfPath = it.getString(it.getColumnIndexOrThrow("pdf_path"))
                )
            } else null
        }
    }
}
