package com.college.certgen.ui;

import com.college.certgen.database.DatabaseService;
import com.college.certgen.models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.prefs.Preferences;

public class LoginFrame extends JFrame {
    private JComboBox<AccountItem> comboAccounts;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkRemember;
    private JLabel lblError;
    private JLabel lblCollegeTag;
    private JButton btnLogin;
    private boolean isPopulating = false;

    public static class AccountItem {
        private final User user;

        public AccountItem(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        @Override
        public String toString() {
            if (user == null) {
                return "+ Create / Register New Account";
            }
            String col = (user.getCollegeName() != null && !user.getCollegeName().trim().isEmpty())
                    ? user.getCollegeName().trim()
                    : "Universal Profile";
            if (col.length() > 26) col = col.substring(0, 26) + "...";
            return user.getFullName() + " (@" + user.getUsername() + ") — " + col;
        }
    }

    public LoginFrame() {
        setTitle("College Certificate Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 640);
        setResizable(false);
        setLocationRelativeTo(null);
        if (UIUtils.getAppLogoImage() != null) {
            setIconImage(UIUtils.getAppLogoImage());
        }

        initUI();
        loadSavedCredentialsLocally();
    }

    private void initUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(241, 245, 249));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(440, 560));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(22, 28, 22, 28)
        ));

        // 1. Header: App Logo Beside Title (Neutral Software Branding)
        JPanel brandHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        brandHeader.setOpaque(false);
        brandHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        ImageIcon logoIcon = UIUtils.getAppLogo(62, 62);
        JLabel logoLabel = (logoIcon != null) ? new JLabel(logoIcon) : new JLabel("🎓");
        brandHeader.add(logoLabel);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 3));
        headerText.setOpaque(false);

        JLabel titleEn = new JLabel("College Certificate Management System");
        titleEn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleEn.setForeground(UIUtils.PRIMARY);

        JLabel subtitle = new JLabel("Universal Certificate & Credential Portal");
        subtitle.setFont(UIUtils.FONT_SMALL);
        subtitle.setForeground(UIUtils.TEXT_MUTED);

        headerText.add(titleEn);
        headerText.add(subtitle);
        brandHeader.add(headerText);

        card.add(brandHeader);
        card.add(Box.createVerticalStrut(16));

        // 2. Saved Accounts Switcher (Lists all accounts created on this PC)
        JLabel lblAcc = new JLabel("Select Account / Switch User");
        lblAcc.setFont(UIUtils.FONT_BOLD);
        lblAcc.setForeground(UIUtils.TEXT_DARK);
        lblAcc.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblAcc);
        card.add(Box.createVerticalStrut(4));

        comboAccounts = new JComboBox<>();
        comboAccounts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        comboAccounts.setFont(UIUtils.FONT_REGULAR);
        populateAccounts();

        comboAccounts.addActionListener(e -> {
            if (isPopulating) return; // Ignore programmatic updates
            AccountItem item = (AccountItem) comboAccounts.getSelectedItem();
            if (item != null) {
                if (item.getUser() == null) {
                    showRegisterDialog();
                } else {
                    User u = item.getUser();
                    txtUsername.setText(u.getUsername());
                    String savedPass = getSavedPasswordForUser(u.getUsername());
                    if (savedPass != null && !savedPass.isEmpty()) {
                        txtPassword.setText(savedPass);
                        chkRemember.setSelected(true);
                    } else {
                        txtPassword.setText("");
                        chkRemember.setSelected(false);
                    }
                    String col = u.getCollegeName() != null && !u.getCollegeName().trim().isEmpty()
                            ? u.getCollegeName().trim()
                            : "Standard Institution";
                    lblCollegeTag.setText("🏫 Institution: " + col);
                }
            }
        });
        card.add(comboAccounts);

        lblCollegeTag = new JLabel(" ");
        lblCollegeTag.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblCollegeTag.setForeground(UIUtils.PRIMARY);
        lblCollegeTag.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblCollegeTag);

        card.add(Box.createVerticalStrut(10));

        // 3. Username Field
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UIUtils.FONT_BOLD);
        lblUser.setForeground(UIUtils.TEXT_DARK);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblUser);
        card.add(Box.createVerticalStrut(4));

        txtUsername = new JTextField("");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtUsername.setFont(UIUtils.FONT_REGULAR);
        txtUsername.putClientProperty("JTextField.placeholderText", "Enter username");
        card.add(txtUsername);

        card.add(Box.createVerticalStrut(10));

        // 4. Password Field
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UIUtils.FONT_BOLD);
        lblPass.setForeground(UIUtils.TEXT_DARK);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblPass);
        card.add(Box.createVerticalStrut(4));

        txtPassword = new JPasswordField("");
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtPassword.setFont(UIUtils.FONT_REGULAR);
        txtPassword.putClientProperty("JTextField.placeholderText", "Enter password");
        card.add(txtPassword);

        // Enter key triggers login
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterKey);
        txtPassword.addKeyListener(enterKey);

        card.add(Box.createVerticalStrut(8));

        // Remember Checkbox
        chkRemember = new JCheckBox("Save password locally on this system");
        chkRemember.setFont(UIUtils.FONT_SMALL);
        chkRemember.setForeground(UIUtils.TEXT_MUTED);
        chkRemember.setOpaque(false);
        chkRemember.setFocusPainted(false);
        chkRemember.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(chkRemember);

        card.add(Box.createVerticalStrut(4));

        // Error message label
        lblError = new JLabel(" ");
        lblError.setFont(UIUtils.FONT_SMALL);
        lblError.setForeground(UIUtils.DANGER);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblError);

        card.add(Box.createVerticalStrut(6));

        // Login Button
        btnLogin = UIUtils.createPrimaryButton("Sign In to Portal →");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> performLogin());
        card.add(btnLogin);

        card.add(Box.createVerticalStrut(12));

        // Create Account Link
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registerPanel.setOpaque(false);
        registerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNoAcc = new JLabel("Need a new institution?");
        lblNoAcc.setFont(UIUtils.FONT_SMALL);
        lblNoAcc.setForeground(UIUtils.TEXT_MUTED);

        JButton btnCreate = new JButton("+ Create New Account");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCreate.setForeground(UIUtils.PRIMARY);
        btnCreate.setBorderPainted(false);
        btnCreate.setContentAreaFilled(false);
        btnCreate.setFocusPainted(false);
        btnCreate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreate.addActionListener(e -> showRegisterDialog());

        registerPanel.add(lblNoAcc);
        registerPanel.add(btnCreate);
        card.add(registerPanel);

        root.add(card);
        setContentPane(root);

        // Pre-select first account if available
        if (comboAccounts.getItemCount() > 1) {
            comboAccounts.setSelectedIndex(0);
        }
    }

    private void populateAccounts() {
        if (comboAccounts == null) return;
        isPopulating = true;
        try {
            comboAccounts.removeAllItems();
            List<User> list = DatabaseService.getInstance().getAllUsers();
            for (User u : list) {
                comboAccounts.addItem(new AccountItem(u));
            }
            comboAccounts.addItem(new AccountItem(null)); // "+ Add / Register New Account"
        } finally {
            isPopulating = false;
        }
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both username and password.");
            return;
        }

        btnLogin.setEnabled(false);
        lblError.setText("Authenticating...");

        SwingUtilities.invokeLater(() -> {
            User user = DatabaseService.getInstance().authenticate(username, password);
            if (user != null) {
                // Set active user branding so all views reflect this specific college
                UIUtils.setActiveUser(user);

                // Save or clear credentials locally
                if (chkRemember.isSelected()) {
                    saveCredentialsLocally(username, password);
                } else {
                    clearSavedCredentialsLocally(username);
                }

                dispose();
                MainFrame mainFrame = new MainFrame(user);
                mainFrame.setVisible(true);
            } else {
                lblError.setText("Invalid credentials. Please verify and try again.");
                btnLogin.setEnabled(true);
            }
        });
    }

    private void showRegisterDialog() {
        JDialog dlg = new JDialog(this, "Create Account & Setup College Profile", true);
        dlg.setSize(540, 680);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(15, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Header with App Logo
        JPanel topHeader = new JPanel(new BorderLayout(12, 0));
        topHeader.setOpaque(false);

        ImageIcon logoSm = UIUtils.getAppLogo(46, 46);
        JLabel lblLogo = (logoSm != null) ? new JLabel(logoSm) : new JLabel("🎓");
        topHeader.add(lblLogo, BorderLayout.WEST);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 2));
        headerText.setOpaque(false);
        JLabel title = new JLabel("Create Account & Setup College Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(UIUtils.TEXT_DARK);

        JLabel sub = new JLabel("Configure your institution details and credentials");
        sub.setFont(UIUtils.FONT_SMALL);
        sub.setForeground(UIUtils.TEXT_MUTED);

        headerText.add(title);
        headerText.add(sub);
        topHeader.add(headerText, BorderLayout.CENTER);

        root.add(topHeader, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        int row = 0;

        // User Details Header
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel lblSec1 = new JLabel("1. Administrator / User Credentials");
        lblSec1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSec1.setForeground(UIUtils.PRIMARY);
        form.add(lblSec1, gbc);

        // Full Name
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        JTextField txtName = new JTextField();
        form.add(txtName, gbc);
        row++;

        // Username
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("Username (Login ID):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        JTextField txtRegUser = new JTextField();
        form.add(txtRegUser, gbc);
        row++;

        // Role
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("Role / Designation:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        JComboBox<String> comboRole = new JComboBox<>(new String[]{
                "Administrator",
                "Faculty / Department Teacher",
                "Exam Controller",
                "Sports In-Charge",
                "Cultural Convener"
        });
        form.add(comboRole, gbc);
        row++;

        // Password
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        JPasswordField txtRegPass = new JPasswordField();
        form.add(txtRegPass, gbc);
        row++;

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        JPasswordField txtRegConfirm = new JPasswordField();
        form.add(txtRegConfirm, gbc);
        row++;

        // Separator
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        form.add(Box.createVerticalStrut(4), gbc);

        // College Details Header (Starts clean and empty for new users)
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel lblSec2 = new JLabel("2. College & University Information");
        lblSec2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSec2.setForeground(UIUtils.PRIMARY);
        form.add(lblSec2, gbc);

        // College Name (Empty by default so new user enters their college)
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("College / Institute Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        JTextField txtCollegeName = new JTextField();
        txtCollegeName.putClientProperty("JTextField.placeholderText", "e.g. State College of Science & Arts");
        form.add(txtCollegeName, gbc);
        row++;

        // University Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("University / Board:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        JTextField txtUnivName = new JTextField();
        txtUnivName.putClientProperty("JTextField.placeholderText", "e.g. Central State University");
        form.add(txtUnivName, gbc);
        row++;

        // College Logo Picker
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        form.add(new JLabel("College Logo / Emblem:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;

        JPanel logoPickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logoPickPanel.setOpaque(false);

        JLabel lblLogoPreview = new JLabel(UIUtils.getAppLogo(42, 42));
        lblLogoPreview.setPreferredSize(new Dimension(44, 44));
        lblLogoPreview.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));

        JButton btnChooseLogo = UIUtils.createSecondaryButton("📁 Choose Logo...");
        btnChooseLogo.setFont(UIUtils.FONT_SMALL);

        JLabel lblLogoFileName = new JLabel("Default App Logo");
        lblLogoFileName.setFont(UIUtils.FONT_SMALL);
        lblLogoFileName.setForeground(UIUtils.TEXT_MUTED);

        final File[] selectedLogoFile = new File[1];

        btnChooseLogo.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select College Logo Image");
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
            int res = fc.showOpenDialog(dlg);
            if (res == JFileChooser.APPROVE_OPTION) {
                File sel = fc.getSelectedFile();
                try {
                    java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(sel);
                    if (img != null) {
                        selectedLogoFile[0] = sel;
                        lblLogoPreview.setIcon(new ImageIcon(img.getScaledInstance(42, 42, Image.SCALE_SMOOTH)));
                        lblLogoFileName.setText(sel.getName());
                    } else {
                        JOptionPane.showMessageDialog(dlg, "The selected file is not a valid image.", "Invalid File", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Error reading image: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        logoPickPanel.add(lblLogoPreview);
        logoPickPanel.add(btnChooseLogo);
        logoPickPanel.add(lblLogoFileName);
        form.add(logoPickPanel, gbc);
        row++;

        // Local Persistence Checkbox
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JCheckBox chkRegSave = new JCheckBox("Save password locally on this system", true);
        chkRegSave.setFont(UIUtils.FONT_SMALL);
        chkRegSave.setForeground(UIUtils.TEXT_MUTED);
        chkRegSave.setOpaque(false);
        form.add(chkRegSave, gbc);

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        btnPanel.setOpaque(false);

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSubmit = UIUtils.createPrimaryButton("Create & Save Account");
        btnSubmit.addActionListener(e -> {
            String fullName = txtName.getText().trim();
            String regUsername = txtRegUser.getText().trim();
            String role = (String) comboRole.getSelectedItem();
            String pass = new String(txtRegPass.getPassword()).trim();
            String confirm = new String(txtRegConfirm.getPassword()).trim();
            String cName = txtCollegeName.getText().trim();
            String uName = txtUnivName.getText().trim();

            if (fullName.isEmpty() || regUsername.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please fill in all required user fields.", "Incomplete Information", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(dlg, "Passwords do not match. Please re-enter carefully.", "Password Mismatch", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (pass.length() < 3) {
                JOptionPane.showMessageDialog(dlg, "Password must be at least 3 characters long.", "Weak Password", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check if username already exists in local DB
            if (DatabaseService.getInstance().usernameExists(regUsername)) {
                JOptionPane.showMessageDialog(dlg, "Username '" + regUsername + "' is already registered. Please choose a different username.", "Username Taken", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (cName.isEmpty()) {
                cName = "College of " + fullName;
            }
            if (uName.isEmpty()) {
                uName = "State Affiliated University";
            }

            String customLogoPath = "";
            if (selectedLogoFile[0] != null && selectedLogoFile[0].exists()) {
                try {
                    File destDir = new File(System.getProperty("user.home"), ".college_cert_gen");
                    if (!destDir.exists()) destDir.mkdirs();
                    String ext = selectedLogoFile[0].getName().toLowerCase().endsWith(".png") ? ".png" : ".jpg";
                    File destFile = new File(destDir, "logo_" + regUsername + ext);
                    java.nio.file.Files.copy(selectedLogoFile[0].toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    customLogoPath = destFile.getAbsolutePath();
                } catch (Exception ex) {
                    System.err.println("Could not copy chosen logo: " + ex.getMessage());
                }
            }

            // Insert new user into local SQLite database with their specific college profile
            User newUser = new User(regUsername, pass, fullName, role, cName, uName, "CONSTITUENT UNIT OF " + uName.toUpperCase(), customLogoPath);
            if (DatabaseService.getInstance().insertUser(newUser)) {
                // Initialize starter departments and courses for the new college
                DatabaseService.getInstance().seedStarterDepartmentsForCollege(cName);

                // If requested, save credentials locally
                if (chkRegSave.isSelected()) {
                    saveCredentialsLocally(regUsername, pass);
                    chkRemember.setSelected(true);
                }

                // Refresh the accounts dropdown on the main login screen
                populateAccounts();

                // Select this newly created user in the combo
                for (int i = 0; i < comboAccounts.getItemCount(); i++) {
                    AccountItem ai = comboAccounts.getItemAt(i);
                    if (ai != null && ai.getUser() != null && ai.getUser().getUsername().equals(regUsername)) {
                        comboAccounts.setSelectedIndex(i);
                        break;
                    }
                }

                txtUsername.setText(regUsername);
                txtPassword.setText(pass);
                lblError.setText("Account created! Click Sign In to enter.");
                lblError.setForeground(UIUtils.SUCCESS);

                JOptionPane.showMessageDialog(dlg,
                        String.format("Account and College Profile created successfully!\n\nName: %s\nUsername: %s\nCollege: %s\n\nYour institution database is fresh and ready.",
                                fullName, regUsername, cName),
                        "Account Created",
                        JOptionPane.INFORMATION_MESSAGE);

                dlg.dispose();
            } else {
                JOptionPane.showMessageDialog(dlg, "Could not save user account to local database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        root.add(btnPanel, BorderLayout.SOUTH);

        dlg.add(root);
        dlg.setVisible(true);
    }

    private void saveCredentialsLocally(String username, String password) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(LoginFrame.class);
            prefs.put("last_username", username);
            String encodedPass = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
            prefs.put("pass_" + username, encodedPass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSavedPasswordForUser(String username) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(LoginFrame.class);
            String enc = prefs.get("pass_" + username, null);
            if (enc != null && !enc.isEmpty()) {
                return new String(Base64.getDecoder().decode(enc), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clearSavedCredentialsLocally(String username) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(LoginFrame.class);
            prefs.remove("pass_" + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSavedCredentialsLocally() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(LoginFrame.class);
            String lastUser = prefs.get("last_username", null);
            if (lastUser != null && !lastUser.isEmpty()) {
                for (int i = 0; i < comboAccounts.getItemCount(); i++) {
                    AccountItem item = comboAccounts.getItemAt(i);
                    if (item != null && item.getUser() != null && item.getUser().getUsername().equals(lastUser)) {
                        comboAccounts.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
