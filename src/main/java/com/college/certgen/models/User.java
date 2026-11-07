package com.college.certgen.models;

public class User {
    private String username;
    private String password;
    private String fullName;
    private String role; // "Administrator", "Faculty"
    private String collegeName;
    private String universityName;
    private String collegeTagline;
    private String collegeLogoPath;

    public User() {}

    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public User(String username, String password, String fullName, String role, String collegeName, String universityName, String collegeTagline, String collegeLogoPath) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.collegeName = collegeName;
        this.universityName = universityName;
        this.collegeTagline = collegeTagline;
        this.collegeLogoPath = collegeLogoPath;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCollegeName() { return collegeName != null ? collegeName : ""; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getUniversityName() { return universityName != null ? universityName : ""; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getCollegeTagline() { return collegeTagline != null ? collegeTagline : ""; }
    public void setCollegeTagline(String collegeTagline) { this.collegeTagline = collegeTagline; }

    public String getCollegeLogoPath() { return collegeLogoPath != null ? collegeLogoPath : ""; }
    public void setCollegeLogoPath(String collegeLogoPath) { this.collegeLogoPath = collegeLogoPath; }
}
