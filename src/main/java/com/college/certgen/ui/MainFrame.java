package com.college.certgen.ui;

import com.college.certgen.models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final User currentUser;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    private DashboardView dashboardView;
    private DepartmentView departmentView;
    private ClassView classView;
    private StudentView studentView;
    private CertificateGeneratorView certGenView;
    private CertificateHistoryView certHistoryView;
    private DatabaseSettingsView dbSettingsView;

    private final Map<String, JButton> navButtons = new HashMap<>();
    private String activePage = "dashboard";

    public MainFrame(User user) {
        this.currentUser = user;

        setTitle(UIUtils.getActiveCollegeName() + " - Certificate Generation Suite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 840);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
        if (UIUtils.getAppLogoImage() != null) {
            setIconImage(UIUtils.getAppLogoImage());
        }

        // Main layout: Sidebar on WEST, CardLayout on CENTER
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BACKGROUND);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // Sidebar
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // Views
        initViews();
        root.add(contentPanel, BorderLayout.CENTER);

        setContentPane(root);
        showPage("dashboard");

        // Automatically show 1-page Software Quickstart Guide for new users on install
        SwingUtilities.invokeLater(() -> {
            SoftwareGuideDialog.showGuide(this, false);
        });
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public CertificateGeneratorView getCertGenView() {
        return certGenView;
    }

    private void initViews() {
        dashboardView = new DashboardView(this);
        departmentView = new DepartmentView(this);
        classView = new ClassView(this);
        studentView = new StudentView(this);
        certGenView = new CertificateGeneratorView(this);
        certHistoryView = new CertificateHistoryView(this);
        dbSettingsView = new DatabaseSettingsView(this);

        contentPanel.add(dashboardView, "dashboard");
        contentPanel.add(departmentView, "departments");
        contentPanel.add(classView, "classes");
        contentPanel.add(studentView, "students");
        contentPanel.add(certGenView, "cert_gen");
        contentPanel.add(certHistoryView, "cert_history");
        contentPanel.add(dbSettingsView, "db_settings");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(UIUtils.SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(20, 16, 20, 16));

        // Top Brand Header
        JPanel brandPanel = new JPanel(new BorderLayout(12, 0));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(0, 4, 24, 4));

        ImageIcon logoIcon = UIUtils.getActiveCollegeLogo(44, 44);
        if (logoIcon == null) logoIcon = UIUtils.getDefaultLogo(44, 44);
        JLabel iconLabel;
        if (logoIcon != null) {
            iconLabel = new JLabel(logoIcon);
        } else {
            iconLabel = new JLabel(UIUtils.getNavIcon("cert", false));
        }

        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 2));
        brandText.setOpaque(false);
        String fullCollegeName = UIUtils.getActiveCollegeName();
        String shortName = fullCollegeName;
        if (shortName.contains(",")) shortName = shortName.substring(0, shortName.indexOf(',')).trim();
        JLabel brandTitle = new JLabel(shortName);
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel(fullCollegeName.length() > 24 ? fullCollegeName.substring(0, 24) + "..." : fullCollegeName);
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        brandSub.setForeground(new Color(148, 163, 184));

        brandText.add(brandTitle);
        brandText.add(brandSub);
        brandPanel.add(iconLabel, BorderLayout.WEST);
        brandPanel.add(brandText, BorderLayout.CENTER);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Center Nav Items
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);

        addNavItem(navList, "dashboard", "Dashboard");
        addNavItem(navList, "departments", "Departments");
        addNavItem(navList, "classes", "Classes & Semesters");
        addNavItem(navList, "students", "Students Directory");
        addNavItem(navList, "cert_gen", "Certificate Studio");
        addNavItem(navList, "cert_history", "Issued Records");
        addNavItem(navList, "db_settings", "Database Settings");

        // Software Quickstart Guide Button
        JButton btnGuide = new JButton("Software Guide");
        btnGuide.setIcon(UIUtils.getNavIcon("guide", false));
        btnGuide.setIconTextGap(12);
        btnGuide.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGuide.setForeground(new Color(199, 210, 254));
        btnGuide.setContentAreaFilled(false);
        btnGuide.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        btnGuide.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuide.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGuide.addActionListener(e -> SoftwareGuideDialog.showGuide(MainFrame.this, true));
        navList.add(Box.createVerticalStrut(8));
        navList.add(btnGuide);

        sidebar.add(navList, BorderLayout.CENTER);

        // Bottom User Profile & Logout
        JPanel userBottom = new JPanel(new BorderLayout(10, 10));
        userBottom.setOpaque(false);
        userBottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(51, 65, 85)),
                new EmptyBorder(16, 8, 0, 8)
        ));

        JPanel userInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        userInfo.setOpaque(false);
        JLabel lblUserName = new JLabel(currentUser != null ? currentUser.getFullName() : "Administrator");
        lblUserName.setFont(UIUtils.FONT_BOLD);
        lblUserName.setForeground(Color.WHITE);

        JLabel lblUserRole = new JLabel(currentUser != null ? currentUser.getRole() : "System Admin");
        lblUserRole.setFont(UIUtils.FONT_SMALL);
        lblUserRole.setForeground(new Color(148, 163, 184));

        userInfo.add(lblUserName);
        userInfo.add(lblUserRole);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setIcon(UIUtils.getNavIcon("logout", false));
        btnLogout.setIconTextGap(8);
        btnLogout.setFont(UIUtils.FONT_BOLD);
        btnLogout.setForeground(new Color(248, 113, 113));
        btnLogout.setBackground(new Color(30, 41, 59));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btnLogout.addActionListener(e -> logout());

        userBottom.add(userInfo, BorderLayout.CENTER);
        userBottom.add(btnLogout, BorderLayout.SOUTH);

        sidebar.add(userBottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavItem(JPanel container, String pageKey, String label) {
        JButton btn = new JButton(label);
        btn.setIcon(UIUtils.getNavIcon(pageKey, false));
        btn.setIconTextGap(12);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(UIUtils.SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));

        btn.addActionListener(e -> showPage(pageKey));
        navButtons.put(pageKey, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    public void showPage(String pageKey) {
        this.activePage = pageKey;
        cardLayout.show(contentPanel, pageKey);

        // Update active nav styling & icons
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            boolean isSelected = entry.getKey().equals(pageKey);
            b.setIcon(UIUtils.getNavIcon(entry.getKey(), isSelected));
            if (isSelected) {
                b.setBackground(UIUtils.PRIMARY);
                b.setForeground(Color.WHITE);
                b.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                b.setBackground(UIUtils.SIDEBAR_BG);
                b.setForeground(new Color(203, 213, 225));
                b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        }

        // Auto refresh targeted view
        if ("dashboard".equals(pageKey) && dashboardView != null) {
            dashboardView.refreshData();
        } else if ("departments".equals(pageKey) && departmentView != null) {
            departmentView.refreshData();
        } else if ("classes".equals(pageKey) && classView != null) {
            classView.refreshData();
        } else if ("students".equals(pageKey) && studentView != null) {
            studentView.refreshData();
        } else if ("cert_gen".equals(pageKey) && certGenView != null) {
            certGenView.refreshStudents();
        } else if ("cert_history".equals(pageKey) && certHistoryView != null) {
            certHistoryView.refreshData();
        }
    }

    public void refreshAllViews() {
        if (dashboardView != null) dashboardView.refreshData();
        if (departmentView != null) departmentView.refreshData();
        if (classView != null) classView.refreshData();
        if (studentView != null) studentView.refreshData();
        if (certGenView != null) certGenView.refreshStudents();
        if (certHistoryView != null) certHistoryView.refreshData();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of College Certificate Suite?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
            });
        }
    }
}

