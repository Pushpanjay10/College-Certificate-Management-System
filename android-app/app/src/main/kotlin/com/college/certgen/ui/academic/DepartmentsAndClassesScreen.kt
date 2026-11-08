package com.college.certgen.ui.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.college.certgen.data.models.UserAccount

@Composable
fun DepartmentsAndClassesScreen(
    currentUser: UserAccount,
    dbHelper: AppDatabaseHelper
) {
    var selectedTab by remember { mutableStateOf(0) }
    val departments = remember { dbHelper.getDepartments(currentUser.collegeName) }
    val classes = remember { dbHelper.getClasses(currentUser.collegeName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        Text(
            text = "Academic Structure & Programs",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Manage university departments, courses, and active batches",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: Departments vs Classes
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1E293B),
            contentColor = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Departments (${departments.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Classes & Semesters (${classes.size})", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // Departments List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(departments) { dept ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = dept.code,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = dept.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "HOD: ${dept.hodName}",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.5.sp
                            )
                            if (dept.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dept.description,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        } else {
            // Classes List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(classes) { cls ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = cls.classCode,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cls.className,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Dept: ${cls.departmentCode}  •  Batch: ${cls.academicYear}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }
    }
}

