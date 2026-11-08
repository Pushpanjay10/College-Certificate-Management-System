package com.college.certgen.ui.studio

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.certgen.data.db.AppDatabaseHelper
import com.college.certgen.data.models.CertificateRecord
import com.college.certgen.data.models.Student
import com.college.certgen.data.models.UserAccount
import com.college.certgen.pdf.CertificatePdfGenerator
import com.college.certgen.pdf.QrCodeUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CertificateStudioScreen(
    currentUser: UserAccount,
    dbHelper: AppDatabaseHelper,
    initialStudent: Student? = null,
    onCertificateIssued: () -> Unit
) {
    val context = LocalContext.current
    val allStudents = remember { dbHelper.getStudents(currentUser.collegeName) }

    var selectedStudent by remember {
        mutableStateOf(initialStudent ?: allStudents.firstOrNull())
    }
    var showStudentPicker by remember { mutableStateOf(false) }

    val categories = listOf(
        "CERTIFICATE OF ACADEMIC EXCELLENCE",
        "CERTIFICATE OF MERIT",
        "COURSE COMPLETION CERTIFICATE",
        "SPORTS & ATHLETICS EXCELLENCE",
        "CERTIFICATE OF APPRECIATION"
    )
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        sdf.format(Date())
    }
    var issueDate by remember { mutableStateOf(todayFormatted) }

    var signatory1 by remember { mutableStateOf(currentUser.hodName) }
    var signatory2 by remember { mutableStateOf(currentUser.principalName) }

    var remarks by remember {
        mutableStateOf(
            "for exemplary scholastic performance and successful completion of curriculum requirements in ${selectedStudent?.classCode ?: "BCA-3"}, Department of ${selectedStudent?.departmentCode ?: "BCA"}. In recognition of outstanding academic performance, exemplary dedication, and the highest standards of scholastic achievement."
        )
    }

    // Update remarks template when student changes
    LaunchedEffect(selectedStudent) {
        selectedStudent?.let { st ->
            remarks = "for exemplary scholastic performance and successful completion of curriculum requirements in ${st.classCode}, Department of ${st.departmentCode}. In recognition of outstanding academic performance, exemplary dedication, and the highest standards of scholastic achievement."
        }
    }

    // Generated PDF state
    var generatedFile by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Certificate Studio",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Customize, preview in real-time, and issue official certificates",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Student Selector Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showStudentPicker = true }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectedStudent != null) selectedStudent!!.name else "Select Student",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (selectedStudent != null)
                            "Roll: ${selectedStudent!!.rollNo}  •  Reg: ${selectedStudent!!.regNo}"
                        else "Tap to choose student from list",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Selector
        Text("Certificate Category", color = Color(0xFFCBD5E1), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(2).forEach { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(if (cat.contains("MERIT")) "Merit Award" else "Academic Excellence", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Citation / Remarks Field
        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Citation / Remarks") },
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Signatories (HOD & Principal)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = signatory1,
                onValueChange = { signatory1 = it },
                label = { Text("HOD In-charge") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = signatory2,
                onValueChange = { signatory2 = it },
                label = { Text("Principal") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- LIVE CERTIFICATE PREVIEW CARD ---
        Text(
            text = "Live Certificate Preview",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LiveCertificatePreview(
            collegeName = currentUser.collegeName,
            universityName = currentUser.universityName,
            categoryTitle = selectedCategory,
            studentName = selectedStudent?.name ?: "STUDENT NAME",
            rollNo = selectedStudent?.rollNo ?: "000",
            regNo = selectedStudent?.regNo ?: "21NCBCA000",
            citation = remarks,
            signatory1 = signatory1,
            signatory2 = signatory2,
            issueDate = issueDate
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- ACTION BUTTONS ---
        val st = selectedStudent
        if (st == null) {
            Button(
                onClick = { showStudentPicker = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Select Student to Generate Certificate")
            }
        } else {
            // Generate & Save Button
            Button(
                onClick = {
                    isGenerating = true
                    try {
                        val certNo = "CERT-${st.rollNo}-${(1000..9999).random()}"
                        val certRecord = CertificateRecord(
                            certificateNo = certNo,
                            studentId = st.id,
                            studentName = st.name,
                            rollNo = st.rollNo,
                            regNo = st.regNo,
                            department = st.departmentCode,
                            className = st.classCode,
                            universityName = currentUser.universityName,
                            collegeName = currentUser.collegeName,
                            certificateTitle = selectedCategory,
                            remarks = remarks,
                            issueDate = issueDate,
                            signatory1 = signatory1,
                            signatory2 = signatory2
                        )
                        val pdfFile = CertificatePdfGenerator.generateCertificatePdf(context, certRecord)
                        val savedRecord = certRecord.copy(pdfPath = pdfFile.absolutePath)
                        dbHelper.insertCertificate(savedRecord)
                        generatedFile = pdfFile
                        onCertificateIssued()
                        Toast.makeText(context, "Certificate Generated & Saved: $certNo", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isGenerating = false
                    }
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.SaveAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isGenerating) "Generating Certificate..." else "Generate & Save Certificate",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PDF Action row (Open & Share)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val file = generatedFile ?: run {
                            val certNo = "CERT-${st.rollNo}-${(1000..9999).random()}"
                            val certRecord = CertificateRecord(
                                certificateNo = certNo,
                                studentId = st.id,
                                studentName = st.name,
                                rollNo = st.rollNo,
                                regNo = st.regNo,
                                department = st.departmentCode,
                                className = st.classCode,
                                universityName = currentUser.universityName,
                                collegeName = currentUser.collegeName,
                                certificateTitle = selectedCategory,
                                remarks = remarks,
                                issueDate = issueDate,
                                signatory1 = signatory1,
                                signatory2 = signatory2
                            )
                            val f = CertificatePdfGenerator.generateCertificatePdf(context, certRecord)
                            generatedFile = f
                            f
                        }
                        CertificatePdfGenerator.openPdf(context, file)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View PDF", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        val file = generatedFile ?: run {
                            val certNo = "CERT-${st.rollNo}-${(1000..9999).random()}"
                            val certRecord = CertificateRecord(
                                certificateNo = certNo,
                                studentId = st.id,
                                studentName = st.name,
                                rollNo = st.rollNo,
                                regNo = st.regNo,
                                department = st.departmentCode,
                                className = st.classCode,
                                universityName = currentUser.universityName,
                                collegeName = currentUser.collegeName,
                                certificateTitle = selectedCategory,
                                remarks = remarks,
                                issueDate = issueDate,
                                signatory1 = signatory1,
                                signatory2 = signatory2
                            )
                            val f = CertificatePdfGenerator.generateCertificatePdf(context, certRecord)
                            generatedFile = f
                            f
                        }
                        CertificatePdfGenerator.sharePdf(context, file, st.name)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Searchable Student Picker Dialog
    if (showStudentPicker) {
        var pickerQuery by remember { mutableStateOf("") }
        val filteredPickerStudents = remember(pickerQuery, allStudents) {
            if (pickerQuery.isBlank()) allStudents
            else {
                val q = pickerQuery.trim().lowercase()
                allStudents.filter {
                    it.name.lowercase().contains(q) || it.rollNo.contains(q) || it.regNo.lowercase().contains(q)
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showStudentPicker = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("Select Student", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                    OutlinedTextField(
                        value = pickerQuery,
                        onValueChange = { pickerQuery = it },
                        placeholder = { Text("Search name or roll no...", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        filteredPickerStudents.forEach { st ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedStudent = st
                                        generatedFile = null
                                        showStudentPicker = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#${st.rollNo}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(st.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Reg: ${st.regNo} (${st.classCode})", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                            }
                            HorizontalDivider(color = Color(0xFF334155))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStudentPicker = false }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun LiveCertificatePreview(
    collegeName: String,
    universityName: String,
    categoryTitle: String,
    studentName: String,
    rollNo: String,
    regNo: String,
    citation: String,
    signatory1: String,
    signatory2: String,
    issueDate: String
) {
    val sampleQr = remember(studentName, rollNo) {
        QrCodeUtils.generateQrBitmap("VERIFY:$studentName|$rollNo|$regNo", 90)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF8)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, Color(0xFFD4AF37), RoundedCornerShape(10.dp))
            .padding(4.dp)
            .border(1.5.dp, Color(0xFF0F2942), RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = universityName.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A5F),
                letterSpacing = 1.sp
            )
            Text(
                text = collegeName.uppercase(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF0A2540)
            )
            Text(
                text = "ESTABLISHED 1870 • CONSTITUENT UNIT OF PATLIPUTRA UNIVERSITY",
                fontSize = 8.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = Color(0xFFD4AF37), thickness = 1.dp, modifier = Modifier.width(180.dp))
            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = categoryTitle.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                color = Color(0xFFB8860B)
            )

            Text(
                text = "THIS CERTIFICATE IS PROUDLY PRESENTED TO",
                fontSize = 8.sp,
                color = Color(0xFF475569),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Student Name
            Text(
                text = studentName.uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF0A2540)
            )

            Text(
                text = "College Roll No: $rollNo   •   University Reg No: $regNo",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Citation
            Text(
                text = citation,
                fontSize = 8.5.sp,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Section: QR, Seal, Signatures
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // QR Code
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = sampleQr.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(45.dp)
                    )
                    Text("Scan to Verify", fontSize = 7.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }

                // Official Seal
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "OFFICIAL\nSEAL",
                        fontSize = 6.sp,
                        color = Color(0xFFB8860B),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 8.sp
                    )
                }

                // Signatories
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = signatory1.take(16),
                            fontSize = 8.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFF0F2942)
                        )
                        HorizontalDivider(color = Color(0xFF94A3B8), thickness = 0.5.dp, modifier = Modifier.width(60.dp))
                        Text("Head of Dept", fontSize = 7.sp, color = Color(0xFF64748B))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = signatory2.take(16),
                            fontSize = 8.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFF0F2942)
                        )
                        HorizontalDivider(color = Color(0xFF94A3B8), thickness = 0.5.dp, modifier = Modifier.width(60.dp))
                        Text("Principal", fontSize = 7.sp, color = Color(0xFF64748B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Date: $issueDate   |   Official College Record",
                fontSize = 7.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
