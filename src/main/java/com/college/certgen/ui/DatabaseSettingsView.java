package com.college.certgen.ui;

import com.college.certgen.database.DatabaseService;
import com.college.certgen.models.DatabaseConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class DatabaseSettingsView extends JPanel {
    private final MainFrame mainFrame;

    private JComboBox<DatabaseConfig.DBType> comboDbType;
    private JLabel lblActiveStatus;

    // Card layout container for type-specific fields
    private CardLayout cardLayout;
    private JPanel dynamicCardPanel;

    // SQLite
    private JTextField txtSqlitePath;

    // Relational (MySQL / Postgres)
    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField txtDbName;
    private JTextField txtUser;
    private JPasswordField txtPass;

    // MongoDB
    private JTextField txtMongoUri;
    private JTextField txtMongoDb;

    // Oracle
    private JTextField txtOracleHost;
    private JTextField txtOraclePort;
    private JTextField txtOracleSid;
    private JTextField txtOracleUser;
    private JPasswordField txtOraclePass;

    // Firebase
    private JTextField txtFirebaseProjectId;
    private JTextField txtFirebaseApiKey;
    private JTextField txtFirebaseDbUrl;

    public DatabaseSettingsView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIUtils.BACKGROUND);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadCurrentConfig();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Database Engine & Connection Settings");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.TEXT_DARK);
        JLabel subtitle = new JLabel("Connect to your custom database: SQLite, MySQL, PostgreSQL, MongoDB, Oracle, or Firebase");
        subtitle.setFont(UIUtils.FONT_REGULAR);
        subtitle.setForeground(UIUtils.TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(subtitle);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsBox.setOpaque(false);

        JButton btnTest = UIUtils.createSecondaryButton("⚡ Test Connection");
        btnTest.addActionListener(e -> testActiveConfiguration());

        JButton btnSave = UIUtils.createPrimaryButton("💾 Apply & Switch Database");
        btnSave.addActionListener(e -> applyConfiguration());

        actionsBox.add(btnTest);
        actionsBox.add(btnSave);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(actionsBox, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content Card
        JPanel centerCard = new JPanel(new BorderLayout(20, 20));
        centerCard.setBackground(UIUtils.CARD_BG);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));

        // Top Status Bar inside card
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        lblActiveStatus = new JLabel("Active Database: Loading...");
        lblActiveStatus.setFont(UIUtils.FONT_BOLD);
        lblActiveStatus.setForeground(UIUtils.PRIMARY);

        JButton btnResetSqlite = UIUtils.createSecondaryButton("↺ Reset to Default SQLite");
        btnResetSqlite.addActionListener(e -> resetToDefaultSqlite());

        statusBar.add(lblActiveStatus, BorderLayout.WEST);
        statusBar.add(btnResetSqlite, BorderLayout.EAST);
        centerCard.add(statusBar, BorderLayout.NORTH);

        // Selection & Dynamic Form
        JPanel formContainer = new JPanel(new BorderLayout(15, 15));
        formContainer.setOpaque(false);

        // Engine Selector
        JPanel selectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        selectorRow.setOpaque(false);

        JLabel lblSelect = new JLabel("Select Database Engine:");
        lblSelect.setFont(UIUtils.FONT_BOLD);
        lblSelect.setForeground(UIUtils.TEXT_DARK);

        comboDbType = new JComboBox<>(DatabaseConfig.DBType.values());
        comboDbType.setFont(UIUtils.FONT_BOLD);
        comboDbType.setPreferredSize(new Dimension(280, 36));
        comboDbType.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DatabaseConfig.DBType) {
                    l.setText(((DatabaseConfig.DBType) value).getDisplayName());
                }
                return l;
            }
        });
        comboDbType.addActionListener(e -> switchCardView());

        selectorRow.add(lblSelect);
        selectorRow.add(comboDbType);
        formContainer.add(selectorRow, BorderLayout.NORTH);

        // Dynamic Cards for each DB Type
        cardLayout = new CardLayout();
        dynamicCardPanel = new JPanel(cardLayout);
        dynamicCardPanel.setOpaque(false);

        dynamicCardPanel.add(createSqlitePanel(), DatabaseConfig.DBType.SQLITE.name());
        dynamicCardPanel.add(createRelationalPanel("MySQL"), DatabaseConfig.DBType.MYSQL.name());
        dynamicCardPanel.add(createRelationalPanel("PostgreSQL"), DatabaseConfig.DBType.POSTGRESQL.name());
        dynamicCardPanel.add(createMongoPanel(), DatabaseConfig.DBType.MONGODB.name());
        dynamicCardPanel.add(createOraclePanel(), DatabaseConfig.DBType.ORACLE.name());
        dynamicCardPanel.add(createFirebasePanel(), DatabaseConfig.DBType.FIREBASE.name());

        formContainer.add(dynamicCardPanel, BorderLayout.CENTER);
        centerCard.add(formContainer, BorderLayout.CENTER);

        add(centerCard, BorderLayout.CENTER);
    }

    private JPanel createSqlitePanel() {
        JPanel p = new JPanel(new GridLayout(3, 1, 10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel info = new JLabel("<html><b>SQLite Embedded Database</b> requires zero configuration. The database is automatically created and stored on your local disk.<br>Ideal for standalone offline operation on both macOS and Windows.</html>");
        info.setFont(UIUtils.FONT_REGULAR);
        info.setForeground(UIUtils.TEXT_MUTED);

        String defaultPath = System.getProperty("user.home") + File.separator + ".college_cert_gen" + File.separator + "college_cert.db";
        txtSqlitePath = new JTextField(defaultPath);
        txtSqlitePath.setEditable(false);
        txtSqlitePath.setPreferredSize(new Dimension(500, 34));

        JPanel pathRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pathRow.setOpaque(false);
        pathRow.add(new JLabel("Local File Location:"));
        pathRow.add(txtSqlitePath);

        p.add(info);
        p.add(pathRow);
        return p;
    }

    private JPanel createRelationalPanel(String dbName) {
        JPanel p = new JPanel(new GridLayout(6, 2, 12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));

        txtHost = new JTextField("localhost");
        txtPort = new JTextField(dbName.equals("MySQL") ? "3306" : "5432");
        txtDbName = new JTextField("college_cert_db");
        txtUser = new JTextField("root");
        txtPass = new JPasswordField("");

        p.add(new JLabel("Server Host / IP:"));
        p.add(txtHost);
        p.add(new JLabel("Port:"));
        p.add(txtPort);
        p.add(new JLabel("Database Name:"));
        p.add(txtDbName);
        p.add(new JLabel("Username:"));
        p.add(txtUser);
        p.add(new JLabel("Password:"));
        p.add(txtPass);

        return p;
    }

    private JPanel createMongoPanel() {
        JPanel p = new JPanel(new GridLayout(4, 2, 12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));

        txtMongoUri = new JTextField("mongodb://localhost:27017");
        txtMongoDb = new JTextField("college_cert_db");

        JLabel info = new JLabel("<html>Connect to MongoDB Atlas cloud cluster or local mongod instance.<br>Supports standard URI syntax: <code>mongodb://user:pass@host:port</code> or <code>mongodb+srv://...</code></html>");
        info.setFont(UIUtils.FONT_SMALL);
        info.setForeground(UIUtils.TEXT_MUTED);

        p.add(new JLabel("Connection URI / Atlas URL:"));
        p.add(txtMongoUri);
        p.add(new JLabel("Database Name:"));
        p.add(txtMongoDb);
        p.add(new JLabel(""));
        p.add(info);

        return p;
    }

    private JPanel createOraclePanel() {
        JPanel p = new JPanel(new GridLayout(6, 2, 12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));

        txtOracleHost = new JTextField("localhost");
        txtOraclePort = new JTextField("1521");
        txtOracleSid = new JTextField("ORCL");
        txtOracleUser = new JTextField("c##college");
        txtOraclePass = new JPasswordField("");

        p.add(new JLabel("Oracle Host:"));
        p.add(txtOracleHost);
        p.add(new JLabel("Oracle Port:"));
        p.add(txtOraclePort);
        p.add(new JLabel("SID / Service Name:"));
        p.add(txtOracleSid);
        p.add(new JLabel("Username / Schema:"));
        p.add(txtOracleUser);
        p.add(new JLabel("Password:"));
        p.add(txtOraclePass);

        return p;
    }

    private JPanel createFirebasePanel() {
        JPanel p = new JPanel(new GridLayout(5, 2, 12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));

        txtFirebaseProjectId = new JTextField("my-college-project-2026");
        txtFirebaseApiKey = new JTextField("AIzaSy...");
        txtFirebaseDbUrl = new JTextField("https://my-college-project-2026.firebaseio.com");

        JLabel info = new JLabel("<html>Connect to Google Firebase (Cloud Firestore / Realtime DB).<br>Certificate records and student directories sync directly with Firebase Cloud.</html>");
        info.setFont(UIUtils.FONT_SMALL);
        info.setForeground(UIUtils.TEXT_MUTED);

        p.add(new JLabel("Firebase Project ID:"));
        p.add(txtFirebaseProjectId);
        p.add(new JLabel("Web API Key / Secret:"));
        p.add(txtFirebaseApiKey);
        p.add(new JLabel("Database URL:"));
        p.add(txtFirebaseDbUrl);
        p.add(new JLabel(""));
        p.add(info);

        return p;
    }

    private void switchCardView() {
        DatabaseConfig.DBType selected = (DatabaseConfig.DBType) comboDbType.getSelectedItem();
        if (selected != null) {
            cardLayout.show(dynamicCardPanel, selected.name());
        }
    }

    private void loadCurrentConfig() {
        DatabaseConfig cfg = DatabaseService.getInstance().getCurrentConfig();
        comboDbType.setSelectedItem(cfg.getType());
        switchCardView();
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        DatabaseConfig cfg = DatabaseService.getInstance().getCurrentConfig();
        lblActiveStatus.setText("Active Engine: " + cfg.getType().getDisplayName() + "  |  Status: Connected ✓");
    }

    private DatabaseConfig buildConfigFromInputs() {
        DatabaseConfig cfg = new DatabaseConfig();
        DatabaseConfig.DBType type = (DatabaseConfig.DBType) comboDbType.getSelectedItem();
        cfg.setType(type);

        if (type == DatabaseConfig.DBType.MYSQL || type == DatabaseConfig.DBType.POSTGRESQL) {
            cfg.setHost(txtHost.getText().trim());
            try {
                cfg.setPort(Integer.parseInt(txtPort.getText().trim()));
            } catch (Exception ignored) {
                cfg.setPort(type == DatabaseConfig.DBType.MYSQL ? 3306 : 5432);
            }
            cfg.setDatabaseName(txtDbName.getText().trim());
            cfg.setUsername(txtUser.getText().trim());
            cfg.setPassword(new String(txtPass.getPassword()));
        } else if (type == DatabaseConfig.DBType.MONGODB) {
            cfg.setConnectionUri(txtMongoUri.getText().trim());
            cfg.setDatabaseName(txtMongoDb.getText().trim());
        } else if (type == DatabaseConfig.DBType.ORACLE) {
            cfg.setHost(txtOracleHost.getText().trim());
            try {
                cfg.setPort(Integer.parseInt(txtOraclePort.getText().trim()));
            } catch (Exception ignored) {
                cfg.setPort(1521);
            }
            cfg.setOracleSid(txtOracleSid.getText().trim());
            cfg.setUsername(txtOracleUser.getText().trim());
            cfg.setPassword(new String(txtOraclePass.getPassword()));
        } else if (type == DatabaseConfig.DBType.FIREBASE) {
            cfg.setFirebaseProjectId(txtFirebaseProjectId.getText().trim());
            cfg.setFirebaseApiKey(txtFirebaseApiKey.getText().trim());
            cfg.setFirebaseDatabaseUrl(txtFirebaseDbUrl.getText().trim());
        }

        return cfg;
    }

    private void testActiveConfiguration() {
        DatabaseConfig cfg = buildConfigFromInputs();
        StringBuilder errorMsg = new StringBuilder();
        boolean ok = DatabaseService.getInstance().testConnection(cfg, errorMsg);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Connection Test Succeeded!\nSuccessfully verified connectivity to " + cfg.getType().getDisplayName() + ".",
                    "Connection Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Connection Test Failed:\n" + errorMsg.toString(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyConfiguration() {
        DatabaseConfig cfg = buildConfigFromInputs();
        StringBuilder errorMsg = new StringBuilder();
        boolean ok = DatabaseService.getInstance().switchDatabase(cfg, errorMsg);
        if (ok) {
            updateStatusLabel();
            JOptionPane.showMessageDialog(this,
                    "Database switched successfully to: " + cfg.getType().getDisplayName(),
                    "Database Updated",
                    JOptionPane.INFORMATION_MESSAGE);
            mainFrame.refreshAllViews();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not switch database:\n" + errorMsg.toString(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetToDefaultSqlite() {
        DatabaseConfig cfg = new DatabaseConfig();
        cfg.setType(DatabaseConfig.DBType.SQLITE);
        StringBuilder errorMsg = new StringBuilder();
        DatabaseService.getInstance().switchDatabase(cfg, errorMsg);
        comboDbType.setSelectedItem(DatabaseConfig.DBType.SQLITE);
        switchCardView();
        updateStatusLabel();
        JOptionPane.showMessageDialog(this, "Reset to local embedded SQLite database successfully.");
        mainFrame.refreshAllViews();
    }
}

