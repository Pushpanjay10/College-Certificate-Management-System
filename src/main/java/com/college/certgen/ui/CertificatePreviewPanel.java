package com.college.certgen.ui;

import com.college.certgen.models.Certificate;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * High-definition Real-time Visual Certificate Preview Canvas.
 * Supports:
 * 1. Academic Certificate
 * 2. Cultural Program Certificate
 * 3. Sports Certificate
 * 4. Tech Fest Certificate
 */
public class CertificatePreviewPanel extends JPanel {
    private Certificate cert;
    private double zoomFactor = 1.0;

    public CertificatePreviewPanel() {
        setBackground(new Color(240, 242, 245));
        setPreferredSize(new Dimension(842, 595));
    }

    public void setCertificate(Certificate cert) {
        this.cert = cert;
        repaint();
    }

    public Certificate getCertificate() {
        return cert;
    }

    public void setZoomFactor(double factor) {
        this.zoomFactor = Math.max(0.5, Math.min(2.0, factor));
        int w = (int) (842 * zoomFactor);
        int h = (int) (595 * zoomFactor);
        setPreferredSize(new Dimension(w + 40, h + 40));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (cert == null) {
            drawEmptyState((Graphics2D) g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int baseW = 842;
        int baseH = 595;

        int canvasW = (int) (baseW * zoomFactor);
        int canvasH = (int) (baseH * zoomFactor);

        int startX = Math.max(20, (getWidth() - canvasW) / 2);
        int startY = Math.max(20, (getHeight() - canvasH) / 2);

        // Drop shadow for certificate sheet
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(startX + 6, startY + 6, canvasW, canvasH, 12, 12);

        // Certificate Paper Background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(startX, startY, canvasW, canvasH, 8, 8);

        // Clip to certificate bounds & apply scaling
        Shape originalClip = g2.getClip();
        g2.clip(new RoundRectangle2D.Double(startX, startY, canvasW, canvasH, 8, 8));

        g2.translate(startX, startY);
        g2.scale(zoomFactor, zoomFactor);

        // Render Certificate Content at 842x595 virtual coordinates
        renderCertificateContent(g2, baseW, baseH);

        g2.setClip(originalClip);
        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2) {
        g2.setColor(new Color(150, 160, 175));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        String msg = "Select a student or fill certificate details to generate live preview";
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(msg)) / 2;
        int y = getHeight() / 2;
        g2.drawString(msg, x, y);
    }

    public void renderCertificateContent(Graphics2D g2, int w, int h) {
        String template = cert.getTemplateName() != null ? cert.getTemplateName() : "Academic Certificate";

        // Determine Theme Variables
        Color primaryColor;
        Color accentColor;
        Color ribbonColor;

        if (template.contains("Cultural")) {
            // Cultural Theme: Royal Magenta, Violet & Rose Gold
            primaryColor = new Color(88, 28, 135);     // Deep Violet
            accentColor = new Color(217, 70, 239);     // Vibrant Fuchsia/Rose Gold
            ribbonColor = new Color(255, 247, 237);    // Warm Blush Ivory
        } else if (template.contains("Sports")) {
            // Sports Theme: Olympic Gold, Sports Crimson & Athletic Navy
            primaryColor = new Color(30, 41, 59);      // Athletic Navy Slate
            accentColor = new Color(217, 119, 6);      // Olympic Amber Gold
            ribbonColor = new Color(254, 252, 232);    // Energetic Warm Ivory
        } else if (template.contains("Tech")) {
            // Tech Fest Theme: Cyber Dark Slate & Electric Cyan / Blue
            primaryColor = new Color(15, 23, 42);      // Deep Cyber Slate
            accentColor = new Color(14, 165, 233);     // Electric Sky Blue
            ribbonColor = new Color(240, 249, 255);    // Cyber Frost White
        } else {
            // Academic Theme: Royal Navy Blue & Imperial Gold
            primaryColor = new Color(16, 37, 85);      // Deep Imperial Navy
            accentColor = new Color(197, 155, 39);     // Imperial Gold
            ribbonColor = new Color(253, 251, 244);    // Classic Parchment Ivory
        }

        // 1. Background Gradient
        GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, w, h, ribbonColor);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        // 2. Center Background Watermark: College Logo (Semi-transparent)
        BufferedImage logoImg = UIUtils.getActiveCollegeLogoImage();
        if (logoImg != null) {
            Composite oldComp = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
            int lw = 260;
            int lh = 260;
            int lx = (w - lw) / 2;
            int ly = (h - lh) / 2 + 15;
            g2.drawImage(logoImg, lx, ly, lw, lh, null);
            g2.setComposite(oldComp);
        }

        // 3. Borders & Ornaments based on Certificate Type
        if (template.contains("Sports")) {
            drawSportsBorders(g2, w, h, primaryColor, accentColor);
        } else if (template.contains("Tech")) {
            drawTechBorders(g2, w, h, primaryColor, accentColor);
        } else if (template.contains("Cultural")) {
            drawCulturalBorders(g2, w, h, primaryColor, accentColor);
        } else {
            drawAcademicBorders(g2, w, h, primaryColor, accentColor);
        }

        // 4. Top Header with Official College Emblem & Names
        int topHeaderY = 56;
        if (logoImg != null) {
            // Draw crisp official logo on top header
            int topLogoSize = 50;
            g2.drawImage(logoImg, (w - topLogoSize) / 2, 42, topLogoSize, topLogoSize, null);
            topHeaderY = 106;
        }

        // University Name
        g2.setColor(new Color(71, 85, 105));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String uniName = cert.getUniversityName() != null ? cert.getUniversityName().toUpperCase() : UIUtils.getActiveUniversityName();
        drawCenteredString(g2, uniName, w, topHeaderY);

        // College Name (Bold Imperial)
        g2.setColor(primaryColor);
        g2.setFont(new Font("Serif", Font.BOLD, 22));
        String colName = cert.getCollegeName() != null ? cert.getCollegeName() : UIUtils.getActiveCollegeName();
        drawCenteredString(g2, colName, w, topHeaderY + 24);

        // Sub-department or Event Subtitle
        g2.setColor(new Color(100, 116, 139));
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        String subHeader;
        if (template.contains("Sports")) {
            subHeader = "DEPARTMENT OF PHYSICAL EDUCATION & SPORTS • ANNUAL SPORTS MEET";
        } else if (template.contains("Cultural")) {
            subHeader = "ANNUAL CULTURAL & YOUTH FESTIVAL 2026 • CREATIVE ARTS WING";
        } else if (template.contains("Tech")) {
            subHeader = "ANNUAL TECHNICAL SYMPOSIUM & HACKATHON • INNOVATION CELL";
        } else {
            subHeader = UIUtils.getActiveCollegeTagline();
        }
        drawCenteredString(g2, subHeader, w, topHeaderY + 40);

        // Decorative Divider with Emblem dot
        g2.setColor(accentColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(w / 2 - 140, topHeaderY + 50, w / 2 + 140, topHeaderY + 50);
        g2.fillOval(w / 2 - 4, topHeaderY + 47, 8, 8);

        // 5. Certificate Title Banner (Auto-fit font size so it never intersects borders)
        g2.setColor(accentColor);
        String title = cert.getCertificateTitle() != null ? cert.getCertificateTitle().trim() : "CERTIFICATE OF EXCELLENCE";
        int maxTitleWidth = w - 190;
        int titleFontSize = 26;
        Font titleFont = new Font("Serif", Font.BOLD | Font.ITALIC, titleFontSize);
        FontMetrics fmTitle = g2.getFontMetrics(titleFont);
        while (fmTitle.stringWidth(title) > maxTitleWidth && titleFontSize > 13) {
            titleFontSize--;
            titleFont = new Font("Serif", Font.BOLD | Font.ITALIC, titleFontSize);
            fmTitle = g2.getFontMetrics(titleFont);
        }
        g2.setFont(titleFont);
        drawCenteredString(g2, title, w, topHeaderY + 84);

        // Subtitle "THIS IS PROUDLY PRESENTED TO"
        g2.setColor(new Color(100, 116, 139));
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        String presentedText = template.contains("Sports") ? "AWARDED WITH DISTINCTION TO" : "THIS CERTIFICATE IS PROUDLY PRESENTED TO";
        drawCenteredString(g2, presentedText, w, topHeaderY + 114);

        // Recipient Name with underline accent
        g2.setColor(primaryColor);
        g2.setFont(new Font("Serif", Font.BOLD, 28));
        String sName = cert.getStudentName() != null ? cert.getStudentName().toUpperCase() : "STUDENT FULL NAME";
        drawCenteredString(g2, sName, w, topHeaderY + 150);

        FontMetrics fmName = g2.getFontMetrics();
        int nameW = fmName.stringWidth(sName);
        g2.setColor(accentColor);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(w / 2 - nameW / 2 - 12, topHeaderY + 158, w / 2 + nameW / 2 + 12, topHeaderY + 158);

        // Student Identifiers (Roll No / Reg No)
        g2.setColor(new Color(51, 65, 85));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String idDetails = String.format("College Roll No: %s        University Reg No: %s",
                cert.getRollNo() != null ? cert.getRollNo() : "NC/BCA/23/001",
                cert.getRegNo() != null ? cert.getRegNo() : "23NCBCA0101");
        drawCenteredString(g2, idDetails, w, topHeaderY + 182);

        // Academic / Event Description lines with clean word wrapping and comfortable line spacing
        g2.setColor(new Color(51, 65, 85));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        String classDeptInfo = "";
        if (cert.getClassName() != null && !cert.getClassName().trim().isEmpty()) {
            classDeptInfo = "of class " + cert.getClassName().trim();
            if (cert.getDepartment() != null && !cert.getDepartment().trim().isEmpty()) {
                classDeptInfo += ", Department of " + cert.getDepartment().trim();
            }
            classDeptInfo += ", ";
        } else if (cert.getDepartment() != null && !cert.getDepartment().trim().isEmpty()) {
            classDeptInfo = "Department of " + cert.getDepartment().trim() + ", ";
        }

        String remarks = (cert.getRemarks() != null && !cert.getRemarks().trim().isEmpty())
                ? cert.getRemarks().trim()
                : "";

        if (remarks.isEmpty()) {
            if (template.contains("Sports")) {
                remarks = "for outstanding athletic performance, sporting merit, and exemplary teamwork in the Annual Sports Meet.";
            } else if (template.contains("Cultural")) {
                remarks = "for vibrant artistic expression, stage excellence, and noteworthy contribution in the Annual Cultural Fest.";
            } else if (template.contains("Tech")) {
                remarks = "for exemplary technical innovation, algorithmic problem-solving, and inventive execution at Tech Fest 2026.";
            } else {
                remarks = "for exemplary scholastic performance and successful completion of curriculum requirements.";
            }
        }

        String formattedRemarks = (remarks.toLowerCase().startsWith("for ") || remarks.toLowerCase().startsWith("in "))
                ? remarks.substring(0, 1).toLowerCase() + remarks.substring(1)
                : remarks;

        String fullDescription = classDeptInfo + formattedRemarks;
        drawWrappedCenteredString(g2, fullDescription, w / 2, topHeaderY + 206, w - 200, 22);

        // 6. Badges & Seals (Positioned cleanly at exact horizontal center!)
        int sealCX = w / 2;
        int sealCY = h - 96;
        if (template.contains("Sports")) {
            drawSportsTrophyMedal(g2, sealCX, sealCY, accentColor);
        } else if (template.contains("Tech")) {
            drawTechHologramBadge(g2, sealCX, sealCY, accentColor);
        } else if (template.contains("Cultural")) {
            drawCulturalMedallion(g2, sealCX, sealCY, accentColor);
        } else {
            drawAcademicSealMedallion(g2, sealCX, sealCY, 34, accentColor);
        }

        // Real Scannable QR Verification Code Graphic (Bottom Left)
        drawRealQRCode(g2, 54, h - 130, 54, cert);

        // 7. Signatures (Left Signatory & Right Signatory with zero overlap)
        int sig1X = 230;
        int sig2X = w - 210;
        int sigY = h - 96;

        String sig1Title = "Head of Department";
        if (template.contains("Sports")) sig1Title = "Sports In-Charge";
        else if (template.contains("Cultural")) sig1Title = "Cultural Convener";
        else if (template.contains("Tech")) sig1Title = "Tech Fest Coordinator";

        drawSignatureBlock(g2, sig1X, sigY,
                cert.getSignatory1() != null ? cert.getSignatory1() : "Prof. Parmanand Prasad",
                sig1Title, primaryColor);

        drawSignatureBlock(g2, sig2X, sigY,
                cert.getSignatory2() != null ? cert.getSignatory2() : "Prof. (Dr.) Ramakant Prasad",
                "Principal", primaryColor);

        // 8. Footer Metadata (Comfortably padded above bottom border)
        g2.setColor(new Color(100, 116, 139));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        String footerText = String.format("Date of Issue: %s   |   Certificate No: %s   |   %s Verified Record",
                cert.getIssueDate() != null ? cert.getIssueDate() : "2026-11-07",
                cert.getCertificateNo() != null ? cert.getCertificateNo() : "NC-2026-0001",
                colName);
        drawCenteredString(g2, footerText, w, h - 42);
    }

    private void drawAcademicBorders(Graphics2D g2, int w, int h, Color primary, Color gold) {
        g2.setColor(gold);
        g2.setStroke(new BasicStroke(4.5f));
        g2.drawRect(24, 24, w - 48, h - 48);

        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRect(30, 30, w - 60, h - 60);

        // Classic Corner Ornaments
        int s = 32;
        g2.setColor(gold);
        g2.fillPolygon(new int[]{30, 30 + s, 30}, new int[]{30, 30, 30 + s}, 3);
        g2.fillPolygon(new int[]{w - 30, w - 30 - s, w - 30}, new int[]{30, 30, 30 + s}, 3);
        g2.fillPolygon(new int[]{30, 30 + s, 30}, new int[]{h - 30, h - 30, h - 30 - s}, 3);
        g2.fillPolygon(new int[]{w - 30, w - 30 - s, w - 30}, new int[]{h - 30, h - 30, h - 30 - s}, 3);
    }

    private void drawCulturalBorders(Graphics2D g2, int w, int h, Color primary, Color fuchsia) {
        // Multi-line festive border
        g2.setColor(primary);
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawRoundRect(22, 22, w - 44, h - 44, 16, 16);

        g2.setColor(fuchsia);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(28, 28, w - 56, h - 56, 12, 12);

        // Artistic decorative florets at corners
        int[] cornersX = {38, w - 38, 38, w - 38};
        int[] cornersY = {38, 38, h - 38, h - 38};
        for (int i = 0; i < 4; i++) {
            g2.setColor(fuchsia);
            g2.fillOval(cornersX[i] - 6, cornersY[i] - 6, 12, 12);
            g2.setColor(primary);
            g2.drawOval(cornersX[i] - 10, cornersY[i] - 10, 20, 20);
        }
    }

    private void drawSportsBorders(Graphics2D g2, int w, int h, Color primary, Color gold) {
        // Outer athletic border
        g2.setColor(new Color(220, 38, 38)); // Crimson Red
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(22, 22, w - 44, h - 44);

        g2.setColor(gold);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(28, 28, w - 56, h - 56);

        // Sports track speed chevrons along top and bottom corners
        g2.setColor(new Color(220, 38, 38));
        int chSize = 16;
        for (int i = 0; i < 4; i++) {
            int cx = 40 + i * 14;
            g2.fillPolygon(new int[]{cx, cx + 8, cx}, new int[]{30, 36, 42}, 3);
            int rx = w - 40 - i * 14;
            g2.fillPolygon(new int[]{rx, rx - 8, rx}, new int[]{30, 36, 42}, 3);
            g2.fillPolygon(new int[]{cx, cx + 8, cx}, new int[]{h - 30, h - 36, h - 42}, 3);
            g2.fillPolygon(new int[]{rx, rx - 8, rx}, new int[]{h - 30, h - 36, h - 42}, 3);
        }
    }

    private void drawTechBorders(Graphics2D g2, int w, int h, Color primary, Color cyan) {
        g2.setColor(primary);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(22, 22, w - 44, h - 44);

        g2.setColor(cyan);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(28, 28, w - 56, h - 56);

        // Cybernetic bracket corners
        int bLen = 28;
        g2.setStroke(new BasicStroke(3f));
        // Top-left
        g2.drawLine(20, 20 + bLen, 20, 20);
        g2.drawLine(20, 20, 20 + bLen, 20);
        // Top-right
        g2.drawLine(w - 20 - bLen, 20, w - 20, 20);
        g2.drawLine(w - 20, 20, w - 20, 20 + bLen);
        // Bottom-left
        g2.drawLine(20, h - 20 - bLen, 20, h - 20);
        g2.drawLine(20, h - 20, 20 + bLen, h - 20);
        // Bottom-right
        g2.drawLine(w - 20 - bLen, h - 20, w - 20, h - 20);
        g2.drawLine(w - 20, h - 20 - bLen, w - 20, h - 20);

        // Circuit trace dots
        g2.fillOval(20 + bLen, 18, 5, 5);
        g2.fillOval(18, 20 + bLen, 5, 5);
        g2.fillOval(w - 20 - bLen - 5, 18, 5, 5);
        g2.fillOval(w - 23, 20 + bLen, 5, 5);
    }

    private void drawCenteredString(Graphics2D g2, String text, int canvasWidth, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (canvasWidth - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }

    private void drawAcademicSealMedallion(Graphics2D g2, int cx, int cy, int radius, Color color) {
        int points = 24;
        int[] xPts = new int[points];
        int[] yPts = new int[points];
        for (int i = 0; i < points; i++) {
            double angle = i * 2 * Math.PI / points;
            int r = (i % 2 == 0) ? radius : radius - 5;
            xPts[i] = (int) (cx + r * Math.cos(angle));
            yPts[i] = (int) (cy + r * Math.sin(angle));
        }
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 210));
        g2.fillPolygon(xPts, yPts, points);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval(cx - radius + 7, cy - radius + 7, (radius - 7) * 2, (radius - 7) * 2);

        g2.setFont(new Font("Serif", Font.BOLD, 9));
        g2.drawString("OFFICIAL", cx - 21, cy - 3);
        g2.drawString("SEAL", cx - 12, cy + 9);
    }

    private void drawCulturalMedallion(Graphics2D g2, int cx, int cy, Color color) {
        int r = 32;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 200));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - r + 4, cy - r + 4, (r - 4) * 2, (r - 4) * 2);

        g2.setFont(new Font("SansSerif", Font.BOLD, 8));
        g2.drawString("CULTURAL", cx - 22, cy - 4);
        g2.drawString("FEST 2026", cx - 22, cy + 8);
    }

    private void drawSportsTrophyMedal(Graphics2D g2, int cx, int cy, Color color) {
        int r = 32;
        // Ribbon hanging
        g2.setColor(new Color(220, 38, 38));
        g2.fillPolygon(new int[]{cx - 16, cx - 8, cx - 14}, new int[]{cy - 40, cy - 40, cy - 20}, 3);
        g2.setColor(new Color(37, 99, 235));
        g2.fillPolygon(new int[]{cx + 16, cx + 8, cx + 14}, new int[]{cy - 40, cy - 40, cy - 20}, 3);

        // Gold Medal
        g2.setColor(new Color(234, 179, 8));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);

        g2.setColor(new Color(180, 83, 9));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - r + 3, cy - r + 3, (r - 3) * 2, (r - 3) * 2);

        // Crisp vector gold star
        g2.setColor(new Color(146, 64, 14));
        int points = 5;
        int[] sx = new int[points * 2];
        int[] sy = new int[points * 2];
        for (int i = 0; i < points * 2; i++) {
            double angle = i * Math.PI / points - Math.PI / 2;
            int radius = (i % 2 == 0) ? 12 : 5;
            sx[i] = (int) (cx + radius * Math.cos(angle));
            sy[i] = (int) (cy - 1 + radius * Math.sin(angle));
        }
        g2.fillPolygon(sx, sy, points * 2);

        g2.setColor(new Color(120, 53, 15));
        g2.setFont(new Font("SansSerif", Font.BOLD, 7));
        g2.drawString("SPORTS MERIT", cx - 25, cy + 18);
    }

    private void drawTechHologramBadge(Graphics2D g2, int cx, int cy, Color cyan) {
        int r = 30;
        // Hexagonal Badge
        int[] hx = new int[6];
        int[] hy = new int[6];
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3;
            hx[i] = (int) (cx + r * Math.cos(angle));
            hy[i] = (int) (cy + r * Math.sin(angle));
        }
        g2.setColor(new Color(15, 23, 42));
        g2.fillPolygon(hx, hy, 6);

        g2.setColor(cyan);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(hx, hy, 6);

        // Crisp vector lightning bolt
        g2.setColor(cyan);
        int[] lx = {cx, cx + 5, cx + 1, cx + 6, cx - 5, cx - 1, cx - 6};
        int[] ly = {cy - 12, cy - 12, cy - 2, cy - 2, cy + 8, cy, cy};
        g2.fillPolygon(lx, ly, 7);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 7));
        g2.drawString("TECH FEST", cx - 20, cy + 15);
    }

    private void drawSignatureBlock(Graphics2D g2, int cx, int cy, String name, String title, Color color) {
        // Signature Line
        g2.setColor(new Color(180, 185, 195));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(cx - 75, cy, cx + 75, cy);

        // Normalize title prefix like (Dr.) to Dr. so role parentheses can be cleanly stripped
        String raw = (name != null && !name.trim().isEmpty()) ? name.trim() : "";
        raw = raw.replaceAll("(?i)\\((Dr\\.?)\\)", "Dr.");
        String printName = raw;
        if (printName.contains("(")) {
            printName = printName.substring(0, printName.indexOf('(')).trim();
        }

        // Simulated Ink cursive sign above line (only the clean personal name)
        String signName = printName.replaceAll("(?i)(Prof\\.?|Dr\\.?|Mr\\.?|Mrs\\.?|Shri)\\s*", "").trim();
        if (signName.isEmpty()) signName = printName;

        g2.setColor(new Color(30, 45, 95));
        g2.setFont(new Font("Brush Script MT", Font.ITALIC, 21));
        FontMetrics fmSign = g2.getFontMetrics();
        g2.drawString(signName, cx - fmSign.stringWidth(signName) / 2, cy - 6);

        // Signatory Name printed below line
        g2.setColor(color);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics fmName = g2.getFontMetrics();
        g2.drawString(printName, cx - fmName.stringWidth(printName) / 2, cy + 14);

        // Designation / Title
        g2.setColor(new Color(100, 116, 139));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        FontMetrics fmTitle = g2.getFontMetrics();
        g2.drawString(title != null ? title : "", cx - fmTitle.stringWidth(title != null ? title : "") / 2, cy + 26);
    }

    private void drawWrappedCenteredString(Graphics2D g2, String text, int centerX, int startY, int maxWidth, int lineHeight) {
        if (text == null || text.trim().isEmpty()) return;
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (fm.stringWidth(testLine) <= maxWidth) {
                currentLine.append(currentLine.length() == 0 ? "" : " ").append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        int y = startY;
        for (String line : lines) {
            int lineW = fm.stringWidth(line);
            g2.drawString(line, centerX - lineW / 2, y);
            y += lineHeight;
        }
    }

    private void drawRealQRCode(Graphics2D g2, int x, int y, int size, Certificate cert) {
        String verificationData = UIUtils.buildCertificateVerificationData(cert);
        BufferedImage qrImg = UIUtils.generateQRCode(verificationData, size * 2, size * 2);
        if (qrImg != null) {
            g2.setColor(Color.WHITE);
            g2.fillRect(x - 3, y - 3, size + 6, size + 6);
            g2.drawImage(qrImg, x, y, size, size, null);
            g2.setColor(new Color(200, 205, 215));
            g2.drawRect(x - 3, y - 3, size + 6, size + 6);
        } else {
            g2.setColor(Color.WHITE);
            g2.fillRect(x, y, size, size);
        }

        g2.setColor(new Color(100, 116, 139));
        g2.setFont(new Font("SansSerif", Font.BOLD, 8));
        g2.drawString("Scan to Verify", x + 1, y + size + 11);
    }

    public BufferedImage renderToImage() {
        int w = 1684; // 2x high resolution
        int h = 1190;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.scale(2.0, 2.0);
        renderCertificateContent(g2, 842, 595);
        g2.dispose();
        return img;
    }
}
