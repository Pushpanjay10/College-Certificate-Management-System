package com.college.certgen.ui;

import com.college.certgen.database.DatabaseService;
import com.college.certgen.models.Department;
import com.college.certgen.models.SchoolClass;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClassView extends JPanel {
    private final MainFrame mainFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> comboDeptFilter;
    private List<SchoolClass> classList = new ArrayList<>();

    public ClassView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIUtils.BACKGROUND);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Classes & Courses");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.TEXT_DARK);
        JLabel subtitle = new JLabel("Organize class sections, semesters, and course departments");
        subtitle.setFont(UIUtils.FONT_REGULAR);
        subtitle.setForeground(UIUtils.TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(subtitle);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsBox.setOpaque(false);

        JButton btnAdd = UIUtils.createPrimaryButton("+ Add Class");
        btnAdd.addActionListener(e -> showAddEditDialog(null));
        actionsBox.add(btnAdd);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(actionsBox, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Card with Filter, Search & Table
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
        txtSearch = new JTextField(18);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search class name or code...");
        txtSearch.addCaretListener(e -> filterData());

        JLabel lblDept = new JLabel("Department:");
        lblDept.setFont(UIUtils.FONT_BOLD);
        comboDeptFilter = new JComboBox<>();
        comboDeptFilter.addItem("All Departments");
        comboDeptFilter.addActionListener(e -> filterData());

        leftFilters.add(lblSearch);
        leftFilters.add(txtSearch);
        leftFilters.add(lblDept);
        leftFilters.add(comboDeptFilter);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JButton btnEdit = UIUtils.createSecondaryButton("Edit Selected");
        btnEdit.addActionListener(e -> editSelected());

        JButton btnDelete = UIUtils.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteSelected());

        JButton btnRefresh = UIUtils.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        rightControls.add(btnEdit);
        rightControls.add(btnDelete);
        rightControls.add(btnRefresh);

        searchBar.add(leftFilters, BorderLayout.WEST);
        searchBar.add(rightControls, BorderLayout.EAST);
        card.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Class Code", "Class Name", "Department", "Semester", "Academic Year"};
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
        classList = DatabaseService.getInstance().getAllClasses();

        // Populate Dept filter
        comboDeptFilter.removeAllItems();
        comboDeptFilter.addItem("All Departments");
        List<Department> depts = DatabaseService.getInstance().getAllDepartments();
        for (Department d : depts) {
            comboDeptFilter.addItem(d.getCode());
        }

        filterData();
    }

    private void filterData() {
        String query = txtSearch.getText().trim().toLowerCase();
        String selectedDept = (String) comboDeptFilter.getSelectedItem();
        boolean filterDept = selectedDept != null && !selectedDept.equals("All Departments");

        tableModel.setRowCount(0);
        for (SchoolClass sc : classList) {
            boolean matchesSearch = query.isEmpty() ||
                    sc.getClassName().toLowerCase().contains(query) ||
                    sc.getClassCode().toLowerCase().contains(query) ||
                    sc.getDepartmentCode().toLowerCase().contains(query);

            boolean matchesDept = !filterDept || sc.getDepartmentCode().equalsIgnoreCase(selectedDept);

            if (matchesSearch && matchesDept) {
                tableModel.addRow(new Object[]{
                        sc.getId(),
                        sc.getClassCode(),
                        sc.getClassName(),
                        sc.getDepartmentCode(),
                        sc.getSemester(),
                        sc.getAcademicYear()
                });
            }
        }
    }

    private void editSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        SchoolClass target = classList.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
        if (target != null) {
            showAddEditDialog(target);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete class '" + name + "'?\nThis action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (DatabaseService.getInstance().deleteClass(id)) {
                JOptionPane.showMessageDialog(this, "Class deleted successfully.");
                refreshData();
                mainFrame.refreshAllViews();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete class.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddEditDialog(SchoolClass cls) {
        boolean isEdit = (cls != null);
        JDialog dlg = new JDialog(mainFrame, isEdit ? "Edit Class" : "Add New Class", true);
        dlg.setSize(480, 420);
        dlg.setLocationRelativeTo(mainFrame);
        dlg.setLayout(new BorderLayout(15, 15));

        JPanel form = new JPanel(new GridLayout(5, 2, 12, 14));
        form.setBorder(new EmptyBorder(20, 24, 10, 24));

        JTextField txtCode = new JTextField(isEdit ? cls.getClassCode() : "");
        JTextField txtName = new JTextField(isEdit ? cls.getClassName() : "");

        JComboBox<Department> comboDept = new JComboBox<>();
        List<Department> depts = DatabaseService.getInstance().getAllDepartments();
        for (Department d : depts) {
            comboDept.addItem(d);
            if (isEdit && d.getCode().equalsIgnoreCase(cls.getDepartmentCode())) {
                comboDept.setSelectedItem(d);
            }
        }

        JTextField txtSem = new JTextField(isEdit ? cls.getSemester() : "Sem 6");
        JTextField txtYear = new JTextField(isEdit ? cls.getAcademicYear() : "2025-2026");

        form.add(new JLabel("Class Code (e.g. CS-3A):"));
        form.add(txtCode);
        form.add(new JLabel("Class / Degree Name:"));
        form.add(txtName);
        form.add(new JLabel("Department:"));
        form.add(comboDept);
        form.add(new JLabel("Semester / Trimester:"));
        form.add(txtSem);
        form.add(new JLabel("Academic Year:"));
        form.add(txtYear);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        btnPanel.setBorder(new EmptyBorder(0, 24, 15, 24));

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UIUtils.createPrimaryButton(isEdit ? "Update Class" : "Save Class");
        btnSave.addActionListener(e -> {
            String code = txtCode.getText().trim();
            String name = txtName.getText().trim();
            Department dept = (Department) comboDept.getSelectedItem();

            if (code.isEmpty() || name.isEmpty() || dept == null) {
                JOptionPane.showMessageDialog(dlg, "Class Code, Name, and Department are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                cls.setClassCode(code);
                cls.setClassName(name);
                cls.setDepartmentCode(dept.getCode());
                cls.setSemester(txtSem.getText().trim());
                cls.setAcademicYear(txtYear.getText().trim());
                if (DatabaseService.getInstance().updateClass(cls)) {
                    dlg.dispose();
                    refreshData();
                    mainFrame.refreshAllViews();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Error updating class.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                SchoolClass newCls = new SchoolClass(0, code, name, dept.getCode(), txtSem.getText().trim(), txtYear.getText().trim());
                if (DatabaseService.getInstance().insertClass(newCls)) {
                    dlg.dispose();
                    refreshData();
                    mainFrame.refreshAllViews();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Error creating class. Code might be duplicate.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}

