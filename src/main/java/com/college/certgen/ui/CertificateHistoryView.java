package com.college.certgen.ui;

import com.college.certgen.database.DatabaseService;
import com.college.certgen.models.Certificate;
import com.college.certgen.services.PdfExportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CertificateHistoryView extends JPanel {
    private final MainFrame mainFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> comboTemplateFilter;
    private List<Certificate> certList = new ArrayList<>();

    public CertificateHistoryView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIUtils.BACKGROUND);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Certificate Issuance Ledger");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.TEXT_DARK);
        JLabel subtitle = new JLabel("Complete audit trail of all generated degrees, credentials, and academic honors");
        subtitle.setFont(UIUtils.FONT_REGULAR);
        subtitle.setForeground(UIUtils.TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(subtitle);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsBox.setOpaque(false);

        JButton btnNew = UIUtils.createPrimaryButton("+ Issue New Certificate");
        btnNew.addActionListener(e -> mainFrame.showPage("cert_gen"));
        actionsBox.add(btnNew);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(actionsBox, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Card
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(UIUtils.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Filters
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UIUtils.FONT_BOLD);
        txtSearch = new JTextField(16);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cert ID, Student, Roll No...");
        txtSearch.addCaretListener(e -> filterData());

        JLabel lblTheme = new JLabel("Template:");
        lblTheme.setFont(UIUtils.FONT_BOLD);
        comboTemplateFilter = new JComboBox<>(new String[]{
                "All Categories",
                "Academic Certificate",
                "Cultural Program Certificate",
                "Sports Certificate",
                "Tech Fest Certificate"
        });
        comboTemplateFilter.addActionListener(e -> filterData());

        leftFilters.add(lblSearch);
        leftFilters.add(txtSearch);
        leftFilters.add(lblTheme);
        leftFilters.add(comboTemplateFilter);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JButton btnPreview = UIUtils.createPrimaryButton("View Certificate");
        btnPreview.addActionListener(e -> previewSelected());

        JButton btnPdf = UIUtils.createSecondaryButton("Download PDF");
        btnPdf.addActionListener(e -> downloadPdfSelected());

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        rightControls.add(btnPreview);
        rightControls.add(btnPdf);
        rightControls.add(btnRefresh);

        searchBar.add(leftFilters, BorderLayout.WEST);
        searchBar.add(rightControls, BorderLayout.EAST);
        card.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Certificate No", "Student Name", "Roll No", "Department", "Class", "Award / Title", "Theme", "Issue Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.setRowHeight(36);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void refreshData() {
        certList = DatabaseService.getInstance().getAllCertificates();
        filterData();
    }

    private void filterData() {
        String query = txtSearch.getText().trim().toLowerCase();
        String selectedTheme = (String) comboTemplateFilter.getSelectedItem();
        boolean filterTheme = selectedTheme != null && !selectedTheme.startsWith("All");

        tableModel.setRowCount(0);
        for (Certificate c : certList) {
            boolean matchesSearch = query.isEmpty() ||
                    c.getCertificateNo().toLowerCase().contains(query) ||
                    c.getStudentName().toLowerCase().contains(query) ||
                    c.getRollNo().toLowerCase().contains(query) ||
                    c.getCertificateTitle().toLowerCase().contains(query);

            boolean matchesTheme = !filterTheme || c.getTemplateName().equalsIgnoreCase(selectedTheme);

            if (matchesSearch && matchesTheme) {
                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getCertificateNo(),
                        c.getStudentName(),
                        c.getRollNo(),
                        c.getDepartment(),
                        c.getClassName(),
                        c.getCertificateTitle(),
                        c.getTemplateName(),
                        c.getIssueDate()
                });
            }
        }
    }

    private Certificate getSelectedCert() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a certificate from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        return certList.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    private void previewSelected() {
        Certificate cert = getSelectedCert();
        if (cert == null) return;

        JDialog dlg = new JDialog(mainFrame, "Certificate Preview: " + cert.getCertificateNo(), true);
        dlg.setSize(920, 680);
        dlg.setLocationRelativeTo(mainFrame);
        dlg.setLayout(new BorderLayout());

        CertificatePreviewPanel panel = new CertificatePreviewPanel();
        panel.setCertificate(cert);
        panel.setZoomFactor(0.9);

        JScrollPane sp = new JScrollPane(panel);
        dlg.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(UIUtils.BACKGROUND);

        JButton btnClose = UIUtils.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());

        JButton btnPdf = UIUtils.createPrimaryButton("Save as PDF");
        btnPdf.addActionListener(e -> {
            dlg.dispose();
            downloadPdfForCert(cert);
        });

        bottom.add(btnClose);
        bottom.add(btnPdf);
        dlg.add(bottom, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void downloadPdfSelected() {
        Certificate cert = getSelectedCert();
        if (cert != null) {
            downloadPdfForCert(cert);
        }
    }

    private void downloadPdfForCert(Certificate cert) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(String.format("Certificate_%s_%s.pdf", cert.getRollNo(), cert.getCertificateNo())));
        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = fc.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".pdf")) {
                target = new File(target.getAbsolutePath() + ".pdf");
            }
            if (PdfExportService.exportCertificate(cert, target)) {
                int open = JOptionPane.showConfirmDialog(
                        this,
                        "Certificate PDF successfully saved!\nFile: " + target.getAbsolutePath() + "\n\nDo you want to open it now?",
                        "Success",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                );
                if (open == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().open(target);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export PDF file.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

