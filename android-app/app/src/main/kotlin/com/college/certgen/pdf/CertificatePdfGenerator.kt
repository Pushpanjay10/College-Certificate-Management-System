package com.college.certgen.pdf

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.college.certgen.data.models.CertificateRecord
import java.io.File
import java.io.FileOutputStream

object CertificatePdfGenerator {

    fun generateCertificatePdf(context: Context, cert: CertificateRecord): File {
        // High-res A4 Landscape: 1191 x 842 points
        val pageWidth = 1191
        val pageHeight = 842

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // 1. Background (Parchment white)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#FFFDF9")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        // 2. Borders
        // Outer Gold Border
        val goldPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawRect(30f, 30f, pageWidth - 30f, pageHeight - 30f, goldPaint)

        // Thin Outer Gold Border
        val thinGoldPaint = Paint().apply {
            color = Color.parseColor("#E5C158")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRect(38f, 38f, pageWidth - 38f, pageHeight - 38f, thinGoldPaint)

        // Inner Navy Border
        val navyBorderPaint = Paint().apply {
            color = Color.parseColor("#0F2942")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRect(46f, 46f, pageWidth - 46f, pageHeight - 46f, navyBorderPaint)

        // Corner Ornaments
        val cornerPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val corners = listOf(
            Pair(30f, 30f),
            Pair(pageWidth - 46f, 30f),
            Pair(30f, pageHeight - 46f),
            Pair(pageWidth - 46f, pageHeight - 46f)
        )
        for ((cx, cy) in corners) {
            canvas.drawRect(cx, cy, cx + 16f, cy + 16f, cornerPaint)
        }

        // 3. Watermark in center background
        val watermarkPaint = Paint().apply {
            color = Color.parseColor("#0A2540")
            alpha = 12
            textAlign = Paint.Align.CENTER
            textSize = 55f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(cert.collegeName, pageWidth / 2f, pageHeight / 2f + 20f, watermarkPaint)

        // 4. Header: University Name
        val univPaint = Paint().apply {
            color = Color.parseColor("#1E3A5F")
            textAlign = Paint.Align.CENTER
            textSize = 17f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.08f
            isAntiAlias = true
        }
        canvas.drawText(cert.universityName.uppercase(), pageWidth / 2f, 95f, univPaint)

        // College Name
        val collegePaint = Paint().apply {
            color = Color.parseColor("#0A2540")
            textAlign = Paint.Align.CENTER
            textSize = 30f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.04f
            isAntiAlias = true
        }
        canvas.drawText(cert.collegeName.uppercase(), pageWidth / 2f, 138f, collegePaint)

        // Tagline
        val taglinePaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textAlign = Paint.Align.CENTER
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            letterSpacing = 0.05f
            isAntiAlias = true
        }
        canvas.drawText("ESTABLISHED 1870 • CONSTITUENT UNIT OF PATLIPUTRA UNIVERSITY", pageWidth / 2f, 162f, taglinePaint)

        // Divider Line
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(pageWidth / 2f - 220f, 180f, pageWidth / 2f + 220f, 180f, dividerPaint)

        // 5. Certificate Title
        val titlePaint = Paint().apply {
            color = Color.parseColor("#B8860B")
            textAlign = Paint.Align.CENTER
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
            letterSpacing = 0.06f
            isAntiAlias = true
        }
        canvas.drawText(cert.certificateTitle.uppercase(), pageWidth / 2f, 225f, titlePaint)

        // 6. Presentation Text
        val presPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textAlign = Paint.Align.CENTER
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            letterSpacing = 0.08f
            isAntiAlias = true
        }
        canvas.drawText("THIS CERTIFICATE IS PROUDLY PRESENTED TO", pageWidth / 2f, 265f, presPaint)

        // 7. Student Name
        val namePaint = Paint().apply {
            color = Color.parseColor("#0A2540")
            textAlign = Paint.Align.CENTER
            textSize = 38f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.04f
            isAntiAlias = true
        }
        canvas.drawText(cert.studentName.uppercase(), pageWidth / 2f, 320f, namePaint)

        // Underline under name
        val nameUnderline = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }
        val nameWidth = namePaint.measureText(cert.studentName.uppercase())
        canvas.drawLine(pageWidth / 2f - (nameWidth / 2f + 20f), 332f, pageWidth / 2f + (nameWidth / 2f + 20f), 332f, nameUnderline)

        // 8. Roll No & Reg No
        val metaPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textAlign = Paint.Align.CENTER
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val rollText = "College Roll No: ${cert.rollNo}     •     University Reg No: ${cert.regNo}"
        canvas.drawText(rollText, pageWidth / 2f, 365f, metaPaint)

        // 9. Citation paragraph (Multi-line text)
        val textPaint = TextPaint().apply {
            color = Color.parseColor("#334155")
            textSize = 15f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val citationText = if (cert.remarks.isNotBlank()) cert.remarks else {
            "for exemplary scholastic performance and successful completion of curriculum requirements in ${cert.className}, Department of ${cert.department}. In recognition of outstanding academic performance, exemplary dedication, and the highest standards of scholastic achievement."
        }
        val staticLayout = StaticLayout.Builder.obtain(
            citationText, 0, citationText.length, textPaint, 880
        ).setAlignment(Layout.Alignment.ALIGN_CENTER).build()

        canvas.save()
        canvas.translate(pageWidth / 2f - 440f, 400f)
        staticLayout.draw(canvas)
        canvas.restore()

        // 10. QR Code (Bottom Left)
        val qrContent = """
            --- COLLEGE CERTIFICATE VERIFICATION ---
            Certificate No: ${cert.certificateNo}
            Student Name: ${cert.studentName}
            Roll No: ${cert.rollNo}
            Reg No: ${cert.regNo}
            College: ${cert.collegeName}
            University: ${cert.universityName}
            Course: ${cert.className}
            Issue Date: ${cert.issueDate}
            Status: VERIFIED & AUTHENTIC
        """.trimIndent()
        val qrBitmap = QrCodeUtils.generateQrBitmap(qrContent, 130)
        canvas.drawBitmap(qrBitmap, 90f, 610f, null)

        val qrLabelPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("SCAN TO VERIFY", 90f + 65f, 755f, qrLabelPaint)

        // 11. Official Seal (Golden circular badge next to QR code)
        val sealX = 290f
        val sealY = 675f
        val sealPaint = Paint().apply {
            color = Color.parseColor("#C5A059")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawCircle(sealX, sealY, 45f, sealPaint)
        val innerSealPaint = Paint().apply {
            color = Color.parseColor("#DFBA67")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawCircle(sealX, sealY, 40f, innerSealPaint)

        val sealTextPaint = Paint().apply {
            color = Color.parseColor("#856404")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("OFFICIAL", sealX, sealY - 6f, sealTextPaint)
        canvas.drawText("SEAL", sealX, sealY + 10f, sealTextPaint)

        // 12. Signatories (Right Bottom)
        // Signatory 1: HOD
        val sig1X = 640f
        val sigLinePaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            strokeWidth = 1.5f
        }
        canvas.drawLine(sig1X - 100f, 690f, sig1X + 100f, 690f, sigLinePaint)

        val cursivePaint = Paint().apply {
            color = Color.parseColor("#0F2942")
            textSize = 19f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(cert.signatory1, sig1X, 678f, cursivePaint)

        val sigTitlePaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(cert.signatory1, sig1X, 710f, sigTitlePaint)

        val sigSubPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Head of Department", sig1X, 726f, sigSubPaint)

        // Signatory 2: Principal
        val sig2X = 960f
        canvas.drawLine(sig2X - 100f, 690f, sig2X + 100f, 690f, sigLinePaint)
        canvas.drawText(cert.signatory2, sig2X, 678f, cursivePaint)
        canvas.drawText(cert.signatory2, sig2X, 710f, sigTitlePaint)
        canvas.drawText("Principal, College", sig2X, 726f, sigSubPaint)

        // 13. Footer Bottom Bar
        val footerPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val footerText = "Date of Issue: ${cert.issueDate}     |     Certificate No: ${cert.certificateNo}     |     ${cert.collegeName} Official Record"
        canvas.drawText(footerText, pageWidth / 2f, 792f, footerPaint)

        document.finishPage(page)

        // Write to file
        val certDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "certificates")
        if (!certDir.exists()) certDir.mkdirs()

        val safeName = cert.studentName.replace(Regex("[^a-zA-Z0-9]"), "_")
        val fileName = "Certificate_${safeName}_${cert.certificateNo}.pdf"
        val pdfFile = File(certDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    fun openPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Open Certificate PDF"))
    }

    fun sharePdf(context: Context, file: File, studentName: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "College Certificate - $studentName")
            putExtra(Intent.EXTRA_TEXT, "Official College Certificate for $studentName issued via College Certificate Management System.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Certificate PDF"))
    }
}
