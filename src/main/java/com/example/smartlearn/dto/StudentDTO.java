package com.example.smartlearn.dto;

public class StudentDTO {
    private Long studentId;
    private String studentName;
    // 可以添加其他需要的字段

    // Constructors, getters and setters
    public StudentDTO(Long studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
    }

    // Getters and setters
    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}
