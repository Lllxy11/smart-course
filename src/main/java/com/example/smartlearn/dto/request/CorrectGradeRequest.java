package com.example.smartlearn.dto.request;

import java.math.BigDecimal;

public class CorrectGradeRequest {
    private BigDecimal grade;
    private String feedback;

    public BigDecimal getGrade() { return grade; }
    public void setGrade(BigDecimal grade) { this.grade = grade; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
} 