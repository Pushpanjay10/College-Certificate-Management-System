package com.college.certgen.ui.records

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.certgen.data.db.AppDatabaseHelper
import com.college.certgen.data.models.CertificateRecord
import com.college.certgen.data.models.UserAccount
import com.college.certgen.pdf.CertificatePdfGenerator
import java.io.File

@Composable
fun IssuedRecordsScreen(
    currentUser: UserAccount,
    dbHelper: AppDatabaseHelper
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var recordsList by remember { mutableStateOf(dbHelper.getCertificates(currentUser.collegeName)) }

    // Verification state
    var verifyCertNo by remember { mutableStateOf("") }
    var verifiedRecord by remember { mutableStateOf<CertificateRecord?>(null) }
    var verifyChecked by remember { mutableStateOf(false) }

    var recordToDelete by remember { mutableStateOf<CertificateRecord?>(null) }

    val filteredList = remember(searchQuery, recordsList) {
        if (searchQuery.isBlank()) recordsList
        else {
            val q = searchQuery.trim().lowercase()
            recordsList.filter {
                it.studentName.lowercase().contains(q) ||
                it.certificateNo.lowercase().contains(q) ||
                it.rollNo.lowercase().contains(q) ||
                it.regNo.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        Text(
            text = "Issued Certificates Archive",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = "Track, search, re-download, share, and verify issued certificates",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- VERIFY CERTIFICATE SECTION ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Instant Certificate Verification", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = verifyCertNo,
                        onValueChange = {
                            verifyCertNo = it
                            verifyChecked = false
                        },
                        placeholder = { Text("Enter Cert No (e.g. CERT-108-4547)", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (verifyCertNo.isNotBlank()) {
                                verifiedRecord = dbHelper.verifyCertificate(verifyCertNo.trim())
                                verifyChecked = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Verify")
                    }
                }

                if (verifyChecked) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val r = verifiedRecord
                    if (r != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("VERIFIED & AUTHENTIC CERTIFICATE", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Student: ${r.studentName} (Roll: ${r.rollNo}, Reg: ${r.regNo})", color = Color.White, fontSize = 12.sp)
                                Text("Title: ${r.certificateTitle}", color = Color(0xFFA7F3D0), fontSize = 11.sp)
                                Text("Issued On: ${r.issueDate} by ${r.collegeName}", color = Color(0xFFD1FAE5), fontSize = 11.sp)
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Certificate Not Found or Unverified.", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search records by student, cert no...", color = Color(0xFF94A3B8)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8)) },
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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Issued Certificates (${filteredList.size})",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No certificates issued yet", color = Color(0xFF94A3B8), fontSize = 15.sp)
                    Text("Issue your first certificate in Certificate Studio!", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { cert ->
                    CertificateRecordCard(
                        cert = cert,
                        onViewPdf = {
                            val f = if (cert.pdfPath.isNotBlank() && File(cert.pdfPath).exists()) {
                                File(cert.pdfPath)
                            } else {
                                CertificatePdfGenerator.generateCertificatePdf(context, cert)
                            }
                            CertificatePdfGenerator.openPdf(context, f)
                        },
                        onSharePdf = {
                            val f = if (cert.pdfPath.isNotBlank() && File(cert.pdfPath).exists()) {
                                File(cert.pdfPath)
                            } else {
                                CertificatePdfGenerator.generateCertificatePdf(context, cert)
                            }
                            CertificatePdfGenerator.sharePdf(context, f, cert.studentName)
                        },
                        onDelete = {
                            recordToDelete = cert
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    recordToDelete?.let { cert ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            containerColor = Color(0xFF1E293B),
            title = { Text("Delete Issued Record?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete certificate ${cert.certificateNo} for ${cert.studentName}?",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        dbHelper.deleteCertificate(cert.id)
                        recordsList = dbHelper.getCertificates(currentUser.collegeName)
                        recordToDelete = null
                        Toast.makeText(context, "Certificate deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun CertificateRecordCard(
    cert: CertificateRecord,
    onViewPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cert No Badge
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = cert.certificateNo,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = cert.issueDate,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = cert.studentName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Roll: ${cert.rollNo}  •  Reg: ${cert.regNo}  •  ${cert.className}",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
            Text(
                text = cert.certificateTitle,
                color = Color(0xFFD4AF37),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View PDF
                FilledTonalButton(
                    onClick = onViewPdf,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Share
                FilledTonalButton(
                    onClick = onSharePdf,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete")
                }
            }
        }
    }
}

