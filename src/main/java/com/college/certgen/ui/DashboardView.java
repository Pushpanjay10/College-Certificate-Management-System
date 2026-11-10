package com.college.certgen.ui;

import com.college.certgen.database.DatabaseService;
import com.college.certgen.models.Certificate;
import com.college.certgen.models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardView extends JPanel {
    private final MainFrame mainFrame;
    private JLabel lblStudentsCount;
    private JLabel lblDeptsCount;
    private JLabel lblClassesCount;
    private JLabel lblCertsCount;
    private JLabel lblDbStatus;
    private JTable recentCertsTable;
    private DefaultTableModel certsTableModel;

    public DashboardView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIUtils.BACKGROUND);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel title = new JLabel(UIUtils.getActiveCollegeName() + " - Overview");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.TEXT_DARK);
        JLabel subtitle = new JLabel("Academic Session 2026 • Real-time Certificate Records & Analytics");
        subtitle.setFont(UIUtils.FONT_REGULAR);
        subtitle.setForeground(UIUtils.TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(subtitle);

        // Action buttons
        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsBox.setOpaque(false);

        JButton btnNewCert = UIUtils.createPrimaryButton("+ Generate Certificate");
        btnNewCert.addActionListener(e -> mainFrame.showPage("cert_gen"));

        JButton btnAddStudent = UIUtils.createSecondaryButton("+ Add Student");
        btnAddStudent.addActionListener(e -> mainFrame.showPage("students"));

        JButton btnDbSettings = UIUtils.createSecondaryButton("DB Setup");
        btnDbSettings.addActionListener(e -> mainFrame.showPage("db_settings"));

        actionsBox.add(btnAddStudent);
        actionsBox.add(btnDbSettings);
        actionsBox.add(btnNewCert);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(actionsBox, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        // KPI Stat Cards Grid (1 row, 4 cards)
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        statsGrid.setOpaque(false);

        lblStudentsCount = new JLabel("0");
        lblDeptsCount = new JLabel("0");
        lblClassesCount = new JLabel("0");
        lblCertsCount = new JLabel("0");

        statsGrid.add(createKpiCard("Total Enrolled Students", lblStudentsCount, "student", new Color(59, 130, 246)));
        statsGrid.add(createKpiCard("Academic Departments", lblDeptsCount, "dept", new Color(16, 185, 129)));
        statsGrid.add(createKpiCard("Active Classes / Courses", lblClassesCount, "class", new Color(245, 158, 11)));
        statsGrid.add(createKpiCard("Certificates Issued", lblCertsCount, "cert", new Color(139, 92, 246)));

        centerPanel.add(statsGrid, BorderLayout.NORTH);

        // Split Section: Recent Certificates & Database Info
        JPanel bottomSplit = new JPanel(new BorderLayout(20, 0));
        bottomSplit.setOpaque(false);

        // Recent Certificates Card
        JPanel certsCard = UIUtils.createCard("Recent Generated Certificates");
        String[] cols = {"Cert No", "Student Name", "Roll No", "Course", "Issue Date"};
        certsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recentCertsTable = new JTable(certsTableModel);
        UIUtils.styleTable(recentCertsTable);
        JScrollPane scrollPane = new JScrollPane(recentCertsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        certsCard.add(scrollPane, BorderLayout.CENTER);

        bottomSplit.add(certsCard, BorderLayout.CENTER);

        // Right side: System & DB Status Card
        JPanel sysCard = UIUtils.createCard("System & Database Status");
        sysCard.setPreferredSize(new Dimension(320, 0));
        JPanel sysContent = new JPanel();
        sysContent.setLayout(new BoxLayout(sysContent, BoxLayout.Y_AXIS));
        sysContent.setOpaque(false);
        sysContent.setBorder(new EmptyBorder(10, 0, 10, 0));

        lblDbStatus = new JLabel("Engine: SQLite Embedded (Active)");
        lblDbStatus.setFont(UIUtils.FONT_BOLD);
        lblDbStatus.setForeground(UIUtils.SUCCESS);

        User currentUser = mainFrame.getCurrentUser();
        JLabel lblUser = new JLabel("Logged in as: " + (currentUser != null ? currentUser.getFullName() : "Admin"));
        lblUser.setFont(UIUtils.FONT_REGULAR);

        JLabel lblRole = new JLabel("Access Level: " + (currentUser != null ? currentUser.getRole() : "Administrator"));
        lblRole.setFont(UIUtils.FONT_REGULAR);

        JLabel lblHost = new JLabel("Local Storage: Ready");
        lblHost.setFont(UIUtils.FONT_REGULAR);
        lblHost.setForeground(UIUtils.TEXT_MUTED);

        JLabel lblSync = new JLabel("Supported DBs: SQLite, MySQL, PG, Mongo, Oracle, Firebase");
        lblSync.setFont(UIUtils.FONT_SMALL);
        lblSync.setForeground(UIUtils.TEXT_MUTED);

        sysContent.add(lblDbStatus);
        sysContent.add(Box.createVerticalStrut(12));
        sysContent.add(lblUser);
        sysContent.add(Box.createVerticalStrut(8));
        sysContent.add(lblRole);
        sysContent.add(Box.createVerticalStrut(8));
        sysContent.add(lblHost);
        sysContent.add(Box.createVerticalStrut(16));
        sysContent.add(new JSeparator());
        sysContent.add(Box.createVerticalStrut(16));
        sysContent.add(lblSync);
        sysContent.add(Box.createVerticalStrut(20));

        JButton btnManageDb = UIUtils.createPrimaryButton("Switch / Add Database");
        btnManageDb.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnManageDb.addActionListener(e -> mainFrame.showPage("db_settings"));
        sysContent.add(btnManageDb);

        sysCard.add(sysContent, BorderLayout.CENTER);
        bottomSplit.add(sysCard, BorderLayout.EAST);

        centerPanel.add(bottomSplit, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel countLabel, String iconKey, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(UIUtils.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Icon Box using VectorIcon (Guaranteed crisp rendering on all platforms without font issues)
        JLabel lblIcon = new JLabel("", SwingConstants.CENTER);
        lblIcon.setIcon(UIUtils.getKpiIcon(iconKey, accentColor, 28));
        lblIcon.setOpaque(true);
        lblIcon.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 25));
        lblIcon.setPreferredSize(new Dimension(54, 54));
        lblIcon.setBorder(BorderFactory.createLineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60), 1, true));

        // Text Box
        JPanel textBox = new JPanel(new GridLayout(2, 1, 0, 2));
        textBox.setOpaque(false);

        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        countLabel.setForeground(UIUtils.TEXT_DARK);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIUtils.FONT_REGULAR);
        lblTitle.setForeground(UIUtils.TEXT_MUTED);

        textBox.add(countLabel);
        textBox.add(lblTitle);

        card.add(lblIcon, BorderLayout.WEST);
        card.add(textBox, BorderLayout.CENTER);

        return card;
    }

    public void refreshData() {
        DatabaseService db = DatabaseService.getInstance();
        lblStudentsCount.setText(String.valueOf(db.getCount("students")));
        lblDeptsCount.setText(String.valueOf(db.getCount("departments")));
        lblClassesCount.setText(String.valueOf(db.getCount("classes")));
        lblCertsCount.setText(String.valueOf(db.getCount("certificates")));

        lblDbStatus.setText("Engine: " + db.getCurrentConfig().getType().getDisplayName());

        certsTableModel.setRowCount(0);
        List<Certificate> certs = db.getAllCertificates();
        for (Certificate c : certs) {
            certsTableModel.addRow(new Object[]{
                    c.getCertificateNo(),
                    c.getStudentName(),
                    c.getRollNo(),
                    c.getClassName(),
                    c.getIssueDate()
            });
        }
    }
}
