package com.college.certgen.models;

public class Student {
    private int id;
    private String rollNo;
    private String name;
    private String regNo;
    private String departmentCode;
    private String classCode;
    private String email;
    private String phone;
    private String certificateStatus; // "Pending" or "Issued"
    private String certificateDate;
    private String collegeName;

    public Student() {
        this.certificateStatus = "Pending";
    }

    public Student(int id, String rollNo, String name, String regNo, String departmentCode, String classCode, String email, String phone) {
        this.id = id;
        this.rollNo = rollNo;
        this.name = name;
        this.regNo = regNo;
        this.departmentCode = departmentCode;
        this.classCode = classCode;
        this.email = email;
        this.phone = phone;
        this.certificateStatus = "Pending";
        this.certificateDate = "-";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCertificateStatus() { return certificateStatus; }
    public void setCertificateStatus(String certificateStatus) { this.certificateStatus = certificateStatus; }

    public String getCertificateDate() { return certificateDate; }
    public void setCertificateDate(String certificateDate) { this.certificateDate = certificateDate; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    @Override
    public String toString() {
        return name + " (" + rollNo + " / " + regNo + ")";
    }
}
