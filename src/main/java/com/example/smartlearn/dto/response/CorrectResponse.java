package com.example.smartlearn.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CorrectResponse {
    private Long submissionId;
    private Long studentId;
    private String studentName;
    private LocalDateTime submittedAt;
    private BigDecimal grade;
    private String feedback;
    private String submissionType; // 新增：提交类型 "TEXT", "FILE", "BOTH"
    private String fileName; // 新增：文件名（如果有文件）

    public CorrectResponse(Long submissionId, Long studentId, String studentName, LocalDateTime submittedAt, BigDecimal grade, String feedback) {
        this.submissionId = submissionId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.submittedAt = submittedAt;
        this.grade = grade;
        this.feedback = feedback;
    }

    public CorrectResponse(Long submissionId, Long studentId, String studentName, LocalDateTime submittedAt, BigDecimal grade, String feedback, String submissionType, String fileName) {
        this.submissionId = submissionId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.submittedAt = submittedAt;
        this.grade = grade;
        this.feedback = feedback;
        this.submissionType = submissionType;
        this.fileName = fileName;
    }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public BigDecimal getGrade() { return grade; }
    public void setGrade(BigDecimal grade) { this.grade = grade; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getSubmissionType() { return submissionType; }
    public void setSubmissionType(String submissionType) { this.submissionType = submissionType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}