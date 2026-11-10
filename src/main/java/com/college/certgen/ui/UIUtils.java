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
}
