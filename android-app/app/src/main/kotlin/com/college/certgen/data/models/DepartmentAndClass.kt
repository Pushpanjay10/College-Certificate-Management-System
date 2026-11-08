package com.college.certgen.data.models

data class Department(
    val id: Long = 0,
    val code: String,
    val name: String,
    val hodName: String,
    val description: String,
    val collegeName: String
)

data class SchoolClass(
    val id: Long = 0,
    val classCode: String,
    val className: String,
    val departmentCode: String,
    val semester: String,
    val academicYear: String,
    val collegeName: String
)
