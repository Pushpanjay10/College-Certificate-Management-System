package com.college.certgen.data.models

data class Student(
    val id: Long = 0,
    val rollNo: String,
    val name: String,
    val regNo: String,
    val departmentCode: String = "BCA",
    val classCode: String = "BCA-3",
    val email: String = "",
    val phone: String = "",
    val collegeName: String = "NALANDA COLLEGE, BIHARSHARIF"
)
