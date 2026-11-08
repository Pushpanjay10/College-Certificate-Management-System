package com.college.certgen.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.certgen.R
import com.college.certgen.data.models.UserAccount

@Composable
fun DashboardScreen(
    currentUser: UserAccount,
    totalStudents: Int,
    totalCertificates: Int,
    onNavigateToStudents: () -> Unit,
    onNavigateToStudio: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToDepts: () -> Unit,
    onShowGuide: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // College Brand Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Emblem",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = currentUser.collegeName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = currentUser.universityName,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("User: ${currentUser.fullName}", color = Color(0xFFCBD5E1), fontSize = 11.5.sp)
                    Text("Role: ${currentUser.role}", color = Color(0xFF38BDF8), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4 Stats Cards in 2x2 Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                title = "Total Students",
                value = totalStudents.toString(),
                icon = Icons.Default.Groups,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Issued Certs",
                value = totalCertificates.toString(),
                icon = Icons.Default.WorkspacePremium,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                title = "Departments",
                value = "6 Depts",
                icon = Icons.Default.AccountBalance,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Classes & Semesters",
                value = "15 Batches",
                icon = Icons.Default.Class,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Quick Navigation",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action 1: Issue Certificate
        ActionCard(
            title = "Certificate Studio & Generator",
            subtitle = "Academic, Cultural, Sports & Tech templates with live preview",
            icon = Icons.Default.WorkspacePremium,
            tint = Color(0xFF38BDF8),
            onClick = onNavigateToStudio
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action 2: Students Directory
        ActionCard(
            title = "Students Directory",
            subtitle = "Manage enrolled students, search by Roll No, or add new records",
            icon = Icons.Default.People,
            tint = Color(0xFFA855F7),
            onClick = onNavigateToStudents
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action 3: Issued History
        ActionCard(
            title = "Issued Certificates Archive",
            subtitle = "Track generated certificates, instant verification & re-download",
            icon = Icons.Default.HistoryEdu,
            tint = Color(0xFF10B981),
            onClick = onNavigateToRecords
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action 4: Academic Programs
        ActionCard(
            title = "Departments & Classes",
            subtitle = "View BCA, MAC, MBA, BBA, BA, MA departments and active batches",
            icon = Icons.Default.School,
            tint = Color(0xFFF59E0B),
            onClick = onNavigateToDepts
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action 5: Software Guide
        ActionCard(
            title = "Software User Guide",
            subtitle = "Read feature overview, QR verification details & quick start instructions",
            icon = Icons.Default.MenuBook,
            tint = Color(0xFFEC4899),
            onClick = onShowGuide
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(title, color = Color(0xFF94A3B8), fontSize = 11.5.sp)
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(tint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = Color(0xFF94A3B8), fontSize = 11.5.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
        }
    }
}
