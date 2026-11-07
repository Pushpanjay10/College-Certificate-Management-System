package com.college.certgen.models;

public class Certificate {
    private int id;
    private String certificateNo;
    private int studentId;
    private String studentName;
    private String rollNo;
    private String regNo;
    private String department;
    private String className;
    private String universityName;
    private String collegeName;
    private String certificateTitle;
    private String remarks;
    private String issueDate;
    private String signatory1;
    private String signatory2;
    private String templateName; // e.g. "Royal Gold", "Modern Indigo", "Emerald Honor", "Ruby Tech"

    public Certificate() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCertificateNo() { return certificateNo; }
    public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getCertificateTitle() { return certificateTitle; }
    public void setCertificateTitle(String certificateTitle) { this.certificateTitle = certificateTitle; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getSignatory1() { return signatory1; }
    public void setSignatory1(String signatory1) { this.signatory1 = signatory1; }

    public String getSignatory2() { return signatory2; }
    public void setSignatory2(String signatory2) { this.signatory2 = signatory2; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
}
