package com.example.smartlearn.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeQuizShortAnswerRequest {
    private Long submissionId;
    private Long questionId;
    private BigDecimal score;
    private String feedback;
} 