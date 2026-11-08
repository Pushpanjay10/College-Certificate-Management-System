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

    // Helper to get theme colors per certificate category
    data class ThemeColors(
        val primaryHex: String,
        val accentHex: String,
        val innerBorderHex: String,
        val titleHex: String,
        val subheaderText: String,
        val sealLabel1: String,
        val sealLabel2: String
    )

    fun getThemeColors(templateName: String, tagline: String): ThemeColors {
        return when {
            templateName.contains("Cultural", ignoreCase = true) -> ThemeColors(
                primaryHex = "#581C87",       // Deep Violet
                accentHex = "#D946EF",        // Magenta / Rose Gold
                innerBorderHex = "#701A75",   // Dark Fuchsia
                titleHex = "#A21CAF",         // Royal Magenta
                subheaderText = "ANNUAL CULTURAL & YOUTH FESTIVAL • CREATIVE ARTS WING",
                sealLabel1 = "CULTURAL",
                sealLabel2 = "EXCELLENCE"
            )
            templateName.contains("Sports", ignoreCase = true) -> ThemeColors(
                primaryHex = "#1E293B",       // Athletic Navy Slate
                accentHex = "#D97706",        // Olympic Amber Gold
                innerBorderHex = "#DC2626",   // Sports Crimson
                titleHex = "#B45309",         // Amber Bronze
                subheaderText = "DEPARTMENT OF PHYSICAL EDUCATION & SPORTS • ANNUAL SPORTS MEET",
                sealLabel1 = "SPORTS",
                sealLabel2 = "CHAMPION"
            )
            templateName.contains("Tech", ignoreCase = true) -> ThemeColors(
                primaryHex = "#0F172A",       // Cyber Dark Slate
                accentHex = "#0EA5E9",        // Electric Cyan
                innerBorderHex = "#0284C7",   // Electric Sky Blue
                titleHex = "#0369A1",         // Deep Tech Blue
                subheaderText = "ANNUAL TECHNICAL SYMPOSIUM & HACKATHON • INNOVATION CELL",
                sealLabel1 = "TECH",
                sealLabel2 = "INNOVATION"
            )
            else -> ThemeColors(
                primaryHex = "#0A2540",       // Deep Navy
                accentHex = "#D4AF37",        // Imperial Gold
                innerBorderHex = "#0F2942",   // Navy Inner Border
                titleHex = "#B8860B",         // Dark Goldenrod
                subheaderText = if (tagline.isNotBlank()) tagline else "ESTABLISHED 1870 • CONSTITUENT UNIT OF PATLIPUTRA UNIVERSITY",
                sealLabel1 = "OFFICIAL",
                sealLabel2 = "SEAL"
            )
        }
    }

    fun generateCertificateBitmap(context: Context, cert: CertificateRecord): Bitmap {
        val pageWidth = 1191
        val pageHeight = 842

        val bitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawCertificateOnCanvas(canvas, pageWidth, pageHeight, cert)

        return bitmap
    }

    fun generateCertificatePdf(context: Context, cert: CertificateRecord): File {
        val pageWidth = 1191
        val pageHeight = 842

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)

        drawCertificateOnCanvas(page.canvas, pageWidth, pageHeight, cert)

        document.finishPage(page)

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

    fun exportCertificatePng(context: Context, cert: CertificateRecord): File {
        val bitmap = generateCertificateBitmap(context, cert)
        val certDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "certificates")
        if (!certDir.exists()) certDir.mkdirs()

        val safeName = cert.studentName.replace(Regex("[^a-zA-Z0-9]"), "_")
        val fileName = "Certificate_${safeName}_${cert.certificateNo}.png"
        val pngFile = File(certDir, fileName)

        FileOutputStream(pngFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return pngFile
    }

    private fun drawCertificateOnCanvas(canvas: Canvas, pageWidth: Int, pageHeight: Int, cert: CertificateRecord) {
        val theme = getThemeColors(cert.templateName, "")

        // 1. Background (Crisp parchment white)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#FFFDF9")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        // 2. Borders
        // Outer Accent Border
        val outerBorderPaint = Paint().apply {
            color = Color.parseColor(theme.accentHex)
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawRect(30f, 30f, pageWidth - 30f, pageHeight - 30f, outerBorderPaint)

        // Thin Middle Border
        val thinBorderPaint = Paint().apply {
            color = Color.parseColor(theme.accentHex)
            alpha = 150
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRect(38f, 38f, pageWidth - 38f, pageHeight - 38f, thinBorderPaint)

        // Inner Theme Border
        val innerBorderPaint = Paint().apply {
            color = Color.parseColor(theme.innerBorderHex)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRect(46f, 46f, pageWidth - 46f, pageHeight - 46f, innerBorderPaint)

        // Corner Ornaments
        val cornerPaint = Paint().apply {
            color = Color.parseColor(theme.accentHex)
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

        // 3. Watermark behind text
        val watermarkPaint = Paint().apply {
            color = Color.parseColor(theme.primaryHex)
            alpha = 10
            textAlign = Paint.Align.CENTER
            textSize = 50f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(cert.collegeName, pageWidth / 2f, pageHeight / 2f + 20f, watermarkPaint)

        // 4. Header: University Name
        val univPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textAlign = Paint.Align.CENTER
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.08f
            isAntiAlias = true
        }
        canvas.drawText(cert.universityName.uppercase(), pageWidth / 2f, 95f, univPaint)

        // College Name
        val collegePaint = Paint().apply {
            color = Color.parseColor(theme.primaryHex)
            textAlign = Paint.Align.CENTER
            textSize = 30f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.04f
            isAntiAlias = true
        }
        canvas.drawText(cert.collegeName.uppercase(), pageWidth / 2f, 138f, collegePaint)

        // Subheader Line (Tailored to Category)
        val taglinePaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textAlign = Paint.Align.CENTER
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.06f
            isAntiAlias = true
        }
        canvas.drawText(theme.subheaderText, pageWidth / 2f, 162f, taglinePaint)

        // Divider Line
        val dividerPaint = Paint().apply {
            color = Color.parseColor(theme.accentHex)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(pageWidth / 2f - 240f, 180f, pageWidth / 2f + 240f, 180f, dividerPaint)

        // 5. Certificate Title
        val titlePaint = Paint().apply {
            color = Color.parseColor(theme.titleHex)
            textAlign = Paint.Align.CENTER
            textSize = 27f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
            letterSpacing = 0.05f
            isAntiAlias = true
        }
        canvas.drawText(cert.certificateTitle.uppercase(), pageWidth / 2f, 224f, titlePaint)

        // 6. Presentation Text
        val presPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textAlign = Paint.Align.CENTER
            textSize = 13.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            letterSpacing = 0.08f
            isAntiAlias = true
        }
        canvas.drawText("THIS CERTIFICATE IS PROUDLY PRESENTED TO", pageWidth / 2f, 265f, presPaint)

        // 7. Student Name
        val namePaint = Paint().apply {
            color = Color.parseColor(theme.primaryHex)
            textAlign = Paint.Align.CENTER
            textSize = 38f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.04f
            isAntiAlias = true
        }
        canvas.drawText(cert.studentName.uppercase(), pageWidth / 2f, 320f, namePaint)

        // Underline under name
        val nameUnderline = Paint().apply {
            color = Color.parseColor(theme.accentHex)
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

        // 9. Citation paragraph
        val textPaint = TextPaint().apply {
            color = Color.parseColor("#334155")
            textSize = 15f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val citationText = if (cert.remarks.isNotBlank()) cert.remarks else {
            "In recognition of outstanding academic performance, exemplary dedication, and the highest standards of scholastic achievement."
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
            Category: ${cert.templateName}
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

        // 11. Category Medallion / Seal (Bottom Center)
        val sealX = 290f
        val sealY = 675f
        val sealPaint = Paint().apply {
            color = Color.parseColor(theme.accentHex)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawCircle(sealX, sealY, 46f, sealPaint)

        val innerSealPaint = Paint().apply {
            color = Color.parseColor(theme.accentHex)
            alpha = 180
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawCircle(sealX, sealY, 41f, innerSealPaint)

        val sealTextPaint = Paint().apply {
            color = Color.parseColor(theme.titleHex)
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(theme.sealLabel1, sealX, sealY - 6f, sealTextPaint)
        canvas.drawText(theme.sealLabel2, sealX, sealY + 10f, sealTextPaint)

        // 12. Signatories (Right Bottom)
        val sig1X = 640f
        val sigLinePaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            strokeWidth = 1.5f
        }
        canvas.drawLine(sig1X - 100f, 690f, sig1X + 100f, 690f, sigLinePaint)

        val cursivePaint = Paint().apply {
            color = Color.parseColor(theme.primaryHex)
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
        val sig1Sub = when {
            cert.templateName.contains("Sports") -> "Sports & Games In-Charge"
            cert.templateName.contains("Cultural") -> "Cultural Convener"
            cert.templateName.contains("Tech") -> "Tech Fest Coordinator"
            else -> "Head of Department"
        }
        canvas.drawText(sig1Sub, sig1X, 726f, sigSubPaint)

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

    fun sharePng(context: Context, file: File, studentName: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "College Certificate Image - $studentName")
            putExtra(Intent.EXTRA_TEXT, "Official College Certificate for $studentName issued via College Certificate Management System.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Certificate Image"))
    }
}
