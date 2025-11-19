package com.example.smartlearn.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UngradedQuizAnswerResponse {
    private Long submissionId;
    private Long studentId;
    private String studentName;
    private Long questionId;
    private String questionTitle;
    private String studentAnswer;
    private LocalDateTime submittedAt;
}