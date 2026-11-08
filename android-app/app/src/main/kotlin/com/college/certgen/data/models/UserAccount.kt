package com.college.certgen.data.models

data class UserAccount(
    val id: Long = 0,
    val username: String,
    val password: String,
    val fullName: String,
    val role: String = "Administrator",
    val collegeName: String = "NALANDA COLLEGE, BIHARSHARIF",
    val universityName: String = "PATLIPUTRA UNIVERSITY, PATNA",
    val collegeTagline: String = "ESTABLISHED 1870 • CONSTITUENT UNIT OF PATLIPUTRA UNIVERSITY",
    val hodName: String = "Prof. Parmanand Prasad (BCA In-charge)",
    val principalName: String = "Prof. (Dr.) Ramakant Prasad (Principal)"
)
