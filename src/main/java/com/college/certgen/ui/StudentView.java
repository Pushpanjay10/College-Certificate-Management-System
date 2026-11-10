package com.college.certgen.ui;

import com.college.certgen.database.DatabaseService;
import com.college.certgen.models.Department;
import com.college.certgen.models.SchoolClass;
import com.college.certgen.models.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StudentView extends JPanel {
    private final MainFrame mainFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> comboDeptFilter;
    private JComboBox<String> comboClassFilter;
    private List<Student> studentList = new ArrayList<>();

    public StudentView(MainFrame mainFrame) {
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
        JLabel title = new JLabel("Student Directory");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.TEXT_DARK);
        JLabel subtitle = new JLabel("Enroll students, manage identity records, bulk import CSV, and track certificates");
        subtitle.setFont(UIUtils.FONT_REGULAR);
        subtitle.setForeground(UIUtils.TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(subtitle);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsBox.setOpaque(false);

        JButton btnImportCsv = UIUtils.createSecondaryButton("Import CSV");
        btnImportCsv.addActionListener(e -> importFromCsv());

        JButton btnExportCsv = UIUtils.createSecondaryButton("Export CSV");
        btnExportCsv.addActionListener(e -> exportToCsv());

        JButton btnAdd = UIUtils.createPrimaryButton("+ Add Student");
        btnAdd.addActionListener(e -> showAddEditDialog(null));

        actionsBox.add(btnImportCsv);
        actionsBox.add(btnExportCsv);
        actionsBox.add(btnAdd);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(actionsBox, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Card with Filters & Table
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(UIUtils.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Filter / Search Bar
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UIUtils.FONT_BOLD);
        txtSearch = new JTextField(14);
        txtSearch.putClientProperty("JTextField.placeholderText", "Name, Roll No, Reg No...");
        txtSearch.addCaretListener(e -> filterData());

        JLabel lblDept = new JLabel("Dept:");
        lblDept.setFont(UIUtils.FONT_BOLD);
        comboDeptFilter = new JComboBox<>();
        comboDeptFilter.addItem("All Departments");
        comboDeptFilter.addActionListener(e -> filterData());

        JLabel lblClass = new JLabel("Class:");
        lblClass.setFont(UIUtils.FONT_BOLD);
        comboClassFilter = new JComboBox<>();
        comboClassFilter.addItem("All Classes");
        comboClassFilter.addActionListener(e -> filterData());

        leftFilters.add(lblSearch);
        leftFilters.add(txtSearch);
        leftFilters.add(lblDept);
        leftFilters.add(comboDeptFilter);
        leftFilters.add(lblClass);
        leftFilters.add(comboClassFilter);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JButton btnGenCert = UIUtils.createPrimaryButton("Issue Certificate");
        btnGenCert.addActionListener(e -> issueCertForSelected());

        JButton btnEdit = UIUtils.createSecondaryButton("Edit");
        btnEdit.addActionListener(e -> editSelected());

        JButton btnDelete = UIUtils.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteSelected());

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        rightControls.add(btnGenCert);
        rightControls.add(btnEdit);
        rightControls.add(btnDelete);
        rightControls.add(btnRefresh);

        searchBar.add(leftFilters, BorderLayout.WEST);
        searchBar.add(rightControls, BorderLayout.EAST);
        card.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Roll No", "Full Name", "Reg No", "Dept", "Class", "Email", "Phone", "Cert Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.setRowHeight(36);

        // Custom status badge renderer
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFocus, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(UIUtils.FONT_BOLD);
                String status = val != null ? val.toString() : "";
                if ("Issued".equalsIgnoreCase(status)) {
                    lbl.setForeground(new Color(16, 185, 129));
                    lbl.setText("● Issued");
                } else {
                    lbl.setForeground(new Color(234, 88, 12));
                    lbl.setText("○ Pending");
                }
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void refreshData() {
        studentList = DatabaseService.getInstance().getAllStudents();

        // Populate Dept dropdown
        comboDeptFilter.removeAllItems();
        comboDeptFilter.addItem("All Departments");
        for (Department d : DatabaseService.getInstance().getAllDepartments()) {
            comboDeptFilter.addItem(d.getCode());
        }

        // Populate Class dropdown
        comboClassFilter.removeAllItems();
        comboClassFilter.addItem("All Classes");
        for (SchoolClass c : DatabaseService.getInstance().getAllClasses()) {
            comboClassFilter.addItem(c.getClassCode());
        }

        filterData();
    }

    private void filterData() {
        String query = txtSearch.getText().trim().toLowerCase();
        String selectedDept = (String) comboDeptFilter.getSelectedItem();
        String selectedClass = (String) comboClassFilter.getSelectedItem();

        boolean filterDept = selectedDept != null && !selectedDept.equals("All Departments");
        boolean filterClass = selectedClass != null && !selectedClass.equals("All Classes");

        tableModel.setRowCount(0);
        for (Student s : studentList) {
            boolean matchesSearch = query.isEmpty() ||
                    s.getName().toLowerCase().contains(query) ||
                    s.getRollNo().toLowerCase().contains(query) ||
                    s.getRegNo().toLowerCase().contains(query) ||
                    s.getEmail().toLowerCase().contains(query);

            boolean matchesDept = !filterDept || s.getDepartmentCode().equalsIgnoreCase(selectedDept);
            boolean matchesClass = !filterClass || s.getClassCode().equalsIgnoreCase(selectedClass);

            if (matchesSearch && matchesDept && matchesClass) {
                tableModel.addRow(new Object[]{
                        s.getId(),
                        s.getRollNo(),
                        s.getName(),
                        s.getRegNo(),
                        s.getDepartmentCode(),
                        s.getClassCode(),
                        s.getEmail(),
                        s.getPhone(),
                        s.getCertificateStatus()
                });
            }
        }
    }

    private void issueCertForSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to generate certificate.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        Student s = studentList.stream().filter(st -> st.getId() == id).findFirst().orElse(null);
        if (s != null) {
            mainFrame.getCertGenView().selectStudentForGeneration(s);
            mainFrame.showPage("cert_gen");
        }
    }

    private void editSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        Student target = studentList.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
        if (target != null) {
            showAddEditDialog(target);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete student '" + name + "'?\nThis action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (DatabaseService.getInstance().deleteStudent(id)) {
                JOptionPane.showMessageDialog(this, "Student deleted successfully.");
                refreshData();
                mainFrame.refreshAllViews();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete student.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddEditDialog(Student st) {
        boolean isEdit = (st != null);
        JDialog dlg = new JDialog(mainFrame, isEdit ? "Edit Student Record" : "Enroll New Student", true);
        dlg.setSize(520, 500);
        dlg.setLocationRelativeTo(mainFrame);
        dlg.setLayout(new BorderLayout(15, 15));

        JPanel form = new JPanel(new GridLayout(7, 2, 12, 12));
        form.setBorder(new EmptyBorder(20, 24, 10, 24));

        JTextField txtRoll = new JTextField(isEdit ? st.getRollNo() : "");
        JTextField txtName = new JTextField(isEdit ? st.getName() : "");
        JTextField txtReg = new JTextField(isEdit ? st.getRegNo() : "");

        JComboBox<Department> comboDept = new JComboBox<>();
        List<Department> depts = DatabaseService.getInstance().getAllDepartments();
        for (Department d : depts) {
            comboDept.addItem(d);
            if (isEdit && d.getCode().equalsIgnoreCase(st.getDepartmentCode())) {
                comboDept.setSelectedItem(d);
            }
        }

        JComboBox<SchoolClass> comboClass = new JComboBox<>();
        List<SchoolClass> classes = DatabaseService.getInstance().getAllClasses();
        for (SchoolClass c : classes) {
            comboClass.addItem(c);
            if (isEdit && c.getClassCode().equalsIgnoreCase(st.getClassCode())) {
                comboClass.setSelectedItem(c);
            }
        }

        JTextField txtEmail = new JTextField(isEdit ? st.getEmail() : "");
        JTextField txtPhone = new JTextField(isEdit ? st.getPhone() : "");

        form.add(new JLabel("Full Student Name:"));
        form.add(txtName);
        form.add(new JLabel("College Roll No:"));
        form.add(txtRoll);
        form.add(new JLabel("University Reg No:"));
        form.add(txtReg);
        form.add(new JLabel("Department:"));
        form.add(comboDept);
        form.add(new JLabel("Class / Degree:"));
        form.add(comboClass);
        form.add(new JLabel("Email Address:"));
        form.add(txtEmail);
        form.add(new JLabel("Phone / Mobile:"));
        form.add(txtPhone);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        btnPanel.setBorder(new EmptyBorder(0, 24, 15, 24));

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UIUtils.createPrimaryButton(isEdit ? "Update Record" : "Enroll Student");
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String roll = txtRoll.getText().trim();
            String reg = txtReg.getText().trim();
            Department dept = (Department) comboDept.getSelectedItem();
            SchoolClass cls = (SchoolClass) comboClass.getSelectedItem();

            if (name.isEmpty() || roll.isEmpty() || reg.isEmpty() || dept == null || cls == null) {
                JOptionPane.showMessageDialog(dlg, "Name, Roll No, Reg No, Department and Class are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                st.setName(name);
                st.setRollNo(roll);
                st.setRegNo(reg);
                st.setDepartmentCode(dept.getCode());
                st.setClassCode(cls.getClassCode());
                st.setEmail(txtEmail.getText().trim());
                st.setPhone(txtPhone.getText().trim());
                if (DatabaseService.getInstance().updateStudent(st)) {
                    dlg.dispose();
                    refreshData();
                    mainFrame.refreshAllViews();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Error updating student record.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                Student newSt = new Student(0, roll, name, reg, dept.getCode(), cls.getClassCode(), txtEmail.getText().trim(), txtPhone.getText().trim());
                if (DatabaseService.getInstance().insertStudent(newSt)) {
                    dlg.dispose();
                    refreshData();
                    mainFrame.refreshAllViews();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Unable to enroll student. Please check the entered details.", "Database Notice", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void importFromCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Student CSV File to Import");
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            int inserted = 0;
            int skipped = 0;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                boolean isFirstLine = true;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] tokens = line.split(",");
                    if (tokens.length < 5) continue;

                    // Skip header row if present
                    if (isFirstLine && (tokens[0].trim().equalsIgnoreCase("roll_no") || tokens[0].trim().equalsIgnoreCase("rollno"))) {
                        isFirstLine = false;
                        continue;
                    }
                    isFirstLine = false;

                    String roll = tokens[0].trim();
                    String name = tokens[1].trim();
                    String reg = tokens[2].trim();
                    String dept = tokens[3].trim();
                    String cls = tokens[4].trim();
                    String email = tokens.length > 5 ? tokens[5].trim() : "";
                    String phone = tokens.length > 6 ? tokens[6].trim() : "";

                    Student st = new Student(0, roll, name, reg, dept, cls, email, phone);
                    if (DatabaseService.getInstance().insertStudent(st)) {
                        inserted++;
                    } else {
                        skipped++;
                    }
                }

                JOptionPane.showMessageDialog(this,
                        String.format("CSV Import Finished!\n- Successfully imported: %d student(s)\n- Skipped / Duplicates: %d", inserted, skipped),
                        "CSV Import Summary",
                        JOptionPane.INFORMATION_MESSAGE);

                refreshData();
                mainFrame.refreshAllViews();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading CSV: " + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("students_export.csv"));
        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                pw.println("id,roll_no,name,reg_no,department,class,email,phone,certificate_status");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object val = tableModel.getValueAt(i, j);
                        String s = val != null ? val.toString().replace("\"", "\"\"") : "";
                        sb.append("\"").append(s).append("\"");
                        if (j < tableModel.getColumnCount() - 1) sb.append(",");
                    }
                    pw.println(sb.toString());
                }
                JOptionPane.showMessageDialog(this, "Exported " + tableModel.getRowCount() + " students to:\n" + file.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
