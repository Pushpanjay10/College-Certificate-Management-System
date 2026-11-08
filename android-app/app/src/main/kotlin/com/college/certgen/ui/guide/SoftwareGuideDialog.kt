package com.college.certgen.ui.guide

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.certgen.R

@Composable
fun SoftwareGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Software User Guide",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "College Certificate Management System",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GuideSection(
                    title = "1. Multi-Institution & Account Management",
                    desc = "Create separate accounts for any college or university with custom names, affiliations, and logos. Data is completely isolated per college."
                )
                GuideSection(
                    title = "2. 4 Dedicated Certificate Categories",
                    desc = "• Academic Certificate: Imperial gold & navy double borders for scholastic honors.\n• Cultural Program: Royal violet & magenta with festive embellishments.\n• Sports Certificate: Olympic amber & athletic crimson for championship wins.\n• Tech Fest: Cyber dark slate & electric cyan for hackathons and coding events."
                )
                GuideSection(
                    title = "3. Scannable QR Code Verification",
                    desc = "Every certificate automatically generates a real-time QR code. When scanned with any mobile camera, it displays verified student credentials."
                )
                GuideSection(
                    title = "4. High-Definition Vector PDF & PNG Export",
                    desc = "Export crystal sharp A4 landscape vector PDFs and high-res PNG images ready for printing or instant sharing."
                )
                GuideSection(
                    title = "5. Instant WhatsApp & Mobile Sharing",
                    desc = "Share certificates directly from your smartphone to students via WhatsApp, Telegram, Email, or Print with one tap."
                )
                GuideSection(
                    title = "6. Batch Issuance by Class",
                    desc = "Issue certificates for an entire semester/batch (e.g. BCA-3) at once using the 'Batch by Class' studio feature."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Got It!")
            }
        }
    )
}

@Composable
private fun GuideSection(title: String, desc: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text(desc, color = Color(0xFFCBD5E1), fontSize = 11.5.sp, lineHeight = 16.sp)
        }
    }
}
