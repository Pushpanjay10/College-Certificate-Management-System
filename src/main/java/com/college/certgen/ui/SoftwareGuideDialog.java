package com.college.certgen.ui;

import com.college.certgen.database.DatabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern 1-page Quickstart Guide presenting all capabilities of the software.
 * Shown to new users upon install/first login, and accessible via the Help button anytime.
 */
public class SoftwareGuideDialog extends JDialog {

    private final JCheckBox chkDontShow;

    public static void showGuide(Component parent, boolean force) {
        if (!force && DatabaseService.getInstance().isSettingTrue("software_guide_dismissed")) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Frame owner = parent instanceof Frame ? (Frame) parent : (Frame) SwingUtilities.getWindowAncestor(parent);
            SoftwareGuideDialog dlg = new SoftwareGuideDialog(owner);
            dlg.setVisible(true);
        });
    }

    public SoftwareGuideDialog(Frame owner) {
        super(owner, "College Certificate Generation System - Quick User Guide", true);
        setSize(820, 670);
        setMinimumSize(new Dimension(780, 620));
        setLocationRelativeTo(owner);
        if (UIUtils.getAppLogoImage() != null) {
            setIconImage(UIUtils.getAppLogoImage());
        }
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(new Color(248, 250, 252));
        root.setBorder(new EmptyBorder(20, 26, 20, 26));

        // 1. Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setOpaque(false);

        ImageIcon logoIcon = UIUtils.getDefaultLogo(58, 58);
        if (logoIcon == null) logoIcon = UIUtils.getActiveCollegeLogo(58, 58);
        JLabel lblLogo = (logoIcon != null) ? new JLabel(logoIcon) : new JLabel("🎓");
        headerPanel.add(lblLogo, BorderLayout.WEST);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);

        JLabel lblTitle = new JLabel("College Certificate Generation System - Quick Guide");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("Everything you need to know to issue official, tamper-proof university certificates in minutes.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        headerText.add(lblTitle);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(lblSub);
        headerPanel.add(headerText, BorderLayout.CENTER);

        root.add(headerPanel, BorderLayout.NORTH);

        // 2. Feature Grid (3 rows x 2 cols)
        JPanel grid = new JPanel(new GridLayout(3, 2, 14, 14));
        grid.setOpaque(false);

        grid.add(createFeatureCard(
                "🏢  College & Department Management",
                "• Configure institution name, university affiliation, and custom logo.\n" +
                "• Manage departments: BCA, MCA, MBA, BBA, BA, MA, etc.\n" +
                "• Set designated HODs / In-charges for automated digital signatures."
        ));

        grid.add(createFeatureCard(
                "👥  Student Directory & Bulk Import",
                "• Store student enrollment with College Roll No & University Reg No.\n" +
                "• Bulk import entire batches (100+ students) from CSV or Attendance sheets.\n" +
                "• Permanent deletion protection: deleted records stay deleted."
        ));

        grid.add(createFeatureCard(
                "🎨  Multi-Category Certificate Studio",
                "• 🎓 Academic Excellence: Classic Blue & Gold with official seal.\n" +
                "• 🎭 Cultural & Youth Fests: Royal purple & fuchsia artistic theme.\n" +
                "• 🏅 Sports Merit: Athletic victory medals & speed track chevrons.\n" +
                "• ⚡ Tech Fest: Slate & cyan cyber theme with hologram badge."
        ));

        grid.add(createFeatureCard(
                "📱  Tamper-Proof Scannable QR Codes",
                "• Every certificate embeds a real, scannable QR verification code.\n" +
                "• Scan with any smartphone camera / Google Lens for instant verification.\n" +
                "• Displays student name, roll, reg no, course, and verification status."
        ));

        grid.add(createFeatureCard(
                "🖨️  Single & Batch PDF / PNG Export",
                "• Export high-resolution, print-ready vector PDF certificates.\n" +
                "• Pinned bottom footer: HOD & Principal signatures never overlap.\n" +
                "• Real-time WYSIWYG zoomable preview with college watermarks."
        ));

        grid.add(createFeatureCard(
                "🔒  100% Offline & Private Security",
                "• Embedded SQLite database stored locally in ~/.college_cert_gen.\n" +
                "• Zero cloud dependency, zero subscription costs, no internet required.\n" +
                "• Complete data ownership and local credential protection."
        ));

        root.add(grid, BorderLayout.CENTER);

        // 3. Bottom Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));

        chkDontShow = new JCheckBox("Don't show this guide on startup", true);
        chkDontShow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkDontShow.setForeground(new Color(100, 116, 139));
        chkDontShow.setOpaque(false);
        chkDontShow.setFocusPainted(false);
        footer.add(chkDontShow, BorderLayout.WEST);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnBox.setOpaque(false);

        JButton btnStart = UIUtils.createPrimaryButton("🚀 Got It, Let's Get Started!");
        btnStart.setPreferredSize(new Dimension(230, 38));
        btnStart.addActionListener(e -> {
            if (chkDontShow.isSelected()) {
                DatabaseService.getInstance().setSetting("software_guide_dismissed", "true");
            }
            dispose();
        });
        btnBox.add(btnStart);
        footer.add(btnBox, BorderLayout.EAST);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createFeatureCard(String title, String bulletPoints) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblCardTitle = new JLabel(title);
        lblCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCardTitle.setForeground(new Color(30, 41, 59));
        card.add(lblCardTitle, BorderLayout.NORTH);

        JTextArea txt = new JTextArea(bulletPoints);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txt.setForeground(new Color(71, 85, 105));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setEditable(false);
        txt.setOpaque(false);
        txt.setFocusable(false);
        txt.setBorder(null);

        card.add(txt, BorderLayout.CENTER);
        return card;
    }
}

