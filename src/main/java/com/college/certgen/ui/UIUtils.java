package com.college.certgen.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import com.college.certgen.models.Certificate;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class UIUtils {
    // Primary Modern Color Palette
    public static final Color PRIMARY = new Color(24, 75, 160);        // Classic Nalanda Blue
    public static final Color PRIMARY_HOVER = new Color(18, 58, 130);
    public static final Color ACCENT = new Color(217, 119, 6);         // Warm Amber/Gold
    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald Green
    public static final Color DANGER = new Color(239, 68, 68);         // Crimson Red
    public static final Color BACKGROUND = new Color(248, 250, 252);   // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(15, 23, 42);       // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // Slate 500
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // Slate 200
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);      // Slate 900 dark

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private static BufferedImage cachedNalandaLogo = null;
    private static BufferedImage cachedDefaultLogo = null;
    private static BufferedImage cachedActiveLogo = null;
    private static com.college.certgen.models.User activeUser = null;

    public static void setActiveUser(com.college.certgen.models.User user) {
        activeUser = user;
        reloadCollegeBranding();
    }

    public static com.college.certgen.models.User getActiveUser() {
        return activeUser;
    }

    public static void reloadCollegeBranding() {
        cachedNalandaLogo = null;
        cachedDefaultLogo = null;
        cachedActiveLogo = null;
    }

    public static BufferedImage getAppLogoImage() {
        if (cachedDefaultLogo != null) return cachedDefaultLogo;
        try {
            InputStream is = UIUtils.class.getResourceAsStream("/images/app_logo.png");
            if (is != null) {
                cachedDefaultLogo = ImageIO.read(is);
                return cachedDefaultLogo;
            }
            File f = new File("src/main/resources/images/app_logo.png");
            if (f.exists()) {
                cachedDefaultLogo = ImageIO.read(f);
                return cachedDefaultLogo;
            }
            File f2 = new File("resources/images/app_logo.png");
            if (f2.exists()) {
                cachedDefaultLogo = ImageIO.read(f2);
                return cachedDefaultLogo;
            }
        } catch (Exception e) {
            System.err.println("Could not load app logo: " + e.getMessage());
        }
        return getDefaultLogoImage();
    }

    public static ImageIcon getAppLogo(int width, int height) {
        BufferedImage img = getAppLogoImage();
        if (img != null) {
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return null;
    }

    public static BufferedImage getDefaultLogoImage() {
        if (cachedDefaultLogo != null) return cachedDefaultLogo;
        try {
            InputStream is = UIUtils.class.getResourceAsStream("/images/default_logo.png");
            if (is != null) {
                cachedDefaultLogo = ImageIO.read(is);
                return cachedDefaultLogo;
            }
            File f = new File("src/main/resources/images/default_logo.png");
            if (f.exists()) {
                cachedDefaultLogo = ImageIO.read(f);
                return cachedDefaultLogo;
            }
            File f2 = new File("resources/images/default_logo.png");
            if (f2.exists()) {
                cachedDefaultLogo = ImageIO.read(f2);
                return cachedDefaultLogo;
            }
        } catch (Exception e) {
            System.err.println("Could not load default logo: " + e.getMessage());
        }
        return getNalandaLogoImage();
    }

    public static ImageIcon getDefaultLogo(int width, int height) {
        BufferedImage img = getAppLogoImage();
        if (img != null) {
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return null;
    }

    public static BufferedImage getNalandaLogoImage() {
        if (cachedNalandaLogo != null) return cachedNalandaLogo;
        try {
            // Check Classpath
            InputStream is = UIUtils.class.getResourceAsStream("/images/nalanda_logo.jpg");
            if (is != null) {
                cachedNalandaLogo = ImageIO.read(is);
                return cachedNalandaLogo;
            }
            // Check Local filesystem
            File f = new File("src/main/resources/images/nalanda_logo.jpg");
            if (f.exists()) {
                cachedNalandaLogo = ImageIO.read(f);
                return cachedNalandaLogo;
            }
            File f2 = new File("resources/images/nalanda_logo.jpg");
            if (f2.exists()) {
                cachedNalandaLogo = ImageIO.read(f2);
                return cachedNalandaLogo;
            }
        } catch (Exception e) {
            System.err.println("Could not load Nalanda logo: " + e.getMessage());
        }
        return null;
    }

    public static ImageIcon getNalandaLogo(int width, int height) {
        BufferedImage img = getNalandaLogoImage();
        if (img != null) {
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return null;
    }

    public static BufferedImage getActiveCollegeLogoImage() {
        if (cachedActiveLogo != null) return cachedActiveLogo;
        try {
            String customPath = (activeUser != null && activeUser.getCollegeLogoPath() != null && !activeUser.getCollegeLogoPath().trim().isEmpty())
                    ? activeUser.getCollegeLogoPath().trim()
                    : com.college.certgen.database.DatabaseService.getInstance().getSetting("college_logo_path");
            if (customPath != null && !customPath.trim().isEmpty()) {
                File customFile = new File(customPath.trim());
                if (customFile.exists() && customFile.canRead()) {
                    cachedActiveLogo = ImageIO.read(customFile);
                    if (cachedActiveLogo != null) return cachedActiveLogo;
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load custom college logo: " + e.getMessage());
        }

        // Check if institution is Nalanda
        String cName = getActiveCollegeName().toLowerCase();
        if (cName.contains("nalanda")) {
            cachedActiveLogo = getNalandaLogoImage();
        } else {
            cachedActiveLogo = getAppLogoImage();
        }
        return cachedActiveLogo != null ? cachedActiveLogo : getNalandaLogoImage();
    }

    public static ImageIcon getActiveCollegeLogo(int width, int height) {
        BufferedImage img = getActiveCollegeLogoImage();
        if (img != null) {
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return null;
    }

    public static String getActiveCollegeName() {
        if (activeUser != null && activeUser.getCollegeName() != null && !activeUser.getCollegeName().trim().isEmpty()) {
            return activeUser.getCollegeName().trim();
        }
        try {
            if (com.college.certgen.database.DatabaseService.hasInstance()) {
                return com.college.certgen.database.DatabaseService.getInstance().getSetting("college_name", "NALANDA COLLEGE, BIHARSHARIF");
            }
        } catch (Throwable ignored) {}
        return "NALANDA COLLEGE, BIHARSHARIF";
    }

    public static String getActiveUniversityName() {
        if (activeUser != null && activeUser.getUniversityName() != null && !activeUser.getUniversityName().trim().isEmpty()) {
            return activeUser.getUniversityName().trim();
        }
        try {
            if (com.college.certgen.database.DatabaseService.hasInstance()) {
                return com.college.certgen.database.DatabaseService.getInstance().getSetting("university_name", "PATLIPUTRA UNIVERSITY, PATNA");
            }
        } catch (Throwable ignored) {}
        return "PATLIPUTRA UNIVERSITY, PATNA";
    }

    public static String getActiveCollegeTagline() {
        if (activeUser != null && activeUser.getCollegeTagline() != null && !activeUser.getCollegeTagline().trim().isEmpty()) {
            return activeUser.getCollegeTagline().trim();
        }
        try {
            if (com.college.certgen.database.DatabaseService.hasInstance()) {
                return com.college.certgen.database.DatabaseService.getInstance().getSetting("college_tagline", "ESTABLISHED 1870 • CONSTITUENT UNIT OF PATLIPUTRA UNIVERSITY");
            }
        } catch (Throwable ignored) {}
        return "ESTABLISHED 1870 • CONSTITUENT UNIT OF PATLIPUTRA UNIVERSITY";
    }

    public static String buildCertificateVerificationData(Certificate cert) {
        String collegeName = (cert != null && cert.getCollegeName() != null && !cert.getCollegeName().isEmpty())
                ? cert.getCollegeName() : getActiveCollegeName();
        String univName = (cert != null && cert.getUniversityName() != null && !cert.getUniversityName().isEmpty())
                ? cert.getUniversityName() : getActiveUniversityName();

        if (cert == null) return "🎓 " + collegeName + " Official Certificate Verification";
        return String.format(
                "🎓 %s\n" +
                "Affiliated to %s\n" +
                "Official Academic Verification: VALID ✓\n" +
                "------------------------------------\n" +
                "Certificate No : %s\n" +
                "Student Name   : %s\n" +
                "Roll No        : %s\n" +
                "Reg. Number    : %s\n" +
                "Class / Degree : %s\n" +
                "Department     : %s\n" +
                "Certificate    : %s\n" +
                "Issue Date     : %s\n" +
                "Verified by %s Credential Portal",
                collegeName,
                univName,
                cert.getCertificateNo() != null ? cert.getCertificateNo() : "CERT-0001",
                cert.getStudentName() != null ? cert.getStudentName() : "-",
                cert.getRollNo() != null ? cert.getRollNo() : "-",
                cert.getRegNo() != null ? cert.getRegNo() : "-",
                cert.getClassName() != null ? cert.getClassName() : "BCA-3",
                cert.getDepartment() != null ? cert.getDepartment() : "BCA",
                cert.getCertificateTitle() != null ? cert.getCertificateTitle() : "Certificate of Excellence",
                cert.getIssueDate() != null ? cert.getIssueDate() : "2026-11-07",
                collegeName
        );
    }

    public static BufferedImage generateQRCode(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            System.err.println("QR generation error: " + e.getMessage());
            return null;
        }
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DANGER, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    public static JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        if (title != null && !title.isEmpty()) {
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(FONT_SUBTITLE);
            lblTitle.setForeground(TEXT_DARK);
            card.add(lblTitle, BorderLayout.NORTH);
        }
        return card;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(FONT_REGULAR);
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(PRIMARY);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!isSel) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
    }

    public static Icon getNavIcon(String pageKey, boolean active) {
        Color color = active ? Color.WHITE : new Color(148, 163, 184);
        return new VectorIcon(pageKey, color, 18);
    }

    public static Icon getKpiIcon(String key, Color color, int size) {
        return new VectorIcon(key, color, size);
    }

    public static class VectorIcon implements Icon {
        private final String key;
        private final Color color;
        private final int size;

        public VectorIcon(String key, Color color, int size) {
            this.key = key != null ? key.toLowerCase() : "";
            this.color = color;
            this.size = size;
        }

        @Override
        public int getIconWidth() { return size; }

        @Override
        public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            double scale = size / 24.0;
            g2.scale(scale, scale);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if (key.contains("dashboard")) {
                // 4 modern tiles grid
                g2.fillRoundRect(2, 2, 8, 8, 3, 3);
                g2.fillRoundRect(14, 2, 8, 8, 3, 3);
                g2.fillRoundRect(2, 14, 8, 8, 3, 3);
                g2.fillRoundRect(14, 14, 8, 8, 3, 3);
            } else if (key.contains("dept") || key.contains("department")) {
                // Classical university building / columns
                g2.fillPolygon(new int[]{2, 12, 22}, new int[]{7, 2, 7}, 3);
                g2.fillRect(3, 8, 18, 2);
                g2.fillRect(4, 10, 3, 10);
                g2.fillRect(10, 10, 4, 10);
                g2.fillRect(17, 10, 3, 10);
                g2.fillRect(2, 20, 20, 2);
            } else if (key.contains("class")) {
                // Stacked book layers
                g2.drawRoundRect(3, 4, 18, 5, 2, 2);
                g2.drawRoundRect(3, 10, 18, 5, 2, 2);
                g2.drawRoundRect(3, 16, 18, 5, 2, 2);
                g2.drawLine(7, 4, 7, 21);
            } else if (key.contains("student")) {
                // Graduation cap
                g2.fillPolygon(new int[]{12, 22, 12, 2}, new int[]{4, 9, 14, 9}, 4);
                g2.fillArc(6, 11, 12, 8, 180, 180);
                g2.drawLine(20, 9, 20, 18);
                g2.fillOval(19, 17, 3, 3);
            } else if (key.contains("cert_gen") || (key.contains("cert") && !key.contains("history"))) {
                // Certificate document with award ribbon
                g2.drawRoundRect(3, 2, 14, 20, 3, 3);
                g2.drawLine(6, 6, 14, 6);
                g2.drawLine(6, 10, 14, 10);
                g2.drawLine(6, 14, 11, 14);
                g2.fillOval(14, 13, 7, 7);
                g2.drawLine(17, 20, 19, 23);
                g2.drawLine(18, 20, 16, 23);
            } else if (key.contains("history") || key.contains("record")) {
                // Archive folder
                g2.drawRoundRect(2, 6, 20, 15, 3, 3);
                g2.fillPolygon(new int[]{2, 8, 11, 20, 20, 2}, new int[]{6, 6, 3, 3, 6, 6}, 6);
                g2.drawLine(6, 11, 14, 11);
                g2.drawLine(6, 15, 18, 15);
            } else if (key.contains("db") || key.contains("setting")) {
                // Database cylinders
                g2.drawOval(4, 3, 16, 5);
                g2.drawArc(4, 9, 16, 5, 180, 180);
                g2.drawArc(4, 15, 16, 5, 180, 180);
                g2.drawLine(4, 5, 4, 17);
                g2.drawLine(20, 5, 20, 17);
            } else if (key.contains("guide")) {
                // Information / Question badge
                g2.drawOval(2, 2, 20, 20);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.drawString("?", 9, 17);
            } else if (key.contains("logout")) {
                // Door & exit arrow
                g2.drawRect(3, 3, 10, 18);
                g2.drawLine(10, 12, 21, 12);
                g2.drawLine(17, 8, 21, 12);
                g2.drawLine(17, 16, 21, 12);
            } else {
                g2.fillOval(4, 4, 16, 16);
            }
            g2.dispose();
        }
    }
}
