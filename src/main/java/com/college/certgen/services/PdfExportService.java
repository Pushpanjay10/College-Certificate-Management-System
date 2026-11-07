package com.college.certgen.services;

import com.college.certgen.models.Certificate;
import com.college.certgen.ui.UIUtils;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

/**
 * High-resolution Vector PDF Certificate Export Service using OpenPDF.
 * Tailored for Nalanda College, Biharsharif with:
 * 1. Academic Certificate
 * 2. Cultural Program Certificate
 * 3. Sports Certificate
 * 4. Tech Fest Certificate
 */
public class PdfExportService {

    public static boolean exportCertificate(Certificate cert, File targetFile) {
        // Landscape A4 (842 x 595 points)
        Rectangle pageSize = PageSize.A4.rotate();
        Document document = new Document(pageSize, 36, 36, 36, 36);

        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();

            PdfContentByte cb = writer.getDirectContent();
            PdfContentByte cbUnder = writer.getDirectContentUnder();

            String template = cert.getTemplateName() != null ? cert.getTemplateName() : "Academic Certificate";

            // 1. Draw Background Watermark: College Logo (Semi-transparent)
            BufferedImage logoImg = UIUtils.getActiveCollegeLogoImage();
            if (logoImg != null) {
                try {
                    com.lowagie.text.Image bgLogo = com.lowagie.text.Image.getInstance(writer, logoImg, 1.0f);
                    float logoSize = 250f;
                    bgLogo.scaleToFit(logoSize, logoSize);
                    float lx = (pageSize.getWidth() - bgLogo.getScaledWidth()) / 2f;
                    float ly = (pageSize.getHeight() - bgLogo.getScaledHeight()) / 2f - 10f;
                    bgLogo.setAbsolutePosition(lx, ly);

                    PdfGState gstate = new PdfGState();
                    gstate.setFillOpacity(0.12f);
                    cbUnder.saveState();
                    cbUnder.setGState(gstate);
                    cbUnder.addImage(bgLogo);
                    cbUnder.restoreState();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // 2. Draw Ornamental Borders based on Certificate Category
            drawBorders(cb, pageSize, template);

            // 3. Draw Category-specific Medallion / Seal
            drawSeal(cb, pageSize, template);

            // 4. Document Header: College Logo
            if (logoImg != null) {
                try {
                    com.lowagie.text.Image topLogo = com.lowagie.text.Image.getInstance(writer, logoImg, 1.0f);
                    topLogo.scaleToFit(50f, 50f);
                    topLogo.setAlignment(Element.ALIGN_CENTER);
                    topLogo.setSpacingAfter(4f);
                    document.add(topLogo);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // University Name
            Font uniFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(71, 85, 105));
            String uniText = cert.getUniversityName() != null ? cert.getUniversityName().toUpperCase() : UIUtils.getActiveUniversityName();
            Paragraph pUni = new Paragraph(uniText, uniFont);
            pUni.setAlignment(Element.ALIGN_CENTER);
            pUni.setSpacingAfter(2f);
            document.add(pUni);

            // College Name
            Font colFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, getPrimaryColor(template));
            String colText = cert.getCollegeName() != null ? cert.getCollegeName().toUpperCase() : UIUtils.getActiveCollegeName();
            Paragraph pCol = new Paragraph(colText, colFont);
            pCol.setAlignment(Element.ALIGN_CENTER);
            pCol.setSpacingAfter(3f);
            document.add(pCol);

            // Sub-Department / Event line
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(100, 116, 139));
            String subHeaderText;
            if (template.contains("Sports")) {
                subHeaderText = "DEPARTMENT OF PHYSICAL EDUCATION & SPORTS • ANNUAL SPORTS MEET";
            } else if (template.contains("Cultural")) {
                subHeaderText = "ANNUAL CULTURAL & YOUTH FESTIVAL 2026 • CREATIVE ARTS WING";
            } else if (template.contains("Tech")) {
                subHeaderText = "ANNUAL TECHNICAL SYMPOSIUM & HACKATHON • INNOVATION CELL";
            } else {
                subHeaderText = UIUtils.getActiveCollegeTagline();
            }
            Paragraph pSubHead = new Paragraph(subHeaderText, subHeaderFont);
            pSubHead.setAlignment(Element.ALIGN_CENTER);
            pSubHead.setSpacingAfter(10f);
            document.add(pSubHead);

            // Certificate Title Banner
            Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 25, getTitleColor(template));
            Paragraph pTitle = new Paragraph(cert.getCertificateTitle() != null ? cert.getCertificateTitle() : "CERTIFICATE OF EXCELLENCE", titleFont);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            pTitle.setSpacingAfter(8f);
            document.add(pTitle);

            // Subtitle
            Font presentedFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(100, 116, 139));
            String presented = template.contains("Sports") ? "AWARDED WITH DISTINCTION TO" : "THIS CERTIFICATE IS PROUDLY PRESENTED TO";
            Paragraph pPresented = new Paragraph(presented, presentedFont);
            pPresented.setAlignment(Element.ALIGN_CENTER);
            pPresented.setSpacingAfter(8f);
            document.add(pPresented);

            // Recipient Student Name
            Font nameFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 24, getPrimaryColor(template));
            Paragraph pName = new Paragraph(cert.getStudentName() != null ? cert.getStudentName().toUpperCase() : "STUDENT NAME", nameFont);
            pName.setAlignment(Element.ALIGN_CENTER);
            pName.setSpacingAfter(8f);
            document.add(pName);

            // Body Statement
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(51, 65, 85));

            String descLine;
            if (template.contains("Sports")) {
                descLine = "for outstanding athletic performance, sporting merit, and exemplary teamwork in " +
                        (cert.getClassName() != null ? cert.getClassName() : "Annual Athletic Meet") +
                        ", Department of " + (cert.getDepartment() != null ? cert.getDepartment() : "Sports") + ".\n" +
                        (cert.getRemarks() != null && !cert.getRemarks().isEmpty() ? cert.getRemarks() : "Awarded this certificate in recognition of athletic merit and dedication.");
            } else if (template.contains("Cultural")) {
                descLine = "for vibrant artistic expression, stage excellence, and noteworthy contribution in " +
                        (cert.getClassName() != null ? cert.getClassName() : "Annual Cultural Fest") +
                        ", Department of " + (cert.getDepartment() != null ? cert.getDepartment() : "Cultural Wing") + ".\n" +
                        (cert.getRemarks() != null && !cert.getRemarks().isEmpty() ? cert.getRemarks() : "Awarded in celebration of creative talent and artistic brilliance.");
            } else if (template.contains("Tech")) {
                descLine = "for exemplary technical innovation, algorithmic problem solving, and inventive execution in " +
                        (cert.getClassName() != null ? cert.getClassName() : "Techno-Hackathon 2026") +
                        ", Department of " + (cert.getDepartment() != null ? cert.getDepartment() : "Computer Applications") + ".\n" +
                        (cert.getRemarks() != null && !cert.getRemarks().isEmpty() ? cert.getRemarks() : "Awarded for technical brilliance, innovation, and coding excellence.");
            } else {
                descLine = "for exemplary scholastic performance and successful completion of curriculum requirements in " +
                        (cert.getClassName() != null ? cert.getClassName() : "Bachelor of Computer Applications") +
                        ", Department of " + (cert.getDepartment() != null ? cert.getDepartment() : "Computer Applications") + ".\n" +
                        (cert.getRemarks() != null && !cert.getRemarks().isEmpty() ? cert.getRemarks() : "Demonstrated exceptional merit and high standard of scholarship.");
            }

            String bodyText = String.format("College Roll No: %s        University Reg No: %s\n%s",
                    cert.getRollNo() != null ? cert.getRollNo() : "NC/BCA/23/001",
                    cert.getRegNo() != null ? cert.getRegNo() : "23NCBCA0101",
                    descLine
            );

            Paragraph pBody = new Paragraph(bodyText, bodyFont);
            pBody.setAlignment(Element.ALIGN_CENTER);
            pBody.setLeading(16f);
            pBody.setSpacingAfter(26f);
            document.add(pBody);

            // Footer Table for Signatures & Date
            PdfPTable footerTable = new PdfPTable(3);
            footerTable.setWidthPercentage(85);
            footerTable.setWidths(new float[]{36, 28, 36});

            // Signatory 1 (Left)
            Font sigFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 41, 59));
            Font sigSubFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 116, 139));

            String sig1Title = "Head of Department";
            if (template.contains("Sports")) sig1Title = "Sports In-Charge";
            else if (template.contains("Cultural")) sig1Title = "Cultural Convener";
            else if (template.contains("Tech")) sig1Title = "Tech Fest Coordinator";

            PdfPCell c1 = new PdfPCell();
            c1.setBorder(Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph pSig1 = new Paragraph(cert.getSignatory1() != null ? cert.getSignatory1() : "Prof. Parmanand Prasad", sigFont);
            pSig1.setAlignment(Element.ALIGN_CENTER);
            Paragraph pSubSig1 = new Paragraph(sig1Title, sigSubFont);
            pSubSig1.setAlignment(Element.ALIGN_CENTER);
            c1.addElement(pSig1);
            c1.addElement(pSubSig1);
            footerTable.addCell(c1);

            // Center Column: Reserved for the Golden Official Seal Medallion (drawn cleanly in center)
            PdfPCell c2 = new PdfPCell();
            c2.setBorder(Rectangle.NO_BORDER);
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            // Spacer to give the central seal medallion dedicated breathing room
            Paragraph pSpacer = new Paragraph(" ", sigFont);
            c2.addElement(pSpacer);
            footerTable.addCell(c2);

            // Signatory 2 (Right)
            PdfPCell c3 = new PdfPCell();
            c3.setBorder(Rectangle.NO_BORDER);
            c3.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph pSig2 = new Paragraph(cert.getSignatory2() != null ? cert.getSignatory2() : "Prof. (Dr.) Ramakant Prasad", sigFont);
            pSig2.setAlignment(Element.ALIGN_CENTER);
            String sig2Sub = "Principal";
            if (cert.getCollegeName() != null && !cert.getCollegeName().trim().isEmpty()) {
                String cShort = cert.getCollegeName().trim();
                if (cShort.length() > 22) cShort = cShort.substring(0, 22) + "...";
                sig2Sub = "Principal, " + cShort;
            }
            Paragraph pSubSig2 = new Paragraph(sig2Sub, sigSubFont);
            pSubSig2.setAlignment(Element.ALIGN_CENTER);
            c3.addElement(pSig2);
            c3.addElement(pSubSig2);
            footerTable.addCell(c3);

            // Real Scannable QR Code on PDF at bottom-left (Cleanly within borders, no collision)
            BufferedImage qrImg = UIUtils.generateQRCode(UIUtils.buildCertificateVerificationData(cert), 140, 140);
            if (qrImg != null) {
                com.lowagie.text.Image pdfQr = com.lowagie.text.Image.getInstance(writer, qrImg, 1.0f);
                pdfQr.scaleToFit(44f, 44f);
                pdfQr.setAbsolutePosition(48f, 54f);
                cb.addImage(pdfQr);

                Font qrLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new Color(100, 116, 139));
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                        new Phrase("Scan to Verify", qrLabelFont),
                        70f, 43f, 0);
            }

            // Draw clean signature lines above Signatory 1 and Signatory 2
            cb.setLineWidth(1f);
            cb.setColorStroke(new Color(180, 185, 195));
            // Left signature line
            cb.moveTo(110f, 96f);
            cb.lineTo(260f, 96f);
            cb.stroke();
            // Right signature line
            cb.moveTo(pageSize.getWidth() - 260f, 96f);
            cb.lineTo(pageSize.getWidth() - 110f, 96f);
            cb.stroke();

            // Fixed bottom position for footerTable (starts at y = 92 from bottom of page)
            footerTable.setTotalWidth(pageSize.getWidth() - 140f);
            footerTable.writeSelectedRows(0, -1, 70f, 92f, cb);

            // Bottom metadata string (Cleanly positioned 12 points ABOVE the inner border line, ZERO overlap)
            Font footerMetaFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 116, 139));
            String activeCol = (cert.getCollegeName() != null && !cert.getCollegeName().trim().isEmpty())
                    ? cert.getCollegeName().trim() : UIUtils.getActiveCollegeName();
            String metaText = "Date of Issue: " + (cert.getIssueDate() != null ? cert.getIssueDate() : "2026-11-07") +
                    "   |   Certificate No: " + (cert.getCertificateNo() != null ? cert.getCertificateNo() : "NC-2026-0001") +
                    "   |   " + activeCol + " Official Verified Record";
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(metaText, footerMetaFont),
                    pageSize.getWidth() / 2f, 38f, 0);

            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Color getPrimaryColor(String template) {
        if (template.contains("Cultural")) return new Color(88, 28, 135);
        if (template.contains("Sports")) return new Color(30, 41, 59);
        if (template.contains("Tech")) return new Color(15, 23, 42);
        return new Color(16, 37, 85); // Academic
    }

    private static Color getTitleColor(String template) {
        if (template.contains("Cultural")) return new Color(192, 38, 211);
        if (template.contains("Sports")) return new Color(217, 119, 6);
        if (template.contains("Tech")) return new Color(14, 165, 233);
        return new Color(197, 155, 39); // Academic Gold
    }

    private static void drawBorders(PdfContentByte cb, Rectangle page, String template) {
        float width = page.getWidth();
        float height = page.getHeight();

        Color borderColor = new Color(197, 155, 39); // Academic Gold
        Color innerColor = new Color(197, 155, 39);

        if (template.contains("Cultural")) {
            borderColor = new Color(88, 28, 135);
            innerColor = new Color(217, 70, 239);
        } else if (template.contains("Sports")) {
            borderColor = new Color(220, 38, 38); // Crimson
            innerColor = new Color(217, 119, 6);  // Gold
        } else if (template.contains("Tech")) {
            borderColor = new Color(15, 23, 42);
            innerColor = new Color(14, 165, 233);
        }

        // Outer Border
        cb.setColorStroke(borderColor);
        cb.setLineWidth(4f);
        cb.rectangle(20, 20, width - 40, height - 40);
        cb.stroke();

        // Inner Border
        cb.setColorStroke(innerColor);
        cb.setLineWidth(1.5f);
        cb.rectangle(26, 26, width - 52, height - 52);
        cb.stroke();

        // Corner Flourishes (Refined compact size to avoid touching content)
        float cornerSize = 18f;
        cb.setColorFill(innerColor);
        // Top Left
        cb.moveTo(26, height - 26);
        cb.lineTo(26 + cornerSize, height - 26);
        cb.lineTo(26, height - 26 - cornerSize);
        cb.closePathFillStroke();

        // Top Right
        cb.moveTo(width - 26, height - 26);
        cb.lineTo(width - 26 - cornerSize, height - 26);
        cb.lineTo(width - 26, height - 26 - cornerSize);
        cb.closePathFillStroke();

        // Bottom Left
        cb.moveTo(26, 26);
        cb.lineTo(26 + cornerSize, 26);
        cb.lineTo(26, 26 + cornerSize);
        cb.closePathFillStroke();

        // Bottom Right
        cb.moveTo(width - 26, 26);
        cb.lineTo(width - 26 - cornerSize, 26);
        cb.lineTo(width - 26, 26 + cornerSize);
        cb.closePathFillStroke();
    }

    private static void drawSeal(PdfContentByte cb, Rectangle page, String template) {
        // Positioned cleanly at EXACT HORIZONTAL CENTER between the two signatories
        float cx = page.getWidth() / 2f;
        float cy = 78f;
        float radius = 25f;

        Color sealColor = new Color(197, 155, 39);
        if (template.contains("Cultural")) sealColor = new Color(192, 38, 211);
        else if (template.contains("Sports")) sealColor = new Color(217, 119, 6);
        else if (template.contains("Tech")) sealColor = new Color(14, 165, 233);

        // Ribbon tails underneath the seal medallion
        cb.setColorFill(sealColor);
        cb.moveTo(cx - 14f, cy - 14f);
        cb.lineTo(cx - 20f, cy - 36f);
        cb.lineTo(cx - 10f, cy - 31f);
        cb.lineTo(cx, cy - 18f);
        cb.closePathFillStroke();

        cb.moveTo(cx + 14f, cy - 14f);
        cb.lineTo(cx + 20f, cy - 36f);
        cb.lineTo(cx + 10f, cy - 31f);
        cb.lineTo(cx, cy - 18f);
        cb.closePathFillStroke();

        // Outer serrated star floret ring
        cb.setColorFill(sealColor);
        int points = 16;
        for (int i = 0; i < points; i++) {
            double a1 = i * (2 * Math.PI / points);
            double a2 = a1 + (Math.PI / points);
            float rOuter = radius + 3f;
            float rInner = radius - 1f;

            float x1 = (float) (cx + rOuter * Math.cos(a1));
            float y1 = (float) (cy + rOuter * Math.sin(a1));
            float x2 = (float) (cx + rInner * Math.cos(a2));
            float y2 = (float) (cy + rInner * Math.sin(a2));

            if (i == 0) cb.moveTo(x1, y1);
            else cb.lineTo(x1, y1);
            cb.lineTo(x2, y2);
        }
        cb.closePathFillStroke();

        // Inner solid background disc
        cb.setColorFill(new Color(254, 252, 232)); // Light golden cream
        cb.circle(cx, cy, radius - 2f);
        cb.fill();

        // Inner concentric rings
        cb.setColorStroke(sealColor);
        cb.setLineWidth(1.5f);
        cb.circle(cx, cy, radius - 3f);
        cb.stroke();

        cb.setLineWidth(0.8f);
        cb.circle(cx, cy, radius - 6f);
        cb.stroke();

        // Center Seal Label Text
        Font sealFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.5f, sealColor);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase("OFFICIAL", sealFont),
                cx, cy + 2f, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase("SEAL", sealFont),
                cx, cy - 7f, 0);
    }
}
