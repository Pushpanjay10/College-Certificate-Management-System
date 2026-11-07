package com.college.certgen.models;

public class SchoolClass {
    private int id;
    private String classCode;
    private String className;
    private String departmentCode;
    private String semester;
    private String academicYear;

    public SchoolClass() {}

    public SchoolClass(int id, String classCode, String className, String departmentCode, String semester, String academicYear) {
        this.id = id;
        this.classCode = classCode;
        this.className = className;
        this.departmentCode = departmentCode;
        this.semester = semester;
        this.academicYear = academicYear;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    @Override
    public String toString() {
        return classCode + " (" + className + ")";
    }
}
