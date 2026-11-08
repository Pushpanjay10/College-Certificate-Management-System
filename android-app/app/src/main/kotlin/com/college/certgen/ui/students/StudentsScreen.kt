package com.college.certgen.ui.students

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.certgen.data.db.AppDatabaseHelper
import com.college.certgen.data.models.Student
import com.college.certgen.data.models.UserAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    currentUser: UserAccount,
    dbHelper: AppDatabaseHelper,
    onIssueForStudent: (Student) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var studentsList by remember { mutableStateOf(dbHelper.getStudents(currentUser.collegeName)) }
    var selectedClassFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    val classFilters = listOf("All", "BCA-3", "BCA-2", "BCA-1", "MAC-1", "MAC-2", "MBA-1", "BBA-1", "BA-1", "MA-1")

    val filteredList = remember(searchQuery, selectedClassFilter, studentsList) {
        var list = studentsList
        if (selectedClassFilter != "All") {
            list = list.filter { it.classCode.equals(selectedClassFilter, ignoreCase = true) }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.rollNo.lowercase().contains(q) ||
                it.regNo.lowercase().contains(q) ||
                it.classCode.lowercase().contains(q)
            }
        }
        list
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Student")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, roll no, or reg no...", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Class Filter Horizontal Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                classFilters.forEach { c ->
                    val isSelected = c == selectedClassFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedClassFilter = c },
                        label = { Text(c, fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2563EB),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Header info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enrolled Students (${filteredList.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = currentUser.collegeName,
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonOff, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No students found in this filter", color = Color(0xFF94A3B8), fontSize = 15.sp)
                        Text("Tap + button below to add a student", color = Color(0xFF64748B), fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { student ->
                        val isIssued = remember(student.id) {
                            dbHelper.isCertificateIssuedForStudent(student.id)
                        }
                        StudentItemCard(
                            student = student,
                            isIssued = isIssued,
                            onIssueClick = { onIssueForStudent(student) },
                            onDeleteClick = { studentToDelete = student }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add Student Dialog
    if (showAddDialog) {
        var newRoll by remember { mutableStateOf("") }
        var newName by remember { mutableStateOf("") }
        var newReg by remember { mutableStateOf("") }
        var newClass by remember { mutableStateOf("BCA-3") }
        var newDept by remember { mutableStateOf("BCA") }
        var newEmail by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var addError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("Add New Student", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    addError?.let {
                        Text(it, color = Color(0xFFEF4444), fontSize = 12.sp)
                    }
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name *") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newRoll,
                            onValueChange = { newRoll = it },
                            label = { Text("Roll No *") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newReg,
                            onValueChange = { newReg = it },
                            label = { Text("Reg No *") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newClass,
                            onValueChange = { newClass = it },
                            label = { Text("Class (e.g. BCA-3)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newDept,
                            onValueChange = { newDept = it },
                            label = { Text("Department") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email (Optional)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone (Optional)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isBlank() || newRoll.isBlank() || newReg.isBlank()) {
                            addError = "Please fill student name, roll no, and reg no."
                            return@Button
                        }
                        val student = Student(
                            rollNo = newRoll.trim(),
                            name = newName.trim().uppercase(),
                            regNo = newReg.trim().uppercase(),
                            departmentCode = newDept.trim(),
                            classCode = newClass.trim(),
                            email = newEmail.trim(),
                            phone = newPhone.trim(),
                            collegeName = currentUser.collegeName
                        )
                        dbHelper.addStudent(student)
                        studentsList = dbHelper.getStudents(currentUser.collegeName)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Add Student")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    studentToDelete?.let { st ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            containerColor = Color(0xFF1E293B),
            title = { Text("Delete Student?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to permanently delete '${st.name}' (Roll No: ${st.rollNo})?",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        dbHelper.deleteStudent(st.id)
                        studentsList = dbHelper.getStudents(currentUser.collegeName)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun StudentItemCard(
    student: Student,
    isIssued: Boolean,
    onIssueClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Roll No Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${student.rollNo}",
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Reg: ${student.regNo}  •  ${student.classCode}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Status Badge
                Surface(
                    color = if (isIssued) Color(0xFF064E3B) else Color(0xFF78350F),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isIssued) "✓ Certificate Issued" else "● Pending Issuance",
                        color = if (isIssued) Color(0xFF34D399) else Color(0xFFFBBF24),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Quick Issue Button
            IconButton(
                onClick = onIssueClick,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF38BDF8))
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = "Issue Certificate")
            }

            // Delete Button
            IconButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
