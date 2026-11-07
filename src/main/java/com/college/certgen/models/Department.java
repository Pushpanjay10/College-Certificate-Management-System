package com.college.certgen.models;

public class Department {
    private int id;
    private String code;
    private String name;
    private String hodName;
    private String description;

    public Department() {}

    public Department(int id, String code, String name, String hodName, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.hodName = hodName;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHodName() { return hodName; }
    public void setHodName(String hodName) { this.hodName = hodName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
