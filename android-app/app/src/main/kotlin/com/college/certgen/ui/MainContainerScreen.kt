package com.college.certgen.ui

import androidx.compose.foundation.Image
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
import com.college.certgen.ui.academic.DepartmentsAndClassesScreen
import com.college.certgen.ui.dashboard.DashboardScreen
import com.college.certgen.ui.guide.SoftwareGuideDialog
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
    var showGuideDialog by remember { mutableStateOf(false) }

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
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "College Certificate Management System",
                                fontSize = 10.5.sp,
                                color = Color(0xFF38BDF8),
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showGuideDialog = true }) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = "User Guide",
                            tint = Color(0xFF38BDF8)
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Default.ExitToApp,
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
                    label = { Text("Home", fontSize = 10.sp) },
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
                    label = { Text("Students", fontSize = 10.sp) },
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
                    label = { Text("Studio", fontSize = 10.sp) },
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
                    label = { Text("Issued", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF0F172A),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = {
                        refreshCounts()
                        selectedTab = 4
                    },
                    icon = { Icon(Icons.Default.School, contentDescription = "Classes") },
                    label = { Text("Programs", fontSize = 10.sp) },
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
                    },
                    onNavigateToDepts = {
                        refreshCounts()
                        selectedTab = 4
                    },
                    onShowGuide = {
                        showGuideDialog = true
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
                4 -> DepartmentsAndClassesScreen(
                    currentUser = currentUser,
                    dbHelper = dbHelper
                )
            }
        }
    }

    if (showGuideDialog) {
        SoftwareGuideDialog(onDismiss = { showGuideDialog = false })
    }
}
