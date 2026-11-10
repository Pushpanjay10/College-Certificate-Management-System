package com.college.certgen.ui;

import com.college.certgen.database.DatabaseService;
import com.college.certgen.models.Certificate;
import com.college.certgen.models.SchoolClass;
import com.college.certgen.models.Student;
import com.college.certgen.services.PdfExportService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CertificateGeneratorView extends JPanel {
    private final MainFrame mainFrame;
    private CertificatePreviewPanel previewPanel;

    private JComboBox<Student> comboStudent;
    private JTextField txtUniName;
    private JTextField txtCollegeName;
    private JComboBox<String> comboCertTitle;
    private JTextField txtIssueDate;
    private JTextField txtSignatory1;
    private JTextField txtSignatory2;
    private JTextArea txtRemarks;
    private JComboBox<String> comboTemplate;

    public CertificateGeneratorView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(15, 15));
        setBackground(UIUtils.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        refreshStudents();
        updatePreview();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Certificate Studio & Generator");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.TEXT_DARK);
        JLabel subtitle = new JLabel("Craft, customize, preview in real-time, and issue university certificates");
        subtitle.setFont(UIUtils.FONT_REGULAR);
        subtitle.setForeground(UIUtils.TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(subtitle);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsBox.setOpaque(false);

        JButton btnBatch = UIUtils.createSecondaryButton("Batch by Class");
        btnBatch.addActionListener(e -> showBatchGenerationDialog());

        JButton btnIssue = UIUtils.createPrimaryButton("Save & Issue");
        btnIssue.addActionListener(e -> saveAndIssueCertificate());

        JButton btnExportPdf = UIUtils.createSecondaryButton("Export PDF");
        btnExportPdf.addActionListener(e -> exportPdf());

        JButton btnExportPng = UIUtils.createSecondaryButton("Export PNG");
        btnExportPng.addActionListener(e -> exportPng());

        actionsBox.add(btnBatch);
        actionsBox.add(btnIssue);
        actionsBox.add(btnExportPdf);
        actionsBox.add(btnExportPng);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(actionsBox, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Split Layout: Left is Form Controls (360px), Right is Live Interactive Preview
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(390);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);

        // Left Form Card
        JPanel formCard = new JPanel(new BorderLayout(10, 10));
        formCard.setBackground(UIUtils.CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);

        // 1. Student selection
        formFields.add(createFieldLabel("Select Student:"));
        comboStudent = new JComboBox<>();
        comboStudent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        comboStudent.addActionListener(e -> updatePreview());
        formFields.add(comboStudent);
        formFields.add(Box.createVerticalStrut(10));

        // 2. Template Style
        formFields.add(createFieldLabel("Certificate Category / Template:"));
        comboTemplate = new JComboBox<>(new String[]{
                "Academic Certificate",
                "Cultural Program Certificate",
                "Sports Certificate",
                "Tech Fest Certificate"
        });
        comboTemplate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        comboTemplate.addActionListener(e -> {
            String sel = (String) comboTemplate.getSelectedItem();
            if (sel != null) {
                if (sel.contains("Sports")) {
                    comboCertTitle.setSelectedItem("CERTIFICATE OF SPORTS MERIT & ACHIEVEMENT");
                    txtSignatory1.setText("Coach Rajesh Singh (Sports In-Charge)");
                    txtRemarks.setText("For exceptional athletic prowess, sportsmanship, and outstanding victory in the Inter-College Annual Sports Championship.");
                } else if (sel.contains("Cultural")) {
                    comboCertTitle.setSelectedItem("CERTIFICATE OF CULTURAL EXCELLENCE");
                    txtSignatory1.setText("Prof. Meera Sen (Cultural Convener)");
                    txtRemarks.setText("In recognition of outstanding artistic flair, stage performance, and exemplary contribution in the Annual Cultural Fest.");
                } else if (sel.contains("Tech")) {
                    comboCertTitle.setSelectedItem("CERTIFICATE OF TECHNICAL EXCELLENCE & INNOVATION");
                    txtSignatory1.setText("Dr. Vikash Roy (Tech Fest Coordinator)");
                    txtRemarks.setText("For innovative technical problem-solving, algorithmic excellence, and outstanding project execution at Tech Fest 2026.");
                } else {
                    comboCertTitle.setSelectedItem("CERTIFICATE OF ACADEMIC EXCELLENCE");
                    txtSignatory1.setText("Prof. Parmanand Prasad (BCA In-charge / HOD)");
                    txtRemarks.setText("In recognition of outstanding academic performance, exemplary dedication, and the highest standards of scholastic achievement.");
                }
            }
            updatePreview();
        });
        formFields.add(comboTemplate);
        formFields.add(Box.createVerticalStrut(10));

        // 3. University Name
        formFields.add(createFieldLabel("University Name:"));
        txtUniName = new JTextField(UIUtils.getActiveUniversityName());
        txtUniName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        addLiveListener(txtUniName);
        formFields.add(txtUniName);
        formFields.add(Box.createVerticalStrut(10));

        // 4. College / Department Name
        formFields.add(createFieldLabel("College / Institute Name:"));
        txtCollegeName = new JTextField(UIUtils.getActiveCollegeName());
        txtCollegeName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        addLiveListener(txtCollegeName);
        formFields.add(txtCollegeName);
        formFields.add(Box.createVerticalStrut(10));

        // 5. Certificate Title
        formFields.add(createFieldLabel("Certificate Title / Award Type:"));
        comboCertTitle = new JComboBox<>(new String[]{
                "CERTIFICATE OF ACADEMIC EXCELLENCE",
                "DEGREE OF BACHELOR OF COMPUTER APPLICATIONS",
                "DEGREE OF MASTER OF COMPUTER APPLICATIONS",
                "CERTIFICATE OF CULTURAL EXCELLENCE",
                "CERTIFICATE OF SPORTS MERIT & ACHIEVEMENT",
                "CERTIFICATE OF TECHNICAL EXCELLENCE & INNOVATION",
                "CERTIFICATE OF MERIT & DISTINCTION",
                "CERTIFICATE OF PARTICIPATION & APPRECIATION"
        });
        comboCertTitle.setEditable(true);
        comboCertTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        comboCertTitle.addActionListener(e -> updatePreview());
        formFields.add(comboCertTitle);
        formFields.add(Box.createVerticalStrut(10));

        // 6. Issue Date
        formFields.add(createFieldLabel("Date of Issue:"));
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        txtIssueDate = new JTextField(sdf.format(new Date()));
        txtIssueDate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        addLiveListener(txtIssueDate);
        formFields.add(txtIssueDate);
        formFields.add(Box.createVerticalStrut(10));

        // 7. Signatories
        formFields.add(createFieldLabel("Left Signatory (HOD / In-charge / Convener):"));
        txtSignatory1 = new JTextField("Prof. Parmanand Prasad (BCA In-charge / HOD)");
        txtSignatory1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        addLiveListener(txtSignatory1);
        formFields.add(txtSignatory1);
        formFields.add(Box.createVerticalStrut(10));

        formFields.add(createFieldLabel("Right Signatory (Principal):"));
        txtSignatory2 = new JTextField("Prof. (Dr.) Ramakant Prasad (Principal)");
        txtSignatory2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        addLiveListener(txtSignatory2);
        formFields.add(txtSignatory2);
        formFields.add(Box.createVerticalStrut(10));

        // 8. Remarks / Citation
        formFields.add(createFieldLabel("Citation / Remarks:"));
        txtRemarks = new JTextArea("In recognition of outstanding academic performance, exemplary dedication, and the highest standards of scholastic achievement.", 3, 20);
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);
        txtRemarks.setFont(UIUtils.FONT_REGULAR);
        txtRemarks.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        txtRemarks.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePreview();
            }
        });
        JScrollPane remarksScroll = new JScrollPane(txtRemarks);
        remarksScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        formFields.add(remarksScroll);

        JScrollPane formScrollPane = new JScrollPane(formFields);
        formScrollPane.setBorder(null);
        formScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formCard.add(formScrollPane, BorderLayout.CENTER);

        splitPane.setLeftComponent(formCard);

        // Right Preview Card with Zoom Bar
        JPanel previewCard = new JPanel(new BorderLayout(10, 10));
        previewCard.setBackground(UIUtils.CARD_BG);
        previewCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // Zoom Bar
        JPanel zoomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        zoomBar.setOpaque(false);
        JLabel lblZoom = new JLabel("Zoom:");
        lblZoom.setFont(UIUtils.FONT_REGULAR);
        lblZoom.setForeground(UIUtils.TEXT_MUTED);

        JButton btnZoomOut = new JButton("−");
        btnZoomOut.addActionListener(e -> zoomOut());
        JButton btnZoomReset = new JButton("100%");
        btnZoomReset.addActionListener(e -> previewPanel.setZoomFactor(1.0));
        JButton btnZoomIn = new JButton("+");
        btnZoomIn.addActionListener(e -> zoomIn());

        zoomBar.add(lblZoom);
        zoomBar.add(btnZoomOut);
        zoomBar.add(btnZoomReset);
        zoomBar.add(btnZoomIn);
        previewCard.add(zoomBar, BorderLayout.NORTH);

        previewPanel = new CertificatePreviewPanel();
        JScrollPane previewScroll = new JScrollPane(previewPanel);
        previewScroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        previewCard.add(previewScroll, BorderLayout.CENTER);

        splitPane.setRightComponent(previewCard);
        add(splitPane, BorderLayout.CENTER);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIUtils.FONT_BOLD);
        lbl.setForeground(UIUtils.TEXT_DARK);
        lbl.setBorder(new EmptyBorder(4, 0, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void addLiveListener(JTextField tf) {
        tf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePreview();
            }
        });
    }

    private double currentZoom = 1.0;

    private void zoomIn() {
        currentZoom = Math.min(1.8, currentZoom + 0.15);
        previewPanel.setZoomFactor(currentZoom);
    }

    private void zoomOut() {
        currentZoom = Math.max(0.6, currentZoom - 0.15);
        previewPanel.setZoomFactor(currentZoom);
    }

    public void refreshStudents() {
        Student currentSelected = (Student) comboStudent.getSelectedItem();
        comboStudent.removeAllItems();
        List<Student> students = DatabaseService.getInstance().getAllStudents();
        for (Student s : students) {
            comboStudent.addItem(s);
        }
        if (currentSelected != null) {
            for (int i = 0; i < comboStudent.getItemCount(); i++) {
                if (comboStudent.getItemAt(i).getId() == currentSelected.getId()) {
                    comboStudent.setSelectedIndex(i);
                    break;
                }
            }
        }
        updatePreview();
    }

    public void selectStudentForGeneration(Student s) {
        for (int i = 0; i < comboStudent.getItemCount(); i++) {
            if (comboStudent.getItemAt(i).getId() == s.getId()) {
                comboStudent.setSelectedIndex(i);
                break;
            }
        }
        updatePreview();
    }

    private Certificate buildCurrentCertificate() {
        Student st = (Student) comboStudent.getSelectedItem();
        Certificate c = new Certificate();
        String certNo = "CERT-" + (st != null ? st.getRollNo() : "2026") + "-" + (int) (Math.random() * 9000 + 1000);
        c.setCertificateNo(certNo);

        if (st != null) {
            c.setStudentId(st.getId());
            c.setStudentName(st.getName());
            c.setRollNo(st.getRollNo());
            c.setRegNo(st.getRegNo());
            c.setDepartment(st.getDepartmentCode());
            c.setClassName(st.getClassCode());
        } else {
            c.setStudentName("AAYUSH KUMAR");
            c.setRollNo("NC/BCA/23/001");
            c.setRegNo("23NCBCA0101");
            c.setDepartment("BCA");
            c.setClassName("BCA-3");
        }

        c.setUniversityName(txtUniName.getText().trim());
        c.setCollegeName(txtCollegeName.getText().trim());
        c.setCertificateTitle(comboCertTitle.getSelectedItem() != null ? comboCertTitle.getSelectedItem().toString() : "CERTIFICATE OF EXCELLENCE");
        c.setRemarks(txtRemarks.getText().trim());
        c.setIssueDate(txtIssueDate.getText().trim());
        c.setSignatory1(txtSignatory1.getText().trim());
        c.setSignatory2(txtSignatory2.getText().trim());
        c.setTemplateName((String) comboTemplate.getSelectedItem());

        return c;
    }

    public void updatePreview() {
        Certificate c = buildCurrentCertificate();
        previewPanel.setCertificate(c);
    }

    private void saveAndIssueCertificate() {
        Student st = (Student) comboStudent.getSelectedItem();
        if (st == null) {
            JOptionPane.showMessageDialog(this, "Please select or add a student first.", "Student Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Certificate cert = buildCurrentCertificate();
        if (DatabaseService.getInstance().insertCertificate(cert)) {
            JOptionPane.showMessageDialog(this,
                    "Certificate issued successfully!\nCertificate ID: " + cert.getCertificateNo() + "\nStudent: " + cert.getStudentName(),
                    "Issued Successfully",
                    JOptionPane.INFORMATION_MESSAGE);
            mainFrame.refreshAllViews();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save certificate record.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPdf() {
        Certificate cert = buildCurrentCertificate();
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(String.format("Certificate_%s_%s.pdf", cert.getRollNo(), cert.getCertificateNo())));
        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            if (PdfExportService.exportCertificate(cert, file)) {
                int open = JOptionPane.showConfirmDialog(
                        this,
                        "PDF Certificate generated successfully!\nFile: " + file.getAbsolutePath() + "\n\nDo you want to open it now?",
                        "Export Successful",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                );
                if (open == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to generate PDF file.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportPng() {
        Certificate cert = buildCurrentCertificate();
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(String.format("Certificate_%s_%s.png", cert.getRollNo(), cert.getCertificateNo())));
        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }
            try {
                BufferedImage img = previewPanel.renderToImage();
                ImageIO.write(img, "PNG", file);
                int open = JOptionPane.showConfirmDialog(
                        this,
                        "High-resolution PNG image saved!\nFile: " + file.getAbsolutePath() + "\n\nDo you want to open it now?",
                        "Export Successful",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                );
                if (open == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving PNG: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showBatchGenerationDialog() {
        JDialog dlg = new JDialog(mainFrame, "Bulk Batch Certificate Generation", true);
        dlg.setSize(520, 420);
        dlg.setLocationRelativeTo(mainFrame);
        dlg.setLayout(new BorderLayout(15, 15));

        JPanel form = new JPanel(new GridLayout(5, 2, 12, 14));
        form.setBorder(new EmptyBorder(24, 24, 10, 24));

        JComboBox<SchoolClass> comboClass = new JComboBox<>();
        List<SchoolClass> classes = DatabaseService.getInstance().getAllClasses();
        for (SchoolClass c : classes) {
            comboClass.addItem(c);
        }

        JComboBox<String> comboTheme = new JComboBox<>(new String[]{
                "Academic Certificate",
                "Cultural Program Certificate",
                "Sports Certificate",
                "Tech Fest Certificate"
        });
        comboTheme.setSelectedItem(comboTemplate.getSelectedItem());

        JTextField txtFolder = new JTextField(System.getProperty("user.home") + File.separator + "Desktop");
        JButton btnBrowse = UIUtils.createSecondaryButton("Browse...");
        btnBrowse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                txtFolder.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel folderPanel = new JPanel(new BorderLayout(5, 0));
        folderPanel.setOpaque(false);
        folderPanel.add(txtFolder, BorderLayout.CENTER);
        folderPanel.add(btnBrowse, BorderLayout.EAST);

        JCheckBox chkOnlyPending = new JCheckBox("Only for Pending students", true);

        form.add(new JLabel("Select Class / Batch:"));
        form.add(comboClass);
        form.add(new JLabel("Template Theme:"));
        form.add(comboTheme);
        form.add(new JLabel("Output Folder:"));
        form.add(folderPanel);
        form.add(new JLabel("Filter:"));
        form.add(chkOnlyPending);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        btnPanel.setBorder(new EmptyBorder(0, 24, 15, 24));

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnStart = UIUtils.createPrimaryButton("Start Bulk Generation");
        btnStart.addActionListener(e -> {
            SchoolClass selectedClass = (SchoolClass) comboClass.getSelectedItem();
            if (selectedClass == null) {
                JOptionPane.showMessageDialog(dlg, "Please select a class.", "Class Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            File outDir = new File(txtFolder.getText().trim());
            if (!outDir.exists()) {
                outDir.mkdirs();
            }

            List<Student> allStudents = DatabaseService.getInstance().getAllStudents();
            List<Student> targets = allStudents.stream()
                    .filter(s -> s.getClassCode().equalsIgnoreCase(selectedClass.getClassCode()))
                    .filter(s -> !chkOnlyPending.isSelected() || !"Issued".equalsIgnoreCase(s.getCertificateStatus()))
                    .toList();

            if (targets.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "No matching students found for class: " + selectedClass.getClassCode(), "No Students", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int success = 0;
            String templateName = (String) comboTheme.getSelectedItem();
            String uni = txtUniName.getText().trim();
            String col = txtCollegeName.getText().trim();
            String title = comboCertTitle.getSelectedItem() != null ? comboCertTitle.getSelectedItem().toString() : "CERTIFICATE OF EXCELLENCE";
            String date = txtIssueDate.getText().trim();
            String sig1 = txtSignatory1.getText().trim();
            String sig2 = txtSignatory2.getText().trim();
            String remarks = txtRemarks.getText().trim();

            for (Student st : targets) {
                Certificate cert = new Certificate();
                cert.setCertificateNo("CERT-" + st.getRollNo() + "-" + (int) (Math.random() * 9000 + 1000));
                cert.setStudentId(st.getId());
                cert.setStudentName(st.getName());
                cert.setRollNo(st.getRollNo());
                cert.setRegNo(st.getRegNo());
                cert.setDepartment(st.getDepartmentCode());
                cert.setClassName(st.getClassCode());
                cert.setUniversityName(uni);
                cert.setCollegeName(col);
                cert.setCertificateTitle(title);
                cert.setIssueDate(date);
                cert.setSignatory1(sig1);
                cert.setSignatory2(sig2);
                cert.setRemarks(remarks);
                cert.setTemplateName(templateName);

                if (DatabaseService.getInstance().insertCertificate(cert)) {
                    File pdfFile = new File(outDir, String.format("Certificate_%s_%s.pdf", st.getRollNo(), cert.getCertificateNo()));
                    if (PdfExportService.exportCertificate(cert, pdfFile)) {
                        success++;
                    }
                }
            }

            dlg.dispose();
            mainFrame.refreshAllViews();

            int open = JOptionPane.showConfirmDialog(
                    mainFrame,
                    String.format("Batch Generation Completed!\n- Successfully generated & issued: %d / %d certificates\n- Saved to: %s\n\nDo you want to open the output folder now?", success, targets.size(), outDir.getAbsolutePath()),
                    "Batch Complete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (open == JOptionPane.YES_OPTION) {
                try {
                    Desktop.getDesktop().open(outDir);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnStart);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
