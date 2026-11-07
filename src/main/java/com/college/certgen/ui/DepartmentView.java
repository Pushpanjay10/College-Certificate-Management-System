package com.college.certgen.ui;

import com.college.certgen.database.DatabaseService;
import com.college.certgen.models.Department;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentView extends JPanel {
    private final MainFrame mainFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private List<Department> departmentList = new ArrayList<>();

    public DepartmentView(MainFrame mainFrame) {
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
        JLabel title = new JLabel("Academic Departments");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.TEXT_DARK);
        JLabel subtitle = new JLabel("Configure faculties, departments, and HOD assignments");
        subtitle.setFont(UIUtils.FONT_REGULAR);
        subtitle.setForeground(UIUtils.TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(subtitle);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsBox.setOpaque(false);

        JButton btnAdd = UIUtils.createPrimaryButton("+ Add Department");
        btnAdd.addActionListener(e -> showAddEditDialog(null));
        actionsBox.add(btnAdd);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(actionsBox, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Card with Search & Table
        JPanel card = UIUtils.createCard("");

        // Search Bar & Filter
        JPanel toolBar = new JPanel(new BorderLayout(12, 0));
        toolBar.setOpaque(false);
        toolBar.setBorder(new EmptyBorder(0, 0, 14, 0));

        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "🔍 Search department by code or name...");
        txtSearch.setFont(UIUtils.FONT_REGULAR);
        txtSearch.setPreferredSize(new Dimension(320, 36));
        txtSearch.addCaretListener(e -> filterData());

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchWrap.setOpaque(false);
        searchWrap.add(txtSearch);

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnWrap.setOpaque(false);

        JButton btnEdit = UIUtils.createSecondaryButton("✏️ Edit Selected");
        btnEdit.addActionListener(e -> editSelected());

        JButton btnDelete = UIUtils.createDangerButton("🗑️ Delete");
        btnDelete.addActionListener(e -> deleteSelected());

        btnWrap.add(btnEdit);
        btnWrap.add(btnDelete);

        toolBar.add(searchWrap, BorderLayout.WEST);
        toolBar.add(btnWrap, BorderLayout.EAST);

        card.add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Code", "Department Name", "Head of Department (HOD)", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void refreshData() {
        departmentList = DatabaseService.getInstance().getAllDepartments();
        filterData();
    }

    private void filterData() {
        String query = txtSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        for (Department d : departmentList) {
            if (query.isEmpty() || d.getCode().toLowerCase().contains(query) || d.getName().toLowerCase().contains(query)) {
                tableModel.addRow(new Object[]{
                        d.getId(),
                        d.getCode(),
                        d.getName(),
                        d.getHodName(),
                        d.getDescription()
                });
            }
        }
    }

    private void showAddEditDialog(Department existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog(mainFrame, isEdit ? "Edit Department" : "Add Department", true);
        dlg.setSize(440, 360);
        dlg.setLocationRelativeTo(mainFrame);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 14));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField txtCode = new JTextField(isEdit ? existing.getCode() : "");
        JTextField txtName = new JTextField(isEdit ? existing.getName() : "");
        JTextField txtHod = new JTextField(isEdit ? existing.getHodName() : "");
        JTextField txtDesc = new JTextField(isEdit ? existing.getDescription() : "");

        form.add(new JLabel("Department Code (e.g. CSE):"));
        form.add(txtCode);
        form.add(new JLabel("Full Department Name:"));
        form.add(txtName);
        form.add(new JLabel("Head of Department (HOD):"));
        form.add(txtHod);
        form.add(new JLabel("Description:"));
        form.add(txtDesc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setBackground(new Color(245, 247, 250));

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UIUtils.createPrimaryButton(isEdit ? "Update" : "Save");
        btnSave.addActionListener(e -> {
            String code = txtCode.getText().trim();
            String name = txtName.getText().trim();
            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Department Code and Name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit) {
                existing.setCode(code);
                existing.setName(name);
                existing.setHodName(txtHod.getText().trim());
                existing.setDescription(txtDesc.getText().trim());
                DatabaseService.getInstance().updateDepartment(existing);
            } else {
                Department dept = new Department(0, code, name, txtHod.getText().trim(), txtDesc.getText().trim());
                DatabaseService.getInstance().insertDepartment(dept);
            }
            refreshData();
            mainFrame.refreshAllViews();
            dlg.dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a department to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        for (Department d : departmentList) {
            if (d.getId() == id) {
                showAddEditDialog(d);
                break;
            }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a department to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        String name = (String) table.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete department '" + name + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseService.getInstance().deleteDepartment(id);
            refreshData();
            mainFrame.refreshAllViews();
        }
    }
}
