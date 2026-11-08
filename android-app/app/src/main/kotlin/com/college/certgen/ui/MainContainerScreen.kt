package com.college.certgen.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.certgen.R
import com.college.certgen.data.db.AppDatabaseHelper
import com.college.certgen.data.models.Student
import com.college.certgen.data.models.UserAccount
import com.college.certgen.ui.dashboard.DashboardScreen
import com.college.certgen.ui.records.IssuedRecordsScreen
import com.college.certgen.ui.students.StudentsScreen
import com.college.certgen.ui.studio.CertificateStudioScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(
    currentUser: UserAccount,
    dbHelper: AppDatabaseHelper,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var studioStudent by remember { mutableStateOf<Student?>(null) }

    // Counts to keep reactive
    var studentCount by remember { mutableStateOf(dbHelper.getStudents(currentUser.collegeName).size) }
    var certCount by remember { mutableStateOf(dbHelper.getCertificates(currentUser.collegeName).size) }

    fun refreshCounts() {
        studentCount = dbHelper.getStudents(currentUser.collegeName).size
        certCount = dbHelper.getCertificates(currentUser.collegeName).size
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentUser.collegeName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "College Certificate System",
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFEF4444)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E293B)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        refreshCounts()
                        selectedTab = 0
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF0F172A),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        refreshCounts()
                        selectedTab = 1
                    },
                    icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                    label = { Text("Students", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF0F172A),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        refreshCounts()
                        selectedTab = 2
                    },
                    icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = "Studio") },
                    label = { Text("Studio", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF0F172A),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        refreshCounts()
                        selectedTab = 3
                    },
                    icon = { Icon(Icons.Default.HistoryEdu, contentDescription = "Records") },
                    label = { Text("Issued", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF0F172A),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    currentUser = currentUser,
                    totalStudents = studentCount,
                    totalCertificates = certCount,
                    onNavigateToStudents = {
                        refreshCounts()
                        selectedTab = 1
                    },
                    onNavigateToStudio = {
                        refreshCounts()
                        selectedTab = 2
                    },
                    onNavigateToRecords = {
                        refreshCounts()
                        selectedTab = 3
                    }
                )
                1 -> StudentsScreen(
                    currentUser = currentUser,
                    dbHelper = dbHelper,
                    onIssueForStudent = { st ->
                        studioStudent = st
                        selectedTab = 2
                    }
                )
                2 -> CertificateStudioScreen(
                    currentUser = currentUser,
                    dbHelper = dbHelper,
                    initialStudent = studioStudent,
                    onCertificateIssued = {
                        refreshCounts()
                    }
                )
                3 -> IssuedRecordsScreen(
                    currentUser = currentUser,
                    dbHelper = dbHelper
                )
            }
        }
    }
}
