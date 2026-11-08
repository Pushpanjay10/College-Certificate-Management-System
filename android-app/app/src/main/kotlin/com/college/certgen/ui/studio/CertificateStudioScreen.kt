package com.college.certgen.ui.studio

import android.widget.Toast
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateStudioScreen(
    currentUser: UserAccount,
    dbHelper: AppDatabaseHelper,
    initialStudent: Student? = null,
    onCertificateIssued: () -> Unit
) {
    val context = LocalContext.current
    var allStudents by remember { mutableStateOf(dbHelper.getStudents(currentUser.collegeName)) }

    var selectedStudent by remember {
        mutableStateOf(initialStudent ?: allStudents.firstOrNull())
    }
    var showStudentPicker by remember { mutableStateOf(false) }
    var showTitlePicker by remember { mutableStateOf(false) }
    var showBatchDialog by remember { mutableStateOf(false) }

    // 4 Official Categories matching PC Software
    val categories = listOf(
        "Academic Certificate",
        "Cultural Program Certificate",
        "Sports Certificate",
        "Tech Fest Certificate"
    )
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    val certTitles = listOf(
        "CERTIFICATE OF ACADEMIC EXCELLENCE",
        "CERTIFICATE OF SPORTS MERIT & ACHIEVEMENT",
        "CERTIFICATE OF CULTURAL EXCELLENCE",
        "CERTIFICATE OF TECHNICAL EXCELLENCE & INNOVATION",
        "DEGREE OF BACHELOR OF COMPUTER APPLICATIONS",
        "DEGREE OF MASTER OF COMPUTER APPLICATIONS",
        "CERTIFICATE OF MERIT & DISTINCTION",
        "CERTIFICATE OF PARTICIPATION & APPRECIATION"
    )
    var certificateTitle by remember { mutableStateOf(certTitles[0]) }

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        sdf.format(Date())
    }
    var issueDate by remember { mutableStateOf(todayFormatted) }

    var signatory1 by remember { mutableStateOf("Prof. Parmanand Prasad (BCA In-charge / HOD)") }
    var signatory2 by remember { mutableStateOf(currentUser.principalName) }

    var remarks by remember {
        mutableStateOf(
            "In recognition of outstanding academic performance, exemplary dedication, and the highest standards of scholastic achievement."
        )
    }

    // Function to apply category defaults
    fun applyCategory(cat: String) {
        selectedCategory = cat
        when {
            cat.contains("Sports") -> {
                certificateTitle = "CERTIFICATE OF SPORTS MERIT & ACHIEVEMENT"
                signatory1 = "Coach Rajesh Singh (Sports In-Charge)"
                remarks = "For exceptional athletic prowess, sportsmanship, and outstanding victory in the Inter-College Annual Sports Championship."
            }
            cat.contains("Cultural") -> {
                certificateTitle = "CERTIFICATE OF CULTURAL EXCELLENCE"
                signatory1 = "Prof. Meera Sen (Cultural Convener)"
                remarks = "In recognition of outstanding artistic flair, stage performance, and exemplary contribution in the Annual Cultural Fest."
            }
            cat.contains("Tech") -> {
                certificateTitle = "CERTIFICATE OF TECHNICAL EXCELLENCE & INNOVATION"
                signatory1 = "Dr. Vikash Roy (Tech Fest Coordinator)"
                remarks = "For innovative technical problem-solving, algorithmic excellence, and outstanding project execution at Tech Fest 2026."
            }
            else -> {
                certificateTitle = "CERTIFICATE OF ACADEMIC EXCELLENCE"
                signatory1 = "Prof. Parmanand Prasad (BCA In-charge / HOD)"
                remarks = "In recognition of outstanding academic performance, exemplary dedication, and the highest standards of scholastic achievement."
            }
        }
    }

    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var generatedPngFile by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Studio Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Certificate Studio & Generator",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Craft, preview in real-time, and issue college certificates",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            // Batch Button
            Button(
                onClick = { showBatchDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("⚡ Batch by Class", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Select Student Card
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
                            "Roll: ${selectedStudent!!.rollNo}  •  Reg: ${selectedStudent!!.regNo}  •  ${selectedStudent!!.classCode}"
                        else "Tap to choose student from directory",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Category / Template Selection (4 Categories)
        Text(
            text = "Certificate Category / Template:",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Grid of 4 categories
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryButton(
                    title = "🎓 Academic Certificate",
                    isSelected = selectedCategory == categories[0],
                    accentColor = Color(0xFFD4AF37),
                    onClick = { applyCategory(categories[0]) },
                    modifier = Modifier.weight(1f)
                )
                CategoryButton(
                    title = "🎭 Cultural Program",
                    isSelected = selectedCategory == categories[1],
                    accentColor = Color(0xFFD946EF),
                    onClick = { applyCategory(categories[1]) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryButton(
                    title = "🏆 Sports Certificate",
                    isSelected = selectedCategory == categories[2],
                    accentColor = Color(0xFFD97706),
                    onClick = { applyCategory(categories[2]) },
                    modifier = Modifier.weight(1f)
                )
                CategoryButton(
                    title = "💻 Tech Fest Certificate",
                    isSelected = selectedCategory == categories[3],
                    accentColor = Color(0xFF0EA5E9),
                    onClick = { applyCategory(categories[3]) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Certificate Title Selector
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTitlePicker = true }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Certificate Title / Award Type:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(certificateTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Citation / Remarks
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

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Signatories
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = signatory1,
                onValueChange = { signatory1 = it },
                label = { Text("Left Signatory") },
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
                label = { Text("Right Signatory") },
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

        // --- REAL-TIME LIVE PREVIEW CANVAS ---
        Text(
            text = "Live Real-Time Certificate Preview",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LiveThemeCertificatePreview(
            collegeName = currentUser.collegeName,
            universityName = currentUser.universityName,
            templateName = selectedCategory,
            certificateTitle = certificateTitle,
            studentName = selectedStudent?.name ?: "STUDENT NAME",
            rollNo = selectedStudent?.rollNo ?: "000",
            regNo = selectedStudent?.regNo ?: "21NCBCA000",
            citation = remarks,
            signatory1 = signatory1,
            signatory2 = signatory2,
            issueDate = issueDate
        )

        Spacer(modifier = Modifier.height(18.dp))

        // --- ACTION BUTTONS MATCHING PC SOFTWARE ---
        val st = selectedStudent
        if (st == null) {
            Button(
                onClick = { showStudentPicker = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Select Student to Enable Issuance")
            }
        } else {
            // Save & Issue Certificate
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
                            certificateTitle = certificateTitle,
                            remarks = remarks,
                            issueDate = issueDate,
                            signatory1 = signatory1,
                            signatory2 = signatory2,
                            templateName = selectedCategory
                        )
                        val pdfFile = CertificatePdfGenerator.generateCertificatePdf(context, certRecord)
                        val savedRecord = certRecord.copy(pdfPath = pdfFile.absolutePath)
                        dbHelper.insertCertificate(savedRecord)
                        generatedPdfFile = pdfFile
                        onCertificateIssued()
                        Toast.makeText(context, "Certificate Issued & Saved: $certNo", Toast.LENGTH_SHORT).show()
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
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("💾 Save & Issue Certificate", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Export Row: PDF, PNG, Share
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // PDF Button
                Button(
                    onClick = {
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
                            certificateTitle = certificateTitle,
                            remarks = remarks,
                            issueDate = issueDate,
                            signatory1 = signatory1,
                            signatory2 = signatory2,
                            templateName = selectedCategory
                        )
                        val file = CertificatePdfGenerator.generateCertificatePdf(context, certRecord)
                        generatedPdfFile = file
                        CertificatePdfGenerator.openPdf(context, file)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 12.sp)
                }

                // PNG Button
                Button(
                    onClick = {
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
                            certificateTitle = certificateTitle,
                            remarks = remarks,
                            issueDate = issueDate,
                            signatory1 = signatory1,
                            signatory2 = signatory2,
                            templateName = selectedCategory
                        )
                        val file = CertificatePdfGenerator.exportCertificatePng(context, certRecord)
                        generatedPngFile = file
                        CertificatePdfGenerator.sharePng(context, file, st.name)
                        Toast.makeText(context, "PNG Exported to Pictures!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PNG", fontSize = 12.sp)
                }

                // Share Button (WhatsApp / All)
                Button(
                    onClick = {
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
                            certificateTitle = certificateTitle,
                            remarks = remarks,
                            issueDate = issueDate,
                            signatory1 = signatory1,
                            signatory2 = signatory2,
                            templateName = selectedCategory
                        )
                        val file = generatedPdfFile ?: CertificatePdfGenerator.generateCertificatePdf(context, certRecord)
                        CertificatePdfGenerator.sharePdf(context, file, st.name)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // --- Student Selection Dialog ---
    if (showStudentPicker) {
        var pickerSearch by remember { mutableStateOf("") }
        val filtered = remember(pickerSearch, allStudents) {
            if (pickerSearch.isBlank()) allStudents
            else {
                val q = pickerSearch.trim().lowercase()
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
                        value = pickerSearch,
                        onValueChange = { pickerSearch = it },
                        placeholder = { Text("Search name or roll no...", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        filtered.forEach { stItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedStudent = stItem
                                        generatedPdfFile = null
                                        generatedPngFile = null
                                        showStudentPicker = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#${stItem.rollNo}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(stItem.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Reg: ${stItem.regNo} (${stItem.classCode})", color = Color(0xFF94A3B8), fontSize = 11.sp)
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

    // --- Title Picker Dialog ---
    if (showTitlePicker) {
        AlertDialog(
            onDismissRequest = { showTitlePicker = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("Select Certificate Title", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    certTitles.forEach { title ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    certificateTitle = title
                                    showTitlePicker = false
                                }
                                .padding(vertical = 12.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = title,
                                color = if (title == certificateTitle) Color(0xFF38BDF8) else Color.White,
                                fontWeight = if (title == certificateTitle) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                        HorizontalDivider(color = Color(0xFF334155))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTitlePicker = false }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // --- Batch by Class Dialog ---
    if (showBatchDialog) {
        val classes = listOf("BCA-3", "BCA-2", "BCA-1", "MAC-1", "MBA-1", "BBA-1", "BA-1")
        var selectedBatchClass by remember { mutableStateOf(classes[0]) }

        AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("⚡ Batch Certificate Generation", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Generate and archive certificates for all students of a specific class simultaneously.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Select Class:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    classes.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBatchClass = c }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedBatchClass == c,
                                onClick = { selectedBatchClass = c },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF38BDF8))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(c, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val batchStudents = dbHelper.getStudentsByClass(currentUser.collegeName, selectedBatchClass)
                        if (batchStudents.isEmpty()) {
                            Toast.makeText(context, "No students found in $selectedBatchClass", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        var count = 0
                        batchStudents.forEach { bst ->
                            val cNo = "CERT-${bst.rollNo}-${(1000..9999).random()}"
                            val rec = CertificateRecord(
                                certificateNo = cNo,
                                studentId = bst.id,
                                studentName = bst.name,
                                rollNo = bst.rollNo,
                                regNo = bst.regNo,
                                department = bst.departmentCode,
                                className = bst.classCode,
                                universityName = currentUser.universityName,
                                collegeName = currentUser.collegeName,
                                certificateTitle = certificateTitle,
                                remarks = remarks,
                                issueDate = issueDate,
                                signatory1 = signatory1,
                                signatory2 = signatory2,
                                templateName = selectedCategory
                            )
                            val f = CertificatePdfGenerator.generateCertificatePdf(context, rec)
                            dbHelper.insertCertificate(rec.copy(pdfPath = f.absolutePath))
                            count++
                        }
                        onCertificateIssued()
                        showBatchDialog = false
                        Toast.makeText(context, "Batch Complete: Generated $count certificates for $selectedBatchClass!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA))
                ) {
                    Text("Generate Batch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun CategoryButton(
    title: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.2f) else Color(0xFF1E293B)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color(0xFF334155),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LiveThemeCertificatePreview(
    collegeName: String,
    universityName: String,
    templateName: String,
    certificateTitle: String,
    studentName: String,
    rollNo: String,
    regNo: String,
    citation: String,
    signatory1: String,
    signatory2: String,
    issueDate: String
) {
    val theme = remember(templateName) {
        CertificatePdfGenerator.getThemeColors(templateName, "")
    }

    val sampleQr = remember(studentName, rollNo, templateName) {
        QrCodeUtils.generateQrBitmap("VERIFY:$studentName|$rollNo|$templateName", 90)
    }

    val outerBorderColor = Color(android.graphics.Color.parseColor(theme.accentHex))
    val innerBorderColor = Color(android.graphics.Color.parseColor(theme.innerBorderHex))
    val primaryTextColor = Color(android.graphics.Color.parseColor(theme.primaryHex))
    val titleTextColor = Color(android.graphics.Color.parseColor(theme.titleHex))

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF8)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, outerBorderColor, RoundedCornerShape(10.dp))
            .padding(4.dp)
            .border(1.5.dp, innerBorderColor, RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // University
            Text(
                text = universityName.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                letterSpacing = 1.sp
            )
            // College
            Text(
                text = collegeName.uppercase(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = primaryTextColor
            )
            // Subheader
            Text(
                text = theme.subheaderText,
                fontSize = 7.5.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = outerBorderColor, thickness = 1.dp, modifier = Modifier.width(180.dp))
            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = certificateTitle.uppercase(),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                color = titleTextColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = "THIS CERTIFICATE IS PROUDLY PRESENTED TO",
                fontSize = 7.5.sp,
                color = Color(0xFF475569),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Student Name
            Text(
                text = studentName.uppercase(),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = primaryTextColor
            )

            Text(
                text = "College Roll No: $rollNo   •   University Reg No: $regNo",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Citation
            Text(
                text = citation,
                fontSize = 8.sp,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Section: QR, Medallion, Signatures
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
                        modifier = Modifier.size(42.dp)
                    )
                    Text("Scan to Verify", fontSize = 6.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }

                // Official Medallion Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .border(1.5.dp, outerBorderColor, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${theme.sealLabel1}\n${theme.sealLabel2}",
                        fontSize = 5.5.sp,
                        color = titleTextColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 7.5.sp
                    )
                }

                // Signatories
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = signatory1.take(15),
                            fontSize = 7.5.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            color = primaryTextColor
                        )
                        HorizontalDivider(color = Color(0xFF94A3B8), thickness = 0.5.dp, modifier = Modifier.width(55.dp))
                        Text(
                            if (templateName.contains("Sports")) "Sports In-Charge"
                            else if (templateName.contains("Cultural")) "Cultural Convener"
                            else if (templateName.contains("Tech")) "Tech Coordinator"
                            else "Head of Dept",
                            fontSize = 6.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = signatory2.take(15),
                            fontSize = 7.5.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            color = primaryTextColor
                        )
                        HorizontalDivider(color = Color(0xFF94A3B8), thickness = 0.5.dp, modifier = Modifier.width(55.dp))
                        Text("Principal", fontSize = 6.5.sp, color = Color(0xFF64748B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Date: $issueDate   |   ${collegeName} Official Record",
                fontSize = 6.5.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
