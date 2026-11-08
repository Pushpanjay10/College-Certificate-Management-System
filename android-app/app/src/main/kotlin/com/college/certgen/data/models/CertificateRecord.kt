package com.college.certgen.data.models

data class CertificateRecord(
    val id: Long = 0,
    val certificateNo: String,
    val studentId: Long,
    val studentName: String,
    val rollNo: String,
    val regNo: String,
    val department: String,
    val className: String,
    val universityName: String,
    val collegeName: String,
    val certificateTitle: String,
    val remarks: String,
    val issueDate: String,
    val signatory1: String,
    val signatory2: String,
    val templateName: String = "Academic Certificate",
    val pdfPath: String = ""
)
